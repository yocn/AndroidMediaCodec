#!/bin/bash

# clean
rm -rf ffmpeg-build/
rm -rf lame-build/
rm -rf ../jniLibs/
rm -rf ../obj/

# libmp3lame
./build-lame.sh

./build-x264.sh

# ffmpeg
#./build-ffmpeg.sh arm
./build-ffmpeg.sh arm64
#./build-ffmpeg.sh x86
#./build-ffmpeg.sh x86_64

# collect all to ffmpeg-mix
#ndk-build NDK_DEBUG=0 \
#          NDK_APPLICATION_MK=Application.mk \
#				  -e APP_BUILD_SCRIPT=ffmpeg.mk
