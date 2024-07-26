package com.hymin.webtoon_review.di

import com.hymin.webtoon_review.data.remote.datasource.UserRemoteDataSource
import com.hymin.webtoon_review.data.remote.service.UserService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataSourceModule {
    @Provides
    @Singleton
    fun provideUserRemoteDataSource(userService: UserService): UserRemoteDataSource {
        return UserRemoteDataSource(userService)
    }
}
