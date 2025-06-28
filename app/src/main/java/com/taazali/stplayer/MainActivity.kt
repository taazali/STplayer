package com.taazali.stplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.ui.PlayerView
import com.taazali.stplayer.ui.theme.STplayerTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentLinkedQueue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            STplayerTheme {
                STplayerApp()
            }
        }
    }
}

@Composable
fun STplayerApp() {
    val videoPlayerViewModel: VideoPlayerViewModel = viewModel()
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "STplayer",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Video Player Section
            VideoPlayerSection(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                videoPlayerViewModel = videoPlayerViewModel
            )
            
            // Video Controls Section
            VideoControlsSection(
                modifier = Modifier.fillMaxWidth(),
                videoPlayerViewModel = videoPlayerViewModel
            )
        }
    }
}

@Composable
fun VideoPlayerSection(
    modifier: Modifier = Modifier,
    videoPlayerViewModel: VideoPlayerViewModel
) {
    val context = LocalContext.current
    val audioCaptureManager = remember { AudioCaptureManager() }
    val subtitleManager = remember { SubtitleManager(context) }
    val whisperBridge = remember { WhisperBridge() }
    val translationManager = remember { TranslationManager(context) }
    val coroutineScope = rememberCoroutineScope()
    
    // Observe subtitle state
    val currentSubtitle by subtitleManager.currentSubtitle.collectAsState()
    val subtitleVisible by subtitleManager.subtitleVisible.collectAsState()
    val fontSize by subtitleManager.fontSize.collectAsState()
    val outlineEnabled by subtitleManager.outlineEnabled.collectAsState()
    val isTranslated by subtitleManager.isTranslated.collectAsState()
    val translationCount by subtitleManager.translationCount.collectAsState()
    
    // Observe translation state
    val isTranslationModelLoaded by translationManager.isModelLoaded.collectAsState()
    val translationQuality by translationManager.translationQuality.collectAsState()
    val currentTranslationModel by translationManager.currentModel.collectAsState()
    val translationStats by translationManager.translationCount.collectAsState()
    val averageTranslationTime by translationManager.averageTranslationTime.collectAsState()
    
    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setAudioSink(audioCaptureManager.createAudioSink(subtitleManager, whisperBridge))
            .build().apply {
                // TODO: Replace with actual video URL from intent or user selection
                val mediaItem = MediaItem.fromUri("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
                setMediaItem(mediaItem)
                prepare()
            }
    }
    
    // Start audio capture when player is ready
    LaunchedEffect(exoPlayer) {
        println("🚀 [MAIN] Starting STplayer initialization...")
        
        // Initialize Whisper model
        println("🔧 [MAIN] Initializing Whisper transcription...")
        try {
            val modelName = "ggml-base.en.bin" // Small English model
            val modelAvailable = whisperBridge.isModelAvailable(context, modelName)
            
            if (modelAvailable) {
                val modelSize = whisperBridge.getModelSize(context, modelName)
                println("✅ [MAIN] Whisper model available: $modelName (${modelSize} bytes)")
                
                val success = whisperBridge.initializeModel(context, modelName)
                if (success) {
                    println("✅ [MAIN] Whisper model initialized successfully")
                    whisperBridge.setParameters("en", "transcribe")
                } else {
                    println("❌ [MAIN] Failed to initialize Whisper model")
                }
            } else {
                println("❌ [MAIN] Whisper model not available: $modelName")
                println("🔧 [MAIN] Available models: ${whisperBridge.getAvailableModels(context)}")
            }
        } catch (e: UnsatisfiedLinkError) {
            println("⚠️ [MAIN] Native library not available: ${e.message}")
            println("🔧 [MAIN] Using fallback transcription for demo")
        } catch (e: Exception) {
            println("❌ [MAIN] Whisper initialization error: ${e.message}")
            println("🔧 [MAIN] Using fallback transcription for demo")
        }
        
        // Initialize ONNX translation model
        println("🔧 [MAIN] Initializing ONNX translation...")
        val translationModelBaseName = "translation_en_ar" // Base name for encoder-decoder models
        
        // Test model detection first
        println("=== Testing Model Detection ===")
        val modelDetection = translationManager.testModelDetection()
        
        // Check for encoder-decoder models first
        val encoderModelName = "${translationModelBaseName}_encoder_int8.onnx"
        val decoderModelName = "${translationModelBaseName}_decoder_int8.onnx"
        
        if (modelDetection["complete_pairs"] as List<String>).contains(translationModelBaseName)) {
            println("✅ [MAIN] Encoder-decoder pair found: $translationModelBaseName")
            println("  - Encoder: $encoderModelName")
            println("  - Decoder: $decoderModelName")
            
            val success = translationManager.loadModel(translationModelBaseName, "en", "ar")
            if (success) {
                println("✅ [MAIN] ONNX encoder-decoder models loaded successfully")
                translationManager.setTranslationQuality(TranslationQuality.MEDIUM)
            } else {
                println("❌ [MAIN] Failed to load ONNX encoder-decoder models")
            }
        } else {
            // Fallback to single model
            val singleModelName = "$translationModelBaseName.onnx"
            if ((modelDetection["single_models"] as List<String>).contains(singleModelName)) {
                println("✅ [MAIN] Single model found: $singleModelName")
                
                val success = translationManager.loadModel(translationModelBaseName, "en", "ar")
                if (success) {
                    println("✅ [MAIN] ONNX single model loaded successfully")
                    translationManager.setTranslationQuality(TranslationQuality.MEDIUM)
                } else {
                    println("❌ [MAIN] Failed to load ONNX single model")
                }
            } else {
                println("❌ [MAIN] No translation models available")
                println("Expected files:")
                println("  - $encoderModelName")
                println("  - $decoderModelName")
                println("  - $singleModelName")
                println("Available models: ${modelDetection["available_models"]}")
                println("Using fallback translation for demo")
            }
        }
        
        println("🔧 [MAIN] Starting audio capture...")
        audioCaptureManager.startCapture()
        
        // Set up translation demo (English to Arabic)
        println("🔧 [MAIN] Configuring subtitle pipeline...")
        subtitleManager.setSourceLanguage("en")
        subtitleManager.setTargetLanguage("ar")
        subtitleManager.setTranslationManager(translationManager)
        
        // Start subtitle simulation (simulates real-time transcription)
        println("🔧 [MAIN] Starting subtitle simulation...")
        coroutineScope.launch {
            simulateRealTimeSubtitles { subtitle ->
                // This simulates the complete flow: Audio → Whisper → SubtitleManager → Translation → Display
                println("🔧 [MAIN] Processing simulated subtitle: '$subtitle'")
                subtitleManager.processTranscription(subtitle)
            }
        }
        
        println("✅ [MAIN] STplayer initialization completed")
    }
    
    DisposableEffect(Unit) {
        onDispose {
            audioCaptureManager.stopCapture()
            subtitleManager.cleanup()
            whisperBridge.cleanup()
            translationManager.unloadModel()
            exoPlayer.release()
        }
    }
    
    Box(modifier = modifier) {
        // ExoPlayer View
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false // We'll use our custom controls
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Real-time Subtitle Overlay
        if (subtitleVisible) {
            SubtitleOverlay(
                text = currentSubtitle,
                fontSize = fontSize,
                outlineEnabled = outlineEnabled,
                isTranslated = isTranslated,
                translationCount = translationCount,
                isTranslationModelLoaded = isTranslationModelLoaded,
                averageTranslationTime = averageTranslationTime,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            )
        }
        
        // Translation Status Indicator
        if (isTranslationModelLoaded) {
            TranslationStatusIndicator(
                modelName = currentTranslationModel ?: "Unknown",
                quality = translationQuality,
                translationCount = translationStats,
                averageTime = averageTranslationTime,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            )
        }
        
        // TODO: Add gesture detection for subtitle controls
        // TODO: Add JNI/Whisper integration for real-time transcription
        // TODO: Add ONNX integration for translation
    }
}

