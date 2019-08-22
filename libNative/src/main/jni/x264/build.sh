#!/bin/bash

NDK=/Users/yocn/android-ndk-r12b
SYSROOT=$NDK/platforms/android-9/arch-arm/
TOOLCHAIN=$NDK/toolchains/arm-linux-androideabi-4.9/prebuilt/darwin-x86_64
function build_one
{
./configure \
--enable-pic \
--enable-strip \
--enable-static \
--cross-prefix=$TOOLCHAIN/bin/arm-linux-androideabi- \
--sysroot=$SYSROOT \
--host=arm-linux \
--prefix=./build \
--extra-cflags="-march=armv7-a -mtune=cortex-a8 -mfloat-abi=softfp -mfpu=neon -D__ARM_ARCH_7__ -D__ARM_ARCH_7A__"

make clean
make
make install
}
build_one
