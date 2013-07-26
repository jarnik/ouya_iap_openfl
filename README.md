OUYA In App Purchases with OpenFL
===============

A simple example that shows how to get the product list, receipts and purchase a product.

Using: 
  * Haxe 3
  * https://github.com/openfl/openfl
  * https://github.com/openfl/openfl-ouya
  

Example:
=======

What the current example does:
* fetch the test product information and receipts
* display a large O icon, unless you have purchased the test product
* press any OUYA controller button to purchase the product
* hide the O icon on successful purchase

There are no other visuals, you'll have to watch the console output using 'adb logcat'.


Getting started:
=======

You will need:
 * your OUYA developer UUID - get it at https://devs.ouya.tv/developers in one of the lower paragraphs
 * .der key file - create new game and get Signing Key file https://devs.ouya.tv/developers/games
 * test product - create one at https://devs.ouya.tv/developers/products
 * your OpenFL app package ID must match the one entered at https://devs.ouya.tv/developers/games
 
Enter the above mentioned into the Main.hx.

IAPHandler.hx class defines multiple callback methods (onProductListReceived, onPurchaseSuccess, etc.).
Subclass it and override the methods with your own handlers.
One such example subclass is a MyIAPHandler.hx.

The JNI bindings are contained within a src\java\com\jarnik\iaptest\OUYA_IAP.java class. 
It's mostly a copy-pasted "iap-sample-app" from the ODK.

Contributing:
=======

My Java and JNI knowledge is fairly limited, so please don't hesitate to contribute with better methods.
