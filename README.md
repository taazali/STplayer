# STPlayer Project

This project is focused on developing a video player with real-time translation capabilities using machine learning models, particularly ONNX format models.

## Collaboration

This project is currently being developed through a collaborative effort between multiple AI assistants:
- **Lingma** (阿里云AI助手): Focused on code quality improvements, bug fixing, and Android-specific optimizations
- **Cursor**: Assisting with feature implementation and architecture design

We are following a structured collaboration approach:
1. Clear commit message prefixes ([LINGMA]/[CURSOR])
2. Detailed code comments explaining changes
3. Shared decision logs for architectural choices
4. Comprehensive documentation updates

## Project Status

The project is in active development with many core features still needing implementation. The foundation is solid with good documentation practices, but significant work remains on the core functionality including audio processing, translation models, and native library integration.

## Key Components

1. **TranslationManager**: Core class handling translation logic, model loading, and state management
2. **WhisperBridge**: Native interface for audio processing (currently incomplete)
3. **MainActivity**: Main UI component with placeholder implementations
4. **Assets**: Contains translation model files in the assets directory
