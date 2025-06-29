package com.taazali.stplayer

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Define the TAG constant at the top of the file
private const val TAG = "SubtitleManager"

/**
 * Manages subtitle state and flow from audio capture to display
 * 
 * This class handles:
 * - Current subtitle text
 * - Subtitle visibility
 * - Subtitle styling preferences
 * - Flow from transcription to translation to display
 * 
 * [CURSOR] Enhanced with proper Android logging and better error handling
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
    
    init {
        Log.d(TAG, "SubtitleManager initialized")
    }
    
    /**
     * Set the translation manager instance
     */
    fun setTranslationManager(manager: TranslationManager) {
        translationManager = manager
        Log.d(TAG, "TranslationManager set in SubtitleManager")
    }
    
    /**
     * Update the current subtitle text
     * Called when new transcription is received from Whisper
     */
    fun updateSubtitle(text: String) {
        Log.d(TAG, "Updating subtitle: '$text'")
        _currentSubtitle.value = text
        _subtitleVisible.value = text.isNotEmpty()
    }
    
    /**
     * Show or hide subtitle overlay
     */
    fun setSubtitleVisible(visible: Boolean) {
        Log.d(TAG, "Setting subtitle visibility: $visible")
        _subtitleVisible.value = visible
    }
    
    /**
     * Update subtitle font size
     */
    fun setFontSize(size: Int) {
        val clampedSize = size.coerceIn(12, 32) // Min 12, Max 32
        Log.d(TAG, "Setting font size: $clampedSize (requested: $size)")
        _fontSize.value = clampedSize
    }
    
    /**
     * Toggle subtitle outline
     */
    fun setOutlineEnabled(enabled: Boolean) {
        Log.d(TAG, "Setting outline enabled: $enabled")
        _outlineEnabled.value = enabled
    }
    
    /**
     * Set source language for transcription
     */
    fun setSourceLanguage(language: String) {
        Log.d(TAG, "Setting source language: $language")
        _sourceLanguage.value = language
        checkTranslationNeeded()
    }
    
    /**
     * Set target language for translation
     */
    fun setTargetLanguage(language: String) {
        Log.d(TAG, "Setting target language: $language")
        _targetLanguage.value = language
        _isTranslated.value = language != _sourceLanguage.value
        checkTranslationNeeded()
    }
    
    /**
     * Process transcribed text and prepare for display
     * This is the main injection point for the transcription pipeline
     * 
     * Flow: Audio → Whisper → processTranscription → Translation → Display
     * 
     * [CURSOR] Enhanced with better logging and error handling
     */
    fun processTranscription(transcribedText: String) {
        Log.d(TAG, "Processing transcription: '$transcribedText'")
        
        // TODO: Add text cleaning and formatting
        // TODO: Add confidence scoring and filtering
        
        val sourceLang = _sourceLanguage.value
        val targetLang = _targetLanguage.value
        
        Log.d(TAG, "Language configuration:")
        Log.d(TAG, "  - Source language: $sourceLang")
        Log.d(TAG, "  - Target language: $targetLang")
        Log.d(TAG, "  - Translation needed: ${sourceLang != targetLang}")
        
        if (sourceLang != targetLang && translationManager?.isTranslationSupported(sourceLang, targetLang) == true) {
            Log.d(TAG, "Translation needed - sending to translation pipeline")
            // Translation needed - send to translation pipeline
            processTranslation(transcribedText, sourceLang, targetLang)
        } else {
            Log.d(TAG, "No translation needed - displaying original text")
            // No translation needed - display original text
            updateSubtitle(transcribedText)
        }
    }
    
    /**
     * Process translated text and prepare for display
     * This is the main injection point for the translation pipeline
     * 
     * [CURSOR] Enhanced with better error handling and logging
     */
    fun processTranslation(originalText: String, sourceLanguage: String, targetLanguage: String) {
        Log.d(TAG, "Processing translation...")
        Log.d(TAG, "Original text: '$originalText'")
        Log.d(TAG, "Translation: $sourceLanguage → $targetLanguage")
        
        try {
            // TODO: Add translation quality checks
            // TODO: Add fallback to original text if translation fails
            
            val translatedText = translationManager?.translate(originalText, sourceLanguage, targetLanguage) ?: originalText
            
            Log.d(TAG, "Translation result: '$translatedText'")
            updateSubtitle(translatedText)
            
        } catch (e: Exception) {
            Log.e(TAG, "Translation processing failed: ${e.message}")
            Log.d(TAG, "Using original text as fallback")
            updateSubtitle(originalText)
        }
    }
    
    /**
     * Load translation model for the current language pair
     * 
     * [CURSOR] Enhanced with better error handling and logging
     */
    fun loadTranslationModel(): Boolean {
        val sourceLang = _sourceLanguage.value
        val targetLang = _targetLanguage.value
        
        Log.d(TAG, "Loading translation model for: $sourceLang → $targetLang")
        
        if (sourceLang == targetLang) {
            Log.d(TAG, "No translation needed for same language: $sourceLang")
            return true
        }
        
        if (translationManager?.isTranslationSupported(sourceLang, targetLang) != true) {
            Log.w(TAG, "Translation not supported: $sourceLang → $targetLang")
            return false
        }
        
        val modelName = "translation_${sourceLang}_${targetLang}"
        val success = translationManager?.loadModel(modelName, sourceLang, targetLang) ?: false
        
        if (success) {
            Log.d(TAG, "Translation model loaded successfully: $modelName")
        } else {
            Log.e(TAG, "Failed to load translation model: $modelName")
        }
        
        return success
    }
    
    /**
     * Set translation quality
     */
    fun setTranslationQuality(quality: TranslationQuality) {
        Log.d(TAG, "Setting translation quality: $quality")
        translationManager?.setTranslationQuality(quality)
    }
    
    /**
     * Check if translation is needed and load model if necessary
     */
    private fun checkTranslationNeeded() {
        val sourceLang = _sourceLanguage.value
        val targetLang = _targetLanguage.value
        
        Log.d(TAG, "Checking translation need: $sourceLang → $targetLang")
        
        if (sourceLang != targetLang) {
            Log.d(TAG, "Translation needed, loading model...")
            loadTranslationModel()
        } else {
            Log.d(TAG, "No translation needed for same language")
        }
    }
    
    /**
     * Get available translation models
     */
    fun getAvailableTranslationModels(): List<String> {
        val models = translationManager?.getAvailableModels() ?: emptyList()
        Log.d(TAG, "Available translation models: $models")
        return models
    }
    
    /**
     * Check if translation is supported for current language pair
     */
    fun isTranslationSupported(): Boolean {
        val supported = translationManager?.isTranslationSupported(_sourceLanguage.value, _targetLanguage.value) ?: false
        Log.d(TAG, "Translation supported for ${_sourceLanguage.value} → ${_targetLanguage.value}: $supported")
        return supported
    }
    
    /**
     * Clear current subtitle
     */
    fun clearSubtitle() {
        Log.d(TAG, "Clearing subtitle")
        _currentSubtitle.value = ""
        _subtitleVisible.value = false
    }
    
    /**
     * Reset all subtitle settings to defaults
     */
    fun resetToDefaults() {
        Log.d(TAG, "Resetting subtitle settings to defaults")
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
        Log.d(TAG, "Cleaning up SubtitleManager resources")
        translationManager?.unloadModel()
    }
    
    /**
     * Start subtitle processing
     * 
     * [CURSOR] Enhanced with better logging
     */
    fun startProcessing() {
        Log.d(TAG, "Starting subtitle processing...")
        _subtitleVisible.value = true
        Log.d(TAG, "✅ Subtitle processing started")
    }
    
    /**
     * Stop subtitle processing
     * 
     * [CURSOR] Enhanced with better logging
     */
    fun stopProcessing() {
        Log.d(TAG, "Stopping subtitle processing...")
        _subtitleVisible.value = false
        _currentSubtitle.value = ""
        Log.d(TAG, "✅ Subtitle processing stopped")
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