# 🚀 Shihab's Execution Guide - STplayer Testing

## 📋 **Step-by-Step Instructions for Testing**

This guide provides exact commands and expected outcomes for testing the STplayer app with encoder-decoder ONNX models.

---

## **Step 1: Build APK in Android Studio Cloud**

### **1.1 Access Cloud Environment**
```bash
# Connect to Android Studio Cloud environment
# Navigate to project directory
cd /workspace/STplayer

# Verify you're in the right location
pwd
# Expected output: /workspace/STplayer
```

### **1.2 Verify Project Structure**
```bash
# Check that all files are present
ls -la
# Should show: app/, gradle/, build.gradle.kts, etc.

# Verify translation models are in place
ls -la app/src/main/assets/translation/
# Should show:
# - translation_en_ar_encoder_int8.onnx (49MB)
# - translation_en_ar_decoder_int8.onnx (179MB)
# - README.md
```

### **1.3 Build the APK**
```bash
# Clean previous builds (optional but recommended)
./gradlew clean

# Build debug APK
./gradlew assembleDebug --no-daemon

# Expected output:
# BUILD SUCCESSFUL in X seconds
# 1 actionable task: 1 executed
```

### **1.4 Verify APK Creation**
```bash
# Check APK was created
ls -la app/build/outputs/apk/debug/
# Should show: app-debug.apk

# Check APK size (should be ~250MB+ due to ONNX models)
ls -lh app/build/outputs/apk/debug/app-debug.apk
# Expected: ~250-300MB
```

---

## **Step 2: Deploy to Tablet via ADB**

### **2.1 Connect and Verify Tablet**
```bash
# Check if tablet is connected
adb devices
# Expected output:
# List of devices attached
# [DEVICE_ID]    device

# If no devices shown, check USB connection and enable USB debugging
```

### **2.2 Install APK**
```bash
# Install the APK
adb install app/build/outputs/apk/debug/app-debug.apk

# Expected output:
# Success

# Verify installation
adb shell pm list packages | grep stplayer
# Expected output:
# package:com.taazali.stplayer
```

### **2.3 Verify App Permissions**
```bash
# Check app permissions
adb shell dumpsys package com.taazali.stplayer | grep -A 10 "requested permissions"
# Should show audio and storage permissions
```

---

## **Step 3: Monitor Logs for Encoder-Decoder Pipeline**

### **3.1 Clear Previous Logs**
```bash
# Clear logcat buffer
adb logcat -c
```

### **3.2 Launch App and Start Monitoring**
```bash
# Launch the app
adb shell am start -n com.taazali.stplayer/.MainActivity

# Start monitoring logs (run this in a separate terminal)
adb logcat | grep -E "(MAIN|WHISPER|TRANSLATION|SUBTITLE)"
```

### **3.3 Expected Log Sequence**
You should see this sequence in the logs:

```
🚀 [MAIN] Starting STplayer initialization...
🔧 [MAIN] Initializing Whisper transcription...
❌ [MAIN] Whisper model not available: ggml-base.en.bin
🔧 [MAIN] Available models: []
🔧 [MAIN] Initializing ONNX translation...
=== Testing Model Detection ===
Found 3 ONNX models in assets/translation/:
  - translation_en_ar.onnx
  - translation_en_ar_decoder_int8.onnx
  - translation_en_ar_encoder_int8.onnx
Model Detection Results:
  Total models: 3
  Encoder models: [translation_en_ar_encoder_int8.onnx]
  Decoder models: [translation_en_ar_decoder_int8.onnx]
  Complete pairs: [translation_en_ar]
✅ [MAIN] Encoder-decoder pair found: translation_en_ar
  - Encoder: translation_en_ar_encoder_int8.onnx
  - Decoder: translation_en_ar_decoder_int8.onnx
🔧 [TRANSLATION] Starting model loading process...
🔧 [TRANSLATION] Model base name: translation_en_ar
🔧 [TRANSLATION] Language pair: en → ar
🔧 [TRANSLATION] Looking for encoder-decoder models:
  - Encoder: translation_en_ar_encoder_int8.onnx
  - Decoder: translation_en_ar_decoder_int8.onnx
✅ [TRANSLATION] Both encoder and decoder files found
  - Encoder size: 51749190 bytes
  - Decoder size: 187244433 bytes
🔧 [TRANSLATION] Loading encoder-decoder architecture...
🔧 [TRANSLATION] Session options configured:
  - Intra-op threads: 2
  - Inter-op threads: 1
  - Execution mode: ORT_PARALLEL
🔧 [TRANSLATION] Loading encoder model: translation_en_ar_encoder_int8.onnx
✅ [TRANSLATION] Encoder model loaded in [X]ms
🔧 [TRANSLATION] Loading decoder model: translation_en_ar_decoder_int8.onnx
✅ [TRANSLATION] Decoder model loaded in [X]ms
🔧 [TRANSLATION] Initializing tokenizer for en → ar
✅ [TRANSLATION] Encoder-decoder models loaded successfully in [X]ms
✅ [TRANSLATION] Translation: en → ar
✅ [TRANSLATION] Encoder path: /data/data/com.taazali.stplayer/cache/translation_en_ar_encoder_int8.onnx
✅ [TRANSLATION] Decoder path: /data/data/com.taazali.stplayer/cache/translation_en_ar_decoder_int8.onnx
✅ [MAIN] ONNX encoder-decoder models loaded successfully
🔧 [MAIN] Starting audio capture...
🔧 [MAIN] Configuring subtitle pipeline...
🔧 [MAIN] Starting subtitle simulation...
✅ [MAIN] STplayer initialization completed
```

### **3.4 Monitor Translation Pipeline**
After initialization, you should see translation logs every 3 seconds:

