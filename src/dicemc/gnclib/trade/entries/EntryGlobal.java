package dicemc.gnclib.trade.entries;

import java.nio.charset.Charset;
import java.util.UUID;

import dicemc.gnclib.util.IBufferable;
import io.netty.buffer.ByteBuf;

public class EntryGlobal  implements IBufferable, IMarketEntry{
	private int id;
	public String stack, vendorName, buyerName;
	public UUID vendor, buyer;
	public double price;
	public boolean giveItem, openTransaction;
	public int stock;
	public long datePosted, dateClosed;

	public EntryGlobal(int id, String stack, String vendorName, String buyerName, UUID vendor, UUID buyer, double price, boolean giveItem, 
			boolean openTransaction, int stock, long datePosted, long dateClosed) {
		this.id = id;
		this.stack = stack;
		this.vendorName = vendorName;
		this.buyerName = buyerName;
		this.vendor = vendor;
		this.buyer = buyer;
		this.price = price;
		this.giveItem = giveItem;
		this.openTransaction = openTransaction;
		this.stock = stock;
		this.datePosted = datePosted;
		this.dateClosed = dateClosed;
	}
	public EntryGlobal(UUID vendor, String vendorName, String itemStack, int stock, double price, boolean giveItem) {
		this(-1, itemStack, vendorName, null, vendor, null, price, giveItem, true, stock, System.currentTimeMillis(), 0L);
	}
	
	@Override
	public ByteBuf writeBytes(ByteBuf buf) {
		buf.writeInt(id);
		buf.writeInt(stack.length());
		buf.writeCharSequence(stack, Charset.defaultCharset());
		buf.writeInt(vendorName.length());
		buf.writeCharSequence(vendorName, Charset.defaultCharset());
		buf.writeInt(buyerName.length());
		buf.writeCharSequence(buyerName, Charset.defaultCharset());
		buf.writeInt(vendor.toString().length());
		buf.writeCharSequence(vendor.toString(), Charset.defaultCharset());
		buf.writeInt(buyer.toString().length());
		buf.writeCharSequence(buyer.toString(), Charset.defaultCharset());
		buf.writeDouble(price);
		buf.writeBoolean(giveItem);
		buf.writeBoolean(openTransaction);
		buf.writeInt(stock);
		buf.writeLong(datePosted);
		buf.writeLong(dateClosed);
		return buf;
	}

	@Override
	public void readBytes(ByteBuf buf) {
		id = buf.readInt();
		int len = buf.readInt();
		stack = buf.readCharSequence(len, Charset.defaultCharset()).toString();
		len = buf.readInt();
		vendorName = buf.readCharSequence(len, Charset.defaultCharset()).toString();
		len = buf.readInt();
		buyerName = buf.readCharSequence(len, Charset.defaultCharset()).toString();
		len = buf.readInt();
		vendor = UUID.fromString(buf.readCharSequence(len, Charset.defaultCharset()).toString());
		len = buf.readInt();
		buyer = UUID.fromString(buf.readCharSequence(len, Charset.defaultCharset()).toString());
		price = buf.readDouble();
		giveItem = buf.readBoolean();
		openTransaction = buf.readBoolean();
		stock = buf.readInt();
		datePosted = buf.readLong();
		dateClosed = buf.readLong();
	}
	@Override
	public int getID() {return id;}
	@Override
	public UUID getVendorID() {return vendor;}
	@Override
	public double getPrice() {return price;}
	@Override
	public int getStock() {return stock;}
	@Override
	public boolean getGiveItem() {return giveItem;}

}
