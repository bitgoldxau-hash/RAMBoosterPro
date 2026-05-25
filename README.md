# RAM Booster Pro - Android App

## Features
- Show Total / Free / Used RAM in real-time
- RAM usage progress bar
- One-tap RAM Boost (kills background processes)
- Virtual RAM toggle with slider (1–8 GB)
- Running apps list with Kill buttons
- Auto-refreshes every 2 seconds

## Project Structure
```
RAMBoosterPro/
├── app/
│   ├── src/main/
│   │   ├── java/com/rambooster/pro/
│   │   │   └── MainActivity.kt       ← Main logic
│   │   ├── res/layout/
│   │   │   └── activity_main.xml     ← UI layout
│   │   └── AndroidManifest.xml       ← Permissions
│   └── build.gradle                  ← Dependencies
└── README.md
```

## How to Build (Android Studio)

1. Install Android Studio: https://developer.android.com/studio
2. Open Android Studio → "Open an Existing Project"
3. Select the `RAMBoosterPro` folder
4. Wait for Gradle sync to finish
5. Connect your Android phone via USB (enable USB Debugging)
6. Click the ▶ Run button

## How to Build (Command Line)
```bash
cd RAMBoosterPro
./gradlew assembleDebug
# APK will be at: app/build/outputs/apk/debug/app-debug.apk
```

## Permissions Used
| Permission | Why |
|------------|-----|
| GET_TASKS | Read list of running apps |
| KILL_BACKGROUND_PROCESSES | Kill apps to free RAM |
| RECEIVE_BOOT_COMPLETED | Auto-start on boot |
| FOREGROUND_SERVICE | Background RAM monitoring |

## Minimum Requirements
- Android 5.0 (Lollipop) or higher
- Works on phones with 2GB+ RAM
- No root required

## Notes on Virtual RAM
The Virtual RAM feature shows a simulated value to demonstrate
what RAM expansion looks like. True virtual RAM (swap space)
requires device manufacturer support or root access.
Phones like Xiaomi, Samsung, Realme have this built into Settings.
