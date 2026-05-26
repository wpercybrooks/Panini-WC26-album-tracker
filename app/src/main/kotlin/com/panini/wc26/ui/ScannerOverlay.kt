package com.panini.wc26.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

class ScannerOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val rectPaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    private val textPaint = Paint().apply {
        color = Color.GREEN
        textSize = 40f
        isFakeBoldText = true
    }

    private var items: List<MatchedId> = emptyList()
    private var imageWidth: Int = 0
    private var imageHeight: Int = 0

    fun updateData(newItems: List<MatchedId>, width: Int, height: Int) {
        items = newItems
        imageWidth = width
        imageHeight = height
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (imageWidth == 0 || imageHeight == 0) return

        val scaleX = width.toFloat() / imageWidth
        val scaleY = height.toFloat() / imageHeight

        for (item in items) {
            val rect = item.boundingBox
            val scaledRect = Rect(
                (rect.left * scaleX).toInt(),
                (rect.top * scaleY).toInt(),
                (rect.right * scaleX).toInt(),
                (rect.bottom * scaleY).toInt()
            )
            canvas.drawRect(scaledRect, rectPaint)
            canvas.drawText(item.id, scaledRect.left.toFloat(), (scaledRect.top - 10).toFloat(), textPaint)
        }
    }
}