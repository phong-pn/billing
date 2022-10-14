package com.proxglobal.proxlibiap

import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import com.android.billingclient.api.*
import com.android.billingclient.api.ProductDetails.OneTimePurchaseOfferDetails
import com.android.billingclient.api.ProductDetails.SubscriptionOfferDetails
import com.android.billingclient.api.Purchase.PurchaseState
import com.proxglobal.proxlibiap.util.logd
import com.proxglobal.proxlibiap.util.loge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BillingManager private constructor(): DefaultLifecycleObserver, PurchasesUpdatedListener,
    BillingClientStateListener {
    companion object {
        val instance by lazy { BillingManager() }
    }
    private lateinit var billingClient: BillingClient

    private val listSubscriptionId = arrayListOf<String>("lib_iap_premium")
    private val listProductId = arrayListOf<String>()

    private val productDetailMap = hashMapOf<String, ProductDetails>()
    private val subscriptionOfferDetailMap = hashMapOf<String, SubscriptionOfferDetails>()
    private val oneTimePurchaseOfferDetailMap = hashMapOf<String, OneTimePurchaseOfferDetails>()

    private val scope = CoroutineScope(Dispatchers.Default)

    fun init(context: Context) {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        logd("billing client initializing...")
        if (!billingClient.isReady) startConnection()
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
        listProductId.forEach { id ->
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
                    this@BillingManager.logd("Query subscription detail response ${result.billingResult.responseCode}")
                    val response = BillingResponse(result.billingResult.responseCode)
                    if (response.isOk) {
                        result.productDetailsList?.size?.logd()
                        result.productDetailsList?.forEach { productDetail ->
                            //store detail
                            productDetail.logd()
                            productDetailMap[productDetail.productId] = productDetail
                            //store all offers and base plans of this subscription
                            if (productDetail.productType == BillingClient.ProductType.SUBS) {
                                productDetail.subscriptionOfferDetails?.forEach {
                                    "offer detail".logd()
                                    it.offerTags.forEach { it.logd() }

                                    subscriptionOfferDetailMap[it.offerToken] = it
                                }
                            }
                        }
                    }
                }
            }

            //Query ProductDetail for products
            if (productList.isNotEmpty()) {
                productDetailParams.setProductList(productList).let { params ->
                    billingClient.queryProductDetails(params.build())
                    val result = billingClient.queryProductDetails(params.build())
                    this@BillingManager.logd("Query product detail response ${result.billingResult.responseCode}")
                    val response = BillingResponse(result.billingResult.responseCode)
                    if (response.isOk) {
                        result.productDetailsList?.forEach { productDetail ->
                            productDetail.logd()
                            // store detail
                            productDetailMap[productDetail.productId] = productDetail
                            //store onetimeProductDetail
                            if (productDetail.productType == BillingClient.ProductType.INAPP) {
                                productDetail.oneTimePurchaseOfferDetails?.let {
                                    oneTimePurchaseOfferDetailMap[productDetail.productId] = it
                                }
                            }
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
        }

    }

    private suspend fun processPurchase(purchases: List<Purchase>?) {
        if (!purchases.isNullOrEmpty()) {
            for (purchase in purchases) {
                if (purchase.purchaseState == PurchaseState.PURCHASED || purchase.purchaseState == PurchaseState.PENDING) {
                    //Grant entitlement to the user.
                    purchase.products.forEach {
                        val detail = productDetailMap[it]
                        when (detail?.productType) {
                            BillingClient.ProductType.SUBS -> {

                            }
                        }
                        // If the state is PURCHASED, acknowledge the purchase if it hasn't been acknowledged yet.
                        if (!purchase.isAcknowledged && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.purchaseToken).build()
                            billingClient.acknowledgePurchase(acknowledgePurchaseParams)
                        }
                    }
                }
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
//                    Log.i(TAG, "onPurchasesUpdated: User canceled the purchase")
                }
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
//                    Log.i(TAG, "onPurchasesUpdated: The user already owns this item")
                }
                BillingClient.BillingResponseCode.DEVELOPER_ERROR -> {
//                    Log.e(
//                        TAG, "onPurchasesUpdated: Developer error means that Google Play " +
//                                "does not recognize the configuration. If you are just getting started, " +
//                                "make sure you have configured the application correctly in the " +
//                                "Google Play Console. The product ID must match and the APK you " +
//                                "are using must be signed with release keys."
//                    )
                }
            }
        }
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
