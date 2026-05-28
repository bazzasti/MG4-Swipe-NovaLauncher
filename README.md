# MG4 Swipe NovaLauncher

Swipe-up gesture launcher for MG4 Android Automotive head unit. Adds a swipe-up zone at the bottom of the screen to quickly launch any app — designed for fast access to Nova Launcher or any other app of your choice.

## Features

- **Swipe up from bottom-right edge** — launches your chosen app (default: Nova Launcher)
- **Swipe up from bottom-left edge** — performs a Back action
- **Floating back button** — draggable overlay button for quick Back action (toggleable)
- **App picker** — select any installed app as the swipe target
- **Auto-start on boot** — service starts automatically when the car powers on
- **Day/night theme** — adapts to the system theme

## How It Works

The app runs a foreground service (`SwipeService`) that places two invisible 10px-high touch zones at the bottom of the screen — one on the left half, one on the right. When a swipe-up gesture is detected:

- **Right side** → launches the selected app via `getLaunchIntentForPackage()`
- **Left side** → sends a broadcast to `AccService`, which performs `GLOBAL_ACTION_BACK` via the Accessibility API

## Permissions Required

| Permission | Why |
|---|---|
| `SYSTEM_ALERT_WINDOW` | Draw the invisible swipe zones and floating button over other apps |
| `ACCESSIBILITY_SERVICE` | Perform the system Back action (no other way on Android Automotive) |
| `RECEIVE_BOOT_COMPLETED` | Auto-start the swipe service when the car boots |
| `FOREGROUND_SERVICE` | Keep the swipe detection running in the background |

## Architecture

```
PermissionActivity (launcher) → checks overlay + accessibility perms
    ↓
MainActivity → app picker list, back button toggle
    ↓
SwipeService (foreground) → gesture detection overlay
    ↓
AccService (accessibility) → performs GLOBAL_ACTION_BACK
    ↓
BootReceiver → auto-starts SwipeService on boot
```

## Build

```bash
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

## Install

```bash
adb install app-debug.apk
```

Then open "MG4 Swipe" on the head unit, grant overlay and accessibility permissions, and select your target app.

## Credits

Original author: **tommasov**
