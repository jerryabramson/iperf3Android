@echo off
REM =================================================
REM  Pre‑deploy script for emulator / device
REM  (converted from the original bash script)
REM =================================================
echo  Running pre-deploy script for emulator/device...
echo %DATE% %TIME%
echo ANDROID_HOME = %ANDROID_HOME%
echo ADB_EXECUTABLE = %ADB_EXECUTABLE%

@echo on
%ADB_EXECUTABLE% devices
%ADB_EXECUTABLE% shell getenforce

REM Different emulators require slightly different syntax for su
%ADB_EXECUTABLE% shell -n su -c  setenforce 0
%ADB_EXECUTABLE% shell su root setenforce 0
%ADB_EXECUTABLE% shell getenforce
echo Pre-deploy script completed.

