package com.pobochii.multiavatarview

import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat

class BadgedDrawable(
    resources: Resources,
    @DrawableRes private val resId: Int,
    @ColorRes iconColor: Int,
    isFromCustomerApp: Boolean = false,
    badgeColorForCustomerApp: Int = 0
) : Drawable() {

    private val density = resources.displayMetrics.density
    var showBadge = false
        set(value) {
            field = value
            invalidateSelf()
        }

    private val rect by lazy {
        RectF()
    }

    private val paint by lazy {
        Paint().apply {
            isAntiAlias = true
            color = if (isFromCustomerApp) badgeColorForCustomerApp
            else Color.RED
            style = Paint.Style.FILL
        }
    }

    private val icon: Bitmap =
        resources.vectorToBitmap(resId, ResourcesCompat.getColor(resources, iconColor, null))

    override fun onBoundsChange(bounds: Rect) {
        rect.set(0f, 0f, icon.width.toFloat(), icon.height.toFloat())
    }

    override fun draw(canvas: Canvas) {
        canvas.save()
        canvas.drawBitmap(icon, -rect.width() / 2f, -rect.height() / 2f, null)
        if (showBadge) {
            when {
                CIRCLE -> canvas.drawCircle(
                    rect.width() * RATE_XY,
                    rect.width() * RATE_XY,
                    BADGE_RADIUS * density,
                    paint
                )
                ARC -> canvas.drawArc(rect, 90f, 180f, true, paint)
                else -> canvas.drawRoundRect(rect, CORNER_RADIUS, CORNER_RADIUS * density, paint)
            }
        }
        canvas.restore()
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
        private const val CIRCLE = true
        private const val ARC = false
        private const val CORNER_RADIUS = 5f
        private const val BADGE_RADIUS = 5f
        private const val RATE_XY = 0.4f
    }
}