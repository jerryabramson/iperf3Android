#!/usr/bin/env bash
EXEC=iperf3.20
URL=https://github.com/davidBar-On/android-iperf3/raw/refs/heads/gh-pages/libs/arm64-v8a/$EXEC
if [[ ! -f $EXEC ]]; then
   printf "iperf3 Executable for arm64-v8a is not loaded, shall I download it? "
   read YN
   if [[ ${YN:0:1} == "y" || ${YN:0:1} == "Y" ]]; then
       DL=wget
       which $DL; RC=$?
       if [[ $RC -eq 0 ]]; then
          wget $URL
       else 
          which curl; RC=$?
          if [[ $RC -eq 0 ]]; then 
              curl  -O "$URL" -L
          else
              printf "No suitable command-line tool available, download manually from\n"
              printf "$URL"
              exit 1
          fi
       fi
   else
       printf "No suitable command-line tool available, download manually from\n"
       printf "$URL"
       exit 1
   fi
fi
rm -f devlist$$
# Check for Android Device connection
adb devices -l 2>&1|tail -n +2|head -1 >devlist$$
LL=$(cat devlist$$ | wc -l)
rm -f devlist$$
if [[ $LL -ne 1 ]]; then
    printf "Either no No Android Devices/emulators coonected, or too many!"
    exit 1
fi
set -x
adb push $EXEC /data/local/tmp/iperf3
adb shell chmod 777 /data/local/tmp/iperf3
set +x

echo "Nice to have, but not a fatal error:"
#adb shell ln -s /data/local/tmp/$EXEC iperf3

