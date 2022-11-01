package com.nuonuo.qlvbpush

import android.hardware.Camera
import android.util.Log


/**
 *视频助手
 * fps：帧率
 * bitrate 码率
 * width 宽
 * height 高
 */
class VideoHelper(private val fps: Int, private val rate: Int) :
    CameraHelper.PreviewChangeListener {
    private val TAG = "VideoHelper"

    //是否在直播
    private var isLive = false

    //初始化video
    private external fun native_Video_Init(fps: Int, bitrate: Int, width: Int, height: Int)

    //推送数据
    private external fun native_Video_Push(byte: ByteArray)


    override fun onChanged(width: Int, height: Int) {
        native_Video_Init(fps, rate, width, height)
    }

    override fun onPreviewFrame(data: ByteArray, camera: Camera) {
        if (isLive) {
            native_Video_Push(data)
        }
    }

    fun startLive() {
        isLive = true
    }

    fun stopLive() {
        isLive = false
    }


}