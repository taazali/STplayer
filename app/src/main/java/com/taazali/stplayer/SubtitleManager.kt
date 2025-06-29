package com.taazali.stplayer

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages subtitle state and flow from audio capture to display
 * 
 * This class handles:
 * - Current subtitle text
 * - Subtitle visibility
 * - Subtitle styling preferences
 * - Flow from transcription to translation to display
 */
class SubtitleManager(context: Context) {
    
    // Translation manager integration
    private var translationManager: TranslationManager? = null
    
    // Subtitle state
    private val _currentSubtitle = MutableStateFlow("")
    val currentSubtitle: StateFlow<String> = _currentSubtitle.asStateFlow()
    
    private val _subtitleVisible = MutableStateFlow(false)
    val subtitleVisible: StateFlow<Boolean> = _subtitleVisible.asStateFlow()
    
    // Subtitle styling preferences (MX Player style)
    private val _fontSize = MutableStateFlow(18)
    val fontSize: StateFlow<Int> = _fontSize.asStateFlow()
    
    private val _outlineEnabled = MutableStateFlow(true)
    val outlineEnabled: StateFlow<Boolean> = _outlineEnabled.asStateFlow()
    
    // Language and translation state
    private val _sourceLanguage = MutableStateFlow("en")
    val sourceLanguage: StateFlow<String> = _sourceLanguage.asStateFlow()
    
    private val _targetLanguage = MutableStateFlow("en")
    val targetLanguage: StateFlow<String> = _targetLanguage.asStateFlow()
    
    private val _isTranslated = MutableStateFlow(false)
    val isTranslated: StateFlow<Boolean> = _isTranslated.asStateFlow()
    
    // Translation manager state access
    val isTranslationModelLoaded: StateFlow<Boolean>
        get() = translationManager?.isModelLoaded ?: MutableStateFlow(false).asStateFlow()
    
    val translationQuality: StateFlow<TranslationQuality>
        get() = translationManager?.translationQuality ?: MutableStateFlow(TranslationQuality.MEDIUM).asStateFlow()
    
    val translationCount: StateFlow<Int>
        get() = translationManager?.translationCount ?: MutableStateFlow(0).asStateFlow()
    
    val averageTranslationTime: StateFlow<Long>
        get() = translationManager?.averageTranslationTime ?: MutableStateFlow(0L).asStateFlow()
    
    /**
     * Set the translation manager instance
     */
    fun setTranslationManager(manager: TranslationManager) {
        translationManager = manager
        println("TranslationManager set in SubtitleManager")
    }
    
    /**
     * Update the current subtitle text
     * Called when new transcription is received from Whisper
     */
    fun updateSubtitle(text: String) {
        _currentSubtitle.value = text
        _subtitleVisible.value = text.isNotEmpty()
    }
    
    /**
     * Show or hide subtitle overlay
     */
    fun setSubtitleVisible(visible: Boolean) {
        _subtitleVisible.value = visible
    }
    
    /**
     * Update subtitle font size
     */
    fun setFontSize(size: Int) {
        _fontSize.value = size.coerceIn(12, 32) // Min 12, Max 32
    }
    
    /**
     * Toggle subtitle outline
     */
    fun setOutlineEnabled(enabled: Boolean) {
        _outlineEnabled.value = enabled
    }
    
    /**
     * Set source language for transcription
     */
    fun setSourceLanguage(language: String) {
        _sourceLanguage.value = language
        checkTranslationNeeded()
    }
    
    /**
     * Set target language for translation
     */
    fun setTargetLanguage(language: String) {
        _targetLanguage.value = language
        _isTranslated.value = language != _sourceLanguage.value
        checkTranslationNeeded()
    }
    
    /**
     * Process transcribed text and prepare for display
     * This is the main injection point for the transcription pipeline
     * 
     * Flow: Audio → Whisper → processTranscription → Translation → Display
     */
    fun processTranscription(transcribedText: String) {
        println("🔧 [SUBTITLE] Processing transcription: '$transcribedText'")
        
        // TODO: Add text cleaning and formatting
        // TODO: Add confidence scoring and filtering
        
        val sourceLang = _sourceLanguage.value
        val targetLang = _targetLanguage.value
        
        println("🔧 [SUBTITLE] Language configuration:")
        println("  - Source language: $sourceLang")
        println("  - Target language: $targetLang")
        println("  - Translation needed: ${sourceLang != targetLang}")
        
        if (sourceLang != targetLang && translationManager?.isTranslationSupported(sourceLang, targetLang) == true) {
            println("🔧 [SUBTITLE] Translation needed - sending to translation pipeline")
            // Translation needed - send to translation pipeline
            processTranslation(transcribedText, sourceLang, targetLang)
        } else {
            println("🔧 [SUBTITLE] No translation needed - displaying original text")
            // No translation needed - display original text
            updateSubtitle(transcribedText)
        }
    }
    
