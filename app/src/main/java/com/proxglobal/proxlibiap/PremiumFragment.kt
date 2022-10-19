package com.proxglobal.proxlibiap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.premium_layout.*

class PremiumFragment: Fragment() {
    private lateinit var premiumViewModel: PremiumViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.premium_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        premiumViewModel = ViewModelProviders.of(requireActivity())[PremiumViewModel::class.java]
        super.onViewCreated(view, savedInstanceState)
        addObserver()
        button3.setOnClickListener {
            premiumViewModel.subscribeOfferMonly(requireActivity())
        }
    }

    private fun addObserver() {
        premiumViewModel.uiState.observe(viewLifecycleOwner) {
            base_month_price.text = it.baseMonthlyPrice
            offer_month_price.text = it.offerMonthlyPrice
        }
    }
}