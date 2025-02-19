package com.hymin.webtoon_review.interceptor

import com.hymin.webtoon_review.data.local.datasource.UserDataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(private val userDataStore: UserDataStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()

        runBlocking {
            val jwt = userDataStore.getJwt.firstOrNull()

            if (!jwt.isNullOrEmpty()) {
                userDataStore.getJwt.let {
                    requestBuilder.addHeader("Authorization", userDataStore.getJwt.firstOrNull()!!)
                }
            }
        }

        return chain.proceed(requestBuilder.build())
    }
}