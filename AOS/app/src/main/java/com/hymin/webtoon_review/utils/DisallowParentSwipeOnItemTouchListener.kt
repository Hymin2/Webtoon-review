package com.hymin.webtoon_review.utils

import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

class DisallowParentSwipeOnItemTouchListener : RecyclerView.OnItemTouchListener {
    var startPoint = 0f
    override fun onInterceptTouchEvent(
        rv: RecyclerView,
        e: MotionEvent,
    ): Boolean {

        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                startPoint = e.x
            }

            MotionEvent.ACTION_MOVE -> {
                val pointX = e.x
                if (abs(pointX - startPoint) > 5f) {
                    rv.parent.requestDisallowInterceptTouchEvent(true)
                }
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL,
            -> {
                rv.parent.requestDisallowInterceptTouchEvent(false)
            }
        }
        return false
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
}