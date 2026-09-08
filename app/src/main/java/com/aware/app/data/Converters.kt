package com.aware.app.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter fun stringToAccountKind(value: String) = AccountKind.valueOf(value)
    @TypeConverter fun accountKindToString(value: AccountKind) = value.name
    @TypeConverter fun stringToTransactionType(value: String) = TransactionType.valueOf(value)
    @TypeConverter fun transactionTypeToString(value: TransactionType) = value.name
    @TypeConverter fun stringToTransactionStatus(value: String) = TransactionStatus.valueOf(value)
    @TypeConverter fun transactionStatusToString(value: TransactionStatus) = value.name
    @TypeConverter fun stringToTransactionSource(value: String) = TransactionSource.valueOf(value)
    @TypeConverter fun transactionSourceToString(value: TransactionSource) = value.name
    @TypeConverter fun stringToCadence(value: String) = RecurrenceCadence.valueOf(value)
    @TypeConverter fun cadenceToString(value: RecurrenceCadence) = value.name
}
