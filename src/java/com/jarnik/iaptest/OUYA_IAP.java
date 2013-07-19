package com.jarnik.iaptest;

import org.haxe.nme.HaxeObject;
import java.util.Arrays;
import android.util.Log;
import java.util.List;
import java.util.ArrayList;
import android.os.Bundle;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import tv.ouya.console.api.Purchasable;
import tv.ouya.console.api.Product;
import tv.ouya.console.api.OuyaFacade;
import tv.ouya.console.api.CancelIgnoringOuyaResponseListener;
import tv.ouya.console.api.OuyaResponseListener;
import tv.ouya.console.api.OuyaErrorCodes;
import tv.ouya.console.api.OuyaEncryptionHelper;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.util.*;

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
	
    private static final byte[] APPLICATION_KEY = {
            (byte) 0x30, 
(byte) 0x81, 
(byte) 0x9f, 
(byte) 0x30, 
(byte) 0x0d, 
(byte) 0x06, 
(byte) 0x09, 
(byte) 0x2a, 
(byte) 0x86, 
(byte) 0x48, 
(byte) 0x86, 
(byte) 0xf7, 
(byte) 0x0d, 
(byte) 0x01, 
(byte) 0x01, 
(byte) 0x01, 
(byte) 0x05, 
(byte) 0x00, 
(byte) 0x03, 
(byte) 0x81, 
(byte) 0x8d, 
(byte) 0x00, 
(byte) 0x30, 
(byte) 0x81, 
(byte) 0x89, 
(byte) 0x02, 
(byte) 0x81, 
(byte) 0x81, 
(byte) 0x00, 
(byte) 0xc9, 
(byte) 0x8a, 
(byte) 0xb4, 
(byte) 0x84, 
(byte) 0x78, 
(byte) 0xa3, 
(byte) 0x89, 
(byte) 0xea, 
(byte) 0x5b, 
(byte) 0x49, 
(byte) 0xed, 
(byte) 0x69, 
(byte) 0x42, 
(byte) 0xa1, 
(byte) 0x11, 
(byte) 0xe3, 
(byte) 0x9d, 
(byte) 0x8e, 
(byte) 0x21, 
(byte) 0xb0, 
(byte) 0xfb, 
(byte) 0x9a, 
(byte) 0x06, 
(byte) 0x61, 
(byte) 0x35, 
(byte) 0xdd, 
(byte) 0x66, 
(byte) 0xd4, 
(byte) 0x31, 
(byte) 0x5e, 
(byte) 0x62, 
(byte) 0xa3, 
(byte) 0x13, 
(byte) 0xb7, 
(byte) 0x7f, 
(byte) 0x26, 
(byte) 0x58, 
(byte) 0xff, 
(byte) 0x3b, 
(byte) 0xe5, 
(byte) 0x04, 
(byte) 0x0e, 
(byte) 0xab, 
(byte) 0xbc, 
(byte) 0x4f, 
(byte) 0x48, 
(byte) 0xeb, 
(byte) 0x57, 
(byte) 0xae, 
(byte) 0xaa, 
(byte) 0xe2, 
(byte) 0x43, 
(byte) 0x64, 
(byte) 0x2d, 
(byte) 0xc1, 
(byte) 0x44, 
(byte) 0x36, 
(byte) 0xe1, 
(byte) 0xdc, 
(byte) 0xae, 
(byte) 0xf1, 
(byte) 0x1e, 
(byte) 0x90, 
(byte) 0x8c, 
(byte) 0x11, 
(byte) 0xa6, 
(byte) 0x17, 
(byte) 0x66, 
(byte) 0x02, 
(byte) 0xa2, 
(byte) 0x50, 
(byte) 0xd5, 
(byte) 0x9c, 
(byte) 0x78, 
(byte) 0xc9, 
(byte) 0x07, 
(byte) 0x30, 
(byte) 0x09, 
(byte) 0xef, 
(byte) 0x15, 
(byte) 0x63, 
(byte) 0x5f, 
(byte) 0x61, 
(byte) 0x89, 
(byte) 0xef, 
(byte) 0x38, 
(byte) 0xd3, 
(byte) 0x43, 
(byte) 0x0f, 
(byte) 0x6c, 
(byte) 0x96, 
(byte) 0x32, 
(byte) 0x35, 
(byte) 0xd5, 
(byte) 0xdc, 
(byte) 0x5e, 
(byte) 0x71, 
(byte) 0xfd, 
(byte) 0xd1, 
(byte) 0xa6, 
(byte) 0x43, 
(byte) 0x13, 
(byte) 0x2e, 
(byte) 0xa1, 
(byte) 0x03, 
(byte) 0x11, 
(byte) 0x52, 
(byte) 0x82, 
(byte) 0x50, 
(byte) 0x63, 
(byte) 0xc6, 
(byte) 0x00, 
(byte) 0x81, 
(byte) 0xbd, 
(byte) 0x38, 
(byte) 0x0c, 
(byte) 0xc0, 
(byte) 0x7d, 
(byte) 0xb1, 
(byte) 0x9e, 
(byte) 0x64, 
(byte) 0xab, 
(byte) 0x8c, 
(byte) 0xa0, 
(byte) 0x50, 
(byte) 0xc6, 
(byte) 0x3e, 
(byte) 0x5b, 
(byte) 0x02, 
(byte) 0x03, 
(byte) 0x01, 
(byte) 0x00, 
(byte) 0x01, 
    };
	
	/**
     * The outstanding purchase request UUIDs.
     */

    private static final Map<String, Product> mOutstandingPurchaseRequests = new HashMap<String, Product>();
	
	public static List<Purchasable> PRODUCT_IDENTIFIER_LIST;
	private static List<Product> mProductList; 
	private static OuyaFacade mOuyaFacade;
	private static PublicKey mPublicKey;
	
	public static void init(OuyaFacade ouyaFacade)
	{
		mOuyaFacade = ouyaFacade;
		
        // Create a PublicKey object from the key data downloaded from the developer portal.
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(APPLICATION_KEY);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            mPublicKey = keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            Log.e("IAP", "Unable to create encryption key", e);
        }
	}
	
	public static void requestProductList(String[] products)
	{
		Log.d("IAP", " will request "+products[0]+" etc " );
		PRODUCT_IDENTIFIER_LIST = Arrays.asList(
			new Purchasable("test_sss_full")
			//new Purchasable("__DECLINED__THIS_PURCHASE")
		); 
		
		Log.d("IAP", "========== created product list" );
		
		mOuyaFacade.requestProductList(PRODUCT_IDENTIFIER_LIST, new CancelIgnoringOuyaResponseListener<ArrayList<Product>>() {
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
	
	public static void requestPurchase( String productName ) 
		throws GeneralSecurityException, UnsupportedEncodingException, JSONException {
		
		Product product = mProductList.get( 0 );
		
		Log.d("IAP", "========== requesting purchase " );
			
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");

        // This is an ID that allows you to associate a successful purchase with
        // it's original request. The server does nothing with this string except
        // pass it back to you, so it only needs to be unique within this instance
        // of your app to allow you to pair responses with requests.
        String uniqueId = Long.toHexString(sr.nextLong());

        JSONObject purchaseRequest = new JSONObject();
        purchaseRequest.put("uuid", uniqueId);
        purchaseRequest.put("identifier", product.getIdentifier());
        //purchaseRequest.put("testing", "true"); // This value is only needed for testing, not setting it results in a live purchase
        String purchaseRequestJson = purchaseRequest.toString();
		Log.w("IAP", "HEYYA requesting "+product.getIdentifier()+" uuid "+uniqueId);

        byte[] keyBytes = new byte[16];
        sr.nextBytes(keyBytes);
        SecretKey key = new SecretKeySpec(keyBytes, "AES");

        byte[] ivBytes = new byte[16];
        sr.nextBytes(ivBytes);
        IvParameterSpec iv = new IvParameterSpec(ivBytes);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] payload = cipher.doFinal(purchaseRequestJson.getBytes("UTF-8"));

        cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, mPublicKey);
        byte[] encryptedKey = cipher.doFinal(keyBytes);

        Purchasable purchasable =
                new Purchasable(
                        product.getIdentifier(),
                        Base64.encodeToString(encryptedKey, Base64.NO_WRAP),
                        Base64.encodeToString(ivBytes, Base64.NO_WRAP),
                        Base64.encodeToString(payload, Base64.NO_WRAP) );

        synchronized (mOutstandingPurchaseRequests) {
            mOutstandingPurchaseRequests.put(uniqueId, product);
        }
		Log.w("IAP", "HEYYA ouyaFacade.requestPurchase");
        mOuyaFacade.requestPurchase(purchasable, new PurchaseListener(product));
	}
	
	
    /**
     * The callback for when the user attempts to purchase something. We're not worried about
     * the user cancelling the purchase so we extend CancelIgnoringOuyaResponseListener, if
     * you want to handle cancelations differently you should extend OuyaResponseListener and
     * implement an onCancel method.
     *
     * @see tv.ouya.console.api.CancelIgnoringOuyaResponseListener
     * @see tv.ouya.console.api.OuyaResponseListener#onCancel()
     */
    public static class PurchaseListener implements OuyaResponseListener<String> {
        /**
         * The ID of the product the user is trying to purchase. This is used in the
         * onFailure method to start a re-purchase if they user wishes to do so.
         */

        private Product mProduct;

        /**
         * Constructor. Store the ID of the product being purchased.
         */

        PurchaseListener(final Product product) {
            mProduct = product;
        }

        /**
         * Handle a successful purchase.
         *
         * @param result The response from the server.
         */
        @Override
        public void onSuccess(String result) {
            Product product;
            String id;
			Log.w("IAP", "HEYYA PurchaseListener.onSuccess");
            try {
                OuyaEncryptionHelper helper = new OuyaEncryptionHelper();

                JSONObject response = new JSONObject(result);
                if(response.has("key") && response.has("iv")) {
                    id = helper.decryptPurchaseResponse(response, mPublicKey);
                    Product storedProduct;
                    synchronized (mOutstandingPurchaseRequests) {
                        storedProduct = mOutstandingPurchaseRequests.remove(id);
                    }
                    if(storedProduct == null || !storedProduct.getIdentifier().equals(mProduct.getIdentifier())) {
                        onFailure(OuyaErrorCodes.THROW_DURING_ON_SUCCESS, "Purchased product is not the same as purchase request product", Bundle.EMPTY);
                        return;
                    }
                } else {
                    product = new Product(new JSONObject(result));
                    if(!mProduct.getIdentifier().equals(product.getIdentifier())) {
                        onFailure(OuyaErrorCodes.THROW_DURING_ON_SUCCESS, "Purchased product is not the same as purchase request product", Bundle.EMPTY);
                        return;
                    }
                }
            } catch (ParseException e) {
                onFailure(OuyaErrorCodes.THROW_DURING_ON_SUCCESS, e.getMessage(), Bundle.EMPTY);
            } catch (JSONException e) {
                if(e.getMessage().contains("ENCRYPTED")) {
                    // This is a hack for some testing code which will be removed
                    // before the consumer release
                    try {
                        product = new Product(new JSONObject(result));
                        if(!mProduct.getIdentifier().equals(product.getIdentifier())) {
                            onFailure(OuyaErrorCodes.THROW_DURING_ON_SUCCESS, "Purchased product is not the same as purchase request product", Bundle.EMPTY);
                            return;
                        }
                    } catch (JSONException jse) {
                        onFailure(OuyaErrorCodes.THROW_DURING_ON_SUCCESS, e.getMessage(), Bundle.EMPTY);
                        return;
                    }
                } else {
                    onFailure(OuyaErrorCodes.THROW_DURING_ON_SUCCESS, e.getMessage(), Bundle.EMPTY);
                    return;
                }
            } catch (IOException e) {
                onFailure(OuyaErrorCodes.THROW_DURING_ON_SUCCESS, e.getMessage(), Bundle.EMPTY);
                return;
            } catch (GeneralSecurityException e) {
                onFailure(OuyaErrorCodes.THROW_DURING_ON_SUCCESS, e.getMessage(), Bundle.EMPTY);
                return;
            }

            //requestReceipts();

        }

        @Override
        public void onFailure(int errorCode, String errorMessage, Bundle optionalData) {
			Log.w("IAP", "HEYYA PurchaseListener.onFailure");
        }

        /*
         * Handling the user canceling
         */
        @Override
        public void onCancel() {
			Log.w("IAP", "HEYYA PurchaseListener.onCancel");
            //showError("User cancelled purchase");
        }
    }
	
}

