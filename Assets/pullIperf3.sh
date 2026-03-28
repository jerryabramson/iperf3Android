PREFIX_URL="https://github.com/davidBar-On/android-iperf3/raw/refs/heads/gh-pages/libs"
ARCH=$(basename $(pwd))
EXEC=iperf3.20
wget $PREFIX_URL/$ARCH/$EXEC
mv $EXEC iperf3
