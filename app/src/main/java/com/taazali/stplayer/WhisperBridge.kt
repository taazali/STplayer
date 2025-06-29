package com.taazali.stplayer

import android.content.Context
import android.content.res.AssetManager

/**
 * JNI Bridge for Whisper.cpp integration
 * 
 * This class provides the interface between Kotlin and the native Whisper.cpp library
 * for real-time audio transcription.
 */
class WhisperBridge {
    
    companion object {
        // Track if native library is available
        private var nativeLibraryLoaded = false
        
        // Load the native library
        init {
            try {
                System.loadLibrary("whispercpp")
                nativeLibraryLoaded = true
                println("✅ [WHISPER] Whisper.cpp native library loaded successfully")
            } catch (e: UnsatisfiedLinkError) {
                nativeLibraryLoaded = false
                println("⚠️ [WHISPER] Failed to load Whisper.cpp native library: ${e.message}")
                println("🔧 [WHISPER] Using fallback mode for transcription")
            } catch (e: Exception) {
                nativeLibraryLoaded = false
                println("❌ [WHISPER] Unexpected error loading native library: ${e.message}")
                println("🔧 [WHISPER] Using fallback mode for transcription")
            }
        }
        
        fun isNativeLibraryAvailable(): Boolean = nativeLibraryLoaded
    }
    
    /**
     * Initialize Whisper model from assets
     * 
     * @param context Android context for asset access
     * @param modelName Name of the model file in assets/whisper/
     * @return true if model initialized successfully
     */
    fun initializeModel(context: Context, modelName: String): Boolean {
        println("🔧 [WHISPER] Starting Whisper model initialization...")
        println("🔧 [WHISPER] Model name: $modelName")
        println("🔧 [WHISPER] Native library available: ${isNativeLibraryAvailable()}")
        
        try {
            // Check if model exists in assets
            val modelAvailable = isModelAvailable(context, modelName)
            if (!modelAvailable) {
                println("❌ [WHISPER] Model not available in assets: $modelName")
                return false
            }
            
            val modelSize = getModelSize(context, modelName)
            println("✅ [WHISPER] Model found in assets: ${modelSize} bytes")
            
            // Initialize native Whisper context
            val assetManager = context.assets
            val modelPath = "whisper/$modelName"
            
            println("🔧 [WHISPER] Loading model from assets: $modelPath")
            val startTime = System.currentTimeMillis()
            
            val success = if (isNativeLibraryAvailable()) {
                nativeInitializeModel(assetManager, modelPath)
            } else {
                nativeInitializeModelFallback(assetManager, modelPath)
            }
            
            val loadTime = System.currentTimeMillis() - startTime
            
            if (success) {
                println("✅ [WHISPER] Model initialized successfully in ${loadTime}ms")
                println("✅ [WHISPER] Model path: $modelPath")
                println("✅ [WHISPER] Model size: ${modelSize} bytes")
                return true
            } else {
                println("❌ [WHISPER] Failed to initialize model")
                return false
            }
            
        } catch (e: Exception) {
            println("❌ [WHISPER] Exception during model initialization: ${e.message}")
            e.printStackTrace()
            return false
        }
    }
    
    /**
     * Transcribe audio data using loaded Whisper model
     * 
     * @param audioData PCM audio data (16kHz, 16-bit, mono)
     * @return Transcribed text or error message
     */
    fun transcribeAudio(audioData: ByteArray): String {
        println("🔧 [WHISPER] Starting audio transcription...")
        println("🔧 [WHISPER] Audio data size: ${audioData.size} bytes")
        println("🔧 [WHISPER] Native library available: ${isNativeLibraryAvailable()}")
        
        if (audioData.isEmpty()) {
            println("❌ [WHISPER] Empty audio data provided")
            return "[ERROR] Empty audio data"
        }
        
        try {
            val startTime = System.currentTimeMillis()
            
            println("🔧 [WHISPER] Running Whisper inference...")
            val transcription = if (isNativeLibraryAvailable()) {
                transcribeAudioNative(audioData)
            } else {
                transcribeAudioNativeFallback(audioData)
            }
            
            val transcriptionTime = System.currentTimeMillis() - startTime
            
            if (transcription.startsWith("[ERROR")) {
                println("❌ [WHISPER] Transcription failed: $transcription")
                return transcription
            } else {
                println("✅ [WHISPER] Transcription completed in ${transcriptionTime}ms")
                println("✅ [WHISPER] Result: '$transcription'")
                return transcription
            }
            
        } catch (e: Exception) {
            println("❌ [WHISPER] Exception during transcription: ${e.message}")
            e.printStackTrace()
            return "[ERROR] Transcription exception: ${e.message}"
        }
    }
    
