package dicemc.gnclib.trade.entries;

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void readBytes(ByteBuf buf) {
		// TODO Auto-generated method stub
		
	}
	
	public int getID() {return id;}
	
}
