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
	public static inline var PRODUCT_IDENTIFIER:String = "test_sss_full";
	public static inline var DER_KEY_PATH:String = "assets/key.der";
	
	#if android
	public static var ouyaFacade:OuyaFacade;
	#end
	
	private var handler:MyIAPHandler;
	private var testProductPurchased:Bool;
	private var icon:Bitmap;
	
	public function new () {
		
		// Help:
		// How do you call Haxe from Java (Android) http://www.openfl.org/forums/general-discussion/how-do-you-call-haxe-java-android/
		// writing JNI bindings http://www3.ntu.edu.sg/home/ehchua/programming/java/JavaNativeInterface.html#zz-4.3
		// JNI elements https://nekonme.googlecode.com/svn/trunk/project/android/JNI.cpp
		
		super ();
		
		testProductPurchased = false;
		addChild( icon = new Bitmap( Assets.getBitmapData("assets/OUYA_O.png" ) ) );
		icon.visible = false;
		
		#if android
		var getContext = JNI.createStaticMethod ("org.haxe.nme.GameActivity", "getContext", "()Landroid/content/Context;", true);
		OuyaController.init ( getContext () );
		ouyaFacade = OuyaFacade.getInstance();
		ouyaFacade.init( getContext(), OUYA_DEVELOPER_ID );
		trace("OUYA controller & facade inited!");
		
		handler = new MyIAPHandler( ouyaFacade.__jobject, DER_KEY_PATH, receiptsReceived );
		handler.requestProductList([ 
			PRODUCT_IDENTIFIER
			//, enter more product IDs here...
		]); 
		
		stage.addEventListener (JoystickEvent.BUTTON_DOWN, stage_onJoystickButtonDown);
		#end
	}
	
	#if android
	private function stage_onJoystickButtonDown( e:JoystickEvent ):Void {
		if ( testProductPurchased )
			return;
		
		trace("OUYA button pressed, starting purchase");
		handler.requestPurchase( PRODUCT_IDENTIFIER );
	}
	#end
	
	private function receiptsReceived( products:Array<String> ):Void {
		trace("===== Main - Receipts received");
		icon.visible = true;
		for ( p in products )
			if ( p == PRODUCT_IDENTIFIER ) {
				trace("===== Main - product purchased");
				icon.visible = false;
				testProductPurchased = true;
			}
	}
	
}