# MG4 Swipe NovaLauncher

Swipe-up gesture launcher for MG4 Android Automotive head unit. Adds invisible touch zones at the bottom of the screen to quickly launch any app or go back — designed for fast access without menus.

## Features

- **Swipe up (right half)** — launches your chosen app (default: Nova Launcher)
- **Swipe up (left half)** — performs a Back action
- **Floating back button** — draggable overlay button (toggleable)
- **App picker** — select any installed app as the swipe target
- **Auto-start on boot** — swipe zones ready as soon as the car powers on
- **Uninstall from UI** — red button in settings with confirmation dialog
- **Configurable sensitivity** — swipe threshold and velocity stored in preferences
- **Button position saved** — floating button remembers where you dragged it

## Architecture

```
PermissionActivity → checks overlay + accessibility perms
    ↓
MainActivity → app picker, back button toggle, uninstall button
    ↓
SwipeService (foreground) → 12dp overlay zones + gesture detection
    ↓
AccService (accessibility) → static triggerBack() for GLOBAL_ACTION_BACK
    ↓
BootReceiver → auto-starts SwipeService on boot
```

## Tech Stack

| | |
|---|---|
| Language | Kotlin (100%) |
| UI binding | ViewBinding |
| Min SDK | 28 (Android 9) |
| Target SDK | 34 (Android 14) |
| Source files | 8 Kotlin files, ~620 lines |
| Tests | 31 unit tests across 5 files |
| CI | GitHub Actions (build + test on every push) |
| Error handling | 16 try/catch blocks, 23 AppLogger calls |

## Permissions

| Permission | Why |
|---|---|
| `SYSTEM_ALERT_WINDOW` | Draw invisible swipe zones over other apps |
| `ACCESSIBILITY_SERVICE` | Perform Back action (only way without root) |
| `RECEIVE_BOOT_COMPLETED` | Auto-start swipe service on boot |
| `FOREGROUND_SERVICE` | Keep swipe detection running in background |

**No platform signing key required** — works on any Android 9+ device.

## Build

```bash
git clone https://github.com/bazzasti/MG4-Swipe-NovaLauncher.git
cd MG4-Swipe-NovaLauncher
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

## Install

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

Then open "MG4 Swipe", grant overlay + accessibility permissions, select target app.

## Uninstall

From the app: tap the red **Uninstall** button → confirm → confirm on system screen.

Or via ADB: `adb uninstall com.tommasov.mg4swipenovalauncher`

## Debug

```bash
adb logcat -s MG4Swipe
```

## Credits

Original author: **tommasov**
