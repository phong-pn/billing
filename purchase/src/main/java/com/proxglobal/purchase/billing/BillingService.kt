package com.proxglobal.purchase.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.android.billingclient.api.Purchase.PurchaseState
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.proxglobal.purchase.PurchaseUpdateListener
import com.proxglobal.purchase.data.sharepreference.ProxPreferences
import com.proxglobal.purchase.model.BasePlanSubscription
import com.proxglobal.purchase.model.OfferSubscription
import com.proxglobal.purchase.model.OnetimeProduct
import com.proxglobal.purchase.model.Subscription
import com.proxglobal.purchase.util.findBasePlan
import com.proxglobal.purchase.util.logd
import com.proxglobal.purchase.util.logeSelf
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

internal class BillingService private constructor() : PurchasesUpdatedListener,
    BillingClientStateListener, CoroutineScope {
    companion object {
        private val instan by lazy { BillingService() }

        @JvmStatic
        internal fun getInstance() = instan
    }

    private val TAG = "Prox_Purchase"
    private fun logDebug(message: String? = null) {
        logd(message, TAG)
    }

    private lateinit var billingClient: BillingClient

    private val setSubscriptionId = mutableSetOf<String>()
    private val setOneTimeProductId = mutableSetOf<String>()
    private val listConsumableId = arrayListOf<String>()

    private val productDetailMap = hashMapOf<String, ProductDetails>()

    private val scope = CoroutineScope(Dispatchers.Default)

    private val ownedProducts = mutableSetOf<String>()

    private var listPurchaseUpdateListener = arrayListOf<PurchaseUpdateListener>()
    internal var onInitBillingFinish: (() -> Unit)? = null

    private val cachedOwnedProducts = mutableSetOf<String>()
    private var syncPurchased = false

    private var queueRunnable = ArrayDeque<Deferred<*>>()

    init {
        val ownedProduct = Gson().fromJson<List<String>>(
            ProxPreferences.valueOf("ownedProduct", ""),
            object : TypeToken<List<String>>() {}.type
        )
        if (ownedProduct != null) {
            logDebug("Found ownedProduct in ProxPreferences: $ownedProduct")
            cachedOwnedProducts.addAll(ownedProduct)
        }
    }

    private fun onOwned(productId: String) {
        // Try to add productId to ownedProducts. If success, listOwnedProductListener will be invoke
        val addSuccess = ownedProducts.add(productId)
        if (addSuccess) {
            logDebug("productId was add $productId")
            onOwnedProduct(productId)
        }
        logDebug("ownProducts size: ${ownedProducts.size}, details: = $ownedProducts")
    }

    fun addPurchaseUpdateListener(listener: PurchaseUpdateListener) {
        listPurchaseUpdateListener.add(listener)
    }

    fun initBillingClient(context: Context) {
        logDebug("billing client initializing...")
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        if (!billingClient.isReady) startConnection()
    }

    /**
     * Add one time product. After add success, all product and purchase will be query again
     */
    fun addOneTimeProductId(listId: List<String>) {
        setOneTimeProductId.addAll(listId)
        if (!billingClient.isReady) {
            startConnection()
        } else {
            queryProductDetails()
            runBlocking {
                queryPurchases()
            }
        }
    }

    /**
     * Add subscription. After add success, all product and purchase will be query again
     */
    fun addSubscriptionId(listId: List<String>) {
        setSubscriptionId.addAll(listId)
        if (!billingClient.isReady) {
            startConnection()
        } else {
            queryProductDetails()
            runBlocking {
                queryPurchases()
            }
        }
    }

    /**
     * Connect billing client
     */
    fun startConnection() {
        billingClient.startConnection(this)
    }

    override fun onBillingServiceDisconnected() {
        logDebug("Billing client is disconnected")
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        logDebug("onBillingSetupFinished: $responseCode $debugMessage")
        if (responseCode == BillingClient.BillingResponseCode.OK) {
            // The billing client is ready.
            // You can query product details and purchases here.
            queryProductDetails()
            runBlocking {
                queryPurchases()
            }
            onInitBillingFinish?.invoke()
        }
    }

    /**
     * In order to make purchases, you need the [ProductDetails] for the item or subscription.
     * This is an asynchronous call that will receive a result in [onProductDetailsResponse].
     *
     * queryProductDetails uses method calls from GPBL 5.0.0. PBL5, released in May 2022,
     * is backwards compatible with previous versions.
     * To learn more about this you can read https://developer.android.com/google/play/billing/compatibility
     */
    private fun queryProductDetails() {

        val subDetailParams = QueryProductDetailsParams.newBuilder()

        val subscriptionList: MutableList<QueryProductDetailsParams.Product> = arrayListOf()
        for (id in setSubscriptionId) {
            subscriptionList.add(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(id)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )
        }

        val productDetailParams = QueryProductDetailsParams.newBuilder()
        val productList = arrayListOf<QueryProductDetailsParams.Product>()
        setOneTimeProductId.forEach { id ->
            productList.add(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(id)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )
        }

        runBlocking {
            logDebug("Querying async ProductDetail ...")
            //Query ProductDetail for subscriptions
            if (subscriptionList.isNotEmpty()) {
                subDetailParams.setProductList(subscriptionList).let { params ->
                    val result = billingClient.queryProductDetails(params.build())
                    logDebug("Query subscription detail response ${result.billingResult.responseCode} ${result.billingResult.debugMessage}")
                    val response = BillingResponse(result.billingResult.responseCode)
                    if (response.isOk) {
                        logDebug("Found ${result.productDetailsList?.size} subscription")
                        result.productDetailsList?.forEach { productDetail ->
                            //store detail
                            logDebug("Product details: $productDetail")
                            productDetailMap[productDetail.productId] = productDetail
                        }
                    }
                }
            }

            //Query ProductDetail for products
            if (productList.isNotEmpty()) {
                productDetailParams.setProductList(productList).let { params ->
                    billingClient.queryProductDetails(params.build())
                    val result = billingClient.queryProductDetails(params.build())
                    logDebug("Query product detail response ${result.billingResult.responseCode}")
                    val response = BillingResponse(result.billingResult.responseCode)
                    if (response.isOk) {
                        logDebug("Found ${result.productDetailsList?.size} one time product")
                        result.productDetailsList?.forEach { productDetail ->
                            logDebug(productDetail.toString())
                            // store detail
                            productDetailMap[productDetail.productId] = productDetail
                        }
                    }
                }
            }
        }
    }

    /**
     * Query Google Play Billing for existing purchases.
     *
     * New purchases will be provided to the PurchasesUpdatedListener.
     * You still need to check the Google Play Billing API to know when purchase tokens are removed.
     */
    fun queryPurchases() {
        logDebug("Querying Purchases....")
        runBlocking {
            if (!billingClient.isReady) {
                logDebug("queryPurchases: BillingClient is not ready")
                billingClient.startConnection(this@BillingService)
                return@runBlocking
            }
            val subPurchaseResult = billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )
            if (BillingResponse(subPurchaseResult.billingResult.responseCode).isOk) {
                logDebug("Query subscription purchases....")
                processPurchase(subPurchaseResult.purchasesList)
            }
            val productPurchaseResult = billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )
            if (BillingResponse(productPurchaseResult.billingResult.responseCode).isOk) {
                logDebug("Query product purchases....")
                processPurchase(productPurchaseResult.purchasesList)
            }
            logDebug("Finish query purchase.... Found ${ownedProducts.size} purchases")
            ProxPreferences.setValue("ownedProduct", Gson().toJson(ownedProducts.toMutableList()))
            syncPurchased = true
        }
    }

    private suspend fun processPurchase(purchases: List<Purchase>?) {
        if (!purchases.isNullOrEmpty()) {
            logDebug("Process purchases. size of purchases: ${purchases.size}...")
            for (purchase in purchases) {
                logDebug("Purchase state: ${purchase.purchaseState}")
                if (purchase.purchaseState == PurchaseState.PURCHASED || purchase.purchaseState == PurchaseState.PENDING) {
                    //Grant entitlement to the user.
                    purchase.products.forEach { productId ->
                        val detail = productDetailMap[productId]
                        logDebug("Product id: $productId, type: ${detail?.productType}")
                        when (detail?.productType) {
                            BillingClient.ProductType.INAPP -> {
                                //consume purchase
                                if (listConsumableId.contains(productId)) consumePurchase(
                                    productId,
                                    purchase.purchaseToken
                                )
                                else onOwned(productId)
                            }
                            BillingClient.ProductType.SUBS -> {
                                onOwned(productId)
                            }
                        }
                        // If the state is PURCHASED, acknowledge the purchase if it hasn't been acknowledged yet.
                        if (!purchase.isAcknowledged && purchase.purchaseState == PurchaseState.PURCHASED) {
                            acknowledgePurchase(purchase.purchaseToken)
                        }
                    }
                }
            }
        }
    }

    private suspend fun consumePurchase(
        productId: String,
        purchaseToken: String
    ) {
        val consumeParams =
            ConsumeParams.newBuilder()
                .setPurchaseToken(purchaseToken)
                .build()
        val result = billingClient.consumePurchase(consumeParams)
        val response = BillingResponse(result.billingResult.responseCode)
        if (response.isOk) {
            onOwned(productId)
        } else {
            logDebug("Consume purchase failure with code: ${result.billingResult.responseCode}")
        }


    }

    private suspend fun acknowledgePurchase(purchaseToken: String) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken).build()
        val result = billingClient.acknowledgePurchase(acknowledgePurchaseParams)
        val response = BillingResponse(result.responseCode)
        if (response.isOk) {
            logDebug("Acknowledge purchase success with code: ${result.responseCode}")
        } else {
            logDebug("Acknowledge purchase failure with code: ${result.responseCode}")
        }

    }

    fun end() {
        billingClient.endConnection()
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        scope.launch {
            when (responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    if (purchases == null) {
                        processPurchase(null)
                    } else {
                        processPurchase(purchases)
                        purchases.forEach {
                            it.products.forEach {
                                onProductPurchased(it)
                            }
                        }
                    }
                }
                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    onUserCancelBilling()
                }
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                    onPurchaseFailure(responseCode, "The user already owns this item")
                    queryPurchases()
                }
                BillingClient.BillingResponseCode.DEVELOPER_ERROR -> {
                    logDebug("developer error")
                    onPurchaseFailure(
                        responseCode, "Developer error means that Google Play " +
                                "does not recognize the configuration. If you are just getting started, " +
                                "make sure you have configured the application correctly in the " +
                                "Google Play Console. The product ID must match and the APK you " +
                                "are using must be signed with release keys."
                    )
//                    Log.e(
//                        TAG, "onPurchasesUpdated: Developer error means that Google Play " +
//                                "does not recognize the configuration. If you are just getting started, " +
//                                "make sure you have configured the application correctly in the " +
//                                "Google Play Console. The product ID must match and the APK you " +
//                                "are using must be signed with release keys."
//                    )
                }
                else -> {
                    onPurchaseFailure(responseCode, "An error occur")
                }
            }
        }
    }

    /**
     * Subscribe a [Subscription]. Result will be update with [PurchaseUpdateListener]
     * @param activity An Activity starting default billing activity of Google
     * @param subscription [Subscription] need purchased
     */
    fun subscribe(activity: Activity, subscription: Subscription) {
        val productDetails = productDetailMap[subscription.productId]
        productDetails?.let {
            launchBillingFlow(activity, productDetails, subscription.token)
        } ?: "Can not get product Details. Please check productId of subs: $subscription".logeSelf()
    }

    /**
     * Purchase a [OnetimeProduct]. Result will be update with [PurchaseUpdateListener]
     */
    fun purchase(activity: Activity, onetimeProduct: OnetimeProduct) {
        val productDetails = productDetailMap[onetimeProduct.productId]
        productDetails?.let {
            launchBillingFlow(activity, productDetails)
        }
            ?: "Can not get product Details. Please check productId of oneTimeProduct: $onetimeProduct".logeSelf()
    }

    private fun launchBillingFlow(
        activity: Activity,
        productDetails: ProductDetails,
        token: String? = null
    ) {
        val params = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
        token?.let { params.setOfferToken(it) }
        val billingParams = BillingFlowParams.newBuilder().setProductDetailsParamsList(
            listOf(params.build())
        ).build()
        val result = billingClient.launchBillingFlow(activity, billingParams)
    }

    /**
     * Return a [BasePlanSubscription] have tags is [basePlanTags], and be owned by a subscription have product id is [subscriptionId].
     * Base plan and Offer is come from Google Play Billing Library. For more information, read
     * this [article](https://support.google.com/googleplay/android-developer/answer/12154973?hl=vi&ref_topic=3452890)
     * @param subscriptionId id of subscription that owned base plan
     * @param basePlanTags list tags of base plan
     */
    fun getBasePlan(
        subscriptionId: String,
        basePlanId: String
    ): BasePlanSubscription {
        var productDetails = productDetailMap[subscriptionId]
        if (productDetails == null) {
            addSubscriptionId(listOf(subscriptionId))
            productDetails = productDetailMap[subscriptionId]
        }
        return productDetails?.findBasePlan(basePlanId) ?: throw NullPointerException(
            "Not found subscription that has id = $subscriptionId. Maybe missing add this subscription with ProxPurchase, or you need waiting more for ProxPurchase's initiation"
        )
    }

    fun getBasePlan(basePlanId: String): BasePlanSubscription {
        for (it in productDetailMap.values) {
            if (it.subscriptionOfferDetails != null) {
                val basePlan = it.findBasePlan(basePlanId)
                if (basePlan != null) {
                    return basePlan
                }
            }
        }
        throw NullPointerException(
            "Not found any basePlan that has id = $basePlanId. Maybe missing add subscription with ProxPurchase, or you need waiting more for ProxPurchase's initiation"
        )
    }

    fun getOffer(
        subscriptionId: String,
        basePlanId: String,
        offerId: String
    ): OfferSubscription? = getBasePlan(subscriptionId, basePlanId).offers.find { it.id == offerId }

    fun getOffer(
        basePlanId: String,
        offerId: String
    ) = getBasePlan(basePlanId).offers.find { it.id == offerId }

    fun getOneTimeProduct(oneTimeProductId: String): OnetimeProduct {
        var productDetails = productDetailMap[oneTimeProductId]
        if (productDetails == null) {
            addOneTimeProductId(listOf(oneTimeProductId))
            productDetails = productDetailMap[oneTimeProductId]
        } else {
            if (productDetails.oneTimePurchaseOfferDetails == null) {
                throw NullPointerException(
                    "Not found product that has id = ${oneTimeProductId}. Maybe missing add this subscription with ProxPurchase, or you need waiting more for ProxPurchase's initiation"
                )
            }
        }
        return productDetails?.oneTimePurchaseOfferDetails!!.let {
            OnetimeProduct(oneTimeProductId, it.formattedPrice, it.priceCurrencyCode)
        }
    }

    /**
     * Return true if user is owning at least one product or available subscription
     */
    fun checkPurchased(): Boolean {
        if (!syncPurchased) {
            queryPurchases()
        }
        return if (!syncPurchased) cachedOwnedProducts.size > 0 else ownedProducts.size > 0
    }

    /**
     * Check whether product have [productId] is owned by user
     */
    fun isPurchased(productId: String): Boolean {
        if (!syncPurchased) {
            queryPurchases()
        }
        return if (!syncPurchased) cachedOwnedProducts.contains(productId)
        else ownedProducts.contains(productId)
    }

    private fun onOwnedProduct(productId: String) {
        logDebug("onOwnedProduct: onOwned: $productId")
        listPurchaseUpdateListener.forEach {
            try {
                it.onOwnedProduct(productId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun onProductPurchased(productId: String) {
        logDebug("onPurchaseUpdate: User purchase product: $productId")
        listPurchaseUpdateListener.forEach {
            try {
                logDebug("listener ongoing...")
                it.onProductPurchased(productId)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }


    private fun onUserCancelBilling() {
        logDebug("User canceled billing")
        listPurchaseUpdateListener.forEach {
            try {
                it.onUserCancelBilling()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun onPurchaseFailure(code: Int, errorMsg: String?) {
        logDebug("Purchase failure: $errorMsg")
        listPurchaseUpdateListener.forEach {
            try {
                it.onPurchaseFailure(code, errorMsg)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default
}

@JvmInline
private value class BillingResponse(val code: Int) {
    val isOk: Boolean
        get() = code == BillingClient.BillingResponseCode.OK
    val canFailGracefully: Boolean
        get() = code == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED
    val isRecoverableError: Boolean
        get() = code in setOf(
            BillingClient.BillingResponseCode.ERROR,
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
        )
    val isNonrecoverableError: Boolean
        get() = code in setOf(
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
            BillingClient.BillingResponseCode.DEVELOPER_ERROR,
        )
    val isTerribleFailure: Boolean
        get() = code in setOf(
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE,
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED,
            BillingClient.BillingResponseCode.ITEM_NOT_OWNED,
            BillingClient.BillingResponseCode.USER_CANCELED,
        )
}
