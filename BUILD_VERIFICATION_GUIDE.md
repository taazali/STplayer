# 🔍 STplayer Build Verification Guide

## **Pre-Build Checklist (Based on ChatGPT Recommendations)**

### **Step 1: Run Verification Script**
```bash
# In your cloud environment
./verify_setup.sh
```

This script will check:
- ✅ Model assets (encoder/decoder ONNX files)
- ✅ Build tools (CMake, Ninja, NDK)
- ✅ Java and Gradle setup
- ✅ File permissions
- ✅ Build configuration validity

### **Step 2: Manual Verification (If Script Shows Issues)**

#### **Check Model Assets**
```bash
ls -lh app/src/main/assets/translation/
ls -lh app/src/main/assets/whisper/
```
**Expected**: Encoder (49MB), Decoder (179MB)

#### **Check Build Tools**
```bash
which cmake
which ninja
cmake --version
ninja --version
```
**Expected**: CMake 3.28.3, Ninja at `/usr/local/bin/ninja`

#### **Check NDK**
```bash
ls $ANDROID_HOME/ndk/
```
**Expected**: Folder `21.4.7075529` exists

#### **Check Java**
```bash
java -version
```
**Expected**: Java 17

### **Step 3: Fix Common Issues**

#### **If Models Missing**
```bash
# Check if files exist but are corrupted
file app/src/main/assets/translation/translation_en_ar_encoder_int8.onnx
file app/src/main/assets/translation/translation_en_ar_decoder_int8.onnx
```

#### **If Permissions Wrong**
```bash
chmod -R 644 app/src/main/assets/translation/*
chmod -R 644 app/src/main/assets/whisper/*
```

#### **If CMake/Ninja Not Found**
```bash
# Install if missing
sudo apt update
sudo apt install cmake ninja-build
```

### **Step 4: Clean Build**
```bash
./gradlew clean
./gradlew assembleDebug
```

### **Step 5: Install and Test**
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.taazali.stplayer/.MainActivity
```

### **Step 6: Monitor Logs**
```bash
adb logcat | grep -E "(MAIN|WHISPER|TRANSLATION|SUBTITLE)"
```

## **Expected Success Indicators**

### **Build Success**
- ✅ No compilation errors
- ✅ Native build completes
- ✅ APK generated successfully

### **Runtime Success**
- ✅ App launches without crashes
- ✅ Models load successfully
- ✅ Translation pipeline functional
- ✅ Subtitle overlay displays

### **Expected Log Sequence**
```
🚀 [MAIN] Starting STplayer initialization...
✅ [TRANSLATION] Encoder model loaded in XXXms
✅ [TRANSLATION] Decoder model loaded in XXXms
✅ [MAIN] ONNX encoder-decoder models loaded successfully
🔧 [MAIN] Starting subtitle simulation...
✅ [MAIN] STplayer initialization completed
```

## **Common Failure Points**

### **Build Failures**
1. **Missing AudioSink methods** → Check MainActivity.kt
2. **CMake/Ninja path issues** → Check build.gradle.kts
3. **Model file corruption** → Re-download models
4. **Permission issues** → Fix file permissions

### **Runtime Failures**
1. **Model loading errors** → Check assets directory
2. **Native library errors** → Check NDK configuration
3. **Memory issues** → Check device memory
4. **Permission denied** → Check app permissions

## **Troubleshooting Commands**

### **If Build Fails**
```bash
./gradlew assembleDebug --stacktrace
./gradlew assembleDebug --info
```

### **If App Crashes**
```bash
adb logcat | grep -E "(FATAL|CRASH|AndroidRuntime)"
```

### **If Models Don't Load**
```bash
adb shell run-as com.taazali.stplayer ls -la /data/data/com.taazali.stplayer/cache/
```

## **Success Criteria**

**✅ BUILD SUCCESS**: Clean build with no errors
**✅ INSTALL SUCCESS**: APK installs without issues
**✅ LAUNCH SUCCESS**: App starts without crashes
**✅ MODEL SUCCESS**: Encoder-decoder models load
**✅ PIPELINE SUCCESS**: Translation pipeline functional
**✅ UI SUCCESS**: Subtitle overlay displays correctly

---

**🎯 Goal**: Complete all 6 success criteria before claiming "build-ready" status. 