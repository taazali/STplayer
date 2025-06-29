# Whisper Models Directory

This directory should contain Whisper.cpp model files for real-time transcription.

## Required Model File

**For the app to work properly, you need to add a Whisper model file:**

- **File name:** `ggml-base.en.bin`
- **Size:** ~142 MB
- **Source:** [Whisper.cpp models](https://huggingface.co/ggerganov/whisper.cpp/tree/main)

## How to Get the Model

1. **Download from Hugging Face:**
   ```bash
   wget https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-base.en.bin
   ```

2. **Place in this directory:** `app/src/main/assets/whisper/ggml-base.en.bin`

## Alternative Models

You can also use other Whisper models:
- `ggml-tiny.en.bin` (~39 MB) - Fastest, lower accuracy
- `ggml-small.en.bin` (~244 MB) - Better accuracy
- `ggml-medium.en.bin` (~769 MB) - Best accuracy

## Current Status

⚠️ **No model file present** - The app will use fallback transcription until a model is added.

## Model Integration

The app will automatically detect and load the model file when it's placed in this directory.

## Model Files

### Expected Models
- `ggml-tiny.en.bin` - Tiny English model (~39MB) - Fastest, lowest accuracy
- `ggml-base.en.bin` - Base English model (~142MB) - Balanced speed/accuracy
- `ggml-small.en.bin` - Small English model (~466MB) - Better accuracy, slower
- `ggml-medium.en.bin` - Medium English model (~1.5GB) - High accuracy, slower

### Model Sources
Download models from the official Whisper.cpp repository:
- **GitHub**: https://github.com/ggerganov/whisper.cpp
- **Direct Download**: https://huggingface.co/ggerganov/whisper.cpp/tree/main

### Download Commands
```bash
# Download base English model (recommended for mobile)
wget https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-base.en.bin

# Download tiny English model (fastest)
wget https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-tiny.en.bin

# Download small English model (better accuracy)
wget https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-small.en.bin
```

## Model Specifications

### Audio Requirements
- **Sample Rate**: 16kHz (Whisper requirement)
- **Channels**: Mono
- **Format**: 16-bit PCM
- **Chunk Size**: 5-second segments recommended for mobile

### Performance Characteristics
| Model | Size | Speed | Accuracy | Mobile Suitability |
|-------|------|-------|----------|-------------------|
| tiny  | 39MB | Fastest | 32.4% WER | ✅ Excellent |
| base  | 142MB | Fast | 23.8% WER | ✅ Good |
| small | 466MB | Medium | 18.8% WER | ⚠️ Limited |
| medium| 1.5GB | Slow | 16.1% WER | ❌ Not recommended |

### Memory Usage
- **Tiny**: ~50MB RAM during inference
- **Base**: ~150MB RAM during inference
- **Small**: ~500MB RAM during inference
- **Medium**: ~1.5GB RAM during inference

## Integration

### Model Loading
Models are loaded dynamically by the `WhisperBridge` class:
```kotlin
// Initialize Whisper model
val modelName = "ggml-base.en.bin"
val success = whisperBridge.initializeModel(context, modelName)
```

### Audio Processing
```kotlin
// Process PCM audio data
val transcription = whisperBridge.transcribeAudio(pcmBuffer)
```

### Error Handling
```kotlin
// Check model availability
val available = whisperBridge.isModelAvailable(context, modelName)
val modelSize = whisperBridge.getModelSize(context, modelName)
```

## Cloud Environment Paths

All model references use cloud environment paths:
- **Project Root**: `/home/user/AndroidStudioProjects/STplayer`
- **Models Path**: `/home/user/AndroidStudioProjects/STplayer/app/src/main/assets/whisper/`
- **Model Loading**: Uses `whisper/` relative path

## Optimization Tips

### For Mobile Deployment
1. **Use Tiny or Base models** for best performance
2. **Process 5-second chunks** to balance latency and accuracy
3. **Use 4 threads** for inference (configurable in JNI)
4. **Enable quantization** if available in Whisper.cpp

### Performance Monitoring
- Monitor transcription latency
- Track memory usage during inference
- Log transcription accuracy metrics
- Profile CPU usage on target devices

## TODO Items
- [ ] Add actual Whisper model files
- [ ] Implement model compression
- [ ] Add multi-language model support
- [ ] Implement streaming transcription
- [ ] Add confidence scoring
- [ ] Optimize for specific device architectures
- [ ] Add model versioning and updates 