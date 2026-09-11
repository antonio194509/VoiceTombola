# Walkthrough - Interactive Card Selection Grid

I have completely redesigned the card selection process for players. Instead of manually typing card numbers, players now use an interactive grid that syncs in real-time with the Caller and other players.

## Key Features

### 1. Visual 72-Card Grid
- The Player Setup screen now displays all 72 cards in a clean, scrollable grid.
- On larger screens, the cards are arranged in **6 rows of 12**, as requested.

### 2. Real-time Multi-player Sync
- **Automatic Discovery**: As soon as a Player enters the "Caller ID", the app automatically connects to the Caller's device in the background.
- **Occupied Cards**: Cards already selected by other players will automatically turn **dark grey** and become unclickable.
- **Instant Locking**: When you tap a card, it is immediately "claimed" on the Caller's server, preventing other players from choosing it at the same time.

### 3. Selection Limits
- The app strictly enforces the limit of **maximum 6 cards** per player.
- Selected cards are highlighted in **blue**.
- If you tap a selected card again, it is released and becomes available for others.

## How to use
1. Go to the **PLAYER** screen.
2. Enter the **CALLER ID** of the host.
3. Wait a second for the grid to update (cards taken by others will grey out).
4. Tap up to **6 cards** to select them.
5. Press **START** to play!

> [!TIP]
> If a card is taken by someone else exactly while you are tapping it, the app will show an alert: *"This card has just been taken by another player!"* and update the grid automatically.
