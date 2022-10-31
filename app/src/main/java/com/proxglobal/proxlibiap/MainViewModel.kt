package com.proxglobal.proxlibiap

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.proxglobal.proxlibiap.utils.showPopUpSale
import com.proxglobal.proxlibiap.utils.showSale
import com.proxglobal.purchase.controller.ProxSale
import com.proxglobal.purchase.sale.ProductPlan
import com.proxglobal.purchase.sale.Script

class MainViewModel: ViewModel() {
    var clickAddCount = 0
    private val currentPlan: ProductPlan?
        get() = ProxSale.currentSaleEvent?.getValidProductPurchase()

    fun onClickAdd(
        context: Context,
        onShowPremium: () -> Unit,
        onCancelShowSale: (script: Script?) -> Unit
    ) {
        clickAddCount++
        showPopUpSale(
            context = context,
            actionId = 5,
            onCancel = onCancelShowSale,
            condition = {
                clickAddCount >= it as Double
            },
            onClickCTA = onShowPremium
        )
    }


}