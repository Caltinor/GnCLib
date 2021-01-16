package dicemc.gnclib.trade.entries;

import java.nio.charset.Charset;
import java.util.UUID;

import dicemc.gnclib.util.IBufferable;
import io.netty.buffer.ByteBuf;

public class EntryOffer implements IBufferable{
	private int id, transactionRef;
	public String marketName, stack, offererName;
	public UUID offerer;
	public long placedDate;
	public int requestedAmount, offeredAmount;
	
	public EntryOffer(int id, int transID, String marketName, String itemStack, UUID offerer, String offererName,
			long placedDate, int requestedAmount, int offeredAmount) {
		this.id = id;
		this.transactionRef = transID;
		this.marketName = marketName;
		this.offerer = offerer;
		this.offererName = offererName;
		this.stack = itemStack;
		this.placedDate = placedDate;
		this.requestedAmount = requestedAmount;
		this.offeredAmount = offeredAmount;
	}
	public EntryOffer(int transID, String marketName, String itemStack, UUID offerer, String offererName,
			int requestedAmount, int offeredAmount) {
		this(-1, transID, marketName, itemStack, offerer, offererName, System.currentTimeMillis(),
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
		buf.writeInt(offererName.length());
		buf.writeCharSequence(offererName, Charset.defaultCharset());
		buf.writeInt(offerer.toString().length());
		buf.writeCharSequence(offerer.toString(), Charset.defaultCharset());
		buf.writeLong(placedDate);
		buf.writeInt(requestedAmount);
		buf.writeInt(offeredAmount);
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
		len = buf.readInt();
		offererName = buf.readCharSequence(len, Charset.defaultCharset()).toString();
		len = buf.readInt();
		offerer = UUID.fromString(buf.readCharSequence(len, Charset.defaultCharset()).toString());
		placedDate = buf.readLong();
		requestedAmount = buf.readInt();
		offeredAmount = buf.readInt();
	}

	public int getID() {return id;}
	public int getTransactionID() {return transactionRef;}
}
