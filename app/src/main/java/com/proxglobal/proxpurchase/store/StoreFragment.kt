package com.proxglobal.proxpurchase.store

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.proxglobal.proxpurchase.databinding.FragmentStoreBinding
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

    private fun addListener() {
        binding.monthSubscription.setOnClickListener {
            ProxPurchase.getInstance().buy(requireActivity(), "offer-monthly-2")
        }

        binding.yearSubscription.setOnClickListener {
            ProxPurchase.getInstance().buy(requireActivity(), "offer-yearly")
        }

        binding.oneTimeProduct.setOnClickListener {
            ProxPurchase.getInstance().buy(requireActivity(), "one_time_payment")
        }
    }

    private fun initView() {
        binding.tvMonthBasePrice.text = ProxPurchase.getInstance().getPrice("offer-monthly-2") + " / tháng"
        binding.tvYearBasePrice.text = ProxPurchase.getInstance().getPrice("offer-yearly")+ " / năm"
        binding.tvOneTimePrice.text = ProxPurchase.getInstance().getPrice("one_time_payment")
    }
}