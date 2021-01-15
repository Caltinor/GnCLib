package dicemc.gnclib.trade;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import dicemc.gnclib.configs.ConfigCore;
import dicemc.gnclib.money.LogicMoney;
import dicemc.gnclib.trade.dbref.H2Impl;
import dicemc.gnclib.trade.dbref.IDBImplTrade;
import dicemc.gnclib.trade.entries.EntryAuction;
import dicemc.gnclib.trade.entries.EntryBid;
import dicemc.gnclib.trade.entries.EntryGlobal;
import dicemc.gnclib.trade.entries.EntryLocal;
import dicemc.gnclib.trade.entries.EntryOffer;
import dicemc.gnclib.trade.entries.EntryServer;
import dicemc.gnclib.trade.entries.EntryStorage;
import dicemc.gnclib.trade.entries.IMarketEntry;
import dicemc.gnclib.util.TranslatableResult;

public class LogicTrade implements IDBImplTrade{
	//Singletone Block
	private static final LogicTrade INSTANCE = new LogicTrade();
	private LogicTrade() {}
	public static LogicTrade get() {return INSTANCE;}
	
	//Primary logical elements
	private static IDBImplTrade service;
	private static Map<MarketType, Marketplace> Marketplaces = new HashMap<MarketType, Marketplace>();
	
	public static void init(String saveName) {
		service = setService(saveName);
		Marketplaces.put(MarketType.LOCAL, new Marketplace("Local"));
		Marketplaces.put(MarketType.GLOBAL, new Marketplace("Global", ConfigCore.MARKET_GLOBAL_TAX_BUY, ConfigCore.MARKET_GLOBAL_TAX_SELL));
		Marketplaces.put(MarketType.AUCTION, new Marketplace("Auction", 0d, ConfigCore.MARKET_AUCTION_TAX_SELL));
		Marketplaces.put(MarketType.SERVER, new Marketplace("Server"));
	}
	
	private static IDBImplTrade setService(String saveName) {
		switch (ConfigCore.DBService.getFromString()) {
		case H2: {
			return new H2Impl(saveName);
		}
		case MY_SQL: {
			break;
		}
		default:
		}
		return new H2Impl(saveName);
	}

