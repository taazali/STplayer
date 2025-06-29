#!/bin/bash

echo "🔍 STplayer Pre-Build Verification Checklist"
echo "=============================================="
echo ""

# 1. Check Model Assets
echo "📦 1. Checking Model Assets..."
echo "--------------------------------"

echo "Translation models:"
if [ -d "app/src/main/assets/translation" ]; then
    ls -lh app/src/main/assets/translation/
    echo ""
    
    # Check specific files
    if [ -f "app/src/main/assets/translation/translation_en_ar_encoder_int8.onnx" ]; then
        echo "✅ Encoder model found"
    else
        echo "❌ Encoder model missing"
    fi
    
    if [ -f "app/src/main/assets/translation/translation_en_ar_decoder_int8.onnx" ]; then
        echo "✅ Decoder model found"
    else
        echo "❌ Decoder model missing"
    fi
    
    # Check file permissions
    echo ""
    echo "File permissions:"
    ls -la app/src/main/assets/translation/
else
    echo "❌ Translation directory missing"
fi

echo ""
echo "Whisper models:"
if [ -d "app/src/main/assets/whisper" ]; then
    ls -lh app/src/main/assets/whisper/
    echo ""
    echo "File permissions:"
    ls -la app/src/main/assets/whisper/
else
    echo "❌ Whisper directory missing"
fi

echo ""
echo ""

# 2. Check NDK, CMake, and Ninja
echo "🔧 2. Checking Build Tools..."
echo "-----------------------------"

echo "CMake:"
if command -v cmake &> /dev/null; then
    echo "✅ CMake found at: $(which cmake)"
    cmake --version | head -1
else
    echo "❌ CMake not found"
fi

echo ""
echo "Ninja:"
if command -v ninja &> /dev/null; then
    echo "✅ Ninja found at: $(which ninja)"
    ninja --version
else
    echo "❌ Ninja not found"
fi

echo ""
echo "NDK:"
if [ -n "$ANDROID_HOME" ]; then
    echo "✅ ANDROID_HOME set to: $ANDROID_HOME"
    if [ -d "$ANDROID_HOME/ndk" ]; then
        echo "NDK versions available:"
        ls -la "$ANDROID_HOME/ndk/"
    else
        echo "❌ NDK directory not found"
    fi
else
    echo "❌ ANDROID_HOME not set"
fi

echo ""
echo ""

# 3. Check Java and Gradle
echo "☕ 3. Checking Java and Gradle..."
echo "--------------------------------"

echo "Java:"
if command -v java &> /dev/null; then
    echo "✅ Java found at: $(which java)"
    java -version
else
    echo "❌ Java not found"
fi

echo ""
echo "Gradle:"
if [ -f "gradlew" ]; then
    echo "✅ Gradle wrapper found"
    ./gradlew --version | head -3
else
    echo "❌ Gradle wrapper missing"
fi

echo ""
echo ""

# 4. Check Build Configuration
echo "⚙️ 4. Checking Build Configuration..."
echo "------------------------------------"

echo "build.gradle.kts syntax check:"
if ./gradlew projects > /dev/null 2>&1; then
    echo "✅ Gradle configuration is valid"
else
    echo "❌ Gradle configuration has errors"
fi

echo ""
echo "Dependencies check:"
if [ -f "gradle/libs.versions.toml" ]; then
    echo "✅ Version catalog found"
else
    echo "❌ Version catalog missing"
fi

echo ""
echo ""

# 5. Check File Permissions
echo "🔐 5. Checking File Permissions..."
echo "---------------------------------"

echo "Setting correct permissions for model files..."
if [ -d "app/src/main/assets/translation" ]; then
    chmod -R 644 app/src/main/assets/translation/*
    echo "✅ Translation model permissions set"
fi

if [ -d "app/src/main/assets/whisper" ]; then
    chmod -R 644 app/src/main/assets/whisper/*
    echo "✅ Whisper model permissions set"
fi

echo ""
echo ""

# 6. Summary
echo "📋 6. Verification Summary..."
echo "-----------------------------"

echo "Ready to build? Check the following:"
echo ""
echo "✅ All required models present and readable"
echo "✅ CMake and Ninja installed and accessible"
echo "✅ NDK version 21.4.7075529 available"
echo "✅ Java 17 available"
echo "✅ Gradle configuration valid"
echo "✅ File permissions correct"
echo ""

echo "🚀 If all checks pass, run: ./gradlew clean assembleDebug"
echo "" 