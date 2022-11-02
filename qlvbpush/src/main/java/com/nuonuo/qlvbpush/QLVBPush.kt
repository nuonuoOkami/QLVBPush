package com.nuonuo.qlvbpush

import android.util.Size


fun QLVBPushHelper.init(action: QLVBPushHelper.() -> Unit): QLVBPushHelper {
    action.invoke(this)
    this.init()
    return this
}

/**
 * http://www.xstrive.com/wp-content/uploads/2019/03/%E8%A7%86%E9%A2%91%E7%A0%81%E7%8E%87%E5%8F%82%E8%80%83%E5%AF%B9%E7%85%A7%E8%A1%A8.pdf
 * 标清 建议360*640  fps  15  码率400k-800k
 * 高清  540*960    fps 15  1200k
 * 超清 720*1080    fps 15  1800k

 */

enum class LVBConf {
    SD,//标清
    HD,//高清
    FHD,//超清,
}

enum class CameraID() {
    FRONT, //前
    BACK//后
}

class LVBConfValue(var size: Size, var fps: Int, var rate: Int)


fun lVBValueOf(conf: LVBConf): LVBConfValue {
    if (conf == LVBConf.HD) {
        return LVBConfValue(Size(720, 1280), 30, 80 * 1000)
    } else if (conf == LVBConf.FHD) {
        return LVBConfValue(Size(1080, 1920), 25, 40 * 1000)
    }

    return LVBConfValue(Size(480, 720), 25, 20 * 1000)
}
