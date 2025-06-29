package com.taazali.stplayer

import android.content.Context
import android.content.res.AssetManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer
import java.nio.LongBuffer
import java.util.concurrent.atomic.AtomicLong
import com.microsoft.onnxruntime.*

/**
 * Manages ONNX-based translation for real-time subtitle translation
 * 
 * This class handles:
 * - Loading ONNX translation models from assets
 * - Translating text between source and target languages
 * - Managing translation quality and options
 * - Integration with the subtitle pipeline
 */
class TranslationManager(private val context: Context) {
    
    // Translation state
    private val _isModelLoaded = MutableStateFlow(false)
    val isModelLoaded: StateFlow<Boolean> = _isModelLoaded.asStateFlow()
    
    private val _translationQuality = MutableStateFlow(TranslationQuality.MEDIUM)
    val translationQuality: StateFlow<TranslationQuality> = _translationQuality.asStateFlow()
    
    private val _currentModel = MutableStateFlow<String?>(null)
    val currentModel: StateFlow<String?> = _currentModel.asStateFlow()
    
    // Translation statistics
    private val _translationCount = MutableStateFlow(0)
    val translationCount: StateFlow<Int> = _translationCount.asStateFlow()
    
    private val _averageTranslationTime = MutableStateFlow(0L)
    val averageTranslationTime: StateFlow<Long> = _averageTranslationTime.asStateFlow()
    
    // ONNX Runtime state
    private var encoderSession: OrtSession? = null
    private var decoderSession: OrtSession? = null
    private var onnxEnvironment: OrtEnvironment? = null
    private var tokenizer: TranslationTokenizer? = null
    
    // Performance tracking
    private val totalInferenceTime = AtomicLong(0)
    private val totalMemoryUsage = AtomicLong(0)
    
    init {
        initializeOnnxRuntime()
    }
    
    /**
     * Initialize ONNX Runtime environment
     */
    private fun initializeOnnxRuntime() {
        try {
            onnxEnvironment = OrtEnvironment.getEnvironment()
            println("✅ [TRANSLATION] ONNX Runtime initialized successfully")
        } catch (e: Exception) {
            println("❌ [TRANSLATION] Failed to initialize ONNX Runtime: ${e.message}")
            println("🔧 [TRANSLATION] Using fallback translation mode")
        }
    }
    
    /**
     * Load ONNX translation models from assets
     * Supports both single model and encoder-decoder architecture
     * 
     * @param modelName Base name for model files (e.g., "translation_en_ar")
     * @param sourceLanguage Source language code (e.g., "en", "es")
     * @param targetLanguage Target language code (e.g., "ar", "fr")
     * @return true if models loaded successfully
     */
    fun loadModel(modelName: String, sourceLanguage: String, targetLanguage: String): Boolean {
        println("🔧 [TRANSLATION] Starting model loading process...")
        println("🔧 [TRANSLATION] Model base name: $modelName")
        println("🔧 [TRANSLATION] Language pair: $sourceLanguage → $targetLanguage")
        
        try {
            if (onnxEnvironment == null) {
                println("❌ [TRANSLATION] ONNX Runtime not initialized, using fallback mode")
                return loadFallbackModel(modelName, sourceLanguage, targetLanguage)
            }
            
            // Try to load encoder-decoder models first
            val encoderModelName = "${modelName}_encoder_int8.onnx"
            val decoderModelName = "${modelName}_decoder_int8.onnx"
            
            println("🔧 [TRANSLATION] Looking for encoder-decoder models:")
            println("  - Encoder: $encoderModelName")
            println("  - Decoder: $decoderModelName")
            
            val encoderFile = extractModelFromAssets(encoderModelName)
            val decoderFile = extractModelFromAssets(decoderModelName)
            
            if (encoderFile != null && decoderFile != null) {
                println("✅ [TRANSLATION] Both encoder and decoder files found")
                println("  - Encoder size: ${encoderFile.length()} bytes")
                println("  - Decoder size: ${decoderFile.length()} bytes")
                
                // Load encoder-decoder architecture
                return loadEncoderDecoderModels(encoderFile, decoderFile, sourceLanguage, targetLanguage)
            } else {
                println("⚠️ [TRANSLATION] Encoder-decoder files not found:")
                println("  - Encoder file exists: ${encoderFile != null}")
                println("  - Decoder file exists: ${decoderFile != null}")
            }
            
            // Fallback to single model
            val singleModelName = "$modelName.onnx"
            println("🔧 [TRANSLATION] Trying single model fallback: $singleModelName")
            
            val singleModelFile = extractModelFromAssets(singleModelName)
            
            if (singleModelFile != null) {
                println("✅ [TRANSLATION] Single model file found: ${singleModelFile.length()} bytes")
                return loadSingleModel(singleModelFile, sourceLanguage, targetLanguage)
            }
            
            println("❌ [TRANSLATION] No translation models found for: $modelName")
            println("❌ [TRANSLATION] Expected files:")
            println("  - $encoderModelName")
            println("  - $decoderModelName")
            println("  - $singleModelName")
            
            // Fallback to demo mode
            return loadFallbackModel(modelName, sourceLanguage, targetLanguage)
            
        } catch (e: Exception) {
            println("❌ [TRANSLATION] Failed to load translation models: ${e.message}")
            e.printStackTrace()
            return loadFallbackModel(modelName, sourceLanguage, targetLanguage)
        }
    }
    