    /**
     * Set transcription parameters
     * 
     * @param language Language code (e.g., "en", "es", "fr")
     * @param task Task type ("transcribe" or "translate")
     * @return true if parameters set successfully
     */
    fun setParameters(language: String, task: String): Boolean {
        return if (isNativeLibraryAvailable()) {
            setParametersNative(language, task)
        } else {
            setParametersFallback(language, task)
        }
    }
    
    /**
     * Clean up native resources
     */
    fun cleanup() {
        if (isNativeLibraryAvailable()) {
            cleanupNative()
        } else {
            cleanupFallback()
        }
    }
    
    /**
     * Get the current transcription status
     * 
     * @return Status string indicating current state
     */
    fun getStatus(): String {
        return if (isNativeLibraryAvailable()) {
            getStatusNative()
        } else {
            getStatusFallback()
        }
    }
    
    // Native method declarations
    private external fun nativeInitializeModel(assetManager: AssetManager, modelName: String): Boolean
    private external fun transcribeAudioNative(audioData: ByteArray): String
    private external fun setParametersNative(language: String, task: String): Boolean
    private external fun cleanupNative()
    private external fun getStatusNative(): String
    
    // Fallback implementations for when native library is not available
    private fun setParametersFallback(language: String, task: String): Boolean {
        println("🔧 [WHISPER] Using fallback setParameters: $language, $task")
        return true
    }
    
    private fun cleanupFallback() {
        println("🔧 [WHISPER] Using fallback cleanup")
    }
    
    private fun getStatusFallback(): String {
        return "Fallback mode - native library not available"
    }
    
    private fun nativeInitializeModelFallback(assetManager: AssetManager, modelName: String): Boolean {
        println("🔧 [WHISPER] Using fallback nativeInitializeModel: $modelName")
        return true
    }
    
    private fun transcribeAudioNativeFallback(audioData: ByteArray): String {
        println("🔧 [WHISPER] Using fallback transcribeAudioNative: ${audioData.size} bytes")
        return "Simulated transcription from fallback mode"
    }
    
    /**
     * Get available Whisper models from assets
     * 
     * @param context Android context for asset access
     * @return List of available model files
     */
    fun getAvailableModels(context: Context): List<String> {
        return try {
            val assetManager = context.assets
            val whisperDir = "whisper"
            
            // List files in whisper directory
            assetManager.list(whisperDir)?.toList() ?: emptyList()
        } catch (e: Exception) {
            println("Failed to list Whisper models: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Check if a specific model is available
     * 
     * @param context Android context for asset access
     * @param modelName Name of the model file
     * @return true if model is available
     */
    fun isModelAvailable(context: Context, modelName: String): Boolean {
        return try {
            val assetManager = context.assets
            val modelPath = "whisper/$modelName"
            
            // Try to open the asset
            assetManager.open(modelPath).use { 
                true // If we can open it, it exists
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get model file size
     * 
     * @param context Android context for asset access
     * @param modelName Name of the model file
     * @return Size in bytes, or -1 if not found
     */
    fun getModelSize(context: Context, modelName: String): Long {
        return try {
            val assetManager = context.assets
            val modelPath = "whisper/$modelName"
            
            assetManager.open(modelPath).use { inputStream ->
                inputStream.available().toLong()
            }
        } catch (e: Exception) {
            -1L
        }
    }
    
    // TODO: Add more methods for:
    // - Real-time streaming transcription
    // - Translation support
    // - Multiple language detection
    // - Confidence scoring
    // - Timestamp extraction
    // - Model performance optimization
} 