package dicemc.gnclib.guilds.entries;

import java.util.UUID;

import dicemc.gnclib.guilds.LogicGuilds.PermKey;
import dicemc.gnclib.util.ComVars;

public class RankPerms {
	public int id;
	public UUID guildID, player;
	public PermKey key;
	public int rank;
	
	public RankPerms(int id, UUID guildID, PermKey key, UUID player , int rank) {
		this.id = id;
		this.guildID = guildID;
		this.player = player;
		this.key = key;
		this.rank = rank;
	}
	public RankPerms(UUID guildID, PermKey key, UUID player, int rank) {
		this(0, guildID, key, player, rank);
	}	
	public RankPerms(UUID guildID, PermKey key, UUID player) {
		this(0, guildID, key, player, -1);
	}
	public RankPerms(UUID guildID, PermKey key, int rank) {
		this(0, guildID, key, ComVars.NIL, rank);
	}
	
	public boolean matches(RankPerms other) {
		return guildID == other.guildID && player == other.player && key == other.key && rank == other.rank;
	}
}
