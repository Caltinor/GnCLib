package dicemc.gnclib.trade.entries;

import dicemc.gnclib.util.Agent;
import dicemc.gnclib.util.IBufferable;
import io.netty.buffer.ByteBuf;

public class EntryBid implements IBufferable{
	private int id, transactionRef;
	public Agent bidder;
	public long placedDate;
	public double value;
	
	public EntryBid(int id, int transID, Agent bidder, long placedDate, double value) {
		this.id = id;
		this.transactionRef = transID;
		this.bidder = bidder;
		this.placedDate = placedDate;
		this.value = value;
	}
	public EntryBid(int transID, Agent bidder, double value) {
		this(-1, transID, bidder, System.currentTimeMillis(), value);
	}
	
	@Override
	public ByteBuf writeBytes(ByteBuf buf) {
		buf.writeInt(id);
		buf.writeInt(transactionRef);
		buf.writeLong(placedDate);
		buf.writeDouble(value);
		buf.writeBytes(bidder.writeBytes(buf));
		return buf;
	}

	@Override
	public void readBytes(ByteBuf buf) {
		id = buf.readInt();
		transactionRef = buf.readInt();
		placedDate = buf.readLong();
		value = buf.readDouble();
		bidder = new Agent();
		bidder.readBytes(buf);
	}
	
	public int getID() {return id;}
	public int getTransactionID() {return transactionRef;}

}
