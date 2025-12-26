# ShortsLock - YouTube Shorts Usage Control

**Production-ready Android app that limits YouTube Shorts usage using OS-level control.**

## Overview

ShortsLock monitors YouTube Shorts usage and enforces time limits without modifying YouTube or requiring root access. After the timer expires, Shorts is blocked until the user pays ₹49 for a 10-minute temporary unlock via UPI.

## Features

✅ **Timer-Based Control** - Set usage limits from 0-60 minutes  
✅ **Real-Time Detection** - Accessibility Service detects Shorts viewing  
✅ **Full-Screen Lock** - System overlay blocks interaction when timer expires  
✅ **Slide-to-Unlock** - Intuitive gesture-based unlock mechanism  
✅ **UPI Payment** - Integrated ₹49 payment for 10-minute unlock  
✅ **State Persistence** - Timer survives app restarts  
✅ **Foreground Service** - Prevents OS from killing monitoring  

## Architecture

### MVVM Structure

```
app/
├── data/
│   └── PreferencesManager.kt      # SharedPreferences state management
├── manager/
│   ├── TimerManager.kt            # Timer countdown logic
│   ├── OverlayManager.kt          # System overlay control
│   └── PaymentManager.kt          # UPI payment handling
├── service/
│   ├── ShortsDetectionService.kt  # Accessibility Service
│   └── MonitoringForegroundService.kt
└── ui/
    └── MainActivity.kt            # User interface
```

### Key Components

**1. Accessibility Service**  
Monitors YouTube app for Shorts detection using:
- Text node analysis ("Shorts" keyword)
- View ID pattern matching
- Vertical pager layout detection

**2. Timer Manager**  
- Counts down only when Shorts is active
- Handles temp unlock expiration
- Persists state across restarts

**3. Overlay Manager**  
- TYPE_APPLICATION_OVERLAY for system-level blocking
- Custom slide-to-unlock gesture
- Prevents back button and touch events

**4. Payment Manager**  
- Standard UPI deep link integration
- Trust-based verification (no gateway)
- Payee: `nitronitish@fam`

## Permissions Required

| Permission | Purpose |
|------------|---------|
| `SYSTEM_ALERT_WINDOW` | Display overlay above YouTube |
| `BIND_ACCESSIBILITY_SERVICE` | Detect Shorts viewing |
| `FOREGROUND_SERVICE` | Keep monitoring active |

## Setup Instructions

### 1. Open in Android Studio

```bash
cd ShortsLock
# Open project in Android Studio
```

### 2. Build APK

```bash
./gradlew assembleDebug
# APK location: app/build/outputs/apk/debug/app-debug.apk
```

### 3. Install on Device

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 4. Grant Permissions

**Overlay Permission:**
1. Open ShortsLock
2. Tap "Grant" when prompted
3. Enable "Display over other apps"

**Accessibility Service:**
1. Settings → Accessibility
2. Find "ShortsLock"
3. Enable the service

## Usage Flow

### 1. Set Timer
- Open ShortsLock app
- Use slider to set duration (0-60 minutes)
- Tap "START MONITORING"

### 2. Monitor Shorts
- App runs in background
- Timer counts down only when viewing Shorts
- Notification shows monitoring status

### 3. Lock Triggered
- When timer reaches 0:
  - Full-screen overlay appears
  - Shorts interaction blocked
  - Slide-to-unlock displayed

### 4. Unlock (Paid)
- Slide the unlock button
- UPI payment screen opens (₹49)
- Complete payment
- Return to app → 10 minutes unlocked

### 5. Re-Lock
- After 10 minutes, overlay reappears
- Repeat payment for additional time

## Technical Details

### Shorts Detection Algorithm

```kotlin
// Multi-strategy detection
1. Text node search: "Shorts" keyword
2. View ID matching: contains "shorts" or "reel"
3. Layout analysis: Vertical ViewPager/RecyclerView
4. Recursive tree traversal
```

### Timer Behavior

- **Active State**: Counts down when Shorts detected
- **Paused State**: Stops when user exits Shorts
- **Locked State**: Timer expired, overlay shown
- **Temp Unlock**: 10-minute paid bypass

### State Persistence

All state stored in SharedPreferences:
- `timer_duration` - Selected duration
- `remaining_time` - Current countdown
- `is_locked` - Lock status
- `is_temp_unlocked` - Paid unlock active
- `temp_unlock_end_time` - Unlock expiration

## Build Configuration

**Min SDK:** 26 (Android 8.0)  
**Target SDK:** 34 (Android 14)  
**Language:** Kotlin 1.9.20  
**Gradle:** 8.2.0  

## Dependencies

```gradle
androidx.core:core-ktx:1.12.0
androidx.appcompat:appcompat:1.6.1
com.google.android.material:material:1.11.0
androidx.constraintlayout:constraintlayout:2.1.4
androidx.cardview:cardview:1.0.0
```

## Limitations

⚠️ **Trust-Based Payment** - No server-side verification  
⚠️ **Detection Accuracy** - YouTube UI changes may affect detection  
⚠️ **Battery Usage** - Accessibility Service runs continuously  
⚠️ **Not Play Store Ready** - Requires manual installation  

## Security Considerations

- No data collection
- No network requests
- Local-only state storage
- No external analytics

## Testing Checklist

- [ ] Timer countdown accuracy
- [ ] Shorts detection reliability
- [ ] Overlay blocking effectiveness
- [ ] Slide-to-unlock responsiveness
- [ ] UPI payment flow
- [ ] State persistence after restart
- [ ] Foreground service stability
- [ ] Permission handling

## Troubleshooting

**Overlay not showing:**
- Check "Display over other apps" permission
- Verify app is not battery optimized

**Shorts not detected:**
- Ensure Accessibility Service is enabled
- Check YouTube app version compatibility
- Restart accessibility service

**Timer not counting:**
- Verify monitoring is active
- Check foreground service notification
- Ensure Shorts is actually detected

## License

This is a production-ready implementation for private use. Not licensed for commercial distribution.

## Support

For issues or questions, contact the developer.

---

**Built with Kotlin • Android Studio • MVVM Architecture**
