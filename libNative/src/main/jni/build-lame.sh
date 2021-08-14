#!/bin/bash


#ndk-build NDK_DEBUG=0 \
#				  -e APP_ABI=arm64-v8a \
#				  -e APP_BUILD_SCRIPT=libmp3lame.mk \
#				  -e ARCH=armv8-a \
#				  -e NDK_APP_DST_DIR=lame-build


#cp -f lame-build/libmp3lame.so ../jniLibs/arm64-v8a/
ROOT_P=`pwd`

ndk-build NDK_DEBUG=0 \
          NDK_APPLICATION_MK=Application.mk \
				  -e APP_BUILD_SCRIPT=libmp3lame.mk \
				  -e NDK_APP_DST_DIR=lame-build

mkdir -p ${ROOT_P}/lame-build/armeabi-v7a
mkdir -p ${ROOT_P}/lame-build/arm64-v8a
mkdir -p ${ROOT_P}/lame-build/x86
mkdir -p ${ROOT_P}/lame-build/x86_64
mkdir -p ${ROOT_P}/lame-build/lame

cp -f  ${ROOT_P}/../obj/local/armeabi-v7a/libmp3lame.a ${ROOT_P}/lame-build/armeabi-v7a/
cp -f ${ROOT_P}/../obj/local/arm64-v8a/libmp3lame.a ${ROOT_P}/lame-build/arm64-v8a/
cp -f ${ROOT_P}/../obj/local/x86/libmp3lame.a ${ROOT_P}/lame-build/x86/
cp -f ${ROOT_P}/../obj/local/x86_64/libmp3lame.a ${ROOT_P}/lame-build/x86_64/
cp -f ${ROOT_P}/lame-3.100/include/lame.h ${ROOT_P}/lame-build/lame/