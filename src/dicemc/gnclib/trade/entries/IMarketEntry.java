package dicemc.gnclib.trade.entries;

import java.util.UUID;

public interface IMarketEntry {
	int getID();
	UUID getVendorID();
	String getVendorName();
	double getPrice();
	int getStock();
	boolean getGiveItem();
	String getStack();
	boolean getActive();
	UUID getBuyerID();
	String getBuyerName();
	UUID getLocality();
	long getBidEnd();
	long getDTGPlaced();
	long getDTGClosed();
}
