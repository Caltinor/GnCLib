package dicemc.gnclib.trade.entries;

import java.nio.charset.Charset;

import dicemc.gnclib.util.Agent;
import dicemc.gnclib.util.IBufferable;
import io.netty.buffer.ByteBuf;

public class EntryStorage implements IBufferable{
	private int id;
	public Agent owner;
	public String stack;
	public int count;
	
	public EntryStorage(int id, Agent owner, String itemStack, int count) {
		this.id = id;
		this.owner = owner;
		this.stack = itemStack;
		this.count = count;
	}
	public EntryStorage(Agent owner, String itemStack, int count) {
		this(-1, owner, itemStack, count);
	}
	
	@Override
	public ByteBuf writeBytes(ByteBuf buf) {
		buf.writeInt(id);
		buf.writeInt(count);
		buf.writeInt(stack.length());
		buf.writeCharSequence(stack, Charset.defaultCharset());
		buf.writeBytes(owner.writeBytes(buf));
		return buf;
	}

	@Override
	public void readBytes(ByteBuf buf) {
		id = buf.readInt();
		count = buf.readInt();
		int len = buf.readInt();
		stack = buf.readCharSequence(len, Charset.defaultCharset()).toString();
		owner = new Agent();
		owner.readBytes(buf);
	}
	
	public int getID() {return id;}
	
}
