package dicemc.gnclib.trade.dbref;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import dicemc.gnclib.configs.ConfigCore;
import dicemc.gnclib.trade.LogicTrade;
import dicemc.gnclib.trade.entries.EntryAuction;
import dicemc.gnclib.trade.entries.EntryBid;
import dicemc.gnclib.trade.entries.EntryGlobal;
import dicemc.gnclib.trade.entries.EntryLocal;
import dicemc.gnclib.trade.entries.EntryOffer;
import dicemc.gnclib.trade.entries.EntryStorage;
import dicemc.gnclib.trade.entries.IMarketEntry;
import dicemc.gnclib.util.Agent;
import dicemc.gnclib.util.IDatabase;
import dicemc.gnclib.util.ResultType;
import dicemc.gnclib.util.TranslatableResult;
import dicemc.gnclib.util.Agent.Type;

public class H2Impl implements IDBImplTrade, IDatabase{
	public static final Map<tblMarkets, String> map_Markets = define_Markets();
	public static final Map<tblBids, String> map_Bids = define_Bids();
	public static final Map<tblStorage, String> map_Storage = define_Storage();
	public static final Map<tblOffers, String> map_Offers = define_Offers();
	public static final Map<MarketType, String> marketTables = defineMarketTableNames();
	public static final Map<tblTransactors, String> map_Transactors = define_Transactors();
	private Connection con;

	public H2Impl(String saveName) {
		String port = ConfigCore.DB_PORT;
		String name = saveName + ConfigCore.DB_NAME;
		String url  = ConfigCore.DB_URL +"\\";
		String host = "jdbc:h2://" + url + port + name;
		String user = ConfigCore.DB_USER;
		String pass = ConfigCore.DB_PASS;
		
		try {
			System.out.println("Attempting Trade DB Connection");
			con = DriverManager.getConnection(host, user, pass);
			System.out.println("Trade DB Connection Successful");
			//Cycle through constructed table definitions to create tables.
			for (Map.Entry<String, String> entry : defineTables().entrySet()) {
				String sql = "CREATE TABLE IF NOT EXISTS "+ entry.getKey() + entry.getValue();
				PreparedStatement st = con.prepareStatement(sql);
				executeUPDATE(st);
			}
		} catch (SQLException e) {e.printStackTrace();}	
	}

	@Override
	public void printAllTables() {
		List<String> tblNames = new ArrayList<String>();
		tblNames.add(marketTables.get(MarketType.LOCAL));
		tblNames.add(marketTables.get(MarketType.GLOBAL));
		tblNames.add(marketTables.get(MarketType.AUCTION));
		tblNames.add(marketTables.get(MarketType.SERVER));
		tblNames.add(map_Bids.get(tblBids.TABLE_NAME));
		tblNames.add(map_Storage.get(tblStorage.TABLE_NAME));
		tblNames.add(map_Offers.get(tblOffers.TABLE_NAME));
		tblNames.add(map_Transactors.get(tblTransactors.TABLE_NAME));
		PreparedStatement st = null;
		for (int t = 0; t< tblNames.size(); t++) {
			String sql = "SELECT * FROM " +tblNames.get(t);
			System.out.println("==========="+tblNames.get(t)+"===========");
			try {
				st = con.prepareStatement(sql);
				ResultSet rs = executeSELECT(st);
				int cc = rs.getMetaData().getColumnCount();				
				while (rs.next()) {
					String output = "";
					for (int i = 1; i <= cc; i++) {
						output += rs.getMetaData().getColumnName(i)+":"+rs.getString(i) + ", ";
					}
					System.out.println(output);
				}
			} catch(SQLException e) {e.printStackTrace();}
		}
		
	}
	
	@Override
	public Map<String, String> defineTables() {
		Map<String, String> map = new HashMap<String, String>();
		map.put(marketTables.get(MarketType.LOCAL), defineMarketTable(MarketType.LOCAL));
		map.put(marketTables.get(MarketType.GLOBAL), defineMarketTable(MarketType.GLOBAL));
		map.put(marketTables.get(MarketType.AUCTION), defineMarketTable(MarketType.AUCTION));
		map.put(marketTables.get(MarketType.SERVER), defineMarketTable(MarketType.SERVER));
		String sql = " (" + map_Bids.get(tblBids.ID)	+" INT NOT NULL AUTO_INCREMENT, " +
				map_Bids.get(tblBids.TRANSACTION_ID)	+" INT NOT NULL, " +
				map_Bids.get(tblBids.BIDDER_ID)			+" INT NOT NULL, " +
				map_Bids.get(tblBids.PRICE)				+" DOUBLE NOT NULL, " +
				map_Bids.get(tblBids.DTG_PLACED)		+" BIGINT NOT NULL, " +
				"PRIMARY KEY (" + map_Bids.get(tblBids.ID) + "));";
		map.put(map_Bids.get(tblBids.TABLE_NAME), sql);
		sql = " (" + map_Offers.get(tblOffers.ID)		+" INT NOT NULL AUTO_INCREMENT, " +
				map_Offers.get(tblOffers.TRANS_ID)		+" INT NOT NULL, " +
				map_Offers.get(tblOffers.MARKET_NAME)	+" VARCHAR(16) NOT NULL, " +
				map_Offers.get(tblOffers.ITEM)			+" LONGTEXT NOT NULL, " +
				map_Offers.get(tblOffers.OFFERER) 		+" INT NOT NULL, " +
				map_Offers.get(tblOffers.DTG_PLACED)	+" BIGINT NOT NULL, " +
				map_Offers.get(tblOffers.REQUESTED_AMOUNT)+" INT NOT NULL, " +
				map_Offers.get(tblOffers.OFFERRED_AMOUNT)+" INT NOT NULL, " +
				"PRIMARY KEY ("+map_Offers.get(tblOffers.ID) + "));";
		map.put(map_Offers.get(tblOffers.TABLE_NAME), sql);
		sql = " (" + map_Storage.get(tblStorage.ID) 		+" INT NOT NULL AUTO_INCREMENT, " +
				map_Storage.get(tblStorage.OWNER) 			+" INT NOT NULL, " +
				map_Storage.get(tblStorage.ITEM) 			+" LONGTEXT NOT NULL, " +
				map_Storage.get(tblStorage.QUANTITY) 		+" INT NOT NULL, " + 
				"PRIMARY KEY (" + map_Storage.get(tblStorage.ID) + "));";
		map.put(map_Storage.get(tblStorage.TABLE_NAME), sql);
		sql = " (" + map_Transactors.get(tblTransactors.ID) +" INT NOT NULL AUTO_INCREMENT, " +
				map_Transactors.get(tblTransactors.REF_ID)  +" UUID, " +
				map_Transactors.get(tblTransactors.TYPE)	+" INT NOT NULL, " +
				map_Transactors.get(tblTransactors.NAME) 	+" VARCHAR(32) NOT NULL, " +
				"PRIMARY KEY ("+map_Transactors.get(tblTransactors.ID)+"));";
		map.put(map_Transactors.get(tblTransactors.TABLE_NAME), sql);
		return map;
	}
	
