package com.proxglobal.proxlibiap.util

import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetails.SubscriptionOfferDetails
import com.proxglobal.proxlibiap.model.BasePlanSubscription
import com.proxglobal.proxlibiap.model.OfferSubscription
import com.proxglobal.proxlibiap.model.Phase

fun ProductDetails.findOffers(
    basePlan: BasePlanSubscription,
    tags: List<String>
): List<OfferSubscription> {
    val offersAndBasePlan = arrayListOf<SubscriptionOfferDetails>()
    subscriptionOfferDetails?.forEach {
        if (it.offerTags.containsAll(basePlan.tags)) {
            offersAndBasePlan.add(it)
        }
    }
    val offers = offersAndBasePlan.filter { it.offerTags.containsAll(tags) }
    return offers.map { offerDetails ->
        OfferSubscription(
            offerDetails.offerTags,
            offerDetails.pricingPhases.pricingPhaseList.map { Phase.fromPricingPhase(it) },
            productId,
            offerDetails.offerToken
        )
    }
}

fun ProductDetails.findBasePlan(tags: List<String>): BasePlanSubscription? {
    val result = arrayListOf<SubscriptionOfferDetails>()
    subscriptionOfferDetails?.forEach {
        if (it.offerTags.onlyContains(tags)) result.add(it)
    }
    return when (result.size) {
        1 -> {
            val basePlan = result[0]
            BasePlanSubscription(
                basePlan.offerTags,
                basePlan.pricingPhases.pricingPhaseList[0].formattedPrice,
                basePlan.pricingPhases.pricingPhaseList[0].priceCurrencyCode,
                productId,
                basePlan.offerToken
            )
        }
        0 -> {
            "Can not find basePlan that has tags is $tags".loge()
            null
        }
        else -> {
            "Find more 1 basePlan that has tags is $tags".loge()
            null
        }
    }
}

fun <T> List<T>.onlyContains(other: List<T>): Boolean {
    if (size == other.size) {
        if (containsAll(other)) return true
    }
    return false
}