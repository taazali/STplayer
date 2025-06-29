#include <jni.h>
#include <string>
#include <android/log.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <vector>
#include <memory>
#include <random>

// TODO: Include actual Whisper.cpp headers when integrated
// #include "whisper.h"
// #include "ggml.h"

#define LOG_TAG "WhisperJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Forward declaration
static std::string simulateTranscription(const std::vector<float>& samples);

// Global variables for Whisper context and model
static struct {
    // TODO: Replace with actual Whisper context
    // struct whisper_context* ctx = nullptr;
    void* ctx = nullptr;
    bool model_loaded = false;
    std::string model_path;
    int sample_rate = 16000; // Whisper expects 16kHz
    int n_threads = 4; // Number of threads for inference
} whisper_state;

// Helper function to load file from assets
static bool load_asset_file(AAssetManager* asset_manager, const char* filename, std::vector<uint8_t>& data) {
    AAsset* asset = AAssetManager_open(asset_manager, filename, AASSET_MODE_BUFFER);
    if (!asset) {
        LOGE("Failed to open asset: %s", filename);
        return false;
    }
    
    long size = AAsset_getLength(asset);
    data.resize(size);
    
    long bytes_read = AAsset_read(asset, data.data(), size);
    AAsset_close(asset);
    
    if (bytes_read != size) {
        LOGE("Failed to read asset: %s (expected %ld, got %ld)", filename, size, bytes_read);
        return false;
    }
    
    LOGI("Successfully loaded asset: %s (%ld bytes)", filename, size);
    return true;
}

extern "C" {

/**
 * Initialize Whisper model from assets
 * 
 * @param env JNI environment
 * @param thiz Java object reference
 * @param assetManager Android asset manager
 * @param modelName Name of the model file in assets/whisper/
 * @return true if initialization successful, false otherwise
 */
JNIEXPORT jboolean JNICALL
Java_com_taazali_stplayer_WhisperBridge_nativeInitializeModel(JNIEnv *env, jobject thiz, jobject assetManager, jstring modelName) {
    (void)thiz; // Suppress unused parameter warning
    const char* model_name = env->GetStringUTFChars(modelName, 0);
    
    LOGI("Initializing Whisper model: %s", model_name);
    
    try {
        // Get asset manager
        AAssetManager* asset_manager = AAssetManager_fromJava(env, assetManager);
        if (!asset_manager) {
            LOGE("Failed to get asset manager");
            env->ReleaseStringUTFChars(modelName, model_name);
            return JNI_FALSE;
        }
        
        // Load model from assets
        std::vector<uint8_t> model_data;
        std::string asset_path = std::string("whisper/") + model_name;
        
        if (!load_asset_file(asset_manager, asset_path.c_str(), model_data)) {
            LOGE("Failed to load model from assets: %s", asset_path.c_str());
            env->ReleaseStringUTFChars(modelName, model_name);
            return JNI_FALSE;
        }
        
        // TODO: Initialize Whisper context with loaded model data
        // whisper_state.ctx = whisper_init_from_buffer(model_data.data(), model_data.size());
        // if (!whisper_state.ctx) {
        //     LOGE("Failed to initialize Whisper context");
        //     env->ReleaseStringUTFChars(modelName, model_name);
        //     return JNI_FALSE;
        // }
        
        // Placeholder implementation for now
        whisper_state.model_loaded = true;
        whisper_state.model_path = model_name;
        
        LOGI("Whisper model initialized successfully: %s", model_name);
        
        env->ReleaseStringUTFChars(modelName, model_name);
        return JNI_TRUE;
        
    } catch (const std::exception& e) {
        LOGE("Exception during model initialization: %s", e.what());
        env->ReleaseStringUTFChars(modelName, model_name);
        return JNI_FALSE;
    }
}

/**
 * Transcribe audio data using Whisper.cpp
 * 
 * @param env JNI environment
 * @param thiz Java object reference
 * @param pcmBuffer Raw PCM audio data (16-bit, 16kHz, mono)
 * @return Transcribed text from the audio
 */
JNIEXPORT jstring JNICALL
Java_com_taazali_stplayer_WhisperBridge_transcribeAudioNative(JNIEnv *env, jobject thiz, jbyteArray pcmBuffer) {
    (void)thiz; // Suppress unused parameter warning
    if (!whisper_state.model_loaded) {
        LOGE("Whisper model not loaded");
        return env->NewStringUTF("[ERROR: Model not loaded]");
    }
    
    jsize buffer_size = env->GetArrayLength(pcmBuffer);
    jbyte* buffer_data = env->GetByteArrayElements(pcmBuffer, nullptr);
    
    LOGI("Transcribing audio buffer: %d bytes", buffer_size);
    
    try {
        // Convert PCM buffer to float samples
        std::vector<float> samples;
        samples.reserve(buffer_size / 2); // 16-bit samples = 2 bytes per sample
        
        for (int i = 0; i < buffer_size; i += 2) {
            // Convert 16-bit PCM to float (-1.0 to 1.0)
            int16_t sample = (buffer_data[i + 1] << 8) | (buffer_data[i] & 0xFF);
            float float_sample = sample / 32768.0f;
            samples.push_back(float_sample);
        }
        
        // TODO: Implement actual Whisper transcription
        // struct whisper_full_params params = whisper_full_params_default();
        // params.print_progress = false;
        // params.print_special = false;
        // params.language = "en";
        // params.n_threads = whisper_state.n_threads;
        // 
        // int result = whisper_full(whisper_state.ctx, params, samples.data(), samples.size());
        // if (result != 0) {
        //     LOGE("Whisper transcription failed: %d", result);
        //     env->ReleaseByteArrayElements(pcmBuffer, buffer_data, JNI_ABORT);
        //     return env->NewStringUTF("[ERROR: Transcription failed]");
        // }
        // 
        // // Get transcription result
        // const int n_segments = whisper_full_n_segments(whisper_state.ctx);
        // std::string transcription;
        // for (int i = 0; i < n_segments; ++i) {
        //     const char* text = whisper_full_get_segment_text(whisper_state.ctx, i);
        //     transcription += text;
        // }
        
        // Placeholder implementation - simulate transcription based on audio characteristics
        std::string transcription = simulateTranscription(samples);
        
        LOGI("Transcription completed: '%s'", transcription.c_str());
        
        env->ReleaseByteArrayElements(pcmBuffer, buffer_data, JNI_ABORT);
        return env->NewStringUTF(transcription.c_str());
        
    } catch (const std::exception& e) {
        LOGE("Exception during transcription: %s", e.what());
        env->ReleaseByteArrayElements(pcmBuffer, buffer_data, JNI_ABORT);
        return env->NewStringUTF("[ERROR: Transcription exception]");
    }
}

/**
 * Set transcription parameters
 * 
 * @param env JNI environment
 * @param thiz Java object reference
 * @param language Language code (e.g., "en", "es", "fr")
 * @param task Task type ("transcribe" or "translate")
 * @return true if parameters set successfully
 */
JNIEXPORT jboolean JNICALL
Java_com_taazali_stplayer_WhisperBridge_setParameters(JNIEnv *env, jobject thiz, jstring language, jstring task) {
    (void)thiz; // Suppress unused parameter warning
    const char* lang = env->GetStringUTFChars(language, 0);
    const char* task_str = env->GetStringUTFChars(task, 0);
    
    LOGI("Setting Whisper parameters: language=%s, task=%s", lang, task_str);
    
    // TODO: Implement actual parameter setting
    // Store language and task for use in transcription
    
    env->ReleaseStringUTFChars(language, lang);
    env->ReleaseStringUTFChars(task, task_str);
    return JNI_TRUE;
}

/**
 * Clean up Whisper resources
 * 
 * @param env JNI environment
 * @param thiz Java object reference
 */
JNIEXPORT void JNICALL
Java_com_taazali_stplayer_WhisperBridge_cleanup(JNIEnv *env, jobject thiz) {
    (void)env; // Suppress unused parameter warning
    (void)thiz; // Suppress unused parameter warning
    
    LOGI("Cleaning up Whisper resources");
    
    // TODO: Implement actual cleanup
    // if (whisper_state.ctx) {
    //     whisper_free(whisper_state.ctx);
    //     whisper_state.ctx = nullptr;
    // }
    
    whisper_state.model_loaded = false;
    whisper_state.model_path.clear();
}

/**
 * Get Whisper status information
 * 
 * @param env JNI environment
 * @param thiz Java object reference
 * @return Status string
 */
JNIEXPORT jstring JNICALL
Java_com_taazali_stplayer_WhisperBridge_getStatus(JNIEnv *env, jobject thiz) {
    (void)thiz; // Suppress unused parameter warning
    
    std::string status = "Whisper Status: ";
    status += whisper_state.model_loaded ? "Model loaded" : "Model not loaded";
    status += " (" + whisper_state.model_path + ")";
    status += ", Sample rate: " + std::to_string(whisper_state.sample_rate);
    status += ", Threads: " + std::to_string(whisper_state.n_threads);
    
    return env->NewStringUTF(status.c_str());
}

} // extern "C"

