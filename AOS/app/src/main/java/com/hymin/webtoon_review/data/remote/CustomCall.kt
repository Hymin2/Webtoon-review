package com.hymin.webtoon_review.data.remote

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CustomCall<T>(
    private val delegate: Call<T>,
    private val context: Context,
) : Call<T> by delegate {

    override fun enqueue(callback: Callback<T>) {
        delegate.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                callback.onResponse(call, response)
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, "잠시 후에 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}