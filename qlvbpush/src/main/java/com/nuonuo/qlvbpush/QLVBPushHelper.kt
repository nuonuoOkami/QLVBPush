package com.nuonuo.qlvbpush

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Build
import android.util.Size
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
    private var cameraId: CameraID = CameraID.FRONT,
    var conf: LVBConf? = null
) {


    //是否初始化完成
    var isInit = false
    var mSize = Size(360, 640)

    //预览控件
    private var preView: SurfaceView? = null


    //是否开始推流
    private var isPush = false

    //推流地址
    private var pushPathUrl: String? = null

    //是否开始预览
    private var isPreview = false


    private lateinit var videoHelper: VideoHelper

    private lateinit var audioHelper: AudioHelper

    private lateinit var cameraHelper: CameraHelper

    //帧率
    private var mFps = 15

    //码率
    private var mRate = 400 * 1000


    init {
        //加载so 切勿忘记
        System.loadLibrary("q_push")
    }


    /**
     * 开始推流
     */
    fun startLive() {
        checkPermissions()
        if (pushPathUrl.isNullOrEmpty()) {
            throw RuntimeException("推流地址不能为null")
        }
        isPush = true

        if (!isInit) {
            init()
        }
        if (!isPreview) {
            startPreview()
        }
        native_start_live(pushPathUrl!!)
        videoHelper.startLive()
        audioHelper.startLive();
    }


    /**
     * 开始预览
     */
    fun startPreview() {
        checkPermissions()
        cameraHelper.setPreviewDisplay(preView!!.holder)
        cameraHelper.startPreview()
        isPreview = true

    }

    /**
     * 坚持权限和预览view
     */
    private fun checkPermissions() {
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

        //如果配置不为null 那么优先基础配置
        if (conf != null) {
            val confValue = lVBValueOf(conf!!)
            mFps = confValue.fps
            mRate = confValue.rate
            mSize = confValue.size
        }




        videoHelper = VideoHelper(mFps,mRate)

        var cId = Camera.CameraInfo.CAMERA_FACING_FRONT
        if (cameraId == CameraID.BACK) {
            cId = Camera.CameraInfo.CAMERA_FACING_BACK
        }
        //回调监听给video
        cameraHelper = CameraHelper(activity, cId, mSize.width, mSize.height)
        //设置回调
        cameraHelper.setPreviewChangeListener(videoHelper)
        audioHelper = AudioHelper()

        pushInit()
       audioHelper.init()
        //初始化音视频助手

        isInit = true
    }


    //初始化
    private external fun pushInit()

    //开播
    private external fun native_start_live(path: String)
    fun size(size: Size) {
        this.mSize = size
    }

    //设置推流地址
    fun rtmpPath(path: String?) {
        this.pushPathUrl = path

    }

    //设置帧率
    fun fps(fps: Int) {
        mFps = fps
    }

    //设置码率
    fun rate(rate: Int) {
        mRate = rate
    }

    //预览view
    fun preView(preView: SurfaceView) {
        this.preView = preView
    }

    //设置摄像头
    fun cameraId(cameraId: CameraID) {
        this.cameraId = cameraId
    }


}