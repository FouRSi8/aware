package com.aware.app.statement

import com.aware.app.data.TransactionType
import org.apache.poi.EncryptedDocumentException
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.poifs.crypt.Decryptor
import org.apache.poi.poifs.crypt.EncryptionInfo
import org.apache.poi.poifs.filesystem.POIFSFileSystem
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.Workbook
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FilterInputStream
import java.io.InputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.security.MessageDigest
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import java.util.zip.ZipInputStream
import javax.xml.parsers.SAXParserFactory
import kotlin.math.abs

class StatementPasswordRequiredException : IllegalArgumentException("This statement needs a password")
class IncorrectStatementPasswordException : IllegalArgumentException("That password did not unlock the statement")

data class StatementCandidate(
    val rowNumber: Int,
    val occurredAt: Long,
    val amountPaise: Long,
    val type: TransactionType,
    val merchant: String,
    val reference: String?,
    val fingerprint: String,
    val warning: String? = null,
    val directionVerified: Boolean = true,
)

data class StatementParseResult(
    val fileName: String,
    val sheetName: String,
    val candidates: List<StatementCandidate>,
    val skippedRows: Int,
    val balanceChecks: Int,
    val balancedMatches: Int,
)

/**
 * Parses bank-exported CSV, XLS, and XLSX files without retaining the source file.
 * Amount and direction detection is deterministic; AI is deliberately not involved.
 */
