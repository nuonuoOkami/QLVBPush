package com.nuonuo.qlvbpush

import android.os.Bundle
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import com.nuonuo.qlvbpush.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val TAG = "MainActivity"

    //https://developer.android.com/codelabs/camerax-getting-started?hl=zh-cn#1
    //https://juejin.cn/post/7096313126588678157
    //https://www.cnblogs.com/rustfisher/p/15700757.html
    //预览界面支持拖动和属性设置 https://zhuanlan.zhihu.com/p/293095630


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val helper = QLVBPushHelper(this).init {
            size(Size(480, 720))
            rtmpPath("rtmp://121.40.103.31:1935/live/99")
            fps(25)
            rate(2 * 1000)
            preView(binding.surface)
            cameraId(CameraID.BACK)
        }

//        //第二种
//        var  helper=QLVBPushHelper(this, conf = LVBConf.STANDARD)
//        helper.rtmpPath("rtmp://xx.xxx.xxx.xx:xxxx/xx/xx")
//        helper.preView(binding.surface)
        //  helper.init()
        binding.btnPre.setOnClickListener {
            //预览
            helper.startPreview()
//            //切换摄像头
//            helper.switchCamera()
//            //关闭 释放直播
//            helper.stop();
        }
        binding.btnStartPush.setOnClickListener {
            //推流
            helper.startLive()
            binding.btnStartPush.text = "推流ing"

        }
    }
}







