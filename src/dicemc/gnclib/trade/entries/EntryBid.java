package dicemc.gnclib.trade.entries;

import java.util.UUID;

import dicemc.gnclib.util.IBufferable;
import io.netty.buffer.ByteBuf;

public class EntryBid implements IBufferable{
	private int id, transactionRef;
	public UUID bidder;
	public String bidderName;
	public long placedDate;
	public double value;
	
	public EntryBid(int id, int transID, UUID bidder, String bidderName, long placedDate, double value) {
		this.id = id;
		this.transactionRef = transID;
		this.bidder = bidder;
		this.bidderName = bidderName;
		this.placedDate = placedDate;
		this.value = value;
	}
	public EntryBid(int transID, UUID bidder, String bidderName, double value) {
		this(-1, transID, bidder, bidderName, System.currentTimeMillis(), value);
	}
	
	@Override
	public ByteBuf writeBytes(ByteBuf buf) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void readBytes(ByteBuf buf) {
		// TODO Auto-generated method stub
		
	}
	
	public int getID() {return id;}
	public int getTransactionID() {return transactionRef;}

}
