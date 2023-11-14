package com.proxglobal.proxpurchase.store

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.proxglobal.proxpurchase.copyToClipboard
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

            override fun onPurchaseSuccess(purchase: String) {
                requireContext().copyToClipboard(purchase)
                Toast.makeText(requireContext(), "Token copied", Toast.LENGTH_SHORT).show()
                super.onPurchaseSuccess(purchase)
            }
        })
        binding.monthSubscription.setOnClickListener {
            selectedPurchaseItem = it
            ProxPurchase.getInstance().buy(requireActivity(), "month")
        }

        binding.yearSubscription.setOnClickListener {
            selectedPurchaseItem = it
            ProxPurchase.getInstance().buy(requireActivity(), "year")
        }

        binding.oneTimeProduct.setOnClickListener {
            selectedPurchaseItem = it
            ProxPurchase.getInstance().buy(requireActivity(), "in_app_product_1")
        }
    }

    private fun initView() {
        binding.tvMonthBasePrice.text = ProxPurchase.getInstance().getPrice("month") + " / tháng"
        binding.tvYearBasePrice.text = ProxPurchase.getInstance().getPrice("year")+ " / năm"
        binding.tvOneTimePrice.text = ProxPurchase.getInstance().getPrice("in_app_product_1")
    }
}