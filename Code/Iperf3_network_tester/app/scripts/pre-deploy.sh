#!/bin/bash
echo "🔧 Running pre-deploy script for emulator/device..."
echo "🔧 Turn off selinux for testing purposes"
set -x
adb shell getenforce
adb shell su root  setenforce 0
adb shell getenforce
set +x
echo "✅ Pre-deploy script completed."
