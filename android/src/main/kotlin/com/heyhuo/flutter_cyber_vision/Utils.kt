package com.heyhuo.flutter_cyber_vision

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.widget.Toast
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.pow
import kotlin.math.roundToInt

class Utils {
    fun showToast(text: String, activity: Activity) {
        Toast.makeText(activity, text, Toast.LENGTH_LONG).show()
    }

    fun loadModelFile(activity: Activity, MODEL_PATH: String): MappedByteBuffer {
        val fileDescriptor = activity.assets.openFd(MODEL_PATH)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        val retFile = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        fileDescriptor.close()
        return retFile
    }

    fun convertArrayToBitmap(
            imageArray: Array<Array<Array<FloatArray>>>,
            imageWidth: Int = 256,
            imageHeight: Int = 256
    ): Bitmap {
        val conf = Bitmap.Config.ARGB_8888 // see other conf types
        val styledImage = Bitmap.createBitmap(imageWidth, imageHeight, conf)

        for (x in 0 until 256) {
            for (y in 0 until 256) {
                val color = Color.argb(
                                ((imageArray[0][3][x][y] + 1) * 0.5f * 255f).roundToInt(),
                                (linearToSrgb((imageArray[0][0][x][y] + 1) * 0.5f) * 255f).roundToInt(),
                                (linearToSrgb((imageArray[0][1][x][y] + 1) * 0.5f) * 255f).roundToInt(),
                                (linearToSrgb((imageArray[0][2][x][y] + 1) * 0.5f) * 255f).roundToInt()
                        )
                // this y, x is in the correct order!!!
                styledImage.setPixel(y, x, color)
            }
        }
        return styledImage
    }

    fun bitmapToBuffer(bitmap: Bitmap): ByteBuffer {

        val width = bitmap.width
        val height = bitmap.height

        val len = width * height
        val pixels = IntArray(len)

        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val rgba = FloatArray(len * 4)
        val inputBuffer = ByteBuffer.allocateDirect(1 * 4 * 256 * 256 * 4)
        inputBuffer.order(ByteOrder.nativeOrder())
        inputBuffer.rewind()
        for (i in 0 until len) {
            val color = pixels[i]
            val R = srgbToLinear(color shr 16 and 0xFF)
            val G = srgbToLinear(color shr 8 and 0xFF)
            val B = srgbToLinear(color and 0xFF)
            val A = ((color shr 24) / 255f) * 2f - 1f
//            inputBuffer.putFloat(R)
//            inputBuffer.putFloat(G)
//            inputBuffer.putFloat(B)
//            inputBuffer.putFloat(A)
            rgba[i] = R
            rgba[len + i] = G
            rgba[len * 2 + i] = B
            rgba[len * 3 + i] = A
        }
        for (i in 0 until 4 * len) {
            inputBuffer.putFloat(rgba[i])
        }

        inputBuffer.rewind()
        return inputBuffer
    }

    private fun srgbToLinear(pixel: Int): Float {

        var p = pixel / 255f

        if (p < 0f) p = 0f
        else if (p > 1f) p = 1f

        if (p <= 0.04045f) p /= 12.92f
        else p = Math.pow(((p + 0.055f) / 1.055), 2.4).toFloat()
        p = p * 2f - 1f

        return p
    }


    fun byteToBitmap(
            imageByte: ByteBuffer,
            width: Int = 256,
            height: Int = 256
    ): Bitmap {
        imageByte.rewind()
        val conf = Bitmap.Config.ARGB_8888 // see other conf types
        val styledImage = Bitmap.createBitmap(width, height, conf)
        val len = width * height
        val pixels = IntArray(len)
        var idx = 0
        var k = 0
        for (i in 0 until len) {
            val r = imageByte.getFloat((idx++) * 4)
            val red: Int = (linearToSrgb((r + 1f) * 0.5f) * 255f).roundToInt()

            val g = imageByte.getFloat((idx++) * 4)
            val green: Int = (linearToSrgb((g + 1f) * 0.5f) * 255f).roundToInt()

            val b = imageByte.getFloat((idx++) * 4)
            val blue: Int = (linearToSrgb((b + 1f) * 0.5f) * 255f).roundToInt()

            val a = imageByte.getFloat((idx++) * 4)
            val alpha: Int = ((a + 1) * 0.5f * 255f).roundToInt()

            pixels[k++] = alpha shl 24 or (red shl 16) or (green shl 8) or blue

        }
        styledImage.setPixels(pixels, 0, width, 0, 0, width, height)
        return styledImage
    }


    private fun linearToSrgb(x: Float): Float {

        var x: Float = x
        if (x < 0f) x = 0f
        else if (x > 1f) x = 1f

        if (x <= 0.003130804953560372f) {
            x *= 12.92f
        } else {
            x = 1.055f * (x.toDouble().pow(((1.0f / 2.4f).toDouble())).toFloat() - 0.055f)
        }
        return x

    }

    fun getLocationBitmap(url: String): Bitmap {
        val fis = FileInputStream(url)
        return BitmapFactory.decodeStream(fis)
    }


    private fun getIdxValue(arr: ByteBuffer, i: Int, j: Int): Float {
        return arr.getFloat(((65536) * 0 + (i * 256) + j) * 4)
    }

    private fun floatToInt(data: Float): Int {
        var tmp = Math.round(data * 255)
        if (tmp < 0) {
            tmp = 0
        } else if (tmp > 255) {
            tmp = 255
        }
        //        Log.e(TAG, tmp + " " + data);
        return tmp
    }


