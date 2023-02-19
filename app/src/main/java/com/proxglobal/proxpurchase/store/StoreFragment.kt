package com.proxglobal.proxpurchase.store

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.florent37.viewanimator.ViewAnimator
import com.proxglobal.proxpurchase.databinding.FragmentStoreBinding
import com.proxglobal.purchase.PurchaseUpdateListener
import com.proxglobal.purchase.billing.ProxPurchase

class StoreFragment: Fragment() {
    private lateinit var binding: FragmentStoreBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStoreBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        addListener()
    }

    private lateinit var selectedPurchaseItem: View

    private fun addListener() {
        ProxPurchase.getInstance().addPurchaseUpdateListener(object : PurchaseUpdateListener{
            override fun onPurchaseFailure(code: Int, errorMsg: String?) {
                super.onPurchaseFailure(code, errorMsg)
            }

            override fun onUserCancelBilling() {
                super.onUserCancelBilling()
            }

            override fun onPurchaseSuccess(productId: String) {
                super.onPurchaseSuccess(productId)
            }
        })
        binding.monthSubscription.setOnClickListener {
            selectedPurchaseItem = it
            ProxPurchase.getInstance().buy(requireActivity(), "offer-monthly-2")
        }

        binding.yearSubscription.setOnClickListener {
            selectedPurchaseItem = it
            ProxPurchase.getInstance().buy(requireActivity(), "offer-yearly")
        }

        binding.oneTimeProduct.setOnClickListener {
            selectedPurchaseItem = it
            ProxPurchase.getInstance().buy(requireActivity(), "one_time_payment")
        }
    }

    private fun initView() {
        binding.tvMonthBasePrice.text = ProxPurchase.getInstance().getPrice("offer-monthly-2") + " / tháng"
        binding.tvYearBasePrice.text = ProxPurchase.getInstance().getPrice("offer-yearly")+ " / năm"
        binding.tvOneTimePrice.text = ProxPurchase.getInstance().getPrice("one_time_payment")
    }
}