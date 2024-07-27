package com.hymin.webtoon_review.data.remote.datasource

import com.hymin.webtoon_review.data.dto.request.user.LoginRequest
import com.hymin.webtoon_review.data.dto.request.user.RegisterRequest
import com.hymin.webtoon_review.data.dto.response.BaseResponse
import com.hymin.webtoon_review.data.dto.response.DataResponse
import com.hymin.webtoon_review.data.remote.service.UserService
import retrofit2.Response
import javax.inject.Inject

class UserRemoteDataSource @Inject constructor(
    private val userService: UserService,
) {

    suspend fun login(loginRequest: LoginRequest): Response<BaseResponse> {
        return userService.login(loginRequest)
    }

    suspend fun register(registerRequest: RegisterRequest): Response<BaseResponse> {
        return userService.register(registerRequest)
    }

    suspend fun checkDuplicatedUsername(username: String): Response<DataResponse<Boolean>> {
        return userService.checkDuplicatedUsername(username)
    }

    suspend fun checkDuplicatedNickname(nickname: String): Response<DataResponse<Boolean>> {
        return userService.checkDuplicatedUsername(nickname)
    }
}
