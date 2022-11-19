package edu.bluejack22_1.fidertime.common

import android.graphics.Rect
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
                    if (parent.getChildAdapterPosition(view) == 0) {
                        right = spaceSize
                    }
                }
                LinearLayoutManager.VERTICAL -> {
                    if (parent.getChildAdapterPosition(view) == 0) {
                        bottom = spaceSize
                    }
                }
                else -> {
                    if (parent.getChildAdapterPosition(view) == 0) {
                        bottom = spaceSize
                    }
                }
            }

        }
    }
}