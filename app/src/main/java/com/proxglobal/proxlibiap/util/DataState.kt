package com.proxglobal.proxlibiap.util

sealed class DataState<out T>(val message: String? = null, val data: T? = null) {
    object Loading: DataState<Nothing>()
    class Failure(message: String? = null, val error: Throwable? = null): DataState<Nothing>(message)
    class Success<out T>(message: String? = null, data: T? = null): DataState<T>(message, data)
}