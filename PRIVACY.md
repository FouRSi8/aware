# Privacy note

aware is an offline-first personal finance application. It has no user account,
advertising, analytics, or cloud synchronization.

## Transaction entry

aware does not request SMS access or read payment-app notifications. Its home-screen
widgets display aggregates already stored in the private ledger. Transactions enter the ledger only when the user records them
manually, confirms a recurring item, or explicitly imports selected bank-statement
rows.

## Bank statement import

CSV, XLS, and XLSX statements are opened through Android's document picker and
parsed locally. The source file is not copied into aware. Its in-memory byte buffer
is cleared after parsing or cancellation. If a workbook is password protected, the
password is used only to unlock that file, cleared from the active UI and parser
memory immediately after the unlock attempt, and never persisted or sent over the
network.

Parsed rows remain in a temporary review screen. Likely duplicates and rows whose
direction cannot be verified start unchecked. Nothing is written to the encrypted
ledger until the user selects and confirms the rows.

## Storage and export

The Room database is encrypted with SQLCipher using a random passphrase protected
by Android Keystore. Password-encrypted backup is available. CSV export is
intentionally unencrypted and is clearly labelled before export.

## Optional AI categorisation

Categorisation is local-first and never determines amount or debit/credit
direction. aware learns merchant-to-category mappings only after the user saves
or imports a reviewed transaction. Those rules, transaction-history matching,
and built-in merchant hints remain on the device.

Monthly forecasts, savings guidance, spending-pattern explanations, and the aware
coach are calculated entirely on the device and never call an AI service.

Groq is an optional fallback for entries that local matching cannot classify. When
a user supplies a Groq key, aware may send unresolved merchants during manual save
or explicit statement auto-categorisation. Requests contain only redacted merchant
words and the user's category names and are batched during statement review.
It does not send statement contents, amounts, account numbers, UPI IDs, phone
numbers, transaction references, balances, statement files, or statement
passwords. Every returned category is validated locally before it is displayed.

## App updates

GitHub-distributed builds check the public GitHub Releases API once a week while connected to a
network, and whenever the user chooses **Check for updates**. This request sends
only standard network metadata and the installed app version; it contains no
financial data. An APK is downloaded only after the user approves an available
release, and Android's system installer always asks for final confirmation.
Google Play builds omit the package-install permission and GitHub update feature;
they update exclusively through Google Play.

## Reporting a privacy issue

Please use GitHub's private security-reporting facility when available. Do not
place real financial messages, backups, keys, or identifiers in a public issue.
