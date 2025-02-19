package com.hymin.webtoon_review.data.model.home

data class WebtoonListModel(
    val id: Long,
    val webtoonName: String,
    val author: String,
    val dayOfWeek: String,
    val starScore: String,
    val thumbnail: String,
)