	private String defineMarketTable(MarketType type) {
		String str = " ("+ map_Markets.get(tblMarkets.ID) 			+" INT NOT NULL AUTO_INCREMENT, " +
				map_Markets.get(tblMarkets.ITEM) 				+" LONGTEXT NOT NULL, " +
				map_Markets.get(tblMarkets.VENDOR_ID)	+" INT NOT NULL, " +
				(type.equals(MarketType.LOCAL) ? map_Markets.get(tblMarkets.LOCALITY)		+" UUID NOT NULL, " : "") +
				(type.equals(MarketType.AUCTION) ? map_Markets.get(tblMarkets.BID_END)		+" BIGINT NOT NULL, "  : "") +
				map_Markets.get(tblMarkets.PRICE)				+" DOUBLE NOT NULL, " +
				(!type.equals(MarketType.AUCTION) ? map_Markets.get(tblMarkets.VENDOR_GIVE_ITEM)	+" TINYINT(1) NOT NULL, " : "") +
				(!type.equals(MarketType.AUCTION) ? map_Markets.get(tblMarkets.STOCK)			+" INT NOT NULL, " : "") +
				map_Markets.get(tblMarkets.ACTIVE_TRANSACTION)	+" TINYINT(1) NOT NULL, " +
				map_Markets.get(tblMarkets.BUYER_ID)			+" INT, " +
				map_Markets.get(tblMarkets.DTG_PLACED)			+" BIGINT NOT NULL, " +
				map_Markets.get(tblMarkets.DTG_CLOSED)			+" BIGINT NOT NULL, " +
				"PRIMARY KEY (" + map_Markets.get(tblMarkets.ID) + "));";
		return str;
	}
	
	@Override
	public TranslatableResult<ResultType> createTransaction(IMarketEntry entry, MarketType type) {
		PreparedStatement st = null;
		if (!type.equals(MarketType.AUCTION)) {
			String sql = "SELECT * FROM " + marketTables.get(type) + " WHERE " + 
					map_Markets.get(tblMarkets.ITEM) + " = ? AND " + 
					map_Markets.get(tblMarkets.VENDOR_ID) +	" = ? AND " + 
					map_Markets.get(tblMarkets.PRICE) + " = ? AND " +
					map_Markets.get(tblMarkets.VENDOR_GIVE_ITEM) + " = ? " +
					(type.equals(MarketType.LOCAL) ? "AND " + map_Markets.get(tblMarkets.LOCALITY) + " = ?" : "") +
					";";
			try {
				st = con.prepareStatement(sql);
				int f = 0;
				st.setString(++f, entry.getStack());
				st.setObject(++f, entry.getVendor().id);
				st.setDouble(++f, entry.getPrice());
				st.setBoolean(++f, entry.getGiveItem());
				if (type.equals(MarketType.LOCAL)) st.setString(++f, entry.getLocality().toString());
				ResultSet rs = executeSELECT(st);
				if (rs.isBeforeFirst()) {
					rs.next();
					int transID = rs.getInt(map_Markets.get(tblMarkets.ID));
					String stack = rs.getString(map_Markets.get(tblMarkets.ITEM));
					double price = rs.getDouble(map_Markets.get(tblMarkets.PRICE));
					boolean active = rs.getBoolean(map_Markets.get(tblMarkets.ACTIVE_TRANSACTION));
					Agent buyer = getTransactor(rs.getInt(map_Markets.get(tblMarkets.BUYER_ID)));
					Agent vendor = getTransactor(rs.getInt(map_Markets.get(tblMarkets.VENDOR_ID)));
					long placed = rs.getLong(map_Markets.get(tblMarkets.DTG_PLACED));
					long closed = rs.getLong(map_Markets.get(tblMarkets.DTG_CLOSED));
					boolean giveItem = rs.getBoolean(map_Markets.get(tblMarkets.VENDOR_GIVE_ITEM));
					int originalStock = rs.getInt(map_Markets.get(tblMarkets.STOCK));
					switch (type) {
					case LOCAL: {
						UUID locality = (UUID) rs.getObject(map_Markets.get(tblMarkets.LOCALITY));						
						EntryLocal newEntry = new EntryLocal(transID, stack, vendor, buyer, locality,
								price, giveItem, active, originalStock, placed, closed);
						 return changeTransactionSupply(type, newEntry, entry.getStock());
					}
					case GLOBAL: case SERVER: {
						EntryGlobal newEntry = new EntryGlobal(transID, stack, vendor, buyer, price,
								giveItem, active, originalStock, placed, closed);
						return changeTransactionSupply(type, newEntry, entry.getStock());
					}
					default: }
				}
			} catch (SQLException e) {e.printStackTrace();}
		}
		String sql = "INSERT INTO " + marketTables.get(type) + " (" + map_Markets.get(tblMarkets.ITEM) +
				", " +	map_Markets.get(tblMarkets.PRICE) +
				", " +	map_Markets.get(tblMarkets.ACTIVE_TRANSACTION) +
				", " +	map_Markets.get(tblMarkets.BUYER_ID) +
				", " +	map_Markets.get(tblMarkets.DTG_PLACED) +
				", " +	map_Markets.get(tblMarkets.DTG_CLOSED) +
				", " +	map_Markets.get(tblMarkets.VENDOR_ID) +
				(type.equals(MarketType.LOCAL) ? 
						", " +map_Markets.get(tblMarkets.LOCALITY) : "") +
				(type.equals(MarketType.AUCTION) ? 
						", " +map_Markets.get(tblMarkets.BID_END) : "") +
				(!type.equals(MarketType.AUCTION) ? 
						", " +map_Markets.get(tblMarkets.VENDOR_GIVE_ITEM) +
						", " +map_Markets.get(tblMarkets.STOCK) : "") +
				") VALUES (?, ?, ?, ?, ?, ?, ?" +
				(type.equals(MarketType.LOCAL) ? ", ?" : "") +
				(type.equals(MarketType.AUCTION) ? ", ?" : "") +
				(!type.equals(MarketType.AUCTION) ? ", ?, ?" : "") +
				");";
		try {
			st = con.prepareStatement(sql);
			int f = 0;
			st.setString(++f, entry.getStack());
			st.setDouble(++f, entry.getPrice());
			st.setBoolean(++f, entry.getActive());
			st.setObject(++f, null);
			st.setLong(++f, entry.getDTGPlaced());
			st.setLong(++f, entry.getDTGClosed());
			st.setInt(++f, entry.getVendor().id);
			if (type.equals(MarketType.LOCAL)) st.setObject(++f, entry.getLocality());
			if (type.equals(MarketType.AUCTION)) st.setLong(++f, entry.getBidEnd());
			if (!type.equals(MarketType.AUCTION)) {
				st.setBoolean(++f, entry.getGiveItem());
				st.setInt(++f, entry.getStock());
			}
			if (executeUPDATE(st) == 0) return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.market.create.failure.sql");
		} catch (SQLException e) {e.printStackTrace();}
		return new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.market.create.success");
	}

