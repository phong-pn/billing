package com.proxglobal.proxlibiap

interface PurchaseUpdateListener {
    fun onProductPurchased(productId: String, transactionDetails: String) { }
    fun onPurchaseFailure(code: Int, errorMsg: String?) { }
    fun onUserCancelBilling() { }
}