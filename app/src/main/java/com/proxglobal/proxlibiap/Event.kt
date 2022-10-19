package com.proxglobal.proxlibiap

import java.lang.ref.WeakReference

class Event<T: Any?>(private val value: T) {
    private var consumers = mutableListOf<WeakReference<Any>>()
    fun getValueIfNotHandle(consumer: Any?): T? {
        var isConsumerExisted = false
        consumers.forEach {
            if (it.get() == consumer) {
                isConsumerExisted = true
                return@forEach
            }
        }
        return if (isConsumerExisted) null else {
            consumers.add(WeakReference(consumer))
            value
        }
    }
    fun getValue() = value
}

fun<T> eventOf(value: T) = Event(value)