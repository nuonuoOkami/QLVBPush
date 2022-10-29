package com.nuonuo.qlvbpush

import android.graphics.ImageFormat.NV21
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.hardware.Camera.PreviewCallback
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import androidx.core.app.ComponentActivity
import kotlin.math.abs

/**
 * 相机辅助类
 * 标清 Standard Definition：640 x 480p

高清 High Definition：1024 x 720p

全高清 Full High Definition：1920 x 1080p

超高清 Ultra High-Definition：3840 x 2160（4K）、7680 x 4320（8K）
 */
class CameraHelper(
    private var mActivity: ComponentActivity,
    var mCameraId: Int = CameraInfo.CAMERA_FACING_BACK,
    private var mWidth: Int,
    private var mHeight: Int

) : PreviewCallback, SurfaceHolder.Callback {
    private val TAG = "CameraHelper"
    private lateinit var mCamera: Camera

    //相机回调数据
    private var buffer: ByteArray = ByteArray((mWidth * mHeight) * 3 / 2)
    private var mSurfaceHolder // SurfaceView的帮助类
            : SurfaceHolder? = null
    private var previewChangeListener: PreviewChangeListener? = null

    /**
     * 停止预览
     */

    fun stopPreview() {
        //预览数据回调接口
        mCamera.setPreviewCallback(null)
        //停止预览
        mCamera.stopPreview()
        //释放摄像头
        mCamera.release()
    }

    /**
     * 切换摄像头
     */
    fun switchCamera() {
        stopPreview()
        mCameraId = if (mCameraId == CameraInfo.CAMERA_FACING_BACK) {
            CameraInfo.CAMERA_FACING_FRONT
        } else {
            CameraInfo.CAMERA_FACING_BACK
        }
        startPreview()

    }

    /**
     * 开始预览
     */
    fun startPreview() {


        //获得camera对象
        mCamera = Camera.open(mCameraId)
        val params = mCamera.parameters
        params?.previewFormat = NV21
        setPreviewOrientation()
        setPreviewSize(params)
        mCamera.parameters = params

        mCamera.addCallbackBuffer(buffer)
        mCamera.setPreviewCallbackWithBuffer(this)
        mCamera.setPreviewDisplay(mSurfaceHolder)
        previewChangeListener?.onChanged(mWidth, mHeight)
        //开启预览
        mCamera.startPreview()
    }

    /**
     * 变焦增加
     */
    fun zoomAdd() {

    }

    /**
     * 变焦减少
     */
    fun zoomDown() {

    }

    /**
     * 开关灯
     * @param enable Boolean
     */
    fun enableTorch(enable: Boolean) {

    }

    /**
     * 是否支持闪光灯
     * @return Boolean
     */
    fun isSupportTorch(): Boolean {

        return false
    }

    /**
     * 当前变焦值
     */
    fun zoomValue(): Float {
        return 1.0f

    }

    /**
     * 旋转画面角度（因为默认预览是歪的，所以就需要旋转画面角度）
     *
     */
    private fun setPreviewOrientation() {
        val info = CameraInfo()
        Camera.getCameraInfo(mCameraId, info)

        var degrees = 0
        when (mActivity.windowManager.defaultDisplay.rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }
        var result: Int
        if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360
            result = (360 - result) % 360 // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360
        }
        // 设置角度, 参考源码注释
        mCamera.setDisplayOrientation(result)
    }

    private fun setPreviewSize(parameters: Camera.Parameters?) {
        if (parameters == null) return
        // 获取摄像头支持的宽、高
        val supportedPreviewSizes = parameters.supportedPreviewSizes
        var size = supportedPreviewSizes[0]
        Log.d(TAG, "Camera支持: " + size.width + "x" + size.height)
        // 选择一个与设置的差距最小的支持分辨率
        var m: Int = abs(size.height * size.width - mWidth * mHeight)
        supportedPreviewSizes.removeAt(0)
        val iterator: Iterator<Camera.Size> = supportedPreviewSizes.iterator()
        // 遍历
        while (iterator.hasNext()) {
            val next = iterator.next()
            Log.d(TAG, "支持 " + next.width + "x" + next.height)
            val n: Int = abs(next.height * next.width - mWidth * mHeight)
            if (n < m) {
                m = n
                size = next
            }
        }
        mWidth = size.width
        mHeight = size.height
        parameters.setPreviewSize(mWidth, mHeight)
        Log.d(TAG, "预览分辨率 width:" + size.width + " height:" + size.height)
    }

    override fun onPreviewFrame(data: ByteArray, camera: Camera) {

    }


    fun setPreviewDisplay(surfaceHolder: SurfaceHolder) {
        mSurfaceHolder = surfaceHolder
        mSurfaceHolder?.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {

    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // 释放摄像头
        stopPreview()
        // 开启摄像头
        // 开启摄像头
        startPreview()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopPreview()
    }

    fun setPreviewChangeListener(listener: PreviewChangeListener) {
        previewChangeListener = listener
    }

    interface PreviewChangeListener : PreviewCallback {
        fun onChanged(width: Int, height: Int)
    }

}