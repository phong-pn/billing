# ProxPurchase

## Features

- ProxPurchase với thư viện Google Play Billing 5. Xem thêm về [thư viện  Google Billing]

## Implementation

```

```


## ProxPurchase
- Add all your product id in onCreate() of your Application

```
ProxPurchase.getInstance().apply {
            init(context)
            addSubscriptionId(listOf("subscription_id"))
            addOneTimeProductId(listOf("one_time_product_id"))
            addConsumableId(listOf("consumable_id"))
        }
```

- After ProxPurchase's initiation finish, you can get information about products, like checkPurchase(), get price..., or make a purchase. If you need query these informations early, you need add initBillingFinishListner

```
   ProxPurchase.getInstance().addInitBillingFinishListener {
            if(ProxPurchase.getInstance().checkPurchased()) {
                getGoToMain()
            } else {
                showAds()
            }
        }
```

- Get Price

```
    ProxPurchase.getInstance().getPrice(id) // id can be basePlanId, offerId, or oneTimeProductId

```

- Make a purchase

```
    ProxPurchase.getInstance().addPurchaseUpdateListener(object : PurchaseUpdateListener{
            override fun onPurchaseFailure(code: Int, errorMsg: String?) {
            }

            override fun onUserCancelBilling() {
            }

            override fun onPurchaseSuccess(productId: String) {
                getGoToPremium()
            }
        })
    ProxPurchase.getInstance().buy(activity, id) // id can be basePlanId, offerId, or oneTimeProductId
```

- Kiểm tra purchase

```
ProxPurchase.getInstance().checkPurchase() // true if have a vaild subscription or one time product
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
