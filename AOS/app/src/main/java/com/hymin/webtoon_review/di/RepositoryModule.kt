package com.hymin.webtoon_review.di

import com.hymin.webtoon_review.data.local.datasource.UserDataStore
import com.hymin.webtoon_review.data.remote.datasource.UserRemoteDataSource
import com.hymin.webtoon_review.data.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {
    @Provides
    @Singleton
    fun provideUserRepository(
        userRemoteDataSource: UserRemoteDataSource,
        userDataStore: UserDataStore,
    ): UserRepository {
        return UserRepository(userRemoteDataSource, userDataStore)
    }
}
