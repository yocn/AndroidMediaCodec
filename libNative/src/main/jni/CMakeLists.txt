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


# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        native-lib

        # Sets the library as a shared library.
        SHARED

        native-lib.cpp
        YUVTrans.cpp
        ffmpegTests.cpp
        )

INCLUDE_DIRECTORIES(
        ${CMAKE_SOURCE_DIR}
        ${CMAKE_SOURCE_DIR}/ffmpeg
        ${CMAKE_SOURCE_DIR}/libyuv/include
)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

find_library(z-lib z)
find_library(gl-lib GLESV2)

#string(TOLOWER "${CMAKE_BUILD_TYPE}" build_type)
#if (build_type MATCHES "^rel")
#    set_target_properties(native-lib PROPERTIES CXX_VISIBILITY_PRESET hidden)
#    set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,--version-script=${CMAKE_SOURCE_DIR}/version_script")
#endif ()

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

message("-----------------------message----------------------")
message(${third-libs})
MESSAGE("-----------------------message----------------------")
MESSAGE(${third-libs})
MESSAGE(${log-lib})

target_link_libraries( # Specifies the target library.
        native-lib
        ${third-libs}
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
        ${z-lib}
        ${gl-lib}
        yuv
        )