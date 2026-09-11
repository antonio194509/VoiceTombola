# Implementation Plan - Fix Neapolitan Smorfia Display

The user reported that when "NAP" (Neapolitan) is selected, the displayed smorfia text is still in Italian. Research confirmed that the `aggiornaUI` function in `index.html` only checks for English and defaults to Italian, missing the Neapolitan case.

## Proposed Changes

### [Component] WebView Assets

#### [MODIFY] [index.html](file:///C:/TombolaFamily/app/src/main/assets/index.html)
- Update `aggiornaUI(n)` to handle `lingua === 'nap'` and use `SMORFIA_NAP`.
- I will also use a more robust way to select the smorfia text based on the selected language.

## Verification Plan

### Manual Verification
- Deploy the app.
- Start a game as Player or Caller.
- Select "NAP" language.
- Extract or click a number (e.g., 1).
- Verify that the text displayed is "'l'Italia" (which is the same in both, maybe choose another one like 2).
- Verify number 2: Italian is "la bambina", Neapolitan is "'a piccerella".
- Verify number 16: Italian is "il deretano", Neapolitan is "'o culo".
