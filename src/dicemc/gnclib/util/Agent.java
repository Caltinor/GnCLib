package dicemc.gnclib.util;

import java.nio.charset.Charset;
import java.util.UUID;

import io.netty.buffer.ByteBuf;

public class Agent implements IBufferable{
	public enum Type {NONE, SERVER, PLAYER, GUILD}
	public int id;
	public Type type;
	public UUID refID;
	public String name;
	
	public Agent(int id, Type type, UUID refID, String name) {
		this.id = id;
		this.type = type;
		this.refID = refID;
		this.name = name;
	}
	public Agent(Type type, UUID refID, String name) {this(-1, type, refID, name);}
	public Agent() {this(-1, Type.NONE, ComVars.NIL, "");}
	
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
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Agent) {
			Agent that = (Agent) o;
			return this.type == that.type && this.refID.equals(that.refID);
		}
		return false; 
	}
}
