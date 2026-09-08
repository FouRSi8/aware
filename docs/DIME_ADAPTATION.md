# Dime → aware design adaptation

Upstream: [rafsoh/dimeApp](https://github.com/rafsoh/dimeApp), GPL-3.0.
Adapted for Android/Jetpack Compose in September 2026.

## Adopted directly

- Outline and filled Log, Insights, Budget, and Settings navigation artwork.
- Dime's expressive category palette and color-owned category chips.
- Rounded system typography strategy. Dime uses Apple's system rounded design;
  aware uses Android's legal platform equivalent, `sans-serif-rounded`.
- Near-black green-tinted dark canvas, neutral grouped surfaces, restrained borders,
  green income, red expense, and white affirmative actions.

## Reimplemented in Compose

- Net-first Log with period control, income/expense pair, upcoming capture queue,
  date-grouped activity, and compact transaction rows.
- Five-position bottom navigation with a wide centered Add control and filled
  selected-state artwork.
- Full-screen transaction input with large amount typography, tactile number pad,
  expense/income switcher, category chips, accounts, transfers, and refunds.
- Insights summary with spent-per-day, income/expense tiles, animated period bars,
  proportional category strip, and activity list.
- Overall semicircle budget gauge, two-column category budget cards, and recurring
  cash-flow section.
- Grouped settings with colored icon tiles and a visible open-source notice.

## aware-specific behavior retained

- New-SMS-only transaction capture and Glance review widget.
- Bank, UPI, and cash accounts; bank-to-cash transfers; refunds and reversals.
- Local encrypted Room storage, encrypted backup, CSV export, and optional Groq
  category suggestions using redacted merchant context.

The Swift source is not compiled into the Android app. Relevant behavior was
ported to Kotlin while preserving Dime attribution and GPL-3.0 obligations.