/**
 * Simulates real-time subtitle generation for testing the UI flow
 * This will be replaced with actual Whisper transcription in the next milestone
 */
private suspend fun simulateRealTimeSubtitles(onSubtitleUpdate: (String) -> Unit) {
    val sampleSubtitles = listOf(
        "Welcome to STplayer!",
        "This is a real-time subtitle demo.",
        "Audio is being captured from ExoPlayer.",
        "Soon this will use actual Whisper transcription.",
        "Translation features will be added next.",
        "The subtitle overlay updates dynamically.",
        "Just like MX Player or other premium apps.",
        "Built with modern Android technologies."
    )
    
    var index = 0
    while (true) {
        onSubtitleUpdate(sampleSubtitles[index % sampleSubtitles.size])
        delay(3000) // Update every 3 seconds
        index++
    }
}

@Composable
fun SubtitleOverlay(
    text: String,
    fontSize: Int = 18,
    outlineEnabled: Boolean = true,
    isTranslated: Boolean = false,
    translationCount: Int = 0,
    isTranslationModelLoaded: Boolean = false,
    averageTranslationTime: Long = 0,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.8f))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Show translation indicator
            if (isTranslated) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🌐 Translated",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    if (isTranslationModelLoaded) {
                        Text(
                            text = " (ONNX: ${averageTranslationTime}ms)",
                            color = Color.Green,
                            fontSize = 12.sp
                        )
                    } else {
                        Text(
                            text = " (Fallback)",
                            color = Color.Yellow,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TranslationStatusIndicator(
    modelName: String,
    quality: TranslationQuality,
    translationCount: Int,
    averageTime: Long,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.8f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "🤖 ONNX Translation",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Model: ${modelName.replace("translation_", "").replace(".onnx", "")}",
                color = Color.Gray,
                fontSize = 12.sp
            )
            
            Text(
                text = "Quality: $quality",
                color = Color.Gray,
                fontSize = 12.sp
            )
            
            Text(
                text = "Count: $translationCount",
                color = Color.Gray,
                fontSize = 12.sp
            )
            
            Text(
                text = "Avg: ${averageTime}ms",
                color = if (averageTime < 100) Color.Green else Color.Yellow,
                fontSize = 12.sp
            )
        }
    }
}

