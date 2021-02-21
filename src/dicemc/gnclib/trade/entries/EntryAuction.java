package dicemc.gnclib.trade.entries;

import java.nio.charset.Charset;
import java.util.UUID;

import dicemc.gnclib.configs.ConfigCore;
import dicemc.gnclib.util.Agent;
import dicemc.gnclib.util.ComVars;
import dicemc.gnclib.util.IBufferable;
import io.netty.buffer.ByteBuf;

public class EntryAuction implements IBufferable, IMarketEntry{
	private int id;
	public String stack;
	public Agent vendor, buyer;
	public long bidEnd, datePosted, dateClosed;
	public double price;
	public boolean openTransaction;
	
	public EntryAuction(int id, String stack, Agent vendor, Agent buyer, long bidEnd, long datePosted, long dateClosed, double price, boolean open) {
		this.id = id;
		this.stack = stack;
		this.vendor = vendor;
		this.buyer = buyer;
		this.bidEnd = bidEnd;
		this.datePosted = datePosted;
		this.dateClosed = dateClosed;
		this.price = price;
		this.openTransaction = open;
	}
	public EntryAuction(Agent vendor, String itemStack, double price) {
		this(0, itemStack, vendor, new Agent(), System.currentTimeMillis()+ConfigCore.AUCTION_OPEN_DURATION, System.currentTimeMillis(), 0L, price, true);
	}
	
	@Override
	public ByteBuf writeBytes(ByteBuf buf) {
		buf.writeInt(id);
		buf.writeInt(stack.length());
		buf.writeCharSequence(stack, Charset.defaultCharset());
		buf.writeLong(bidEnd);
		buf.writeLong(datePosted);
		buf.writeLong(dateClosed);
		buf.writeDouble(price);
		buf.writeBoolean(openTransaction);
		buf.writeBytes(vendor.writeBytes(buf));
		buf.writeBytes(buyer.writeBytes(buf));
		return buf;
	}

	@Override
	public void readBytes(ByteBuf buf) {
		id = buf.readInt();
		int len = buf.readInt();
		stack = buf.readCharSequence(len, Charset.defaultCharset()).toString();
		bidEnd = buf.readLong();
		datePosted = buf.readLong();
		dateClosed = buf.readLong();
		price = buf.readDouble();
		openTransaction = buf.readBoolean();
		vendor = new Agent();
		vendor.readBytes(buf);
		buyer = new Agent();
		buyer.readBytes(buf);
	}
	
	@Override
	public int getID() {return id;}
	@Override
	public Agent getVendor() {return vendor;}
	@Override
	public Agent getBuyer() {return buyer;}
	@Override
	public double getPrice() {return price;}
	@Override
	public int getStock() {return 1;}
	@Override
	public boolean getGiveItem() {return true;}
	@Override
	public String getStack() {return stack;}
	@Override
	public boolean getActive() {return openTransaction;}
	@Override
	public UUID getLocality() {return ComVars.NIL;}
	@Override
	public long getBidEnd() {return bidEnd;}
	@Override
	public long getDTGPlaced() {return datePosted;}
	@Override
	public long getDTGClosed() {return dateClosed;}
	@Override
	public void setVendor(Agent vendor) {this.vendor = vendor;}
	@Override
	public void setBuyer(Agent buyer) {this.buyer = buyer;}

}
