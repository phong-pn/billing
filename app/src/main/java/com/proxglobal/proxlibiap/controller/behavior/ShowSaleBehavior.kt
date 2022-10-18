package com.proxglobal.proxlibiap.controller.behavior

import com.proxglobal.proxlibiap.sale.SaleEvent
import com.proxglobal.proxlibiap.sale.Script

/**
 * Define how to show a sale script
 */
interface ShowSaleBehavior {
    /**
     * Call when sale condition is not match
     */
    fun onCancel(event: SaleEvent?, script: Script?) {}

    /**
     * Call when sale condition is match
     */
    fun onShow(event: SaleEvent, script: Script)

    /**
     * Check if show sale condition is match
     */
    fun checkCondition(event: SaleEvent, script: Script): Boolean = true
}