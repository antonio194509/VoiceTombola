# Walkthrough - Fix Neapolitan Smorfia Display

I have fixed the issue where selecting Neapolitan ("NAP") would still display smorfia text in Italian.

## Changes Made

### WebView Assets

#### [index.html](file:///C:/TombolaFamily/app/src/main/assets/index.html)
- Updated the `aggiornaUI(n)` function to explicitly handle the Neapolitan language.
- It now correctly checks if `lingua === 'nap'` and retrieves the text from `SMORFIA_NAP`.
- The logic for selecting smorfia text now prioritizes custom titles from the Android interface, then checks for English, then Neapolitan, and defaults to Italian.

```javascript
    function aggiornaUI(n) {
        mostraIconaGrande(n);
        let nm = getAndroidInterface()?.getCustomTitle(n);
        if (!nm) {
            if (lingua === 'en') nm = SMORFIA_EN[String(n).padStart(2, '0')];
            else if (lingua === 'nap') nm = SMORFIA_NAP[String(n).padStart(2, '0')];
            else nm = SMORFIA_IT[String(n).padStart(2, '0')];
        }
        // ...
```

## Verification Results

### Code Review
- Confirmed that `SMORFIA_NAP` is correctly referenced and follows the same pattern as `SMORFIA_EN` and `SMORFIA_IT`.
- Verified that `smorfia_nap.js` is correctly included in the HTML.

### Manual Verification (Pending User Test)
- Selecting "NAP" and extracting a number (e.g., 2) should now show "'a piccerella" instead of "la bambina".
