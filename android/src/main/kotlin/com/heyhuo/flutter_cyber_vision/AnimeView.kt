package com.heyhuo.flutter_cyber_vision

import AnimeThread
import android.content.Context
import android.graphics.Bitmap
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.platform.PlatformView
import org.tensorflow.lite.Interpreter
import java.nio.MappedByteBuffer


class AnimeView(private var context: Context, private val viewId: Int, binaryMessenger: BinaryMessenger,
                private val activityBinding: ActivityPluginBinding,
                private val call: MethodCall) : PlatformView {

    private val activity = activityBinding.activity
    private var interpreter: Interpreter? = null
    private var textView: TextView = TextView(context)
    private var imageView: ImageView = ImageView(context)
    private lateinit var morphImg: Bitmap
    private lateinit var inputBufssfer: MappedByteBuffer
    private lateinit var outputBuffer: MappedByteBuffer
    private val utils = Utils()
    private lateinit var animeThread:AnimeThread
    val url = "/data/user/0/com.heyhuo.flutter_cyber_vision_example/cache/image_picker6400962099791283903.png"
//    private var mSurfaceHolder:SurfaceHolder=Hold;    // 画布


    private var animeDisplayView: SurfaceView = SurfaceView(context)


//    init {
//        setupAnimePreview()
//    }

    /*初始化SurfaceView*/
    private fun setupAnimePreview() {
//        animeDisplayView.visibility = View.GONE
        /*animeDisplayView.holder.addCallback(
                object : SurfaceHolder.Callback {
                    override fun surfaceCreated(holder: SurfaceHolder?) {
                        val paint = Paint() //画笔
                        paint.isAntiAlias = true;//设置是否抗锯齿
                        paint.style = Paint.Style.STROKE;//设置画笔风格
                        val bitmap: Bitmap = utils.getLocationBitmap(url)
                        val canvas: Canvas = holder!!.lockCanvas() // 先锁定当前surfaceView的画布

                        canvas.drawBitmap(bitmap,0f,0f,paint) //执行绘制操作

                        holder.unlockCanvasAndPost(canvas) // 解除锁定并显示在界面上


                    }

                    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
                        TODO("Not yet implemented")
                    }

                    override fun surfaceDestroyed(holder: SurfaceHolder?) {
                        TODO("Not yet implemented")
                    }

                }
        )*/

        animeDisplayView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                val bitmap = utils.getLocationBitmap(url)
                animeThread = AnimeThread(
                        context, activity, call, holder, bitmap
                )
                animeThread.start()
//                handler = WeakHandler(animeThread.getLooper(), this)
                /*   val paint = Paint() //画笔
                paint.isAntiAlias = true //设置是否抗锯齿
                paint.style = Paint.Style.FILL //设置画笔风格
                // 先锁定当前surfaceView的画布
                val canvas = holder.lockCanvas()
                canvas.drawColor(Color.WHITE)
                canvas.drawBitmap(utils.getLocationBitmap(url),)


               for (i in 0 until 5) {
                    val param = floatArrayOf(0.3f+(i.toFloat()/5f), 0.5f, 0.5f)
                    val bitmap: Bitmap =
                            AnimeProducer(activity, call).runModel(param)
                    canvas.drawBitmap(bitmap, 0f, 0f, paint) //执行绘制操作
                }

                // 解除锁定并显示在界面上
                holder.unlockCanvasAndPost(canvas)*/
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                animeThread.quit()

//                animeThread.destroy()
            }
        })
    }

    override fun getView(): View {

        setupAnimePreview()


        return animeDisplayView

//        imageView.setImageBitmap(getLocationBitmap(url))
//        imageView.setImageBitmap(morphImg)
//        return imageView
    }

    override fun dispose() {
        TODO("Not yet implemented")
    }

}
