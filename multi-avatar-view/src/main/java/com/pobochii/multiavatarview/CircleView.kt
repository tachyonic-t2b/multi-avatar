package com.pobochii.multiavatarview

import android.content.Context
import android.graphics.*
import android.media.ThumbnailUtils
import android.util.AttributeSet
import android.util.SparseArray
import android.widget.ImageView
import androidx.annotation.ColorInt

class CircleView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ImageView(context, attrs, defStyleAttr) {

    private val bitmaps: SparseArray<Bitmap> = SparseArray(MAX_CAPACITY)
    private val density = resources.displayMetrics.density
    private val cornerRadius = CORNER_RADIUS * density
    private val divider = DIVIDER_WIDTH * density
    private val margin = MARGIN * density
    private val margs = margin * 2
    private var statusX: Float = 0.0f
    private var statusY: Float = 0.0f
    private val strokeRadius by lazy { STATUS_STROKE_RADIUS * density }
    private val fillRadius by lazy { STATUS_FILL_RADIUS * density }
    private val textSize by lazy { TEXT_SIZE_DPI * density }
    private var canvas: Canvas
    var online = false
        set(value) {
            field = value
            invalidate()
        }
    var withStatus = false
        set(value) {
            field = value
            invalidate()
        }
    private val statusPaintOn by lazy {
        Paint().apply {
            color = Color.GREEN
            isAntiAlias = true
            style = Paint.Style.FILL
        }
    }
    private val statusPaintOff by lazy {
        Paint().apply {
            color = Color.GRAY
            isAntiAlias = true
            style = Paint.Style.FILL
        }
    }

