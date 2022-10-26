package com.nuonuo.qlvbpush

import java.lang.annotation.Native


/**
 *视频助手
 * fps：帧率
 * bitrate 码率
 * width 宽
 * height 高
 */
class VideoHelper(private val fps: Int, private val rate: Int, width: Int, height: Int) {

    init {
        //native初始化video
        videoInit(fps, rate, width, height)
    }

    /**
     * 开始推流到native
     */
    fun push(data: ByteArray) {
        pushData(data)
    }

    //初始化video
    private external fun videoInit(fps: Int, bitrate: Int, width: Int, height: Int)

    //推送数据
    private external fun pushData(data: ByteArray)


}