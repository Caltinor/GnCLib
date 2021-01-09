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
	public static enum FilterType {NAME, SOURCE, DATE_FROM, DATE_TO, IS_OFFER}
	//Market Specific actions
	TranslatableResult<TradeResult> createTransaction(IMarketEntry entry, MarketType type);
	
	TranslatableResult<TradeResult> closeTransaction(int id, MarketType type);
	
	TranslatableResult<TradeResult> acceptOffer(IMarketEntry entry, MarketType type, EntryOffer offer);
	
	TranslatableResult<TradeResult> expireBid(EntryAuction entry);
	
	TranslatableResult<TradeResult> executeTransaction(IMarketEntry entry, MarketType type, UUID buyer, String buyerName, int count);
	
	TranslatableResult<TradeResult> submitOffer(IMarketEntry entry, EntryOffer offer);
	
	TranslatableResult<TradeResult> placeBid(EntryBid bid);
	
	TranslatableResult<TradeResult> addToStorage(EntryStorage entry);
	
	TranslatableResult<TradeResult> pullFromStorage(EntryStorage entry, int count);
	
	TranslatableResult<TradeResult> changeTransactionSupply(MarketType type, IMarketEntry entry, int newSupply);
	
	List<IMarketEntry> getMarketList(MarketType type, int indexStart, int indexEnd, Map<FilterType, String> filters);
	
	List<EntryStorage> getStorageList(int indexStart, int indexEnd, Map<FilterType, String> filters);
	
	List<EntryBid> getBidList(int id);
	
	List<EntryOffer> getOfferList(int id);
	
	List<IMarketEntry> getTransactionHistory(MarketType type, int indexStart, int indexEnd, Map<FilterType, String> filters);
}
