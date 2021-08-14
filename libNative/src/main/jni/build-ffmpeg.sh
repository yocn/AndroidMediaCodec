#!/bin/bash

TARGET_ARCH=$1

if test -z ${TARGET_ARCH}
then
    echo "flavor in [neon arm64 x84 x86_64] using default arm64"
    TARGET_ARCH=arm64
fi

if [ "${TARGET_ARCH}" == "arm" ]; then
  ARCH_DIR=armeabi-v7a
  CPU=armv7-a
  ARCH=arm
  CC=armv7a-linux-androideabi21-clang
  CXX=armv7a-linux-androideabi21-clang++
  STRIP=arm-linux-androideabi-strip
  CROSS_PREFIX=$NDK/toolchains/llvm/prebuilt/darwin-x86_64/bin/arm-linux-androideabi-
elif [ "${TARGET_ARCH}" == "arm64" ]; then
  ARCH_DIR=arm64-v8a
  CPU=armv8-a
  ARCH=aarch64
  CC=aarch64-linux-android21-clang
  CXX=aarch64-linux-android21-clang++
  STRIP=aarch64-linux-android-strip
  CROSS_PREFIX=$NDK/toolchains/llvm/prebuilt/darwin-x86_64/bin/aarch64-linux-android-
  EXTRA_CFLAGS+=" -fstack-protector -fstrict-aliasing"
elif [ "${TARGET_ARCH}" == "x86" ]; then
  ARCH_DIR=x86
  CPU=atom
  ARCH=x86
  CC=i686-linux-android21-clang
  CXX=i686-linux-android21-clang++
  STRIP=i686-linux-android-strip
  CROSS_PREFIX=$NDK/toolchains/llvm/prebuilt/darwin-x86_64/bin/i686-linux-android-
elif [ "${TARGET_ARCH}" == "x86_64" ]; then
  ARCH_DIR=x86_64
  CPU=atom
  ARCH=x86_64
  CC=x86_64-linux-android21-clang
  CXX=x86_64-linux-android21-clang++
  STRIP=x86_64-linux-android-strip
  CROSS_PREFIX=$NDK/toolchains/llvm/prebuilt/darwin-x86_64/bin/x86_64-linux-android-

fi
#!/bin/bash
#NDK=/Users/lin/Library/Android/sdk/ndk/20.1.5948944
#ADDI_LDFLAGS="-fPIE -pie"

#ADDI_CFLAGS="-fPIE -pie -march=armv7-a -mfloat-abi=softfp -mfpu=neon"
#EXTRA_CFLAGS="-mtune=atom -msse3 -mssse3 -mfpmath=sse"
LAME_ROOT=$(pwd)/lame-build
X264_ROOT=$(pwd)/x264
EXTRA_CFLAGS+=" -Wno-deprecated-declarations -Wno-unused-variable -Wno-unused-function"
EXTRA_CFLAGS+=" -I ${LAME_ROOT} -I ${X264_ROOT}/install/include -ftree-vectorize -ffunction-sections -funwind-tables -fomit-frame-pointer -no-canonical-prefixes -pipe"

#ADDI_CFLAGS="-O2 -fpic -I ${LAME_ROOT}-ftree-vectorize -ffunction-sections -funwind-tables -fomit-frame-pointer -no-canonical-prefixes -pipe"
ADDI_LDFLAGS="-L ${LAME_ROOT}/${ARCH_DIR} -L ${X264_ROOT}/install/lib "
SYSROOT=$NDK/toolchains/llvm/prebuilt/darwin-x86_64/sysroot
TOOLCHAIN=$NDK/toolchains/llvm/prebuilt/darwin-x86_64/bin
PREFIX=$(pwd)/ffmpeg-build/${ARCH_DIR}

cd ffmpeg

configure() {
  ./configure \
    --prefix=$PREFIX \
    --enable-cross-compile \
    --cross-prefix=$CROSS_PREFIX \
    --target-os=android \
    --arch=$ARCH \
    --cpu=$CPU \
    --sysroot=$SYSROOT \
    --cc=$TOOLCHAIN/${CC} \
    --cxx=$TOOLCHAIN/${CXX} \
    --strip=$TOOLCHAIN/${STRIP} \
    --extra-cflags="$EXTRA_CFLAGS" \
    --extra-ldflags="$ADDI_LDFLAGS" \
    --disable-doc \
    --disable-ffmpeg \
    --disable-ffplay \
    --disable-network \
    --disable-symver \
    --disable-postproc \
    --enable-shared \
    --enable-static \
    --disable-ffprobe \
    --enable-gpl \
    --enable-pic \
    --enable-libmp3lame \
    --enable-jni \
    --enable-mediacodec \
    --enable-libx264 \

}

build() {
  configure
  make clean
  make -j4
  make install
}

build
