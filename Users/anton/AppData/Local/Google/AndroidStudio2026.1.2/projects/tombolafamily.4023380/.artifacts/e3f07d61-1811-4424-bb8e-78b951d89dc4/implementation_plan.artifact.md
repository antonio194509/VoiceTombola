# Implementation Plan - Replace Blocking Alerts with Native Toasts

The goal is to eliminate intrusive dialogue popups (alerts) for simple notifications. This will resolve the "file://" header issue and the "stuck on OK" bug by using non-blocking Android Toasts.

## User Review Required

> [!NOTE]
> **Toasts vs Alerts**: Toasts appear at the bottom of the screen and disappear automatically after a few seconds. They do not block the user or the game flow.
> I will keep the `confirm()` dialogs (like "Restore smorfia?") as popups because they require a Yes/No choice, but I will make them more reliable.

## Proposed Changes

### Android Bridge (Kotlin)

#### [MODIFY] [MainBridge.kt](file:///C:/TombolaFamily/app/src/main/java/com/anton/MainBridge.kt) and [FamilyBridge.kt](file:///C:/TombolaFamily/app/src/main/java/com/anton/FamilyBridge.kt)
- Add a new `@JavascriptInterface` method: `toast(message: String)`.
- Use `Toast.makeText(context, message, Toast.LENGTH_SHORT).show()` inside a `runOnUiThread` block.

### Web Interface (HTML/JS)

#### [MODIFY] [index.html](file:///C:/TombolaFamily/app/src/main/assets/index.html)
- Create a helper function `showAlert(msg)` that uses `Android.toast(msg)` if available, falling back to `alert(msg)`.
- Replace all instances of `alert(...)` with `showAlert(...)`.
- Keep `confirm(...)` as is (it will be handled by the custom `WebChromeClient` which removes the "file://" header).

## Verification Plan

### Manual Verification
1. **Notification Test**: Select a card that is already taken. A Toast should appear at the bottom without blocking the screen or showing "file://".
2. **Limit Test**: Try selecting 7 cards. A Toast should appear: "You can select maximum 6 cards!".
3. **Confirmation Test**: Click "Restore Smorfia". A clean Android popup (without "file://") should appear with "OK" and "Cancel" buttons.
4. **Resiliency**: Verify that clicking "OK" on confirmation dialogs no longer hangs the app.