    /**
     * Load fallback model (no ONNX, just tokenizer)
     */
    private fun loadFallbackModel(modelName: String, sourceLanguage: String, targetLanguage: String): Boolean {
        try {
            // Initialize tokenizer for the language pair
            println("🔧 [TRANSLATION] Initializing fallback tokenizer for $sourceLanguage → $targetLanguage")
            tokenizer = TranslationTokenizer(sourceLanguage, targetLanguage)
            
            _isModelLoaded.value = true
            _currentModel.value = "fallback_${modelName}"
            
            println("✅ [TRANSLATION] Fallback translation model loaded successfully")
            println("✅ [TRANSLATION] Translation: $sourceLanguage → $targetLanguage")
            
            return true
            
        } catch (e: Exception) {
            println("❌ [TRANSLATION] Failed to load fallback model: ${e.message}")
            e.printStackTrace()
            _isModelLoaded.value = false
            _currentModel.value = null
            return false
        }
    }
    
    /**
     * Load encoder-decoder architecture models
     */
    private fun loadEncoderDecoderModels(
        encoderFile: File, 
        decoderFile: File, 
        sourceLanguage: String, 
        targetLanguage: String
    ): Boolean {
        println("🔧 [TRANSLATION] Loading encoder-decoder architecture...")
        
        try {
            val sessionOptions = createSessionOptions()
            println("🔧 [TRANSLATION] Session options configured:")
            println("  - Intra-op threads: ${sessionOptions.intraOpNumThreads}")
            println("  - Inter-op threads: ${sessionOptions.interOpNumThreads}")
            println("  - Execution mode: ${sessionOptions.executionMode}")
            
            // Load encoder model
            println("🔧 [TRANSLATION] Loading encoder model: ${encoderFile.name}")
            val encoderStartTime = System.currentTimeMillis()
            encoderSession = onnxEnvironment!!.createSession(encoderFile.absolutePath, sessionOptions)
            val encoderLoadTime = System.currentTimeMillis() - encoderStartTime
            println("✅ [TRANSLATION] Encoder model loaded in ${encoderLoadTime}ms")
            
            // Load decoder model
            println("🔧 [TRANSLATION] Loading decoder model: ${decoderFile.name}")
            val decoderStartTime = System.currentTimeMillis()
            decoderSession = onnxEnvironment!!.createSession(decoderFile.absolutePath, sessionOptions)
            val decoderLoadTime = System.currentTimeMillis() - decoderStartTime
            println("✅ [TRANSLATION] Decoder model loaded in ${decoderLoadTime}ms")
            
            // Initialize tokenizer for the language pair
            println("🔧 [TRANSLATION] Initializing tokenizer for $sourceLanguage → $targetLanguage")
            tokenizer = TranslationTokenizer(sourceLanguage, targetLanguage)
            
            _isModelLoaded.value = true
            _currentModel.value = "${encoderFile.nameWithoutExtension}_${decoderFile.nameWithoutExtension}"
            
            val totalLoadTime = encoderLoadTime + decoderLoadTime
            println("✅ [TRANSLATION] Encoder-decoder models loaded successfully in ${totalLoadTime}ms")
            println("✅ [TRANSLATION] Translation: $sourceLanguage → $targetLanguage")
            println("✅ [TRANSLATION] Encoder path: ${encoderFile.absolutePath}")
            println("✅ [TRANSLATION] Decoder path: ${decoderFile.absolutePath}")
            
            return true
            
        } catch (e: Exception) {
            println("❌ [TRANSLATION] Failed to load encoder-decoder models: ${e.message}")
            e.printStackTrace()
            encoderSession?.close()
            decoderSession?.close()
            encoderSession = null
            decoderSession = null
            return false
        }
    }
    
