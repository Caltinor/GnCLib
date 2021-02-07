package dicemc.gnclib.trade.entries;

import java.nio.charset.Charset;
import java.util.UUID;

import dicemc.gnclib.util.ComVars;
import dicemc.gnclib.util.IBufferable;
import io.netty.buffer.ByteBuf;

public class EntryTransactor implements IBufferable{
	public enum Type {NONE, SERVER, PLAYER, GUILD}
	public int id;
	public Type type;
	public UUID refID;
	public String name;
	
	public EntryTransactor(int id, Type type, UUID refID, String name) {
		this.id = id;
		this.type = type;
		this.refID = refID;
		this.name = name;
	}
	public EntryTransactor(Type type, UUID refID, String name) {this(-1, type, refID, name);}
	public EntryTransactor() {this(-1, Type.NONE, ComVars.NIL, "");}
	
	@Override
	public ByteBuf writeBytes(ByteBuf buf) {
		buf.writeInt(id);
		buf.writeInt(type.ordinal());
		buf.writeInt(refID.toString().length());
		buf.writeCharSequence(refID.toString(), Charset.defaultCharset());
		buf.writeInt(name.length());
		buf.writeCharSequence(name, Charset.defaultCharset());
		return buf;
	}
	@Override
	public void readBytes(ByteBuf buf) {
		id = buf.readInt();
		type = Type.values()[buf.readInt()];
		int len = buf.readInt();
		refID = UUID.fromString(buf.readCharSequence(len, Charset.defaultCharset()).toString());
		len = buf.readInt();
		name = buf.readCharSequence(len, Charset.defaultCharset()).toString();
	}
}
