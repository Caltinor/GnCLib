package dicemc.gnclib.trade.entries;

import java.util.UUID;

public interface IMarketEntry {
	int getID();
	EntryTransactor getVendor();
	EntryTransactor getBuyer();
	double getPrice();
	int getStock();
	boolean getGiveItem();
	String getStack();
	boolean getActive();
	UUID getLocality();
	long getBidEnd();
	long getDTGPlaced();
	long getDTGClosed();
}