	@Override
	public TranslatableResult<ResultType> closeTransaction(int id, MarketType type) {
		PreparedStatement st = null;
		String sql = "UPDATE " + marketTables.get(type) + " SET " + 
				map_Markets.get(tblMarkets.DTG_CLOSED) +" =?, "+ 
				map_Markets.get(tblMarkets.ACTIVE_TRANSACTION) + " =? WHERE " + 
				map_Markets.get(tblMarkets.ID) + " = ?;";
		try {
			st = con.prepareStatement(sql);
			st.setLong(1, System.currentTimeMillis());
			st.setBoolean(2, false);
			st.setInt(3, id);
			if (executeUPDATE(st) == 0) return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.market.close.failure.missing");
		} catch (SQLException e) {e.printStackTrace();}
		List<EntryOffer> offerList = getOfferList(id, type);
		for (EntryOffer list : offerList) {
			addToStorage(new EntryStorage(list.offerer, list.stack, list.offeredAmount));
			sql = "DELETE FROM " + map_Offers.get(tblOffers.TABLE_NAME) + 
					" WHERE " + map_Offers.get(tblOffers.TRANS_ID) + " = ?;";
			try {
				st = con.prepareStatement(sql);
				st.setInt(1, list.getTransactionID());
				if (executeUPDATE(st) == 0) return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.market.close.failure.missing");
			} catch (SQLException e) {e.printStackTrace();}
		}
		return new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.market.close.success");
	}

	@SuppressWarnings("resource")
	@Override
	public TranslatableResult<ResultType> acceptOffer(IMarketEntry entry, MarketType type, EntryOffer offer) {
		entry = getMarketEntry(entry.getID(), type);
		addToStorage(new EntryStorage(offer.offerer, entry.getStack(), offer.requestedAmount));
		addToStorage(new EntryStorage(entry.getVendor(), offer.stack, offer.offeredAmount));
		PreparedStatement st = null;
		String sql = "DELETE FROM " + 
				map_Offers.get(tblOffers.TABLE_NAME) +" WHERE " +
				map_Offers.get(tblOffers.ID) +" = ?";
		try {
			st = con.prepareStatement(sql);
			st.setInt(1, offer.getID());
			if (executeUPDATE(st) == 0) return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.market.acceptoffer.failure.missing");
		} catch(SQLException e) {e.printStackTrace();} 
		if (offer.requestedAmount == entry.getStock()) {
			sql = "UPDATE " + marketTables.get(type) + " SET " +
					map_Markets.get(tblMarkets.BUYER_ID) + " =?, " +
					map_Markets.get(tblMarkets.PRICE) + " =? WHERE " +
					map_Markets.get(tblMarkets.ID) + " =?";
			try {
				st = con.prepareStatement(sql);
				st.setObject(1, offer.offerer.id);
				st.setDouble(2, 0);
				st.setInt(4, entry.getID());
				if (executeUPDATE(st) == 0) return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.market.acceptoffer.failure.missing");
			} catch(SQLException e) {e.printStackTrace();}
			return closeTransaction(entry.getID(), type);
		}
		else {
			sql = "UPDATE " + marketTables.get(type) + " SET " +
					map_Markets.get(tblMarkets.STOCK) + " =? WHERE " +
					map_Markets.get(tblMarkets.ID) + " =?";
			try {
				st = con.prepareStatement(sql);
				st.setInt(1, entry.getStock() - offer.requestedAmount);
				st.setInt(2, entry.getID());
				if (executeUPDATE(st) == 0) return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.market.acceptoffer.failure.missing");
			} catch(SQLException e) {e.printStackTrace();}
			sql = "INSERT INTO " + marketTables.get(type) + " (" +
					map_Markets.get(tblMarkets.ITEM) +", " +
					map_Markets.get(tblMarkets.PRICE) +", " +
					map_Markets.get(tblMarkets.ACTIVE_TRANSACTION) +", " +
					map_Markets.get(tblMarkets.BUYER_ID) +", " +
					map_Markets.get(tblMarkets.DTG_PLACED) +", " +
					map_Markets.get(tblMarkets.DTG_CLOSED) +", " +
					map_Markets.get(tblMarkets.VENDOR_ID) +", " +
					map_Markets.get(tblMarkets.VENDOR_GIVE_ITEM) +", " +
					map_Markets.get(tblMarkets.STOCK) +
					(type.equals(MarketType.LOCAL) ? 
						", "+ map_Markets.get(tblMarkets.LOCALITY) : "") +
					") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?" +
					(type.equals(MarketType.LOCAL) ? ", ?" : "") +
					");";
			try {
				st = con.prepareStatement(sql);
				int f = 0;
				st.setString(++f, entry.getStack());
				st.setDouble(++f, 0);
				st.setBoolean(++f, false);
				st.setInt(++f, offer.offerer.id);
				st.setLong(++f, entry.getDTGPlaced());
				st.setLong(++f, System.currentTimeMillis());
				st.setInt(++f, entry.getVendor().id);
				st.setBoolean(++f, entry.getGiveItem());
				st.setInt(++f, offer.requestedAmount);
				if (type.equals(MarketType.LOCAL)) st.setObject(++f, entry.getLocality());
				if (executeUPDATE(st) == 0 ) return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.market.acceptoffer.failure.insert");
			} catch (SQLException e) {e.printStackTrace();}
		}
		
		return new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.market.acceptoffer.success");
	}

