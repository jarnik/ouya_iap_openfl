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

class Main extends Sprite {
	
	public static inline var OUYA_DEVELOPER_ID:String = "a589aa6a-cf50-4f72-9313-0a515e4dab95";
	#if android
	public static var ouyaFacade:OuyaFacade;
	#end
	
	private var p:IAP_Handler;
	
	public function new () {
		
		super ();
		
		addChild( new Bitmap( Assets.getBitmapData("assets/OUYA_O.png" ) ) );
		
		#if android
		var getContext = JNI.createStaticMethod ("org.haxe.nme.GameActivity", "getContext", "()Landroid/content/Context;", true);
		OuyaController.init ( getContext () );
		ouyaFacade = OuyaFacade.getInstance();
		ouyaFacade.init( getContext(), OUYA_DEVELOPER_ID );
		trace("OUYA controller & facade inited!");
		//ShopOUYA.init();
		
		// How do you call Haxe from Java (Android) http://www.openfl.org/forums/general-discussion/how-do-you-call-haxe-java-android/
		
		p = new IAP_Handler( ouyaFacade.__jobject );
		
		p.requestProductList(["test_sss_full", "__DECLINED__THIS_PURCHASE"]);
		
		stage.addEventListener (JoystickEvent.BUTTON_DOWN, stage_onJoystickButtonDown);
		
		#end
		
	}
	
	#if android
	private function stage_onJoystickButtonDown( e:JoystickEvent ):Void {
		trace("pressed button, will purchase");
		p.requestPurchase( "test_sss_full" );
	}
	#end
	
}

class IAP_Handler
{	
	
	public var initCall:Dynamic;
	public var requestProductListCall:Dynamic;
	public var getProductListIDsCall:Dynamic;
	public var requestReceiptsCall:Dynamic;
	public var getReceiptProductIDsCall:Dynamic;
	public var requestPurchaseCall:Dynamic;
	public var ouyaFacadeObject:Dynamic;
	
	public function new( ouyaFacadeObject:Dynamic )
	{
		this.ouyaFacadeObject = ouyaFacadeObject;
		#if android
		//var fn = openfl.utils.JNI.createStaticMethod("com.jarnik.iaptest.OUYA_IAP", "purchaseSomething", "(Lorg/haxe/nme/HaxeObject;)V", true);
		//fn([this]);
		
		// writing JNI bindings http://www3.ntu.edu.sg/home/ehchua/programming/java/JavaNativeInterface.html#zz-4.3
		// JNI elements https://nekonme.googlecode.com/svn/trunk/project/android/JNI.cpp
		trace("=================== JNI linking");
		initCall = openfl.utils.JNI.createStaticMethod
			("com.jarnik.iaptest.OUYA_IAP", "init", "(Lorg/haxe/nme/HaxeObject;Ltv/ouya/console/api/OuyaFacade;Ljava/lang/String;)V", true);
			//										   (Lorg/haxe/nme/HaxeObject;Ltv/ouya/console/api/OuyaFacade;[B)V
		requestProductListCall = openfl.utils.JNI.createStaticMethod
			("com.jarnik.iaptest.OUYA_IAP", "requestProductList", "([Ljava/lang/String;)V", true);
		getProductListIDsCall = openfl.utils.JNI.createStaticMethod
			("com.jarnik.iaptest.OUYA_IAP", "getProductListIDs", "()Ljava/lang/String;", true);
		requestPurchaseCall = openfl.utils.JNI.createStaticMethod
			("com.jarnik.iaptest.OUYA_IAP", "requestPurchase", "(Ljava/lang/String;)V", true);
		requestReceiptsCall = openfl.utils.JNI.createStaticMethod
			("com.jarnik.iaptest.OUYA_IAP", "requestReceipts", "()V", true);
		getReceiptProductIDsCall = openfl.utils.JNI.createStaticMethod
			("com.jarnik.iaptest.OUYA_IAP", "getReceiptProductIDs", "()Ljava/lang/String;", true);
		trace("=================== JNI linked!");
		var appKey:ByteArray =  Assets.getBytes("assets/key.der");
		
		// I don't know how to pass ByteArray to JNI, let's use Base64
		var BASE64:String = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"; 
		var base64:BaseCode = new BaseCode( Bytes.ofString( BASE64 ) );
		var appKey64:String = base64.encodeBytes( appKey ).toString();
		
		var params:Array<Dynamic> = [this, ouyaFacadeObject, appKey64 ];
		trace("gonna run initCall from Haxe");
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
	
	// ==================================== CALLBACKS ======================= 

	public function onProductListReceived()
	{
		var p:Array<String> = getProductListIDsCall().split(" ");
		trace("=== onProductListReceived! >> " + p.join("x"));
		
		requestReceipts();
	}
	public function onProductListFailed(error:String)
	{
		trace("=== onProductListFailed! "+error);
	}
	
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
	
	public function onReceiptsReceived()
	{
		var p:Array<String> = getReceiptProductIDsCall().split(" ");
		trace("=== onReceiptsReceived! >> "+p.join("x"));
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


