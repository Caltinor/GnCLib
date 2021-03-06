package dicemc.gnclib.money.dbref;

import java.util.UUID;

public interface IDBImplMoney {	
	public static enum tblMoney {TABLE_NAME, ID, OWNER, TYPE, BALANCE}
	
	double getBalance(UUID owner, String ownerType);
	
	boolean setBalance(UUID owner, String ownerType, double value);
	
	boolean changeBalance(UUID owner, String ownerType, double value);
	
	boolean transferFunds(UUID ownerFrom, String ownerFromType, UUID ownerTo, String ownerToType, double value);
	
	double addAccount(UUID owner, String ownerType);
	
	void deleteAccount(UUID owner, String ownerType);
}
