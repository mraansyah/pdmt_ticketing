package com.pdmt.ticketing.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pdmt_prefs")

class TokenManager(private val context: Context) {

    companion object {
        val TOKEN_KEY = stringPreferencesKey("jwt_token")
        val USERNAME_KEY = stringPreferencesKey("username")
        val ROLE_KEY = stringPreferencesKey("role")
        val USER_NAME_KEY = stringPreferencesKey("user_name")
    }

    // Simpan token dan info user setelah login
    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
    }

    suspend fun saveUserInfo(username: String, role: String, name: String) {
        context.dataStore.edit { prefs ->
            prefs[USERNAME_KEY] = username
            prefs[ROLE_KEY] = role
            prefs[USER_NAME_KEY] = name
        }
    }

    // Ambil token (untuk Interceptor — sync)
    fun getTokenSync(): String? = runBlocking {
        context.dataStore.data.map { it[TOKEN_KEY] }.first()
    }

    // Ambil token (untuk coroutine)
    suspend fun getToken(): String? {
        return context.dataStore.data.map { it[TOKEN_KEY] }.first()
    }

    suspend fun getRole(): String? {
        return context.dataStore.data.map { it[ROLE_KEY] }.first()
    }

    suspend fun getUserName(): String? {
        return context.dataStore.data.map { it[USER_NAME_KEY] }.first()
    }

    // Hapus semua data saat logout
    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }

    // Cek apakah sudah login
    suspend fun isLoggedIn(): Boolean {
        return getToken() != null
    }
}