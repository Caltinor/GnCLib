package dicemc.gnclib.trade;

import java.util.List;
import java.util.UUID;

import dicemc.gnclib.trade.dbref.IDBImplTrade;
import dicemc.gnclib.trade.entries.EntryBid;
import dicemc.gnclib.trade.entries.EntryOffer;
import dicemc.gnclib.trade.entries.EntryStorage;
import dicemc.gnclib.trade.entries.IMarketEntry;
import dicemc.gnclib.util.TranslatableResult;

public class Marketplace implements IDBImplTrade{
	public String marketName;
	public double buyFee, sellFee;
	
	public Marketplace(String name, double buyFee, double sellFee) {
		this.marketName = name;
		this.buyFee = buyFee;
		this.sellFee = sellFee;
	}
	public Marketplace(String name) {
		this(name, 0d, 0d);
	}
	

	@Override
	public TranslatableResult<TradeResult> createTransaction(IMarketEntry entry, MarketType type) {
		//TODO conduct account logic and market-specific checks before performing data manipulations through the DB Service
		return LogicTrade.service.createTransaction(entry, type);
	}

	@Override
	public TranslatableResult<TradeResult> closeTransaction(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TranslatableResult<TradeResult> executeTransaction(IMarketEntry entry, MarketType type, UUID vendor,
			String vendorName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TranslatableResult<TradeResult> submitOffer(int id, EntryOffer offer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TranslatableResult<TradeResult> placeBid(EntryBid bid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TranslatableResult<TradeResult> addToStorage(EntryStorage entry) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TranslatableResult<TradeResult> pullFromStorage(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TranslatableResult<TradeResult> changeTransactionSupply(int id, int newSupply) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IMarketEntry> getMarketList(MarketType type, int indexStart, int indexEnd) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<EntryStorage> getStorageList(int indexStart, int indexEnd) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<EntryBid> getBidList(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<EntryOffer> getOfferList(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IMarketEntry> getTransactionHistory(MarketType type, int indexStart, int indexEnd) {
		// TODO Auto-generated method stub
		return null;
	}

}
