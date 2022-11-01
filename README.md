# QLVBPush

![Image text](https://github.com/nuonuoOkami/images/blob/main/qlvb_push_icon.png)

### 依赖库

    rtmp
    faac
    x264

### 支持

### native占用

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

### 异常处理

    暂未开放

### Change Log

#### 1.0

     暂未开放

### 联系我

    QQ:1085214089
    
    
    

