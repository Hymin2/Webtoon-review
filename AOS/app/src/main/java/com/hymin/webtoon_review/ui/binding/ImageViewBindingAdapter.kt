package com.hymin.webtoon_review.ui.binding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hymin.webtoon_review.R

@BindingAdapter("imageUrl")
fun bindImageFromUrl(view: ImageView, imageUrl: String?) {
    if (!imageUrl.isNullOrEmpty()) {
        Glide.with(view.context)
            .load(imageUrl)
            .apply(RequestOptions().centerCrop())
            .placeholder(R.drawable.placeholder)
            .into(view)
    } else {
        view.setImageResource(R.drawable.green_border_box)
    }
}