package com.proxglobal.purchase.billing

import android.app.Activity
import android.content.Context
import com.proxglobal.purchase.PurchaseUpdateListener
import com.proxglobal.purchase.data.sharepreference.ProxPreferences
import com.proxglobal.purchase.model.BasePlanSubscription
import com.proxglobal.purchase.model.OfferSubscription
import com.proxglobal.purchase.model.OnetimeProduct
import com.proxglobal.purchase.util.logd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ProxPurchase private constructor() {
    private val TAG = "Prox_Purchase"

    private fun logDebug(message: String? = null) {
        logd(message, TAG)
    }


    companion object {
        private val INSTANCE: ProxPurchase by lazy { ProxPurchase() }

        fun getInstance() = INSTANCE
    }

    private val billingService by lazy {
        BillingService.getInstance().apply {
            onInitBillingFinish = {
                onInitBillingFinish()
            }
        }
    }

    private val initFinishListener = arrayListOf<() -> Unit>()

    fun addPurchaseUpdateListener(listener: PurchaseUpdateListener) {
        billingService.addPurchaseUpdateListener(listener)
    }

    fun addInitBillingFinishListener(listener: () -> Unit) {
        initFinishListener.add(listener)
        if (isAvailable) listener()
    }

    private var initBillingFinish = false

    var isAvailable = false
    fun init(
        context: Context
    ) {
        ProxPreferences.init(context)
        billingService.initBillingClient(context)
    }

    private fun onInitBillingFinish() {
        initBillingFinish = true
        isAvailable = true
        CoroutineScope(Dispatchers.IO).launch {
            delay(200)
            initFinishListener.forEach {
                it.invoke()
            }
        }
    }

    fun addOneTimeProductId(listId: List<String>) {
        billingService.addOneTimeProductId(listId)
    }

    /**
     * Add subscription. After add success, all product and purchase will be query again
     */
    fun addSubscriptionId(listId: List<String>) {
        billingService.addSubscriptionId(listId)
    }

    fun addConsumableId(listId: List<String>) {
        billingService.addConsumableProductId(listId)
    }

    /**
     * Connect billing client
     */
    fun startConnection() {
        billingService.startConnection()
    }

    /**
     * Query and update information of all product that user purchased and still valid
     */
    fun queryPurchases() {
        billingService.queryPurchases()
    }

    fun end() {
        billingService.end()
    }

    /**
     * Subscribe a subscription, or purchase a oneTimeProduct.
     *
     * Result will be update with [PurchaseUpdateListener]
     * @param activity: activity that start billing popup
     * @param id can be a [BasePlanSubscription.basePlanId], or [OfferSubscription.offerId], or [OnetimeProduct.productId]
     *
     */
    fun buy(activity: Activity, id: String) {
        billingService.buy(activity, id)
    }



    fun getBasePlan(basePlanId: String) =
        billingService.getBasePlan(basePlanId)

    fun getOffer(offerId: String) =
        billingService.getOffer(offerId)

    fun getOneTimeProduct(id: String): OnetimeProduct {
        return billingService.getOneTimeProduct(id)
    }

    fun getPrice(id: String): String {
        return billingService.getPrice(id)
    }

    fun getDiscountPrice(id: String): String {
        return billingService.getDiscountPrice(id)
    }

    /**
     * Return true if user is owning at least one product or available subscription
     */
    fun checkPurchased(): Boolean {
        return billingService.checkPurchased()
    }

    /**
     * Check whether product have [productId] is owned by user
     */
    fun isPurchased(productId: String): Boolean {
        return billingService.isPurchased(productId)
    }
}