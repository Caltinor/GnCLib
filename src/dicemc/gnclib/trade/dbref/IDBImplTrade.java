package dicemc.gnclib.trade.dbref;

import java.util.List;
import java.util.UUID;

import dicemc.gnclib.trade.entries.EntryBid;
import dicemc.gnclib.trade.entries.EntryOffer;
import dicemc.gnclib.trade.entries.EntryStorage;
import dicemc.gnclib.trade.entries.IMarketEntry;
import dicemc.gnclib.util.TranslatableResult;

public interface IDBImplTrade {
	public static enum TradeResult {SUCCESS, FAILURE}
	public static enum MarketType {LOCAL, GLOBAL, AUCTION, SERVER}
	//Market Specific actions
	TranslatableResult<TradeResult> createTransaction(IMarketEntry entry, MarketType type);
	
	TranslatableResult<TradeResult> closeTransaction(int id);
	
	TranslatableResult<TradeResult> executeTransaction(IMarketEntry entry, MarketType type, UUID vendor, String vendorName);
	
	TranslatableResult<TradeResult> submitOffer(int id, EntryOffer offer);
	
	TranslatableResult<TradeResult> placeBid(EntryBid bid);
	
	TranslatableResult<TradeResult> addToStorage(EntryStorage entry);
	
	TranslatableResult<TradeResult> pullFromStorage(int id);
	
	TranslatableResult<TradeResult> changeTransactionSupply(int id, int newSupply);
	
	List<IMarketEntry> getMarketList(MarketType type, int indexStart, int indexEnd);
	
	List<EntryStorage> getStorageList(int indexStart, int indexEnd);
	
	List<EntryBid> getBidList(int id);
	
	List<EntryOffer> getOfferList(int id);
	
	List<IMarketEntry> getTransactionHistory(MarketType type, int indexStart, int indexEnd);
}
