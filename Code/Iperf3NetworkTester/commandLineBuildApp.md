
# 1 Add the SDK location to your environment

```bash
# Add to PATH (Linux/macOS)
export ANDROID_SDK_ROOT=$HOME/android-sdk
export PATH=$PATH:$ANDROID_SDK_ROOT/cmdline-tools/stable/bin:$ANDROID_SDK_ROOT/platform-tools

# Windows PowerShell (persisted via [Environment]::SetEnvironmentVariable)
$Env:ANDROID_SDK_ROOT = "C:\android-sdk"
$Env:Path += ";C:\android-sdk\cmdline-tools\stable\bin;C:\android-sdk\platform-tools"
```

# 2 Install required SDK components

```bash
# Update the package list
sdkmanager --update
```
# 3 Generate the Gradle Wrapper

```bash
gradle wrapper \
       --gradle-version 9.3.1  \
       --distribution-type all \
       --gradle-distribution-sha256-sum 17f277867f6914d61b1aa02efab1ba7bb439ad652ca485cd8ca6842fccec6e43

```

This creates `gradlew`, `gradlew.bat`, and the `gradle/wrapper` folder.

# ️4 Build the APK from the Command Line

```bash
./gradlew assembleDebug   # Linux/macOS
# or on Windows
gradlew.bat assembleDebug
```

The resulting APK appears at:

```
app/build/outputs/apk/debug/app-debug.apk
```

#  5. Prepare Your Android Device for USB Debugging

1. **Enable Developer Options**  
   - Settings → About phone → tap **Build number** 7 times.  
   - Return to Settings → System → **Developer options**.

2. **Turn on USB debugging**  
   - In Developer options, toggle **USB debugging** ON.

3. **(Optional but recommended) Enable "Install via USB"**  
   - Still in Developer options, make sure **Install via USB** is enabled - this lets `adb install` work without extra prompts.

4. **Connect the phone via USB**  
   - Use a good data-capable USB cable (charging-only cables won't work for debugging).

5. **Authorize your PC**  
   - On the phone, a dialog will appear: "Allow USB debugging?" - tap **Allow**.  
   - You can optionally tick **Always allow from this computer** to avoid future prompts.

# 6 Verify that `adb` sees the device

```bash
adb devices
# Expected output (example)
# List of devices attached
# 0123456789ABCDEF    device
```

# 7 Install iperf3 onto either the emulator or the real device

```
cd ../../Assets
./pushIperf3.sh
```

*If you get no output from this command, you can only run the stubbed version of the application.*

```
adb shell ls -ls /bin/iperf3
108 -rwxr-xr-x 1 root shell 120368 2025-08-06 18:11 /bin/iperf3
```

# 8 Install & Run the APK on the Device

```bash
cd ../../Code
adb install -r app/build/outputs/apk/debug/app-debug.apk
# -r  = replace existing installation (useful for rapid iteration)
```

You'll see:

```
Success
```

If the app was already installed, `-r` forces an upgrade.


# 9 Launch the app using the emulator or real device
