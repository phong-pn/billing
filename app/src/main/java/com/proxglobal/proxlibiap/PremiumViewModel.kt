package com.proxglobal.proxlibiap

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.proxglobal.purchase.ProxPurchase
import com.proxglobal.purchase.PurchaseUpdateListener
import com.proxglobal.purchase.controller.ProxSale
import com.proxglobal.purchase.model.BasePlanSubscription
import com.proxglobal.purchase.model.OfferSubscription
import com.proxglobal.purchase.model.OnetimeProduct
import com.proxglobal.purchase.sale.PricePlan
import com.proxglobal.purchase.sale.SaleBasePlan
import com.proxglobal.purchase.util.DataState
import com.proxglobal.purchase.util.onlyContains
import com.proxglobal.util.logd
import java.util.logging.Handler

class PremiumViewModel(application: Application) : AndroidViewModel(application) {
    var currentPricePlan: MutableLiveData<PricePlan> = MutableLiveData()
    var billing = ProxPurchase.getInstance()

    var uiState = MutableLiveData(PremiumUiState(isPurchased = billing.checkPurchased()))
    lateinit var basePlanMonth: BasePlanSubscription
    var offerMonth: OfferSubscription? = null
    init {

        ProxSale.currentSaleEvent?.let {
            getPrice()
        } ?: ProxSale.fetch {
            if (it is DataState.Success) {
                it.data?.apply {
                    pricePlans.forEach {
                        it.saleSubscriptions.logd()
                    }
                    ProxPurchase.getInstance().run {
                        getAllSubscription().size.logd()
                        addOneTimeProductId(getAllOneTimeProduct().map {
                            it.productId })
                        addSubscriptionId(getAllSubscription().map { it.productId })
                    }
                }
                android.os.Handler().postDelayed({
                    getPrice()
                }, 500)
            }
        }
        billing.addPurchaseUpdateListener(object : PurchaseUpdateListener{
            override fun onProductPurchased(productId: String) {
                super.onProductPurchased(productId)
                uiState.postValue {
                    isPurchased = true
                }
            }
        })


    }

    private fun getPrice() {
        currentPricePlan.value = ProxSale.currentSaleEvent?.pricePlans!![0]
        basePlanMonth = getBasePlanMonthly()!!
        offerMonth = getOfferOfBasePlanSubscription(basePlanMonth)

        val text = "Khong con ${basePlanMonth.price}, chi la ${offerMonth!!.pricingPhases[0].price} trong vong ${offerMonth!!.pricingPhases[0].billingPeriod}"
        uiState.postValue {
            baseMonthlyPrice = basePlanMonth.price
            offerMonthlyPrice = text
        }
    }


    private fun getBasePlanMonthly(): BasePlanSubscription? {
        val listBase = getListBasePlanSubscription()
        listBase.size.logd()
        return listBase.find { it.tags.onlyContains(listOf("monthly-premium")) }
    }

    fun getBasePlanYearly(): BasePlanSubscription? =
        getListBasePlanSubscription().find { it.tags.onlyContains(listOf("yearly-premium")) }

    private fun getListBasePlanSubscription(): List<BasePlanSubscription> {
        val basePlans = arrayListOf<BasePlanSubscription>()
        currentPricePlan.value!!.saleSubscriptions.forEach { saleSubs ->
            saleSubs.saleBasePlans.map { saleBasePlan ->
                billing.getBasePlan(saleSubs.productId, saleBasePlan.tags) ?.let { basePlans.add(it) }
            }
        }
        return basePlans
    }

    fun getOfferOfBasePlanSubscription(basePlanSubscription: BasePlanSubscription): OfferSubscription? {
        var offer: OfferSubscription? = null
        currentPricePlan.value!!.saleSubscriptions.forEach { saleSubs ->
            val saleBasePlan = saleSubs.saleBasePlans.find {
                it.tags.onlyContains(basePlanSubscription.tags)
            }
            offer = billing.getOfferSubscription(basePlanSubscription, saleBasePlan!!.offerTags)?.let { if (it.isNotEmpty()) it.get(0) else null }
            return@forEach
        }
        return offer
    }

    fun subscribeOfferMonly(activity: Activity) {
        if (offerMonth != null) {
            billing.subscribe(activity, offerMonth!!)
        }
    }

}

fun<T: Any> MutableLiveData<T>.postValue(block: T.() -> Unit) {
    block(value!!)
    postValue(value)
}

data class PremiumUiState(
    var isPurchased: Boolean,
    var baseMonthlyPrice: String = "",
    var offerMonthlyPrice: String? = null,
    var basePlanYearlyPrice: String = "",
    var offerYearlyPrice: String? = null,
    var onetimeProductPrice: String =""
)