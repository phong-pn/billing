package com.proxglobal.purchase.model

import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetails.SubscriptionOfferDetails

/**
 * Subscription is class that represent a subscription in Play Billing System.
 * @see [OfferSubscription]
 * @see BasePlanSubscription
 */
abstract class Subscription(
    internal val productId: String,
    internal val token: String,
)