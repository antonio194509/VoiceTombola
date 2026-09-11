# Implementation Plan - Prevent Auto-Mode Deactivation

The user reported that the **AUTO** mode deactivates on "every click". Research shows that currently, clicking the **VOICE** button explicitly stops the **AUTO** mode. Additionally, there is no way to manually extract a number by clicking the board, which might be another "click" interaction the user expects to work without stopping the automation.

## User Review Required

> [!IMPORTANT]
> I am removing the mutual exclusivity between **AUTO** and **VOICE** modes. They can now both be active at once. If a number is extracted manually (via voice or board click) while **AUTO** is running, the automatic timer will reset to ensure the next draw happens after the full interval.

## Proposed Changes

### Assets Component

#### [MODIFY] [index.html](file:///C:/TombolaFamily/app/src/main/assets/index.html)
- **Remove Automatic Stops**:
    - In `estrai()`: Remove the line that calls `stopAuto()`.
    - In `toggleAuto()`: Remove the line that calls `stopVoice()`.
- **Manual Extraction via Board**:
    - Update `buildTabellone()` to use a new `onclick` handler: `gestisciCella(n)`.
    - Implement `gestisciCella(n)`:
        - If the number is not drawn, extract it.
        - If the number is already drawn, call `cancellaNumero(n)`.
- **Timer Resets**:
    - Implement `resetAutoTimer()` to restart the `setInterval` with the current interval value.
    - Call `resetAutoTimer()` inside `aggiornaUI(n)` so that any extraction (manual or automatic) pushes the next automatic draw forward by the full interval.

## Verification Plan

### Manual Verification
1.  **Mode Coexistence**: Start **AUTO**. Click **VOICE**. Verify the button says "AUTO: ON" and the microphone pulses.
2.  **Manual Draw Reset**: Set interval to 15s. Start **AUTO**. After 5s, click a number on the board to extract it. Verify the next automatic extraction happens ~15s after your click, not 5s later.
3.  **Board Extraction**: Click an un-extracted number as a Caller. Verify it gets marked, announced, and the smorfia appears.
4.  **Board Cancellation**: Click an extracted number. Verify the confirmation dialog appears and it can be cancelled as before.
