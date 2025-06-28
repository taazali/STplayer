# STplayer Testing Workflow for Shihab

## 🎯 **Testing Objective**
Test the complete STplayer pipeline with real encoder-decoder ONNX models for English→Arabic translation.

## 📋 **Pre-Test Checklist**

### **Hardware Setup**
- [ ] Android tablet connected via USB
- [ ] USB debugging enabled
- [ ] Tablet has sufficient storage (>500MB free)
- [ ] Tablet has sufficient RAM (>2GB available)

### **Software Setup**
- [ ] Android Studio Cloud environment ready
- [ ] Latest STplayer code pulled from repository
- [ ] Encoder-decoder models in place:
  - `translation_en_ar_encoder_int8.onnx` (49MB)
  - `translation_en_ar_decoder_int8.onnx` (179MB)

## 🚀 **Step-by-Step Testing Workflow**

### **Step 1: Build APK**
```bash
# In Android Studio Cloud
cd /workspace/STplayer
./gradlew assembleDebug --no-daemon
```

**Expected Output:**
- Build successful
- APK generated at `app/build/outputs/apk/debug/app-debug.apk`
- No compilation errors

### **Step 2: Deploy to Tablet**
```bash
# Install APK directly to connected tablet
adb install app/build/outputs/apk/debug/app-debug.apk

# Verify installation
adb shell pm list packages | grep stplayer
```

**Expected Output:**
- Installation successful
- Package `com.taazali.stplayer` appears in package list

### **Step 3: Launch App with Logging**
```bash
# Launch app and capture logs
adb logcat -c  # Clear previous logs
adb shell am start -n com.taazali.stplayer/.MainActivity
adb logcat | grep -E "(MAIN|WHISPER|TRANSLATION|SUBTITLE)"
```

### **Step 4: Monitor App Launch**

**Expected Log Sequence:**
```
🚀 [MAIN] Starting STplayer initialization...
🔧 [MAIN] Initializing Whisper transcription...
❌ [MAIN] Whisper model not available: ggml-base.en.bin
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
🔧 [TRANSLATION] Starting model loading process...
🔧 [TRANSLATION] Looking for encoder-decoder models:
  - Encoder: translation_en_ar_encoder_int8.onnx
  - Decoder: translation_en_ar_decoder_int8.onnx
✅ [TRANSLATION] Both encoder and decoder files found
  - Encoder size: 51749190 bytes
  - Decoder size: 187244433 bytes
🔧 [TRANSLATION] Loading encoder-decoder architecture...
✅ [TRANSLATION] Encoder model loaded in XXXms
✅ [TRANSLATION] Decoder model loaded in XXXms
✅ [TRANSLATION] Encoder-decoder models loaded successfully in XXXms
✅ [MAIN] ONNX encoder-decoder models loaded successfully
🔧 [MAIN] Starting audio capture...
🔧 [MAIN] Configuring subtitle pipeline...
🔧 [MAIN] Starting subtitle simulation...
✅ [MAIN] STplayer initialization completed
```

### **Step 5: Monitor Subtitle Pipeline**

**Expected Log Sequence (Every 3 seconds):**
```
🔧 [MAIN] Processing simulated subtitle: 'Welcome to STplayer!'
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
✅ [TRANSLATION] Tokenization completed in Xms
🔧 [TRANSLATION] Using encoder-decoder architecture
🔧 [TRANSLATION] Starting encoder-decoder translation...
🔧 [TRANSLATION] Step 1: Encoding input tokens...
✅ [TRANSLATION] Encoder completed in Xms
🔧 [TRANSLATION] Step 2: Decoding to target language...
✅ [TRANSLATION] Decoder completed in Xms
✅ [TRANSLATION] Encoder-decoder translation completed in Xms
✅ [TRANSLATION] Result: '[TOKEN_X TOKEN_Y TOKEN_Z]'
✅ [TRANSLATION] Translation completed in Xms
✅ [TRANSLATION] en → ar: 'Welcome to STplayer!' → '[TOKEN_X TOKEN_Y TOKEN_Z]'
✅ [SUBTITLE] Translation result: '[TOKEN_X TOKEN_Y TOKEN_Z]'
```

## 📊 **Performance Benchmarks**

### **Model Loading Times**
- **Encoder Model**: < 5 seconds
- **Decoder Model**: < 10 seconds
- **Total Loading**: < 15 seconds

### **Translation Performance**
- **Tokenization**: < 10ms
- **Encoder Inference**: 20-50ms
- **Decoder Inference**: 30-80ms
- **Total Translation**: 50-130ms

### **Memory Usage**
- **Encoder Model**: ~50MB RAM
- **Decoder Model**: ~180MB RAM
- **Total ONNX**: ~230MB RAM
- **App Total**: < 500MB RAM

## 🔍 **What to Look For**

### **✅ Success Indicators**
1. **Model Detection**: Both encoder and decoder models found
2. **Model Loading**: Both models load without errors
3. **Translation Pipeline**: Encoder-decoder architecture used
4. **Performance**: Translation times within expected ranges
5. **UI Updates**: Subtitle overlay shows translated text
6. **No Crashes**: App runs continuously without crashes

### **❌ Problem Indicators**
1. **Model Not Found**: Check file names and paths
2. **Loading Failures**: Check ONNX format and compatibility
3. **Slow Performance**: Check device capabilities
4. **Memory Issues**: Check available RAM
5. **Crashes**: Check for native library issues

## 🐛 **Troubleshooting**

### **Model Loading Issues**
```bash
# Check if models are in APK
adb shell run-as com.taazali.stplayer ls -la /data/data/com.taazali.stplayer/cache/
```

### **Performance Issues**
```bash
# Monitor device performance
adb shell top | grep stplayer
adb shell dumpsys meminfo com.taazali.stplayer
```

### **Log Analysis**
```bash
# Filter for specific components
adb logcat | grep -E "(ERROR|FATAL|CRASH)"
adb logcat | grep -E "(TRANSLATION|WHISPER)" | tail -50
```

## 📝 **Test Report Template**

### **Test Results Summary**
- **Date**: [Date]
- **Device**: [Tablet Model]
- **Android Version**: [Version]
- **APK Version**: [Version]

### **Model Loading**
- [ ] Encoder model loaded successfully
- [ ] Decoder model loaded successfully
- [ ] Loading time within expected range
- [ ] No loading errors

### **Translation Pipeline**
- [ ] Encoder-decoder architecture detected
- [ ] Translation pipeline functional
- [ ] Performance within expected ranges
- [ ] No translation errors

### **UI/UX**
- [ ] App launches successfully
- [ ] Subtitle overlay displays
- [ ] Translation status indicator shows
- [ ] No UI crashes

### **Performance Metrics**
- **Encoder Load Time**: [X]ms
- **Decoder Load Time**: [X]ms
- **Average Translation Time**: [X]ms
- **Memory Usage**: [X]MB

### **Issues Found**
- [List any issues encountered]

### **Recommendations**
- [List any recommendations for improvements]

## 🎯 **Next Steps**

After successful testing:
1. **Report Results**: Share test results with ChatGPT and Cursor
2. **Performance Optimization**: If needed, adjust thread counts and quality settings
3. **Model Optimization**: Consider model quantization or size reduction
4. **Feature Enhancement**: Plan next development milestones

---

**Remember**: This is a comprehensive test of the complete AI pipeline. Take detailed notes and capture any unexpected behavior for further analysis. 