package com.proxglobal.purchase.data.sharepreference

import android.content.Context
import android.content.SharedPreferences

object ProxPreferences {

    private const val NAME = "prox_share_preference"
    private const val MODE = Context.MODE_PRIVATE
    private lateinit var preferences: SharedPreferences
    private var initialed: Boolean = false

    @JvmStatic
    fun init(context: Context) {
        preferences = context.getSharedPreferences(NAME, MODE)
        initialed = true
    }

    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = edit()
        operation(editor)
        editor.apply()
    }

    fun isInitial(): Boolean {
        return try {
            initialed && (ProxPreferences::preferences.isInitialized)
        } catch (ex: Exception) {
            false
        }
    }

    fun <T> valueOf(key: String, defaultValue: T): T {
        return when (defaultValue) {
            is Boolean -> {
                preferences.getBoolean(key, defaultValue)
            }
            is Int -> {
                preferences.getInt(key, defaultValue)
            }
            is Float -> {
                preferences.getFloat(key, defaultValue)
            }
            is Long -> {
                preferences.getLong(key, defaultValue)
            }
            is String -> {
                preferences.getString(key, defaultValue)
            }
            else -> {
                throw IllegalArgumentException("Generic type is not supported")
                1
            }
        } as T
    }


    fun <T> setValue(key: String, value: T) {
        when (value) {
            is Boolean -> {
                preferences.edit {
                    it.putBoolean(key, value)
                }
            }
            is Int -> {
                preferences.edit {
                    it.putInt(key, value)
                }
            }
            is Float -> {
                preferences.edit {
                    it.putFloat(key, value)
                }
            }
            is Long -> {
                preferences.edit {
                    it.putLong(key, value)
                }
            }
            is String -> {
                preferences.edit {
                    it.putString(key, value)
                }
            }
            else -> {
                throw IllegalArgumentException("$value type is not supported")
            }
        }
    }


}