package dicemc.gnclib.trade.entries;

import java.util.UUID;

public interface IMarketEntry {
	int getID();
	EntryTransactor getVendor();
	void setVendor(EntryTransactor vendor);
	EntryTransactor getBuyer();
	void setBuyer(EntryTransactor buyer);
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
