package dicemc.gnclib.trade.entries;

import java.nio.charset.Charset;
import java.util.UUID;

import dicemc.gnclib.util.Agent;
import dicemc.gnclib.util.ComVars;
import dicemc.gnclib.util.IBufferable;
import io.netty.buffer.ByteBuf;

public class EntryGlobal  implements IBufferable, IMarketEntry{
	private int id;
	public String stack;
	public Agent vendor, buyer;
	public double price;
	public boolean giveItem, openTransaction;
	public int stock;
	public long datePosted, dateClosed;

	public EntryGlobal(int id, String stack, Agent vendor, Agent buyer, double price, boolean giveItem, 
			boolean openTransaction, int stock, long datePosted, long dateClosed) {
		this.id = id;
		this.stack = stack;
		this.vendor = vendor;
		this.buyer = buyer;
		this.price = price;
		this.giveItem = giveItem;
		this.openTransaction = openTransaction;
		this.stock = stock;
		this.datePosted = datePosted;
		this.dateClosed = dateClosed;
	}
	public EntryGlobal(Agent vendor, String itemStack, int stock, double price, boolean giveItem) {
		this(-1, itemStack, vendor, new Agent(), price, giveItem, true, stock, System.currentTimeMillis(), 0L);
	}
	
	@Override
	public ByteBuf writeBytes(ByteBuf buf) {
		buf.writeInt(id);
		buf.writeInt(stack.length());
		buf.writeCharSequence(stack, Charset.defaultCharset());
		buf.writeDouble(price);
		buf.writeBoolean(giveItem);
		buf.writeBoolean(openTransaction);
		buf.writeInt(stock);
		buf.writeLong(datePosted);
		buf.writeLong(dateClosed);
		buf.writeBytes(vendor.writeBytes(buf));
		buf.writeBytes(buyer.writeBytes(buf));
		return buf;
	}

	@Override
	public void readBytes(ByteBuf buf) {
		id = buf.readInt();
		int len = buf.readInt();
		stack = buf.readCharSequence(len, Charset.defaultCharset()).toString();
		price = buf.readDouble();
		giveItem = buf.readBoolean();
		openTransaction = buf.readBoolean();
		stock = buf.readInt();
		datePosted = buf.readLong();
		dateClosed = buf.readLong();
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
	public int getStock() {return stock;}
	@Override
	public boolean getGiveItem() {return giveItem;}
	@Override
	public String getStack() {return stack;}
	@Override
	public boolean getActive() {return openTransaction;}
	@Override
	public UUID getLocality() {return ComVars.NIL;}
	@Override
	public long getBidEnd() {return 0L;}
	@Override
	public long getDTGPlaced() {return datePosted;}
	@Override
	public long getDTGClosed() {return dateClosed;}
	@Override
	public void setVendor(Agent vendor) {this.vendor = vendor;}
	@Override
	public void setBuyer(Agent buyer) {this.buyer = buyer;}

}
