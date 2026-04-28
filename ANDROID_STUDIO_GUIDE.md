# Android Studio Setup & Debugging Guide

## Quick Start in Android Studio

### 1. Open Project

```bash
# In Android Studio:
# File → Open → Select /mnt/hdd/VeloPass/android folder
# Wait for Gradle sync to complete (1-2 minutes)
```

### 2. Configure Run Configuration

The project includes pre-configured run configurations:

- **app (debug)** - Standard debug build with emulator/device selection
  - Auto-deploys APK on file changes
  - Shows logcat automatically
  - Debugger enabled by default

### 3. Run the App

**Option A: Using Run Button**
```
1. Click "Run" button (green play icon) in toolbar
2. Select target device/emulator
3. App builds and installs automatically
```

**Option B: Using Keyboard Shortcut**
```
Ctrl+R (Windows/Linux)
Cmd+R (Mac)
```

**Option C: Using Menu**
```
Run → Run 'app'
```

### 4. Debug the App

**Start Debugging:**
```
1. Set breakpoint by clicking line number in code editor
2. Click "Debug" button (green bug icon) or press Shift+F9
3. Select target device
4. App builds and installs with debugger attached
```

**Debug Controls:**
- **F8** - Step over
- **F7** - Step into
- **Shift+F8** - Step out
- **Ctrl+F8** - Toggle breakpoint
- **F9** - Resume program

**View Debug Info:**
- Variables: Bottom panel → "Variables" tab
- Call Stack: Bottom panel → "Debugger" tab
- Logcat: Bottom panel → "Logcat" tab

### 5. Emulator Setup

**Create Emulator:**
```
1. Tools → Device Manager
2. Click "Create device"
3. Select "Pixel 6 Pro" (recommended)
4. Select "Android 13" or higher
5. Click "Finish"
```

**Run Emulator:**
```
1. Tools → Device Manager
2. Click play icon next to device
3. Wait for boot (30-60 seconds)
```

**Recommended Emulator Settings:**
- Resolution: 1440 x 3120 (Pixel 6 Pro)
- RAM: 4-8 GB (configure in AVD settings)
- Internal Storage: 64 GB
- Graphics: Hardware (GPU)
- Network: WiFi

### 6. Testing

**Run Unit Tests:**
```
Right-click on test class → Run 'TestClassName'
Or use: Ctrl+Shift+F10
```

**Run Instrumented Tests (on emulator/device):**
```
Right-click on androidTest class → Run 'TestClassName'
Or use: Ctrl+Shift+F10
```

**Run All Tests:**
```
Gradle panel → Tasks → verification → test
Or: ./gradlew test
```

### 7. Logcat Filtering

**View App Logs:**
```
1. Open Logcat: View → Tool Windows → Logcat (Alt+6)
2. Filter by package: "com.velopass.app"
3. Log level filter: "Verbose" or "Debug" during development
```

**Show Only App Logs:**
```
Logcat → Edit Filter Configuration
Add: package:com.velopass.app
```

### 8. Performance Profiling

**CPU Profiler:**
```
1. Run app in debug mode
2. View → Tool Windows → Profiler
3. Click "CPU" tab
4. Record method traces while interacting with app
```

**Memory Profiler:**
```
1. Run app in debug mode
2. View → Tool Windows → Profiler
3. Click "Memory" tab
4. Monitor heap allocations
```

**Network Profiler:**
```
1. Run app in debug mode
2. View → Tool Windows → Profiler
3. Click "Network" tab
4. Monitor API calls to Appwrite
```

### 9. Layout Inspector

**Inspect UI Hierarchy:**
```
1. Run app in debug mode
2. Tools → Layout Inspector
3. Select device
4. Inspect widget properties and hierarchy
```

### 10. Gradle Build Configuration

**Build Variants:**
```
Android Studio → Build Variants panel (bottom-left)
Select:
- Debug: Fast builds, debugging enabled
- Release: Optimized, no debugging
```

**Gradle Properties:**
Edit `gradle.properties` for build optimization:
- `org.gradle.daemon=true` - Faster subsequent builds
- `org.gradle.parallel=true` - Parallel compilation
- `android.incremental=true` - Faster rebuilds

### 11. Common Issues & Solutions

**Issue: Gradle sync fails**
```
Solution:
1. File → Sync Now
2. If fails: File → Invalidate Caches → Restart
3. Delete .gradle folder and retry
```

**Issue: App not installing**
```
Solution:
1. Disconnect device/close emulator
2. Uninstall app: Run → app → Uninstall and Rerun
3. Check adb devices: adb devices
```

**Issue: Debugger not attaching**
```
Solution:
1. Run with Debug button, not Run button
2. Check "Debug" view panel is visible
3. Ensure app has INTERNET permission
```

**Issue: Slow builds**
```
Solution:
1. Increase Gradle heap: org.gradle.jvmargs=-Xmx2048m
2. Enable daemon: org.gradle.daemon=true
3. Clean: ./gradlew clean
```

**Issue: Emulator crashes**
```
Solution:
1. Delete emulator: Tools → Device Manager → ⋮ → Wipe Data
2. Restart: Close emulator, reopen
3. Or create new emulator
```

### 12. Keyboard Shortcuts (Windows/Linux)

| Shortcut | Action |
|----------|--------|
| Ctrl+R | Run app |
| Shift+F9 | Debug app |
| F9 | Resume execution |
| F8 | Step over |
| F7 | Step into |
| Shift+F8 | Step out |
| Ctrl+F8 | Toggle breakpoint |
| Alt+6 | Show Logcat |
| Ctrl+Shift+A | Find action |
| Ctrl+P | Show parameter info |
| Ctrl+H | Show class hierarchy |

### 13. macOS Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| Cmd+R | Run app |
| Cmd+D | Debug app |
| Cmd+Y | Resume execution |
| F8 | Step over |
| F7 | Step into |
| Cmd+Shift+8 | Step out |
| Cmd+F8 | Toggle breakpoint |
| Cmd+6 | Show Logcat |
| Cmd+Shift+A | Find action |

### 14. Tips & Best Practices

**Development Workflow:**
1. Make code changes in editor
2. Enable "Hot Reload" if available (Android Studio → preferences)
3. Use incremental builds for speed
4. Test frequently on emulator/device
5. Use debugger for complex issues

**Performance Tips:**
- Use emulator with GPU enabled for smooth UI
- Profile app regularly during development
- Monitor memory leaks in Memory Profiler
- Test on device periodically, not just emulator

**Code Quality:**
- Run linter: Analyze → Run Inspection by Name
- Format code: Code → Reformat Code (Ctrl+Alt+L)
- Optimize imports: Code → Optimize Imports
- Enable lint warnings in Editor → Inspections

### 15. Additional Resources

- [Android Studio Docs](https://developer.android.com/studio/intro)
- [Kotlin Docs](https://kotlinlang.org/docs/)
- [Compose Docs](https://developer.android.com/jetpack/compose)
- [Debugging Docs](https://developer.android.com/studio/debug)
- [Profiling Docs](https://developer.android.com/studio/profile)

---

**Happy Debugging!** 🐛

For issues, check SETUP_AND_DEPLOYMENT.md or Android Studio's built-in help.
