# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.10.2)

# Declares and names the project.

project("myapplication")

add_subdirectory(libyuv)

add_compile_options(-Wno-deprecated-declarations)
add_compile_options(-Wno-unused-variable)
add_compile_options(-Wno-unused-function)

if (ANDROID_ABI STREQUAL "arm64-v8a")
    add_definitions(-DTARGET_ARM64)
    add_compile_options(-fstack-protector)
    add_compile_options(-fstrict-aliasing)
    add_compile_options(-march=armv8-a)
    add_compile_options(-fPIC)
elseif (ANDROID_ABI STREQUAL "armeabi-v7a")
    add_definitions(-DTARGET_ARMV7)
    add_compile_options(-fstack-protector)
    add_compile_options(-fstrict-aliasing)
    add_compile_options(-mfloat-abi=softfp)
    add_compile_options(-mfpu=neon)
    add_compile_options(-mtune=cortex-a8)
    add_compile_options(-Wl,--fix-cortex-a8)
    add_compile_options(-march=armv7-a)
    add_compile_options(-fPIC)
endif ()

# 三方静态库命名为third-libs
set(
        third-libs
        ${CMAKE_SOURCE_DIR}/ffmpeg-build/${ANDROID_ABI}/lib/libavfilter.a
        ${CMAKE_SOURCE_DIR}/ffmpeg-build/${ANDROID_ABI}/lib/libavformat.a
        ${CMAKE_SOURCE_DIR}/ffmpeg-build/${ANDROID_ABI}/lib/libavcodec.a
        ${CMAKE_SOURCE_DIR}/ffmpeg-build/${ANDROID_ABI}/lib/libavutil.a
        ${CMAKE_SOURCE_DIR}/ffmpeg-build/${ANDROID_ABI}/lib/libswresample.a
        ${CMAKE_SOURCE_DIR}/ffmpeg-build/${ANDROID_ABI}/lib/libswscale.a
        ${CMAKE_SOURCE_DIR}/lame-build/${ANDROID_ABI}/libmp3lame.a
        ${CMAKE_SOURCE_DIR}/x264/install/lib/libx264.a
)

set(
        self-code

        code-self/native-lib.cpp
        code-self/YUVTrans.cpp
        code-self/common/Util.cpp
        code-self/common/JniProgress.cpp
        code-self/ffmpeg/ffmpegTests.cpp
        code-self/ffmpeg/FFMpegSimpleVideoPlayer.cpp
        code-self/ffmpeg/FFMpegSimpleAudioPlayer.cpp
        code-self/ffmpeg/FFMpegSimpleAudioConvert.cpp
        code-self/audio/play/opensl_render.cpp
        code-self/audio/encode/Mp3Lame.cpp
)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

#创建一个so，名字叫native-lib
add_library( # Sets the name of the library.
        native-lib

        # Sets the library as a shared library.
        SHARED

        ${self-code}
        )

INCLUDE_DIRECTORIES(
        ${CMAKE_SOURCE_DIR}
        ${CMAKE_SOURCE_DIR}/ffmpeg
        ${CMAKE_SOURCE_DIR}/code-self
        ${CMAKE_SOURCE_DIR}/code-self/common
        ${CMAKE_SOURCE_DIR}/code-self/audio/play
        ${CMAKE_SOURCE_DIR}/code-self/audio/encode
        ${CMAKE_SOURCE_DIR}/libyuv/include
)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

# 找到log库，命名为log-lib
find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)
#找到z库，命名为z-lib，必须使用z库，否则会报错
find_library(z-lib z)
find_library(gl-lib GLESV2)
find_library(OpenSLES-lib OpenSLES)

# 打印信息在.cxx/cmake/debug/arm64-v8a/cmake_build_output.txt
message("-----------------------message----------------------")
message(${third-libs})

target_link_libraries( # Specifies the target library.
        native-lib
        ${third-libs}
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
        ${z-lib}
        -landroid
        ${gl-lib}
        ${OpenSLES-lib}
        yuv
        )