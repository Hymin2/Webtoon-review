package com.hymin.webtoon_review.data.remote.service

import com.hymin.webtoon_review.data.dto.response.DataResponse
import com.hymin.webtoon_review.data.dto.response.home.GenreResponse
import com.hymin.webtoon_review.data.dto.response.home.WebtoonListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface HomeService {

    @GET("/webtoons/categories")
    suspend fun getGenreList(): Response<DataResponse<List<GenreResponse>>>

    @GET("/webtoons")
    suspend fun getWebtoonList(
        @Query("sort") sort: String,
        @Query("dayOfWeek") dayOfWeek: String? = null,
        @Query("genre") genre: String? = null,
        @Query("lastValue") lastValue: String? = null,
        @Query("updatedAt") updatedAt: String? = null,
    ): Response<DataResponse<List<WebtoonListResponse>>>

    @GET("/webtoons/hot-webtoons")
    suspend fun getHotWebtoonList(): Response<DataResponse<List<WebtoonListResponse>>>
}