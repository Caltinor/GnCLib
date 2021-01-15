package dicemc.gnclib.trade.entries;

import java.nio.charset.Charset;
import java.util.UUID;

import dicemc.gnclib.configs.ConfigCore;
import dicemc.gnclib.util.ComVars;
import dicemc.gnclib.util.IBufferable;
import io.netty.buffer.ByteBuf;

public class EntryAuction implements IBufferable, IMarketEntry{
	private int id;
	public String stack, vendorName, buyerName;
	public UUID vendor, buyer;
	public long bidEnd, datePosted, dateClosed;
	public double price;
	public boolean openTransaction;
	
	public EntryAuction(int id, String stack, String vendorName, String buyerName, UUID vendor, UUID buyer, long bidEnd, long datePosted, long dateClosed, double price, boolean open) {
		this.id = id;
		this.stack = stack;
		this.vendorName = vendorName;
		this.buyerName = buyerName;
		this.vendor = vendor;
		this.buyer = buyer;
		this.bidEnd = bidEnd;
		this.datePosted = datePosted;
		this.dateClosed = dateClosed;
		this.price = price;
		this.openTransaction = open;
	}
	public EntryAuction(UUID vendor, String vendorName, String itemStack, double price) {
		this(0, itemStack, vendorName, null, vendor, null, System.currentTimeMillis()+ConfigCore.AUCTION_OPEN_DURATION, System.currentTimeMillis(), 0L, price, true);
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
		buf.writeLong(bidEnd);
		buf.writeLong(datePosted);
		buf.writeLong(dateClosed);
		buf.writeDouble(price);
		buf.writeBoolean(openTransaction);
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
		bidEnd = buf.readLong();
		datePosted = buf.readLong();
		dateClosed = buf.readLong();
		price = buf.readDouble();
		openTransaction = buf.readBoolean();
	}
	
	@Override
	public int getID() {return id;}
	@Override
	public UUID getVendorID() {return vendor;}
	@Override
	public double getPrice() {return price;}
	@Override
	public int getStock() {return 1;}
	@Override
	public boolean getGiveItem() {return true;}
	@Override
	public String getVendorName() {return vendorName;}
	@Override
	public String getStack() {return stack;}
	@Override
	public boolean getActive() {return openTransaction;}
	@Override
	public UUID getBuyerID() {return buyer;}
	@Override
	public String getBuyerName() {return buyerName;}
	@Override
	public UUID getLocality() {return ComVars.NIL;}
	@Override
	public long getBidEnd() {return bidEnd;}
	@Override
	public long getDTGPlaced() {return datePosted;}
	@Override
	public long getDTGClosed() {return dateClosed;}

}
