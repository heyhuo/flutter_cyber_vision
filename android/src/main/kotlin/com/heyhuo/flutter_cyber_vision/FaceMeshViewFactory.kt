package com.heyhuo.flutter_cyber_vision

import android.content.Context
import android.widget.Toast
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

class FaceMeshViewFactory(private val binaryMessenger: BinaryMessenger,
                          private val methodCall: MethodCall,
                          private var activityBinding: ActivityPluginBinding) :
        PlatformViewFactory(StandardMessageCodec.INSTANCE) {
    override fun create(context: Context, viewId: Int, args: Any?): PlatformView? {

        val methodCallTag = methodCall.method
        Toast.makeText(activityBinding.activity, "工厂视图启动->" + methodCallTag, Toast.LENGTH_LONG).show()

        if (methodCallTag == "getFaceMeshView")
            return FaceMeshView(context, viewId, binaryMessenger, activityBinding)
        else if (methodCallTag == "getAnimeView")
            return AnimeView(context, viewId, binaryMessenger, activityBinding, methodCall)
        else return null
    }
}
