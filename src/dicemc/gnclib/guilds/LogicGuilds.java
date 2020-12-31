package dicemc.gnclib.guilds;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import dicemc.gnclib.configs.ConfigCore;
import dicemc.gnclib.guilds.dbref.H2Impl;
import dicemc.gnclib.guilds.dbref.IDBImplGuild;
import dicemc.gnclib.guilds.entries.Guild;
import dicemc.gnclib.guilds.entries.RankPerms;
import dicemc.gnclib.money.LogicMoney;
import dicemc.gnclib.money.LogicMoney.AccountType;
import dicemc.gnclib.util.ComVars;
import dicemc.gnclib.util.TranslatableResult;

public class LogicGuilds {
	private static IDBImplGuild service;
	public static enum GuildResult {SUCCESS, FAIL}
	public static enum GuildUpdates {NAME, OPEN, TAX}
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
	//GuildID, guildData
	private static Map<UUID, Guild> GUILDS = new HashMap<UUID, Guild>();
	//GuildID, playerid, memberData
	private static Map<UUID, Map<UUID, Integer>> MEMBERS = new HashMap<UUID, Map<UUID, Integer>>();
	//RankID, rankData
	private static Map<UUID, Map<Integer, String>> RANKS = new HashMap<UUID, Map<Integer, String>>();
	//GuildID<PermKey<list of permitted classifications>>
	private static Map<UUID, Map<PermKey, List<RankPerms>>> PERMS = new HashMap<UUID, Map<PermKey, List<RankPerms>>>();
	/* this map and any subsequent maps should be populated
	 * and saved as they are needed.  the below methods should
	 * first check if the data exists in the map before querying
	 * the database.  this should limit queries and maintain
	 * adequate performance.
	 */
	
