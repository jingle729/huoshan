package com.effectsar.labcv.ebox.image

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.State
import com.volcengine.effectone.utils.SizeUtil
import kotlin.math.roundToInt

class ImageQualityItemDecoration(private val horizontalItemSpacing: Float) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: State) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.apply {
            left = SizeUtil.dp2px(horizontalItemSpacing / 2)
            right = SizeUtil.dp2px(horizontalItemSpacing / 2)
        }
    }

    private val paint by lazy {
        Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            alpha = (255 * 0.12).roundToInt()
        }
    }

    private val mBounds = Rect()

    @SuppressLint("UseKtx")
    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: State) {
        super.onDraw(canvas, parent, state)
        val child = parent.getChildAt(0) ?: return
        val index = parent.getChildAdapterPosition(child)
        if (index == 0) {
            val layoutManager = parent.layoutManager ?: return
            canvas.save()
            val top: Int = SizeUtil.dp2px(24F)
            val bottom: Int = top + SizeUtil.dp2px(54F)
            layoutManager.getDecoratedBoundsWithMargins(child, mBounds)
            val right = mBounds.right + child.translationX
            val left = right - 1
            canvas.drawRect(left, top.toFloat(), right, bottom.toFloat(), paint)
            canvas.restore()
        }
    }
}