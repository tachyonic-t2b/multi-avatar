package com.pobochii.multiavatarview

import android.graphics.*
import android.graphics.drawable.Drawable

class CircleDrawable(private val bitmap: Bitmap, private val cornerRadius: Float, private val margin: Int) : Drawable() {
    private val bitmapShader by lazy { BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP) }

    private val rect by lazy {
        RectF()
    }

    private val paint by lazy {
        Paint().apply {
            isAntiAlias = true
            shader = bitmapShader
        }
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)

        rect.set(margin.toFloat(), margin.toFloat(), (bounds.width() - margin).toFloat(), (bounds.height() - margin).toFloat())

        if (USE_VIGNETTE) {
            val vignette = RadialGradient(
                    rect.centerX(), rect.centerY() * 1.0f / 0.7f, rect.centerX() * 1.3f,
                    intArrayOf(0, 0, 0x7f000000), floatArrayOf(0.0f, 0.7f, 1.0f),
                    Shader.TileMode.CLAMP)

            val oval = Matrix()
            oval.setScale(1.0f, 0.7f)
            vignette.setLocalMatrix(oval)

            paint.shader = ComposeShader(bitmapShader, vignette, PorterDuff.Mode.SRC_OVER)
        }
    }

    override fun draw(canvas: Canvas) {
        if (CIRCLE) canvas.drawCircle(rect.width() / 2, rect.height() / 2, rect.width() / 2, paint)
        else if (ARC) canvas.drawArc(rect, 90f, 180f, true, paint)
        else canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    companion object {
        private val USE_VIGNETTE = false
        private val CIRCLE = true
        private val ARC = false
    }
}