    /**
     * Load single model architecture
     */
    private fun loadSingleModel(
        modelFile: File, 
        sourceLanguage: String, 
        targetLanguage: String
    ): Boolean {
        try {
            val sessionOptions = createSessionOptions()
            
            // Load single model (encoder-decoder combined)
            encoderSession = onnxEnvironment!!.createSession(modelFile.absolutePath, sessionOptions)
            decoderSession = null // Not used for single model
            
            // Initialize tokenizer for the language pair
            tokenizer = TranslationTokenizer(sourceLanguage, targetLanguage)
            
            _isModelLoaded.value = true
            _currentModel.value = modelFile.name
            
            println("✅ [TRANSLATION] Single model loaded successfully: ${modelFile.name}")
            println("✅ [TRANSLATION] Translation: $sourceLanguage → $targetLanguage")
            println("✅ [TRANSLATION] Model path: ${modelFile.absolutePath}")
            
            return true
            
        } catch (e: Exception) {
            println("❌ [TRANSLATION] Failed to load single model: ${e.message}")
            e.printStackTrace()
            encoderSession?.close()
            encoderSession = null
            return false
        }
    }
    
    /**
     * Create session options based on translation quality
     */
    private fun createSessionOptions(): OrtSession.SessionOptions {
        val sessionOptions = OrtSession.SessionOptions()
        
        when (_translationQuality.value) {
            TranslationQuality.FAST -> {
                sessionOptions.setIntraOpNumThreads(1)
                sessionOptions.setInterOpNumThreads(1)
                sessionOptions.setExecutionMode(OrtSession.SessionOptions.ExecutionMode.ORT_PARALLEL)
            }
            TranslationQuality.MEDIUM -> {
                sessionOptions.setIntraOpNumThreads(2)
                sessionOptions.setInterOpNumThreads(1)
                sessionOptions.setExecutionMode(OrtSession.SessionOptions.ExecutionMode.ORT_PARALLEL)
            }
            TranslationQuality.HIGH -> {
                sessionOptions.setIntraOpNumThreads(4)
                sessionOptions.setInterOpNumThreads(2)
                sessionOptions.setExecutionMode(OrtSession.SessionOptions.ExecutionMode.ORT_PARALLEL)
            }
        }
        
        return sessionOptions
    }
    
