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
import dicemc.gnclib.trade.entries.EntryStorage;
import dicemc.gnclib.trade.entries.EntryTransactor;
import dicemc.gnclib.trade.entries.EntryTransactor.Type;
import dicemc.gnclib.trade.entries.IMarketEntry;
import dicemc.gnclib.util.TranslatableResult;

public class LogicTrade implements IDBImplTrade{
	//Singleton Block
	private static final LogicTrade INSTANCE = new LogicTrade();
	private LogicTrade() {}
	public static LogicTrade get() {return INSTANCE;}
	
	public void printTables() {((H2Impl)service).printAllTables();}
	
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
	
	public String getMarketName(MarketType type) { return Marketplaces.get(type).marketName; }

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
			if (!entry.getGiveItem()) {
				double balP = LogicMoney.getBalance(entry.getVendor().refID, LogicMoney.transactorType(entry.getVendor().type));
				if (balP < entry.getPrice()) return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.create.failure.funds");
				LogicMoney.changeBalance(entry.getVendor().refID, LogicMoney.transactorType(entry.getVendor().type), -(entry.getPrice()));
			}
			break;
		}
		case GLOBAL: {
			if (!(entry instanceof EntryGlobal)) return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.create.failure.typemismatch");
			double pBal = LogicMoney.getBalance(entry.getVendor().refID, LogicMoney.transactorType(entry.getVendor().type));
			double tax = entry.getPrice()*Marketplaces.get(type).sellFee;
			double fee = (entry.getGiveItem() ? tax * entry.getStock() : (tax + entry.getPrice()) * entry.getStock());
			if (pBal < fee) {return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.create.failure.funds");}
			else {LogicMoney.changeBalance(entry.getVendor().refID, LogicMoney.transactorType(entry.getVendor().type), -(fee));}
			break;
		}
		case AUCTION: {
			if (!(entry instanceof EntryAuction)) return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.create.failure.typemismatch");
			double pBal = LogicMoney.getBalance(entry.getVendor().refID, LogicMoney.transactorType(entry.getVendor().type));
			double fee = entry.getPrice()*Marketplaces.get(type).sellFee;
			if (pBal < fee) {return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.create.failure.funds");}
			else {LogicMoney.changeBalance(entry.getVendor().refID, LogicMoney.transactorType(entry.getVendor().type), -(fee));}
			break;
		}
		case SERVER: {
			if (!(entry instanceof EntryGlobal)) return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.create.failure.typemismatch");
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
		if (bidlist.size() == 0) {
			EntryStorage sto = new EntryStorage(entry.vendor, entry.stack, 1);
			addToStorage(sto);
			return service.expireBid(entry);
		}
		EntryBid highestBid = bidlist.get(bidlist.size()-1);
		LogicMoney.changeBalance(entry.vendor.refID, LogicMoney.transactorType(entry.vendor.type), highestBid.value);
		return service.expireBid(entry);
	}
	
	@Override
	public TranslatableResult<TradeResult> executeTransaction(IMarketEntry entry, MarketType type, EntryTransactor buyer, int count) {
		if (type.equals(MarketType.AUCTION)) return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.executetrans.failue.type");
		if (isStockInsufficient(entry.getStock(), count)) return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.executetrans.failure.count");
		double fee = entry.getPrice() * Marketplaces.get(type).buyFee * count;
		double cost = entry.getGiveItem() ? entry.getPrice() * count : 0d;
		double balP = LogicMoney.getBalance(buyer.refID, LogicMoney.transactorType(buyer.type));
		if (balP < (cost + fee)) return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.executetrans.failure.fund");
		if (cost != 0 ) LogicMoney.transferFunds(buyer.refID, LogicMoney.transactorType(buyer.type), entry.getVendor().refID, LogicMoney.transactorType(entry.getVendor().type), cost);
		if (fee != 0) LogicMoney.changeBalance(buyer.refID, LogicMoney.transactorType(buyer.type), -fee);
		return service.executeTransaction(entry, type, buyer, count);
	}

	@Override
	public TranslatableResult<TradeResult> submitOffer(IMarketEntry entry, EntryOffer offer, MarketType type) {
		if (type.equals(MarketType.AUCTION) || type.equals(MarketType.SERVER) || !entry.getGiveItem()) {
			return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.offer.failure.type");
		}
		else if (entry.getStock() < offer.requestedAmount) {
			return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.offer.failure.stock");
		}
		else if (type.equals(MarketType.LOCAL)) {
			if (!offer.marketName.equals(Marketplaces.get(MarketType.LOCAL).marketName)) 
				return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.offer.failure.type");
		}
		else if (type.equals(MarketType.GLOBAL)) {
			if (!offer.marketName.equals(Marketplaces.get(MarketType.GLOBAL).marketName)) 
				return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.offer.failure.type");
			double fee = offer.requestedAmount * Marketplaces.get(MarketType.GLOBAL).buyFee * entry.getPrice();
			double balP = LogicMoney.getBalance(offer.offerer.refID, LogicMoney.transactorType(offer.offerer.type));
			if (balP < fee) return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.offer.failure.fund");
			LogicMoney.changeBalance(offer.offerer.refID, LogicMoney.transactorType(offer.offerer.type), -(fee));
		}
		else return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "");
		return service.submitOffer(entry, offer, type);
	}

	@Override
	public TranslatableResult<TradeResult> placeBid(EntryBid bid, double itemValue) {
		List<EntryBid> bidList = service.getBidList(bid.getTransactionID());
		boolean firstBid = true;
		EntryBid highestBid = null;
		if (bidList.size() > 0) {
			firstBid = false;
			highestBid = bidList.get(bidList.size()-1);
			if (highestBid.value >= bid.value) {return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.placebid.failure.toolow");}
		}
		else { 
			if (itemValue >= bid.value) {return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.placebid.failure.toolow");}
		}
		double pBal = LogicMoney.getBalance(bid.bidder.refID, LogicMoney.transactorType(bid.bidder.type));
		if (pBal < bid.value) {return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.placebid.failure.fund");}
		LogicMoney.changeBalance(bid.bidder.refID, LogicMoney.transactorType(bid.bidder.type), -(bid.value));
		if (!firstBid) LogicMoney.changeBalance(highestBid.bidder.refID, LogicMoney.transactorType(highestBid.bidder.type), highestBid.value);
		return service.placeBid(bid, itemValue);
	}

	@Override
	public TranslatableResult<TradeResult> addToStorage(EntryStorage entry) {
		return service.addToStorage(entry);
	}

	@Override
	public TranslatableResult<TradeResult> pullFromStorage(EntryStorage entry, int count) {
		entry = getStorageEntry(entry.getID());
		if (count > entry.count) return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.storage.pull.failure.count");
		return service.pullFromStorage(entry, count);
	}

	@Override
	public TranslatableResult<TradeResult> changeTransactionSupply(MarketType type, IMarketEntry entry, int newSupply) {
		if (type.equals(MarketType.AUCTION)) return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.supplychange.failure.type");
		if ((entry.getStock() + newSupply) < 0) return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.supplychange.failure.negative");
		if ((entry.getStock() + newSupply) == 0) return closeTransaction(entry.getID(), type);
		if (Marketplaces.get(type).sellFee > 0) {
			double fee = entry.getPrice() * newSupply * Marketplaces.get(type).sellFee;
			double balP = LogicMoney.getBalance(entry.getVendor().refID, LogicMoney.transactorType(entry.getVendor().type));
			if (fee > balP) return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.supplychange.failure.fund");
			
			LogicMoney.changeBalance(entry.getVendor().refID, LogicMoney.transactorType(entry.getVendor().type), -(fee));
		}		
		return service.changeTransactionSupply(type, entry, newSupply);
	}

	@Override
	public List<IMarketEntry> getMarketList(MarketType type, int indexStart, int rowCount, Map<FilterType, String> filters, boolean isHistory) {
		return service.getMarketList(type, indexStart, rowCount, filters, isHistory);
	}

	@Override
	public List<EntryStorage> getStorageList(int indexStart, int rowCount, EntryTransactor owner) {
		return service.getStorageList(indexStart, rowCount, owner);
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
	public IMarketEntry getMarketEntry(int id, MarketType type) {return service.getMarketEntry(id, type);}
	
	@Override
	public EntryStorage getStorageEntry(int id) {return service.getStorageEntry(id);}
	
	@Override
	public EntryTransactor getTransactor(int id) {return service.getTransactor(id);}
	
	@Override
	public EntryTransactor getTransactor(UUID refID, Type type) {return service.getTransactor(refID, type);}
	
	private boolean isStockInsufficient(int stock, int asked) {
		if (stock < 0) return false;
		return stock < asked;
	}
	
	
}
