package com.nuonuo.qlvbpush

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Build
import android.util.Log
import android.util.Size
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.app.ComponentActivity
import androidx.core.content.ContextCompat


/**
 * 标清 建议360*640  fps  15  码率400k-800k
 * 高清  540*960    fps 15  1200k
 * 超清 720*1080    fps 15  1800k
 */
class QLVBPushHelper(

    private val activity: ComponentActivity,
    cameraId: Int = Camera.CameraInfo.CAMERA_FACING_BACK,
    mWidth: Int = 360,
    mHeight: Int = 640,
    fps: Int = 15,
    rate: Int = 400 * 1000

) {
    private val TAG = "QLVBPushHelper"

    var isInit = false;
    var size = Size(360, 640)
    var preView: SurfaceView? = null


    //是否开始推流
    private var isPush = false

    //推流地址
    var pushPathUrl: String? = null

    //是否开始预览
    var isPreview = false

    private var videoHelper: VideoHelper

    private var audioHelper: AudioHelper

    private var cameraHelper: CameraHelper
    var mFps = 15
    var mRate = 400 * 1000;

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
     * 开始推流
     */
    fun startLive() {

        if (pushPathUrl.isNullOrEmpty()) {
            throw RuntimeException("推流地址不能为null")
        }
        isPush = true
        if (!isPreview) {
            startPreview()
        }
        native_start_live(pushPathUrl!!)
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
        if (preView == null) {
            throw RuntimeException(
                "CameraHelper Error =preView==null"
            )
        }
        cameraHelper.setPreviewDisplay(preView!!.holder)
        cameraHelper.startPreview()
        isPreview = true

    }

    /**
     * 停止推流
     */
    fun stopPush() {
        isPush = false
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

    /**
     * 初始化
     */
    fun init() {
        pushInit()
        isInit = true
    }


    //初始化
    private external fun pushInit()

    private external fun native_start_live(path: String)
    fun size(size: Size) {
        this.size = size
    }

    fun rtmpPath(path: String?) {
        this.pushPathUrl = path

    }

    fun fps(fps: Int) {
        mFps = fps
    }

    fun rate(rate: Int) {
        mRate = rate
    }

    fun preView(preView: SurfaceView) {
        this.preView = preView
    }
}