# Implementation Plan - Interactive Card Selection Grid

The goal is to replace the manual card number entry with a 6x12 grid of all 72 available cards. The grid will show which cards are already taken by other players and allow the user to select up to 6 cards by clicking them.

## User Review Required

> [!IMPORTANT]
> **Server Sync**: The app will automatically fetch the "occupied" status from the Caller's device as soon as a valid Caller ID is entered. This requires the Caller to be active on the network.
>
> **Max Cards**: The limit of 6 cards per player will still be enforced.

## Proposed Changes

### Web Interface (HTML/JS)

#### [MODIFY] [index.html](file:///C:/TombolaFamily/app/src/main/assets/index.html)
- **HTML**: Replace the manual input `card-number-input` and `ADD` button with a `div` container for the 72-card grid.
- **CSS**:
    - Create a responsive grid layout (12 columns on wide screens, or scrollable).
    - Style for `card-flag`: default, `selected` (blue), and `occupied` (grey/disabled).
- **JS Logic**:
    - `renderCardGrid()`: Generates 72 clickable elements.
    - `fetchOccupiedCards()`: Calls the Android Bridge to get the list of cards taken by other players from the Caller's `GameServer`.
    - `toggleCard(id)`: Handles selection logic (check if taken, check limit of 6, send claim/release request to server).
    - Update `onLocalCallerChecked`: If a caller is found, trigger the fetch of occupied cards immediately.

### Android Bridge (Kotlin)

#### [MODIFY] [MainBridge.kt](file:///C:/TombolaFamily/app/src/main/java/com/anton/MainBridge.kt)
- Ensure the `sendGameRequest` method is robust and handles responses correctly to pass them back to JS.

## Verification Plan

### Manual Verification
1. **Grid Layout**: Open the Player Setup screen and verify there are 72 numbered boxes arranged in rows.
2. **Multi-Player Sync**:
    - Start a Caller on Device A.
    - Open Player Setup on Device B. Select card #10.
    - Open Player Setup on Device C. Card #10 should appear as "Occupied" (e.g., greyed out) and should not be selectable.
3. **Limit Enforcement**: Try selecting more than 6 cards; the app should prevent it.
4. **Deselection**: Deselect a card on Device B and verify it becomes available again on Device C.
