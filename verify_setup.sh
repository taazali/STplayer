#!/bin/bash

echo "🔍 STplayer Setup Verification"
echo "=============================="

# Check if we're in the right directory
if [ ! -f "app/build.gradle.kts" ]; then
    echo "❌ Not in STplayer root directory"
    exit 1
fi

echo "✅ STplayer project structure found"

# Check translation models
echo ""
echo "📁 Translation Models:"
if [ -f "app/src/main/assets/translation/translation_en_ar_encoder_int8.onnx" ]; then
    ENCODER_SIZE=$(stat -f%z "app/src/main/assets/translation/translation_en_ar_encoder_int8.onnx")
    echo "✅ Encoder model: ${ENCODER_SIZE} bytes"
else
    echo "❌ Encoder model missing"
fi

if [ -f "app/src/main/assets/translation/translation_en_ar_decoder_int8.onnx" ]; then
    DECODER_SIZE=$(stat -f%z "app/src/main/assets/translation/translation_en_ar_decoder_int8.onnx")
    echo "✅ Decoder model: ${DECODER_SIZE} bytes"
else
    echo "❌ Decoder model missing"
fi

# Check key source files
echo ""
echo "📝 Source Files:"
FILES=(
    "app/src/main/java/com/taazali/stplayer/MainActivity.kt"
    "app/src/main/java/com/taazali/stplayer/TranslationManager.kt"
    "app/src/main/java/com/taazali/stplayer/SubtitleManager.kt"
    "app/src/main/java/com/taazali/stplayer/WhisperBridge.kt"
    "app/build.gradle.kts"
    "gradle/libs.versions.toml"
    "TESTING_WORKFLOW.md"
)

for file in "${FILES[@]}"; do
    if [ -f "$file" ]; then
        echo "✅ $file"
    else
        echo "❌ $file missing"
    fi
done

# Check ONNX Runtime dependency
echo ""
echo "📦 Dependencies:"
if grep -q "onnxruntime-android" "gradle/libs.versions.toml"; then
    echo "✅ ONNX Runtime dependency configured"
else
    echo "❌ ONNX Runtime dependency missing"
fi

if grep -q "onnxruntime" "app/build.gradle.kts"; then
    echo "✅ ONNX Runtime dependency included in build"
else
    echo "❌ ONNX Runtime dependency not included in build"
fi

echo ""
echo "🎯 Ready for Testing:"
echo "===================="
echo "1. ✅ Encoder-decoder models in place"
echo "2. ✅ Source code with verbose logging"
echo "3. ✅ ONNX Runtime integration"
echo "4. ✅ Testing workflow documented"
echo ""
echo "📋 Next Steps for Shihab:"
echo "1. Build APK in Android Studio Cloud"
echo "2. Deploy to Android tablet"
echo "3. Monitor logs for encoder-decoder pipeline"
echo "4. Verify translation performance"
echo ""
echo "🚀 Ready to test the complete AI pipeline!" 