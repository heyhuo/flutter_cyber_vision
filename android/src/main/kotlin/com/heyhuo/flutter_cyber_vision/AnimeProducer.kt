package com.heyhuo.flutter_cyber_vision

import android.app.Activity
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import io.flutter.plugin.common.MethodCall
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer


class AnimeProducer(private val activity: Activity,
                    private val call: MethodCall) {

    companion object {
        private const val MODEL_PATH: String = "morpher.tflite"
        private const val THREAD_NUM: Int = 128
    }

    private lateinit var model: MappedByteBuffer
    private var options = Interpreter.Options()
    private var interpreter: Interpreter? = null
    private lateinit var imgBitmap: Bitmap
    private val utils = Utils()
    private lateinit var sourceImgBuffer: ByteBuffer
    private lateinit var morphParamBuffer: ByteBuffer
    private lateinit var outputBuffer: MappedByteBuffer
    private var imgPath: String = "/data/user/0/com.heyhuo.flutter_cyber_vision_example/cache/image_picker6400962099791283903.png" //args.argument("imgPath")
    private val ddims = intArrayOf(1, 4, 256, 256)

    init {
        initInterpreterAndImgBuffer()
    }

    public fun runModel(param: FloatArray): Bitmap {
        val inputs = setInputs(param)

        val outputs = setOutputs()

        val inferenceStartTimeNanos = SystemClock.elapsedRealtimeNanos()
        interpreter!!.runForMultipleInputsOutputs(inputs, outputs)
        val lastInferenceTimeNanos = SystemClock.elapsedRealtimeNanos() - inferenceStartTimeNanos

        Log.i(
                "anime",
                String.format("Interpreter took %.2f ms", 1.0f * lastInferenceTimeNanos / 1_000_000)
        )


        /*查看输出的bitmap*/
        val outImg = outputs[0]
        val morphImg = utils.convertArrayToBitmap(outImg as Array<Array<Array<FloatArray>>>)

        return morphImg
    }

    private fun setOutputs(): HashMap<Int, Any> {
//        val re1: ByteBuffer = ByteBuffer.allocateDirect(4 * 4 * 256 * 256)
//        re1.order(ByteOrder.nativeOrder())

        val re1: Array<Array<Array<FloatArray>>> =
                Array(1) { Array(4) { Array(256) { FloatArray(256) } } }
        val outputs = HashMap<Int, Any>()
        outputs[0] = re1
        return outputs
    }

    private fun setInputs(morphParam: FloatArray): Array<Any> {
        sourceImgBuffer.rewind()
        return arrayOf(sourceImgBuffer, morphParam)
    }


    private fun initInterpreterAndImgBuffer() {
        model = utils.loadModelFile(activity, MODEL_PATH)
        /*!!不能用NNAPI加速，数值不对
        options.setUseNNAPI(true)*/

        /*！！暂时不能用，初始化使用 GPU 代理的解释器
        val delegate = GpuDelegate()
        options.addDelegate(delegate)*/

        options.setNumThreads(THREAD_NUM)
        interpreter = Interpreter(model, options)

        imgBitmap = utils.getLocationBitmap(imgPath)
        sourceImgBuffer = utils.bitmapToBuffer(imgBitmap)

        /*查看输入的bitmap*/
        //val im = utils.byteToBitmap(sourceImgBuffer)
    }


}