    private val statusStrokePaint by lazy {
        Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
            strokeWidth = STATUS_STROKE_WIDTH * density
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
        }
    }

    private val textPaint by lazy {
        Paint().also {
            it.color = Color.WHITE
            it.textSize = textSize
            it.isAntiAlias = true
            it.textAlign = Paint.Align.CENTER
        }
    }

    private var canvasWidth: Float

    private var canvasHeight: Float

    private var canvasHalfWidth: Float

    private var canvasHalfHeight: Float

    var avatarsCount: Int? = 1
        set(value) {
            field = value?.let { if (value <= MAX_CAPACITY) value else MAX_CAPACITY } ?: 1
        }

    private var dirty = false

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.CircleView)
        val viewWidth = a.getDimensionPixelSize(R.styleable.CircleView_android_layout_width, (WIDTH * density).toInt())
        val viewHeight = a.getDimensionPixelSize(R.styleable.CircleView_android_layout_height, (HEIGHT * density).toInt())
        a.recycle()
        val createBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888)
        canvas = Canvas(createBitmap)
        background = CircleDrawable(createBitmap, cornerRadius, margin.toInt())
        canvasWidth = canvas.width - margin * 2
        canvasHeight = canvas.height - margin * 2
        canvasHalfWidth = canvasWidth / 2
        canvasHalfHeight = canvasHeight / 2
        textPaint.textSize = canvasWidth * 0.45f
    }

    fun clear() {
        bitmaps.clear()
        online = false
        dirty = false
    }

    fun addBitmap(key: Int, bitmap: Bitmap) {
        if (bitmaps.indexOfKey(key) < 0) {
            bitmaps.put(key, bitmap)
            dirty = bitmaps.size() == avatarsCount
        }
        if (dirty) {
            dirty = false
            updateBitmap()
        }
    }

    fun addBitmap(bitmap: Bitmap) {
        addBitmap(0, bitmap)
    }

    fun addTextBitmap(key: Int, text: String, @ColorInt color: Int) {
        val textBitmap = Bitmap.createBitmap(canvasWidth.toInt(), canvasHeight.toInt(), Bitmap.Config.ARGB_8888)
        with(Canvas(textBitmap)) {
            save()
            drawColor(color)
            drawText(text, (width / 2).toFloat(), (height / 2 - (textPaint.ascent() + textPaint.descent()) / 2), textPaint)
            restore()
        }
        addBitmap(key, textBitmap)
    }

    fun addTextBitmap(text: String, @ColorInt color: Int) {
        val textBitmap = Bitmap.createBitmap(canvasWidth.toInt(), canvasHeight.toInt(), Bitmap.Config.ARGB_8888)
        with(Canvas(textBitmap)) {
            save()
            drawColor(color)
            drawText(text, (width / 2).toFloat(), (height / 2 - (textPaint.ascent() + textPaint.descent()) / 2), textPaint)
            restore()
        }
        addBitmap(textBitmap)
    }

    private fun updateBitmap() {
        if (bitmaps.isEmpty()) return
        canvas.drawColor(Color.WHITE)
        when (bitmaps.size()) {
            1, 2 -> {
                val bitmap = bitmaps.valueAt(0)
                val src = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
                val dst = RectF(0f, 0f, canvasWidth, canvasHeight)
                val matrix = Matrix().also {
                    it.setRectToRect(src, dst, Matrix.ScaleToFit.START)
                }
                canvas.drawBitmap(bitmap, matrix, null)
            }
            3 -> {
                for (index in 0 until bitmaps.size()) {
                    val key = bitmaps.keyAt(index)
                    val bitmap = bitmaps[key]
                    val src = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
                    val dst = when (key) {
                        0 -> RectF(0f, 0f, canvasHalfWidth - divider / 2, canvasHeight)
                        1 -> RectF(canvasHalfWidth + divider / 2, 0f, canvasWidth, canvasHalfHeight - divider / 2)
                        2 -> RectF(canvasHalfWidth + divider / 2, canvasHalfHeight + divider / 2, canvasWidth, canvasHeight)
                        else -> return
                    }
                    when (key) {
                        0 -> {
                            val thumb = ThumbnailUtils.extractThumbnail(bitmap, (canvasHalfWidth - divider / 2).toInt(), canvasHeight.toInt())
                            canvas.drawBitmap(thumb, Matrix(), null)
                        }
                        1 -> {
                            val matrix = Matrix().also {
                                it.setRectToRect(src, dst, Matrix.ScaleToFit.START)
                            }
                            canvas.drawBitmap(bitmap, matrix, null)
                        }
                        2 -> {
                            val matrix = Matrix().also {
                                it.setRectToRect(src, dst, Matrix.ScaleToFit.END)
                            }
                            canvas.drawBitmap(bitmap, matrix, null)
                        }
                        else -> return
                    }
                }
            }
            4 -> {
                for (index in 0 until bitmaps.size()) {
                    val key = bitmaps.keyAt(index)
                    val bitmap = bitmaps[key]
                    val src = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
                    val dst = when (key) {
                        0 -> RectF(0f, 0f, canvasHalfWidth - divider / 2, canvasHalfHeight - divider / 2)
                        1 -> RectF(canvasHalfWidth + divider / 2, 0f, canvasWidth, canvasHalfHeight - divider / 2)
                        2 -> RectF(canvasHalfWidth + divider / 2, canvasHalfHeight + divider / 2, canvasWidth, canvasHeight)
                        3 -> RectF(0f, canvasHalfHeight + divider / 2, canvasHalfWidth - divider / 2, canvasHeight)
                        else -> return
                    }
                    when (key) {
                        0 -> {
                            val matrix = Matrix().also {
                                it.setRectToRect(src, dst, Matrix.ScaleToFit.START)
                            }
                            canvas.drawBitmap(bitmap, matrix, null)
                        }
                        1 -> {
                            val matrix = Matrix().also {
                                it.setRectToRect(src, dst, Matrix.ScaleToFit.START)
                            }
                            canvas.drawBitmap(bitmap, matrix, null)
                        }
                        2 -> {
                            val matrix = Matrix().also {
                                it.setRectToRect(src, dst, Matrix.ScaleToFit.END)
                            }
                            canvas.drawBitmap(bitmap, matrix, null)
                        }
                        3 -> {
                            val matrix = Matrix().also {
                                it.setRectToRect(src, dst, Matrix.ScaleToFit.END)
                            }
                            canvas.drawBitmap(bitmap, matrix, null)
                        }
                        else -> return
                    }
                }
            }
        }
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        statusX = (MeasureSpec.getSize(widthMeasureSpec) - margs) * RATE_XY
        statusY = (MeasureSpec.getSize(heightMeasureSpec) - margs) * RATE_XY
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (withStatus) {
            canvas?.let {
                with(it) {
                    save()
                    drawCircle(statusX, statusY, strokeRadius, statusStrokePaint)
                    drawCircle(statusX, statusY, fillRadius, if (online) statusPaintOn else statusPaintOff)
                    restore()
                }
            }
        }
    }

    companion object {
        private val CORNER_RADIUS = 5 // dips
        private val DIVIDER_WIDTH = 1 // dips
        private val MARGIN = 0 // dips
        private val WIDTH = 100 // dips
        private val HEIGHT = 100 //dips
        private const val RATE_XY = 0.8535f
        private const val STATUS_STROKE_RADIUS = 5f //dips
        private const val STATUS_FILL_RADIUS = 5f //dips
        private const val STATUS_STROKE_WIDTH = 1f //dips
        private const val TEXT_SIZE_DPI = 14f //dips
        private const val MAX_CAPACITY = 4
    }
}