	@Override
	public TranslatableResult<ResultType> expireBid(EntryAuction entry) {
		List<EntryBid> bids = getBidList(entry.getID());
		if (bids.size()==0) addToStorage(new EntryStorage(entry.vendor, entry.stack, entry.getStock()));
		else addToStorage(new EntryStorage(bids.get(bids.size()-1).bidder, entry.stack, entry.getStock()));
		return closeTransaction(entry.getID(), MarketType.AUCTION);
	}

	@Override
	public TranslatableResult<ResultType> executeTransaction(IMarketEntry entry, MarketType type, Agent buyer, int count) {
		entry = getMarketEntry(entry.getID(), type);
		PreparedStatement st = null;
		if (count == entry.getStock()) {
			String sql = "UPDATE " + marketTables.get(type) + " SET " +
					map_Markets.get(tblMarkets.BUYER_ID) + " =? WHERE " +
					map_Markets.get(tblMarkets.ID) + " =?";
			try {
				st = con.prepareStatement(sql);
				st.setInt(1, buyer.id);
				st.setInt(2, entry.getID());
				if (executeUPDATE(st) == 0) return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.market.executetrans.failure.missing");
			} catch(SQLException e) {e.printStackTrace();}
			return closeTransaction(entry.getID(), type);
		}
		else {
			String sql = "UPDATE " + marketTables.get(type) + " SET " +
					map_Markets.get(tblMarkets.STOCK) + " =? WHERE " +
					map_Markets.get(tblMarkets.ID) + " =?";
			try {
				st = con.prepareStatement(sql);
				st.setInt(1, entry.getStock() - count);
				st.setInt(2, entry.getID());
				if (executeUPDATE(st) == 0) return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.market.executetrans.failure.missing");
			} catch(SQLException e) {e.printStackTrace();}
			sql = "INSERT INTO " + marketTables.get(type) + "(" +
					map_Markets.get(tblMarkets.ITEM) +", " +
					map_Markets.get(tblMarkets.PRICE) +", " +
					map_Markets.get(tblMarkets.ACTIVE_TRANSACTION) +", " +
					map_Markets.get(tblMarkets.BUYER_ID) +", " +
					map_Markets.get(tblMarkets.DTG_PLACED) +", " +
					map_Markets.get(tblMarkets.DTG_CLOSED) +", " +
					map_Markets.get(tblMarkets.VENDOR_ID) +", " +
					map_Markets.get(tblMarkets.VENDOR_GIVE_ITEM) +", " +
					map_Markets.get(tblMarkets.STOCK) +
					(type.equals(MarketType.LOCAL) ? 
						", "+ map_Markets.get(tblMarkets.LOCALITY) : "") +
					") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?" +
					(type.equals(MarketType.LOCAL) ? ", ?" : "") +
					");";
			try {
				st = con.prepareStatement(sql);
				int f = 0;
				st.setString(++f, entry.getStack());
				st.setDouble(++f, entry.getPrice());
				st.setBoolean(++f, false);
				st.setInt(++f, buyer.id);
				st.setLong(++f, entry.getDTGPlaced());
				st.setLong(++f, System.currentTimeMillis());
				st.setInt(++f, entry.getVendor().id);
				st.setBoolean(++f, entry.getGiveItem());
				st.setInt(++f, count);
				if (type.equals(MarketType.LOCAL)) st.setObject(++f, entry.getLocality());
				if (executeUPDATE(st) == 0 ) return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.market.executetrans.failure.missing");
			} catch (SQLException e) {e.printStackTrace();}
		}
		TranslatableResult<ResultType> res = addToStorage(new EntryStorage(buyer, entry.getStack(), count));
		if (res.result.equals(ResultType.FAILURE)) return res;
		return new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.market.executetrans.success");
	}

	@Override
	public TranslatableResult<ResultType> submitOffer(IMarketEntry entry, EntryOffer offer, MarketType type) {
		entry = getMarketEntry(entry.getID(), type);
		PreparedStatement st = null;
		String sql = "INSERT INTO " + map_Offers.get(tblOffers.TABLE_NAME) + " (" +
				map_Offers.get(tblOffers.MARKET_NAME) + ", " +
				map_Offers.get(tblOffers.TRANS_ID) + ", " +
				map_Offers.get(tblOffers.ITEM) + ", " +
				map_Offers.get(tblOffers.OFFERER) + ", " +
				map_Offers.get(tblOffers.OFFERRED_AMOUNT) + ", " +
				map_Offers.get(tblOffers.REQUESTED_AMOUNT) + ", " +
				map_Offers.get(tblOffers.DTG_PLACED) + 
				") VALUES (?, ?, ?, ?, ?, ?, ?, ?);" ;
		try {
			st = con.prepareStatement(sql);
			st.setString(1, offer.marketName);
			st.setInt(2, entry.getID());
			st.setString(3, offer.stack);
			st.setInt(4, offer.offerer.id);
			st.setInt(6, offer.offeredAmount);
			st.setInt(7, offer.requestedAmount);
			st.setLong(8, offer.placedDate);
			if (executeUPDATE(st) == 0) return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.market.offer.failure.insert");
		} catch(SQLException e) {e.printStackTrace();}
		return new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.market.offer.success");
	}

