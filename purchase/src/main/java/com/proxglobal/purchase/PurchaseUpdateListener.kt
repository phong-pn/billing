package com.proxglobal.purchase

interface PurchaseUpdateListener {
    fun onPurchaseSuccess(productId: String) { }
    fun onPurchaseFailure(code: Int, errorMsg: String?) { }
    fun onUserCancelBilling() { }
    fun onOwnedProduct(productId: String) { }
}