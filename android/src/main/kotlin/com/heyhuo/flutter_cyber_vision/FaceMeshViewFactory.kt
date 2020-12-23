package com.heyhuo.flutter_cyber_vision

import android.app.Activity
import android.content.Context
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

class FaceMeshViewFactory(private val binaryMessenger: BinaryMessenger,
                          private var activityBinding: ActivityPluginBinding) :
        PlatformViewFactory(StandardMessageCodec.INSTANCE) {
    override fun create(context: Context?, viewId: Int, args: Any?): PlatformView {
        return FaceMeshView(context, viewId, binaryMessenger, activityBinding)
    }
}
