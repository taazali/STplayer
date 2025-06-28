# STplayer Assets Directory

This directory contains AI models and assets for STplayer's real-time transcription and translation features.

## Directory Structure

```
assets/
├── README.md                 # This file
├── whisper/                  # Whisper.cpp models for transcription
│   ├── README.md            # Whisper model documentation
│   ├── ggml-base.en.bin     # English transcription model
│   └── ggml-base.ar.bin     # Arabic transcription model
└── translation/             # ONNX translation models
    ├── README.md            # Translation model documentation
    ├── translation_en_ar.onnx  # English → Arabic translation
    ├── translation_en_es.onnx  # English → Spanish translation
    └── translation_en_fr.onnx  # English → French translation
```

## AI Pipeline Overview

STplayer implements a complete AI pipeline for real-time subtitle generation:

### 1. Audio Capture
- **Source**: ExoPlayer PCM audio output
- **Format**: 16kHz, 16-bit mono
- **Chunking**: 5-second segments for processing

### 2. Speech Recognition (Whisper.cpp)
- **Models**: GGML format Whisper models
- **Languages**: English, Arabic, and more
- **Output**: Real-time transcription text

### 3. Translation (ONNX Runtime)
- **Models**: MarianMT/OPUS-MT ONNX models
- **Languages**: English → Arabic, Spanish, French, etc.
- **Output**: Translated subtitle text

### 4. Display
- **Format**: Dynamic subtitle overlay
- **Styling**: MX Player-style with outline and positioning
- **Real-time**: Updates as audio plays

## Model Requirements

### Whisper Models (Transcription)
- **Format**: GGML (optimized for mobile)
- **Size**: Base models (~150MB) recommended
- **Languages**: English (primary), Arabic, Spanish, French
- **Performance**: 2-5 seconds per 5-second audio chunk

### Translation Models (ONNX)
- **Format**: ONNX (Open Neural Network Exchange)
- **Architecture**: MarianMT/OPUS-MT
- **Size**: Base/Tiny models (~70MB) for mobile
- **Quantization**: INT8 for size optimization
- **Performance**: 50-200ms per sentence

## Getting Started

### 1. Download Whisper Models
```bash
# Download English model
wget https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-base.en.bin \
  -O app/src/main/assets/whisper/ggml-base.en.bin

# Download Arabic model
wget https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-base.ar.bin \
  -O app/src/main/assets/whisper/ggml-base.ar.bin
```

### 2. Download Translation Models
See `translation/README.md` for detailed instructions on:
- Finding pre-converted ONNX models
- Converting HuggingFace models to ONNX
- Model optimization and quantization

### 3. Verify Setup
The app will automatically detect available models and log:
- Model availability and sizes
- Loading success/failure
- Performance metrics

## Performance Optimization

### For Android TV Devices
1. **Whisper**: Use base models, not large models
2. **Translation**: Use quantized ONNX models
3. **Memory**: Monitor RAM usage (target <500MB total)
4. **CPU**: Adjust thread count based on device capabilities

### For Mobile Devices
1. **Whisper**: Use tiny models for faster processing
2. **Translation**: Use smallest available models
3. **Battery**: Optimize for power efficiency
4. **Storage**: Compress models where possible

## Cloud Environment

### Model Paths
In cloud environment, all models should be placed in:
```
/workspace/STplayer/app/src/main/assets/
```

### Model Conversion
Use the provided scripts in each model directory:
- `whisper/convert_whisper.sh` - Convert Whisper models
- `translation/convert_models.sh` - Convert translation models

### Build Process
1. Models are bundled with the APK during build
2. Models are extracted to cache on first app launch
3. Models are loaded into memory when needed
4. Performance is monitored and logged

## Troubleshooting

### Common Issues
1. **Models not found**: Check file paths and naming
2. **Loading fails**: Verify model format and compatibility
3. **Slow performance**: Use smaller models or optimize settings
4. **Memory issues**: Monitor RAM usage and reduce model size

### Debug Information
Enable debug logging to see:
- Model loading progress and timing
- Inference performance metrics
- Memory usage patterns
- Error details and stack traces

## Development Workflow

### Local Development (macOS)
- Models are loaded from assets during development
- Changes require app restart to reload models
- Use Android Studio's asset manager for easy updates

### Cloud Development (Ubuntu)
- Models are converted and optimized in cloud
- Build process includes model bundling
- Performance testing on various Android TV devices

## License and Attribution

### Whisper Models
- **License**: MIT License (OpenAI)
- **Attribution**: "Transcription powered by Whisper (OpenAI)"

### Translation Models
- **License**: Apache 2.0 (OPUS-MT) / MIT (MarianMT)
- **Attribution**: "Translation powered by OPUS-MT (Helsinki-NLP)"

## Next Steps

1. **Model Optimization**: Implement model pruning and compression
2. **Batch Processing**: Support multiple audio chunks for efficiency
3. **Model Caching**: Intelligent model loading and caching
4. **Dynamic Languages**: Load models on-demand based on content
5. **Quality Tuning**: Adjust model parameters for different content types 