package com.hymin.webtoon_review.data.repository

import com.hymin.webtoon_review.data.dto.request.user.LoginRequest
import com.hymin.webtoon_review.data.dto.request.user.RegisterRequest
import com.hymin.webtoon_review.data.dto.response.BaseResponse
import com.hymin.webtoon_review.data.dto.response.DataResponse
import com.hymin.webtoon_review.data.local.datasource.UserDataStore
import com.hymin.webtoon_review.data.remote.datasource.UserRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import retrofit2.Response
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userRemoteDataSource: UserRemoteDataSource,
    private val userDataStore: UserDataStore,
) {

    suspend fun login(loginRequest: LoginRequest): Response<BaseResponse> {
        return userRemoteDataSource.login(loginRequest)
    }

    suspend fun register(registerRequest: RegisterRequest): Response<BaseResponse> {
        return userRemoteDataSource.register(registerRequest)
    }

    suspend fun checkDuplicatedUsername(username: String): Response<DataResponse<Boolean>> {
        return userRemoteDataSource.checkDuplicatedUsername(username)
    }

    suspend fun checkDuplicatedNickname(nickname: String): Response<DataResponse<Boolean>> {
        return userRemoteDataSource.checkDuplicatedNickname(nickname)
    }

    suspend fun storeJwt(jwt: String) {
        userDataStore.storeJwt(jwt)
    }

    suspend fun existJwt(): Boolean {
        return getJwt().firstOrNull()?.isNotEmpty() == true
    }

    fun getJwt(): Flow<String?> {
        return userDataStore.getJwt
    }
}