# ProxPurchase And Sale off example

## Features
- Lấy config sale off từ Remote Config. Xem thêm document về config sale off tại [đây]
- ProxPurchase với thư viện Google Play Billing 5. Xem thêm về [thư viện  Google Billing]
-
## Implementation
```
implementation "prox-lib:prox-utils-max-saleoff:2.3.8.1" // for max-ads
implementation "prox-lib:prox-utils-admob-saleoff:1.4.4" // for admob-ads, chưa update lastest code. Please wait
```

## Config trong RemoteConfig
**1. Params: config_purchase_id:**  chứa id của tất cả các subscription và one time product. Mỗi khi thêm 1 product hay subscription, đều phải thêm id vào config này
- Sample:
```
param name: config_purchase_id
value:
{
  "subscriptions_id": [
    "lib_iap_premium"
  ],
  "one_time_product_id": [
    "one_time_payment"
  ]
}
```

**2. Các params sale**. Xem thêm ocument về sale-off, quy tắc đặt tên params tại [đây]
- Sample
```
{
  "content_default": {
    "title": "Sale title",
    "description": "This is description test for default sale",
    "feature": [
      "Unlimited Premium Access 🔥",
      "All Updates & New Features 👆",
      "Completely Ad Free 🆓",
      "Quick Access to Your Apps 😎"
    ],
    "cta": "Go Premium"
  },
  "plans": [
    {
      "name": "base_price",
      "type": "base",
      "subscriptions": {
        "product_id": "lib_iap_premium",
        "base_plans": [
          {
            "tags": [
              "monthly-premium"
            ],
            "type": "month"
          },
          {
            "tags": [
              "yearly-premium"
            ],
            "type": "year"
          }
        ]
      },
      "one_time_products": {
        "base": {
          "product_id": "one_time_payment"
        }
      }
    }
  ],
  "script": [
    {
      "enable": true,
      "script_name": "click_start",
      "action_id": 2,
      "show_type": "full_screen",
      "show_condition_value": 2,
      "show_condition_type": "number",
      "image": [
        {
          "name": "banner_top",
          "url": "kanamomonogi.png"
        }
      ]
    },
    {
      "enable": true,
      "script_name": "click_save_script",
      "action_id": 3,
      "show_type": "pop_up",
      "show_condition_type": "period",
      "show_condition_value": {
        "start_time": "2022-10-18T00:00:00+0700",
        "end_time": "2022-10-30T00:00:00+0700",
        "enable": false
      },
      "image": [
        {
          "name": "pop_up_background",
          "url": "kanamomonogi.png"
        }
      ]
    },
    {
      "enable": true,
      "script_name": "add_point_swipe",
      "action_id": 5,
      "show_type": "pop_up",
      "show_condition_type": "boolean",
      "show_condition_value": true,
      "image": [
        {
          "name": "pop_up_background",
          "url": "kanamomonogi.png"
        }
      ]
    },
    {
      "enable": false,
      "script_name": "banner",
      "action_id": 9,
      "show_type": "banner",
      "show_condition_type": "boolean",
      "show_condition_value": true,
      "image": [
        {
          "name": "banner_background",
          "url": "kanamomonogi.png"
        }
      ]
    }
  ]
}
```


## ProxSale: Object lấy thông tin về saleEvent
- Lấy sale event:

```sh
ProxSale.currentSaleEvent // sale-off event, if exist. Else sale-default event
ProxSale.defaultSaleEvent // sale-default event
```

- Các product, subs, baseplan, offer
```
      val currentPlan = ProxSale.currentSaleEvent?.getValidProductPurchase()

        if (currentPlan != null) {
            val monthBasePlan = currentPlan.getMonthlyBasePlan()
            val offerMonth = monthBasePlan?.getValidOffer()

            val yearBasePlan = currentPlan.getYearlyBasePlan()
            val offerYearly = yearBasePlan?.getValidOffer()

            val saleOneTimeProducts = currentPlan.saleOneTimeProduct
            val offerOneTime = saleOneTimeProducts.getValidOffer()
            onetimeProduct = billing.getOneTimeProduct(saleOneTimeProducts.base)
        } else {
            Log.d(TAG, "Not found any valid Product Plan")
        }
```
- Show sale script
```
 ProxSale.showSale(actionId, currentPlan, object : DefaultShowSaleBehavior(
        numberTypeChecker = condition
    ) {
        override fun onShow(productPlan: ProductPlan?, script: Script) {
            if (ProxSale.currentSaleEvent?.isSaleOff == true) { //sale off
                SaleDialog(context, script, onClickCTA) { onCancel(null) }.show()
            } else {
                //sale default
            }
        }

        override fun onCancel(script: Script?) {
            onCancel(null)
        }

    })
```

## ProxPurchase
- Nếu không dùng Remote Config để truyền id qua config_purchase_id, cần thêm các id bằng các thú công:
```
ProxPurchase.getInstance().addOneTimeProductId(oneTimeProductId)
ProxPurchase.getInstance().addSubscriptionId(subscriptionId)
```

- Lấy thông tin của các  product
```
 var billing = ProxPurchase.getInstance()
 val currentPlan = ProxSale.currentSaleEvent?.getValidProductPurchase()
 val monthBasePlan = currentPlan.getMonthlyBasePlan()
    monthBasePlan?.let { monthBasePlan ->
         monthBasePlanSubscription = billing.getBasePlanSubscription(monthBasePlan)
        val offerMonth = monthBasePlan.getValidOffer()
        offerMonth?.let { offer ->
             monthOfferSubscription = billing.getOfferSubscription(monthBasePlan, offer)
        }
        //Get price
        val baseMonthPrice = monthBasePlanSubscription.price
        val offerMonthPrice = monthOfferSubscription?.getDiscountPhase()?.price
    }

```

- Purchase/Subscribe
```
    ProxPurchase.getInstance().subscribe(activity, subscription)
    ProxPurchase.getInstance().purchase(activity, oneTimeProduct)
```

- Kiểm tra purchase
```
ProxPurchase.getInstance().checkPurchase() // true nếu user có ít nhất 1 one time product hoặc available subscription
ProxPurchase.getInstance().isPurchase(productId)
```

[thư viện Google Billing]: <https://support.google.com/googleplay/android-developer/answer/12154973?hl=vi&ref_topic=345289>
[đây]: <https://www.figma.com/file/cqG2LMeQvsKliLBKBZEmFq/Document_Remote_Sale?node-id=0%3A1>

[Gulp]: <http://gulpjs.com>

[PlDb]: <https://github.com/joemccann/dillinger/tree/master/plugins/dropbox/README.md>
[PlGh]: <https://github.com/joemccann/dillinger/tree/master/plugins/github/README.md>
[PlGd]: <https://github.com/joemccann/dillinger/tree/master/plugins/googledrive/README.md>
[PlOd]: <https://github.com/joemccann/dillinger/tree/master/plugins/onedrive/README.md>
[PlMe]: <https://github.com/joemccann/dillinger/tree/master/plugins/medium/README.md>
[PlGa]: <https://github.com/RahulHP/dillinger/blob/master/plugins/googleanalytics/README.md>
1
