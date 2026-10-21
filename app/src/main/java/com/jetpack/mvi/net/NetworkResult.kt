package com.jetpack.mvi.net

/**
 * @author: xiaxueyi
 * @date: 2025-10-21
 * @time: 17:00
 * @说明: 网络统一返回结构
 */
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val code: Int, val message: String) : NetworkResult<Nothing>()
    data class Exception(val throwable: Throwable) : NetworkResult<Nothing>()

    inline fun <R> map(transform: (T) -> R): NetworkResult<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> Error(code, message)
            is Exception -> Exception(throwable)
        }
    }

    fun toUiMessage(): String {
        return when (this) {
            is Success -> ""
            is Error -> message
            is Exception -> throwable.message ?: "未知错误"
        }
    }
}
