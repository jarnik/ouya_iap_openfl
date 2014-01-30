package com.jarnik.iaptest;

import org.haxe.lime.HaxeObject;
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
import tv.ouya.console.api.Receipt;
import tv.ouya.console.api.OuyaFacade;
import tv.ouya.console.api.CancelIgnoringOuyaResponseListener;
import tv.ouya.console.api.OuyaResponseListener;
import tv.ouya.console.api.OuyaErrorCodes;
import tv.ouya.console.api.OuyaEncryptionHelper;
import tv.ouya.console.api.GamerInfo;

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
	// This is mostly a copy-paste of ODK's iap-sample-app --Jaroslav Meloun
	
    private static final Map<String, Product> mOutstandingPurchaseRequests = new HashMap<String, Product>();
	
	public static List<Purchasable> PRODUCT_IDENTIFIER_LIST;
	private static List<Product> mProductList; 
	private static List<Receipt> mReceiptList;
	private static String mLastPurchasedProductID;
	
	public static HaxeObject mCallback;
	private static OuyaFacade mOuyaFacade;
	private static PublicKey mPublicKey;
	
	public static void init(final HaxeObject callback, OuyaFacade ouyaFacade, String APPLICATION_KEY_64 )
	{
		mOuyaFacade = ouyaFacade;
		mCallback = callback;
		
		byte[] APPLICATION_KEY = Base64.decode( APPLICATION_KEY_64, Base64.NO_WRAP );
        // Create a PublicKey object from the key data downloaded from the developer portal.
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(APPLICATION_KEY);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            mPublicKey = keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            Log.e("IAP", "Unable to create encryption key", e);
        }
	}
	
	// ======================================= PRODUCT LIST =====================================================
	
	public static void requestProductList(String[] products)
	{
		PRODUCT_IDENTIFIER_LIST = new ArrayList<Purchasable>();
		for ( String s : products )
			PRODUCT_IDENTIFIER_LIST.add( new Purchasable( s ) );
		
		mOuyaFacade.requestProductList(PRODUCT_IDENTIFIER_LIST, new CancelIgnoringOuyaResponseListener<ArrayList<Product>>() {
            @Override
            public void onSuccess(final ArrayList<Product> products) {
                mProductList = products;
				for(Product p : products) {
                    Log.d("IAP", p.getName() + " costs " + p.getPriceInCents());
                }
				Log.d("IAP", "Received "+products.size()+" products." );
				mCallback.call("onProductListReceived", new Object[] {} );
            }

            @Override
            public void onFailure(int errorCode, String errorMessage, Bundle optionalData) {
                // Your app probably wants to do something more sophisticated than popping a Toast. This is
                // here to tell you that your app needs to handle this case: if your app doesn't display
                // something, the user won't know of the failure.
                //Toast.makeText(IapSampleActivity.this, "Could not fetch product information (error " + errorCode + ": " + errorMessage + ")", Toast.LENGTH_LONG).show();
				mCallback.call("onProductListFailed",  new Object[] { errorCode + ": " + errorMessage } );
            }
        }); 
	}
	
	public static String getProductListIDs() {
		// will return list of received product identifiers, delimited by space character
		String product_ids = "";
		for ( int i = 0; i < mProductList.size(); i++ )
			product_ids += ( i > 0 ? " ": "" ) + mProductList.get( i ).getIdentifier();
		return product_ids;
	}
	
	// ======================================= PURCHASE =====================================================
		
	public static void requestPurchase( String productName ) 
		throws GeneralSecurityException, UnsupportedEncodingException, JSONException {
		
		Product product = null;
		for ( Product p: mProductList )
			if ( p.getIdentifier().equals( productName ) ) {
				product = p;
				break;
			}
		if ( product == null ) {
			Log.w("IAP", "Requested product ID "+productName+" not found in products!" );
            return;
		}
			
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");

        // This is an ID that allows you to associate a successful purchase with
        // it's original request. The server does nothing with this string except
        // pass it back to you, so it only needs to be unique within this instance
        // of your app to allow you to pair responses with requests.
        String uniqueId = Long.toHexString(sr.nextLong());

        JSONObject purchaseRequest = new JSONObject();
        purchaseRequest.put("uuid", uniqueId);
        purchaseRequest.put("identifier", product.getIdentifier());
        String purchaseRequestJson = purchaseRequest.toString();

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
        mOuyaFacade.requestPurchase(purchasable, new PurchaseListener(product));
	}
	
	public static String getLastPurchasedProductID() {
		return mLastPurchasedProductID;
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

			mLastPurchasedProductID = mProduct.getIdentifier();
			mCallback.call("onPurchaseSuccess", new Object[] { } );
        }

        @Override
        public void onFailure(int errorCode, String errorMessage, Bundle optionalData) {
			mCallback.call("onPurchaseFailed", new Object[] { errorCode+": "+errorMessage } );
        }

        /*
         * Handling the user canceling
         */
        @Override
        public void onCancel() {
			mCallback.call("onPurchaseCancelled", new Object[] {} );
        }
    }
	
	// ======================================= RECEIPTS =====================================================
	
	public static void requestReceipts() {
        mOuyaFacade.requestReceipts(new ReceiptListener());
    }

    private static void showError(final String errorMessage) {
		mCallback.call("onReceiptsFailed", new Object[] { errorMessage } );
    }

    /**
     * The callback for when the list of user receipts has been requested.
     */
    public static class ReceiptListener implements OuyaResponseListener<String>
    {
        /**
         * Handle the successful fetching of the data for the receipts from the server.
         *
         * @param receiptResponse The response from the server.
         */
        @Override
        public void onSuccess(String receiptResponse) {
            OuyaEncryptionHelper helper = new OuyaEncryptionHelper();
            List<Receipt> receipts;
            try {
                JSONObject response = new JSONObject(receiptResponse);
                if(response.has("key") && response.has("iv")) {
                    receipts = helper.decryptReceiptResponse(response, mPublicKey);
                } else {
                    receipts = helper.parseJSONReceiptResponse(receiptResponse);
                }
            } catch (ParseException e) {
                throw new RuntimeException(e);
            } catch (JSONException e) {
                if(e.getMessage().contains("ENCRYPTED")) {
                    // This is a hack for some testing code which will be removed
                    // before the consumer release
                    try {
                        receipts = helper.parseJSONReceiptResponse(receiptResponse);
                    } catch (IOException ioe) {
                        throw new RuntimeException(ioe);
                    }
                } else {
                    throw new RuntimeException(e);
                }
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Collections.sort(receipts, new Comparator<Receipt>() {
                @Override
                public int compare(Receipt lhs, Receipt rhs) {
                    return rhs.getPurchaseDate().compareTo(lhs.getPurchaseDate());
                }
            });

            mReceiptList = receipts;
			String[] receipt_ids = new String[ mReceiptList.size() ];
			for ( int i = 0; i < mReceiptList.size(); i++ )
				receipt_ids[ i ] = mReceiptList.get( i ).getIdentifier();
			mCallback.call("onReceiptsReceived", new Object[] { receipt_ids } );
        }

        /**
         * Handle a failure. Because displaying the receipts is not critical to the application we just show an error
         * message rather than asking the user to authenticate themselves just to start the application up.
         *
         * @param errorCode An HTTP error code between 0 and 999, if there was one. Otherwise, an internal error code from the
         *                  Ouya server, documented in the {@link OuyaErrorCodes} class.
         *
         * @param errorMessage Empty for HTTP error codes. Otherwise, a brief, non-localized, explanation of the error.
         *
         * @param optionalData A Map of optional key/value pairs which provide additional information.
         */

        @Override
        public void onFailure(int errorCode, String errorMessage, Bundle optionalData) {
            showError("Could not fetch receipts (error " + errorCode + ": " + errorMessage + ")");
        }

        /*
         * Handle user canceling
         */
        @Override
        public void onCancel()
        {
			mCallback.call("onReceiptsCancelled", new Object[] {} );
        }
    }
	
	public static String getReceiptProductIDs() {
		// will return list of purchased products identifiers, delimited by space character
		String product_ids = "";
		for ( int i = 0; i < mReceiptList.size(); i++ )
			product_ids += ( i > 0 ? " ": "" ) + mReceiptList.get( i ).getIdentifier();
		return product_ids;
	}
	
}