	/** Takes the entry and performs account transactions for the type
	 * then calls the DB method for creating the new DB entry
	 * 
	 * @param entry a completed MarketEntry corresponding to the type being passed
	 * @param type the market type the entry is being applied to
	 * @return translatable result that gives a result status and corresponding translation key
	 */
	@Override
	public TranslatableResult<TradeResult> createTransaction(IMarketEntry entry, MarketType type) {
		switch (type) {
		case LOCAL: {
			if (!(entry instanceof EntryLocal)) return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.create.failure.typemismatch");
			
			break;
		}
		case GLOBAL: {
			if (!(entry instanceof EntryGlobal)) return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.create.failure.typemismatch");
			double pBal = LogicMoney.getBalance(entry.getVendorID(), LogicMoney.AccountType.PLAYER.rl);
			double tax = entry.getPrice()*Marketplaces.get(type).sellFee;
			double fee = (entry.getGiveItem() ? tax : (tax + entry.getPrice()));
			if (pBal < fee) {return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.create.failure.funds");}
			else {LogicMoney.changeBalance(entry.getVendorID(), LogicMoney.AccountType.PLAYER.rl, -(fee));}
			break;
		}
		case AUCTION: {
			if (!(entry instanceof EntryAuction)) return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.create.failure.typemismatch");
			double pBal = LogicMoney.getBalance(entry.getVendorID(), LogicMoney.AccountType.PLAYER.rl);
			double fee = entry.getPrice()*Marketplaces.get(type).sellFee;
			if (pBal < fee) {return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.create.failure.funds");}
			else {LogicMoney.changeBalance(entry.getVendorID(), LogicMoney.AccountType.PLAYER.rl, -(fee));}
			break;
		}
		case SERVER: {
			if (!(entry instanceof EntryServer)) return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.create.failure.typemismatch");
			break;
		}
		default:}
		return service.createTransaction(entry, type);
	}

	@Override
	public TranslatableResult<TradeResult> closeTransaction(int id, MarketType type) {
		/* Triggered when the last item in a transaction is purchased to close the master
		 * record or to force close one from the admin menu.
		 * - [DB] Return all offered Items to their owners' storage
		 * - [DB] Set Transaction to closed 
		 */
		return service.closeTransaction(id, type);
	}

	@Override
	public TranslatableResult<TradeResult> acceptOffer(IMarketEntry entry, MarketType type, EntryOffer offer) {
		/* [DB] reduce stock, remove offer, create historical entry
		 * [DB] if stock depeleted call closeTransaction
		 */
		switch (type) {
		case SERVER: case AUCTION: {
			return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.acceptoffer.failure.type");}
		case LOCAL: case GLOBAL: {
			if (offer.requestedAmount > entry.getStock()) return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.acceptoffer.failure.count");
			break;
		}		
		default:}
		return service.acceptOffer(entry, type, offer);
	}

	@Override
	public TranslatableResult<TradeResult> expireBid(EntryAuction entry) {
		/* [DB] close transaction
		 * - transfer highest bid funds to the seller
		 */
		List<EntryBid> bidlist = getBidList(entry.getID());
		EntryBid highestBid = bidlist.get(bidlist.size()-1);
		LogicMoney.changeBalance(entry.vendor, LogicMoney.AccountType.PLAYER.rl, highestBid.value);
		return service.expireBid(entry);
	}
	
	@Override
	public TranslatableResult<TradeResult> executeTransaction(IMarketEntry entry, MarketType type, UUID buyer, String buyerName, int count) {
		if (type.equals(MarketType.AUCTION)) return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.executetrans.failue.type");
		if (entry.getStock() < count) return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.executetrans.failure.count");
		if (Marketplaces.get(type).buyFee > 0) {
			double fee = entry.getPrice() * Marketplaces.get(type).buyFee * count;
			double cost = entry.getGiveItem() ? entry.getPrice() * count : 0d;
			double balP = LogicMoney.getBalance(buyer, LogicMoney.AccountType.PLAYER.rl);
			if (balP < (cost + fee)) return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.executetrans.failure.fund");
			LogicMoney.transferFunds(buyer, LogicMoney.AccountType.PLAYER.rl, entry.getVendorID(), LogicMoney.AccountType.PLAYER.rl, entry.getPrice());
		}
		return service.executeTransaction(entry, type, buyer, buyerName, count);
	}

	@Override
	public TranslatableResult<TradeResult> submitOffer(IMarketEntry entry, EntryOffer offer) {
		if (entry instanceof EntryAuction || entry instanceof EntryServer || !entry.getGiveItem()) {
			return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.offer.failure.type");
		}
		else if (entry.getStock() < offer.requestedAmount) {
			return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.offer.failure.stock");
		}
		else if (entry instanceof EntryLocal) {
			if (!offer.marketName.equals(Marketplaces.get(MarketType.LOCAL).marketName)) 
				return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.offer.failure.type");
		}
		else if (entry instanceof EntryGlobal) {
			if (!offer.marketName.equals(Marketplaces.get(MarketType.GLOBAL).marketName)) 
				return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.offer.failure.type");
			double fee = offer.requestedAmount * Marketplaces.get(MarketType.GLOBAL).buyFee * entry.getPrice();
			double balP = LogicMoney.getBalance(offer.offerer, LogicMoney.AccountType.PLAYER.rl);
			if (balP < fee) return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.offer.failure.fund");
			LogicMoney.changeBalance(offer.offerer, LogicMoney.AccountType.PLAYER.rl, -(fee));
		}
		else return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "");
		return service.submitOffer(entry, offer);
	}

	@Override
	public TranslatableResult<TradeResult> placeBid(EntryBid bid) {
		List<EntryBid> bidList = service.getBidList(bid.getTransactionID());
		EntryBid highestBid = bidList.get(bidList.size()-1);
		if (highestBid.value >= bid.value) {return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.placebid.failure.toolow");} 
		double pBal = LogicMoney.getBalance(bid.bidder, LogicMoney.AccountType.PLAYER.rl);
		if (pBal < bid.value) {return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.placebid.failure.fund");}
		LogicMoney.changeBalance(bid.bidder, LogicMoney.AccountType.PLAYER.rl, -(bid.value));
		LogicMoney.changeBalance(highestBid.bidder, LogicMoney.AccountType.PLAYER.rl, highestBid.value);
		return service.placeBid(bid);
	}

	@Override
	public TranslatableResult<TradeResult> addToStorage(EntryStorage entry) {
		return service.addToStorage(entry);
	}

	@Override
	public TranslatableResult<TradeResult> pullFromStorage(EntryStorage entry, int count) {
		if (count > entry.count) return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "");
		return service.pullFromStorage(entry, count);
	}

	@Override
	public TranslatableResult<TradeResult> changeTransactionSupply(MarketType type, IMarketEntry entry, int newSupply) {
		if (Marketplaces.get(type).sellFee > 0) {
			double fee = entry.getPrice() * newSupply * Marketplaces.get(type).sellFee;
			double balP = LogicMoney.getBalance(entry.getVendorID(), LogicMoney.AccountType.PLAYER.rl);
			if (fee > balP) return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.supplychange.failure.fund");
			LogicMoney.changeBalance(entry.getVendorID(), LogicMoney.AccountType.PLAYER.rl, -(fee));
		}
		if ((entry.getStock() + newSupply) < 0) return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "");
		return service.changeTransactionSupply(type, entry, newSupply);
	}

	@Override
	public List<IMarketEntry> getMarketList(MarketType type, int indexStart, int indexEnd, Map<FilterType, String> filters) {
		return service.getMarketList(type, indexStart, indexEnd, filters);
	}

	@Override
	public List<EntryStorage> getStorageList(int indexStart, int indexEnd, Map<FilterType, String> filters) {
		return service.getStorageList(indexStart, indexEnd, filters);
	}

	@Override
	public List<EntryBid> getBidList(int id) {
		return service.getBidList(id);
	}

	@Override
	public List<EntryOffer> getOfferList(int id, MarketType type) {
		return service.getOfferList(id, type);
	}

	@Override
	public List<IMarketEntry> getTransactionHistory(MarketType type, int indexStart, int indexEnd, Map<FilterType, String> filters) {
		return service.getTransactionHistory(type, indexStart, indexEnd, filters);
	}
	
}