    /**
     * Extract ONNX model from assets to cache directory
     */
    private fun extractModelFromAssets(modelName: String): File? {
        try {
            val cacheDir = context.cacheDir
            val modelFile = File(cacheDir, modelName)
            
            // Check if model already exists in cache
            if (modelFile.exists()) {
                println("Using cached model: ${modelFile.absolutePath}")
                return modelFile
            }
            
            // Extract from assets
            val assetManager = context.assets
            val inputStream = assetManager.open("translation/$modelName")
            val outputStream = FileOutputStream(modelFile)
            
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            
            println("Model extracted to cache: ${modelFile.absolutePath}")
            return modelFile
            
        } catch (e: IOException) {
            println("Failed to extract model from assets: ${e.message}")
            return null
        }
    }
    
    /**
     * Translate text using loaded ONNX models
     * 
     * @param text Text to translate
     * @param sourceLanguage Source language code
     * @param targetLanguage Target language code
     * @return Translated text or original text if translation fails
     */
    fun translateText(text: String, sourceLanguage: String, targetLanguage: String): String {
        println("🔧 [TRANSLATION] Starting translation: '$text'")
        println("🔧 [TRANSLATION] Language pair: $sourceLanguage → $targetLanguage")
        
        if (!_isModelLoaded.value || text.isBlank() || tokenizer == null) {
            println("❌ [TRANSLATION] Translation prerequisites not met:")
            println("  - Model loaded: ${_isModelLoaded.value}")
            println("  - Text blank: ${text.isBlank()}")
            println("  - Tokenizer: ${tokenizer != null}")
            return text
        }
        
        val startTime = System.currentTimeMillis()
        
        try {
            // Tokenize input text
            println("🔧 [TRANSLATION] Tokenizing input text...")
            val tokenizeStartTime = System.currentTimeMillis()
            val inputTokens = tokenizer!!.tokenize(text)
            val tokenizeTime = System.currentTimeMillis() - tokenizeStartTime
            println("✅ [TRANSLATION] Tokenization completed in ${tokenizeTime}ms")
            println("  - Input tokens: ${inputTokens.size} tokens")
            println("  - Token array: ${inputTokens.take(10).contentToString()}...")
            
            if (inputTokens.isEmpty()) {
                println("❌ [TRANSLATION] No tokens generated, returning original text")
                return text
            }
            
            val translatedText = if (encoderSession != null && decoderSession != null) {
                println("🔧 [TRANSLATION] Using encoder-decoder architecture")
                translateWithEncoderDecoder(inputTokens)
            } else if (encoderSession != null) {
                println("🔧 [TRANSLATION] Using single model architecture")
                translateWithSingleModel(inputTokens)
            } else {
                println("🔧 [TRANSLATION] Using fallback translation")
                translateWithFallback(inputTokens)
            }
            
            val translationTime = System.currentTimeMillis() - startTime
            updateTranslationStats(translationTime)
            
            // Track performance metrics
            totalInferenceTime.addAndGet(translationTime)
            val memoryUsage = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
            totalMemoryUsage.addAndGet(memoryUsage)
            
            println("✅ [TRANSLATION] Translation completed in ${translationTime}ms")
            println("✅ [TRANSLATION] $sourceLanguage → $targetLanguage: '$text' → '$translatedText'")
            println("✅ [TRANSLATION] Memory usage: ${memoryUsage / 1024 / 1024}MB")
            
            return translatedText
            
        } catch (e: Exception) {
            println("❌ [TRANSLATION] Translation failed: ${e.message}")
            e.printStackTrace()
            
            // Fallback to placeholder translation for demo
            println("🔧 [TRANSLATION] Using fallback translation")
            return getFallbackTranslation(text, targetLanguage)
        }
    }
    
