# QLVBPush

![Image text](https://github.com/nuonuoOkami/images/blob/main/qlvb_push_icon.png)

### 依赖库

    rtmp
    faac
    x264

### 支持
    rtmp推流直播
    提供SD,HD,FHD三种基础配置
### native占用
![Image_text](https://github.com/nuonuoOkami/images/blob/main/push_native.png)

### 依赖使用

    暂未开放

### 使用方式

    //建议初始化方式
        val helper = QLVBPushHelper(this).init {
            size(Size(360, 640))
            rtmpPath("rtmp://xx.xx.xx.31:xx/xxx/xxx")
            fps(15)
            rate(400 * 1000)
            preView(binding.surface)
            cameraId(CameraID.FRONT)

        }
    //普通版本  STANDARD  STANDARD,//标清 HD,//高清   ULTRA_CLEAR//超清
    var  helper=QLVBPushHelper(this, conf = LVBConf.STANDARD)
    helper.rtmpPath("3232")
    helper.preView(binding.surface)
    //预览
    helper.startPreview()
    //推流
    helper.startLive()
    helper.startPreview()
    //切换摄像头
    helper.switchCamera()
    //关闭 释放直播
    helper.stop();

### 异常处理

    暂未开放

### Change Log

#### 1.0
    支持切换摄像头
    提供两者启动方式
    最基础的推拉流sdk 
    测试延迟基本在1.5-3s之间 和服务器有关系

### 联系我

    QQ:1085214089
    
    
    

