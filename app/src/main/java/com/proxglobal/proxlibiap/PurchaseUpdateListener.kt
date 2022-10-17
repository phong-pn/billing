package com.proxglobal.proxlibiap

interface PurchaseUpdateListener {
    fun onProductPurchased(productId: String) { }
    fun onPurchaseFailure(code: Int, errorMsg: String?) { }
    fun onUserCancelBilling() { }
}