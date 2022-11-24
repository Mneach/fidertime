package edu.bluejack22_1.fidertime.common

import android.graphics.Rect
import android.util.Log
import android.view.View
import androidx.core.graphics.or
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Orientation

class MarginItemDecoration(private val spaceSize: Int, private val orientation: Int?) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect, view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        with(outRect) {
            when (orientation) {
                LinearLayoutManager.HORIZONTAL -> {
                    left = spaceSize / 2
                    right = spaceSize / 2
                    if (parent.getChildAdapterPosition(view) == 0) {
                        left = 0
                    }
                }
                LinearLayoutManager.VERTICAL -> {
                    top = spaceSize / 2
                    bottom = spaceSize / 2
                    if (parent.getChildAdapterPosition(view) == 0) {
                        top = 0
                    }
                }
                else -> {
                    top = spaceSize / 2
                    bottom = spaceSize / 2
                    if (parent.getChildAdapterPosition(view) == 0) {
                        top = 0
                    }
                }
            }

        }
    }
}