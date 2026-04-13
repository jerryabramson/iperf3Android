@echo off
REM =================================================
REM  Pre‑deploy script for emulator / device
REM  (converted from the original bash script)
REM =================================================

>> ..\pre-deploy.log 2>&1
echo %DATE% %TIME%
echo  Running pre-deploy script for emulator/device...
echo "ANDROID_HOME = %ANDROID_HOME%"
echo "ADB_EXECUTABLE = %ADB_EXECUTABLE%"

echo  List the devices that ADB sees
%ADB_EXECUTABLE% devices
echo Turn off SELinux for testing purposes
echo  Query the current SELinux mode
%ADB_EXECUTABLE% shell getenforce
echo  Disable SELinux enforcement (requires root)
%ADB_EXECUTABLE% shell su root setenforce 0
echo Verify that it is now permissive
%ADB_EXECUTABLE% shell getenforce
echo Pre-deploy script completed.

