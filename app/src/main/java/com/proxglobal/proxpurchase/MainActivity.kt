package com.proxglobal.proxpurchase

import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.proxglobal.proxpurchase.game.player.GameRenderView
import com.proxglobal.proxpurchase.store.StoreFragment
import com.proxglobal.purchase.billing.ProxPurchase
import com.proxglobal.purchase.util.logd
import com.proxglobal.purchase.util.logdSelf
import kotlin.math.log

val subId = "lib_iap_premium"
val onetimeProductId = "one_time_payment"

class MainActivity : AppCompatActivity() {
    private var positions: List<Float>
    private var humanPosition: Int
    init {
        val w = Resources.getSystem().displayMetrics.widthPixels.toFloat()
        positions = listOf(
            w / 8f,
            3 * w / 8f,
            5 * w / 8f,
            7 * w / 8f
        )
        humanPosition = 2
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ProxPurchase.getInstance().addInitBillingFinishListener {
            logd("Init oke")
            supportFragmentManager.beginTransaction()
                .add(android.R.id.content, StoreFragment(), null)
                .commit()
        }

        val gameRenderView = findViewById<GameRenderView>(R.id.game_render)
        Handler(Looper.myLooper()!!).postDelayed({
            gameRenderView.startGame()
        }, 1000)

        val human = findViewById<ImageView>(R.id.human)
        gameRenderView.human = human
        human.x = positions[humanPosition] - human.width / 2
        var xGesture = 0f
        var yGesture = 0f
        gameRenderView.setOnTouchListener { v, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                logd(human.boundingBox.toString())

                xGesture = event.x
            }
            if (event.actionMasked == MotionEvent.ACTION_UP) {
                if (event.x - xGesture > 0) {
                    if (humanPosition < positions.size - 1) {
                        humanPosition++
                        human.x = positions[humanPosition] - human.width / 2
                    }
                } else if (event.x - xGesture < 0) {
                    if (humanPosition > 0) {
                        humanPosition--
                        human.x = positions[humanPosition] - human.width / 2
                    }
                }
            }
            true
        }


//
//        Handler(Looper.myLooper()!!).postDelayed({
//            ProxPurchase.getInstance().getBasePlan("premium-monthly").run {
//                logd("basePlanId = $basePlanId,  price = $price")
//            }
//            ProxPurchase.getInstance().getOffer("free-trial").run {
//                logd("offerId = $offerId, price = ${getDiscountPhase()}")
//            }
//            ProxPurchase.getInstance().getOneTimeProduct(onetimeProductId).logdSelf()
//        }, 2500)

    }
}