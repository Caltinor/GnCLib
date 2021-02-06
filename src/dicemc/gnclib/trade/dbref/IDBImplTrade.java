package dicemc.gnclib.trade.dbref;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import dicemc.gnclib.trade.entries.EntryAuction;
import dicemc.gnclib.trade.entries.EntryBid;
import dicemc.gnclib.trade.entries.EntryOffer;
import dicemc.gnclib.trade.entries.EntryStorage;
import dicemc.gnclib.trade.entries.IMarketEntry;
import dicemc.gnclib.util.TranslatableResult;

public interface IDBImplTrade {
	public static enum TradeResult {SUCCESS, FAILURE}
	public static enum MarketType {LOCAL, GLOBAL, AUCTION, SERVER}
	public static enum FilterType {SOURCE_VENDOR, SOURCE_LOCALITY, PRICE_FROM, PRICE_TO, IS_OFFER, ORDER_PRICE, INCLUDE_MY_SALES}
	public static enum tblMarkets {ID, ITEM, VENDOR_ID, VENDOR_NAME, LOCALITY, BID_END, PRICE,
		VENDOR_GIVE_ITEM, STOCK, ACTIVE_TRANSACTION, BUYER_ID, BUYER_NAME, DTG_PLACED, DTG_CLOSED}
	public static enum tblBids {TABLE_NAME, ID, TRANSACTION_ID, BIDDER_ID, BIDDER_NAME, DTG_PLACED, PRICE}
	public static enum tblStorage {TABLE_NAME, ID, OWNER, ITEM, QUANTITY}
	public static enum tblOffers {TABLE_NAME, ID, MARKET_NAME, TRANS_ID, ITEM, OFFERER, OFFERER_NAME, DTG_PLACED,
		REQUESTED_AMOUNT, OFFERRED_AMOUNT}
	//Market Specific actions
	TranslatableResult<TradeResult> createTransaction(IMarketEntry entry, MarketType type);
	
	TranslatableResult<TradeResult> closeTransaction(int id, MarketType type);
	
	TranslatableResult<TradeResult> acceptOffer(IMarketEntry entry, MarketType type, EntryOffer offer);
	
	TranslatableResult<TradeResult> expireBid(EntryAuction entry);
	
	TranslatableResult<TradeResult> executeTransaction(IMarketEntry entry, MarketType type, UUID buyer, String buyerName, int count);
	
	TranslatableResult<TradeResult> submitOffer(IMarketEntry entry, EntryOffer offer);
	
	TranslatableResult<TradeResult> placeBid(EntryBid bid, double itemValue);
	
	TranslatableResult<TradeResult> addToStorage(EntryStorage entry);
	
	TranslatableResult<TradeResult> pullFromStorage(EntryStorage entry, int count);
	
	TranslatableResult<TradeResult> changeTransactionSupply(MarketType type, IMarketEntry entry, int newSupply);
	
	List<IMarketEntry> getMarketList(MarketType type, int indexStart, int rowCount, Map<FilterType, String> filters, boolean isHistory);
	
	List<EntryStorage> getStorageList(int indexStart, int rowCount, UUID owner);
	
	List<EntryBid> getBidList(int id);
	
	List<EntryOffer> getOfferList(int id, MarketType type);
	
	IMarketEntry getMarketEntry(int id, MarketType type);
	
	EntryStorage getStorageEntry(int id);
}
