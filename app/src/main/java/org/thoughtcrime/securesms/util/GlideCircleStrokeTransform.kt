package org.thoughtcrime.securesms.util

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Cap
import android.graphics.Shader.TileMode
import androidx.annotation.ColorInt
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest

/**
 * Created by Author on 2020/4/30
 */
class GlideCircleStrokeTransform : BitmapTransformation {
    private var mBorderWidth = 4
    private var mBorderColor = -1

    constructor() {}
    constructor(borderWidth: Int, @ColorInt borderColor: Int) {
        mBorderWidth = borderWidth
        mBorderColor = borderColor
    }

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        val size = Math.min(toTransform.width, toTransform.height)
        val x = (toTransform.width - size) / 2
        val y = (toTransform.height - size) / 2
        val squaredBitmap = Bitmap.createBitmap(toTransform, x, y, size, size)
        val bitmap = Bitmap.createBitmap(
            size,
            size,
            if (toTransform.config != null) toTransform.config else Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        val paint = Paint()
        val shader = BitmapShader(squaredBitmap, TileMode.CLAMP, TileMode.CLAMP)
        paint.shader = shader
        paint.isAntiAlias = true
        val mBorderPaint = Paint()
        mBorderPaint.style = Paint.Style.STROKE
        mBorderPaint.strokeWidth = mBorderWidth.toFloat()
        mBorderPaint.color = mBorderColor
        mBorderPaint.strokeCap = Cap.ROUND
        mBorderPaint.isAntiAlias = true
        val r = size.toFloat() / 2.0f
        val r1 = (size - 2 * mBorderWidth).toFloat() / 2.0f
        canvas.drawCircle(r, r, r1, paint)
        canvas.drawCircle(r, r, r1, mBorderPaint)
        squaredBitmap.recycle()
        return bitmap
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {}
}