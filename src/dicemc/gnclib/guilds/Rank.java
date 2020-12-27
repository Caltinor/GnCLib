package dicemc.gnclib.guilds;

import java.util.UUID;

public class Rank {
	public UUID id;
	public String title;
	public int sequence;
	
	public Rank(String title) {
		this(UUID.randomUUID(), title, 0);
	}
	
	public Rank(String title, int sequence) {
		this(UUID.randomUUID(), title, sequence);
	}
	
	public Rank(UUID id, String title, int sequence) {
		this.id = id;
		this.title = title;
		this.sequence = sequence;
	}
}
