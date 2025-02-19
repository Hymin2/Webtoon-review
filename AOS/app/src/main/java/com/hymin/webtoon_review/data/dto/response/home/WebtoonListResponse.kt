package com.hymin.webtoon_review.data.dto.response.home

data class WebtoonListResponse(
    val id: Long,
    val name: String,
    val thumbnail: String,
    val updatedAt: String,
    val authorName: String,
    val dayOfWeek: String,
    val genre: String,
    val recommendationCount: Int,
    val starScore: Int,
    val manPopularityScore: Int,
    val femalePopularityScore: Int,
)
