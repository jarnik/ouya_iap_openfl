import flash.display.Bitmap;
import flash.display.Sprite;

import openfl.Assets;

#if android
import openfl.utils.JNI;
import tv.ouya.console.api.OuyaController;
import tv.ouya.console.api.OuyaFacade;
#end

class Main extends Sprite {
	
	public static inline var OUYA_DEVELOPER_ID:String = "a589aa6a-cf50-4f72-9313-0a515e4dab95";
	#if android
	public static var ouyaFacade:OuyaFacade;
	#end	
	
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
		
		var p:Purchases = new Purchases( ouyaFacade.__jobject );
		
		p.requestProductList(["aaa", "bbb"]);
		
		#end
		
	}
	
}

class Purchases
{	
	
	public var IAP_requestListObject:Dynamic;
	public var ouyaFacadeObject:Dynamic;
	
	public function new( ouyaFacadeObject:Dynamic )
	{
		this.ouyaFacadeObject = ouyaFacadeObject;
		#if android
		//var fn = openfl.utils.JNI.createStaticMethod("com.jarnik.iaptest.OUYA_IAP", "purchaseSomething", "(Lorg/haxe/nme/HaxeObject;)V", true);
		//fn([this]);
		
		// writing JNI bindings http://www3.ntu.edu.sg/home/ehchua/programming/java/JavaNativeInterface.html#zz-4.3
		trace("=================== JNI linking");
		IAP_requestListObject = openfl.utils.JNI.createStaticMethod
			("com.jarnik.iaptest.OUYA_IAP", "requestProductList", "([Ljava/lang/String;Ltv/ouya/console/api/OuyaFacade;)V", true);
		trace("=================== JNI linked!");
		//fn([this]);
		#end
	}
	
	public function requestProductList( products:Array<String> ):Void {
		trace("requesting " + products.join(" "));
		IAP_requestListObject( [products, ouyaFacadeObject] );
	}

	public function onPurchase(productID:String)
	{
		trace("HEYAAAAAAAA callback! "+productID);
	}
}


