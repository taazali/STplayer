# More flexible CMake version requirement for cloud environments
cmake_minimum_required(VERSION 3.10.2)

project("stplayer")

# Set the native library name
set(LIBRARY_NAME "whispercpp")

# Create the shared library
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

# Compiler flags
target_compile_options(${LIBRARY_NAME} PRIVATE
    -Wall
    -Wextra
    -O2
    # TODO: Add platform-specific flags for ARM64/x86 when needed
)

# Set C++ standard
set_target_properties(${LIBRARY_NAME} PROPERTIES
    CXX_STANDARD 17
    CXX_STANDARD_REQUIRED YES
) 