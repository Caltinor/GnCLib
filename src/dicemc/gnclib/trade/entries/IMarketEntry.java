package dicemc.gnclib.trade.entries;

import java.util.UUID;

public interface IMarketEntry {
	int getID();
	UUID getVendorID();
	double getPrice();
	int getStock();
	boolean getGiveItem();
}
