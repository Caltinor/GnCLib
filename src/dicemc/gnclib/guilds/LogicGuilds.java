package dicemc.gnclib.guilds;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import dicemc.gnclib.configs.ConfigCore;
import dicemc.gnclib.guilds.dbref.H2Impl;
import dicemc.gnclib.guilds.dbref.IDBImplGuild;
import dicemc.gnclib.guilds.entries.Guild;
import dicemc.gnclib.guilds.entries.Rank;
import dicemc.gnclib.money.LogicMoney;
import dicemc.gnclib.money.LogicMoney.AccountType;
import dicemc.gnclib.util.ComVars;
import dicemc.gnclib.util.TranslatableResult;

public class LogicGuilds {
	private static IDBImplGuild service;
	public static enum GuildResult {SUCCESS, FAIL}
	public static enum GuildUpdates {NAME, OPEN, TAX, PERMLVLS, PERMS, MEMBERS, PERMLVL_ADD}
	public static enum PermKey {
    	CLAIM_LAND,			//can claim land connected to the core
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
	private static Map<UUID, Guild> GUILDS = new HashMap<UUID, Guild>();
	/* this map and any subsequent maps should be populated
	 * and saved as they are needed.  the below methods should
	 * first check if the data exists in the map before querying
	 * the database.  this should limit queries and maintain
	 * adequate performance.
	 */
	
	public static void init(String worldName) {
		service = setService(worldName);
	}
	
	private static IDBImplGuild setService(String worldName) {
		switch (ConfigCore.DBService.getFromString()) {
		case H2: {
			return new H2Impl(worldName);
		}
		case MY_SQL: {
			break;
		}
		default:
		}
		return new H2Impl(worldName);
	}
	
	public static  Map<UUID, Guild> getGuilds() {return GUILDS;}

	public static Guild getGuildByID(UUID guild) {
		return getGuilds().get(guild);
	}
	
    public static Guild getGuildByName(String guildName) {
		for (Map.Entry<UUID, Guild> entry : getGuilds().entrySet()) {
			if (entry.getValue().name.equalsIgnoreCase(guildName)) {return entry.getValue();}
		}
    	return null;
    }
    
    public static Guild getGuildByMember(UUID playerID) {
		for (Map.Entry<UUID, Guild> entry : getGuilds().entrySet()) {
			//TODO if (!entry.getValue().members.getOrDefault(playerID, ComVars.INV).equals(ComVars.INV)) {return entry.getValue();}
		}
    	return null;
    }
    
    public static TranslatableResult<GuildResult> setGuild(Guild guild, List<GuildUpdates> changes) {
    	Guild ogg = getGuilds().get(guild.guildID);
    	for (int i = 0; i < changes.size(); i++) {
    		switch (changes.get(i)) {
    		case NAME: { 
    			if (getGuildByName(guild.name)!= null) {
    				return new TranslatableResult<GuildResult>(GuildResult.SUCCESS, "lib.guild.create.failure");
    			}
    			if (LogicMoney.getBalance(guild.guildID, AccountType.GUILD.rl) >= ConfigCore.GUILD_NAME_CHANGE_COST) {
    				LogicMoney.changeBalance(guild.guildID, AccountType.GUILD.rl, -(ConfigCore.GUILD_NAME_CHANGE_COST));
    				ogg.name = guild.name;	
        			break; 
    			}    			
    		}
    		case OPEN :{ ogg.open = guild.open; break; }
    		case TAX: { ogg.tax = guild.tax; break;}
    		case PERMLVLS: { 
    			//TODO ogg.ranks = guild.ranks; break;
    		}
    		case PERMS: { 
    			//TODO ogg.permissions = guild.permissions; break;
    			}
    		case MEMBERS: { 
    			//TODO ogg.members = guild.members; break;
    			}
    		case PERMLVL_ADD: {
    			if (LogicMoney.getBalance(guild.guildID, AccountType.GUILD.rl) >= ConfigCore.GUILD_RANK_ADD_COST) {
    				LogicMoney.changeBalance(guild.guildID, AccountType.GUILD.rl, -(ConfigCore.GUILD_RANK_ADD_COST));
    				addRank(guild.guildID, "New Rank");
    			}
    			break;}
    		default:
    		}
    	}
    	getGuilds().put(ogg.guildID, ogg);
    	return new TranslatableResult<GuildResult>(GuildResult.SUCCESS, "lib.guild.update");
    }

