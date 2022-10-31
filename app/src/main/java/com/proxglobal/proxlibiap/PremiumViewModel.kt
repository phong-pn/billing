package com.proxglobal.proxlibiap

import android.app.Activity
import android.app.Application
import android.os.Handler
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.phongpn.countdown.util.logd
import com.proxglobal.purchase.PurchaseUpdateListener
import com.proxglobal.purchase.billing.ProxPurchase
import com.proxglobal.purchase.controller.ProxSale
import com.proxglobal.purchase.model.BasePlanSubscription
import com.proxglobal.purchase.model.OfferSubscription
import com.proxglobal.purchase.model.OnetimeProduct
import com.proxglobal.purchase.sale.ProductPlan
import com.proxglobal.purchase.sale.product.SaleBasePlan
import com.proxglobal.purchase.sale.product.SaleOffer

class PremiumViewModel(application: Application) : AndroidViewModel(application) {
    var currentPricePlan: MutableLiveData<ProductPlan> = MutableLiveData()
    var billing = ProxPurchase.getInstance()

    var uiState = MutableLiveData(PremiumUiState(isPurchased = billing.checkPurchased()))
    lateinit var basePlanMonth: BasePlanSubscription
    var offerMonth: OfferSubscription? = null

    lateinit var monthBasePlanSubscription: BasePlanSubscription
    lateinit var yearBasePlanSubscription: BasePlanSubscription
    lateinit var onetimeProduct: OnetimeProduct

    var monthOfferSubscription: OfferSubscription? = null
    var yearOfferSubscription: OfferSubscription? = null
    var offerOnetimeProduct: OnetimeProduct? = null
    init {
        billing.addPurchaseUpdateListener(object : PurchaseUpdateListener {
            override fun onProductPurchased(productId: String) {
                super.onProductPurchased(productId)
                uiState.postValue {
                    isPurchased = true
                }
            }
        })

        Handler().postDelayed({
            getPrice()
        }, 2000)
    }

    private fun getPrice() {
        currentPricePlan.value = ProxSale.currentSaleEvent!!.getValidProductPurchase()

        val monthBasePlan = currentPricePlan.value?.getMonthlyBasePlan()!!
        monthBasePlanSubscription = billing.getBasePlanSubscription(monthBasePlan)
        val offerMonth = monthBasePlan.getValidOffer()
        monthOfferSubscription = offerMonth?.let { getOffer(monthBasePlan, it) }

        val yearBasePlan = currentPricePlan.value!!.getYearlyBasePlan()!!
        yearBasePlanSubscription = billing.getBasePlanSubscription(yearBasePlan)
        yearOfferSubscription =
            yearBasePlan.getValidOffer()?.let { billing.getOfferSubscription(yearBasePlan, it) }

        val saleOneTimeProducts = currentPricePlan.value!!.saleOneTimeProduct
        onetimeProduct = billing.getOneTimeProduct(saleOneTimeProducts.base)
        offerOnetimeProduct = saleOneTimeProducts.getValidOffer()?.let { billing.getOneTimeProduct(it) }


        uiState.postValue {
            baseMonthlyPrice = monthBasePlanSubscription.price
            offerMonthlyPrice = monthOfferSubscription?.getDiscountPhase()?.price
            discountMonthly = offerMonth?.percent ?: 0

            basePlanYearlyPrice = yearBasePlanSubscription.price
            offerYearlyPrice = yearOfferSubscription?.getDiscountPhase()?.price
            discountYearly = yearBasePlan.getValidOffer()?.percent ?: 0

            onetimeProductPrice = onetimeProduct.price
        }

    }


    fun getOffer(saleBasePlan: SaleBasePlan, saleOffer: SaleOffer): OfferSubscription {
        return billing.getOfferSubscription(saleBasePlan, saleOffer)


    }

    fun subscribe(activity: Activity) {
        var billing = ProxPurchase.getInstance()
        val currentPlan = ProxSale.currentSaleEvent?.getValidProductPurchase()
        if (currentPlan != null) {
            val monthBasePlan = currentPlan.getMonthlyBasePlan()

            monthBasePlan?.let { monthBasePlan ->
                monthBasePlanSubscription = billing.getBasePlanSubscription(monthBasePlan)
                val offerMonth = monthBasePlan.getValidOffer()
                offerMonth?.let { offer ->
                    monthOfferSubscription = billing.getOfferSubscription(monthBasePlan, offer)
                }

                val baseMonthPrice = monthBasePlanSubscription.price
                val offerMonthPrice = monthOfferSubscription?.getDiscountPhase()?.price
            }


            val yearBasePlan = currentPlan.getYearlyBasePlan()
            val offerYearly = yearBasePlan?.getValidOffer()

            val saleOneTimeProducts = currentPlan.saleOneTimeProduct
            val offerOneTime = saleOneTimeProducts.getValidOffer()
            onetimeProduct = billing.getOneTimeProduct(saleOneTimeProducts.base)
        } else {
            logd("Not found any valid Product Plan")
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