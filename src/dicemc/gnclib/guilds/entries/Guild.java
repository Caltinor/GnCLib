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
	public int id;
	public UUID guildID;
	public String name;
	public boolean open;
	public boolean isAdmin;
	public double tax;
	private int tpX, tpY, tpZ, marketSize;


	public Guild(int id, String name, UUID guildID, boolean isOpen, double tax, boolean isAdmin) {
		this.id = id;
		this.guildID = guildID;
		this.isAdmin = isAdmin;
		this.name = name;
		this.open = isOpen;
		this.tax = tax;
	}
	public Guild(String name, UUID guildID, boolean isAdmin) {
		this(0, name, guildID, false, 0.0, isAdmin);
	}
	
	public static Guild getDefault() {
		return new Guild(-1, "No Guild", ComVars.NIL, false, 0.0, true);
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
}