    /**
     * Translate using encoder-decoder architecture
     */
    private fun translateWithEncoderDecoder(inputTokens: IntArray): String {
        println("🔧 [TRANSLATION] Starting encoder-decoder translation...")
        
        // Step 1: Encode input tokens
        println("🔧 [TRANSLATION] Step 1: Encoding input tokens...")
        val encodeStartTime = System.currentTimeMillis()
        
        val inputShape = longArrayOf(1, inputTokens.size.toLong())
        val inputTensor = OnnxTensor.createTensor(
            onnxEnvironment!!.memoryInfo,
            IntBuffer.wrap(inputTokens),
            inputShape
        )
        
        val encoderInputs = mapOf("input_ids" to inputTensor)
        println("🔧 [TRANSLATION] Running encoder inference...")
        val encoderOutputs = encoderSession!!.run(encoderInputs)
        
        // Get encoder output (usually "last_hidden_state")
        val encoderOutput = encoderOutputs["last_hidden_state"] as OnnxTensor
        val encoderOutputShape = encoderOutput.info.shape
        
        val encodeTime = System.currentTimeMillis() - encodeStartTime
        println("✅ [TRANSLATION] Encoder completed in ${encodeTime}ms")
        println("✅ [TRANSLATION] Encoder output shape: ${encoderOutputShape.joinToString(", ")}")
        
        // Step 2: Decode to target language
        println("🔧 [TRANSLATION] Step 2: Decoding to target language...")
        val decodeStartTime = System.currentTimeMillis()
        
        val decoderInputs = mutableMapOf<String, OnnxTensor>()
        
        // Add encoder output to decoder inputs
        decoderInputs["encoder_hidden_states"] = encoderOutput
        
        // Add decoder input tokens (start with BOS token)
        val decoderInputTokens = intArrayOf(1) // BOS token, typically 1
        val decoderInputShape = longArrayOf(1, decoderInputTokens.size.toLong())
        val decoderInputTensor = OnnxTensor.createTensor(
            onnxEnvironment!!.memoryInfo,
            IntBuffer.wrap(decoderInputTokens),
            decoderInputShape
        )
        decoderInputs["input_ids"] = decoderInputTensor
        
        println("🔧 [TRANSLATION] Running decoder inference...")
        val decoderOutputs = decoderSession!!.run(decoderInputs)
        
        // Get decoder output (usually "logits")
        val decoderOutput = decoderOutputs["logits"] as OnnxTensor
        val decoderOutputShape = decoderOutput.info.shape
        
        val decodeTime = System.currentTimeMillis() - decodeStartTime
        println("✅ [TRANSLATION] Decoder completed in ${decodeTime}ms")
        println("✅ [TRANSLATION] Decoder output shape: ${decoderOutputShape.joinToString(", ")}")
        
        // Decode output tokens to text
        println("🔧 [TRANSLATION] Decoding output tokens to text...")
        val result = tokenizer!!.decode(decoderOutput.buffer, decoderOutputShape)
        
        val totalTime = encodeTime + decodeTime
        println("✅ [TRANSLATION] Encoder-decoder translation completed in ${totalTime}ms")
        println("✅ [TRANSLATION] Result: '$result'")
        
        return result
    }
    
    /**
     * Translate using single model architecture
     */
    private fun translateWithSingleModel(inputTokens: IntArray): String {
        // Prepare input tensor
        val inputShape = longArrayOf(1, inputTokens.size.toLong())
        val inputTensor = OnnxTensor.createTensor(
            onnxEnvironment!!.memoryInfo,
            IntBuffer.wrap(inputTokens),
            inputShape
        )
        
        // Run inference on single model
        val inputs = mapOf("input_ids" to inputTensor)
        val outputs = encoderSession!!.run(inputs)
        
        // Extract output tokens
        val outputTensor = outputs["logits"] as OnnxTensor
        val outputBuffer = outputTensor.buffer
        val outputShape = outputTensor.info.shape
        
        println("✅ [TRANSLATION] Single model output shape: ${outputShape.joinToString(", ")}")
        
        // Decode output tokens to text
        return tokenizer!!.decode(outputBuffer, outputShape)
    }
    
