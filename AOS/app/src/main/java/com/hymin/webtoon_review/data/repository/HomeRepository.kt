package com.hymin.webtoon_review.data.repository

import com.hymin.webtoon_review.data.model.home.WebtoonGenreModel
import com.hymin.webtoon_review.data.model.home.WebtoonListModel
import com.hymin.webtoon_review.data.remote.datasource.HomeRemoteDataSource
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val homeRemoteDataSource: HomeRemoteDataSource,
) {

    suspend fun getGenreList(): List<WebtoonGenreModel> {
        return homeRemoteDataSource.getGenreList()!!.data.map {
            WebtoonGenreModel(it.id, it.name)
        }.toList()
    }

    suspend fun getWebtoonList(
        sort: String,
        dayOfWeek: String?,
        genre: String?,
        lastValue: String?,
        updatedAt: String?,
    ): List<WebtoonListModel> {
        return homeRemoteDataSource.getWebtoonList(
            sort,
            dayOfWeek,
            genre,
            lastValue,
            updatedAt
        )!!.data.map {
            val starScore: Double =
                it.starScore / Math.pow(10.0, (it.starScore.toString().length - 1).toDouble())
            WebtoonListModel(
                it.id,
                it.name,
                it.authorName,
                it.dayOfWeek,
                starScore.toString(),
                it.thumbnail
            )
        }
    }

    suspend fun getHotWebtoonList(): List<WebtoonListModel> {
        return homeRemoteDataSource.getHotWebtoonList()!!.data.map {
            val starScore: Double =
                it.starScore / Math.pow(10.0, (it.starScore.toString().length - 1).toDouble())
            WebtoonListModel(
                it.id,
                it.name,
                it.authorName,
                it.dayOfWeek,
                starScore.toString(),
                it.thumbnail
            )
        }
    }
}