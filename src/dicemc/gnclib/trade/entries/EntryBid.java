package dicemc.gnclib.trade.entries;

import java.nio.charset.Charset;
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
		buf.writeInt(id);
		buf.writeInt(transactionRef);
		buf.writeInt(bidder.toString().length());
		buf.writeCharSequence(bidder.toString(), Charset.defaultCharset());
		buf.writeInt(bidderName.length());
		buf.writeCharSequence(bidderName, Charset.defaultCharset());
		buf.writeLong(placedDate);
		buf.writeDouble(value);
		return buf;
	}

	@Override
	public void readBytes(ByteBuf buf) {
		id = buf.readInt();
		transactionRef = buf.readInt();
		int len = buf.readInt();
		bidder = UUID.fromString(buf.readCharSequence(len, Charset.defaultCharset()).toString());
		len = buf.readInt();
		bidderName = buf.readCharSequence(len, Charset.defaultCharset()).toString();
		placedDate = buf.readLong();
		value = buf.readDouble();
	}
	
	public int getID() {return id;}
	public int getTransactionID() {return transactionRef;}

}
