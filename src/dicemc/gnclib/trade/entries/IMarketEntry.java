package dicemc.gnclib.trade.entries;

import java.util.UUID;

import dicemc.gnclib.util.Agent;

public interface IMarketEntry {
	int getID();
	Agent getVendor();
	void setVendor(Agent vendor);
	Agent getBuyer();
	void setBuyer(Agent buyer);
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
