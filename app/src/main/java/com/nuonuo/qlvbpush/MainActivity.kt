package com.nuonuo.qlvbpush

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.Surface
import android.view.Surface.ROTATION_0
import android.view.Surface.ROTATION_180
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.impl.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.nuonuo.qlvbpush.databinding.ActivityMainBinding
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import kotlin.math.log


class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    private val TAG = "MainActivity"

    //https://developer.android.com/codelabs/camerax-getting-started?hl=zh-cn#1
    //https://juejin.cn/post/7096313126588678157
    //https://www.cnblogs.com/rustfisher/p/15700757.html
    private var imageCapture: ImageCapture? = null

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null

    private lateinit var cameraExecutor: ExecutorService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        viewBinding.btnStart.setOnClickListener {
            startCamera()
            if (allPermissionsGranted()) {
//                startCamera()
            } else {
                ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
                )
            }

        }
    }

    private fun startCamera() {

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({


            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                val preview = Preview.Builder()

                    .build().also {
                        it.setSurfaceProvider {
                            viewBinding.viewFinder.surfaceProvider
                        }
                    }


                val mImageAnalysis =
                    ImageAnalysis.Builder()
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .setTargetResolution(Size(720, 1280)) // 图片的建议尺寸
                        .setOutputImageRotationEnabled(true) // 是否旋转分析器中得到的图片
                        .setTargetRotation(Surface.ROTATION_0) // 允许旋转后 得到图片的旋转设置
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build().apply {
                            setAnalyzer({  },
                                { Log.e(TAG, "analyze: " ) });
                        }
                val group = UseCaseGroup.Builder().addUseCase(
                    preview
                ).addUseCase(mImageAnalysis)
                    .build()


                cameraProvider.bindToLifecycle(this, cameraSelector, group)

                Log.e(TAG, "startCamera: " )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
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

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }


}