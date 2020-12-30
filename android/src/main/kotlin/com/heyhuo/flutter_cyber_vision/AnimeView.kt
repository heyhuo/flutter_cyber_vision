package com.heyhuo.flutter_cyber_vision

import AnimeThread
import android.content.Context
import android.graphics.*
import android.util.DisplayMetrics
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
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.random.Random


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
    private lateinit var animeThread: AnimeThread
    private var displayMetrics: DisplayMetrics = context.getResources().getDisplayMetrics()
    private var width = displayMetrics.widthPixels
    private var height = displayMetrics.heightPixels
    private val imageH = 256f
    private val imageW = 256f
    private val matrix = Matrix()
    val url = call.argument<String>("imgPath") //"/data/user/0/com.heyhuo.flutter_cyber_vision_example/cache/image_picker6400962099791283903.png"
//    private var mSurfaceHolder:SurfaceHolder=Hold;    // 画布


    private var animeDisplayView: SurfaceView = SurfaceView(context)


    init {
        matrix.setScale(width / imageW, height / imageH)
        morphImg = utils.getLocationBitmap(url!!)
        setupAnimePreview()
    }

    /*初始化SurfaceView*/
    private fun setupAnimePreview() {

        animeDisplayView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {

                val animeThreadPool: ThreadPoolExecutor =
                        ThreadPoolExecutor(1, 128, 10, TimeUnit.SECONDS,
                                LinkedBlockingQueue<Runnable>(256))
                val animeProducer = AnimeProducer(activity, call)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)

                for (i in 0 until 64) {
                    val runnable = Runnable {
                        try {
                            val rnd = Random.nextInt(10) / 10f
                            val param = floatArrayOf(rnd, rnd, rnd)
                            val interpreter = animeProducer.getInterpreter()
                            val bitmap: Bitmap = animeProducer.runModel(interpreter,param)
                            val lockCanvas: Canvas = holder.lockCanvas()
                            if(i%2==1)
                                lockCanvas.drawColor(Color.BLUE)
                            else lockCanvas.drawColor(Color.GRAY)
                            lockCanvas.drawBitmap(bitmap, matrix, paint) //执行绘制操作
                            holder.unlockCanvasAndPost(lockCanvas) //解锁画布
//                            if (!bitmap.isRecycled)
                            bitmap.recycle()
//                            Thread.sleep(3000)
//                            Log.e("TAG", "run : " + finali.toString() + "  当前线程：" + Thread.currentThread().name)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                    }
                    animeThreadPool.execute(runnable)
//                    animeThread = AnimeThread(context, activity, call, holder, morphImg)
                }


//                animeThread.start()

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


        return animeDisplayView

//        imageView.setImageBitmap(getLocationBitmap(url))
//        imageView.setImageBitmap(morphImg)
//        return imageView
    }

    override fun dispose() {
        TODO("Not yet implemented")
    }

}
