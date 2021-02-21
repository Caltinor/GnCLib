package dicemc.gnclib.trade.entries;

import java.nio.charset.Charset;

import dicemc.gnclib.util.Agent;
import dicemc.gnclib.util.IBufferable;
import io.netty.buffer.ByteBuf;

public class EntryOffer implements IBufferable{
	private int id, transactionRef;
	public String marketName, stack;
	public Agent offerer;
	public long placedDate;
	public int requestedAmount, offeredAmount;
	
	public EntryOffer(int id, int transID, String marketName, String itemStack, Agent offerer,
			long placedDate, int requestedAmount, int offeredAmount) {
		this.id = id;
		this.transactionRef = transID;
		this.marketName = marketName;
		this.offerer = offerer;
		this.stack = itemStack;
		this.placedDate = placedDate;
		this.requestedAmount = requestedAmount;
		this.offeredAmount = offeredAmount;
	}
	public EntryOffer(int transID, String marketName, String itemStack, Agent offerer,
			int requestedAmount, int offeredAmount) {
		this(-1, transID, marketName, itemStack, offerer, System.currentTimeMillis(),
				requestedAmount, offeredAmount);
	}
	
	@Override
	public ByteBuf writeBytes(ByteBuf buf) {
		buf.writeInt(id);
		buf.writeInt(transactionRef);
		buf.writeInt(marketName.length());
		buf.writeCharSequence(marketName, Charset.defaultCharset());
		buf.writeInt(stack.length());
		buf.writeCharSequence(stack, Charset.defaultCharset());
		buf.writeLong(placedDate);
		buf.writeInt(requestedAmount);
		buf.writeInt(offeredAmount);
		buf.writeBytes(offerer.writeBytes(buf));
		return buf;
	}

	@Override
	public void readBytes(ByteBuf buf) {
		id = buf.readInt();
		transactionRef = buf.readInt();
		int len = buf.readInt();
		marketName = buf.readCharSequence(len, Charset.defaultCharset()).toString();
		len = buf.readInt();
		stack = buf.readCharSequence(len, Charset.defaultCharset()).toString();
		placedDate = buf.readLong();
		requestedAmount = buf.readInt();
		offeredAmount = buf.readInt();
		offerer = new Agent();
		offerer.readBytes(buf);
	}

	public int getID() {return id;}
	public int getTransactionID() {return transactionRef;}
}
