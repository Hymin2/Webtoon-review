package com.hymin.webtoon_review.data.dto.response

data class DataResponse<T>(
    val status: Int,
    val message: String,
    val time: String,
    val data: T,
)