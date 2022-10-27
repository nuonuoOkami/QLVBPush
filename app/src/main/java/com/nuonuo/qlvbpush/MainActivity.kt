package com.nuonuo.qlvbpush

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import com.nuonuo.qlvbpush.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val TAG = "MainActivity"

    //https://developer.android.com/codelabs/camerax-getting-started?hl=zh-cn#1
    //https://juejin.cn/post/7096313126588678157
    //https://www.cnblogs.com/rustfisher/p/15700757.html
    //预览界面支持拖动和属性设置 https://zhuanlan.zhihu.com/p/293095630

    private lateinit var helper: QLVBPushHelper
    private var dengState = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnStart.setOnClickListener {

            helper = QLVBPushHelper(this, binding.viewFinder)
            helper.startPreview()
        }
        binding.btnStop.setOnClickListener {
            helper.stopPreview()
        }
        binding.btnSwitch.setOnClickListener {
            helper.cameraHelper?.switchCamera()
        }

        binding.btnChangeAdd.setOnClickListener {
            helper.cameraHelper?.zoomAdd();
        }
        binding.btnChangeDel.setOnClickListener {
            helper.cameraHelper?.zoomDown();
        }
        binding.btnDeng.setOnClickListener {
            dengState = !dengState
            helper.cameraHelper?.enableTorch(dengState);


        }
    }
}







