package dicemc.gnclib.guilds.entries;

import java.nio.charset.Charset;
import java.util.UUID;

import dicemc.gnclib.trade.LogicTrade;
import dicemc.gnclib.util.Agent;
import dicemc.gnclib.util.Agent.Type;
import dicemc.gnclib.util.ComVars;
import dicemc.gnclib.util.IBufferable;
import io.netty.buffer.ByteBuf;

public class Guild implements IBufferable{
	private int id;
	public UUID guildID;
	public String name;
	public boolean open;
	public boolean isAdmin;
	public double tax;
	private int tpX, tpY, tpZ, marketSize;

	public int getID() {return id;}

	public Guild(int id, String name, UUID guildID, boolean isOpen, double tax, boolean isAdmin,
			int tpX, int tpY, int tpZ, int marketSize) {
		this.id = id;
		this.guildID = guildID;
		this.isAdmin = isAdmin;
		this.name = name;
		this.open = isOpen;
		this.tax = tax;
		this.tpX = tpX;
		this.tpY = tpY;
		this.tpZ = tpZ;
		this.marketSize = marketSize;
	}
	public Guild(String name, UUID guildID, boolean isAdmin) {
		this(-1, name, guildID, false, 0.0, isAdmin, 0, 0, 0, 0);
	}
	
	public static Guild getDefault() {
		return new Guild(-1, "No Guild", ComVars.NIL, false, 0.0, true, 0, 0, 0, 0);
	}
	
	public Agent asAgent() {
		return LogicTrade.get().getTransactor(guildID, Type.GUILD, name);
	}
	
	public void setTPLocation(int x, int y, int z) {
		tpX = x;
		tpY = y;
		tpZ = z;
	}
	public int getTPX() {return tpX;}
	public int getTPY() {return tpY;}
	public int getTPZ() {return tpZ;}
	
	public void setMarketSize(int size) {
		marketSize = size;
	}
	public void changeMarketSize(int change) {
		setMarketSize((marketSize + change) >= 0 ? marketSize + change : 0);
	}
	public int getMarketSize() {return marketSize;}
	
	@Override
	public ByteBuf writeBytes(ByteBuf buf) {
		buf.writeInt(id);
		buf.writeInt(tpX);
		buf.writeInt(tpY);
		buf.writeInt(tpZ);
		buf.writeInt(marketSize);
		buf.writeBoolean(open);
		buf.writeBoolean(isAdmin);
		buf.writeDouble(tax);
		buf.writeInt(guildID.toString().length());
		buf.writeCharSequence(guildID.toString(), Charset.defaultCharset());
		buf.writeInt(name.length());
		buf.writeCharSequence(name, Charset.defaultCharset());
		return buf;
	}
	@Override
	public void readBytes(ByteBuf buf) {
		id = buf.readInt();
		tpX = buf.readInt();
		tpY = buf.readInt();
		tpZ = buf.readInt();
		marketSize = buf.readInt();
		open = buf.readBoolean();
		isAdmin = buf.readBoolean();
		tax = buf.readDouble();
		int len = buf.readInt();
		guildID = UUID.fromString(buf.readCharSequence(len, Charset.defaultCharset()).toString());
		len = buf.readInt();
		name = buf.readCharSequence(len, Charset.defaultCharset()).toString();
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Guild)) return false;
		Guild b = (Guild) o;
		Guild a = this;
		return a.id == b.id 
				&& a.guildID.equals(b.guildID) 
				&& a.name.equals(b.name) 
				&& a.open == b.open 
				&& a.isAdmin == b.isAdmin 
				&& a.tax == b.tax 
				&& a.tpX == b.tpX
				&& a.tpY == b.tpY
				&& a.tpZ == b.tpZ
				&& a.marketSize == b.marketSize;
	}
}