	public static void init(String worldName) {
		service = setService(worldName);
		GUILDS = service.getAllGuilds();
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
	
	public static Map<UUID, Guild> getGuilds() {return GUILDS;}
	public static Map<UUID, Integer> getMembers(UUID guildID) {
		if (!MEMBERS.containsKey(guildID)) MEMBERS.put(guildID, service.getGuildMembers(guildID));
		return MEMBERS.get(guildID);
	}
	public static Map<Integer, String> getRanks(UUID guildID) {
		if (!RANKS.containsKey(guildID)) RANKS.put(guildID, service.getGuildRanks(guildID));
		return RANKS.get(guildID);
	}
	public static Map<PermKey, List<RankPerms>> getPerms(UUID guildID) {
		if (!PERMS.containsKey(guildID)) {
			PERMS.put(guildID, new HashMap<PermKey, List<RankPerms>>());
			for (PermKey key : PermKey.values()) {
				PERMS.get(guildID).put(key, service.getPermissionEntries(guildID, key));
			}
		}
		return PERMS.get(guildID);
	}

	public static Guild getGuildByID(UUID guild) {		
		return getGuilds().getOrDefault(guild, Guild.getDefault());
	}
	
    public static Guild getGuildByName(String guildName) {
		for (Map.Entry<UUID, Guild> entry : getGuilds().entrySet()) {
			if (entry.getValue().name.equalsIgnoreCase(guildName)) {return entry.getValue();}
		}
    	return Guild.getDefault();
    }
    
    public static Guild getGuildByMember(UUID playerID) {
		for (Map.Entry<UUID, Guild> guilds : getGuilds().entrySet()) {
			if (getMembers(guilds.getKey()).getOrDefault(playerID, Integer.MAX_VALUE) != Integer.MAX_VALUE) return guilds.getValue();
		}		
		return Guild.getDefault();
    }
    
    public static TranslatableResult<GuildResult> setGuild(Guild newGuildImage, List<GuildUpdates> changes) {
    	Guild ogg = getGuilds().get(newGuildImage.guildID);
    	for (int i = 0; i < changes.size(); i++) {
    		switch (changes.get(i)) {
    		case NAME: { 
    			if (LogicGuilds.getGuildByName(newGuildImage.name).id == -1) {
    				return new TranslatableResult<GuildResult>(GuildResult.SUCCESS, "lib.guild.create.failure");}
    			if (LogicMoney.getBalance(newGuildImage.guildID, AccountType.GUILD.rl) < ConfigCore.GUILD_NAME_CHANGE_COST) {
    				return new TranslatableResult<GuildResult>(GuildResult.SUCCESS, "lib.guild.update.name.falure");}
    			else {    				
    				LogicMoney.changeBalance(newGuildImage.guildID, AccountType.GUILD.rl, -(ConfigCore.GUILD_NAME_CHANGE_COST));
    				ogg.name = newGuildImage.name;	
        			break; 
    			}    			
    		}
    		case OPEN :{ ogg.open = newGuildImage.open; break; }
    		case TAX: { ogg.tax = newGuildImage.tax; break;}
    		default:
    		}
    	}
    	getGuilds().put(ogg.guildID, ogg);
    	return service.setGuild(ogg);
    }

	public static TranslatableResult<GuildResult> createGuild(String name, boolean isAdmin) {
		UUID id = ComVars.unrepeatedUUIDs(getGuilds());
		Guild newGuild = new Guild(name, id, isAdmin);
		newGuild = service.addGuild(newGuild);
    	getGuilds().put(id, newGuild);
    	LogicMoney.getBalance(id, ComVars.MOD_ID+":guild");
    	RANKS.put(id, new HashMap<Integer, String>());
    	addRank(id, "Member");
    	for (PermKey perms : PermKey.values()) {addPermission(id, perms, ComVars.NIL, 0);}
    	return new TranslatableResult<GuildResult>(GuildResult.SUCCESS, "lib.guild.create.success");
	}
	
	public static TranslatableResult<GuildResult> removeGuild(UUID guildID) {
		getGuilds().remove(guildID);
		return service.removeGuild(guildID);
	}
	
	public static TranslatableResult<GuildResult> addMember(UUID guildID, UUID playerID, int rank) {return updateMember(guildID, playerID, rank);}
	
	public static TranslatableResult<GuildResult> updateMember(UUID guildID, UUID playerID, int rank) {
    	if (!getRanks(guildID).containsKey(rank)) return new TranslatableResult<GuildResult>(GuildResult.FAIL, "lib.guild.member.update.failure");
    	else {
    		getMembers(guildID).put(playerID, rank);
    	}
    	return service.setMember(guildID, playerID, rank);
    }
	
	public static TranslatableResult<GuildResult> removeMember(UUID guildID, UUID playerID) {
    	if (getMembers(guildID).remove(playerID) != null) return new TranslatableResult<GuildResult>(GuildResult.SUCCESS, "lib.guild.member.remove.success");   	
    	else return new TranslatableResult<GuildResult>(GuildResult.FAIL, "lib.guild.member.remove.failure");
    }	
	
	public static TranslatableResult<GuildResult> addPermission(UUID guildID, PermKey permTag, UUID owner, int rank) { 
		return setPermission(guildID, permTag, owner, rank); }
    
	public static TranslatableResult<GuildResult> setPermission(UUID guildID, PermKey permTag, UUID owner, int rank) {
    	RankPerms perm = new RankPerms(guildID, permTag, owner, rank);
    	List<RankPerms> list = getPerms(guildID).get(permTag);
    	for (int i = 0; i < list.size(); i++) {
    		if (list.get(i).matches(perm)) return new TranslatableResult<GuildResult>(GuildResult.FAIL, "lib.guild.permission.set.failure");
    	}
    	list.add(perm);
    	getPerms(guildID).put(permTag, list);
    	return service.setPermission(perm);
    }
	
	public static TranslatableResult<GuildResult> removePermission(UUID guildID, PermKey permTag, UUID owner, int rank) {
		TranslatableResult<GuildResult> result = new TranslatableResult<GuildResult>(GuildResult.FAIL, "lib.guild.permission.remove.failure");
		RankPerms perm = new RankPerms(guildID, permTag, owner, rank);
		List<RankPerms> list = getPerms(guildID).get(permTag);
		for (int i = 0; i < list.size(); i++) {
    		if (list.get(i).matches(perm)) {
    			getPerms(guildID).get(permTag).remove(i); 
    			result = service.removePermission(perm);
    			break;
    		} 
    	}
		return result;
	}
	
	public static boolean hasPermission(UUID guildID, PermKey key, UUID player) {
		int rank = getMembers(guildID).get(player);
		List<RankPerms> list = getPerms(guildID).get(key);
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).player.equals(player) || list.get(i).rank == rank) return true;
		}
		return false;
	}
	
	public static TranslatableResult<GuildResult> addRank(UUID guildID, String title) {
		int rank = getBottomRank(guildID)+1;
    	getRanks(guildID).put(rank, title);
    	service.addRank(guildID, rank, title);
    	return new TranslatableResult<GuildResult>(GuildResult.SUCCESS, "lib.guild.rank.add.success");
    }
	
	public static TranslatableResult<GuildResult> setRankTitle(UUID guildID, int rank, String newTitle) {
		if (!getGuilds().containsKey(guildID)) {return new TranslatableResult<GuildResult>(GuildResult.SUCCESS, "lib.guild.rank.set.failure");}
		if (!getRanks(guildID).containsKey(rank)) {return new TranslatableResult<GuildResult>(GuildResult.SUCCESS, "lib.guild.rank.set.failure");}
		if (LogicMoney.getBalance(guildID, LogicMoney.AccountType.GUILD.rl) < ConfigCore.GUILD_NAME_CHANGE_COST) {
			return new TranslatableResult<GuildResult>(GuildResult.SUCCESS, "lib.guild.rank.set.failure");}
		else {
			LogicMoney.changeBalance(guildID, LogicMoney.AccountType.GUILD.rl, -(ConfigCore.GUILD_NAME_CHANGE_COST));
		}
		getRanks(guildID).put(rank, newTitle);
		service.setRankTitle(guildID, rank, newTitle);
    	return new TranslatableResult<GuildResult>(GuildResult.SUCCESS, "lib.guild.rank.set.success");
    }
	
	public static int getBottomRank(UUID guildID) {
		int sequence = 0;
		while (sequence >= 0) {
			if (!getRanks(guildID).containsKey(sequence)) return (sequence - 1);
			sequence++;
		}
    	return sequence;
    }
}
