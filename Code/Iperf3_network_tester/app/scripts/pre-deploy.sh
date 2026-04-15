#!/bin/bash
date
echo "Running pre-deploy script for emulator/device..."
echo "\$ANDROID_HOME='$ANDROID_HOME'"
echo "\$ADB_EXECUTABLE='$ADB_EXECUTABLE'"
$ADB_EXECUTABLE devices
echo "Turn off selinux for testing purposes"
set -x
$ADB_EXECUTABLE shell getenforce
$ADB_EXECUTABLE shell -n su -c  setenforce 0
$ADB_EXECUTABLE shell getenforce
set +x
echo "Pre-deploy script completed."
