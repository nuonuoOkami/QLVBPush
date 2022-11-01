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
            size(Size(360, 640))
            rtmpPath("rtmp://121.40.103.31:1935/live/999533")
            fps(15)
            rate(400 * 1000)
            preView(binding.surface)
            cameraId(CameraID.FRONT)
        }

//        //第二种
//        var  helper=QLVBPushHelper(this, conf = LVBConf.STANDARD)
//        helper.rtmpPath("rtmp://xx.xxx.xxx.xx:xxxx/xx/xx")
//        helper.preView(binding.surface)
        //  helper.init()
        binding.btnPre.setOnClickListener {
            //预览
            helper.startPreview()
        }
        binding.btnStartPush.setOnClickListener {
            //推流
            helper.startLive()
            binding.btnStartPush.text = "推流ing"

        }
    }
}







