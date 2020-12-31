package dicemc.gnclib.guilds.entries;

import java.util.UUID;

import dicemc.gnclib.util.ComVars;

public class Guild {
	public int id;
	public UUID guildID;
	public String name;
	public boolean open;
	public boolean isAdmin;
	public double tax;

	public Guild(int id, String name, UUID guildID, boolean isOpen, double tax, boolean isAdmin) {
		this.id = id;
		this.guildID = guildID;
		this.isAdmin = isAdmin;
		this.name = name;
		this.open = isOpen;
		this.tax = tax;
	}
	public Guild(String name, UUID guildID, boolean isAdmin) {
		this(0, name, guildID, false, 0.0, isAdmin);
	}
	
	public static Guild getDefault() {
		return new Guild(-1, "No Guild", ComVars.NIL, false, 0.0, true);
	}
}