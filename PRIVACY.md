# Privacy note

aware is an offline-first personal finance application. It has no user account,
advertising, analytics, or cloud synchronization.

## Transaction messages

If granted, Android's `RECEIVE_SMS` permission lets aware process new incoming
messages. The app does not request `READ_SMS` and does not import SMS history.
Parsing, direction detection, amount extraction, and deduplication happen on the
device. Encrypted raw text is retained only while a capture candidate is
unresolved and for no more than seven days.

## Storage and export

The Room database is encrypted with SQLCipher using a random passphrase protected
by Android Keystore. Password-encrypted backup is available. CSV export is
intentionally unencrypted and is clearly labelled before export.

## Optional AI categorisation

AI is optional and never determines amount or debit/credit direction. When a
user supplies a Groq key, aware may send redacted merchant context and category
choices. It does not send raw SMS, account numbers, UPI IDs, phone numbers,
transaction references, or balances.

## Reporting a privacy issue

Please use GitHub's private security-reporting facility when available. Do not
place real financial messages, backups, keys, or identifiers in a public issue.
