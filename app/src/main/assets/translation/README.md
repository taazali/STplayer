# Translation Models Directory

This directory contains ONNX translation models for real-time subtitle translation in STplayer.

## Model Requirements

### Supported Language Pairs
- **English → Arabic** (`translation_en_ar_encoder_int8.onnx`, `translation_en_ar_decoder_int8.onnx`) - Primary target
- **English → Spanish** (`translation_en_es_encoder_int8.onnx`, `translation_en_es_decoder_int8.onnx`)
- **English → French** (`translation_en_fr_encoder_int8.onnx`, `translation_en_fr_decoder_int8.onnx`)
- **English → German** (`translation_en_de_encoder_int8.onnx`, `translation_en_de_decoder_int8.onnx`)
- **English → Chinese** (`translation_en_zh_encoder_int8.onnx`, `translation_en_zh_decoder_int8.onnx`)

### Model Architecture
STplayer supports two model architectures:

#### 1. Encoder-Decoder Architecture (Recommended)
- **Encoder Model**: `translation_{source}_{target}_encoder_int8.onnx`
- **Decoder Model**: `translation_{source}_{target}_decoder_int8.onnx`
- **Example**: `translation_en_ar_encoder_int8.onnx`, `translation_en_ar_decoder_int8.onnx`

#### 2. Single Model Architecture (Fallback)
- **Combined Model**: `translation_{source}_{target}.onnx`
- **Example**: `translation_en_ar.onnx`

### Model Specifications
- **Format**: ONNX (Open Neural Network Exchange)
- **Architecture**: MarianMT/OPUS-MT (recommended)
- **Size**: Base/Tiny models (~70MB total for encoder+decoder)
- **Quantization**: INT8 quantized for smaller file size and faster inference
- **Opset**: 13+ for compatibility with ONNX Runtime Mobile

## Getting Translation Models

