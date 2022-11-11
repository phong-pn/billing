package com.proxglobal.proxlibiap.utils

import android.content.Context
import android.view.WindowManager
import androidx.fragment.app.FragmentManager
import com.proxglobal.proxlibiap.PremiumFragment
import com.proxglobal.proxlibiap.ui.SaleDialog
import com.proxglobal.purchase.controller.ProxSale
import com.proxglobal.purchase.controller.behavior.DefaultShowSaleBehavior
import com.proxglobal.purchase.sale.ProductPlan
import com.proxglobal.purchase.sale.Script
import com.proxglobal.purchase.util.Action

val currentPlan: ProductPlan?
    get() = ProxSale.currentSaleEvent?.getValidProductPlan()

fun showSale(
    actionId: Int,
    productPlan: ProductPlan?,
    condition: (Number) -> Boolean,
    onShow: (pricePlan: ProductPlan?, script: Script) -> Unit,
    onCancel: (script: Script?) -> Unit
) {
    ProxSale.showSale(
        actionId,
        productPlan,
        //DefaultShowSaleBehavior already check condition for script has condition type is Script.BOOLEAN or Script.PERIOD
        // if script's type is Script.NUMBER, you must custom your condition
        object : DefaultShowSaleBehavior(
            numberTypeChecker = condition
        ) {
            override fun onShow(productPlan: ProductPlan?, script: Script) {
                onShow.invoke(productPlan, script)
            }

            override fun onCancel(script: Script?) {
                onCancel.invoke(script)
            }

        })
}

fun showFullscreen(fm: FragmentManager, layoutContainer: Int, productPlan: ProductPlan) {
    ProxSale.showSale(2, productPlan, object : DefaultShowSaleBehavior(){
        override fun onShow(productPlan: ProductPlan?, script: Script) {
            fm.beginTransaction()
                .add(layoutContainer, PremiumFragment.newInstance(), null)
                .commit()
        }
    })
}

fun showPopUpSale(
    actionId: Int,
    condition: (Number) -> Boolean,
    context: Context,
    onCancel: (script: Script?) -> Unit,
    onClickCTA: Action,
) {
    ProxSale.showSale(actionId, currentPlan, object : DefaultShowSaleBehavior(
        numberTypeChecker = condition
    ) {
        override fun onShow(productPlan: ProductPlan?, script: Script) {
            if (ProxSale.currentSaleEvent?.isSaleOff == true) { //sale off
                SaleDialog(context, script, onClickCTA) { onCancel(null) }.show()
            } else {
                //sale default
            }
        }

        override fun onCancel(script: Script?) {
            onCancel(null)
        }

    })
}