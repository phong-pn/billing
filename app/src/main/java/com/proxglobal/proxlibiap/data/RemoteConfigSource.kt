package com.proxglobal.proxlibiap.data

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.get
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.gson.Gson
import com.proxglobal.proxlibiap.BuildConfig
import com.proxglobal.proxlibiap.data.sharepreference.ProxPreferences
import com.proxglobal.proxlibiap.sale.SaleEvent
import com.proxglobal.proxlibiap.sale.Script
import com.proxglobal.proxlibiap.util.DataState
import com.proxglobal.proxlibiap.util.logd

class RemoteConfigSource {
    private val TAG = "RemoteConfigSale"
    var remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig.apply {
        setConfigSettingsAsync(remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0 else 0
        })
    }

    internal var event: SaleEvent? = null

    internal var defaultEvent: SaleEvent? = null

    fun getScript(actionId: Int): Script? {
        var script = event?.saleScripts?.find { it.actionId == actionId }
        if (script == null) {
//            event?.subEvents?.forEach {
//                it.Scripts?.find { it.actionId == actionId }?.let {
//                    script = it
//                    return@forEach
//                }
//            }
        }
        return script
    }

    internal fun fetch(result: (DataState<SaleEvent>) -> Unit) {
        remoteConfig.fetchAndActivate().addOnCompleteListener {
            if (!it.isSuccessful) return@addOnCompleteListener
            val saleKeyConfigs = remoteConfig.getKeysByPrefix("sale").toList()
            saleKeyConfigs.logd()
//            val testSaleKeyConfig = saleKeyConfigs.filter { it.contains("sale")  && it.contains("test") }
//            testSaleKeyConfig.logd()
            if (saleKeyConfigs.size == 1) { // sale default
                val saleConfig = remoteConfig.getString(saleKeyConfigs.first())
                ProxPreferences.setValue("sale_default", saleConfig)
                event = getEvent(saleConfig)
                defaultEvent = event
            } else {
                // Have some sale-off event, so we need choose the best sale event match some condition. The sale event must
                // First, be started and not ended
                // Second, be enable
                // Third, if 2 or more sale-off event match conditions, choose the first. If have no sale-off event is chosen, choose the default sale-event
                val saleConfigs = saleKeyConfigs.map { remoteConfig.getString(it) }
                val saleEvents =
                    saleConfigs.mapIndexed { index, keyConfig ->
                        val event = getEvent(keyConfig)
                        if (saleKeyConfigs[index] != "sale_default") {
                            event.isSaleOff = true
                        } else {
                            defaultEvent = event
                            ProxPreferences.setValue("sale_default", keyConfig)
                        }
                        event
                    }.toMutableList()
                for (i in saleEvents.indices) {
                    if (i < saleEvents.size && saleEvents[i].enable == false) {
                        saleEvents.remove(saleEvents[i])
                    }
                }
                if (saleEvents.size > 2) {
                    // Remove default sale-event
                    saleEvents.remove(saleEvents.find { !it.isSaleOff })
                    event = saleEvents.first()
                } else {
                    event =
                        saleEvents.find { it.isSaleOff } // find the sale-off event. If null, find the default sale-event
                            ?: saleEvents.find { !it.isSaleOff }!!
                }
            }
            event!!.saleScripts.forEach { script ->
                if (script.saleContent == null) {
                    script.saleContent = event!!.saleDefaultContent
                }
            }
            result.invoke(DataState.Success(data = event))
        }.addOnFailureListener {
            it.logd()
            val saleDefault = ProxPreferences.valueOf("sale_default", "")
            if (saleDefault != "") {
                event = getEvent(saleDefault)
                defaultEvent = event
                result.invoke(DataState.Success(data = event))
            } else {
                result.invoke(DataState.Failure(message = "Can not get event. Please check your connection"))
            }
        }
    }

    internal fun fetchSaleEvent(result: (DataState<SaleEvent>) -> Unit) {
        remoteConfig.fetchAndActivate().addOnCompleteListener {
            if (!it.isSuccessful) return@addOnCompleteListener
            val saleKeyConfigs = remoteConfig.getKeysByPrefix("sale").toList()
            saleKeyConfigs.logd(TAG)
            val saleConfigs = saleKeyConfigs.associateWith { remoteConfig.getString(it) }
            saleConfigs.logd(TAG)

            val testSaleConfig = saleConfigs.filter { it.key.isTestSaleConfig }
            testSaleConfig.logd("$TAG test")
            val realSaleConfig = saleConfigs.filter { !it.key.isTestSaleConfig }
            realSaleConfig.logd("$TAG real")

            if (BuildConfig.DEBUG) {
                defaultEvent =
                    getSaleDefaultFromConfigs(testSaleConfig) ?: getSaleDefaultFromConfigs(
                        realSaleConfig
                    )
                (defaultEvent?.let { it.saleDefaultContent?.cta }
                    ?: "null").logd("$TAG defaultevent")
                defaultEvent?.let {
                    val configSaleDefault = getConfigsSaleDefaultFromConfigs(testSaleConfig)
                        ?: getConfigsSaleDefaultFromConfigs(realSaleConfig)
                    configSaleDefault?.logd()
                    ProxPreferences.setValue("test_sale_default", configSaleDefault)
                }
                event =
                    getEventFromConfigs(testSaleConfig) ?: getSaleDefaultFromConfigs(testSaleConfig)
                            ?: getEventFromConfigs(realSaleConfig) ?: getSaleDefaultFromConfigs(
                        realSaleConfig
                    )
                (event?.let { it.saleDefaultContent?.cta } ?: "null").logd("$TAG event")
                if (event == null) event = defaultEvent
                (event?.let { it.saleDefaultContent?.cta } ?: "null").logd("$TAG event")
            } else {
                defaultEvent = getSaleDefaultFromConfigs(realSaleConfig)
                (defaultEvent ?: "null").logd("$TAG defaultevent")
                defaultEvent?.let {
                    val keyConfigSaleDefault = getConfigsSaleDefaultFromConfigs(realSaleConfig)
                    ProxPreferences.setValue("sale_default", keyConfigSaleDefault)
                }
                event = getEventFromConfigs(realSaleConfig)
                if (event == null) event = defaultEvent
                (event ?: "null").logd("$TAG defaultevent")
            }
            result.invoke(DataState.Success(data = event))
        }.addOnFailureListener {
            val testSaleDefaultConfig = ProxPreferences.valueOf("test_sale_default", "")
            val realSaleDefaultConfig = ProxPreferences.valueOf("sale_default", "")
            if (BuildConfig.DEBUG) {
                if (testSaleDefaultConfig.isNotEmpty()) {
                    defaultEvent = getEvent(testSaleDefaultConfig)
                } else if (realSaleDefaultConfig.isNotEmpty()) {
                    defaultEvent = getEvent(realSaleDefaultConfig)
                }
            } else {
                if (realSaleDefaultConfig.isNotEmpty()) {
                    defaultEvent = getEvent(realSaleDefaultConfig)
                }
            }
            event = defaultEvent
            if (defaultEvent != null) result.invoke(DataState.Success(data = defaultEvent))
            else result.invoke(DataState.Failure(message = "Can not get event. Please check your connection"))
        }
    }

    private fun getSaleDefaultFromConfigs(saleConfigs: Map<String, String>): SaleEvent? {
        val defaultSaleConfig = saleConfigs.mapNotNull {
            if (it.key.isDefaultSaleConfig) it.value else null
        }
        return if (defaultSaleConfig.isEmpty()) null else {
            getEvent(defaultSaleConfig.first())
        }
    }

    private fun getConfigsSaleDefaultFromConfigs(saleConfigs: Map<String, String>): String? {
        val defaultSaleConfig = saleConfigs.mapNotNull {
            if (it.key.isDefaultSaleConfig) it.value else null
        }
        return if (defaultSaleConfig.isEmpty()) null else defaultSaleConfig.first()
    }

    private fun getEventFromConfigs(saleConfigs: Map<String, String>): SaleEvent? {
        if (saleConfigs.isEmpty()) return null
        if (saleConfigs.size == 1) { // sale default
            val saleConfig = saleConfigs.values.first()
            return if (!saleConfigs.keys.first().isDefaultSaleConfig) getEvent(saleConfig) else null

        } else {
            // Have some sale-off event, so we need choose the best sale event match some condition. The sale event must
            // First, be started and not ended
            // Second, be enable
            // Third, if 2 or more sale-off event match conditions, choose the first. If have no sale-off event is chosen, choose the default sale-event
            val saleEvents =
                saleConfigs.map {
                    val event = getEvent(it.value)
                    if (!it.key.isDefaultSaleConfig) {
                        event.isSaleOff = true
                    }
                    event
                }.toMutableList()
            for (i in saleEvents.indices) {
                if (i < saleEvents.size && saleEvents[i].enable == false) {
                    saleEvents.remove(saleEvents[i])
                }
            }
            if (saleEvents.size > 2) {
                // Remove default sale-event
                saleEvents.remove(saleEvents.find { !it.isSaleOff })
                event = saleEvents.first()
            } else {
                event = saleEvents.find { it.isSaleOff } // find the sale-off event
            }
            return event
        }
    }

    private val String.isTestSaleConfig: Boolean
        get() = contains("test") && contains("sale")

    private val String.isDefaultSaleConfig: Boolean
        get() = contains("sale_default")

    private fun getEvent(json: String): SaleEvent {
        val event = Gson().fromJson(json, SaleEvent::class.java)

//        event.subEvents?.forEach { subEvent ->
//            subEvent.isSaleOff = true
//            //copy all field of event into subEvent, if value of this field in subEvent = null.
//            event.javaClass.declaredFields.forEach {
//                it.isAccessible = true
//                if (it.get(subEvent) == null) {
//                    it.set(subEvent, it.get(event))
//                }
//            }
//            subEvent.Scripts?.forEach {
//                it.saleEventParent = subEvent
//                if (it.saleContent == null) it.saleContent = subEvent.saleDefaultContent
//            }
//        }
//
//        event.saleScripts?.forEach {
//            it.saleEventParent = event
//            if (it.saleContent == null) {
//                it.saleContent = event!!.saleDefaultContent
//            }
//        }

        return event
    }

    inline fun <reified T> valueOf(key: String): T {
        return when (T::class) {
            Boolean::class -> {
                remoteConfig.getBoolean(key)
            }
            Double::class -> {
                remoteConfig.getDouble(key)
            }
            Long::class -> {
                remoteConfig.getLong(key)
            }
            String::class -> {
                remoteConfig.getString(key)
            }
            else -> {
                remoteConfig.get(key)
            }
        } as T
    }

}