	@Override
	public TranslatableResult<ResultType> placeBid(EntryBid bid, double itemValue) {
		PreparedStatement st = null;
		String sql = "INSERT INTO " + map_Bids.get(tblBids.TABLE_NAME) + " (" +
				map_Bids.get(tblBids.TRANSACTION_ID) + ", " +
				map_Bids.get(tblBids.BIDDER_ID) + ", " +
				map_Bids.get(tblBids.PRICE) + ", " +
				map_Bids.get(tblBids.DTG_PLACED) +
				") VALUES (?, ?, ?, ?, ?);";
		try {
			st = con.prepareStatement(sql);
			st.setInt(1, bid.getTransactionID());
			st.setInt(2, bid.bidder.id);
			st.setDouble(4, bid.value);
			st.setLong(5, bid.placedDate);
			if (executeUPDATE(st) == 0) return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.market.placebid.failure.insert");
		} catch(SQLException e) {e.printStackTrace();}
		return new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.market.placebid.success");
	}

	@Override
	public TranslatableResult<ResultType> addToStorage(EntryStorage entry) {
		List<EntryStorage> stoList = getStorageList(0, -1, entry.owner);
		int existingEntryID = -1;
		int newCount = entry.count;
		for (EntryStorage e : stoList) {
			if (e.stack.equalsIgnoreCase(entry.stack)) {
				existingEntryID = e.getID(); 
				newCount += e.count;
				break;}
		}
		PreparedStatement st = null;
		String sql = "";
		if (existingEntryID >= 0) {
			sql = "UPDATE " + map_Storage.get(tblStorage.TABLE_NAME) + " SET " +
					map_Storage.get(tblStorage.QUANTITY) + " =? WHERE " +
					map_Storage.get(tblStorage.ID) +" =?;";
			try {
				st = con.prepareStatement(sql);
				st.setInt(1, newCount);
				st.setInt(2, existingEntryID);
				if (executeUPDATE(st) == 0) return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.market.storage.place.failure.insert");
			} catch(SQLException e) {e.printStackTrace();}
		}
		else {//(existingEntryID == -1)
			sql = "INSERT INTO " + map_Storage.get(tblStorage.TABLE_NAME) + "(" +
					map_Storage.get(tblStorage.ITEM) + ", " +
					map_Storage.get(tblStorage.OWNER) + ", " +
					map_Storage.get(tblStorage.QUANTITY) + 
					") VALUES (?, ?, ?);" ;
			try {
				st = con.prepareStatement(sql);
				st.setString(1, entry.stack);
				st.setInt(2, entry.owner.id);
				st.setInt(3, entry.count);
				if (executeUPDATE(st) == 0) return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.market.storage.place.failure.insert");
			} catch(SQLException e) {e.printStackTrace();}
		}		
		return new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.market.storage.place.success");
	}

	@Override
	public TranslatableResult<ResultType> pullFromStorage(EntryStorage entry, int count) {
		if (entry.getID() == -1) new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.market.storage.pull.failure.entry");
		PreparedStatement st = null;
		String sql = "SELECT ";
		if (entry.count == count) {
			sql = "DELETE FROM " + map_Storage.get(tblStorage.TABLE_NAME) + " WHERE " +
					map_Storage.get(tblStorage.ID) + " = ?;";
			try {
				st = con.prepareStatement(sql);
				st.setInt(1, entry.getID());
				if (executeUPDATE(st) == 0) return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.market.storage.pull.failure.missing");
			} catch(SQLException e) {e.printStackTrace();}
		}
		else if (entry.count > count) {
			sql = "UPDATE " + map_Storage.get(tblStorage.TABLE_NAME) + " SET " +
					map_Storage.get(tblStorage.QUANTITY) + " =? WHERE " +
					map_Storage.get(tblStorage.ID) + " =?;";
			try {
				st = con.prepareStatement(sql);
				st.setInt(1, entry.count-count);
				st.setInt(2, entry.getID());
				if (executeUPDATE(st) == 0) return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.market.storage.pull.failure.missing");
			} catch(SQLException e) {e.printStackTrace();}
		}
		else if (entry.count < count) return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.market.storage.pull.failure.count");
		return new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.market.storage.pull.success");
	}

	@Override
	public TranslatableResult<ResultType> changeTransactionSupply(MarketType type, IMarketEntry entry, int newSupply) {
		entry = getMarketEntry(entry.getID(), type);
		PreparedStatement st = null;
		String sql = "UPDATE " + marketTables.get(type) + " SET " + 
				map_Markets.get(tblMarkets.STOCK) + " = ? WHERE " +
				map_Markets.get(tblMarkets.ID) + " = ?;";
		try {
			st = con.prepareStatement(sql);
			st.setInt(1, entry.getStock() + newSupply);
			st.setInt(2, entry.getID());
			if (executeUPDATE(st) == 0) return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.market.supplychange.failure.missing");
		} catch(SQLException e) {e.printStackTrace();}
		return new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.market.supplychange.success");
	}

