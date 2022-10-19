# ProxPurchase And Sale off example
Hello
## Features
- Lấy config sale off từ Remote Config. Xem thêm document về config sale off tại [đây]
- ProxPurchase với thư viện Google Play Billing 5. Xem thêm về [thư viện  Google Billing]

## Sử dụng (ProxSale)
- Để lấy config sale từ Remote Config, dùng
```sh
ProxSale.fetch {
         if (it is DataState.Success) {
         it.data.apply{ // SaleEvent
             
         }
        } else {
            //fail for fetching
        }
}
```
- Show sale script
```
ProxSale.showSale(actionId, pricePlan, behavior)
```

## Sử dụng ProxPurchase
- Add product Id 
```
ProxPurchase.getInstance().addOneTimeProductId(oneTimeProductId)
ProxPurchase.getInstance().addSubscriptionId(subscriptionId)
```

- Lấy thông tin của các billing product
```
val basePlan = ProxPurchase.getInstance().getBasePlan(subsId, listOf("base_plan_month"))
val price = basePlan.price

val offer = ProxPurchase.getInstance().getOffer(basePlan, listOf("offer_month"))

val oneTimeProduct = ProxPurchase.getInstance().getOneTimeProduct(oneTimeProductId)
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
