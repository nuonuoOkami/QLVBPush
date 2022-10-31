package com.nuonuo.qlvbpush

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Build
import android.view.SurfaceHolder
import androidx.core.app.ComponentActivity
import androidx.core.content.ContextCompat


/**
 *
 */
class QLVBPushHelper(

    private val activity: ComponentActivity,
    cameraId: Int = Camera.CameraInfo.CAMERA_FACING_BACK,
    mWidth: Int = 1080,
    mHeight: Int = 1920,
    fps: Int = 25,
    rate: Int = 800000

) {
    private val TAG = "QLVBPushHelper"


    //是否开始推流
    private var isOpenPush = false

    //推流地址
    private var pushPath: String? = null

    private var videoHelper: VideoHelper

    private var audioHelper: AudioHelper

    var cameraHelper: CameraHelper

    init {
        //加载so 切勿忘记
        System.loadLibrary("q_push")
        //初始化音视频助手
        audioHelper = AudioHelper()
        videoHelper = VideoHelper(fps, rate, mWidth, mHeight)

        //回调监听给video
        cameraHelper = CameraHelper(activity, cameraId, mWidth, mHeight)
        //设置回调
        cameraHelper.setPreviewChangeListener(videoHelper)


    }

    /**
     * 设置SurfaceHolder 用于预览
     */
    fun setPreviewDisplay(holder: SurfaceHolder) {
        cameraHelper.setPreviewDisplay(holder)
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
        native_start_live(pushPath)
        videoHelper.startLive()
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
        pushInit()
        cameraHelper.startPreview()
    }

    /**
     * 停止推流
     */
    fun stopPush() {
        isOpenPush = false
        cameraHelper.stopPreview()
        release()
    }

    /**
     * 停止预览
     */
    fun stopPreview() {
        cameraHelper.stopPreview()
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


    /**
     * 释放
     */
    private fun release() {

    }


    //初始化
    private external fun pushInit()

    private external fun native_start_live(path: String)

}