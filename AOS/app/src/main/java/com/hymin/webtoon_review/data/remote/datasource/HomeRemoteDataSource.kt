package com.hymin.webtoon_review.data.remote.datasource

import com.hymin.webtoon_review.data.dto.response.DataResponse
import com.hymin.webtoon_review.data.dto.response.home.GenreResponse
import com.hymin.webtoon_review.data.dto.response.home.WebtoonListResponse
import com.hymin.webtoon_review.data.remote.service.HomeService
import javax.inject.Inject

class HomeRemoteDataSource @Inject constructor(
    private val homeService: HomeService,
) {

    suspend fun getGenreList(): DataResponse<List<GenreResponse>>? {
        val response = homeService.getGenreList()

        if (response.isSuccessful) {
            response.body().let {
                return it
            }
        } else {
            throw Exception("Failed to get category list: ${response.message()}")
        }
    }

    suspend fun getWebtoonList(
        sort: String,
        dayOfWeek: String?,
        genre: String?,
        lastValue: String?,
        updatedAt: String?,
    ): DataResponse<List<WebtoonListResponse>>? {
        val response = homeService.getWebtoonList(sort, dayOfWeek, genre, lastValue, updatedAt)

        if (response.isSuccessful) {
            response.body().let {
                return it
            }
        } else {
            throw Exception("Failed to get webtoon list: ${response.message()}")
        }
    }

    suspend fun getHotWebtoonList(): DataResponse<List<WebtoonListResponse>>? {
        homeService.getHotWebtoonList().apply {
            if (this.isSuccessful) {
                this.body().let {
                    return it
                }
            } else {
                throw Exception("Failed to get hot webtoon list: ${this.message()}")
            }
        }
    }
}