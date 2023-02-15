package com.proxglobal.purchase.billing

import android.app.Activity
import android.content.Context
import com.proxglobal.purchase.PurchaseUpdateListener
import com.proxglobal.purchase.model.BasePlanSubscription
import com.proxglobal.purchase.model.OfferSubscription
import com.proxglobal.purchase.model.OnetimeProduct
import com.proxglobal.purchase.model.Subscription
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

    private val initFinishListener = arrayListOf<(Boolean) -> Unit>()

    fun addPurchaseUpdateListener(listener: PurchaseUpdateListener) {
        billingService.addPurchaseUpdateListener(listener)
    }

    fun addInitBillingFinishListener(listener: (Boolean) -> Unit) {
        initFinishListener.add(listener)
        if (isAvailable) listener(isAvailable)
    }

    private var purchaseIdFetchSuccess = false
    private var initBillingFinish = false

    val isAvailable: Boolean
        get() = purchaseIdFetchSuccess && initBillingFinish

    fun init(
        context: Context
    ) {
        billingService.initBillingClient(context)
//        PurchaseIdSource().fetch {
//            logDebug("Fetch success product id, subscriptions id = ${it.listSubsId}, one time product id = ${it.listOneTimeProductId}")
//            billingService.addSubscriptionId(it.listSubsId)
//            billingService.addOneTimeProductId(it.listOneTimeProductId)
//            purchaseIdFetchSuccess = true
//            if (isAvailable)
//                CoroutineScope(Dispatchers.IO).launch {
//                    delay(500)
//                    initFinishListener.forEach {
//                        it.invoke(isAvailable)
//                    }
//                }
//        }
    }

    private fun onInitBillingFinish() {
        initBillingFinish = true
        if (isAvailable) {
            CoroutineScope(Dispatchers.IO).launch {
                delay(500)
                initFinishListener.forEach {
                    it.invoke(isAvailable)
                }
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
     * Subscribe a subscription. [subscription] can be a [BasePlanSubscription] or [OfferSubscription].
     * Result will be update with [PurchaseUpdateListener]
     * @param activity: activity that start billing popup
     * @param subscription: a [BasePlanSubscription] or [OfferSubscription]
     *
     */
    fun subscribe(activity: Activity, subscription: Subscription) {
        billingService.subscribe(activity, subscription)
    }

    /**
     * Purchase a [OnetimeProduct]. Result will be update with [PurchaseUpdateListener]
     */
    fun purchase(activity: Activity, onetimeProduct: OnetimeProduct) {
        billingService.purchase(activity, onetimeProduct)
    }


    /**
     * Return a [BasePlanSubscription] have id is [basePlanId], and be owned by a subscription have product id is [subscriptionId].
     * Base plan and Offer is come from Google Play Billing Library. For more information, read
     * this [article](https://support.google.com/googleplay/android-developer/answer/12154973?hl=vi&ref_topic=3452890)
     * @param subscriptionId id of subscription that owned base plan
     * @param basePlanId id of base plan
     */
    fun getBasePlan(subscriptionId: String, basePlanId: String) =
        billingService.getBasePlan(subscriptionId, basePlanId)

    fun getBasePlan(basePlanId: String) =
        billingService.getBasePlan(basePlanId)

    /**
     * Return a [OfferSubscription] have id is [offerId], and be owned by a subscription have product id is [subscriptionId].
     * Base plan and Offer is come from Google Play Billing Library. For more information, read
     * this [article](https://support.google.com/googleplay/android-developer/answer/12154973?hl=vi&ref_topic=3452890)
     * @param subscriptionId id of subscription that owned base plan
     * @param basePlanId id of base plan
     */
    fun getOffer(subscriptionId: String, basePlanId: String, offerId: String) =
        billingService.getOffer(subscriptionId, basePlanId, offerId)

    fun getOffer(basePlanId: String, offerId: String) =
        billingService.getOffer(basePlanId, offerId)




    fun getOneTimeProduct(id: String): OnetimeProduct {
        return billingService.getOneTimeProduct(id)
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