    /*private fun floatArrayToBitmap(data: ByteBuffer): Bitmap? {
        data.rewind()
        val width = 256
        val height = 256
        val intdata = IntArray(width * height)
        // 因为我们用的Bitmap是ARGB的格式，而data是RGB的格式，所以要经过转换，A指的是透明度
        val len = width * height
        for (i in 0 until len) {
            val R: Int = floatToInt(data.getFloat((i) * 4))
            val G: Int = floatToInt(data.getFloat((len + i) * 4))
            val B: Int = floatToInt(data.getFloat((len * 2 + i) * 4))
            val A: Int = floatToInt(data.getFloat((len * 3 + i) * 4))
            intdata[i] = 0xff shl 24 or (R shl 16) or (G shl 8) or (B shl 0)
//            Log.e(TAG, intdata[i]+"");
        }
        //得到位图
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(intdata, 0, width, 0, 0, width, height)
        return bitmap
    }*/

    private fun floatArrayToBitmap(floatArray: Any, flag: String): Bitmap {

        // Create empty bitmap in RGBA format
        val width = 256
        val height = 256
        val bmp: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(width * height * 4)

        // mapping smallest value to 0 and largest value to 255
//        val maxValue = floatArray.max() ?: 1.0f
//        val minValue = floatArray.min() ?: -1.0f
//        val delta = maxValue-minValue

        // Define if float min..max will be mapped to 0..255 or 255..0
//        val conversion = { v: Float -> ((v-minValue)/delta*255.0f).roundToInt()}

        if (flag == "arr") {
            val floatArrays = floatArray as Array<Array<Array<FloatArray>>>
            // copy each value from float array to RGB channels and set alpha channel
            for (i in 0 until width) {
                for (j in 0 until height) {
                    val r = (floatArrays[0][0][i][j] * 255).toInt()
                    val g = (floatArrays[0][1][i][j] * 255).toInt()
                    val b = (floatArrays[0][2][i][j] * 255).toInt()
                    val a = (floatArrays[0][3][i][j] * 255).toInt()
                    pixels[i * 256 + j] = Color.argb(a, r, g, b)
                }
            }
        } else {
//            val arr = ByteBuffer.allocateDirect(4 * height * width * 4)
            val arr = floatArray as ByteBuffer
            arr.rewind()
            for (i in 0 until width * height) {
                val r = (arr.getFloat(i * 4) * 255).roundToInt()
                val g = (arr.getFloat((i + width * height) * 4) * 255).roundToInt()
                val b = (arr.getFloat((i + 2 * width * height) * 4) * 255).roundToInt()
                val a = (arr.getFloat((i + 3 * width * height) * 4) * 255).roundToInt()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    pixels[i] = Color.argb(a, r, g, b)
                }
            }
        }
        bmp.setPixels(pixels, 0, width, 0, 0, width, height)

        return bmp
    }

    private fun getImgBuffer(bitmap: Bitmap, flag: String): Any {


        val width = bitmap.width
        val height = bitmap.height

        val len = width * height
        val pixels = IntArray(len)

        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        if (flag == "arr") {
            val aa = Array(1) { Array(4) { Array(256) { FloatArray(256) } } }
            for (i in 0 until width) {
                for (j in 0 until height) {
                    val color = bitmap.getPixel(j, i)
                    val R = srgbToLinear(color shr 16 and 0xFF)
                    val G = srgbToLinear(color shr 8 and 0xFF)
                    val B = srgbToLinear(color and 0xFF)
                    val A = ((color shr 24) / 255f) * 2f - 1f
                    aa[0][0][i][j] = R
                    aa[0][1][i][j] = G
                    aa[0][2][i][j] = B
                    aa[0][3][i][j] = A
                }
            }
            return aa
        } else if (flag == "byte") {
            val rgba = FloatArray(len * 4)
            val inputBuffer = ByteBuffer.allocateDirect(1 * 4 * 256 * 256 * 4)
            inputBuffer.order(ByteOrder.nativeOrder())
            inputBuffer.rewind()
            for (i in 0 until len) {
                val color = pixels[i]
                val R = srgbToLinear(color shr 16 and 0xFF)
                val G = srgbToLinear(color shr 8 and 0xFF)
                val B = srgbToLinear(color and 0xFF)
                val A = ((color shr 24) / 255f) * 2f - 1f
                rgba[i] = R
                rgba[len + i] = G
                rgba[len * 2 + i] = B
                rgba[len * 3 + i] = A
            }
            for (i in 0 until 4 * len) {
                inputBuffer.putFloat(rgba[i])
            }
            /*        var pixel = 0
                    val mean = 0
                    val std = 255f
                    for (y in 0 until height) {
                        for (x in 0 until width) {
                            val value = pixels[pixel++]
                            // Normalize channel values to [-1.0, 1.0]. This requirement varies by
                            // model. For example, some models might require values to be normalized
                            // to the range [0.0, 1.0] instead.
                            inputBuffer.putFloat(((value shr 24) - mean) / std)
                            inputBuffer.putFloat(((value shr 16 and 0xFF) - mean) / std)
                            inputBuffer.putFloat(((value shr 8 and 0xFF) - mean) / std)
                            inputBuffer.putFloat(((value and 0xFF) - mean) / std)
        //                    for (k in 0 until 4) {
        //                        inputBuffer.putFloat(0.1f)
        //                    }
                        }
                    }*/

//            val a = inputBuffer.getFloat(25700 * 4)
            inputBuffer.rewind()
            return inputBuffer
        } else {
            val rgba = FloatArray(len * 4)
            for (i in 0 until len) {
                val color = pixels[i]
                val R = srgbToLinear(color shr 16 and 0xFF)
                val G = srgbToLinear(color shr 8 and 0xFF)
                val B = srgbToLinear(color and 0xFF)
                val A = (color shr 24) / 255f
                rgba[i] = R
                rgba[len + i] = G
                rgba[len * 2 + i] = B
                rgba[len * 3 + i] = A
            }
            return rgba
        }
    }

}