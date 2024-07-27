package com.hymin.webtoon_review.data.remote

import android.content.Context
import retrofit2.Call
import retrofit2.CallAdapter
import java.lang.reflect.Type

class CustomCallAdapter<T>(
    private val responseType: Type,
    private val context: Context,
) : CallAdapter<T, Call<T>> {

    override fun responseType(): Type = responseType

    override fun adapt(call: Call<T>): Call<T> {
        return CustomCall(call, context)
    }
}