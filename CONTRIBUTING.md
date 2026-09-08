# Contributing to aware

Thank you for helping make private money management better.

## Before opening an issue

- Search existing issues first.
- Remove every real name, account suffix, phone number, UPI ID, reference, balance, and transaction detail from screenshots or logs.
- Prefer a synthetic SMS fixture that reproduces parser behavior.
- Include Android version, device/emulator model, app version, and exact steps.

## Development flow

1. Fork the repository and create a focused branch.
2. Keep UI work correct in Cozy light, Cozy dark, and the maximal identity.
3. Add or update deterministic tests for business logic.
4. Run `./gradlew testDebugUnitTest lintDebug assembleDebug`.
5. Explain the change and include before/after images for visual work.

Never commit signing keys, API keys, personal SMS messages, real financial data,
`local.properties`, build artifacts, or emulator state.

By contributing, you agree that your contribution is licensed under GPL-3.0.