	@Override
	public List<IMarketEntry> getMarketList(MarketType type, int indexStart, int rowCount, Map<FilterType, String> filters, boolean isHistory) {
		//Safety measures to ensure vendor and local are not referenced on tables that do not have them.
		if (!type.equals(MarketType.LOCAL) && filters.containsKey(FilterType.SOURCE_LOCALITY)) filters.remove(FilterType.SOURCE_LOCALITY);
		
		List<IMarketEntry> list = new ArrayList<IMarketEntry>();
		PreparedStatement st = null;
		String sql = "SELECT * FROM " + marketTables.get(type) + " WHERE ((" +
				map_Markets.get(tblMarkets.PRICE) + " BETWEEN ? AND ?)" +
				(filters.containsKey(FilterType.SOURCE_VENDOR) ? " AND ("+ map_Markets.get(tblMarkets.VENDOR_ID) + " =?)" : "") +
				(filters.containsKey(FilterType.SOURCE_LOCALITY) ? " AND (" + map_Markets.get(tblMarkets.LOCALITY) + " =?)" : "") + 			
				(filters.containsKey(FilterType.INCLUDE_MY_SALES) ? " AND (" + map_Markets.get(tblMarkets.VENDOR_ID) + " <>?)" : "") +
				(!type.equals(MarketType.AUCTION) ? " AND ("+ map_Markets.get(tblMarkets.VENDOR_GIVE_ITEM) + " =?)" : "") +
				" AND (" + map_Markets.get(tblMarkets.ACTIVE_TRANSACTION) + " =?))" +
				" ORDER BY " + 
				(!filters.containsKey(FilterType.ORDER_PRICE) ? map_Markets.get(tblMarkets.DTG_PLACED) + " DESC " : "") +
				(filters.containsKey(FilterType.ORDER_PRICE) ? map_Markets.get(tblMarkets.PRICE) +" "+ filters.get(FilterType.ORDER_PRICE)+" " : "") +
				(rowCount == -1 ? "" : "LIMIT ? ") +
				"OFFSET ?;";
		try {
			st = con.prepareStatement(sql);
			int f = 0;
			st.setDouble(++f, Double.valueOf(filters.getOrDefault(FilterType.PRICE_FROM, "0")));
			st.setDouble(++f, Double.valueOf(filters.getOrDefault(FilterType.PRICE_TO, String.valueOf(Double.MAX_VALUE))));
			if (filters.containsKey(FilterType.SOURCE_VENDOR)) st.setInt(++f, Integer.valueOf(filters.get(FilterType.SOURCE_VENDOR)));
			if (filters.containsKey(FilterType.SOURCE_LOCALITY)) st.setObject(++f, UUID.fromString(filters.get(FilterType.SOURCE_LOCALITY)));		
			if (filters.containsKey(FilterType.INCLUDE_MY_SALES)) st.setInt(++f, Integer.valueOf(filters.getOrDefault(FilterType.INCLUDE_MY_SALES, "-1")));
			if (!type.equals(MarketType.AUCTION)) st.setBoolean(++f, Boolean.valueOf(filters.getOrDefault(FilterType.IS_OFFER, "true")));
			st.setBoolean(++f, !isHistory);
			if (rowCount >= 0) {st.setInt(++f, rowCount);}
			st.setInt(++f, indexStart);
			ResultSet rs = executeSELECT(st);
			if (!rs.isBeforeFirst()) return list;
			while (rs.next()) {
				int transID = rs.getInt(map_Markets.get(tblMarkets.ID));
				String stack = rs.getString(map_Markets.get(tblMarkets.ITEM));
				double price = rs.getDouble(map_Markets.get(tblMarkets.PRICE));
				boolean active = rs.getBoolean(map_Markets.get(tblMarkets.ACTIVE_TRANSACTION));
				Agent buyer = getTransactor(rs.getInt(map_Markets.get(tblMarkets.BUYER_ID)));
				Agent vendor = getTransactor(rs.getInt(map_Markets.get(tblMarkets.VENDOR_ID)));
				long placed = rs.getLong(map_Markets.get(tblMarkets.DTG_PLACED));
				long closed = rs.getLong(map_Markets.get(tblMarkets.DTG_CLOSED));
				switch (type) {
				case LOCAL: {
					UUID locality = (UUID) rs.getObject(map_Markets.get(tblMarkets.LOCALITY));					
					boolean giveItem = rs.getBoolean(map_Markets.get(tblMarkets.VENDOR_GIVE_ITEM));
					int originalStock = rs.getInt(map_Markets.get(tblMarkets.STOCK));
					EntryLocal newEntry = new EntryLocal(transID, stack, vendor, buyer, locality,
							price, giveItem, active, originalStock, placed, closed);
					list.add(newEntry);
					break;
				}
				case GLOBAL: case SERVER: {
					boolean giveItem = rs.getBoolean(map_Markets.get(tblMarkets.VENDOR_GIVE_ITEM));
					int originalStock = rs.getInt(map_Markets.get(tblMarkets.STOCK));
					EntryGlobal newEntry = new EntryGlobal(transID, stack, vendor, buyer, price,
							giveItem, active, originalStock, placed, closed);
					list.add(newEntry);
					break;
				}
				case AUCTION: {
					long bidEnd = rs.getLong(map_Markets.get(tblMarkets.BID_END));
					EntryAuction newEntry = new EntryAuction(transID, stack, vendor, buyer, bidEnd, placed, closed, price, active);
					if (bidEnd <= System.currentTimeMillis() && !isHistory) {expireBid(newEntry); break;}
					list.add(newEntry);
					break;
				}
				default: }
			}
 		} catch(SQLException e) {e.printStackTrace();}
		if (type.equals(MarketType.AUCTION)) {
			list.forEach(i -> {
				List<EntryBid> bidList = getBidList(i.getID());
				if (bidList.size()==0) return;
				i = new EntryAuction(i.getID(), i.getStack(), i.getVendor(),
						i.getBuyer(), i.getBidEnd(), i.getDTGPlaced(), i.getDTGClosed(), bidList.get(bidList.size()-1).value, i.getActive());
			});
		}
		return list;
	}
	//NOTE -1 rowCount == get ALL rows
	@Override
	public List<EntryStorage> getStorageList(int indexStart, int rowCount, Agent owner) {
		List<EntryStorage> list = new ArrayList<EntryStorage>();
		PreparedStatement st = null;
		String sql = "SELECT * FROM " + map_Storage.get(tblStorage.TABLE_NAME) + " WHERE " +
				map_Storage.get(tblStorage.OWNER) + " =?" +
				(rowCount == -1 ? "" : "LIMIT ?") +
				"OFFSET ?;";
		try {
			st = con.prepareStatement(sql);
			int f = 0;
			st.setInt(++f, owner.id);
			if (rowCount >= 0) st.setInt(++f, rowCount);
			st.setInt(++f, indexStart);
			ResultSet rs = executeSELECT(st);
			if (!rs.isBeforeFirst()) return list;
			while (rs.next()) {
				int transID = rs.getInt(map_Storage.get(tblStorage.ID));
				String stack = rs.getString(map_Storage.get(tblStorage.ITEM));
				int count = rs.getInt(map_Storage.get(tblStorage.QUANTITY));
				EntryStorage newEntry = new EntryStorage(transID, owner, stack, count);
				list.add(newEntry);
			}
		} catch(SQLException e) {e.printStackTrace();}
		return list;
	}

