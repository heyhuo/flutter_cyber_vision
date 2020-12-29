package com.heyhuo.flutter_cyber_vision

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.os.Handler
import android.os.Looper
import android.util.Size
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.mediapipe.components.*
import com.google.mediapipe.framework.AndroidAssetUtil
import com.google.mediapipe.glutil.EglManager
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.*
import io.flutter.plugin.platform.PlatformView

class FaceMeshView(context: Context?, private val viewId: Int?, binaryMessenger: BinaryMessenger,
                   private var activityBinding: ActivityPluginBinding)
    : PlatformView, MethodChannel.MethodCallHandler {

    companion object {
        private const val TAG = "FaceMeshPlugin"
        private const val NAMESPACE = "plugins.heyhuo.com/face_mesh_plugin"
        private const val BINARY_GRAPH_NAME = "mobile_gpu.binarypb"
        private const val INPUT_VIDEO_STREAM_NAME = "input_video"
        private const val OUTPUT_VIDEO_STREAM_NAME = "output_video"
        private const val OUTPUT_HAND_PRESENCE_STREAM_NAME = "hand_presence"
        private const val OUTPUT_LANDMARKS_STREAM_NAME = "hand_landmarks"
        private val CAMERA_FACING = CameraHelper.CameraFacing.FRONT
        private const val FLIP_FRAMES_VERTICALLY = true

        init { // Load all native libraries needed by the app.
            System.loadLibrary("mediapipe_jni")
            System.loadLibrary("opencv_java3")
        }
    }

    val textView: TextView = TextView(context)
    var txt: String = "测试成功"

    private val activity:Activity = activityBinding.activity
    private val methodChannel: MethodChannel = MethodChannel(binaryMessenger, "$NAMESPACE/$viewId")
    private val eventChannel: EventChannel = EventChannel(binaryMessenger, "$NAMESPACE/$viewId/landmarks")
    private var eventSink: EventChannel.EventSink? = null
    private val uiThreadHandler: Handler = Handler(Looper.getMainLooper())
    private var previewFrameTexture: SurfaceTexture? = null
    private var previewDisplayView: SurfaceView = SurfaceView(context)
    private var eglManager: EglManager = EglManager(null)
    private var processor: FrameProcessor = FrameProcessor(
            activity,
            eglManager.nativeContext,
            BINARY_GRAPH_NAME,
            INPUT_VIDEO_STREAM_NAME,
            OUTPUT_VIDEO_STREAM_NAME)
    private var converter: ExternalTextureConverter? = null
    private var cameraHelper: CameraXPreviewHelper? = null

    init {
        activityBinding.addRequestPermissionsResultListener(
                CameraRequestPermissionsListener()
        )
        AndroidAssetUtil.initializeNativeAssetManager(activity)
        setupPreviewDisplayView()
        setupProcess()
//        onResume()
        PermissionHelper.checkAndRequestCameraPermissions(activity)
        if (PermissionHelper.cameraPermissionsGranted(activity)) onResume()

    }

    private fun onResume() {
        converter = ExternalTextureConverter(eglManager.context)
        converter!!.setFlipY(FLIP_FRAMES_VERTICALLY)
        converter!!.setConsumer(processor)
        if (PermissionHelper.cameraPermissionsGranted(activity)) {
            startCamera()
        }

    }

    private fun startCamera() {
        cameraHelper = CameraXPreviewHelper()
        cameraHelper!!.setOnCameraStartedListener { surfaceTexture: SurfaceTexture? ->
            previewFrameTexture = surfaceTexture
            // Make the display view visible to start showing the preview. This triggers the
            // SurfaceHolder.Callback added to (the holder of) previewDisplayView.
            previewDisplayView.visibility = View.VISIBLE
        }
        cameraHelper!!.startCamera(activity, CAMERA_FACING,  /*surfaceTexture=*/null)
    }

    private fun setupProcess() {
        processor.videoSurfaceOutput.setFlipY(FLIP_FRAMES_VERTICALLY)
//        processor.addPacketCallback(
//
//        )
    }

    private fun setupPreviewDisplayView() {
        previewDisplayView.visibility = View.GONE
        previewDisplayView.holder.addCallback(
                object :SurfaceHolder.Callback{
                    override fun surfaceCreated(holder: SurfaceHolder?) {
                        processor.videoSurfaceOutput.setSurface(holder?.surface)
                    }

                    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
                        val viewSize = Size(width, height)
                        val displaySize = cameraHelper!!.computeDisplaySizeFromViewSize(viewSize)
                        val isCameraRotated = cameraHelper!!.isCameraRotated

                        converter!!.setSurfaceTextureAndAttachToGLContext(
                                previewFrameTexture,
                                if (isCameraRotated) displaySize.height else displaySize.width,
                                if (isCameraRotated) displaySize.width else displaySize.height)
                    }

                    override fun surfaceDestroyed(holder: SurfaceHolder?) {
                        processor.videoSurfaceOutput.setSurface(null)
                    }

                }
        )
    }

    private inner class CameraRequestPermissionsListener :
            PluginRegistry.RequestPermissionsResultListener {
        override fun onRequestPermissionsResult(requestCode: Int,
                                                permissions: Array<out String>?,
                                                grantResults: IntArray?): Boolean {
            return if (requestCode != 0) false
            else {
                for (result in grantResults!!) {
                    if (result == PackageManager.PERMISSION_GRANTED) onResume()
                    else Toast.makeText(activity, "请授予摄像头权限", Toast.LENGTH_LONG).show()
                }
                true
            }
        }

    }

    override fun getView(): SurfaceView? {
//        textView.setText(txt)
        return previewDisplayView
    }

    override fun dispose() {
        TODO("Not yet implemented")
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        TODO("Not yet implemented")
    }




}
