package com.proxglobal.proxlibiap

import android.app.Activity
import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.android.billingclient.api.*
import com.android.billingclient.api.Purchase.PurchaseState
import com.proxglobal.proxlibiap.model.BasePlanSubscription
import com.proxglobal.proxlibiap.model.OfferSubscription
import com.proxglobal.proxlibiap.model.OnetimeProduct
import com.proxglobal.proxlibiap.model.Subscription
import com.proxglobal.proxlibiap.util.findBasePlan
import com.proxglobal.proxlibiap.util.findOffers
import com.proxglobal.proxlibiap.util.logd
import com.proxglobal.proxlibiap.util.loge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProxPurchase private constructor() : DefaultLifecycleObserver, PurchasesUpdatedListener,
    BillingClientStateListener {
    companion object {
        val instance by lazy { ProxPurchase() }
    }

    private lateinit var billingClient: BillingClient

    private val listSubscriptionId = arrayListOf<String>("lib_iap_premium")
    private val listOneTimeProductId = arrayListOf<String>()
    private val listConsumableId = arrayListOf<String>()

    private val productDetailMap = hashMapOf<String, ProductDetails>()

    private val scope = CoroutineScope(Dispatchers.Default)

    private val ownedProducts = mutableSetOf<String>()

    private var listPurchaseUpdateListener = arrayListOf<PurchaseUpdateListener>()

    private var syncPurchased = false

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        queryPurchases()
    }

    private fun onOwned(productId: String, transactionDetails: String) {
        // Try to add productId to ownedProducts. If success, listOwnedProductListener will be invoke
        val addSuccess = ownedProducts.add(productId)
        if (addSuccess) {
            listPurchaseUpdateListener.forEach {
                it.onProductPurchased(productId, transactionDetails)
            }
        }
    }

    fun addPurchaseUpdateListener(listener: PurchaseUpdateListener) {
        listPurchaseUpdateListener.add(listener)
    }

    fun init(context: Context) {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        logd("billing client initializing...")
        if (!billingClient.isReady) startConnection()
    }

    fun addOneTimeProductId(listId: List<String>) {
        listOneTimeProductId.addAll(listId)
        queryProductDetails()
    }

    fun addSubscriptionId(listId: List<String>) {
        listSubscriptionId.addAll(listId)
        queryProductDetails()
    }

    fun startConnection() {
        billingClient.startConnection(this)
    }

    override fun onBillingServiceDisconnected() {
        logd("Billing client is disconnected")
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        logd("onBillingSetupFinished: $responseCode $debugMessage")
        if (responseCode == BillingClient.BillingResponseCode.OK) {
            // The billing client is ready.
            // You can query product details and purchases here.
            queryProductDetails()
            queryPurchases()
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
        for (id in listSubscriptionId) {
            subscriptionList.add(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(id)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )
        }

        val productDetailParams = QueryProductDetailsParams.newBuilder()
        val productList = arrayListOf<QueryProductDetailsParams.Product>()
        listOneTimeProductId.forEach { id ->
            productList.add(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(id)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )
        }

        scope.launch {
            logd("Querying async ProductDetail ...")
            //Query ProductDetail for subscriptions
            if (subscriptionList.isNotEmpty()) {
                subDetailParams.setProductList(subscriptionList).let { params ->
                    val result = billingClient.queryProductDetails(params.build())
                    this@ProxPurchase.logd("Query subscription detail response ${result.billingResult.responseCode}")
                    val response = BillingResponse(result.billingResult.responseCode)
                    if (response.isOk) {
                        result.productDetailsList?.size?.logd()
                        result.productDetailsList?.forEach { productDetail ->
                            //store detail
                            productDetail.logd()
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
                    this@ProxPurchase.logd("Query product detail response ${result.billingResult.responseCode}")
                    val response = BillingResponse(result.billingResult.responseCode)
                    if (response.isOk) {
                        result.productDetailsList?.forEach { productDetail ->
                            productDetail.logd()
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
        if (!billingClient.isReady) {
            loge("queryPurchases: BillingClient is not ready")
            billingClient.startConnection(this)
        }
        scope.launch {
            val subPurchaseResult = billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )
            if (BillingResponse(subPurchaseResult.billingResult.responseCode).isOk) {
                processPurchase(subPurchaseResult.purchasesList)
            }
            val productPurchaseResult = billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )
            if (BillingResponse(productPurchaseResult.billingResult.responseCode).isOk) {
                processPurchase(productPurchaseResult.purchasesList)
            }
            syncPurchased = true
        }

    }

    private fun processPurchase(purchases: List<Purchase>?) {
        if (!purchases.isNullOrEmpty()) {
            for (purchase in purchases) {
                if (purchase.purchaseState == PurchaseState.PURCHASED || purchase.purchaseState == PurchaseState.PENDING) {
                    //Grant entitlement to the user.
                    purchase.products.forEach { productId ->
                        val detail = productDetailMap[productId]
                        when (detail?.productType) {
                            BillingClient.ProductType.INAPP -> {
                                //consume purchase
                                if (listConsumableId.contains(productId)) consumePurchase(
                                    productId,
                                    purchase.purchaseToken,
                                    purchase.originalJson
                                )
                                else onOwned(productId, purchase.originalJson)
                            }
                            BillingClient.ProductType.SUBS -> {
                                onOwned(productId, purchase.originalJson)
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

    private fun consumePurchase(
        productId: String,
        purchaseToken: String,
        transactionDetails: String
    ) {
        val consumeParams =
            ConsumeParams.newBuilder()
                .setPurchaseToken(purchaseToken)
                .build()
        scope.launch(Dispatchers.IO) {
            val result = billingClient.consumePurchase(consumeParams)
            val response = BillingResponse(result.billingResult.responseCode)
            if (response.isOk) {
                onOwned(productId, transactionDetails)
            } else {
                loge("Consume purchase failure with code: ${result.billingResult.responseCode}")
            }

        }
    }

    private fun acknowledgePurchase(purchaseToken: String) {
        scope.launch {
            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchaseToken).build()
            val result = billingClient.acknowledgePurchase(acknowledgePurchaseParams)
            val response = BillingResponse(result.responseCode)
            if (response.isOk) {
            } else {
                loge("Consume purchase failure with code: ${result.responseCode}")
            }
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
                        logd("onPurchasesUpdated: null purchase list")
                        processPurchase(null)
                    } else {
                        processPurchase(purchases)
                    }
                }
                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    logd("onPurchaseUpdate: User canceled billing")
                    listPurchaseUpdateListener.forEach {
                        it.onUserCancelBilling()
                    }
//                    Log.i(TAG, "onPurchasesUpdated: User canceled the purchase")
                }
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
//                    Log.i(TAG, "onPurchasesUpdated: The user already owns this item")
                    logd("onPurchasesUpdated: The user already owns this item")
                    listPurchaseUpdateListener.forEach {
                        it.onPurchaseFailure(responseCode, "The user already owns this item")
                    }
                }
                BillingClient.BillingResponseCode.DEVELOPER_ERROR -> {
                    logd("developer error")
                    listPurchaseUpdateListener.forEach {
                        it.onPurchaseFailure(
                            responseCode, "Developer error means that Google Play " +
                                    "does not recognize the configuration. If you are just getting started, " +
                                    "make sure you have configured the application correctly in the " +
                                    "Google Play Console. The product ID must match and the APK you " +
                                    "are using must be signed with release keys."
                        )
                    }
//                    Log.e(
//                        TAG, "onPurchasesUpdated: Developer error means that Google Play " +
//                                "does not recognize the configuration. If you are just getting started, " +
//                                "make sure you have configured the application correctly in the " +
//                                "Google Play Console. The product ID must match and the APK you " +
//                                "are using must be signed with release keys."
//                    )
                }
                else -> {
                    listPurchaseUpdateListener.forEach {
                        it.onPurchaseFailure(responseCode, "An error occur")
                    }
                }
            }
        }
    }

    fun subscribe(activity: Activity, subscription: Subscription) {
        val productDetails = productDetailMap[subscription.productId]
        productDetails?.let {
            launchBillingFlow(activity, productDetails, subscription.token)
        } ?: "Can not get product Details. Please check productId of subs: $subscription".loge()
    }

    fun purchase(activity: Activity, onetimeProduct: OnetimeProduct) {
        val productDetails = productDetailMap[onetimeProduct.productId]
        productDetails?.let {
            launchBillingFlow(activity, productDetails)
        }
            ?: "Can not get product Details. Please check productId of oneTimeProduct: $onetimeProduct".loge()
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
        billingClient.launchBillingFlow(activity, billingParams)
    }

    fun getBasePlan(subscriptionId: String, tags: List<String>): BasePlanSubscription? {
        val productDetails = productDetailMap[subscriptionId]
        return productDetails?.findBasePlan(tags)
    }

    fun getOfferSubscription(
        subscriptionId: String,
        basePlanTags: List<String>,
        offerTags: List<String>
    ): List<OfferSubscription> {
        val productDetails = productDetailMap[subscriptionId]
        return when (val basePlan = productDetails?.findBasePlan(basePlanTags)) {
            null -> listOf()
            else -> productDetails.findOffers(basePlan, offerTags)
        }
    }

    fun getOfferSubscription(
        basePlan: BasePlanSubscription,
        offerTags: List<String>
    ): List<OfferSubscription> {
        val productDetails = productDetailMap[basePlan.productId]
        return productDetails?.findOffers(basePlan, offerTags) ?: listOf()
    }

    fun getOneTimeProduct(productId: String): OnetimeProduct? {
        val productDetails = productDetailMap[productId]
        return productDetails?.oneTimePurchaseOfferDetails?.let {
            OnetimeProduct(productId, it.formattedPrice, it.priceCurrencyCode)
        }
    }

    fun checkPurchased(): Boolean {
        if (!syncPurchased) {
            queryPurchases()
            return ownedProducts.size > 0
        } else return ownedProducts.size > 0
    }
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
