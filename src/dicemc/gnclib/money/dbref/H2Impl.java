package dicemc.gnclib.money.dbref;

import java.util.UUID;

public class H2Impl implements IDBImplMoney{
	
	public H2Impl() {}

	@Override
	public double getBalance(UUID owner, String ownerType) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean setBalance(UUID owner, String ownerType, double value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean changeBalance(UUID owner, String ownerType, double value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean transferFunds(UUID ownerFrom, String ownerFromType, UUID ownerTo, String owernToType, double value) {
		// TODO Auto-generated method stub
		return false;
	}

}
