package dicemc.gnclib.guilds.entries;

import java.util.UUID;

public class Guild {
	public UUID guildID;
	public String name;
	public boolean open;
	public boolean isAdmin;
	public double tax;

	public Guild(String name, UUID guildID, boolean isAdmin) {
		this.guildID = guildID;
		this.isAdmin = isAdmin;
		this.name = name;
		open = true;
		tax = 0.0;
	}
}
