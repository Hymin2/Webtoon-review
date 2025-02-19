package com.hymin.webtoon_review.utils

import androidx.recyclerview.widget.DiffUtil
import com.hymin.webtoon_review.data.model.home.WebtoonGenreModel
import com.hymin.webtoon_review.data.model.home.WebtoonSortListModel

class ItemDiffCallBack<T> : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T & Any, newItem: T & Any): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: T & Any, newItem: T & Any): Boolean {
        if (oldItem is WebtoonGenreModel && newItem is WebtoonGenreModel) {
            return oldItem.id == newItem.id
        } else if (oldItem is WebtoonSortListModel && newItem is WebtoonSortListModel) {
            return oldItem.id == newItem.id
        }

        return false
    }
}