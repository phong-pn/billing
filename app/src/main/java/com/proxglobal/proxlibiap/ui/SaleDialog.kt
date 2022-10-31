package com.proxglobal.proxlibiap.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Outline
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.Button
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.phongpn.countdown.countdown.CountDownView
import com.proxglobal.proxlibiap.R
import com.proxglobal.proxlibiap.utils.dp
import com.proxglobal.proxlibiap.utils.visible
import com.proxglobal.purchase.controller.ProxSale
import com.proxglobal.purchase.sale.Script
import com.proxglobal.purchase.util.Action
import com.proxglobal.purchase.util.parseTime

open class SaleDialog(
    context: Context,
    private val script: Script,
    private val onClickCTA: Action,
    private val onClosed: Action
) : Dialog(context, R.style.CustomDialog) {
    private lateinit var view: View
    override fun onCreate(savedInstanceState: Bundle?) {
        view = View.inflate(context, R.layout.bottom_sheet_sale_popup, null)
        window?.apply {
//            attributes = attributes?.apply {
//                width = 200.dp.toInt()//ViewGroup.LayoutParams.MATCH_PARENT
//            }
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//            requestWindowFeature(Window.FEATURE_NO_TITLE)
        }
        setContentView(view)
        setupViews()
        addEvent()
        setCanceledOnTouchOutside(true)
    }

    private fun addEvent() {
        setOnDismissListener {
            onClosed()
        }
        setOnShowListener {
            findViewById<View>(R.id.iv_exit_pop_up_sale)?.setOnClickListener {
                dismiss()
            }

            findViewById<Button>(R.id.bt_continue)?.setOnClickListener {
                onClickCTA()
                dismiss()
            }
        }
    }

    open protected fun setupViews() {
        view.clipToOutline = true
        view.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, 16.dp)
            }

        }
        val background = findViewById<ImageView>(R.id.iv_popup_background_1)!!
        Glide.with(background)
            .load(script.getImageUrl("pop_up_background"))
            .apply(RequestOptions.bitmapTransform(RoundedCorners(16.dp.toInt())))
            .placeholder(R.drawable.ic_sale_dialog_background_placeholder)
            .override(
                context.resources.getDimension(com.intuit.sdp.R.dimen._300sdp).toInt(),
                context.resources.getDimension(com.intuit.sdp.R.dimen._375sdp).toInt()
            )
            .into(background)
        if (ProxSale.currentSaleEvent?.isSaleOff == true) {
            findViewById<View>(R.id.count_down_area)!!.visible()
            findViewById<CountDownView>(R.id.count_down)!!
                .text {
                    padding(l = 3.dp, r = 3.dp)
                    offset(t = 4.dp.toInt(), b = 4.dp.toInt())
                }
                .suffix {
                    margin(l = 3.dp, r = 3.dp)
                }.display {
                    showDay = true
                }
                .startUtil(909090)
        }
        script.saleContent?.cta?.let {
            findViewById<Button>(R.id.bt_continue).text = it
        }
    }
}