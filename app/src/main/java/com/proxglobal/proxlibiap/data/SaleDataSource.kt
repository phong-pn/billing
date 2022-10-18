package com.example.sale_lib.sale.data

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import com.proxglobal.proxlibiap.BuildConfig
import com.proxglobal.proxlibiap.data.sharepreference.ProxPreferences
import com.proxglobal.proxlibiap.sale.SaleEvent
import com.proxglobal.proxlibiap.sale.Script
import com.proxglobal.proxlibiap.util.DataState
import com.proxglobal.proxlibiap.util.logd

class SaleDataSource(private val remoteConfig: FirebaseRemoteConfig) {
    private val TAG = "RemoteConfigSale"



    internal var event: SaleEvent? = null

    internal var defaultEvent: SaleEvent? = null

    fun getScript(actionId: Int): Script? {
        return event?.saleScripts?.find { it.actionId == actionId }
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
        return event
    }
}