    /**
     * Translate using fallback (no ONNX)
     */
    private fun translateWithFallback(inputTokens: IntArray): String {
        // Fallback translation - just return a placeholder for now
        println("🔧 [TRANSLATION] Using fallback translation")
        return "[TRANSLATED] ${tokenizer?.decode(inputTokens) ?: "Translation placeholder"}"
    }
    
    /**
     * Set translation quality/options
     * 
     * @param quality Translation quality level
     */
    fun setTranslationQuality(quality: TranslationQuality) {
        _translationQuality.value = quality
        println("Translation quality set to: $quality")
        
        // Reload model with new quality settings if already loaded
        val currentModel = _currentModel.value
        if (currentModel != null && _isModelLoaded.value) {
            println("Reloading model with new quality settings...")
            unloadModel()
            loadModel(currentModel, "en", "ar") // Assuming English to Arabic for now
        }
    }
    
    /**
     * Unload current model and free resources
     */
    fun unloadModel() {
        try {
            encoderSession?.close()
            encoderSession = null
            decoderSession?.close()
            decoderSession = null
            tokenizer = null
            
            _isModelLoaded.value = false
            _currentModel.value = null
            
            println("ONNX models unloaded successfully")
            
        } catch (e: Exception) {
            println("Failed to unload models: ${e.message}")
        }
    }
    
