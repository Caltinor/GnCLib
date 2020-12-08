package dicemc.gnclib.guilds;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Guild {
	public UUID guildID;
	public String name;
	public boolean open;
	public boolean isAdmin;
	public double tax;
	public Map<Integer, String> permLevels = new HashMap<Integer, String>();
	public Map<permKey, Integer> permissions = new HashMap<permKey, Integer>();
	public Map<UUID, Integer> members = new HashMap<UUID, Integer>();
	
	public static enum permKey {
    	CORE_CLAIM,			//can claim land connected to the core
    	OUTPOST_CLAIM,		//can claim land connected to an outpost (increases taxation) 
    	OUTPOST_CREATE,		//can create new outposts
    	CLAIM_ABANDON,		//can abandon claims
    	CLAIM_SELL,			//can sell claims
    	SUBLET_MANAGE,		//can change sublet settings
    	CHANGE_NAME,
    	SET_TAX,
    	SET_OPEN_TO_JOIN,
    	BUY_NEW_RANK,
    	RANK_TITLE_CHANGE,
    	ACCOUNT_WITHDRAW,
    	MANAGE_PERMISSIONS,
    	SET_MEMBER_RANKS,
    	INVITE_MEMBERS,
    	KICK_MEMBER
    }

	public Guild(String name, UUID guildID, boolean isAdmin) {
		this.guildID = guildID;
		this.isAdmin = isAdmin;
		this.name = name;
		open = true;
		tax = 0.0;
		permLevels.put(0, "Member");
	}
}
