package dicemc.gnclib.trade.entries;

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