	@Override
	public List<EntryBid> getBidList(int id) {
		List<EntryBid> list = new ArrayList<EntryBid>();
		PreparedStatement st = null;
		String sql = "SELECT * FROM " + map_Bids.get(tblBids.TABLE_NAME) + " WHERE " + map_Bids.get(tblBids.TRANSACTION_ID) + " = ? " +
				"ORDER BY "+ map_Bids.get(tblBids.PRICE) +" ASC";
		try {
			st = con.prepareStatement(sql);
			st.setInt(1, id);
			ResultSet rs = executeSELECT(st);
			if (!rs.isBeforeFirst()) return list;
			else {
				while (rs.next()) {
					int bidID = rs.getInt(map_Bids.get(tblBids.ID));
					Agent bidder = getTransactor(rs.getInt(map_Bids.get(tblBids.BIDDER_ID)));
					long placed = rs.getLong(map_Bids.get(tblBids.DTG_PLACED));
					double price = rs.getDouble(map_Bids.get(tblBids.PRICE));
					EntryBid bid = new EntryBid(bidID, id, bidder, placed, price);
					list.add(bid);
				}
			}
		} catch(SQLException e) {e.printStackTrace();}
		return list;
	}

	@Override
	public List<EntryOffer> getOfferList(int id, MarketType type) {
		List<EntryOffer> list = new ArrayList<EntryOffer>();
		if (type.equals(MarketType.AUCTION) || type.equals(MarketType.SERVER)) return list;
		PreparedStatement st = null;
		String sql = "SELECT * FROM " + map_Offers.get(tblOffers.TABLE_NAME) + " WHERE " + 
				map_Offers.get(tblOffers.MARKET_NAME) + " = ? AND " +
				map_Offers.get(tblOffers.TRANS_ID) + " = ?;";
		try {
			st = con.prepareStatement(sql);
			st.setString(1, LogicTrade.get().getMarketName(type));
			st.setInt(2, id);
			ResultSet rs = executeSELECT(st);
			if (!rs.isBeforeFirst()) return list;
			else {
				while (rs.next()) {
					int offerID = rs.getInt(map_Offers.get(tblOffers.ID));
					String marketName = rs.getString(map_Offers.get(tblOffers.MARKET_NAME));
					String stack = rs.getString(map_Offers.get(tblOffers.ITEM));
					Agent offerer = getTransactor(rs.getInt(map_Offers.get(tblOffers.OFFERER)));
					long placed = rs.getLong(map_Offers.get(tblOffers.DTG_PLACED));
					int requested = rs.getInt(map_Offers.get(tblOffers.REQUESTED_AMOUNT));
					int offerred = rs.getInt(map_Offers.get(tblOffers.OFFERRED_AMOUNT));
					EntryOffer offer = new EntryOffer(offerID, id, marketName, stack, offerer, placed, requested, offerred);
					list.add(offer);
				}
			}
		} catch(SQLException e) {e.printStackTrace();}
		return list;
	}
	
	@Override
	public IMarketEntry getMarketEntry(int id, MarketType type) {
		PreparedStatement st = null;
		String sql = "SELECT * FROM " +marketTables.get(type) + " WHERE "+
				map_Markets.get(tblMarkets.ID) + "=?";
		try {
			st = con.prepareStatement(sql);
			st.setInt(1, id);
			ResultSet rs = executeSELECT(st);
			if (!rs.isBeforeFirst()) return null;
			rs.next();
			int transID = rs.getInt(map_Markets.get(tblMarkets.ID));
			String stack = rs.getString(map_Markets.get(tblMarkets.ITEM));
			double price = rs.getDouble(map_Markets.get(tblMarkets.PRICE));
			boolean active = rs.getBoolean(map_Markets.get(tblMarkets.ACTIVE_TRANSACTION));
			Agent buyer = getTransactor(rs.getInt(map_Markets.get(tblMarkets.BUYER_ID)));
			long placed = rs.getLong(map_Markets.get(tblMarkets.DTG_PLACED));
			long closed = rs.getLong(map_Markets.get(tblMarkets.DTG_CLOSED));
			switch (type) {
			case LOCAL: {
				UUID locality = (UUID) rs.getObject(map_Markets.get(tblMarkets.LOCALITY));
				Agent vendor = getTransactor(rs.getInt(map_Markets.get(tblMarkets.VENDOR_ID)));
				boolean giveItem = rs.getBoolean(map_Markets.get(tblMarkets.VENDOR_GIVE_ITEM));
				int originalStock = rs.getInt(map_Markets.get(tblMarkets.STOCK));
				return new EntryLocal(transID, stack, vendor, buyer, locality,
						price, giveItem, active, originalStock, placed, closed);
			}
			case GLOBAL: case SERVER: {
				Agent vendor = getTransactor(rs.getInt(map_Markets.get(tblMarkets.VENDOR_ID)));
				boolean giveItem = rs.getBoolean(map_Markets.get(tblMarkets.VENDOR_GIVE_ITEM));
				int originalStock = rs.getInt(map_Markets.get(tblMarkets.STOCK));
				return new EntryGlobal(transID, stack, vendor, buyer, price,
						giveItem, active, originalStock, placed, closed);
			}
			case AUCTION: {
				Agent vendor = getTransactor(rs.getInt(map_Markets.get(tblMarkets.VENDOR_ID)));
				long bidEnd = rs.getLong(map_Markets.get(tblMarkets.BID_END));
				return new EntryAuction(transID, stack, vendor, buyer, bidEnd, placed, closed, price, active);
			}
			default:}
		} catch(SQLException e) {e.printStackTrace();}
		return null;
	}
	
	@Override
	public EntryStorage getStorageEntry(int id) {
		PreparedStatement st = null;
		String sql = "SELECT * FROM " + map_Storage.get(tblStorage.TABLE_NAME) + " WHERE "+
				map_Storage.get(tblStorage.ID) + "=?";
		try {
			st = con.prepareStatement(sql);
			st.setInt(1, id);
			ResultSet rs = executeSELECT(st);
			if (!rs.isBeforeFirst()) return new EntryStorage(-1, new Agent(), "Error", 0);
			rs.next();
			int realID = rs.getInt(map_Storage.get(tblStorage.ID));
			Agent owner = getTransactor(rs.getInt(map_Storage.get(tblStorage.OWNER)));
			String stack = rs.getString(map_Storage.get(tblStorage.ITEM));
			int supply = rs.getInt(map_Storage.get(tblStorage.QUANTITY));
			return new EntryStorage(realID, owner, stack, supply);			
		} catch(SQLException e) {e.printStackTrace();}
		return new EntryStorage(-1, new Agent(), "Error", 0);
	}
	
