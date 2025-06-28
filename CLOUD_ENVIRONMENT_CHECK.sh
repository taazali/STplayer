#!/bin/bash

echo "🔍 Android Studio Cloud Environment Analysis"
echo "=============================================="
echo ""

echo "📋 System Information:"
echo "======================"
echo "OS: $(uname -a)"
echo "Architecture: $(uname -m)"
echo "Kernel: $(uname -r)"
echo "Hostname: $(hostname)"
echo ""

echo "📦 Java Environment:"
echo "==================="
echo "JAVA_HOME: $JAVA_HOME"
echo "Java version:"
java -version 2>&1
echo ""

echo "🔧 Gradle Environment:"
echo "====================="
echo "Gradle version:"
./gradlew --version 2>/dev/null || echo "Gradle wrapper not found"
echo ""

echo "📱 Android SDK:"
echo "=============="
echo "ANDROID_HOME: $ANDROID_HOME"
echo "ANDROID_SDK_ROOT: $ANDROID_SDK_ROOT"
echo ""

if [ -n "$ANDROID_HOME" ]; then
    echo "Android SDK Contents:"
    ls -la "$ANDROID_HOME" 2>/dev/null || echo "ANDROID_HOME not accessible"
    echo ""
    
    echo "Android SDK Platforms:"
    ls -la "$ANDROID_HOME/platforms" 2>/dev/null || echo "No platforms directory"
    echo ""
    
    echo "Android SDK Build Tools:"
    ls -la "$ANDROID_HOME/build-tools" 2>/dev/null || echo "No build-tools directory"
    echo ""
    
    echo "Android SDK NDK:"
    ls -la "$ANDROID_HOME/ndk" 2>/dev/null || echo "No NDK directory"
    echo ""
    
    echo "Android SDK CMake:"
    ls -la "$ANDROID_HOME/cmake" 2>/dev/null || echo "No CMake directory"
    echo ""
    
    echo "Android SDK Extras:"
    ls -la "$ANDROID_HOME/extras" 2>/dev/null || echo "No extras directory"
    echo ""
    
    echo "Android SDK Licenses:"
    ls -la "$ANDROID_HOME/licenses" 2>/dev/null || echo "No licenses directory"
    echo ""
fi

echo "🔨 Build Tools:"
echo "=============="
echo "CMake version:"
cmake --version 2>/dev/null || echo "CMake not found"
echo ""

echo "Ninja version:"
ninja --version 2>/dev/null || echo "Ninja not found"
echo ""

echo "NDK version:"
if [ -n "$ANDROID_HOME" ]; then
    ls -la "$ANDROID_HOME/ndk" 2>/dev/null | head -5 || echo "NDK not found"
fi
echo ""

echo "📊 System Resources:"
echo "==================="
echo "CPU cores: $(nproc)"
echo "Memory:"
free -h 2>/dev/null || echo "Memory info not available"
echo ""

echo "Disk space:"
df -h . 2>/dev/null || echo "Disk info not available"
echo ""

echo "🔍 Environment Variables:"
echo "======================="
echo "PATH: $PATH"
echo ""

echo "📁 Directory Structure Analysis:"
echo "==============================="
echo "Current working directory: $(pwd)"
echo ""

echo "Home directory:"
echo "ls -la ~"
ls -la ~ 2>/dev/null | head -10 || echo "Home directory not accessible"
echo ""

echo "Root directory structure:"
echo "ls -la /"
ls -la / 2>/dev/null | head -15 || echo "Root directory not accessible"
echo ""

echo "Common Android directories:"
echo "=========================="
echo "ls -la /opt/android*"
ls -la /opt/android* 2>/dev/null || echo "No /opt/android directories"
echo ""

echo "ls -la /usr/local/android*"
ls -la /usr/local/android* 2>/dev/null || echo "No /usr/local/android directories"
echo ""

echo "ls -la /home/*/android*"
ls -la /home/*/android* 2>/dev/null || echo "No /home/*/android directories"
echo ""

echo "ls -la /workspace*"
ls -la /workspace* 2>/dev/null || echo "No /workspace directories"
echo ""

echo "ls -la /tmp"
ls -la /tmp 2>/dev/null | head -10 || echo "No /tmp directory"
echo ""

echo "📦 Package Managers and Tools:"
echo "============================="
echo "Available in PATH:"
which cmake 2>/dev/null || echo "CMake not in PATH"
which ninja 2>/dev/null || echo "Ninja not in PATH"
which java 2>/dev/null || echo "Java not in PATH"
which gradle 2>/dev/null || echo "Gradle not in PATH"
which android 2>/dev/null || echo "Android CLI not in PATH"
which adb 2>/dev/null || echo "ADB not in PATH"
which sdkmanager 2>/dev/null || echo "SDK Manager not in PATH"
echo ""

echo "apt packages (if available):"
apt list --installed 2>/dev/null | grep -E "(cmake|ninja|android|ndk|openjdk)" | head -10 || echo "apt not available"
echo ""

echo "yum packages (if available):"
yum list installed 2>/dev/null | grep -E "(cmake|ninja|android|ndk|openjdk)" | head -10 || echo "yum not available"
echo ""

echo "📋 File System Details:"
echo "======================"
echo "File system type:"
df -T . 2>/dev/null || echo "File system info not available"
echo ""

echo "Inode information:"
df -i . 2>/dev/null || echo "Inode info not available"
echo ""

echo "🔧 Build Tool Locations:"
echo "======================="
echo "CMake locations:"
find /usr -name "cmake" 2>/dev/null | head -5 || echo "CMake not found in /usr"
find /opt -name "cmake" 2>/dev/null | head -5 || echo "CMake not found in /opt"
echo ""

echo "Ninja locations:"
find /usr -name "ninja" 2>/dev/null | head -5 || echo "Ninja not found in /usr"
find /opt -name "ninja" 2>/dev/null | head -5 || echo "Ninja not found in /opt"
echo ""

echo "Java installations:"
find /usr -name "java" 2>/dev/null | grep -E "(bin/java)" | head -5 || echo "Java not found in /usr"
find /opt -name "java" 2>/dev/null | grep -E "(bin/java)" | head -5 || echo "Java not found in /opt"
echo ""

echo "🏁 Analysis Complete!"
echo "==================="
echo "Copy the output above and share it for perfect project configuration." 