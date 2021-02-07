package dicemc.gnclib.trade.entries;

import java.nio.charset.Charset;
import java.util.UUID;

import dicemc.gnclib.util.IBufferable;
import io.netty.buffer.ByteBuf;

public class EntryLocal  implements IBufferable, IMarketEntry{
	private int id;
	public String stack;
	public UUID locality;
	public EntryTransactor vendor, buyer;
	public double price;
	public boolean giveItem, openTransaction;
	public int stock;
	public long datePosted, dateClosed;

	public EntryLocal(int id, String stack, EntryTransactor vendor, EntryTransactor buyer, UUID locality, double price, boolean giveItem, 
			boolean openTransaction, int stock, long datePosted, long dateClosed) {
		this.id = id;
		this.stack = stack;
		this.vendor = vendor;
		this.buyer = buyer;
		this.locality = locality;
		this.price = price;
		this.giveItem = giveItem;
		this.openTransaction = openTransaction;
		this.stock = stock;
		this.datePosted = datePosted;
		this.dateClosed = dateClosed;
	}
	public EntryLocal(UUID locality, EntryTransactor vendor, String itemStack, int stock, double price, boolean giveItem) {
		this(-1, itemStack, vendor, new EntryTransactor(), locality, price, giveItem, true, stock, System.currentTimeMillis(), 0L);
	}
	
	@Override
	public ByteBuf writeBytes(ByteBuf buf) {
		buf.writeInt(id);
		buf.writeInt(stack.length());
		buf.writeCharSequence(stack, Charset.defaultCharset());
		buf.writeInt(locality.toString().length());
		buf.writeCharSequence(locality.toString(), Charset.defaultCharset());
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
		len = buf.readInt();
		locality = UUID.fromString(buf.readCharSequence(len, Charset.defaultCharset()).toString());
		price = buf.readDouble();
		giveItem = buf.readBoolean();
		openTransaction = buf.readBoolean();
		stock = buf.readInt();
		datePosted = buf.readLong();
		dateClosed = buf.readLong();
		vendor = new EntryTransactor();
		vendor.readBytes(buf);
		buyer = new EntryTransactor();
		buyer.readBytes(buf);
	}
	
	@Override
	public int getID() {return id;}
	@Override
	public EntryTransactor getVendor() {return vendor;}
	@Override
	public EntryTransactor getBuyer() {return buyer;}
	@Override
	public double getPrice() {return price;}
	@Override
	public int getStock() {return stock;}
	@Override
	public boolean getGiveItem() {return giveItem;}	
	public UUID getLocality() {return locality;}
	@Override
	public String getStack() {return stack;}
	@Override
	public boolean getActive() {return openTransaction;}
	@Override
	public long getBidEnd() {return 0L;}
	@Override
	public long getDTGPlaced() {return datePosted;}
	@Override
	public long getDTGClosed() {return dateClosed;}

}
