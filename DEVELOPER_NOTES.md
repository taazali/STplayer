# STplayer Developer Notes

## 🚀 Project Overview

STplayer is an AI-powered video player with real-time transcription and translation capabilities, built for Android TV and tablets. The app uses Whisper.cpp for speech-to-text and ONNX Runtime for translation.

## 🏗️ Architecture

### Core Components

1. **MainActivity**: Main UI with ExoPlayer integration
2. **SubtitleManager**: Manages subtitle display and state
3. **TranslationManager**: Handles ONNX-based translation
4. **WhisperBridge**: JNI bridge to Whisper.cpp
5. **AudioCaptureManager**: Captures PCM audio from ExoPlayer

### Data Flow

```
ExoPlayer → AudioCaptureManager → WhisperBridge → SubtitleManager → TranslationManager → UI
```

## 📦 Model Requirements

### Translation Models

**Location**: `app/src/main/assets/translation/`

**Supported Architectures**:
1. **Encoder-Decoder** (Recommended):
   - `translation_en_ar_encoder_int8.onnx`
   - `translation_en_ar_decoder_int8.onnx`

2. **Single Model** (Fallback):
   - `translation_en_ar.onnx`

**Model Specifications**:
- **Format**: ONNX (Open Neural Network Exchange)
- **Quantization**: INT8 (recommended for mobile)
- **Max Size**: 150MB per model
- **Supported Languages**: English → Arabic, Spanish, French, German, Chinese

### Whisper Models

**Location**: `app/src/main/assets/whisper/`

**Supported Models**:
- `ggml-base.en.bin` (Recommended - 142MB)
- `ggml-small.en.bin` (Faster - 244MB)
- `ggml-tiny.en.bin` (Fastest - 39MB)

## 🔧 Setup Instructions

### Prerequisites

1. **Android Studio Cloud** with:
   - Java 17
   - Android SDK 34
   - NDK 21.4.7075529
   - CMake 3.28.3

2. **Dependencies**:
   - ONNX Runtime Android 1.18.0
   - ExoPlayer 1.3.1
   - Jetpack Compose 2024.09.00

### Build Configuration

```kotlin
// app/build.gradle.kts
android {
    compileSdk = 34
    targetSdk = 34
    ndkVersion = "21.4.7075529"
    
    externalNativeBuild {
        cmake {
            version = "3.28.3"
        }
    }
}

dependencies {
    implementation("com.microsoft.onnxruntime:onnxruntime-android:1.18.0")
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    // ... other dependencies
}
```

### Model Setup

1. **Download Models**:
   ```bash
   # Translation models (example for English→Arabic)
   wget https://huggingface.co/Helsinki-NLP/opus-mt-en-ar/resolve/main/encoder_model_int8.onnx -O app/src/main/assets/translation/translation_en_ar_encoder_int8.onnx
   wget https://huggingface.co/Helsinki-NLP/opus-mt-en-ar/resolve/main/decoder_model_int8.onnx -O app/src/main/assets/translation/translation_en_ar_decoder_int8.onnx
   
   # Whisper model
   wget https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-base.en.bin -O app/src/main/assets/whisper/ggml-base.en.bin
   ```

2. **Verify Assets Structure**:
   ```
   app/src/main/assets/
   ├── translation/
   │   ├── translation_en_ar_encoder_int8.onnx
   │   ├── translation_en_ar_decoder_int8.onnx
   │   └── translation_en_ar.onnx (fallback)
   └── whisper/
       └── ggml-base.en.bin
   ```

## 🎯 Performance Optimization

### ONNX Runtime Settings

```kotlin
// TranslationQuality.FAST
sessionOptions.setIntraOpNumThreads(1)
sessionOptions.setInterOpNumThreads(1)

// TranslationQuality.MEDIUM (Default)
sessionOptions.setIntraOpNumThreads(2)
sessionOptions.setInterOpNumThreads(1)

// TranslationQuality.HIGH
sessionOptions.setIntraOpNumThreads(4)
sessionOptions.setInterOpNumThreads(2)
```

### Audio Processing

- **Chunk Size**: 3-5 seconds of PCM audio
- **Sample Rate**: 16kHz (Whisper requirement)
- **Buffer Size**: 4KB circular buffer
- **Processing**: Background coroutine to avoid UI blocking

### Memory Management

- **Model Caching**: Models extracted to cache directory on first run
- **Session Reuse**: ONNX sessions created once, reused for all translations
- **Memory Monitoring**: Real-time memory usage tracking

## 🐛 Troubleshooting

### Common Issues

1. **ONNX Runtime Not Found**:
   ```
   ❌ [TRANSLATION] Failed to initialize ONNX Runtime
   ```
   **Solution**: Check ONNX Runtime dependency in build.gradle.kts

2. **Model Loading Failed**:
   ```
   ❌ [TRANSLATION] No translation models found
   ```
   **Solution**: Verify models exist in assets/translation/

3. **Native Library Error**:
   ```
   UnsatisfiedLinkError: No implementation found
   ```
   **Solution**: Check NDK configuration and CMake setup

4. **Memory Issues**:
   ```
   OutOfMemoryError: Failed to allocate
   ```
   **Solution**: Use smaller models or reduce thread count

### Debug Logging

Enable verbose logging:
```kotlin
// In MainActivity or TranslationManager
println("🔧 [DEBUG] Detailed operation info")
println("✅ [SUCCESS] Operation completed")
println("❌ [ERROR] Operation failed")
```

## 📊 Performance Metrics

### Translation Performance

- **Target Latency**: < 100ms for short phrases
- **Memory Usage**: < 200MB total
- **Throughput**: 10+ translations per second

### Audio Processing

- **Transcription Latency**: < 2 seconds
- **Audio Quality**: 16kHz, 16-bit PCM
- **Buffer Management**: Zero-copy where possible

## 🔄 Development Workflow

### Testing

1. **Unit Tests**: Run `./gradlew test`
2. **Integration Tests**: Run `./gradlew connectedAndroidTest`
3. **Performance Tests**: Monitor logs for timing information

### Deployment

1. **Build**: `./gradlew assembleRelease`
2. **Sign**: Use release keystore
3. **Install**: `adb install app/build/outputs/apk/release/app-release.apk`

## 📚 API Reference

### TranslationManager

```kotlin
class TranslationManager(context: Context) {
    fun loadModel(modelName: String, sourceLanguage: String, targetLanguage: String): Boolean
    fun translateText(text: String, sourceLanguage: String, targetLanguage: String): String
    fun setTranslationQuality(quality: TranslationQuality)
    fun unloadModel()
    fun getPerformanceStats(): TranslationPerformanceStats
}
```

### SubtitleManager

```kotlin
class SubtitleManager(context: Context) {
    fun processTranscription(text: String)
    fun setSourceLanguage(language: String)
    fun setTargetLanguage(language: String)
    fun setTranslationManager(manager: TranslationManager)
}
```

## 🚀 Future Enhancements

1. **Model Conversion**: Add scripts for converting HuggingFace models to ONNX
2. **Gesture Controls**: Implement subtitle positioning gestures
3. **Batch Processing**: Support for batch translation
4. **Model Compression**: Further model optimization
5. **Offline Mode**: Complete offline functionality

## 📞 Support

For issues and questions:
1. Check this documentation
2. Review logs for error messages
3. Test with fallback mode
4. Verify model compatibility

---

**Last Updated**: December 2024
**Version**: 1.0.0
**Compatibility**: Android 5.0+ (API 21+) 