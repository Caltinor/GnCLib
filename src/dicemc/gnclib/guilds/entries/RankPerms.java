package dicemc.gnclib.guilds.entries;

import java.util.UUID;

import dicemc.gnclib.guilds.LogicGuilds.PermKey;

public class RankPerms {
	int id;
	UUID guildID, owner;
	PermKey key;
	boolean isPlayer;
	
	public RankPerms(int id, UUID guildID, UUID owner, PermKey key, boolean isPlayer) {
		this.guildID = guildID;
		this.owner = owner;
		this.key = key;
		this.isPlayer = isPlayer;
	}
}
