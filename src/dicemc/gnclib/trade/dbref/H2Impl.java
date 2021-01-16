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
import dicemc.gnclib.trade.entries.EntryAuction;
import dicemc.gnclib.trade.entries.EntryBid;
import dicemc.gnclib.trade.entries.EntryGlobal;
import dicemc.gnclib.trade.entries.EntryLocal;
import dicemc.gnclib.trade.entries.EntryOffer;
import dicemc.gnclib.trade.entries.EntryServer;
import dicemc.gnclib.trade.entries.EntryStorage;
import dicemc.gnclib.trade.entries.IMarketEntry;
import dicemc.gnclib.util.IDatabase;
import dicemc.gnclib.util.TranslatableResult;

public class H2Impl implements IDBImplTrade, IDatabase{
	public static final Map<tblMarkets, String> map_Markets = define_Markets();
	public static final Map<tblBids, String> map_Bids = define_Bids();
	public static final Map<tblStorage, String> map_Storage = define_Storage();
	public static final Map<tblOffers, String> map_Offers = define_Offers();
	public static final Map<MarketType, String> marketTables = defineMarketTableNames();
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
	public Map<String, String> defineTables() {
		Map<String, String> map = new HashMap<String, String>();
		map.put(marketTables.get(MarketType.LOCAL), defineMarketTable(MarketType.LOCAL));
		map.put(marketTables.get(MarketType.GLOBAL), defineMarketTable(MarketType.GLOBAL));
		map.put(marketTables.get(MarketType.AUCTION), defineMarketTable(MarketType.AUCTION));
		map.put(marketTables.get(MarketType.SERVER), defineMarketTable(MarketType.SERVER));
		String sql = " (" + map_Bids.get(tblBids.ID)	+" INT NOT NULL AUTO_INCREMENT, " +
				map_Bids.get(tblBids.TRANSACTION_ID)	+" INT NOT NULL, " +
				map_Bids.get(tblBids.BIDDER_ID)			+" VARCHAR(36) NOT NULL, " +
				map_Bids.get(tblBids.BIDDER_NAME)		+" VARCHAR(32) NOT NULL, " +
				map_Bids.get(tblBids.PRICE)				+" DOUBLE NOT NULL, " +
				map_Bids.get(tblBids.DTG_PLACED)		+" MEDIUMTEXT NOT NULL, " +
				"PRIMARY KEY (" + map_Bids.get(tblBids.ID) + "));";
		map.put(map_Bids.get(tblBids.TABLE_NAME), sql);
		sql = " (" + map_Offers.get(tblOffers.ID)		+" INT NOT NULL AUTO_INCREMENT, " +
				map_Offers.get(tblOffers.TRANS_ID)		+" INT NOT NULL, " +
				map_Offers.get(tblOffers.MARKET_NAME)	+" VARCHAR(16) NOT NULL, " +
				map_Offers.get(tblOffers.ITEM)			+" LONGTEXT NOT NULL, " +
				map_Offers.get(tblOffers.OFFERER) 		+" VARCHAR(36) NOT NULL, " +
				map_Offers.get(tblOffers.OFFERER_NAME)	+" VARCHAR(32) NOT NULL, " +
				map_Offers.get(tblOffers.DTG_PLACED)	+" MEDIUMTEXT NOT NULL, " +
				map_Offers.get(tblOffers.REQUESTED_AMOUNT)+" INT NOT NULL, " +
				map_Offers.get(tblOffers.OFFERRED_AMOUNT)+" INT NOT NULL, " +
				"PRIMARY KEY ("+map_Offers.get(tblOffers.ID) + "));";
		map.put(map_Offers.get(tblOffers.TABLE_NAME), sql);
		sql = " (" + map_Storage.get(tblStorage.ID) 		+" INT NOT NULL AUTO_INCREMENT, " +
				map_Storage.get(tblStorage.OWNER) 			+" VARCHAR(36) NOT NULL, " +
				map_Storage.get(tblStorage.ITEM) 			+" LONGTEXT NOT NULL, " +
				map_Storage.get(tblStorage.QUANTITY) 		+" INT NOT NULL, " + 
				"PRIMARY KEY (" + map_Storage.get(tblStorage.ID) + "));";
		map.put(map_Storage.get(tblStorage.TABLE_NAME), sql);
		return map;
	}
	
	private String defineMarketTable(MarketType type) {
		String str = " ("+ map_Markets.get(tblMarkets.ID) 			+" INT NOT NULL AUTO_INCREMENT, " +
				map_Markets.get(tblMarkets.ITEM) 				+" LONGTEXT NOT NULL, " +
				(!type.equals(MarketType.SERVER) ? map_Markets.get(tblMarkets.VENDOR_ID)	+" VARCHAR(36) NOT NULL, " : "") +
				(!type.equals(MarketType.SERVER) ? map_Markets.get(tblMarkets.VENDOR_NAME)	+" VARCHAR(32) NOT NULL, " : "") +
				(type.equals(MarketType.LOCAL) ? map_Markets.get(tblMarkets.LOCALITY)		+" VARCHAR(36) NOT NULL, " : "") +
				(type.equals(MarketType.AUCTION) ? map_Markets.get(tblMarkets.BID_END)		+" MEDIUMTEXT NOT NULL, "  : "") +
				map_Markets.get(tblMarkets.PRICE)				+" DOUBLE NOT NULL, " +
				(!type.equals(MarketType.AUCTION) ? map_Markets.get(tblMarkets.VENDOR_GIVE_ITEM)	+" TINYINT(1) NOT NULL, " : "") +
				(!type.equals(MarketType.AUCTION) ? map_Markets.get(tblMarkets.STOCK)			+" INT NOT NULL, " : "") +
				map_Markets.get(tblMarkets.ACTIVE_TRANSACTION)	+" TINYINT(1) NOT NULL, " +
				map_Markets.get(tblMarkets.BUYER_ID)			+" VARCHAR(36) NOT NULL, " +
				map_Markets.get(tblMarkets.DTG_PLACED)			+" MEDIUMTEXT NOT NULL, " +
				map_Markets.get(tblMarkets.DTG_CLOSED)			+" MEDIUMTEXT NOT NULL, " +
				"PRIMARY KEY (" + map_Markets.get(tblMarkets.ID) + "));";
		return str;
	}
	
