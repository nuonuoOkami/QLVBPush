package com.nuonuo.qlvbpush

import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ComponentActivity
import androidx.core.content.ContextCompat

/**
 * 相机辅助类
 */
class CameraHelper(
    private val activity: ComponentActivity,
    private val previewView: PreviewView,
    private var cameraSelector: CameraSelector,
    private val mImageAnalysis: UseCase
) {
    private val TAG = "CameraHelper"

    //xxx 暂时没想好叫什么
    private lateinit var cameraProvider: ProcessCameraProvider

    //相机
    private lateinit var camera: Camera


    //变焦值
    private var zoomValue = 0f;

    private lateinit var useCaseGroup: UseCaseGroup
    private lateinit var preview: Preview

    init {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            preview = Preview.Builder().build().apply {
                this.setSurfaceProvider(previewView.surfaceProvider)
            }
            useCaseGroup =
                UseCaseGroup.Builder().addUseCase(preview).addUseCase(mImageAnalysis).build()

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(activity, cameraSelector, useCaseGroup)
            } catch (ex: Exception) {
                Log.e(TAG, "QLVBPushHelper bindToLifecycle :Error$ex ")
            }


        }, ContextCompat.getMainExecutor(activity))
    }

    /**
     * 停止预览
     */

    fun stopPreview() {
        cameraProvider.unbindAll()
    }

    /**
     * 切换摄像头
     */
    fun switchCamera() {
        stopPreview()
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        startPreview()

    }

    /**
     * 开始预览
     */
    fun startPreview() {
        try {
            stopPreview()
            camera = cameraProvider.bindToLifecycle(activity, cameraSelector, useCaseGroup)
        } catch (ex: Exception) {
            Log.e(TAG, "$TAG startPreview bindToLifecycle :Error$ex ")
        }
    }

    /**
     * 变焦增加
     */
    fun zoomAdd() {
        val cameraControl = camera.cameraControl;
        val zoomState = camera.cameraInfo.zoomState.value

        if (zoomState != null) {
            var linearZoom = zoomState.linearZoom;
            Log.e(TAG, "linearZoom---> : $linearZoom")
            if (linearZoom <= 1f) {
                linearZoom += 0.1f;
                if (linearZoom >= 1f) {
                    linearZoom = 1f
                }
            }

            cameraControl.setLinearZoom(linearZoom)
        }
    }

    /**
     * 变焦减少
     */
    fun zoomDown() {
        val cameraControl = camera.cameraControl;
        val zoomState = camera.cameraInfo.zoomState.value

        if (zoomState != null) {
            zoomValue = zoomState.linearZoom;
            if (zoomValue >= 0f) {
                zoomValue -= 0.1f;
                if (zoomValue <= 0f) {
                    zoomValue = 0f
                }
            }

        }
        cameraControl.setLinearZoom(zoomValue)
    }

    /**
     * 开关灯
     * @param enable Boolean
     */
    fun enableTorch(enable: Boolean) {
        val cameraControl = camera.cameraControl
        if (isSupportTorch()) {
            cameraControl.enableTorch(enable)
        } else {
            Log.e(TAG, "enableTorch:  不支持闪光灯")
        }
    }

    /**
     * 是否支持闪光灯
     * @return Boolean
     */
    fun isSupportTorch(): Boolean {
        return camera.cameraInfo.hasFlashUnit()
    }

    /**
     * 当前变焦值
     */
    fun zoomValue(): Float? {
        return camera.cameraInfo.zoomState.value?.linearZoom
    }
}