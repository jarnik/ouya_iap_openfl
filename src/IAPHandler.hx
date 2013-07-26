import flash.display.Bitmap;
import flash.display.Sprite;
import flash.utils.ByteArray;
import haxe.io.Bytes;
import haxe.crypto.BaseCode;

import openfl.Assets;

#if android
import openfl.utils.JNI;
import tv.ouya.console.api.OuyaController;
import tv.ouya.console.api.OuyaFacade;
import openfl.events.JoystickEvent;
#end

class IAPHandler
{	
	
	public var initCall:Dynamic;
	public var requestProductListCall:Dynamic;
	public var getProductListIDsCall:Dynamic;
	public var requestReceiptsCall:Dynamic;
	public var getReceiptProductIDsCall:Dynamic;
	public var requestPurchaseCall:Dynamic;
	public var ouyaFacadeObject:Dynamic;
	
	public function new( ouyaFacadeObject:Dynamic, DERKeyPath:String )
	{
		this.ouyaFacadeObject = ouyaFacadeObject;
		
		#if android
		
		// bind methods to JNI
		trace("=================== JNI linking methods...");
		initCall = openfl.utils.JNI.createStaticMethod
			("com.jarnik.iaptest.OUYA_IAP", "init", "(Lorg/haxe/nme/HaxeObject;Ltv/ouya/console/api/OuyaFacade;Ljava/lang/String;)V", true);
		requestProductListCall = openfl.utils.JNI.createStaticMethod
			("com.jarnik.iaptest.OUYA_IAP", "requestProductList", "([Ljava/lang/String;)V", true);
		getProductListIDsCall = openfl.utils.JNI.createStaticMethod
			("com.jarnik.iaptest.OUYA_IAP", "getProductListIDs", "()Ljava/lang/String;", true);
		requestReceiptsCall = openfl.utils.JNI.createStaticMethod
			("com.jarnik.iaptest.OUYA_IAP", "requestReceipts", "()V", true);
		getReceiptProductIDsCall = openfl.utils.JNI.createStaticMethod
			("com.jarnik.iaptest.OUYA_IAP", "getReceiptProductIDs", "()Ljava/lang/String;", true);
		requestPurchaseCall = openfl.utils.JNI.createStaticMethod
			("com.jarnik.iaptest.OUYA_IAP", "requestPurchase", "(Ljava/lang/String;)V", true);
		trace("=================== JNI methods linked!");
		var appKey:ByteArray =  Assets.getBytes( DERKeyPath );
		
		// I don't know how to pass ByteArray to JNI, let's use Base64 encoding
		var BASE:String = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"; 
		var base64:BaseCode = new BaseCode( Bytes.ofString( BASE ) );
		var appKey64:String = base64.encodeBytes( appKey ).toString();
		
		// init the IAP
		var params:Array<Dynamic> = [this, ouyaFacadeObject, appKey64 ];
		initCall( params );
		#end
	}
	
	public function requestProductList( products:Array<String> ):Void {
		trace("requesting " + products.join(" "));
		requestProductListCall( [products] );
	}
	
	public function requestPurchase( product:String ):Void {
		trace("purchasing " + product );
		requestPurchaseCall( [product] );
	}
	
	public function requestReceipts():Void {
		trace("requestReceipts");
		requestReceiptsCall( [] );
	}
	
	public function getProductListIDs():String {
		// will return list of received product identifiers, delimited by space character
		return getProductListIDsCall();
	}
	
	public function getReceiptProductIDs():String {
		// will return list of purchased products identifiers, delimited by space character
		return getReceiptProductIDsCall();
	}
	
	// ==================================== CALLBACKS - OVERRIDE THESE! ======================= 

	// ==== Product List
	public function onProductListReceived()
	{
		var p:Array<String> = getProductListIDs().split(" ");
		trace("=== onProductListReceived! " + p.join(" "));
		
		requestReceipts();
	}
	public function onProductListFailed(error:String)
	{
		trace("=== onProductListFailed! "+error);
	}
	
	// ==== Purchasing
	public function onPurchaseSuccess(productID:String)
	{
		trace("=== onPurchaseSuccess! "+productID);
	}
	public function onPurchaseFailed(error:String)
	{
		trace("=== onPurchaseFailed! "+error);
	}
	public function onPurchaseCancelled()
	{
		trace("=== onPurchaseCancelled! ");
	}
	
	// ==== Receipt List
	public function onReceiptsReceived()
	{
		var p:Array<String> = getReceiptProductIDs().split(" ");
		trace("=== onReceiptsReceived! "+p.join("x"));
	}
	public function onReceiptsFailed( error:String )
	{
		trace("=== onReceiptsFailed "+error);
	}
	public function onReceiptsCancelled()
	{
		trace("=== onReceiptsCancelled ");
	}
}


