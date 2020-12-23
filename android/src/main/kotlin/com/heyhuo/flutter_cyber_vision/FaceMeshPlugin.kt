package com.heyhuo.flutter_cyber_vision

import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry

/** FlutterCyberVisionPlugin */
class FaceMeshPlugin :
        FlutterPlugin, MethodCallHandler, ActivityAware {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel

    //    lateinit var activity: Activity
    private lateinit var activity: Activity
    private lateinit var pluginBinding: FlutterPlugin.FlutterPluginBinding

    override fun onAttachedToEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(binding.binaryMessenger, "flutter_cyber_vision")
        channel.setMethodCallHandler(this)

        pluginBinding = binding
//        binding.platformViewRegistry.registerViewFactory(
//                "faceMeshView", FaceMeshViewFactory(binding.binaryMessenger,activity)
//        )
//        Toast.makeText(activity, "dassadasd", Toast.LENGTH_LONG).show()

    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        if (call.method == "getPlatformVersion") {
            result.success("Android ${android.os.Build.VERSION.RELEASE}")
        } else {
            result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        pluginBinding.platformViewRegistry.registerViewFactory(
                "faceMeshView", FaceMeshViewFactory(pluginBinding.binaryMessenger, binding)
        )
//        Toast.makeText(binding.activity, "Dsa", Toast.LENGTH_LONG).show()
    }

    override fun onDetachedFromActivityForConfigChanges() {
        TODO("Not yet implemented")
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        TODO("Not yet implemented")
    }

    override fun onDetachedFromActivity() {
        TODO("Not yet implemented")
    }

//    private inner class CameraRequestPermissionsListener :
//            PluginRegistry.RequestPermissionsResultListener {
//        override fun onRequestPermissionsResult(requestCode: Int,
//                                                permissions: Array<out String>?,
//                                                grantResults: IntArray?): Boolean {
//            return if (requestCode != 0) false
//            else {
//                for (result in grantResults!!) {
//                    if (result == PackageManager.PERMISSION_GRANTED)
//                    //onResume()
//                        Toast.makeText(activity, "授予摄像头权限", Toast.LENGTH_LONG).show()
//                    else
//                        Toast.makeText(activity, "请授予摄像头权限", Toast.LENGTH_LONG).show()
//                }
//                true
//            }
//        }
//
//    }

}