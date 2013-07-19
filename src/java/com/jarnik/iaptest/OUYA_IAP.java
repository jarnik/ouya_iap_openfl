package com.jarnik.iaptest;

import org.haxe.nme.HaxeObject;
import java.util.Arrays;
import android.util.Log;
import java.util.List;
import java.util.ArrayList;
import android.os.Bundle;

import tv.ouya.console.api.Purchasable;
import tv.ouya.console.api.Product;
import tv.ouya.console.api.OuyaFacade;
import tv.ouya.console.api.CancelIgnoringOuyaResponseListener;

public class OUYA_IAP
{
	/*
	public static void requestProduct(final HaxeObject callback)
	{
		GameActivity.getInstance().runOnUiThread
		(
			new Runnable()
			{ 
				public void run() 
				{
					callback.call("onPurchase", new Object[] {"junk"});
				}
			}
		);
		//callback.call("onPurchase", new Object[] {"junk"});
	}*/

	public static List<Purchasable> PRODUCT_IDENTIFIER_LIST;
	private static List<Product> mProductList; 
	
	public static void requestProductList(String[] products, OuyaFacade ouyaFacade)
	{
		/*GameActivity.getInstance().runOnUiThread
		(
			new Runnable()
			{ 
				public void run() 
				{
					callback.call("onPurchase", new Object[] {"junk"});
				}
			}
		);*/
		Log.d("IAP", " will request "+products[0]+" etc " );
		PRODUCT_IDENTIFIER_LIST = Arrays.asList(
			new Purchasable("test_sss_full"),
			new Purchasable("__DECLINED__THIS_PURCHASE")
		); 
		
		Log.d("IAP", "========== created product list" );
		
		ouyaFacade.requestProductList(PRODUCT_IDENTIFIER_LIST, new CancelIgnoringOuyaResponseListener<ArrayList<Product>>() {
            @Override
            public void onSuccess(final ArrayList<Product> products) {
                mProductList = products;
                //addProducts();
				Log.d("IAP", "========== SUCCESS " );
            }

            @Override
            public void onFailure(int errorCode, String errorMessage, Bundle optionalData) {
                // Your app probably wants to do something more sophisticated than popping a Toast. This is
                // here to tell you that your app needs to handle this case: if your app doesn't display
                // something, the user won't know of the failure.
                //Toast.makeText(IapSampleActivity.this, "Could not fetch product information (error " + errorCode + ": " + errorMessage + ")", Toast.LENGTH_LONG).show();
				Log.d("IAP", "========== FAIL " );
            }
        }); 
		
		Log.d("IAP", "========== requested product list " );
		
		//callback.call("onPurchase", new Object[] {"junk"});
	}
}