    /**
     * Process translated text and prepare for display
     * This is the main injection point for the translation pipeline
     */
    fun processTranslation(originalText: String, sourceLanguage: String, targetLanguage: String) {
        println("🔧 [SUBTITLE] Processing translation...")
        println("🔧 [SUBTITLE] Original text: '$originalText'")
        println("🔧 [SUBTITLE] Translation: $sourceLanguage → $targetLanguage")
        
        // TODO: Add translation quality checks
        // TODO: Add fallback to original text if translation fails
        
        val translatedText = translationManager?.translate(originalText, sourceLanguage, targetLanguage) ?: originalText
        
        println("✅ [SUBTITLE] Translation result: '$translatedText'")
        updateSubtitle(translatedText)
    }
    
    /**
     * Load translation model for the current language pair
     */
    fun loadTranslationModel(): Boolean {
        val sourceLang = _sourceLanguage.value
        val targetLang = _targetLanguage.value
        
        if (sourceLang == targetLang) {
            println("No translation needed for same language: $sourceLang")
            return true
        }
        
        if (translationManager?.isTranslationSupported(sourceLang, targetLang) != true) {
            println("Translation not supported: $sourceLang → $targetLang")
            return false
        }
        
        val modelName = "translation_${sourceLang}_${targetLang}.onnx"
        val success = translationManager?.loadModel(modelName, sourceLang, targetLang) ?: false
        
        if (success) {
            println("Translation model loaded successfully: $modelName")
        } else {
            println("Failed to load translation model: $modelName")
        }
        
        return success
    }
    
    /**
     * Set translation quality
     */
    fun setTranslationQuality(quality: TranslationQuality) {
        translationManager?.setTranslationQuality(quality)
    }
    
    /**
     * Check if translation is needed and load model if necessary
     */
    private fun checkTranslationNeeded() {
        val sourceLang = _sourceLanguage.value
        val targetLang = _targetLanguage.value
        
        if (sourceLang != targetLang) {
            loadTranslationModel()
        }
    }
    
    /**
     * Get available translation models
     */
    fun getAvailableTranslationModels(): List<String> {
        return translationManager?.getAvailableModels() ?: emptyList()
    }
    
    /**
     * Check if translation is supported for current language pair
     */
    fun isTranslationSupported(): Boolean {
        return translationManager?.isTranslationSupported(_sourceLanguage.value, _targetLanguage.value) ?: false
    }
    
    /**
     * Clear current subtitle
     */
    fun clearSubtitle() {
        _currentSubtitle.value = ""
        _subtitleVisible.value = false
    }
    
    /**
     * Reset all subtitle settings to defaults
     */
    fun resetToDefaults() {
        _currentSubtitle.value = ""
        _subtitleVisible.value = false
        _fontSize.value = 18
        _outlineEnabled.value = true
        _sourceLanguage.value = "en"
        _targetLanguage.value = "en"
        _isTranslated.value = false
        
        // Unload translation model
        translationManager?.unloadModel()
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        translationManager?.unloadModel()
    }
    
    /**
     * Start subtitle processing
     */
    fun startProcessing() {
        println("🔧 [SUBTITLE] Starting subtitle processing...")
        _subtitleVisible.value = true
        println("✅ [SUBTITLE] Subtitle processing started")
    }
    
    /**
     * Stop subtitle processing
     */
    fun stopProcessing() {
        println("🔧 [SUBTITLE] Stopping subtitle processing...")
        _subtitleVisible.value = false
        _currentSubtitle.value = ""
        println("✅ [SUBTITLE] Subtitle processing stopped")
    }
    
    companion object {
        // Supported languages for transcription and translation
        val SUPPORTED_LANGUAGES = mapOf(
            "en" to "English",
            "es" to "Spanish", 
            "fr" to "French",
            "de" to "German",
            "it" to "Italian",
            "pt" to "Portuguese",
            "ru" to "Russian",
            "ja" to "Japanese",
            "ko" to "Korean",
            "zh" to "Chinese",
            "ar" to "Arabic"
        )
    }
} 