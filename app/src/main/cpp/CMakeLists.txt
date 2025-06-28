# CMake configuration for Android NDK with Whisper.cpp integration
# Optimized for CMake 3.28.3 and NDK 21.4.7075529

cmake_minimum_required(VERSION 3.28.3)

project("stplayer")

# Set the native library name
set(LIBRARY_NAME "whispercpp")

# Create the shared library with minimal configuration
add_library(${LIBRARY_NAME} SHARED
    whispercpp/whisper_jni.cpp
)

# TODO: Add Whisper.cpp source files when integrated
# add_library(${LIBRARY_NAME} SHARED
#     whispercpp/whisper_jni.cpp
#     whispercpp/whisper.cpp
#     whispercpp/ggml.c
#     # Add other Whisper.cpp dependencies
# )

# Find and link required libraries
find_library(log-lib log)

# Link libraries
target_link_libraries(${LIBRARY_NAME}
    ${log-lib}
    # TODO: Add other required libraries when integrating Whisper.cpp
)

# Include directories
target_include_directories(${LIBRARY_NAME} PRIVATE
    whispercpp/
    # TODO: Add Whisper.cpp include directories when integrated
)

# Compiler flags optimized for Android
target_compile_options(${LIBRARY_NAME} PRIVATE
    -Wall
    -Wextra
    -O2
    -fPIC
    # Platform-specific optimizations
    $<$<AND:$<CONFIG:Release>,$<STREQUAL:${ANDROID_ABI},arm64-v8a>>:-march=armv8-a>
    $<$<AND:$<CONFIG:Release>,$<STREQUAL:${ANDROID_ABI},x86_64>>:-march=x86-64>
)

# Set C++ standard
set_target_properties(${LIBRARY_NAME} PROPERTIES
    CXX_STANDARD 17
    CXX_STANDARD_REQUIRED YES
    CXX_EXTENSIONS NO
) 