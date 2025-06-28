# 🚀 Shihab's Testing Steps

## **Step 1: Build APK in Android Studio Cloud**

```bash
# Navigate to project
cd /workspace/STplayer

# Verify models are present
ls -la app/src/main/assets/translation/
# Should show: translation_en_ar_encoder_int8.onnx, translation_en_ar_decoder_int8.onnx

# Build APK
./gradlew assembleDebug --no-daemon

# Verify APK created
ls -lh app/build/outputs/apk/debug/app-debug.apk
# Should be ~250-300MB
```

## **Step 2: Deploy to Tablet**

```bash
# Check tablet connection
adb devices

# Install APK
adb install app/build/outputs/apk/debug/app-debug.apk

# Verify installation
adb shell pm list packages | grep stplayer
```

## **Step 3: Monitor Logs**

```bash
# Clear logs
adb logcat -c

# Launch app
adb shell am start -n com.taazali.stplayer/.MainActivity

# Monitor logs (run in separate terminal)
adb logcat | grep -E "(MAIN|TRANSLATION|SUBTITLE)"
```

## **Step 4: Expected Results**

### **Model Loading Success:**
```
✅ [MAIN] Encoder-decoder pair found: translation_en_ar
✅ [TRANSLATION] Both encoder and decoder files found
✅ [TRANSLATION] Encoder model loaded in [X]ms
✅ [TRANSLATION] Decoder model loaded in [X]ms
✅ [TRANSLATION] Encoder-decoder models loaded successfully
```

### **Translation Pipeline:**
```
🔧 [TRANSLATION] Using encoder-decoder architecture
✅ [TRANSLATION] Encoder completed in [X]ms
✅ [TRANSLATION] Decoder completed in [X]ms
✅ [TRANSLATION] Translation completed in [X]ms
```

## **Performance Expectations:**
- **Model Loading**: 7-15 seconds total
- **Translation Time**: 50-130ms per sentence
- **Memory Usage**: 200-400MB
- **No Crashes**: App runs continuously

## **Report Back:**
1. Did APK build successfully? ✅/❌
2. Did models load? ✅/❌
3. Did translation work? ✅/❌
4. Any crashes or errors? List them
5. Performance metrics (timing, memory)

**Execute these steps and report results!** 🎯 