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
import dicemc.gnclib.util.Duo;
import dicemc.gnclib.util.TranslatableResult;

public class LogicGuilds {
	private static IDBImplGuild service;
	public static enum GuildResult {SUCCESS, FAIL}
	public static enum GuildUpdates {NAME, OPEN, TAX}
	public static enum PermKey {
    	CLAIM_LAND(ComVars.MOD_ID+":claim_land"),			//can claim land connected to the core
    	OUTPOST_CREATE(ComVars.MOD_ID+":outpost_create"),		//can create new outposts
    	CLAIM_ABANDON(ComVars.MOD_ID+":claim_abandon"),		//can abandon claims
    	CLAIM_SELL(ComVars.MOD_ID+":claim_sell"),			//can sell claims
    	SUBLET_MANAGE(ComVars.MOD_ID+":sublet_manage"),		//can change sublet settings
    	CHANGE_NAME(ComVars.MOD_ID+":change_name"),
    	SET_TAX(ComVars.MOD_ID+":set_tax"),
    	SET_OPEN_TO_JOIN(ComVars.MOD_ID+":set_open_to_join"),
    	BUY_NEW_RANK(ComVars.MOD_ID+":buy_new_rank"),
    	RANK_TITLE_CHANGE(ComVars.MOD_ID+":rank_title_change"),
    	ACCOUNT_WITHDRAW(ComVars.MOD_ID+":account_withdraw"),
    	MANAGE_PERMISSIONS(ComVars.MOD_ID+":manage_permissions"),
    	SET_MEMBER_RANKS(ComVars.MOD_ID+":set_member_ranks"),
    	INVITE_MEMBERS(ComVars.MOD_ID+":invite_members"),
    	KICK_MEMBER(ComVars.MOD_ID+":kick_member");
    	public final String rl;
		PermKey(String resourceLocation) {rl = resourceLocation;}
    }
	