    /**
     * Get available translation models from assets
     * 
     * @return List of available model files
     */
    fun getAvailableModels(): List<String> {
        return try {
            val assetManager = context.assets
            val translationDir = "translation"
            if (assetManager.list(translationDir) != null) {
                val models = assetManager.list(translationDir)!!.filter { it.endsWith(".onnx") }
                println("Found ${models.size} ONNX models in assets/translation/:")
                models.forEach { println("  - $it") }
                models
            } else {
                println("No translation directory found in assets")
                emptyList()
            }
        } catch (e: Exception) {
            println("Failed to list translation models: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Test model detection and provide detailed information
     */
    fun testModelDetection(): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        val availableModels = getAvailableModels()
        
        result["total_models"] = availableModels.size
        result["available_models"] = availableModels
        
        // Check for encoder-decoder pairs
        val encoderModels = availableModels.filter { it.contains("_encoder_int8.onnx") }
        val decoderModels = availableModels.filter { it.contains("_decoder_int8.onnx") }
        val singleModels = availableModels.filter { !it.contains("_encoder_int8.onnx") && !it.contains("_decoder_int8.onnx") }
        
        result["encoder_models"] = encoderModels
        result["decoder_models"] = decoderModels
        result["single_models"] = singleModels
        
        // Find complete pairs
        val pairs = mutableListOf<String>()
        for (encoder in encoderModels) {
            val baseName = encoder.replace("_encoder_int8.onnx", "")
            val decoder = "${baseName}_decoder_int8.onnx"
            if (decoderModels.contains(decoder)) {
                pairs.add(baseName)
            }
        }
        result["complete_pairs"] = pairs
        
        println("Model Detection Results:")
        println("  Total models: ${result["total_models"]}")
        println("  Encoder models: ${result["encoder_models"]}")
        println("  Decoder models: ${result["decoder_models"]}")
        println("  Single models: ${result["single_models"]}")
        println("  Complete pairs: ${result["complete_pairs"]}")
        
        return result
    }
    
    /**
     * Check if translation is supported for given language pair
     * 
     * @param sourceLanguage Source language code
     * @param targetLanguage Target language code
     * @return true if translation is supported
     */
    fun isTranslationSupported(sourceLanguage: String, targetLanguage: String): Boolean {
        val supportedPairs = mapOf(
            "en" to listOf("ar", "es", "fr", "de", "zh"),
            "es" to listOf("en", "ar", "fr"),
            "fr" to listOf("en", "ar", "es")
        )
        
        return supportedPairs[sourceLanguage]?.contains(targetLanguage) == true
    }
    
    /**
     * Get performance statistics
     */
    fun getPerformanceStats(): TranslationPerformanceStats {
        val count = _translationCount.value
        return TranslationPerformanceStats(
            totalTranslations = count,
            averageInferenceTime = if (count > 0) totalInferenceTime.get() / count else 0,
            totalMemoryUsage = totalMemoryUsage.get(),
            modelLoaded = _isModelLoaded.value,
            currentModel = _currentModel.value
        )
    }
    
    // Private helper methods
    
    private fun updateTranslationStats(translationTime: Long) {
        val currentCount = _translationCount.value
        val currentAvg = _averageTranslationTime.value
        
        _translationCount.value = currentCount + 1
        
        // Calculate running average
        val newAvg = if (currentCount == 0) {
            translationTime
        } else {
            ((currentAvg * currentCount) + translationTime) / (currentCount + 1)
        }
        
        _averageTranslationTime.value = newAvg
    }
    
    // Fallback translation methods (for demo purposes when ONNX fails)
    private fun getFallbackTranslation(text: String, targetLanguage: String): String {
        return when (targetLanguage) {
            "ar" -> getArabicTranslation(text)
            "es" -> getSpanishTranslation(text)
            "fr" -> getFrenchTranslation(text)
            else -> text
        }
    }
    
    // Placeholder translation methods (for demo purposes)
    private fun getArabicTranslation(text: String): String {
        val translations = mapOf(
            "Welcome to STplayer!" to "مرحباً بك في STplayer!",
            "This is a real-time subtitle demo." to "هذا عرض توضيحي للترجمة في الوقت الفعلي.",
            "Audio is being captured from ExoPlayer." to "يتم التقاط الصوت من ExoPlayer.",
            "Soon this will use actual Whisper transcription." to "قريباً سيستخدم هذا النص الفعلي من Whisper.",
            "Translation features will be added next." to "ستضاف ميزات الترجمة بعد ذلك.",
            "The subtitle overlay updates dynamically." to "تتحدث ترجمة الشاشة ديناميكياً.",
            "Just like MX Player or other premium apps." to "تماماً مثل MX Player أو التطبيقات المميزة الأخرى.",
            "Built with modern Android technologies." to "مبني بتقنيات Android الحديثة."
        )
        
        return translations[text] ?: "[ترجمة عربية: $text]"
    }
    
    private fun getSpanishTranslation(text: String): String {
        val translations = mapOf(
            "Welcome to STplayer!" to "¡Bienvenido a STplayer!",
            "This is a real-time subtitle demo." to "Esta es una demostración de subtítulos en tiempo real.",
            "Audio is being captured from ExoPlayer." to "El audio se está capturando desde ExoPlayer.",
            "Soon this will use actual Whisper transcription." to "Pronto esto usará transcripción real de Whisper.",
            "Translation features will be added next." to "Las funciones de traducción se agregarán a continuación.",
            "The subtitle overlay updates dynamically." to "La superposición de subtítulos se actualiza dinámicamente.",
            "Just like MX Player or other premium apps." to "Al igual que MX Player u otras aplicaciones premium.",
            "Built with modern Android technologies." to "Construido con tecnologías modernas de Android."
        )
        
        return translations[text] ?: "[Traducción española: $text]"
    }
    
    private fun getFrenchTranslation(text: String): String {
        val translations = mapOf(
            "Welcome to STplayer!" to "Bienvenue sur STplayer!",
            "This is a real-time subtitle demo." to "Ceci est une démo de sous-titres en temps réel.",
            "Audio is being captured from ExoPlayer." to "L'audio est capturé depuis ExoPlayer.",
            "Soon this will use actual Whisper transcription." to "Bientôt, cela utilisera la transcription réelle de Whisper.",
            "Translation features will be added next." to "Les fonctionnalités de traduction seront ajoutées ensuite.",
            "The subtitle overlay updates dynamically." to "La superposition de sous-titres se met à jour dynamiquement.",
            "Just like MX Player or other premium apps." to "Comme MX Player ou d'autres applications premium.",
            "Built with modern Android technologies." to "Construit avec des technologies Android modernes."
        )
        
        return translations[text] ?: "[Traduction française: $text]"
    }
}

/**
 * Simple tokenizer for translation models
 * This is a placeholder implementation - real models would use proper tokenizers
 */
class TranslationTokenizer(
    private val sourceLanguage: String,
    private val targetLanguage: String
) {
    
    fun tokenize(text: String): IntArray {
        // Simple word-based tokenization for demo
        // Real implementation would use proper tokenizers (BPE, SentencePiece, etc.)
        return text.split(" ").mapIndexed { index, word ->
            word.hashCode() % 10000 + index // Simple hash-based tokenization
        }.toIntArray()
    }
    
    fun decode(tokens: IntArray): String {
        // Simple decoding for demo
        // Real implementation would use proper vocabulary and decoding
        return tokens.joinToString(" ") { token ->
            "[TOKEN_$token]"
        }
    }
    
    fun decode(buffer: ByteBuffer, shape: LongArray): String {
        // Simple decoding for demo
        // Real implementation would use proper vocabulary and decoding
        val tokens = IntArray(shape[1].toInt())
        val intBuffer = buffer.order(ByteOrder.LITTLE_ENDIAN).asIntBuffer()
        intBuffer.get(tokens)
        
        return decode(tokens)
    }
}

/**
 * Translation quality levels
 */
enum class TranslationQuality {
    FAST,      // Lower quality, faster inference
    MEDIUM,    // Balanced quality and speed
    HIGH       // Higher quality, slower inference
}

/**
 * Performance statistics for translation
 */
data class TranslationPerformanceStats(
    val totalTranslations: Int,
    val averageInferenceTime: Long,
    val totalMemoryUsage: Long,
    val modelLoaded: Boolean,
    val currentModel: String?
)

/**
 * Expected ONNX model files in app/src/main/assets/translation/
 * 
 * Model naming convention: 
 * - Encoder-decoder: translation_{source}_{target}_encoder_int8.onnx, translation_{source}_{target}_decoder_int8.onnx
 * - Single model: translation_{source}_{target}.onnx
 * 
 * Example: translation_en_ar_encoder_int8.onnx, translation_en_ar_decoder_int8.onnx
 */
object TranslationModels {
    // Base names for encoder-decoder models
    const val ENGLISH_TO_ARABIC_BASE = "translation_en_ar"
    const val ENGLISH_TO_SPANISH_BASE = "translation_en_es"
    const val ENGLISH_TO_FRENCH_BASE = "translation_en_fr"
    const val ENGLISH_TO_GERMAN_BASE = "translation_en_de"
    const val ENGLISH_TO_CHINESE_BASE = "translation_en_zh"
    const val SPANISH_TO_ENGLISH_BASE = "translation_es_en"
    const val FRENCH_TO_ENGLISH_BASE = "translation_fr_en"
    
    // Legacy single model names (for backward compatibility)
    const val ENGLISH_TO_ARABIC = "translation_en_ar.onnx"
    const val ENGLISH_TO_SPANISH = "translation_en_es.onnx"
    const val ENGLISH_TO_FRENCH = "translation_en_fr.onnx"
    const val ENGLISH_TO_GERMAN = "translation_en_de.onnx"
    const val ENGLISH_TO_CHINESE = "translation_en_zh.onnx"
    const val SPANISH_TO_ENGLISH = "translation_es_en.onnx"
    const val FRENCH_TO_ENGLISH = "translation_fr_en.onnx"
    
    // Helper function to get encoder model name
    fun getEncoderModelName(baseName: String): String = "${baseName}_encoder_int8.onnx"
    
    // Helper function to get decoder model name
    fun getDecoderModelName(baseName: String): String = "${baseName}_decoder_int8.onnx"
    
    // Helper function to get single model name
    fun getSingleModelName(baseName: String): String = "$baseName.onnx"
} 