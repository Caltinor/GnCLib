package dicemc.gnclib.trade.dbref;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import dicemc.gnclib.trade.entries.EntryAuction;
import dicemc.gnclib.trade.entries.EntryBid;
import dicemc.gnclib.trade.entries.EntryOffer;
import dicemc.gnclib.trade.entries.EntryStorage;
import dicemc.gnclib.trade.entries.IMarketEntry;
import dicemc.gnclib.util.Agent;
import dicemc.gnclib.util.TranslatableResult;
import dicemc.gnclib.util.Agent.Type;
import dicemc.gnclib.util.ResultType;

public interface IDBImplTrade {
	public static enum MarketType {LOCAL, GLOBAL, AUCTION, SERVER}
	public static enum FilterType {SOURCE_VENDOR, SOURCE_LOCALITY, PRICE_FROM, PRICE_TO, IS_OFFER, ORDER_PRICE, INCLUDE_MY_SALES}
	public static enum tblMarkets {ID, ITEM, VENDOR_ID, LOCALITY, BID_END, PRICE, VENDOR_GIVE_ITEM, STOCK, ACTIVE_TRANSACTION, BUYER_ID, DTG_PLACED, DTG_CLOSED}
	public static enum tblBids {TABLE_NAME, ID, TRANSACTION_ID, BIDDER_ID, DTG_PLACED, PRICE}
	public static enum tblStorage {TABLE_NAME, ID, OWNER, ITEM, QUANTITY}
	public static enum tblOffers {TABLE_NAME, ID, MARKET_NAME, TRANS_ID, ITEM, OFFERER, DTG_PLACED,	REQUESTED_AMOUNT, OFFERRED_AMOUNT}
	public static enum tblTransactors {TABLE_NAME, ID, REF_ID, TYPE, NAME}
	//Market Specific actions
	TranslatableResult<ResultType> createTransaction(IMarketEntry entry, MarketType type);
	
	TranslatableResult<ResultType> closeTransaction(int id, MarketType type);
	
	TranslatableResult<ResultType> acceptOffer(IMarketEntry entry, MarketType type, EntryOffer offer);
	
	TranslatableResult<ResultType> expireBid(EntryAuction entry);
	
	TranslatableResult<ResultType> executeTransaction(IMarketEntry entry, MarketType type, Agent buyer, int count);
	
	TranslatableResult<ResultType> submitOffer(IMarketEntry entry, EntryOffer offer, MarketType type);
	
	TranslatableResult<ResultType> placeBid(EntryBid bid, double itemValue);
	
	TranslatableResult<ResultType> addToStorage(EntryStorage entry);
	
	TranslatableResult<ResultType> pullFromStorage(EntryStorage entry, int count);
	
	TranslatableResult<ResultType> changeTransactionSupply(MarketType type, IMarketEntry entry, int newSupply);
	
	List<IMarketEntry> getMarketList(MarketType type, int indexStart, int rowCount, Map<FilterType, String> filters, boolean isHistory);
	
	List<EntryStorage> getStorageList(int indexStart, int rowCount, Agent owner);
	
	List<EntryBid> getBidList(int id);
	
	List<EntryOffer> getOfferList(int id, MarketType type);
	
	IMarketEntry getMarketEntry(int id, MarketType type);
	
	EntryStorage getStorageEntry(int id);
	
	Agent getTransactor(int id);
	
	Agent getTransactor(UUID refID, Type type, String name);
}