/**
 * Simulate transcription based on audio characteristics
 * This is a placeholder implementation until actual Whisper.cpp is integrated
 */
static std::string simulateTranscription(const std::vector<float>& samples) {
    if (samples.empty()) {
        return "[Silence]";
    }
    
    // Calculate audio characteristics
    float max_amplitude = 0.0f;
    float avg_amplitude = 0.0f;
    int zero_crossings = 0;
    
    for (size_t i = 0; i < samples.size(); ++i) {
        float abs_sample = std::abs(samples[i]);
        max_amplitude = std::max(max_amplitude, abs_sample);
        avg_amplitude += abs_sample;
        
        if (i > 0 && ((samples[i] >= 0) != (samples[i-1] >= 0))) {
            zero_crossings++;
        }
    }
    avg_amplitude /= samples.size();
    
    // Generate simulated transcription based on audio characteristics
    std::random_device rd;
    std::mt19937 gen(rd());
    std::uniform_int_distribution<> dis(0, 3);
    
    std::string transcription;
    
    if (max_amplitude < 0.01f) {
        transcription = "[Silence or very quiet audio]";
    } else if (max_amplitude < 0.1f) {
        transcription = "[Quiet speech detected]";
    } else if (zero_crossings > static_cast<int>(samples.size()) / 100) {
        transcription = "[Speech with varied pitch]";
    } else {
        transcription = "[Clear speech detected]";
    }
    
    // Add some random simulated text
    const char* phrases[] = {
        " Hello world",
        " Testing audio",
        " Whisper simulation",
        " Audio transcription"
    };
    
    transcription += phrases[dis(gen)];
    
    return transcription;
} 