// Audio Capture Manager for capturing PCM data from ExoPlayer
class AudioCaptureManager {
    private val pcmBuffer = ConcurrentLinkedQueue<ByteArray>()
    private var isCapturing = false
    
    // This PCM buffer will be sent to Whisper.cpp for transcription
    private val audioBuffer = ByteBuffer.allocate(4096) // 4KB buffer
    
    // Audio processing parameters
    private val sampleRate = 16000 // Whisper expects 16kHz
    private val chunkDurationMs = 5000 // Process 5-second chunks
    private val chunkSize = sampleRate * 2 * chunkDurationMs / 1000 // 16-bit samples
    
    fun createAudioSink(subtitleManager: SubtitleManager, whisperBridge: WhisperBridge): AudioSink {
        return object : DefaultAudioSink.Builder().build() {
            override fun handleBuffer(
                buffer: ByteBuffer,
                presentationTimeUs: Long,
                encodedAccessUnitCount: Int
            ): Boolean {
                // Capture PCM data for transcription
                if (isCapturing) {
                    val pcmData = ByteArray(buffer.remaining())
                    buffer.get(pcmData)
                    pcmBuffer.offer(pcmData)
                    
                    // Process audio data in chunks for transcription
                    if (pcmBuffer.size >= 10) { // Process every 10 chunks
                        processAudioChunk(subtitleManager, whisperBridge)
                    }
                }
                
                return super.handleBuffer(buffer, presentationTimeUs, encodedAccessUnitCount)
            }
        }
    }
    
    private fun processAudioChunk(subtitleManager: SubtitleManager, whisperBridge: WhisperBridge) {
        val audioData = mutableListOf<ByteArray>()
        repeat(10) {
            pcmBuffer.poll()?.let { audioData.add(it) }
        }
        
        if (audioData.isNotEmpty()) {
            // Combine audio chunks and send to Whisper
            val combinedAudio = audioData.flatMap { it.toList() }.toByteArray()
            
            // TODO: Send to Whisper via JNI bridge for real transcription
            // TODO: Replace simulation with actual transcription result
            val transcription = whisperBridge.transcribeAudio(combinedAudio)
            println("Whisper transcription: $transcription")
            
            // Process transcription through subtitle pipeline
            if (transcription.isNotEmpty() && !transcription.startsWith("[ERROR")) {
                subtitleManager.processTranscription(transcription)
            } else if (transcription.startsWith("[ERROR")) {
                println("Whisper transcription error: $transcription")
                // Fallback to simulation for demo purposes
                subtitleManager.processTranscription("Audio captured, processing...")
            }
        }
    }
    
    fun startCapture() {
        isCapturing = true
        println("Audio capture started")
    }
    
    fun stopCapture() {
        isCapturing = false
        pcmBuffer.clear()
        println("Audio capture stopped")
    }
}

@Composable
fun VideoControlsSection(
    modifier: Modifier = Modifier,
    videoPlayerViewModel: VideoPlayerViewModel
) {
    var isPlaying by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Play/Pause Button
            IconButton(
                onClick = {
                    isPlaying = !isPlaying
                    // TODO: Implement actual play/pause functionality
                }
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Seek Bar Placeholder
            Slider(
                value = 0f,
                onValueChange = { /* TODO: Implement seek functionality */ },
                modifier = Modifier.weight(1f)
            )
            
            // TODO: Add volume controls
            // TODO: Add fullscreen toggle
            // TODO: Add subtitle toggle
        }
    }
}

// TODO: Implement VideoPlayerViewModel for state management
class VideoPlayerViewModel {
    // TODO: Add video state management
    // TODO: Add subtitle state management
    // TODO: Add transcription/translation state management
}

// TODO: Handle external video player intents (e.g., from Stremio/Torrentio)
// TODO: Implement JNI bridge for Whisper integration
// TODO: Implement ONNX runtime for translation
// TODO: Add gesture controls for subtitle positioning
// TODO: Add subtitle file loading and parsing