	public static TranslatableResult<GuildResult> createGuild(String name, boolean isAdmin) {
		UUID id = ComVars.unrepeatedUUIDs(getGuilds());
    	getGuilds().put(id, new Guild(name, id, isAdmin));
    	LogicMoney.getBalance(id, ComVars.MOD_ID+":guild");
    	for (PermKey perms : PermKey.values()) {addPermission(id, perms, 0);}
    	return new TranslatableResult<GuildResult>(GuildResult.SUCCESS, "lib.guild.create.success");
	}

	
	public static TranslatableResult<GuildResult> addMember(UUID guildID, UUID playerID, UUID rank) {return updateMember(guildID, playerID, rank);}
	
	public static TranslatableResult<GuildResult> updateMember(UUID guildID, UUID playerID, UUID rank) {
    	/*TODO if (!getGuilds().get(guildID).ranks.containsKey(rank)) return new TranslatableResult<GuildResult>(GuildResult.FAIL, "lib.guild.member.update.failure");
    	else {
    		getGuilds().get(guildID).members.put(playerID, rank);
    	}*/
    	return new TranslatableResult<GuildResult>(GuildResult.SUCCESS, "lib.guild.member.update.success");
    }
	
	public static TranslatableResult<GuildResult> removeMember(UUID guildID, UUID playerID) {
    	/*TODO if (getGuilds().get(guildID).members.remove(playerID) != null) return new TranslatableResult<GuildResult>(GuildResult.SUCCESS, "lib.guild.member.remove.success");   	
    	else*/ return new TranslatableResult<GuildResult>(GuildResult.FAIL, "lib.guild.member.remove.failure");
    }
	
	public static Map<UUID, UUID> getMembers(UUID guildID) {
		//TODO get the thing
		return null;
	}
	
	public static TranslatableResult<GuildResult> addPermission(UUID guildID, PermKey permTag, int value) { return setPermission(guildID, permTag, value); }
    
	public static TranslatableResult<GuildResult> setPermission(UUID guildID, PermKey permTag, int value) {
    	//TODO the thing
    	return new TranslatableResult<GuildResult>(GuildResult.SUCCESS, "lib.guild.permission.set.success");
    }
	
	public static TranslatableResult<GuildResult> addRank(UUID guildID, String title) {
		Guild guild = getGuilds().get(guildID);
		//TODO Rank rank = new Rank(ComVars.unrepeatedUUIDs(guild.ranks), "title", guild.ranks.get(getBottomRank(guildID)).sequence + 1);
    	//guild.ranks.put(ComVars.unrepeatedUUIDs(guild.ranks), rank);
    	return new TranslatableResult<GuildResult>(GuildResult.SUCCESS, "lib.guild.rank.add.success");
    }
	
	public static TranslatableResult<GuildResult> setRankTitle(UUID guildID, UUID rank, String newTitle) {
    	//TODO if (!getGuilds().get(guildID).ranks.containsKey(rank)) return new TranslatableResult<GuildResult>(GuildResult.FAIL, "lib.guild.rank.set.failure");
    	// getGuilds().get(guildID).ranks.get(rank).title = newTitle;
    	return new TranslatableResult<GuildResult>(GuildResult.SUCCESS, "lib.guild.rank.set.success");
    }
	
	public static Map<UUID, Rank> getRanks(UUID guildID) {return null; //TODO getGuilds().get(guildID).ranks;
	}
	
	public static UUID getBottomRank(UUID guildID) {
		int sequence = 0;
		UUID result = null;
		/*TODO outer:
		while (sequence >= 0) {
			for (Map.Entry<UUID, Rank> map : getGuilds().get(guildID).ranks.entrySet()) {
				if (map.getValue().sequence == sequence) {
					sequence++;
					result = map.getKey();
					continue outer;
				}				
			}
			sequence = -1;
		}*/
    	return result;
    }
}
