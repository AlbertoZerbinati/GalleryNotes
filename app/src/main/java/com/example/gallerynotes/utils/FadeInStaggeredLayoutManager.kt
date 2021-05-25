package com.example.gallerynotes.utils

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
import androidx.annotation.WorkerThread
import androidx.recyclerview.widget.StaggeredGridLayoutManager

open class FadeInStaggeredGridLayoutManager(spanCount: Int, orientation: Int) : StaggeredGridLayoutManager(spanCount, orientation) {


    override fun addView(child: View, index: Int) {
        super.addView(child, index)
        // begin animation when view is laid out
//        child.alpha = 0.3f
//        child.animate().alpha(1f)
//            .setDuration(500L)
    }
}