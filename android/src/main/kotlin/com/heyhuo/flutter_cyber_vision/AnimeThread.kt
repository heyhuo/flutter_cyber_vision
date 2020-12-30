import android.R.attr.scaleX
import android.app.Activity
import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.DisplayMetrics
import android.view.SurfaceHolder
import com.heyhuo.flutter_cyber_vision.AnimeProducer
import io.flutter.plugin.common.MethodCall
import org.checkerframework.checker.units.qual.h
import kotlin.math.roundToInt
import kotlin.random.Random


//3、自定义线程，继承HandlerThread
class AnimeThread(
        //缓存视图（我们所有的图片都是绘制在我们的Hodler上面的）
        private val context: Context,
        private val activity: Activity,
        private val call: MethodCall,
        private var drawingHolder: SurfaceHolder,
        private val bitmap: Bitmap) : HandlerThread("DrawingThread"), Handler.Callback {

    //画笔
    private var paint: Paint

    private val iconBitmap: Bitmap = bitmap //我们需要绘制的图片
    private var revelver: Handler? = null //定义Handler，更新UI线程
    private var isRunning = false //线程是否在运行
    private var displayMetrics: DisplayMetrics = context.getResources().getDisplayMetrics()
    private var width = displayMetrics.widthPixels
    private var height = displayMetrics.heightPixels
    private val imageH = 256f
    private val imageW = 256f
    private val matrix = Matrix()
    private var animeProducer: AnimeProducer


    companion object {
        //4、定义线程处理，相关的参数
        private const val MSG_ADD = 101 //创建消息
    }

    init {
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        matrix.setScale(width / imageW, height / imageH)
        animeProducer = AnimeProducer(activity, call)
    }

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            MSG_ADD -> {
                //绘图
                if (!isRunning) {
                    return true //线程没有运行，直接返回
                }
                //获取加锁的画布（避免线程问题）
                val lockCanvas: Canvas = drawingHolder.lockCanvas()
                lockCanvas.drawColor(Color.WHITE) //清空画布。这里设置画布为黑色
                /* val rnd = Random.nextInt(10) / 10f
                val param = floatArrayOf(rnd, 0.5f, 0.5f)
                val bitmap: Bitmap = animeProducer.runModel(param)*/

                lockCanvas.drawBitmap(bitmap, matrix, paint) //执行绘制操作
                drawingHolder.unlockCanvasAndPost(lockCanvas) //解锁画布
                if (!bitmap.isRecycled)
                    bitmap.recycle()
            }
        }
        return false
    }

    //5、监听线程的生命周期（如：当线程退出，就不需要绘制图片了）
    override fun onLooperPrepared() {
        super.onLooperPrepared()
        //提供给我们初始化基本参数
        revelver = Handler(getLooper(), this)
        isRunning = true
        revelver!!.sendEmptyMessage(MSG_ADD) //发一个默认消息
    }

    override fun quit(): Boolean {
        //当线程退出，就不需要绘制图片了
        isRunning = false
        //当线程退出，将所有的消息给清理掉
        revelver!!.removeCallbacksAndMessages(null)
        return super.quit()
    }


}