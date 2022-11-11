package com.proxglobal.proxlibiap

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.proxglobal.purchase.PurchaseUpdateListener
import com.proxglobal.purchase.billing.ProxPurchase
import com.proxglobal.purchase.controller.ProxSale
import com.proxglobal.purchase.model.BasePlanSubscription
import com.proxglobal.purchase.model.OfferSubscription
import com.proxglobal.purchase.model.OnetimeProduct
import com.proxglobal.util.logeSelf

class PremiumViewModel(application: Application) : AndroidViewModel(application) {
    var billing = ProxPurchase.getInstance()

    var uiState = MutableLiveData(PremiumUiState(isPurchased = billing.checkPurchased()))

    lateinit var monthBasePlanSubscription: BasePlanSubscription
    lateinit var yearBasePlanSubscription: BasePlanSubscription
    lateinit var onetimeProduct: OnetimeProduct

    var monthOfferSubscription: OfferSubscription? = null
    var yearOfferSubscription: OfferSubscription? = null
    var offerOnetimeProduct: OnetimeProduct? = null

    init {
        billing.addPurchaseUpdateListener(object : PurchaseUpdateListener {
            override fun onOwnedProduct(productId: String) {

            }

            override fun onProductPurchased(productId: String) {
                super.onProductPurchased(productId)
                uiState.postValue {
                    isPurchased = true
                }
            }
        })

        billing.checkPurchased().logeSelf()

        ProxPurchase.getInstance().addInitBillingFinishListener {
            if (it) {
                "billing is available".logeSelf()
                getPrice()
                billing.checkPurchased().logeSelf()
            }
        }
    }

    private fun getPrice() {
        val currentPlan = ProxSale.currentSaleEvent!!.getValidProductPlan()

        monthBasePlanSubscription = currentPlan.getMonthlyBasePlanSubscription()!!
        monthOfferSubscription = currentPlan.getValidMonthlyOfferSubscription()

        yearBasePlanSubscription = currentPlan.getYearlyBasePlanSubscription()!!
        yearOfferSubscription = currentPlan.getValidYearlyOfferSubscription()

        onetimeProduct = currentPlan.getBaseOneTimeProduct()!!
        offerOnetimeProduct = currentPlan.getValidOfferOneTimeProduct()


        uiState.postValue {
            baseMonthlyPrice = monthBasePlanSubscription.price
            offerMonthlyPrice = monthOfferSubscription?.getDiscountPhase()?.price

            basePlanYearlyPrice = yearBasePlanSubscription.price
            offerYearlyPrice = yearOfferSubscription?.getDiscountPhase()?.price

            onetimeProductPrice = onetimeProduct.price
        }
    }

    fun buyMonth(activity: Activity) {
        billing.subscribe(activity, monthOfferSubscription ?: monthBasePlanSubscription)
    }

    fun buyYear(activity: Activity) {
        billing.subscribe(activity, yearOfferSubscription ?: yearBasePlanSubscription)
    }

    fun buyOneTime(activity: Activity) {
        billing.purchase(activity, offerOnetimeProduct ?: onetimeProduct)
    }

}

fun <T : Any> MutableLiveData<T>.postValue(block: T.() -> Unit) {
    block(value!!)
    postValue(value)
}

data class PremiumUiState(
    var isPurchased: Boolean,

    var baseMonthlyPrice: String = "",
    var discountMonthly: Int = 0,
    var offerMonthlyPrice: String? = null,

    var basePlanYearlyPrice: String = "",
    var discountYearly: Int = 0,
    var offerYearlyPrice: String? = null,

    var onetimeProductPrice: String = "",
    var discountOneTime: Int = 0,
    var offerOneTimePrice: String? = null
)