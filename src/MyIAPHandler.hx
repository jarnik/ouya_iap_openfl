
class MyIAPHandler extends IAPHandler 
{
	
	private var receiptsReceivedCallback:Array<String>->Void;
	
	public function new( ouyaFacadeObject:Dynamic, DERKeyPath:String, receiptsReceivedCallback:Array<String>->Void )
	{
		super( ouyaFacadeObject, DERKeyPath );
		this.receiptsReceivedCallback = receiptsReceivedCallback;
	}

	override public function onPurchaseSuccess()
	{
		trace("=== onPurchaseSuccess! " + getLastPurchasedProductID());
		requestReceipts();
	}
	
	override public function onReceiptsReceived()
	{
		receiptsReceivedCallback( getReceiptProductIDs() );
	}
	
}


