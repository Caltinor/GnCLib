package dicemc.gnclib.money;

import java.util.UUID;

import dicemc.gnclib.configs.ConfigCore;
import dicemc.gnclib.util.ComVars;
import dicemc.gnclib.money.dbref.H2Impl;
import dicemc.gnclib.money.dbref.IDBImplMoney;
import dicemc.gnclib.trade.entries.EntryTransactor;

public class LogicMoney {
	private static IDBImplMoney service;
	
	public enum AccountType{
		SERVER(ComVars.MOD_ID+":server"),
		PLAYER(ComVars.MOD_ID+":player"),
		GUILD(ComVars.MOD_ID+":guild"),
		RANK(ComVars.MOD_ID+":rank"),
		DEBT(ComVars.MOD_ID+":debt");
		public final String rl;
		AccountType(String resourceLocation) {rl = resourceLocation;}
	}
	
	public static void init(String worldName) {
		service = setService(worldName);
	}
	
	private static IDBImplMoney setService(String worldName) {
		switch (ConfigCore.DBService.getFromString()) {
		case H2: {
			return new H2Impl(worldName);
		}
		case MY_SQL: {
			break;
		}
		default:
		}
		return new H2Impl(worldName);
	}
	
	public static void printTable() {((H2Impl)service).printAllTables();}

	/* String type resourceLocations are "MODID:ACCOUNT_TYPE" strings
	 * this is to keep in the style of MC ResourceLocations for 
	 * any future compat that may be needed. 
	 */
	public static double getBalance(UUID owner, String resourceLocation) {
		return service.getBalance(owner, resourceLocation);
	}
	
	public static boolean setBalance(UUID owner, String resourceLocation, double value) {
		return service.setBalance(owner, resourceLocation, value);
	}

	public static void changeBalance(UUID owner, String resourceLocation, double d) {
		service.changeBalance(owner, resourceLocation, d);
	}

	public static boolean transferFunds(UUID ownerFrom, String ownerFromType, UUID ownerTo, String ownerToType, double value) {
		return service.transferFunds(ownerFrom, ownerFromType, ownerTo, ownerToType, value);
	}
	
	public static String transactorType(EntryTransactor.Type type) {
		switch (type) {
		case NONE: {break;}
		case SERVER: {return AccountType.SERVER.rl;} 
		case PLAYER: {return AccountType.PLAYER.rl;}
		case GUILD: {return AccountType.GUILD.rl;}
		default:}
		return "";
	}
}
