package com.jetpack.mvi.repository

import com.framework.mvi.repository.BaseRepository
import kotlinx.coroutines.delay

class LoginRepository : BaseRepository() {

    suspend fun login(username: String, password: String): Boolean {
        delay(1200)
        return username.isNotBlank() && password.isNotBlank()
    }
}
