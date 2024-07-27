package com.hymin.webtoon_review.data.local.datasource

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore("user")

@Singleton
class UserDataStore @Inject constructor(
    private val context: Context,
) {
    companion object {
        private val JSON_WEB_TOKEN = stringPreferencesKey("jwt")
    }

    suspend fun storeJwt(jwt: String) {
        context.dataStore.edit { preferences ->
            preferences[JSON_WEB_TOKEN] = jwt
        }
    }

    val getJwt: Flow<String?> = context.dataStore.data.map { jwt ->
        jwt[JSON_WEB_TOKEN]
    }
}