	/* this map and any subsequent maps should be populated
	 * and saved as they are needed.  the below methods should
	 * first check if the data exists in the map before querying
	 * the database.  this should limit queries and maintain
	 * adequate performance.
	 */
	//GuildID, guildData
	private static Map<UUID, Guild> GUILDS = new HashMap<UUID, Guild>();
	//GuildID, playerid, memberData
	private static Map<UUID, Map<UUID, Integer>> MEMBERS = new HashMap<UUID, Map<UUID, Integer>>();
	//GuildID, ranksequence, rankTitle
	private static Map<UUID, Map<Integer, String>> RANKS = new HashMap<UUID, Map<Integer, String>>();
	//GuildID<PermKey<list of permitted classifications>>
	private static Map<UUID, Map<String, List<RankPerms>>> PERMS = new HashMap<UUID, Map<String, List<RankPerms>>>();
	
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
	public static Map<String, List<RankPerms>> getPerms(UUID guildID) {
		if (!PERMS.containsKey(guildID)) {
			PERMS.put(guildID, new HashMap<String, List<RankPerms>>());
			for (PermKey key : PermKey.values()) {
				PERMS.get(guildID).put(key.rl, service.getPermissionEntries(guildID, key.rl));
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
    	for (PermKey perms : PermKey.values()) {addPermission(id, perms.rl, ComVars.NIL, 0);}
    	return new TranslatableResult<GuildResult>(GuildResult.SUCCESS, "lib.guild.create.success");
	}
	
	public static TranslatableResult<GuildResult> removeGuild(UUID guildID) {
		getGuilds().remove(guildID);
		MEMBERS.remove(guildID);
		RANKS.remove(guildID);
		PERMS.remove(guildID);
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
	
	public static TranslatableResult<GuildResult> addPermission(UUID guildID, String permTag, UUID owner, int rank) { 
		return setPermission(guildID, permTag, owner, rank); }
    
	public static TranslatableResult<GuildResult> setPermission(UUID guildID, String permTag, UUID owner, int rank) {
    	RankPerms perm = new RankPerms(guildID, permTag, owner, rank);
    	List<RankPerms> list = getPerms(guildID).get(permTag);
    	for (int i = 0; i < list.size(); i++) {
    		if (list.get(i).matches(perm)) return new TranslatableResult<GuildResult>(GuildResult.FAIL, "lib.guild.permission.set.failure");
    	}
    	list.add(perm);
    	getPerms(guildID).put(permTag, list);
    	return service.setPermission(perm);
    }
	
	public static TranslatableResult<GuildResult> removePermission(UUID guildID, String permTag, UUID owner, int rank) {
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
	
	public static boolean hasPermission(UUID guildID, String key, UUID player) {
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
	
	public static int getMemberCount(UUID guildID) {
		int count = 0;
		for (Map.Entry<UUID, Integer> mbrs : MEMBERS.get(guildID).entrySet()) {
			if (mbrs.getValue() >= 0) count++;
		}
		return count;
	}
	
	public static void applyTaxes(Map<UUID, Duo<Integer, Double>> chunkCounts) {
		//TODO change return type to allow for a stream of messages that can be sent to appropriate entities
		for (Map.Entry<UUID, Map<UUID, Integer>> mbrs : MEMBERS.entrySet()) {
			Guild guild = GUILDS.get(mbrs.getKey());
			if (guild.tax == 0) continue;
			boolean isDividends = guild.tax < 0;			
			int mbrCount = mbrs.getValue().size();
			double taxedSrc = isDividends ? LogicMoney.getBalance(mbrs.getKey(), LogicMoney.AccountType.GUILD.rl) : 0d;
			double tax = taxedSrc * guild.tax;
			for (Map.Entry<UUID, Integer> m : mbrs.getValue().entrySet()) {
				if (!isDividends) {taxedSrc = LogicMoney.getBalance(m.getKey(), LogicMoney.AccountType.PLAYER.rl);}
				tax = taxedSrc * guild.tax;
				if (isDividends) {LogicMoney.transferFunds(guild.guildID, LogicMoney.AccountType.GUILD.rl, 
						m.getKey(), LogicMoney.AccountType.PLAYER.rl, (tax / mbrCount));
				}
				else {LogicMoney.transferFunds(m.getKey(), LogicMoney.AccountType.PLAYER.rl, 
						guild.guildID, LogicMoney.AccountType.GUILD.rl, tax);
				}
			}
		}
		for (Map.Entry<UUID, Guild> guilds : GUILDS.entrySet()) {
			if (guilds.getValue().isAdmin) continue;
			double gbal = LogicMoney.getBalance(guilds.getKey(), LogicMoney.AccountType.GUILD.rl);
			double debt = LogicMoney.getBalance(guilds.getKey(), LogicMoney.AccountType.DEBT.rl);
			int chunkCount = chunkCounts.get(guilds.getKey()).getL();
			double worth = chunkCounts.get(guilds.getKey()).getR();
			int memberCount = getMemberCount(guilds.getKey());
			if (debt > 0 && gbal > debt) {
				LogicMoney.changeBalance(guilds.getKey(), LogicMoney.AccountType.GUILD.rl, -debt);
				LogicMoney.setBalance(guilds.getKey(), LogicMoney.AccountType.DEBT.rl, 0D);
				gbal -= debt;
				//INSERT notification to players that debt has been paid
			}
			else if (gbal < debt && debt > 0D && gbal > 0) {
				LogicMoney.setBalance(guilds.getKey(), LogicMoney.AccountType.GUILD.rl, 0D);
				LogicMoney.setBalance(guilds.getKey(), LogicMoney.AccountType.DEBT.rl, -gbal);
				debt -= gbal;
				gbal = 0d;
				//INSERT notification to players that x amount of debt has been reduced
			}
			double proportion = ((memberCount * ConfigCore.CHUNKS_PER_MEMBER) >= chunkCount ? 0 : 1d - ((double)(memberCount * ConfigCore.CHUNKS_PER_MEMBER) / (double)chunkCount));
			if (proportion == 0) continue;
			double taxableWorth = worth * proportion;			
			if (gbal < (taxableWorth * ConfigCore.GLOBAL_TAX_RATE)) {
				LogicMoney.setBalance(guilds.getKey(), LogicMoney.AccountType.GUILD.rl, 0D);
				taxableWorth -= gbal;
				LogicMoney.changeBalance(guilds.getKey(), LogicMoney.AccountType.DEBT.rl, taxableWorth);
				debt += taxableWorth;
				//INSERT notification that debt has been incurred
			}
			else {
				LogicMoney.changeBalance(guilds.getKey(), LogicMoney.AccountType.GUILD.rl, -(taxableWorth * ConfigCore.GLOBAL_TAX_RATE));
				//INSERT message that taxes at X rate have been paid
			}
			if (debt > (worth/2)) {
				removeGuild(guilds.getKey());
				//INSERT message that X guild has gone bankrupt.
			}
			//INSERT tax execution complete notice.
		}
	}
}
