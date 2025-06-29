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
import androidx.lifecycle.ViewModel
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

@OptIn(ExperimentalMaterial3Api::class)
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
        
        val completePairs = modelDetection["complete_pairs"] as? List<String> ?: emptyList()
        if (completePairs.contains(translationModelBaseName)) {
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
            val singleModels = modelDetection["single_models"] as? List<String> ?: emptyList()
            if (singleModels.contains(singleModelName)) {
                println("✅ [MAIN] Single model found: $singleModelName")
                
                val success = translationManager.loadModel(translationModelBaseName, "en", "ar")
                if (success) {
                    println("✅ [MAIN] ONNX single model loaded successfully")
                    translationManager.setTranslationQuality(TranslationQuality.MEDIUM)
                } else {
                    println("❌ [MAIN] Failed to load ONNX single model")
                }
            } else {
                println("❌ [MAIN] No ONNX models found, using fallback translation")
                val success = translationManager.loadModel(translationModelBaseName, "en", "ar")
                if (success) {
                    println("✅ [MAIN] Fallback translation model loaded")
                } else {
                    println("❌ [MAIN] Failed to load fallback translation model")
                }
            }
        }
        
        // Start audio capture
        audioCaptureManager.startCapture()
        println("✅ [MAIN] Audio capture started")
        
        // Start subtitle processing
        subtitleManager.startProcessing()
        println("✅ [MAIN] Subtitle processing started")
        
        println("🚀 [MAIN] STplayer initialization completed successfully!")
    }
    
    // Cleanup on dispose
    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
            audioCaptureManager.stopCapture()
            subtitleManager.stopProcessing()
            println("🧹 [MAIN] STplayer cleanup completed")
        }
    }
    
    Column(modifier = modifier) {
        // Video Player
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false // We'll use our custom controls
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        
        // Subtitle Display
        if (subtitleVisible && currentSubtitle.isNotEmpty()) {
            SubtitleOverlay(
                subtitle = currentSubtitle,
                fontSize = fontSize,
                outlineEnabled = outlineEnabled,
                isTranslated = isTranslated,
                translationCount = translationCount,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
        
        // Translation Status Indicator
        TranslationStatusIndicator(
            isModelLoaded = isTranslationModelLoaded,
            translationQuality = translationQuality,
            currentModel = currentTranslationModel,
            translationStats = translationStats,
            averageTranslationTime = averageTranslationTime,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
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
        return CustomAudioSink(subtitleManager, whisperBridge, this)
    }
    
    fun addPcmData(data: ByteArray) {
        if (isCapturing) {
            pcmBuffer.offer(data)
        }
    }
    
    fun getBufferSize(): Int = pcmBuffer.size
    
    fun processAudioChunk(subtitleManager: SubtitleManager, whisperBridge: WhisperBridge) {
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

// Custom AudioSink implementation for capturing PCM data
class CustomAudioSink(
    private val subtitleManager: SubtitleManager,
    private val whisperBridge: WhisperBridge,
    private val audioCaptureManager: AudioCaptureManager
) : AudioSink {
    
    private val delegate = DefaultAudioSink.Builder().build()
    
    override fun handleBuffer(
        buffer: ByteBuffer,
        presentationTimeUs: Long,
        encodedAccessUnitCount: Int
    ): Boolean {
        // Capture PCM data for transcription
        val pcmData = ByteArray(buffer.remaining())
        buffer.get(pcmData)
        audioCaptureManager.addPcmData(pcmData)
        
        // Process audio data in chunks for transcription
        if (audioCaptureManager.getBufferSize() >= 10) { // Process every 10 chunks
            audioCaptureManager.processAudioChunk(subtitleManager, whisperBridge)
        }
        
        return delegate.handleBuffer(buffer, presentationTimeUs, encodedAccessUnitCount)
    }
    
    override fun supportsFormat(format: androidx.media3.common.Format): Boolean {
        return delegate.supportsFormat(format)
    }
    
    override fun getFormatSupport(format: androidx.media3.common.Format): Int {
        return delegate.getFormatSupport(format)
    }
    
    override fun configure(format: androidx.media3.common.Format, p1: Int, p2: IntArray?): Unit {
        delegate.configure(format, p1, p2)
    }
    
    override fun getCurrentPositionUs(isSourceEnded: Boolean): Long {
        return delegate.getCurrentPositionUs(isSourceEnded)
    }
    
    override fun play(): Unit = delegate.play()
    
    override fun playToEndOfStream(): Unit = delegate.playToEndOfStream()
    
    override fun isEnded(): Boolean = delegate.isEnded()
    
    override fun hasPendingData(): Boolean = delegate.hasPendingData()
    
    override fun handleDiscontinuity(): Unit = delegate.handleDiscontinuity()
    
    override fun setPlaybackParameters(playbackParameters: androidx.media3.common.PlaybackParameters): Unit = delegate.setPlaybackParameters(playbackParameters)
    
    override fun getPlaybackParameters(): androidx.media3.common.PlaybackParameters = delegate.playbackParameters
    
    override fun setSkipSilenceEnabled(skipSilenceEnabled: Boolean): Unit = delegate.setSkipSilenceEnabled(skipSilenceEnabled)
    
    override fun getSkipSilenceEnabled(): Boolean = delegate.skipSilenceEnabled
    
    override fun setListener(listener: AudioSink.Listener): Unit = delegate.setListener(listener)
    
    override fun setAudioAttributes(audioAttributes: androidx.media3.common.AudioAttributes): Unit = delegate.setAudioAttributes(audioAttributes)
    
    override fun setAudioSessionId(audioSessionId: Int): Unit = delegate.setAudioSessionId(audioSessionId)
    
    override fun setAuxEffectInfo(auxEffectInfo: androidx.media3.common.AuxEffectInfo): Unit = delegate.setAuxEffectInfo(auxEffectInfo)
    
    override fun setPreferredDevice(preferredDevice: android.media.AudioDeviceInfo?): Unit = delegate.setPreferredDevice(preferredDevice)
    
    override fun setVolume(volume: Float): Unit = delegate.setVolume(volume)
    
    override fun pause(): Unit = delegate.pause()
    
    override fun flush(): Unit = delegate.flush()
    
    override fun reset(): Unit = delegate.reset()
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

// VideoPlayerViewModel for state management
class VideoPlayerViewModel : ViewModel() {
    // TODO: Add video state management
    // TODO: Add subtitle state management
    // TODO: Add transcription/translation state management
}

@Composable
fun SubtitleOverlay(
    subtitle: String,
    fontSize: Int = 18,
    outlineEnabled: Boolean = true,
    isTranslated: Boolean = false,
    translationCount: Int = 0,
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
                text = subtitle,
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
                    Text(
                        text = " (Count: $translationCount)",
                        color = Color.Green,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TranslationStatusIndicator(
    isModelLoaded: Boolean,
    translationQuality: TranslationQuality,
    currentModel: String?,
    translationStats: Int,
    averageTranslationTime: Long,
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
                text = "Model: ${currentModel?.replace("translation_", "")?.replace(".onnx", "") ?: "None"}",
                color = Color.Gray,
                fontSize = 12.sp
            )
            
            Text(
                text = "Quality: $translationQuality",
                color = Color.Gray,
                fontSize = 12.sp
            )
            
            Text(
                text = "Count: $translationStats",
                color = Color.Gray,
                fontSize = 12.sp
            )
            
            Text(
                text = "Avg: ${averageTranslationTime}ms",
                color = if (averageTranslationTime < 100) Color.Green else Color.Yellow,
                fontSize = 12.sp
            )
            
            Text(
                text = "Status: ${if (isModelLoaded) "✅ Loaded" else "❌ Not Loaded"}",
                color = if (isModelLoaded) Color.Green else Color.Red,
                fontSize = 12.sp
            )
        }
    }
}

// TODO: Handle external video player intents (e.g., from Stremio/Torrentio)
// TODO: Implement JNI bridge for Whisper integration
// TODO: Implement ONNX runtime for translation
// TODO: Add gesture controls for subtitle positioning
// TODO: Add subtitle file loading and parsing