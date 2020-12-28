package dicemc.gnclib.money.dbref;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

public interface IDBImplMoney {
	ResultSet executeSELECT(PreparedStatement sql);
	
	int executeUPDATE(PreparedStatement sql);
	
	double getBalance(UUID owner, String ownerType);
	
	boolean setBalance(UUID owner, String ownerType, double value);
	
	boolean changeBalance(UUID owner, String ownerType, double value);
	
	boolean transferFunds(UUID ownerFrom, String ownerFromType, UUID ownerTo, String ownerToType, double value);
	
	double addAccount(UUID owner, String ownerType);
}
