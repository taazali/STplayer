# STplayer Developer Notes

## Development Environment & Workflow

### Local Development (Cursor/IDE)
- **OS**: macOS Big Sur (supports up to Java 11)
- **Role**: Code generation, editing, and refactoring only
- **Build System**: Local builds may fail due to Java version constraints - this is expected and should be ignored

### Cloud Development (Android Studio Cloud)
- **Environment**: Java 17+ with latest Android tools
- **Role**: Official building, testing, and dependency management
- **Source of Truth**: All build configurations and dependencies

### Android Studio Cloud Environment Details
- **Project Root**: `/home/user/AndroidStudioProjects/STplayer`
- **OS**: Ubuntu 24.04.2 LTS
- **Java**: OpenJDK 17+
- **Android SDK**: `/home/user/Android/Sdk`
- **ANDROID_HOME**: `/home/user/Android/Sdk`

### Path Conventions
- **Native/C++ Code**: Always place under `app/src/main/cpp/`
- **SDK/NDK References**: Use `$ANDROID_HOME` variable or `/home/user/Android/Sdk`
- **JNI Libraries**: Build to `app/src/main/cpp/` directory
- **Assets**: Place in `app/src/main/assets/`
- **CMake Files**: Use `app/src/main/cpp/CMakeLists.txt`

## Important Workflow Rules

### What Cursor Should Do:
1. ✅ Generate and edit Kotlin/Compose/ExoPlayer code
2. ✅ Refactor and improve existing code
3. ✅ Create new features and components
4. ✅ Work with the latest code from GitHub
5. ✅ Push changes for cloud review and build
6. ✅ Use cloud environment paths for all references

### What Cursor Should NOT Do:
1. ❌ Change build.gradle, gradle wrapper, or project settings for Java 11 compatibility
2. ❌ Attempt local builds or dependency management
3. ❌ Downgrade Android Gradle Plugin or other tools
4. ❌ Fix local build errors related to Java/Gradle versions
5. ❌ Suggest compatibility changes unless specifically requested
6. ❌ Use local macOS paths in code or documentation

### Build & Test Process:
1. **Local**: Code editing and generation only
2. **Cloud**: All building, testing, and dependency updates
3. **GitHub**: Source of truth for latest code
4. **Shihab**: Manages cloud environment and final builds

## Project Architecture

### Current Components:
- **MainActivity**: ExoPlayer integration with Compose UI
- **AudioCaptureManager**: PCM audio capture from ExoPlayer
- **WhisperBridge**: JNI interface for native Whisper.cpp integration
- **SubtitleManager**: Real-time subtitle display and state management
- **TranslationManager**: ONNX Runtime integration for translation
- **SubtitleOverlay**: MX Player-style subtitle display component

### AI Pipeline:
1. **Audio Capture**: ExoPlayer PCM → AudioCaptureManager
2. **Transcription**: Whisper.cpp via JNI → WhisperBridge
3. **Translation**: ONNX Runtime → TranslationManager
4. **Display**: SubtitleManager → SubtitleOverlay

### Planned Features:
- Real-time audio transcription via Whisper.cpp ✅ (Implemented)
- Multi-language translation via ONNX ✅ (Implemented)
- Gesture controls for subtitle positioning
- External video player intent handling

### Native Code Structure:
```
app/src/main/cpp/
├── CMakeLists.txt
└── whispercpp/
    └── whisper_jni.cpp
```

### Assets Structure:
```
app/src/main/assets/
├── whisper/                  # Whisper.cpp models
│   ├── ggml-base.en.bin     # English transcription
│   └── ggml-base.ar.bin     # Arabic transcription
└── translation/             # ONNX translation models
    ├── translation_en_ar.onnx  # English → Arabic
    ├── translation_en_es.onnx  # English → Spanish
    └── translation_en_fr.onnx  # English → French
```

## Dependencies & Libraries

### Core Dependencies:
- **ExoPlayer**: Media playback and audio capture
- **Jetpack Compose**: Modern UI framework
- **ONNX Runtime Mobile**: Translation model inference
- **Whisper.cpp**: Native speech recognition

### Version Management:
- All versions managed in `gradle/libs.versions.toml`
- Cloud environment handles dependency resolution
- Local development uses version catalog for consistency

## Code Standards

### Kotlin/Compose:
- Use modern Compose patterns and best practices
- Follow Material Design 3 guidelines
- Implement proper state management with ViewModels
- Use coroutines for async operations

### Native Code (JNI):
- Clear separation between Kotlin and C++ layers
- Proper error handling and resource management
- Comprehensive logging for debugging
- Memory-efficient audio processing
- Always use cloud environment paths

### ONNX Integration:
- Load models from assets to cache directory
- Use proper session configuration for performance
- Implement fallback mechanisms for model failures
- Monitor memory usage and inference timing
- Support multiple quality levels (FAST/MEDIUM/HIGH)

### Comments & Documentation:
- Mark all TODO sections clearly
- Document injection points for future features
- Explain complex algorithms and data flows
- Keep comments up-to-date with code changes
- Reference cloud environment paths in documentation

## Performance Considerations

### Memory Management:
- **Whisper Models**: ~150MB RAM per model
- **Translation Models**: ~70MB RAM per model
- **Total Target**: <500MB RAM for all AI models
- **Cache Management**: Extract models to cache on first use

### Inference Performance:
- **Whisper**: 2-5 seconds per 5-second audio chunk
- **Translation**: 50-200ms per sentence
- **UI Updates**: Real-time subtitle overlay
- **Threading**: Parallel processing where possible

### Optimization Strategies:
- Use quantized ONNX models (INT8)
- Implement model caching and reuse
- Adjust thread count based on device capabilities
- Monitor and log performance metrics

## Troubleshooting

### Common Issues:
1. **Local Build Failures**: Expected due to Java version - ignore and use cloud builds
2. **Gradle Sync Errors**: Usually related to local environment - check cloud builds instead
3. **Missing Dependencies**: Add to libs.versions.toml and let cloud handle resolution
4. **Path Issues**: Always use cloud environment paths, not local macOS paths
5. **Model Loading Failures**: Check asset paths and ONNX format compatibility
6. **Memory Issues**: Monitor RAM usage and use smaller models if needed

### Model-Specific Issues:
- **Whisper**: Verify GGML format and model file integrity
- **ONNX**: Check opset version and quantization compatibility
- **Translation**: Ensure proper input/output tensor shapes
- **Performance**: Adjust quality settings and thread configuration

### Getting Help:
- Check cloud build logs for actual issues
- Review GitHub for latest working code
- Ask Shihab for cloud environment status
- Verify all paths match cloud environment
- Check model documentation in assets/ directories

---

**Remember**: Local development is for code editing only. All builds and tests happen in the cloud environment with Java 17+ and latest Android tools. Always use cloud environment paths for consistency. 