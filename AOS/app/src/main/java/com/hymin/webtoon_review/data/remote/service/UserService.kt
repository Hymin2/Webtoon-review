package com.hymin.webtoon_review.data.remote.service

import com.hymin.webtoon_review.data.dto.request.user.LoginRequest
import com.hymin.webtoon_review.data.dto.request.user.RegisterRequest
import com.hymin.webtoon_review.data.dto.response.BaseResponse
import com.hymin.webtoon_review.data.dto.response.DataResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface UserService {

    @POST("/users/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<BaseResponse>

    @POST("/users")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<BaseResponse>

    @GET("/users/check/username")
    suspend fun checkDuplicatedUsername(@Query("username") username: String): Response<DataResponse<Boolean>>

    @GET("/users/check/nickname")
    suspend fun checkDuplicatedNickname(@Query("nickname") nickname: String): Response<DataResponse<Boolean>>
}
