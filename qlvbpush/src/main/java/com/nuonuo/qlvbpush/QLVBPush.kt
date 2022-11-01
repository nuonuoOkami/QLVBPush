package com.nuonuo.qlvbpush

import android.util.Size


fun QLVBPushHelper.init(action: QLVBPushHelper.() -> Unit): QLVBPushHelper {
    action.invoke(this)
    this.init()
    return this
}

/**
 * 标清 建议360*640  fps  15  码率400k-800k
 * 高清  540*960    fps 15  1200k
 * 超清 720*1080    fps 15  1800k
 */

enum class LVBConf {
    STANDARD,//标清
    HD,//高清
    ULTRA_CLEAR//超清
}

enum class CameraID() {
    FRONT, //前
    BACK//后
}

class LVBConfValue(var size: Size, var fps: Int, var rate: Int)


fun LVBValueOf(conf: LVBConf): LVBConfValue {
    if (conf == LVBConf.HD) {
        return LVBConfValue(Size(540, 960), 15, 1200 * 1000)
    } else if (conf == LVBConf.ULTRA_CLEAR) {
        return LVBConfValue(Size(720, 1080), 15, 1800 * 1000)
    }

    return LVBConfValue(Size(360, 640), 15, 400 * 1000)
}
