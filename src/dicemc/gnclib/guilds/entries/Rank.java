package dicemc.gnclib.guilds.entries;

import java.util.UUID;

public class Rank {
	public int id;
	public UUID rankID, guildID;
	public String title;
	public int sequence;
	
	public Rank(UUID guildID, String title) {
		this(0, guildID, UUID.randomUUID(), title, 0);
	}
	
	public Rank(UUID guildID, String title, int sequence) {
		this(0, guildID, UUID.randomUUID(), title, sequence);
	}
	
	public Rank(int id, UUID guildID, UUID rankID, String title, int sequence) {
		this.id = id;
		this.guildID = guildID;
		this.rankID = rankID;
		this.title = title;
		this.sequence = sequence;
	}
}
