package com.pobochii.multiavatarview

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.SparseArray
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat

fun SparseArray<*>.isEmpty(): Boolean {
    return size() == 0
}

/**
 * Demonstrates converting a [Drawable] to a [Bitmap],
 * for use as a marker icon.
 */
fun Resources.vectorToBitmap(@DrawableRes id: Int, @ColorInt color: Int): Bitmap {
    val vectorDrawable = ResourcesCompat.getDrawable(this, id, null)?.mutate()
    vectorDrawable?.let {
        val bitmap = Bitmap.createBitmap(it.intrinsicWidth, it.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        it.setBounds(0, 0, canvas.width, canvas.height)
        DrawableCompat.setTint(vectorDrawable, color)
        it.draw(canvas)
        return bitmap
    }
    return Bitmap.createBitmap(0, 0, Bitmap.Config.ARGB_8888)
}