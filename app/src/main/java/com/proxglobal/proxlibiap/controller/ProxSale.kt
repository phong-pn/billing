package com.example.sale_lib.sale.controller

import android.widget.FrameLayout
import com.example.sale_lib.sale.data.SaleDataSource
import com.google.firebase.inappmessaging.FirebaseInAppMessagingDisplayCallbacks
import com.google.firebase.inappmessaging.ktx.inAppMessaging
import com.google.firebase.inappmessaging.model.InAppMessage
import com.google.firebase.ktx.Firebase
import com.proxglobal.proxlibiap.controller.behavior.ShowSaleBehavior
import com.proxglobal.proxlibiap.data.RemoteConfigSource
import com.proxglobal.proxlibiap.sale.SaleEvent
import com.proxglobal.proxlibiap.sale.Script
import com.proxglobal.proxlibiap.util.Action
import com.proxglobal.proxlibiap.util.DataState
import com.proxglobal.proxlibiap.util.logd

/**
 * Controller for show sale script
 */
object ProxSale {

    private val source = SaleDataSource(RemoteConfigSource().remoteConfig)

    fun fetch(result: (DataState<SaleEvent>) -> Unit) {
        source.fetchSaleEvent(result)
    }

    init {
        // temporarily disable show in-app-message
//        Firebase.inAppMessaging.setMessagesSuppressed(true)
//        ProxSale.registerInAppMessagingListener(
//            onClick = { message, action ->
//                action.actionUrl?.logd()
//            }
//        )
    }

    val currentSaleEvent: SaleEvent?
        get() = source.event ?: source.defaultEvent

    val defaultSaleEvent: SaleEvent?
        get() = source.defaultEvent

    fun getScript(actionId: Int) = source.getScript(actionId)

    /**
     * Show a script with with [behavior]
     * @see ShowSaleBehavior
     */
    fun showSale(
        actionId: Int,
        behavior: ShowSaleBehavior,
    ) {
        val script = source.getScript(actionId)
        script?.let {
            if (behavior.checkCondition(currentSaleEvent!!, script)) {
                behavior.onShow(currentSaleEvent!!, script)
            } else behavior.onCancel(currentSaleEvent!!, script)
        } ?: behavior.onCancel(currentSaleEvent, script)
    }

//    /**
//     * Show a banner script. By default, only banner image will be hold inside [container].
//     * If you want custom your showing behavior, like adding a count down, you can custom [ShowSaleBehavior]
//     * and after that, set [behavior] with your custom behavior
//     * @param container: a [FrameLayout] hold the banner view
//     * @param scriptId: id of script
//     * @param behavior: showing banner behavior. By default, this is a [DefaultShowBannerSaleBehavior]
//     */
//    fun showBanner(
//        container: FrameLayout,
//        actionId: Int,
//        onClick: Action,
//        onClose: Action,
//        behavior: ShowSaleBehavior = DefaultShowBannerSaleBehavior(container, onClick, onClose)
//    ) {
//        showSale(actionId, behavior)
//    }
//
//    fun showPopup(
//        actionId: Int,
//        behavior: ShowSaleBehavior = DefaultShowPopupSaleBehavior()
//    ) = showSale(actionId, behavior)
//
//    fun showFullscreen(
//        actionId: Int,
//        behavior: DefaultShowFullscreenSaleBehavior
//    ) = showSale(actionId, behavior)
//
//    /**
//     * Programmatically show in-app-message. Note that [triggerEvent] must be match with trigger eventID
//     * of message.
//     *@see <a href = "https://firebase.google.com/docs/in-app-messaging/modify-message-behavior?hl=en&authuser=0&platform=android#trigger_in-app_messages_programmatically_2">See more about trigger event</a>
//     */
//    fun showInAppMessaging(
//        triggerEvent: String = "ready_display",
//        behavior: ShowSaleBehavior = DefaultShowInAppMessingBehavior(triggerEvent)
//    ) {
//        if (inAppMessageScript != null) showSale(
//            inAppMessageScript!!.actionId,
//            behavior
//        ) else {
//            "no_inapp_message_found".logd()
//        }
//    }
//
//    /**
//     * Register [Firebase.inAppMessaging] listener for showing message
//     */
//    fun registerInAppMessagingListener(
//        onClick: (message: InAppMessage, action: com.google.firebase.inappmessaging.model.Action) -> Unit = { _, _ -> },
//        onDismiss: (message: InAppMessage) -> Unit = { _ -> },
//        onDisplayError: (message: InAppMessage, reson: FirebaseInAppMessagingDisplayCallbacks.InAppMessagingErrorReason) -> Unit = { _, reson -> reson.logd() },
//        onDisplay: (message: InAppMessage) -> Unit = { _ -> "display".logd() },
//    ) {
//        Firebase.inAppMessaging.apply {
//            addClickListener(onClick)
//            addDismissListener(onDismiss)
//            addDisplayErrorListener(onDisplayError)
//            addImpressionListener(onDisplay)
//        }
//    }
//
//    private val inAppMessageScript: Script?
//        get() {
//            return currentSaleEvent?.saleScripts?.find { it.showType == Script.TYPE_SHOW_IN_APP_MESSAGE }
//        }
}

