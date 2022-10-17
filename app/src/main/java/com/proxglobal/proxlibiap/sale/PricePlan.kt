package com.proxglobal.proxlibiap.sale

class PricePlan(
    var name: String,
    var startTime: String,
    var endTime: String,
    var subscriptions: List<Subscription>,
    var oneTimeProducts: List<OneTimeProduct>
) {
}