package dicemc.gnclib.trade.entries;

import java.nio.charset.Charset;
import java.util.UUID;

import dicemc.gnclib.util.IBufferable;
import io.netty.buffer.ByteBuf;

public class EntryStorage implements IBufferable{
	private int id;
	public UUID owner;
	public String stack;
	public int count;
	
	public EntryStorage(int id, UUID owner, String itemStack, int count) {
		this.id = id;
		this.owner = owner;
		this.stack = itemStack;
		this.count = count;
	}
	public EntryStorage(UUID owner, String itemStack, int count) {
		this(-1, owner, itemStack, count);
	}
	
	@Override
	public ByteBuf writeBytes(ByteBuf buf) {
		buf.writeInt(id);
		buf.writeInt(count);
		buf.writeInt(owner.toString().length());
		buf.writeCharSequence(owner.toString(), Charset.defaultCharset());
		buf.writeInt(stack.length());
		buf.writeCharSequence(stack, Charset.defaultCharset());
		return buf;
	}

	@Override
	public void readBytes(ByteBuf buf) {
		id = buf.readInt();
		count = buf.readInt();
		int len = buf.readInt();
		owner = UUID.fromString(buf.readCharSequence(len, Charset.defaultCharset()).toString());
		len = buf.readInt();
		stack = buf.readCharSequence(len, Charset.defaultCharset()).toString();
	}
	
	public int getID() {return id;}
	
}
