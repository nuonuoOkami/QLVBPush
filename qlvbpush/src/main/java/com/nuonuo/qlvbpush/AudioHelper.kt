package com.nuonuo.qlvbpush

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.log

/**

 * @author ：leo
 * @创建时间 on 2022/10/26 20:53
 * @version 1
 * @描述 音频助手 负责和cpp交互
 */
@SuppressLint("MissingPermission")
class AudioHelper(private var channels: Int = 2) {
    private val TAG = "AudioHelper"

    // 通道数为2，说明是2个通道(人类的耳朵，两个耳朵， 左声道/右声道)
    private lateinit var audioRecord: AudioRecord
    private var executorService: ExecutorService = Executors.newSingleThreadExecutor()
    var inputSamples = 0
    private var isLive = false
    private var channelConfig = AudioFormat.CHANNEL_IN_STEREO
    fun startLive() {
        isLive = true
        executorService.submit {
            audioRecord.startRecording() // 开始录音（调用Android的API录制手机麦克风的声音）
            // 单通道样本数：1024
            // 位深： 16bit位 2字节
            // 声道数：双声道
            // 1024单通道样本数 * 2 * 2 = 4096
            val bytes = ByteArray(inputSamples) // 接收录制声音数据的 byte[]
            // 读取数据
            while (isLive) {
                // 每次读多少数据要根据编码器来定！
                val len: Int = audioRecord.read(bytes, 0, bytes.size)
                if (len > 0) {
                    // 成功采集到音频数据了
                    // 对音频数据进行编码并发送（将编码后的数据push到安全队列中）
                    native_pushAudio(bytes)
                    Log.e(TAG, ":native_pushAudio ")
                }
            }
            audioRecord.stop() // 停止录音

        }
    }

    fun init() {
        native_initAudioEncoder(44100, channels)
        if (channels == 1) {
            channelConfig = AudioFormat.CHANNEL_IN_MONO
        }
        inputSamples = native_getInputSamples() * 2

        val minBufferSize =
            AudioRecord.getMinBufferSize(44100, channelConfig, AudioFormat.ENCODING_PCM_16BIT) * 2
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,  // 安卓手机的麦克风
            44100,  // 采样率
            channelConfig,  // 声道数 双声道
            AudioFormat.ENCODING_PCM_16BIT,  // 位深 16位 2字节
            inputSamples.coerceAtLeast(minBufferSize)
        )

    }

    fun stopLive() {
        isLive = false
    }

    // 初始化faac音频编码器
    private external fun native_initAudioEncoder(sampleRate: Int, numChannels: Int)

    // 获取facc编码器 样本数
    private external fun native_getInputSamples(): Int

    // 把audioRecord采集的原始数据，给C++层编码-->入队---> 发给流媒体服务器
    private external fun native_pushAudio(bytes: ByteArray)
}