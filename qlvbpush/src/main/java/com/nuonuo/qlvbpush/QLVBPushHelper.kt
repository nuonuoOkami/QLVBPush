package com.nuonuo.qlvbpush

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.util.Size
import android.view.Surface
import androidx.camera.core.*
import androidx.camera.view.PreviewView
import androidx.core.app.ComponentActivity
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors

class QLVBPushHelper(

    private val activity: ComponentActivity,
    private val previewView: PreviewView,
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    private var width: Int = 1080,
    private var height: Int = 1920
) {
    private val TAG = "QLVBPushHelper"

    //是否开始推流
    private var isOpenPush = true

    //推流地址
    private var pushPath: String? = null


    //相机帮助类 支持 变焦/切换摄像头/开关闪光灯
    var cameraHelper: CameraHelper?

    init {
        val executorService = Executors.newSingleThreadExecutor()
        val mImageAnalysis =
            ImageAnalysis.Builder()
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .setTargetResolution(Size(width, height)) // 图片的建议尺寸
                .setOutputImageRotationEnabled(true) // 是否旋转分析器中得到的图片
                .setTargetRotation(Surface.ROTATION_0) // 允许旋转后 得到图片的旋转设置
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build().apply {
                    setAnalyzer(
                        executorService
                    ) {
                        if (isOpenPush) {
                            it.close()
                        }

                    }
                }

        cameraHelper =
            CameraHelper(activity, previewView, cameraSelector, mImageAnalysis)
    }

    /**
     * 开始推流
     */
    fun startPush(pushPath: String?) {
        if (pushPath.isNullOrEmpty()) {
            throw RuntimeException("推流地址不能为null")
        }
        this.pushPath = pushPath;
        isOpenPush = true
    }

    /**
     * 开始预览
     */
    fun startPreview() {
        if (!allPermissionsGranted()) {
            throw RuntimeException(
                "CameraHelper Error \n" +
                        "请检查\n" +
                        "Manifest.permission.CAMERA,\n" +
                        "Manifest.permission.RECORD_AUDIO\n" +
                        "WRITE_EXTERNAL_STORAGE" +
                        "\n权限是否申请"
            )

        }
        cameraHelper?.startPreview()

    }

    /**
     * 停止推流
     */
    fun stopPush() {
        isOpenPush = false
        cameraHelper?.stopPreview()
    }

    /**
     * 停止预览
     */
    fun stopPreview() {
        cameraHelper?.stopPreview()
    }


    /**
     * 判断是否拥有全部权限
     * @return Boolean
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            activity, it
        ) == PackageManager.PERMISSION_GRANTED
    }


    companion object {
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

}