	@Override
	public TranslatableResult<TradeResult> createTransaction(IMarketEntry entry, MarketType type) {
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
				int f = 1;
				st.setString(f, entry.getStack());
				st.setString(f++, entry.getVendorID().toString());
				st.setDouble(f++, entry.getPrice());
				st.setBoolean(f++, entry.getGiveItem());
				if (type.equals(MarketType.LOCAL)) st.setString(f++, entry.getLocality().toString());
				ResultSet rs = executeSELECT(st);
				if (rs.isBeforeFirst()) {
					rs.next();
					int transID = rs.getInt(map_Markets.get(tblMarkets.ID));
					String stack = rs.getString(map_Markets.get(tblMarkets.ITEM));
					double price = rs.getDouble(map_Markets.get(tblMarkets.PRICE));
					boolean active = rs.getBoolean(map_Markets.get(tblMarkets.ACTIVE_TRANSACTION));
					UUID buyer = UUID.fromString(rs.getString(map_Markets.get(tblMarkets.BUYER_ID)));
					String buyerName = rs.getString(map_Markets.get(tblMarkets.BUYER_NAME));
					long placed = rs.getLong(map_Markets.get(tblMarkets.DTG_PLACED));
					long closed = rs.getLong(map_Markets.get(tblMarkets.DTG_CLOSED));
					switch (type) {
					case LOCAL: {
						UUID locality = UUID.fromString(rs.getString(map_Markets.get(tblMarkets.LOCALITY)));
						UUID vendor = UUID.fromString(rs.getString(map_Markets.get(tblMarkets.VENDOR_ID)));
						String vendorName = rs.getString(map_Markets.get(tblMarkets.VENDOR_NAME));
						boolean giveItem = rs.getBoolean(map_Markets.get(tblMarkets.VENDOR_GIVE_ITEM));
						int originalStock = rs.getInt(map_Markets.get(tblMarkets.STOCK));
						EntryLocal newEntry = new EntryLocal(transID, stack, vendorName, buyerName, vendor, buyer, locality,
								price, giveItem, active, originalStock, placed, closed);
						 return changeTransactionSupply(type, newEntry, entry.getStock());
					}
					case GLOBAL: {
						UUID vendor = UUID.fromString(rs.getString(map_Markets.get(tblMarkets.VENDOR_ID)));
						String vendorName = rs.getString(map_Markets.get(tblMarkets.VENDOR_NAME));
						boolean giveItem = rs.getBoolean(map_Markets.get(tblMarkets.VENDOR_GIVE_ITEM));
						int originalStock = rs.getInt(map_Markets.get(tblMarkets.STOCK));
						EntryGlobal newEntry = new EntryGlobal(transID, stack, vendorName, buyerName, vendor, buyer, price,
								giveItem, active, originalStock, placed, closed);
						return changeTransactionSupply(type, newEntry, entry.getStock());
					}
					case SERVER: {
						boolean giveItem = rs.getBoolean(map_Markets.get(tblMarkets.VENDOR_GIVE_ITEM));
						int originalStock = rs.getInt(map_Markets.get(tblMarkets.STOCK));
						EntryServer newEntry = new EntryServer(transID, stack, buyerName, buyer, price, giveItem, active,
								originalStock, placed, closed);
						return changeTransactionSupply(type, newEntry, entry.getStock());
					}
					default: }
				}
			} catch (SQLException e) {e.printStackTrace();}
		}
		String sql = "INSERT INTO " + marketTables.get(type) + "(" +
				map_Markets.get(tblMarkets.ITEM) +", " +
				map_Markets.get(tblMarkets.PRICE) +", " +
				map_Markets.get(tblMarkets.ACTIVE_TRANSACTION) +", " +
				map_Markets.get(tblMarkets.BUYER_ID) +", " +
				map_Markets.get(tblMarkets.BUYER_NAME) +", " +
				map_Markets.get(tblMarkets.DTG_PLACED) +", " +
				map_Markets.get(tblMarkets.DTG_CLOSED) +", " +
				(type.equals(MarketType.LOCAL) ? 
					map_Markets.get(tblMarkets.LOCALITY) +", " : "") +
				(type.equals(MarketType.AUCTION) ? 
						map_Markets.get(tblMarkets.BID_END) +", " : "") +
				(!type.equals(MarketType.SERVER) ? 
						map_Markets.get(tblMarkets.VENDOR_ID) +", " +
						map_Markets.get(tblMarkets.VENDOR_NAME) +", " : "") +
				(!type.equals(MarketType.AUCTION) ? 
						map_Markets.get(tblMarkets.VENDOR_GIVE_ITEM) +", " +
						map_Markets.get(tblMarkets.STOCK) +", " : "") +
				") VALUES (?, ?, ?, ?, ?, ?" +
				(type.equals(MarketType.LOCAL) ? ", ?" : "") +
				(type.equals(MarketType.AUCTION) ? ", ?" : "") +
				(!type.equals(MarketType.SERVER) ? ", ?, ?" : "") +
				(!type.equals(MarketType.AUCTION) ? ", ?, ?" : "") +
				");";
		try {
			st = con.prepareStatement(sql);
			int f = 1;
			st.setString(f, entry.getStack());
			st.setDouble(f++, entry.getPrice());
			st.setBoolean(f++, entry.getActive());
			st.setString(f++, entry.getBuyerID().toString());
			st.setString(f++, entry.getBuyerName());
			st.setLong(f++, entry.getDTGPlaced());
			st.setLong(f++, entry.getDTGClosed());
			if (type.equals(MarketType.LOCAL)) st.setString(f++, entry.getLocality().toString());
			if (type.equals(MarketType.AUCTION)) st.setLong(f++, entry.getBidEnd());
			if (!type.equals(MarketType.SERVER)) { 
				st.setString(f++, entry.getVendorID().toString());
				st.setString(f++, entry.getVendorName());}
			if (!type.equals(MarketType.AUCTION)) {
				st.setBoolean(f++, entry.getGiveItem());
				st.setInt(f++, entry.getStock());
			}
			if (executeUPDATE(st) == 0 ) return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.create.failure.sql");
		} catch (SQLException e) {e.printStackTrace();}
		return new TranslatableResult<TradeResult>(TradeResult.SUCCESS, "lib.market.create.success");
	}

	@Override
	public TranslatableResult<TradeResult> closeTransaction(int id, MarketType type) {
		PreparedStatement st = null;
		String sql = "UPDATE " + marketTables.get(type) + " SET " + 
				map_Markets.get(tblMarkets.DTG_CLOSED) +" = ? AND "+ 
				map_Markets.get(tblMarkets.ACTIVE_TRANSACTION) + " =? WHERE " + 
				map_Markets.get(tblMarkets.ID) + " = ?;";
		try {
			st = con.prepareStatement(sql);
			st.setLong(1, System.currentTimeMillis());
			st.setBoolean(2, false);
			st.setInt(3, id);
			if (executeUPDATE(st) == 0) return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.close.failure.missing");
		} catch (SQLException e) {e.printStackTrace();}
		List<EntryOffer> offerList = getOfferList(id, type);
		for (EntryOffer list : offerList) {
			addToStorage(new EntryStorage(list.offerer, list.stack, list.offeredAmount));
			sql = "DELETE FROM " + map_Offers.get(tblOffers.TABLE_NAME) + 
					"WHERE " + map_Offers.get(tblOffers.TRANS_ID) + " = ?;";
			try {
				st = con.prepareStatement(sql);
				st.setInt(1, list.getTransactionID());
				if (executeUPDATE(st) == 0) return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.close.failure.missing");
			} catch (SQLException e) {e.printStackTrace();}
		}
		return new TranslatableResult<TradeResult>(TradeResult.SUCCESS, "lib.market.close.success");
	}

	@SuppressWarnings("resource")
	@Override
	public TranslatableResult<TradeResult> acceptOffer(IMarketEntry entry, MarketType type, EntryOffer offer) {
		PreparedStatement st = null;
		String sql = "DELETE FROM " + 
				map_Offers.get(tblOffers.TABLE_NAME) +" WHERE " +
				map_Offers.get(tblOffers.ID) +" = ?";
		try {
			st = con.prepareStatement(sql);
			st.setInt(1, offer.getID());
			if (executeUPDATE(st) == 0) return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.acceptoffer.failure.missing");
		} catch(SQLException e) {e.printStackTrace();} 
		if (offer.requestedAmount == entry.getStock()) {
			sql = "UPDATE " + marketTables.get(type) + " SET " +
					map_Markets.get(tblMarkets.BUYER_ID) + " =? AND " +
					map_Markets.get(tblMarkets.BUYER_NAME) + " =? AND " +
					map_Markets.get(tblMarkets.PRICE) + " =? WHERE " +
					map_Markets.get(tblMarkets.ID) + " =?";
			try {
				st = con.prepareStatement(sql);
				st.setString(1, offer.offerer.toString());
				st.setString(2, offer.offererName);
				st.setDouble(3, 0);
				st.setInt(4, entry.getID());
				if (executeUPDATE(st) == 0) return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.acceptoffer.failure.missing");
			} catch(SQLException e) {e.printStackTrace();}
			return closeTransaction(entry.getID(), type);
		}
		else if (offer.requestedAmount < entry.getStock()) {
			sql = "UPDATE " + marketTables.get(type) + " SET " +
					map_Markets.get(tblMarkets.STOCK) + " =? WHERE " +
					map_Markets.get(tblMarkets.ID) + " =?";
			try {
				st = con.prepareStatement(sql);
				st.setInt(1, entry.getStock() - offer.requestedAmount);
				st.setInt(2, entry.getID());
				if (executeUPDATE(st) == 0) return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.acceptoffer.failure.missing");
			} catch(SQLException e) {e.printStackTrace();}
			sql = "INSERT INTO " + marketTables.get(type) + "(" +
					map_Markets.get(tblMarkets.ITEM) +", " +
					map_Markets.get(tblMarkets.PRICE) +", " +
					map_Markets.get(tblMarkets.ACTIVE_TRANSACTION) +", " +
					map_Markets.get(tblMarkets.BUYER_ID) +", " +
					map_Markets.get(tblMarkets.BUYER_NAME) +", " +
					map_Markets.get(tblMarkets.DTG_PLACED) +", " +
					map_Markets.get(tblMarkets.DTG_CLOSED) +", " +
					map_Markets.get(tblMarkets.VENDOR_ID) +", " +
					map_Markets.get(tblMarkets.VENDOR_NAME) +", " +
					map_Markets.get(tblMarkets.VENDOR_GIVE_ITEM) +", " +
					map_Markets.get(tblMarkets.STOCK) +", " +
					(type.equals(MarketType.LOCAL) ? 
						map_Markets.get(tblMarkets.LOCALITY) +", " : "") +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?" +
					(type.equals(MarketType.LOCAL) ? ", ?" : "") +
					");";
			try {
				st = con.prepareStatement(sql);
				int f = 1;
				st.setString(f, entry.getStack());
				st.setDouble(f++, 0);
				st.setBoolean(f++, false);
				st.setString(f++, offer.offerer.toString());
				st.setString(f++, offer.offererName);
				st.setLong(f++, entry.getDTGPlaced());
				st.setLong(f++, System.currentTimeMillis());
				st.setString(f++, entry.getVendorID().toString());
				st.setString(f++, entry.getVendorName());
				st.setBoolean(f++, entry.getGiveItem());
				st.setInt(f++, offer.requestedAmount);
				if (type.equals(MarketType.LOCAL)) st.setString(f++, entry.getLocality().toString());
				if (executeUPDATE(st) == 0 ) return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.acceptoffer.failure.insert");
			} catch (SQLException e) {e.printStackTrace();}
		}
		else return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.acceptoffer.failure.count");
		
		return new TranslatableResult<TradeResult>(TradeResult.SUCCESS, "lib.market.acceptoffer.success");
	}

	@Override
	public TranslatableResult<TradeResult> expireBid(EntryAuction entry) {
		return closeTransaction(entry.getID(), MarketType.AUCTION);
	}

	@Override
	public TranslatableResult<TradeResult> executeTransaction(IMarketEntry entry, MarketType type, UUID buyer,
			String buyerName, int count) {
		PreparedStatement st = null;
		String sql = "";
		if (count == entry.getStock()) {
			sql = "UPDATE " + marketTables.get(type) + " SET " +
					map_Markets.get(tblMarkets.BUYER_ID) + " =? AND " +
					map_Markets.get(tblMarkets.BUYER_NAME) + " =? WHERE " +
					map_Markets.get(tblMarkets.ID) + " =?";
			try {
				st = con.prepareStatement(sql);
				st.setString(1, buyer.toString());
				st.setString(2, buyerName);
				st.setInt(3, entry.getID());
				if (executeUPDATE(st) == 0) return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.executetrans.failure.missing");
			} catch(SQLException e) {e.printStackTrace();}
			return closeTransaction(entry.getID(), type);
		}
		else if (count < entry.getStock()) {
			sql = "UPDATE " + marketTables.get(type) + " SET " +
					map_Markets.get(tblMarkets.STOCK) + " =? WHERE " +
					map_Markets.get(tblMarkets.ID) + " =?";
			try {
				st = con.prepareStatement(sql);
				st.setInt(1, entry.getStock() - count);
				st.setInt(2, entry.getID());
				if (executeUPDATE(st) == 0) return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.executetrans.failure.missing");
			} catch(SQLException e) {e.printStackTrace();}
			sql = "INSERT INTO " + marketTables.get(type) + "(" +
					map_Markets.get(tblMarkets.ITEM) +", " +
					map_Markets.get(tblMarkets.PRICE) +", " +
					map_Markets.get(tblMarkets.ACTIVE_TRANSACTION) +", " +
					map_Markets.get(tblMarkets.BUYER_ID) +", " +
					map_Markets.get(tblMarkets.BUYER_NAME) +", " +
					map_Markets.get(tblMarkets.DTG_PLACED) +", " +
					map_Markets.get(tblMarkets.DTG_CLOSED) +", " +
					map_Markets.get(tblMarkets.VENDOR_ID) +", " +
					map_Markets.get(tblMarkets.VENDOR_NAME) +", " +
					map_Markets.get(tblMarkets.VENDOR_GIVE_ITEM) +", " +
					map_Markets.get(tblMarkets.STOCK) +", " +
					(type.equals(MarketType.LOCAL) ? 
						map_Markets.get(tblMarkets.LOCALITY) : "") +
					") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?" +
					(type.equals(MarketType.LOCAL) ? ", ?" : "") +
					");";
			try {
				st = con.prepareStatement(sql);
				int f = 1;
				st.setString(f, entry.getStack());
				st.setDouble(f++, entry.getPrice());
				st.setBoolean(f++, false);
				st.setString(f++, buyer.toString());
				st.setString(f++, buyerName);
				st.setLong(f++, entry.getDTGPlaced());
				st.setLong(f++, System.currentTimeMillis());
				st.setString(f++, entry.getVendorID().toString());
				st.setString(f++, entry.getVendorName());
				st.setBoolean(f++, entry.getGiveItem());
				st.setInt(f++, count);
				if (type.equals(MarketType.LOCAL)) st.setString(f++, entry.getLocality().toString());
				if (executeUPDATE(st) == 0 ) return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.executetrans.failure.missing");
			} catch (SQLException e) {e.printStackTrace();}
		}
		return new TranslatableResult<TradeResult>(TradeResult.SUCCESS, "lib.market.executetrans.success");
	}

	@Override
	public TranslatableResult<TradeResult> submitOffer(IMarketEntry entry, EntryOffer offer) {
		if (entry instanceof EntryAuction || entry instanceof EntryServer) 
			return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.offer.failure.type");
		PreparedStatement st = null;
		String sql = "INSERT INTO " + map_Offers.get(tblOffers.TABLE_NAME) + " (" +
				map_Offers.get(tblOffers.MARKET_NAME) + ", " +
				map_Offers.get(tblOffers.TRANS_ID) + ", " +
				map_Offers.get(tblOffers.ITEM) + ", " +
				map_Offers.get(tblOffers.OFFERER) + ", " +
				map_Offers.get(tblOffers.OFFERER_NAME) + ", " +
				map_Offers.get(tblOffers.OFFERRED_AMOUNT) + ", " +
				map_Offers.get(tblOffers.REQUESTED_AMOUNT) + ", " +
				map_Offers.get(tblOffers.DTG_PLACED) + 
				") VALUES (?, ?, ?, ?, ?, ?, ?, ?);" ;
		try {
			st = con.prepareStatement(sql);
			st.setString(1, offer.marketName);
			st.setInt(2, entry.getID());
			st.setString(3, offer.stack);
			st.setString(4, offer.offerer.toString());
			st.setString(5, offer.offererName);
			st.setInt(6, offer.offeredAmount);
			st.setInt(7, offer.requestedAmount);
			st.setLong(8, offer.placedDate);
			if (executeUPDATE(st) == 0) return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.offer.failure.insert");
		} catch(SQLException e) {e.printStackTrace();}
		return new TranslatableResult<TradeResult>(TradeResult.SUCCESS, "lib.market.offer.success");
	}

	@Override
	public TranslatableResult<TradeResult> placeBid(EntryBid bid) {
		PreparedStatement st = null;
		String sql = "INSERT INTO " + map_Bids.get(tblBids.TABLE_NAME) + "(" +
				map_Bids.get(tblBids.TRANSACTION_ID) + ", " +
				map_Bids.get(tblBids.BIDDER_ID) + ", " +
				map_Bids.get(tblBids.BIDDER_NAME) + ", " +
				map_Bids.get(tblBids.PRICE) + ", " +
				map_Bids.get(tblBids.DTG_PLACED) + ", " +
				") VALUES (?, ?, ?, ?, ?);";
		try {
			st = con.prepareStatement(sql);
			st.setInt(1, bid.getTransactionID());
			st.setString(2, bid.bidder.toString());
			st.setString(3, bid.bidderName);
			st.setDouble(4, bid.value);
			st.setLong(5, bid.placedDate);
			if (executeUPDATE(st) == 0) return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.placebid.failure.insert");
		} catch(SQLException e) {e.printStackTrace();}
		return new TranslatableResult<TradeResult>(TradeResult.SUCCESS, "lib.market.placebid.successs");
	}

	@Override
	public TranslatableResult<TradeResult> addToStorage(EntryStorage entry) {
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
				if (executeUPDATE(st) == 0) return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.storage.place.failure.insert");
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
				st.setString(2, entry.owner.toString());
				st.setInt(3, entry.count);
				if (executeUPDATE(st) == 0) return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.storage.place.failure.insert");
			} catch(SQLException e) {e.printStackTrace();}
		}		
		return new TranslatableResult<TradeResult>(TradeResult.SUCCESS, "lib.market.storage.place.success");
	}

	@Override
	public TranslatableResult<TradeResult> pullFromStorage(EntryStorage entry, int count) {
		PreparedStatement st = null;
		String sql = "";
		if (entry.count == count) {
			sql = "DELETE FROM " + map_Storage.get(tblStorage.TABLE_NAME) + " WHERE " +
					map_Storage.get(tblStorage.ID) + " = ?;";
			try {
				st = con.prepareStatement(sql);
				st.setInt(1, entry.getID());
				if (executeUPDATE(st) == 0) return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.storage.pull.failure.missing");
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
				if (executeUPDATE(st) == 0) return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.storage.pull.failure.missing");
			} catch(SQLException e) {e.printStackTrace();}
		}
		else if (entry.count < count) return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.storage.pull.failure.count");
		return new TranslatableResult<TradeResult>(TradeResult.SUCCESS, "lib.market.storage.pull.success");
	}

	@Override
	public TranslatableResult<TradeResult> changeTransactionSupply(MarketType type, IMarketEntry entry, int newSupply) {
		PreparedStatement st = null;
		String sql = "UPDATE " + marketTables.get(type) + " SET " + 
				map_Markets.get(tblMarkets.STOCK) + " = ? WHERE " +
				map_Markets.get(tblMarkets.ID) + " = ?;";
		try {
			st = con.prepareStatement(sql);
			st.setInt(1, entry.getStock() + newSupply);
			st.setInt(2, entry.getID());
			if (executeUPDATE(st) == 0) return new TranslatableResult<TradeResult>(TradeResult.FAILURE, "lib.market.supplychange.failure.missing");
		} catch(SQLException e) {e.printStackTrace();}
		return new TranslatableResult<TradeResult>(TradeResult.SUCCESS, "lib.market.supplychange.success");
	}

	@Override
	public List<IMarketEntry> getMarketList(MarketType type, int indexStart, int rowCount, Map<FilterType, String> filters) {
		List<IMarketEntry> list = new ArrayList<IMarketEntry>();
		PreparedStatement st = null;
		String sql = "SELECT * FROM " + marketTables.get(type) + " WHERE " +
				map_Markets.get(tblMarkets.ITEM) + " LIKE \"*\" & ? & \"*\" AND (" +
				map_Markets.get(tblMarkets.VENDOR_NAME) + " =? OR " +
				map_Markets.get(tblMarkets.LOCALITY) + " =?) AND (" +
				map_Markets.get(tblMarkets.PRICE) + " BETWEEN ? AND ?) AND " +
				map_Markets.get(tblMarkets.VENDOR_GIVE_ITEM) + " =? AND " +
				map_Markets.get(tblMarkets.ACTIVE_TRANSACTION) + " =? " +
				(rowCount == -1 ? "" : "LIMIT ?") +
				"OFFSET ?;";
		try {
			st = con.prepareStatement(sql);
			int f = 1;
			st.setString(f, filters.getOrDefault(FilterType.NAME, ""));
			st.setString(f++, filters.getOrDefault(FilterType.SOURCE, ""));
			st.setString(f++, filters.getOrDefault(FilterType.SOURCE, ""));
			st.setDouble(f++, Double.valueOf(filters.getOrDefault(FilterType.PRICE_FROM, "0")));
			st.setDouble(f++, Double.valueOf(filters.getOrDefault(FilterType.PRICE_FROM, String.valueOf(Double.MAX_VALUE))));
			st.setBoolean(f++, Boolean.valueOf(filters.getOrDefault(FilterType.IS_OFFER, String.valueOf(true))));
			st.setBoolean(f++, true);
			if (rowCount >= 0) st.setInt(f++, rowCount);
			st.setInt(f++, indexStart);
			ResultSet rs = executeSELECT(st);
			if (!rs.isBeforeFirst()) return list;
			while (rs.next()) {
				int transID = rs.getInt(map_Markets.get(tblMarkets.ID));
				String stack = rs.getString(map_Markets.get(tblMarkets.ITEM));
				double price = rs.getDouble(map_Markets.get(tblMarkets.PRICE));
				boolean active = rs.getBoolean(map_Markets.get(tblMarkets.ACTIVE_TRANSACTION));
				UUID buyer = UUID.fromString(rs.getString(map_Markets.get(tblMarkets.BUYER_ID)));
				String buyerName = rs.getString(map_Markets.get(tblMarkets.BUYER_NAME));
				long placed = rs.getLong(map_Markets.get(tblMarkets.DTG_PLACED));
				long closed = rs.getLong(map_Markets.get(tblMarkets.DTG_CLOSED));
				switch (type) {
				case LOCAL: {
					UUID locality = UUID.fromString(rs.getString(map_Markets.get(tblMarkets.LOCALITY)));
					UUID vendor = UUID.fromString(rs.getString(map_Markets.get(tblMarkets.VENDOR_ID)));
					String vendorName = rs.getString(map_Markets.get(tblMarkets.VENDOR_NAME));
					boolean giveItem = rs.getBoolean(map_Markets.get(tblMarkets.VENDOR_GIVE_ITEM));
					int originalStock = rs.getInt(map_Markets.get(tblMarkets.STOCK));
					EntryLocal newEntry = new EntryLocal(transID, stack, vendorName, buyerName, vendor, buyer, locality,
							price, giveItem, active, originalStock, placed, closed);
					list.add(newEntry);
					break;
				}
				case GLOBAL: {
					UUID vendor = UUID.fromString(rs.getString(map_Markets.get(tblMarkets.VENDOR_ID)));
					String vendorName = rs.getString(map_Markets.get(tblMarkets.VENDOR_NAME));
					boolean giveItem = rs.getBoolean(map_Markets.get(tblMarkets.VENDOR_GIVE_ITEM));
					int originalStock = rs.getInt(map_Markets.get(tblMarkets.STOCK));
					EntryGlobal newEntry = new EntryGlobal(transID, stack, vendorName, buyerName, vendor, buyer, price,
							giveItem, active, originalStock, placed, closed);
					list.add(newEntry);
					break;
				}
				case AUCTION: {
					UUID vendor = UUID.fromString(rs.getString(map_Markets.get(tblMarkets.VENDOR_ID)));
					String vendorName = rs.getString(map_Markets.get(tblMarkets.VENDOR_NAME));
					long bidEnd = rs.getLong(map_Markets.get(tblMarkets.BID_END));
					EntryAuction newEntry = new EntryAuction(transID, stack, vendorName, buyerName, vendor, buyer, bidEnd, placed, closed, price, active);
					list.add(newEntry);
					break;
				}
				case SERVER: {
					boolean giveItem = rs.getBoolean(map_Markets.get(tblMarkets.VENDOR_GIVE_ITEM));
					int originalStock = rs.getInt(map_Markets.get(tblMarkets.STOCK));
					EntryServer newEntry = new EntryServer(transID, stack, buyerName, buyer, price, giveItem, active,
							originalStock, placed, closed);
					list.add(newEntry);
					break;
				}
				default: }
			}
 		} catch(SQLException e) {e.printStackTrace();}
		return list;
	}
	//NOTE -1 rowCount == get ALL rows
	@Override
	public List<EntryStorage> getStorageList(int indexStart, int rowCount, UUID owner) {
		List<EntryStorage> list = new ArrayList<EntryStorage>();
		PreparedStatement st = null;
		String sql = "SELECT * FROM " + map_Storage.get(tblStorage.TABLE_NAME) + " WHERE " +
				map_Storage.get(tblStorage.OWNER) + " =?" +
				(rowCount == -1 ? "" : "LIMIT ?") +
				"OFFSET ?;";
		try {
			st = con.prepareStatement(sql);
			int f = 1;
			st.setString(f, owner.toString());
			if (rowCount >= 0) st.setInt(f++, rowCount);
			st.setInt(f++, indexStart);
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
		String sql = "SELECT * FROM " + map_Bids.get(tblBids.TABLE_NAME) + " WHERE " + map_Bids.get(tblBids.ID) + " = ?;";
		try {
			st = con.prepareStatement(sql);
			st.setInt(1, id);
			ResultSet rs = executeSELECT(st);
			if (!rs.isBeforeFirst()) return list;
			else {
				while (rs.next()) {
					int bidID = rs.getInt(map_Bids.get(tblBids.ID));
					UUID bidder = UUID.fromString(rs.getString(map_Bids.get(tblBids.BIDDER_ID)));
					String bidderName = rs.getString(map_Bids.get(tblBids.BIDDER_NAME));
					long placed = rs.getLong(map_Bids.get(tblBids.DTG_PLACED));
					double price = rs.getDouble(map_Bids.get(tblBids.PRICE));
					EntryBid bid = new EntryBid(bidID, id, bidder, bidderName, placed, price);
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
			st.setString(1, marketTables.get(type));
			st.setInt(2, id);
			ResultSet rs = executeSELECT(st);
			if (!rs.isBeforeFirst()) return list;
			else {
				while (rs.next()) {
					int offerID = rs.getInt(map_Offers.get(tblOffers.ID));
					String marketName = rs.getString(map_Offers.get(tblOffers.MARKET_NAME));
					String stack = rs.getString(map_Offers.get(tblOffers.ITEM));
					UUID offerer = UUID.fromString(rs.getString(map_Offers.get(tblOffers.OFFERER)));
					String offererName = rs.getString(map_Offers.get(tblOffers.OFFERER_NAME));
					long placed = rs.getLong(map_Offers.get(tblOffers.DTG_PLACED));
					int requested = rs.getInt(map_Offers.get(tblOffers.REQUESTED_AMOUNT));
					int offerred = rs.getInt(map_Offers.get(tblOffers.OFFERRED_AMOUNT));
					EntryOffer offer = new EntryOffer(offerID, id, marketName, stack, offerer, offererName,
							placed, requested, offerred);
					list.add(offer);
				}
			}
		} catch(SQLException e) {e.printStackTrace();}
		return list;
	}

	@Override
	public List<IMarketEntry> getTransactionHistory(MarketType type, int indexStart, int rowCount, Map<FilterType, String> filters) {
		List<IMarketEntry> list = new ArrayList<IMarketEntry>();
		PreparedStatement st = null;
		String sql = "SELECT * FROM " + marketTables.get(type) + " WHERE " +
				map_Markets.get(tblMarkets.ITEM) + " LIKE \"*\" & ? & \"*\" AND (" +
				map_Markets.get(tblMarkets.VENDOR_NAME) + " =? OR " +
				map_Markets.get(tblMarkets.LOCALITY) + " =?) AND (" +
				map_Markets.get(tblMarkets.PRICE) + " BETWEEN ? AND ?) AND " +
				map_Markets.get(tblMarkets.VENDOR_GIVE_ITEM) + " =? AND " +
				" ORDER BY " + map_Markets.get(tblMarkets.DTG_CLOSED) + " DESC " +
				(rowCount == -1 ? "" : "LIMIT ?") +
				"OFFSET ?;";
		try {
			st = con.prepareStatement(sql);
			int f = 1;
			st.setString(f, filters.getOrDefault(FilterType.NAME, ""));
			st.setString(f++, filters.getOrDefault(FilterType.SOURCE, ""));
			st.setString(f++, filters.getOrDefault(FilterType.SOURCE, ""));
			st.setDouble(f++, Double.valueOf(filters.getOrDefault(FilterType.PRICE_FROM, "0")));
			st.setDouble(f++, Double.valueOf(filters.getOrDefault(FilterType.PRICE_FROM, String.valueOf(Double.MAX_VALUE))));
			st.setBoolean(f++, Boolean.valueOf(filters.getOrDefault(FilterType.IS_OFFER, String.valueOf(true))));
			if (rowCount >= 0) st.setInt(f++, rowCount);
			st.setInt(f++, indexStart);
			ResultSet rs = executeSELECT(st);
			if (!rs.isBeforeFirst()) return list;
			while (rs.next()) {
				int transID = rs.getInt(map_Markets.get(tblMarkets.ID));
				String stack = rs.getString(map_Markets.get(tblMarkets.ITEM));
				double price = rs.getDouble(map_Markets.get(tblMarkets.PRICE));
				boolean active = rs.getBoolean(map_Markets.get(tblMarkets.ACTIVE_TRANSACTION));
				UUID buyer = UUID.fromString(rs.getString(map_Markets.get(tblMarkets.BUYER_ID)));
				String buyerName = rs.getString(map_Markets.get(tblMarkets.BUYER_NAME));
				long placed = rs.getLong(map_Markets.get(tblMarkets.DTG_PLACED));
				long closed = rs.getLong(map_Markets.get(tblMarkets.DTG_CLOSED));
				switch (type) {
				case LOCAL: {
					UUID locality = UUID.fromString(rs.getString(map_Markets.get(tblMarkets.LOCALITY)));
					UUID vendor = UUID.fromString(rs.getString(map_Markets.get(tblMarkets.VENDOR_ID)));
					String vendorName = rs.getString(map_Markets.get(tblMarkets.VENDOR_NAME));
					boolean giveItem = rs.getBoolean(map_Markets.get(tblMarkets.VENDOR_GIVE_ITEM));
					int originalStock = rs.getInt(map_Markets.get(tblMarkets.STOCK));
					EntryLocal newEntry = new EntryLocal(transID, stack, vendorName, buyerName, vendor, buyer, locality,
							price, giveItem, active, originalStock, placed, closed);
					list.add(newEntry);
					break;
				}
				case GLOBAL: {
					UUID vendor = UUID.fromString(rs.getString(map_Markets.get(tblMarkets.VENDOR_ID)));
					String vendorName = rs.getString(map_Markets.get(tblMarkets.VENDOR_NAME));
					boolean giveItem = rs.getBoolean(map_Markets.get(tblMarkets.VENDOR_GIVE_ITEM));
					int originalStock = rs.getInt(map_Markets.get(tblMarkets.STOCK));
					EntryGlobal newEntry = new EntryGlobal(transID, stack, vendorName, buyerName, vendor, buyer, price,
							giveItem, active, originalStock, placed, closed);
					list.add(newEntry);
					break;
				}
				case AUCTION: {
					UUID vendor = UUID.fromString(rs.getString(map_Markets.get(tblMarkets.VENDOR_ID)));
					String vendorName = rs.getString(map_Markets.get(tblMarkets.VENDOR_NAME));
					long bidEnd = rs.getLong(map_Markets.get(tblMarkets.BID_END));
					EntryAuction newEntry = new EntryAuction(transID, stack, vendorName, buyerName, vendor, buyer, bidEnd, placed, closed, price, active);
					list.add(newEntry);
					break;
				}
				case SERVER: {
					boolean giveItem = rs.getBoolean(map_Markets.get(tblMarkets.VENDOR_GIVE_ITEM));
					int originalStock = rs.getInt(map_Markets.get(tblMarkets.STOCK));
					EntryServer newEntry = new EntryServer(transID, stack, buyerName, buyer, price, giveItem, active,
							originalStock, placed, closed);
					list.add(newEntry);
					break;
				}
				default: }
			}
 		} catch(SQLException e) {e.printStackTrace();}
		return list;
	}
	
	private static Map<tblMarkets, String> define_Markets() {
		Map<tblMarkets, String> map = new HashMap<tblMarkets, String>();
		map.put(tblMarkets.ID, "ID");
		map.put(tblMarkets.ITEM, "Item");
		map.put(tblMarkets.VENDOR_ID, "Vendor");
		map.put(tblMarkets.VENDOR_NAME, "VENDOR_NAME");
		map.put(tblMarkets.LOCALITY, "locality");
		map.put(tblMarkets.BID_END, "bid_end");
		map.put(tblMarkets.PRICE, "Price");
		map.put(tblMarkets.VENDOR_GIVE_ITEM, "GiveItem");
		map.put(tblMarkets.STOCK, "Stock");
		map.put(tblMarkets.ACTIVE_TRANSACTION, "active");
		map.put(tblMarkets.BUYER_ID, "buyer");
		map.put(tblMarkets.BUYER_NAME, "BUYER_NAME");
		map.put(tblMarkets.DTG_PLACED, "DTG_PLACED");
		map.put(tblMarkets.DTG_CLOSED, "DTG_CLOSED");
		return map;
	}
	
	private static Map<tblBids, String> define_Bids() {
		Map<tblBids, String> map = new HashMap<tblBids, String>();
		map.put(tblBids.TABLE_NAME, "tbl_bids_");
		map.put(tblBids.ID, "ID");
		map.put(tblBids.TRANSACTION_ID, "trans_ID");
		map.put(tblBids.BIDDER_ID, "bidder");
		map.put(tblBids.BIDDER_NAME, "BIDDER_NAME");
		map.put(tblBids.DTG_PLACED, "DTG");
		map.put(tblBids.PRICE, "Price");
		return map;
	}
	
	private static Map<tblStorage, String> define_Storage() {
		Map<tblStorage, String> map = new HashMap<tblStorage, String>();
		map.put(tblStorage.TABLE_NAME, "tbl_storage_");
		map.put(tblStorage.ID, "ID");
		map.put(tblStorage.OWNER, "Owner");
		map.put(tblStorage.ITEM, "Item");
		map.put(tblStorage.QUANTITY, "Count");
		return map;
	}
	
	private static Map<tblOffers, String> define_Offers() {
		Map<tblOffers, String> map = new HashMap<tblOffers, String>();
		map.put(tblOffers.TABLE_NAME, "TBL_OFFERS_");
		map.put(tblOffers.ID, "ID");
		map.put(tblOffers.MARKET_NAME, "MARKET_NAME");
		map.put(tblOffers.TRANS_ID, "TRANS_ID");
		map.put(tblOffers.ITEM, "ITEMSTACK");
		map.put(tblOffers.OFFERER, "OFFERER");
		map.put(tblOffers.OFFERER_NAME, "OFFERER_NAME");
		map.put(tblOffers.DTG_PLACED, "DTG");
		map.put(tblOffers.REQUESTED_AMOUNT, "REQUESTED");
		map.put(tblOffers.OFFERRED_AMOUNT, "OFFERRED");
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
