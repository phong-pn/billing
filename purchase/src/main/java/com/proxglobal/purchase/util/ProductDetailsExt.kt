package com.proxglobal.purchase.util

import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetails.OneTimePurchaseOfferDetails
import com.android.billingclient.api.ProductDetails.SubscriptionOfferDetails
import com.proxglobal.purchase.model.BasePlanSubscription
import com.proxglobal.purchase.model.OfferSubscription
import com.proxglobal.purchase.model.OnetimeProduct
import com.proxglobal.purchase.model.Phase

/**
 * Find all offer in a product detail
 */
private fun ProductDetails.findOffers(basePlan: BasePlanSubscription): List<OfferSubscription> {
    val result = mutableListOf<OfferSubscription>()
    subscriptionOfferDetails?.forEach {
        if ((it.offerId != null) && (it.basePlanId == basePlan.basePlanId)) {
            result.add(
                OfferSubscription(
                    it.offerId!!,
                    basePlan,
                    it.pricingPhases.pricingPhaseList.map { Phase.fromPricingPhase(it) },
                    productId,
                    it.offerToken
                )
            )
        }
    }
    return result
}

fun ProductDetails.findBasePlan(id: String): BasePlanSubscription? {
    val result = subscriptionOfferDetails?.find { it.offerId == null }
    return result?.let { basePlan ->
        BasePlanSubscription(
            id,
            basePlan.offerTags,
            Phase.fromPricingPhase(basePlan.pricingPhases.pricingPhaseList[0]),
            productId,
            basePlan.offerToken
        ).apply {
            offers = findOffers(this)
        }
    }
}

fun ProductDetails.findAllBasePlan(): List<BasePlanSubscription> {
    val result = mutableListOf<BasePlanSubscription>()
    subscriptionOfferDetails?.forEach {
        if (it.offerId == null) {
            result.add(it.toBasePlan(productId))
        }
    }
    result.forEach {
        it.offers = findOffers(it)
    }
    return result
}

fun SubscriptionOfferDetails.toBasePlan(productId: String) = BasePlanSubscription(
    basePlanId,
    offerTags,
    Phase.fromPricingPhase(pricingPhases.pricingPhaseList[0]),
    productId,
    offerToken
)

fun OneTimePurchaseOfferDetails.toOneTimeProduct(productId: String) =
    OnetimeProduct(productId, formattedPrice, priceAmountMicros / 1000000f, priceCurrencyCode)