### Option 1: Pre-converted ONNX Models
1. Search HuggingFace for ONNX exports:
   - [Helsinki-NLP/opus-mt-en-ar](https://huggingface.co/Helsinki-NLP/opus-mt-en-ar)
   - [Community ONNX exports](https://huggingface.co/mys/opus-mt-en-ar-onnx)

2. Download and place in this directory with naming convention:
   ```
   translation_en_ar_encoder_int8.onnx
   translation_en_ar_decoder_int8.onnx
   translation_en_es_encoder_int8.onnx
   translation_en_es_decoder_int8.onnx
   ```

### Option 2: Convert from HuggingFace Models
```bash
# Install conversion tools
pip install optimum-cli onnxruntime-tools

# Convert MarianMT model to ONNX with encoder-decoder split
optimum-cli export onnx \
  --model Helsinki-NLP/opus-mt-en-ar \
  --task translation \
  --opset 13 \
  --framework pt2 \
  --device cpu \
  --split encoder-decoder \
  ./translation_en_ar

# Quantize encoder model
onnxruntime_tools quantize \
  --input ./translation_en_ar/encoder_model.onnx \
  --output ./translation_en_ar/encoder_model_int8.onnx \
  --mode dynamic

# Quantize decoder model
onnxruntime_tools quantize \
  --input ./translation_en_ar/decoder_model.onnx \
  --output ./translation_en_ar/decoder_model_int8.onnx \
  --mode dynamic

# Copy to assets directory
cp ./translation_en_ar/encoder_model_int8.onnx app/src/main/assets/translation/translation_en_ar_encoder_int8.onnx
cp ./translation_en_ar/decoder_model_int8.onnx app/src/main/assets/translation/translation_en_ar_decoder_int8.onnx
```

### Option 3: Use Demo Models (Fallback)
If no ONNX models are available, the app will use hardcoded translations for demo purposes.

## Model Performance

### Expected Performance on Android TV
- **Encoder Inference**: 20-50ms per sentence
- **Decoder Inference**: 30-80ms per sentence
- **Total Translation**: 50-130ms per sentence
- **Memory Usage**: 150-400MB RAM (both models)
- **Model Loading**: 3-8 seconds on first launch

### Optimization Tips
1. Use quantized models (INT8) for 2-4x size reduction
2. Use base/tiny models instead of large models
3. Enable parallel execution in ONNX Runtime
4. Adjust thread count based on device capabilities
5. Consider using single model if memory is limited

## Integration with STplayer

### Model Loading
Models are automatically detected and loaded when:
1. App starts and translation is enabled
2. User changes target language
3. Translation quality settings change

### Loading Priority
1. **Encoder-Decoder Models**: `translation_en_ar_encoder_int8.onnx` + `translation_en_ar_decoder_int8.onnx`
2. **Single Model**: `translation_en_ar.onnx`
3. **Fallback**: Hardcoded translations

### Fallback Behavior
If ONNX model loading fails:
1. App logs error and continues
2. Uses hardcoded translations for demo
3. Shows "Fallback" indicator in UI

### Performance Monitoring
The app tracks:
- Model loading time for both encoder and decoder
- Inference time for each model separately
- Memory usage for both models
- Translation count and success rate

## Cloud Environment Setup

### Model Paths
In cloud environment, models should be placed in:
```
/workspace/STplayer/app/src/main/assets/translation/
```

### Model Conversion Script
```bash
#!/bin/bash
# convert_models.sh - Convert HuggingFace models to ONNX encoder-decoder

MODELS=(
  "Helsinki-NLP/opus-mt-en-ar"
  "Helsinki-NLP/opus-mt-en-es"
  "Helsinki-NLP/opus-mt-en-fr"
)

for model in "${MODELS[@]}"; do
  base_name=$(echo "$model" | sed 's/.*\///')
  echo "Converting $model..."
  
  # Export with encoder-decoder split
  optimum-cli export onnx \
    --model "$model" \
    --task translation \
    --opset 13 \
    --framework pt2 \
    --device cpu \
    --split encoder-decoder \
    "./temp_$base_name"
  
  # Quantize encoder
  onnxruntime_tools quantize \
    --input "./temp_$base_name/encoder_model.onnx" \
    --output "./temp_$base_name/encoder_model_int8.onnx" \
    --mode dynamic
  
  # Quantize decoder
  onnxruntime_tools quantize \
    --input "./temp_$base_name/decoder_model.onnx" \
    --output "./temp_$base_name/decoder_model_int8.onnx" \
    --mode dynamic
  
  # Copy to assets with proper naming
  cp "./temp_$base_name/encoder_model_int8.onnx" "app/src/main/assets/translation/translation_${base_name}_encoder_int8.onnx"
  cp "./temp_$base_name/decoder_model_int8.onnx" "app/src/main/assets/translation/translation_${base_name}_decoder_int8.onnx"
done
```

## Troubleshooting

### Common Issues
1. **Models not found**: Check file naming convention (encoder/decoder pairs)
2. **Loading fails**: Verify ONNX format and opset version
3. **Slow inference**: Try quantized model or reduce quality
4. **Memory issues**: Use smaller model or reduce thread count
5. **Encoder/decoder mismatch**: Ensure both models are from same source

### Debug Information
Enable debug logging to see:
- Model loading progress for encoder and decoder
- Inference timing for each model
- Memory usage for both models
- Error details and stack traces

## License and Attribution

### Model Licenses
- **OPUS-MT**: Apache 2.0 License
- **MarianMT**: MIT License
- **Community exports**: Check individual model licenses

### Attribution
If using OPUS-MT models, please include:
```
Translation powered by OPUS-MT (Helsinki-NLP)
```

## Next Steps

1. **Model Optimization**: Implement model pruning and compression
2. **Batch Processing**: Support multiple sentences for better efficiency
3. **Model Caching**: Cache frequently used models
4. **Dynamic Loading**: Load models on-demand based on user preferences
5. **Advanced Decoding**: Implement beam search and sampling strategies 