class StatementImportParser(
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) {
    fun parse(fileName: String, bytes: ByteArray, password: CharArray? = null): StatementParseResult {
        require(bytes.isNotEmpty()) { "The selected statement is empty" }
        require(bytes.size <= MAX_FILE_BYTES) { "Statement files must be 15 MB or smaller" }
        return try {
            val sheets = if (fileName.lowercase(Locale.ROOT).endsWith(".csv")) {
                listOf(SheetData("CSV", parseCsv(bytes.decodeToString())))
            } else {
                parseWorkbook(bytes, password)
            }
            val selected = sheets.mapNotNull(::findTable).maxByOrNull { it.header.score }
                ?: throw IllegalArgumentException("Couldn’t find date, description, and amount columns")
            buildResult(fileName, selected)
        } finally {
            password?.fill('\u0000')
        }
    }

    private fun parseWorkbook(bytes: ByteArray, password: CharArray?): List<SheetData> {
        return when {
            bytes.startsWith(ZIP_MAGIC) -> parseXlsx(bytes)
            bytes.startsWith(OLE_MAGIC) -> parseOleWorkbook(bytes, password)
            else -> throw IllegalArgumentException("This is not a supported XLS or XLSX workbook")
        }
    }

    private fun parseOleWorkbook(bytes: ByteArray, password: CharArray?): List<SheetData> {
        return POIFSFileSystem(ByteArrayInputStream(bytes)).use { fileSystem ->
            if (fileSystem.root.hasEntry(Decryptor.DEFAULT_POIFS_ENTRY)) {
                if (password == null) throw StatementPasswordRequiredException()
                val decryptor = Decryptor.getInstance(EncryptionInfo(fileSystem))
                val accepted = runCatching { decryptor.verifyPassword(password.concatToString()) }.getOrDefault(false)
                if (!accepted) throw IncorrectStatementPasswordException()
                val decrypted = decryptor.getDataStream(fileSystem).use { it.readLimited(MAX_DECRYPTED_BYTES) }
                return@use try {
                    parseXlsx(decrypted)
                } finally {
                    decrypted.fill(0)
                }
            }
            Biff8EncryptionKey.setCurrentUserPassword(password?.concatToString())
            try {
                HSSFWorkbook(fileSystem, true).use(::workbookSheets)
            } catch (_: EncryptedDocumentException) {
                if (password == null) throw StatementPasswordRequiredException()
                throw IncorrectStatementPasswordException()
            } finally {
                Biff8EncryptionKey.setCurrentUserPassword(null)
            }
        }
    }

    private fun workbookSheets(workbook: Workbook): List<SheetData> {
        val formatter = DataFormatter(Locale.ENGLISH)
        return (0 until workbook.numberOfSheets).mapNotNull { sheetIndex ->
            val sheet = workbook.getSheetAt(sheetIndex)
            val rows = mutableListOf<List<String>>()
            val last = minOf(sheet.lastRowNum, MAX_ROWS + MAX_HEADER_SCAN)
            for (rowIndex in sheet.firstRowNum..last) {
                val row = sheet.getRow(rowIndex)
                val width = minOf((row?.lastCellNum?.toInt() ?: 0).coerceAtLeast(0), MAX_COLUMNS)
                rows += (0 until width).map { column ->
                    row?.getCell(column)?.let { formatter.formatCellValue(it) }.orEmpty().trim()
                }
            }
            SheetData(sheet.sheetName, rows).takeIf { data -> data.rows.any { row -> row.any(String::isNotBlank) } }
        }
    }

    private fun parseXlsx(bytes: ByteArray): List<SheetData> {
        val sharedStrings = mutableListOf<String>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                if (entry.name == "xl/sharedStrings.xml") {
                    val handler = SharedStringsHandler()
                    parseSecureXml(LimitedNonClosingInputStream(zip, MAX_XML_ENTRY_BYTES), handler)
                    sharedStrings += handler.values
                    break
                }
            }
        }
        val sheets = mutableListOf<SheetData>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            while (sheets.size < MAX_SHEETS) {
                val entry = zip.nextEntry ?: break
                if (entry.name.matches(XLSX_SHEET_PATH)) {
                    val handler = XlsxSheetHandler(sharedStrings)
                    parseSecureXml(LimitedNonClosingInputStream(zip, MAX_XML_ENTRY_BYTES), handler)
                    if (handler.rows.any { row -> row.any(String::isNotBlank) }) {
                        sheets += SheetData(entry.name.substringAfterLast('/').substringBeforeLast('.'), handler.rows)
                    }
                }
            }
        }
        require(sheets.isNotEmpty()) { "This XLSX file has no readable worksheets" }
        return sheets
    }

    private fun parseSecureXml(input: InputStream, handler: DefaultHandler) {
        val factory = SAXParserFactory.newInstance().apply {
            isNamespaceAware = true
            runCatching { setFeature("http://apache.org/xml/features/disallow-doctype-decl", true) }
            runCatching { setFeature("http://xml.org/sax/features/external-general-entities", false) }
            runCatching { setFeature("http://xml.org/sax/features/external-parameter-entities", false) }
        }
        factory.newSAXParser().parse(input, handler)
    }

    private fun InputStream.readLimited(limit: Int): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(16 * 1024)
        try {
            var total = 0
            while (true) {
                val read = read(buffer)
                if (read < 0) break
                total += read
                require(total <= limit) { "The decrypted workbook is too large" }
                output.write(buffer, 0, read)
            }
            return output.toByteArray()
        } finally {
            buffer.fill(0)
        }
    }

    private fun ByteArray.startsWith(prefix: ByteArray): Boolean =
        size >= prefix.size && prefix.indices.all { this[it] == prefix[it] }

    private class LimitedNonClosingInputStream(input: InputStream, private val limit: Int) : FilterInputStream(input) {
        private var count = 0
        override fun read(): Int = super.read().also { if (it >= 0) addCount(1) }
        override fun read(buffer: ByteArray, offset: Int, length: Int): Int =
            super.read(buffer, offset, length).also { if (it > 0) addCount(it) }
        override fun close() = Unit
        private fun addCount(value: Int) {
            count += value
            require(count <= limit) { "A worksheet is too large" }
        }
    }

    private class SharedStringsHandler : DefaultHandler() {
        val values = mutableListOf<String>()
        private var insideItem = false
        private var insideText = false
        private val current = StringBuilder()

        override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
            when (XmlHelpers.xmlName(localName, qName)) {
                "si" -> { insideItem = true; current.clear() }
                "t" -> if (insideItem) insideText = true
            }
        }

        override fun characters(chars: CharArray, start: Int, length: Int) {
            if (insideText && current.length < MAX_SHARED_STRING_LENGTH) {
                current.append(chars, start, minOf(length, MAX_SHARED_STRING_LENGTH - current.length))
            }
        }

        override fun endElement(uri: String?, localName: String?, qName: String?) {
            when (XmlHelpers.xmlName(localName, qName)) {
                "t" -> insideText = false
                "si" -> {
                    if (values.size < MAX_SHARED_STRINGS) values += current.toString()
                    insideItem = false
                }
            }
        }
    }

    private class XlsxSheetHandler(private val sharedStrings: List<String>) : DefaultHandler() {
        val rows = mutableListOf<List<String>>()
        private var row = mutableListOf<String>()
        private var column = 0
        private var type = ""
        private var captureValue = false
        private var captureInline = false
        private val value = StringBuilder()
        private val inline = StringBuilder()

        override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
            when (XmlHelpers.xmlName(localName, qName)) {
                "row" -> row = mutableListOf()
                "c" -> {
                    column = attributes?.getValue("r")?.let(::columnFromReference) ?: row.size
                    type = attributes?.getValue("t").orEmpty()
                    value.clear()
                    inline.clear()
                }
                "v" -> captureValue = true
                "t" -> if (type == "inlineStr") captureInline = true
            }
        }

        override fun characters(chars: CharArray, start: Int, length: Int) {
            if (captureValue && value.length < MAX_CELL_LENGTH) value.append(chars, start, minOf(length, MAX_CELL_LENGTH - value.length))
            if (captureInline && inline.length < MAX_CELL_LENGTH) inline.append(chars, start, minOf(length, MAX_CELL_LENGTH - inline.length))
        }

        override fun endElement(uri: String?, localName: String?, qName: String?) {
            when (XmlHelpers.xmlName(localName, qName)) {
                "v" -> captureValue = false
                "t" -> captureInline = false
                "c" -> if (column in 0 until MAX_COLUMNS) {
                    while (row.size <= column) row += ""
                    row[column] = when (type) {
                        "s" -> value.toString().toIntOrNull()?.let(sharedStrings::getOrNull).orEmpty()
                        "inlineStr" -> inline.toString()
                        "b" -> if (value.toString() == "1") "TRUE" else "FALSE"
                        else -> value.toString()
                    }.trim()
                }
                "row" -> if (rows.size < MAX_ROWS + MAX_HEADER_SCAN) rows += row.toList()
            }
        }

        private fun columnFromReference(reference: String): Int {
            var result = 0
            reference.takeWhile(Char::isLetter).forEach { letter -> result = result * 26 + (letter.uppercaseChar() - 'A' + 1) }
            return (result - 1).coerceAtLeast(0)
        }
    }

    private object XmlHelpers {
        fun xmlName(localName: String?, qName: String?): String = localName?.takeIf(String::isNotBlank) ?: qName.orEmpty().substringAfter(':')
    }

    private fun findTable(sheet: SheetData): TableData? {
        val scan = sheet.rows.take(MAX_HEADER_SCAN)
        val header = scan.mapIndexedNotNull { index, row -> classifyHeader(index, row) }.maxByOrNull { it.score }
            ?: return null
        if (header.score < 5 || header.date == null || header.description == null || (header.amount == null && header.debit == null && header.credit == null)) return null
        return TableData(sheet.name, header, sheet.rows.drop(header.rowIndex + 1))
    }

    private fun classifyHeader(rowIndex: Int, row: List<String>): HeaderMap? {
        var date: Int? = null
        var description: Int? = null
        var debit: Int? = null
        var credit: Int? = null
        var amount: Int? = null
        var direction: Int? = null
        var balance: Int? = null
        var reference: Int? = null
        row.forEachIndexed { index, raw ->
            val value = normalizeHeader(raw)
            when {
                value in DATE_HEADERS -> date = date ?: index
                value in DESCRIPTION_HEADERS -> description = description ?: index
                value in DEBIT_HEADERS || value.contains("withdrawal") || value.contains("debitamount") -> debit = debit ?: index
                value in CREDIT_HEADERS || value.contains("deposit") || value.contains("creditamount") -> credit = credit ?: index
                value in BALANCE_HEADERS || value.endsWith("balance") -> balance = balance ?: index
                value in DIRECTION_HEADERS -> direction = direction ?: index
                value in REFERENCE_HEADERS || value.contains("referenceno") || value.contains("transactionid") -> reference = reference ?: index
                value in AMOUNT_HEADERS || value == "transactionamount" -> amount = amount ?: index
            }
        }
        val score = (if (date != null) 2 else 0) + (if (description != null) 2 else 0) +
            (if (debit != null || credit != null || amount != null) 2 else 0) +
            (if (debit != null && credit != null) 2 else 0) + (if (direction != null) 1 else 0) +
            (if (balance != null) 1 else 0) + (if (reference != null) 1 else 0)
        return HeaderMap(rowIndex, date, description, debit, credit, amount, direction, balance, reference, score)
            .takeIf { score > 0 }
    }

    private fun buildResult(fileName: String, table: TableData): StatementParseResult {
        val parsedRows = table.rows.take(MAX_ROWS).mapIndexedNotNull { offset, values ->
            parseRow(table.header.rowIndex + offset + 2, values, table.header)
        }
        val chronological = parsedRows.firstOrNull()?.occurredAt.orZero() <= parsedRows.lastOrNull()?.occurredAt.orZero()
        var balanceChecks = 0
        var balanceMatches = 0
        val reconciled = parsedRows.mapIndexed { index, row ->
            if (row.type != null) return@mapIndexed row
            val neighbour = if (chronological) parsedRows.getOrNull(index - 1) else parsedRows.getOrNull(index + 1)
            val delta = neighbour?.balancePaise?.let { previous -> row.balancePaise?.minus(previous) }
            if (delta != null) {
                balanceChecks++
                if (abs(abs(delta) - row.amountPaise) <= BALANCE_TOLERANCE_PAISE) {
                    balanceMatches++
                    row.copy(type = typeForDirection(delta, row.description), directionVerified = true)
                } else row
            } else row
        }
        val occurrences = mutableMapOf<String, Int>()
        val candidates = reconciled.map { row ->
            val resolvedType = row.type ?: TransactionType.EXPENSE
            val base = listOf(
                Instant.ofEpochMilli(row.occurredAt).atZone(zoneId).toLocalDate().toString(),
                row.amountPaise.toString(),
                resolvedType.name,
                normalizeMerchant(row.description),
                row.reference.orEmpty().lowercase(Locale.ROOT),
            ).joinToString("|")
            val occurrence = occurrences.merge(base, 1, Int::plus) ?: 1
            StatementCandidate(
                rowNumber = row.rowNumber,
                occurredAt = row.occurredAt,
                amountPaise = row.amountPaise,
                type = resolvedType,
                merchant = row.description.take(MAX_MERCHANT_LENGTH),
                reference = row.reference,
                fingerprint = "statement:${sha256("$base|$occurrence")}",
                warning = when {
                    row.type == null -> "Debit or credit could not be verified"
                    resolvedType == TransactionType.TRANSFER -> "Choose the receiving account"
                    else -> null
                },
                directionVerified = row.type != null && row.directionVerified,
            )
        }
        return StatementParseResult(
            fileName = fileName,
            sheetName = table.sheetName,
            candidates = candidates,
            skippedRows = table.rows.take(MAX_ROWS).size - parsedRows.size,
            balanceChecks = balanceChecks,
            balancedMatches = balanceMatches,
        )
    }

    private fun parseRow(rowNumber: Int, values: List<String>, header: HeaderMap): ParsedRow? {
        fun value(index: Int?): String = index?.let { values.getOrNull(it) }.orEmpty().trim()
        val occurredAt = parseDate(value(header.date)) ?: return null
        val description = value(header.description).replace(Regex("\\s+"), " ").trim().takeIf(String::isNotBlank) ?: return null
        val debit = parseAmount(value(header.debit))
        val credit = parseAmount(value(header.credit))
        val genericRaw = value(header.amount)
        val generic = parseSignedAmount(genericRaw)
        val genericMarker = genericRaw.uppercase(Locale.ROOT).trim()
        val direction = normalizeHeader(value(header.direction))
        val reference = value(header.reference).takeIf(String::isNotBlank) ?: extractReference(description)
        val balance = parseAmount(value(header.balance))
        val explicit = when {
            debit != null && debit > 0 && (credit == null || credit == 0L) -> DirectionAmount(debit, typeForDirection(-debit, description), true)
            credit != null && credit > 0 && (debit == null || debit == 0L) -> DirectionAmount(credit, typeForDirection(credit, description), true)
            direction in DEBIT_VALUES && generic != null -> DirectionAmount(abs(generic), typeForDirection(-1, description), true)
            direction in CREDIT_VALUES && generic != null -> DirectionAmount(abs(generic), typeForDirection(1, description), true)
            genericMarker.endsWith("DR") && generic != null -> DirectionAmount(abs(generic), typeForDirection(-1, description), true)
            genericMarker.endsWith("CR") && generic != null -> DirectionAmount(abs(generic), typeForDirection(1, description), true)
            generic != null && generic < 0 -> DirectionAmount(abs(generic), typeForDirection(-1, description), true)
            generic != null -> DirectionAmount(abs(generic), null, false)
            else -> null
        } ?: return null
        if (explicit.amountPaise <= 0) return null
        return ParsedRow(rowNumber, occurredAt, explicit.amountPaise, explicit.type, description, reference, balance, explicit.verified)
    }

    private fun parseDate(raw: String): Long? {
        val text = raw.trim().replace(Regex("\\s+"), " ")
        if (text.isBlank()) return null
        text.toDoubleOrNull()?.takeIf { it in 20_000.0..100_000.0 }?.let { serial ->
            return LocalDate.of(1899, 12, 30).plusDays(serial.toLong()).atStartOfDay(zoneId).toInstant().toEpochMilli()
        }
        runCatching { return Instant.parse(text).toEpochMilli() }
        DATE_TIME_FORMATS.forEach { formatter ->
            try {
                return LocalDateTime.parse(text, formatter).atZone(zoneId).toInstant().toEpochMilli()
            } catch (_: DateTimeParseException) {
                // Try the next known bank-statement format.
            }
        }
        DATE_FORMATS.forEach { formatter ->
            try {
                return LocalDate.parse(text, formatter).atStartOfDay(zoneId).toInstant().toEpochMilli()
            } catch (_: DateTimeParseException) {
                // Try the next known bank-statement format.
            }
        }
        return null
    }

    private fun parseAmount(raw: String): Long? = parseSignedAmount(raw)?.let(::abs)

    private fun parseSignedAmount(raw: String): Long? {
        if (raw.isBlank() || raw.trim() in setOf("-", "—", "–")) return null
        val upper = raw.uppercase(Locale.ROOT).trim()
        val negative = upper.startsWith("-") || upper.startsWith("(") || upper.endsWith("DR")
        val numeric = upper.replace(",", "").replace(Regex("[^0-9.]"), "")
        if (numeric.isBlank() || numeric.count { it == '.' } > 1) return null
        return runCatching {
            val paise = BigDecimal(numeric).movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact()
            if (negative) -paise else paise
        }.getOrNull()
    }

    private fun typeForDirection(signedAmount: Long, description: String): TransactionType = when {
        SELF_TRANSFER.containsMatchIn(description) -> TransactionType.TRANSFER
        signedAmount > 0 && REFUND_WORDS.containsMatchIn(description) -> TransactionType.REFUND
        signedAmount > 0 -> TransactionType.INCOME
        else -> TransactionType.EXPENSE
    }

    private fun extractReference(description: String): String? = REFERENCE_PATTERN.find(description)
        ?.groupValues?.getOrNull(1)?.trim()?.take(48)

    private fun parseCsv(source: String): List<List<String>> {
        val rows = mutableListOf<MutableList<String>>()
        var row = mutableListOf<String>()
        val cell = StringBuilder()
        var quoted = false
        var index = 0
        while (index < source.length) {
            val char = source[index]
            when {
                char == '"' && quoted && source.getOrNull(index + 1) == '"' -> { cell.append('"'); index++ }
                char == '"' -> quoted = !quoted
                char == ',' && !quoted -> { row += cell.toString().trim(); cell.clear() }
                (char == '\n' || char == '\r') && !quoted -> {
                    if (char == '\r' && source.getOrNull(index + 1) == '\n') index++
                    row += cell.toString().trim(); cell.clear()
                    if (row.any(String::isNotBlank)) rows += row
                    row = mutableListOf()
                }
                else -> cell.append(char)
            }
            index++
        }
        row += cell.toString().trim()
        if (row.any(String::isNotBlank)) rows += row
        return rows
    }

    private fun normalizeHeader(value: String): String = value.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9]"), "")
    private fun normalizeMerchant(value: String): String = value.lowercase(Locale.ROOT)
        .replace(Regex("[a-z0-9._%+-]+@[a-z0-9.-]+"), " ")
        .replace(Regex("\\d"), " ")
        .replace(Regex("[^a-z ]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray())
        .joinToString("") { "%02x".format(it) }

    private data class SheetData(val name: String, val rows: List<List<String>>)
    private data class TableData(val sheetName: String, val header: HeaderMap, val rows: List<List<String>>)
    private data class HeaderMap(
        val rowIndex: Int,
        val date: Int?,
        val description: Int?,
        val debit: Int?,
        val credit: Int?,
        val amount: Int?,
        val direction: Int?,
        val balance: Int?,
        val reference: Int?,
        val score: Int,
    )
    private data class DirectionAmount(val amountPaise: Long, val type: TransactionType?, val verified: Boolean)
    private data class ParsedRow(
        val rowNumber: Int,
        val occurredAt: Long,
        val amountPaise: Long,
        val type: TransactionType?,
        val description: String,
        val reference: String?,
        val balancePaise: Long?,
        val directionVerified: Boolean,
    )

    private fun Long?.orZero() = this ?: 0L

    companion object {
        const val MAX_FILE_BYTES = 15 * 1024 * 1024
        private const val MAX_ROWS = 5_000
        private const val MAX_COLUMNS = 64
        private const val MAX_HEADER_SCAN = 25
        private const val MAX_MERCHANT_LENGTH = 180
        private const val BALANCE_TOLERANCE_PAISE = 2L
        private const val MAX_DECRYPTED_BYTES = 64 * 1024 * 1024
        private const val MAX_XML_ENTRY_BYTES = 32 * 1024 * 1024
        private const val MAX_SHEETS = 32
        private const val MAX_SHARED_STRINGS = 100_000
        private const val MAX_SHARED_STRING_LENGTH = 4_096
        private const val MAX_CELL_LENGTH = 4_096
        private val ZIP_MAGIC = byteArrayOf(0x50, 0x4B, 0x03, 0x04)
        private val OLE_MAGIC = byteArrayOf(
            0xD0.toByte(), 0xCF.toByte(), 0x11, 0xE0.toByte(), 0xA1.toByte(), 0xB1.toByte(), 0x1A, 0xE1.toByte(),
        )
        private val XLSX_SHEET_PATH = Regex("xl/worksheets/sheet\\d+\\.xml")

        private val DATE_HEADERS = setOf("date", "transactiondate", "txndate", "valuedate", "postingdate", "posteddate")
        private val DESCRIPTION_HEADERS = setOf("description", "narration", "particulars", "transactiondetails", "details", "remarks", "merchant", "payee")
        private val DEBIT_HEADERS = setOf("debit", "withdrawal", "withdrawals", "debitamount", "dramount")
        private val CREDIT_HEADERS = setOf("credit", "deposit", "deposits", "creditamount", "cramount")
        private val AMOUNT_HEADERS = setOf("amount", "transactionamount", "txnamount")
        private val BALANCE_HEADERS = setOf("balance", "closingbalance", "runningbalance", "availablebalance")
        private val DIRECTION_HEADERS = setOf("type", "transactiontype", "drcr", "debitcredit", "direction")
        private val REFERENCE_HEADERS = setOf("reference", "referencenumber", "refno", "transactionid", "txnid", "utr", "chequeno")
        private val DEBIT_VALUES = setOf("d", "dr", "debit", "withdrawal")
        private val CREDIT_VALUES = setOf("c", "cr", "credit", "deposit")

        private val REFUND_WORDS = Regex("\\b(refund|reversal|reversed|cashback|chargeback)\\b", RegexOption.IGNORE_CASE)
        private val SELF_TRANSFER = Regex("\\b(self transfer|own account|transfer to self)\\b", RegexOption.IGNORE_CASE)
        private val REFERENCE_PATTERN = Regex("(?:UTR|REF(?:ERENCE)?|TXN(?: ID)?)[\\s:#-]*([A-Z0-9-]{5,48})", RegexOption.IGNORE_CASE)
        private val DATE_FORMATS = listOf(
            "d/M/uuuu", "d/M/uu", "d-M-uuuu", "d-MMM-uuuu", "d-MMM-uu", "d.M.uuuu", "uuuu-MM-dd",
            "d MMM uuuu", "dd MMM uuuu", "d MMM, uuuu", "MMM d, uuuu", "M/d/uuuu", "M/d/uu",
        ).map { DateTimeFormatter.ofPattern(it, Locale.ENGLISH) }
        private val DATE_TIME_FORMATS = listOf(
            "d/M/uuuu H:mm", "d-M-uuuu H:mm", "uuuu-MM-dd HH:mm:ss", "d MMM uuuu HH:mm:ss",
        ).map { DateTimeFormatter.ofPattern(it, Locale.ENGLISH) }
    }
}
