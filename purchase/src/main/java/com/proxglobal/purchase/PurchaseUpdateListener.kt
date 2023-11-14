package com.proxglobal.purchase

interface PurchaseUpdateListener {
    fun onPurchaseSuccess(purchase: String) {}
    fun onPurchaseFailure(code: Int, errorMsg: String?) {}
    fun onUserCancelBilling() {}
    fun onOwnedProduct(productId: String) {}
}