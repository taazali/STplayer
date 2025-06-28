# 🚀 Quick Commands for Shihab

## **Copy & Paste These Commands:**

### **1. Build APK**
```bash
cd /workspace/STplayer
./gradlew assembleDebug --no-daemon
```

### **2. Check APK Size**
```bash
ls -lh app/build/outputs/apk/debug/app-debug.apk
```

### **3. Install on Tablet**
```bash
adb devices
adb install app/build/outputs/apk/debug/app-debug.apk
```

### **4. Launch & Monitor**
```bash
adb logcat -c
adb shell am start -n com.taazali.stplayer/.MainActivity
```

### **5. Watch Logs (in new terminal)**
```bash
adb logcat | grep -E "(MAIN|TRANSLATION|SUBTITLE)"
```

### **6. Check for Crashes**
```bash
adb logcat | grep -E "(FATAL|CRASH|AndroidRuntime)"
```

### **7. Monitor Performance**
```bash
adb shell dumpsys meminfo com.taazali.stplayer
```

---

## **Expected Success Logs:**
```
✅ [MAIN] Encoder-decoder pair found: translation_en_ar
✅ [TRANSLATION] Both encoder and decoder files found
✅ [TRANSLATION] Encoder model loaded in [X]ms
✅ [TRANSLATION] Decoder model loaded in [X]ms
✅ [TRANSLATION] Encoder-decoder models loaded successfully
🔧 [TRANSLATION] Using encoder-decoder architecture
✅ [TRANSLATION] Translation completed in [X]ms
```

---

## **Report Format:**
- APK Build: ✅/❌
- Model Loading: ✅/❌  
- Translation Working: ✅/❌
- Any Errors: [List them]
- Performance: [Timing/Memory]

**Ready to execute!** 🎯 