	@Override
	public Agent getTransactor(int id) {
		PreparedStatement st = null;
		String sql = "SELECT * FROM " + map_Transactors.get(tblTransactors.TABLE_NAME) + " WHERE "+
				map_Transactors.get(tblTransactors.ID) + "=?";
		try {
			st = con.prepareStatement(sql);
			st.setInt(1, id);
			ResultSet rs = executeSELECT(st);
			if (!rs.isBeforeFirst()) return null;
			rs.next();
			int realID = rs.getInt(map_Transactors.get(tblTransactors.ID));
			UUID refID = (UUID) rs.getObject(map_Transactors.get(tblTransactors.REF_ID));
			Type type = Type.values()[rs.getInt(map_Transactors.get(tblTransactors.TYPE))];
			String name = rs.getString(map_Transactors.get(tblTransactors.NAME));
			return new Agent(realID, type, refID, name);
		} catch(SQLException e) {e.printStackTrace();}
		return new Agent();
	}
	
	@Override
	public Agent getTransactor(UUID refID, Type type, String name) {
		PreparedStatement st = null;
		String sql = "SELECT * FROM " + map_Transactors.get(tblTransactors.TABLE_NAME) + " WHERE "+
				map_Transactors.get(tblTransactors.REF_ID) + "=? AND " +
				map_Transactors.get(tblTransactors.TYPE) + "=?";
		try {
			st = con.prepareStatement(sql);
			st.setObject(1, refID);
			st.setInt(2, type.ordinal());
			ResultSet rs = executeSELECT(st);
			if (!rs.isBeforeFirst()) {
				sql = "INSERT INTO " + map_Transactors.get(tblTransactors.TABLE_NAME) + "("+
						map_Transactors.get(tblTransactors.REF_ID) + ", " +
						map_Transactors.get(tblTransactors.TYPE) + ", " +
						map_Transactors.get(tblTransactors.NAME) + 
						") VALUES (?, ?, ?);";
				st = con.prepareStatement(sql);
				st.setObject(1, refID);
				st.setInt(2, type.ordinal());
				st.setString(3, name);
				if (executeUPDATE(st) == 0) return null;
				return getTransactor(refID, type, name);
			}
			rs.next();
			int realID = rs.getInt(map_Transactors.get(tblTransactors.ID));
			UUID realRefID = (UUID) rs.getObject(map_Transactors.get(tblTransactors.REF_ID));
			Type realType = Type.values()[rs.getInt(map_Transactors.get(tblTransactors.TYPE))];
			String realName = rs.getString(map_Transactors.get(tblTransactors.NAME));
			//TODO add in a name update to the agents table
			return new Agent(realID, realType, realRefID, realName);
		} catch(SQLException e) {e.printStackTrace();}
		return new Agent();
	}
	
	private static Map<tblMarkets, String> define_Markets() {
		Map<tblMarkets, String> map = new HashMap<tblMarkets, String>();
		map.put(tblMarkets.ID, "ID");
		map.put(tblMarkets.ITEM, "ITEM");
		map.put(tblMarkets.VENDOR_ID, "VENDOR");
		map.put(tblMarkets.LOCALITY, "LOCALITY");
		map.put(tblMarkets.BID_END, "BID_END");
		map.put(tblMarkets.PRICE, "PRICE");
		map.put(tblMarkets.VENDOR_GIVE_ITEM, "GIVE_ITEM");
		map.put(tblMarkets.STOCK, "STOCK");
		map.put(tblMarkets.ACTIVE_TRANSACTION, "ACTIVE");
		map.put(tblMarkets.BUYER_ID, "BUYER");
		map.put(tblMarkets.DTG_PLACED, "DTG_PLACED");
		map.put(tblMarkets.DTG_CLOSED, "DTG_CLOSED");
		return map;
	}
	
	private static Map<tblBids, String> define_Bids() {
		Map<tblBids, String> map = new HashMap<tblBids, String>();
		map.put(tblBids.TABLE_NAME, "TBL_BIDS");
		map.put(tblBids.ID, "ID");
		map.put(tblBids.TRANSACTION_ID, "TRANS_ID");
		map.put(tblBids.BIDDER_ID, "BIDDER");
		map.put(tblBids.DTG_PLACED, "DTG");
		map.put(tblBids.PRICE, "PRICE");
		return map;
	}
	
	private static Map<tblStorage, String> define_Storage() {
		Map<tblStorage, String> map = new HashMap<tblStorage, String>();
		map.put(tblStorage.TABLE_NAME, "TBL_STORAGE");
		map.put(tblStorage.ID, "ID");
		map.put(tblStorage.OWNER, "OWNER");
		map.put(tblStorage.ITEM, "ITEM");
		map.put(tblStorage.QUANTITY, "COUNT");
		return map;
	}
	
	private static Map<tblOffers, String> define_Offers() {
		Map<tblOffers, String> map = new HashMap<tblOffers, String>();
		map.put(tblOffers.TABLE_NAME, "TBL_OFFERS");
		map.put(tblOffers.ID, "ID");
		map.put(tblOffers.MARKET_NAME, "MARKET_NAME");
		map.put(tblOffers.TRANS_ID, "TRANS_ID");
		map.put(tblOffers.ITEM, "ITEMSTACK");
		map.put(tblOffers.OFFERER, "OFFERER");
		map.put(tblOffers.DTG_PLACED, "DTG");
		map.put(tblOffers.REQUESTED_AMOUNT, "REQUESTED");
		map.put(tblOffers.OFFERRED_AMOUNT, "OFFERRED");
		return map;
	}
	
	private static Map<tblTransactors, String> define_Transactors() {
		Map<tblTransactors, String> map = new HashMap<tblTransactors, String>();
		map.put(tblTransactors.TABLE_NAME, "TBL_VENDORS");
		map.put(tblTransactors.ID, "ID");
		map.put(tblTransactors.REF_ID, "REF_ID");
		map.put(tblTransactors.TYPE, "TYPE");
		map.put(tblTransactors.NAME, "NAME");
		return map;
	}
	
	private static Map<MarketType, String> defineMarketTableNames() {
		Map<MarketType, String> map = new HashMap<MarketType, String>();
		map.put(MarketType.LOCAL, 	"MARKET_LOCAL");
		map.put(MarketType.GLOBAL, 	"MARKET_GLOBAL");
		map.put(MarketType.AUCTION, "MARKET_AUCTION");
		map.put(MarketType.SERVER, 	"MARKET_SERVER");
		return map;
	}

	
}
