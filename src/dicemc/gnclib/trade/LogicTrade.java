package dicemc.gnclib.trade;

import java.util.HashMap;
import java.util.Map;
import dicemc.gnclib.configs.ConfigCore;
import dicemc.gnclib.trade.dbref.H2Impl;
import dicemc.gnclib.trade.dbref.IDBImplTrade;
import dicemc.gnclib.trade.dbref.IDBImplTrade.MarketType;

public class LogicTrade {
	public static IDBImplTrade service;
	private static Map<MarketType, Marketplace> Marketplaces = new HashMap<MarketType, Marketplace>();
	
	public static void init() {
		service = setService();
		Marketplaces.put(MarketType.LOCAL, new Marketplace("Local"));
		Marketplaces.put(MarketType.GLOBAL, new Marketplace("Global", ConfigCore.MARKET_GLOBAL_TAX_BUY, ConfigCore.MARKET_GLOBAL_TAX_SELL));
		Marketplaces.put(MarketType.AUCTION, new Marketplace("Auction", 0d, ConfigCore.MARKET_AUCTION_TAX_SELL));
		Marketplaces.put(MarketType.SERVER, new Marketplace("Server"));
	}
	
	private static IDBImplTrade setService() {
		switch (ConfigCore.DBService.getFromString()) {
		case H2: {
			return new H2Impl();
		}
		case MY_SQL: {
			break;
		}
		default:
		}
		return new H2Impl();
	}
	
}
