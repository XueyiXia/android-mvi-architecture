package com.jetpack.mvi.repository

import com.framework.mvi.repository.BaseRepository
import com.jetpack.mvi.db.AppDatabase
import com.jetpack.mvi.db.UserEntity
import com.jetpack.mvi.error.ErrorHandler
import com.jetpack.mvi.error.UiError
import com.jetpack.mvi.model.User
import com.jetpack.mvi.net.ApiClient
import com.jetpack.mvi.net.NetworkResult
import android.content.Context

/**
 * @author: xiaxueyi
 * @date: 2025-10-21
 * @time: 17:00
 * @说明: 用户仓库：Retrofit + Room 缓存
 */
class UserRepository(private val context: Context) : BaseRepository() {

    private val apiService = ApiClient.create()
    private val database = AppDatabase.getInstance(context)
    private val userDao = database.userDao()

    suspend fun getUsers(): NetworkResult<List<User>> {
        return try {
            val remoteUsers = apiService.getUsers()
            val entities = remoteUsers.map { UserEntity(it.id, it.name, it.email) }
            userDao.clear()
            userDao.insertAll(entities)
            NetworkResult.Success(remoteUsers)
        } catch (e: Exception) {
            val cached = userDao.getAll()
            if (cached.isNotEmpty()) {
                NetworkResult.Success(cached.map { User(it.id, it.name, it.email) })
            } else {
                NetworkResult.Exception(e)
            }
        }
    }

    fun mapError(throwable: Throwable): UiError {
        return ErrorHandler.handleThrowable(throwable)
    }
}
