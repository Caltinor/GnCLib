package dicemc.gnclib.configs;

import java.util.ArrayList;
import java.util.List;

import dicemc.gnclib.money.LogicMoney;

public class ConfigCore {
	/* Declarations constitute library defaults for config values
	 * Implementations must use the static methods to redefine them
	 * when their respective configs are loaded and able to provide
	 * overwriting values.
	 */
	//Data Storage config values
	public static boolean MARKET_USE_EXTERNAL_DB = false;
	public static String DB_PORT = "";
	public static String DB_NAME = "GNC";
	public static String DB_SERVICE = "h2";
	public static String DB_URL = "";
	public static String DB_USER = "sa";
	public static String DB_PASS = "";
	//Money Related Variables
	public static double STARTING_FUNDS = 1000d;
	public static double GUILD_STARTING_FUNDS = 0d;
	//Guild Related Variables
	public static double GLOBAL_TAX_RATE = 0.1;
	public static long GLOBAL_TAX_INTERVAL = 864000L;
	public static double GUILD_CREATE_COST = 2500d;
	public static double GUILD_NAME_CHANGE_COST = 1500d;
	public static double GUILD_RANK_ADD_COST = 1000d;
	//Real Estate Related Variables
	public static double DEFAULT_LAND_PRICE = 10d;
	public static long TEMPCLAIM_DURATION = 43200000L;
	public static int CHUNKS_PER_MEMBER = 9;
	public static double TEMPCLAIM_RATE = 0.1;
	public static double LAND_ABANDON_REFUND_RATE = 0.75;
	public static double OUTPOST_CREATE_COST = 2000d;
	public static boolean AUTO_TEMPCLAIM = true;
	//Protection Related Variables
	public static boolean UNOWNED_PROTECTED = true;
	public static List<String> PROTECTED_DIMENSION_BLACKLIST = new ArrayList<String>();
	public static List<String> UNOWNED_WHITELIST = new ArrayList<String>();
	//Trade Related Variables
	public static double MARKET_GLOBAL_TAX_BUY = 0.1;
	public static double MARKET_GLOBAL_TAX_SELL = 0.1;
	public static double MARKET_AUCTION_TAX_SELL = 0.3;
	public static long AUCTION_OPEN_DURATION = 259200000L;
	
	public enum DBService {
		H2("h2"),
		MY_SQL("mysql");
		public final String serviceString;
		DBService(String serviceString) {this.serviceString = serviceString;}
		
		public static DBService getFromString() {
			for (int i = 0; i < DBService.values().length; i++) {
				if (DBService.values()[i].serviceString.equals(DB_SERVICE)) return DBService.values()[i];
			}
			return H2;
		}
	}
	
	/**Sets the library DB variables.  If useExternalStorage is set to false,
	 * all other parameters are ignored.  
	 * 
	 * @param useExternalStorage determines if the data should be store in an interan H2 database or point to an external DB.
	 * @param port port for the external DB
	 * @param name table/schema for the external DB
	 * @param service name of the service being used
	 * @param url path to the remote DB service
	 * @param user username for the DB profile
	 * @param password password for the user
	 * @return a log-printable confirmation of the method running
	 */
	public static String defineDataStorageConfigValues(boolean useExternalStorage, String port, String name, String service, String url, String user, String password) {
		MARKET_USE_EXTERNAL_DB = useExternalStorage;
		if (useExternalStorage) {
			DB_PORT = port;
			DB_NAME = name;
			DB_SERVICE = service;
			DB_URL = url;
			DB_USER = user;
			DB_PASS = password;
		}
		return "Data Storage Values uploaded to Lib Variables";
	}
	
	public static String defineMoneyConfigValues(double startingFunds, double guildStartingFunds) {
		STARTING_FUNDS = startingFunds;
		GUILD_STARTING_FUNDS = guildStartingFunds;
		LogicMoney.setService();
		return "Money Values uploaded to Lib Variables";
	}
	
	public static String defineGuildConfigValues(double globalTaxRate, long globalTaxInterval, double guildCreateCost, double guildNameChangeCost, double guildRankAddCost) {
		GLOBAL_TAX_RATE = globalTaxRate;
		GLOBAL_TAX_INTERVAL = globalTaxInterval;
		GUILD_CREATE_COST = guildCreateCost;
		GUILD_NAME_CHANGE_COST = guildNameChangeCost;
		GUILD_RANK_ADD_COST = guildRankAddCost;
		return "Guild Values uploaded to Lib Variables";
	}
	
	public static String defineRealEstateConfigValues(double defaultLandPrice, long tempclaimDuration, int chunksPerMember, double tempclaimRate,
			double landAbandonRefundRate, double outpostCreateCost, boolean autoTempclaim) {
		DEFAULT_LAND_PRICE = defaultLandPrice;
		TEMPCLAIM_DURATION = tempclaimDuration;
		CHUNKS_PER_MEMBER = chunksPerMember;
		TEMPCLAIM_RATE = tempclaimRate;
		LAND_ABANDON_REFUND_RATE = landAbandonRefundRate;
		OUTPOST_CREATE_COST = outpostCreateCost;
		AUTO_TEMPCLAIM = autoTempclaim;
		return "Real Estate Values uploaded to Lib Variables";
	}
	
	public static String defineProtectionConfigValues(boolean unownedProtected, List<String> unownedWhitelist, List<String> protectionBlacklist) {
		UNOWNED_PROTECTED = unownedProtected;
		UNOWNED_WHITELIST = unownedWhitelist;
		PROTECTED_DIMENSION_BLACKLIST = protectionBlacklist;
		return "Protection Values uploaded to Lib Variables";
	}
	
	public static String defineTradeConfigValues(double globalTaxBuy, double globalTaxSell, double auctionTaxSell, long auctionOpenDuration) {
		MARKET_GLOBAL_TAX_BUY = globalTaxBuy;
		MARKET_GLOBAL_TAX_SELL = globalTaxSell;
		MARKET_AUCTION_TAX_SELL = auctionTaxSell;
		AUCTION_OPEN_DURATION = auctionOpenDuration;
		return "Trade Values uploaded to Lib Variables";
	}
}