```
🔧 [SUBTITLE] Processing transcription: 'Welcome to STplayer!'
🔧 [SUBTITLE] Language configuration:
  - Source language: en
  - Target language: ar
  - Translation needed: true
🔧 [SUBTITLE] Translation needed - sending to translation pipeline
🔧 [SUBTITLE] Processing translation...
🔧 [SUBTITLE] Original text: 'Welcome to STplayer!'
🔧 [SUBTITLE] Translation: en → ar
🔧 [TRANSLATION] Starting translation: 'Welcome to STplayer!'
🔧 [TRANSLATION] Language pair: en → ar
🔧 [TRANSLATION] Tokenizing input text...
✅ [TRANSLATION] Tokenization completed in [X]ms
  - Input tokens: [X] tokens
  - Token array: [X, X, X, X, X, X, X, X, X, X]...
🔧 [TRANSLATION] Using encoder-decoder architecture
🔧 [TRANSLATION] Starting encoder-decoder translation...
🔧 [TRANSLATION] Step 1: Encoding input tokens...
🔧 [TRANSLATION] Running encoder inference...
✅ [TRANSLATION] Encoder completed in [X]ms
✅ [TRANSLATION] Encoder output shape: [1, X, X]
🔧 [TRANSLATION] Step 2: Decoding to target language...
🔧 [TRANSLATION] Running decoder inference...
✅ [TRANSLATION] Decoder completed in [X]ms
✅ [TRANSLATION] Decoder output shape: [1, X, X]
🔧 [TRANSLATION] Decoding output tokens to text...
✅ [TRANSLATION] Encoder-decoder translation completed in [X]ms
✅ [TRANSLATION] Result: '[TOKEN_X TOKEN_Y TOKEN_Z]'
✅ [TRANSLATION] Translation completed in [X]ms
✅ [TRANSLATION] en → ar: 'Welcome to STplayer!' → '[TOKEN_X TOKEN_Y TOKEN_Z]'
✅ [TRANSLATION] Memory usage: [X]MB
✅ [SUBTITLE] Translation result: '[TOKEN_X TOKEN_Y TOKEN_Z]'
```

---

## **Step 4: Verify Performance and Report Results**

### **4.1 Performance Monitoring Commands**
```bash
# Monitor app performance
adb shell top | grep stplayer

# Check memory usage
adb shell dumpsys meminfo com.taazali.stplayer

# Monitor CPU usage
adb shell dumpsys cpuinfo | grep stplayer

# Check for any crashes
adb logcat | grep -E "(FATAL|CRASH|AndroidRuntime)"
```

### **4.2 Expected Performance Metrics**
- **Encoder Load Time**: 2-5 seconds
- **Decoder Load Time**: 5-10 seconds
- **Total Model Loading**: 7-15 seconds
- **Translation Time**: 50-130ms per sentence
- **Memory Usage**: 200-400MB total
- **CPU Usage**: < 50% during translation

### **4.3 Test Report Template**

Create a file called `TEST_RESULTS.md` with this template:

```markdown
# STplayer Test Results

## Test Information
- **Date**: [Current Date]
- **Device**: [Tablet Model]
- **Android Version**: [Version]
- **APK Version**: 1.0

## Model Loading Results
- **Encoder Load Time**: [X]ms
- **Decoder Load Time**: [X]ms
- **Total Loading Time**: [X]ms
- **Loading Success**: ✅/❌

## Translation Performance
- **Average Translation Time**: [X]ms
- **Fastest Translation**: [X]ms
- **Slowest Translation**: [X]ms
- **Translation Success Rate**: [X]%

## Memory Usage
- **Peak Memory Usage**: [X]MB
- **Average Memory Usage**: [X]MB
- **Memory Stable**: ✅/❌

## Issues Found
- [List any issues encountered]

## Recommendations
- [List any recommendations for improvements]

## Overall Assessment
- **App Launch**: ✅/❌
- **Model Loading**: ✅/❌
- **Translation Pipeline**: ✅/❌
- **UI/UX**: ✅/❌
- **Performance**: ✅/❌
```

---

## **🚨 Troubleshooting**

### **If APK Build Fails**
```bash
# Check Gradle version
./gradlew --version

# Check Android SDK
echo $ANDROID_HOME

# Try with more verbose output
./gradlew assembleDebug --info
```

### **If Installation Fails**
```bash
# Uninstall previous version
adb uninstall com.taazali.stplayer

# Try installation again
adb install app/build/outputs/apk/debug/app-debug.apk
```

### **If Models Don't Load**
```bash
# Check if models are in APK
adb shell run-as com.taazali.stplayer ls -la /data/data/com.taazali.stplayer/cache/

# Check app logs for model loading errors
adb logcat | grep -E "(TRANSLATION|ONNX|Model)"
```

### **If App Crashes**
```bash
# Get crash logs
adb logcat | grep -E "(FATAL|CRASH|AndroidRuntime)"

# Check app state
adb shell dumpsys activity com.taazali.stplayer
```

---

## **✅ Success Criteria**

The test is successful if:

1. **✅ APK builds without errors**
2. **✅ APK installs successfully**
3. **✅ App launches without crashes**
4. **✅ Both encoder and decoder models load**
5. **✅ Translation pipeline works**
6. **✅ Performance is within expected ranges**
7. **✅ UI shows translated subtitles**

---

## **📤 Report Back**

After completing the test:

1. **Share the test results** using the template above
2. **Include any error logs** if issues occurred
3. **Provide performance metrics** from the monitoring
4. **Note any unexpected behavior** or crashes

**Ready to execute! Follow these steps exactly and report back with the results.** 🚀 