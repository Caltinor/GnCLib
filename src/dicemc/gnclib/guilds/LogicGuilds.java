package dicemc.gnclib.guilds;

import java.util.ArrayList;
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
import dicemc.gnclib.util.Agent;
import dicemc.gnclib.util.ComVars;
import dicemc.gnclib.util.Duo;
import dicemc.gnclib.util.ResultType;
import dicemc.gnclib.util.TranslatableResult;
import dicemc.gnclib.util.Agent.Type;

public class LogicGuilds {
	private static IDBImplGuild service;
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
    	KICK_MEMBER(ComVars.MOD_ID+":kick_member"),
    	SET_SHOP_TP(ComVars.MOD_ID+":set_shop_tp");
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
	
	public static void printTables() {((H2Impl)service).printAllTables();}
	
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
			if (getMembers(guilds.getKey()).getOrDefault(playerID, -1) >= 0) return guilds.getValue();
		}		
		return Guild.getDefault();
    }
    
    public static TranslatableResult<ResultType> setGuildName(UUID guildID, Agent exec, String newName) {
    	if (!hasPermission(guildID, PermKey.CHANGE_NAME.rl, exec.refID))
    		return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.guild.update.failure.permission");
    	if (LogicMoney.getBalance(guildID, LogicMoney.agentType(Type.GUILD)) < ConfigCore.GUILD_NAME_CHANGE_COST)
    		return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.guild.update.failure.funds");
    	LogicMoney.changeBalance(guildID, LogicMoney.agentType(Type.GUILD), -ConfigCore.GUILD_NAME_CHANGE_COST);
    	Guild ogg = getGuilds().get(guildID);
    	ogg.name = newName;
    	getGuilds().put(ogg.guildID, ogg);
    	return service.setGuild(ogg);
    }
    
    public static TranslatableResult<ResultType> setGuildOpen(UUID guildID, Agent exec, boolean openSetting) {
    	if (!hasPermission(guildID, PermKey.SET_OPEN_TO_JOIN.rl, exec.refID))
    		return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.guild.update.failure.permission");
    	Guild ogg = getGuilds().get(guildID);
    	ogg.open = openSetting;
    	getGuilds().put(ogg.guildID, ogg);
    	return service.setGuild(ogg);
    }
    
    public static TranslatableResult<ResultType> setGuildTax(UUID guildID, Agent exec, double newTax) {
    	if (!hasPermission(guildID, PermKey.SET_TAX.rl, exec.refID))
    		return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.guild.update.failure.permission");
    	Guild ogg = getGuilds().get(guildID);
    	ogg.tax = newTax;
    	getGuilds().put(ogg.guildID, ogg);
    	return service.setGuild(ogg);
    }
    
    public static TranslatableResult<ResultType> setGuildShopLoc(UUID guildID, Agent exec, int x, int y, int z) {
    	if (!hasPermission(guildID, PermKey.SET_SHOP_TP.rl, exec.refID))
    		return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.guild.update.failure.permission");
    	Guild ogg = getGuilds().get(guildID);
    	ogg.setTPLocation(x, y, z);
    	getGuilds().put(ogg.guildID, ogg);
    	return service.setGuild(ogg);
    }

    public static TranslatableResult<ResultType> playerCreateGuild(Agent agent, String name, boolean isAdmin) {
    	if (!isAdmin && LogicMoney.getBalance(agent.refID, LogicMoney.agentType(agent.type)) < ConfigCore.GUILD_CREATE_COST)
			return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.guild.create.failure.funds");
    	LogicMoney.changeBalance(agent.refID, LogicMoney.agentType(agent.type), -ConfigCore.GUILD_CREATE_COST);
    	return createGuild(agent, name, isAdmin);
    }
    
    public static TranslatableResult<ResultType> createGuild(Agent agent, String name, boolean isAdmin) {	
    	if (!getGuildByMember(agent.refID).equals(Guild.getDefault()))
    		return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.guild.create.failure.inguild");
		UUID id = ComVars.unrepeatedUUIDs(getGuilds());
		Guild newGuild = new Guild(name, id, isAdmin);
		newGuild.isAdmin = isAdmin;
		newGuild = service.addGuild(newGuild);
    	getGuilds().put(id, newGuild);
    	LogicMoney.getBalance(id, LogicMoney.AccountType.GUILD.rl);
    	LogicMoney.setBalance(newGuild.guildID, LogicMoney.agentType(Type.GUILD), ConfigCore.GUILD_STARTING_FUNDS);
    	RANKS.put(id, new HashMap<Integer, String>());
    	addRank(id, "Member");
    	for (PermKey perms : PermKey.values()) {setPermission(new RankPerms(id, perms.rl, ComVars.NIL, 0, true));}
    	addMember(id, agent.refID, 0);
    	return new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.guild.create.success");
	}
	
	public static TranslatableResult<ResultType> removeGuild(UUID guildID) {
		getGuilds().remove(guildID);
		MEMBERS.remove(guildID);
		RANKS.remove(guildID);
		PERMS.remove(guildID);
		LogicMoney.deleteAccount(guildID, LogicMoney.AccountType.GUILD.rl);
		return service.removeGuild(guildID);
	}
	
	public static TranslatableResult<ResultType> inviteMember(UUID guildID, Agent exec, Agent target) {
		if (!hasPermission(guildID, PermKey.INVITE_MEMBERS.rl, exec.refID))
			return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.guild.member.invite.failure.permission");
		return addMember(guildID, target.refID, -1);
	}
	
	public static TranslatableResult<ResultType> addMember(UUID guildID, UUID playerID, int rank) {
		getMembers(guildID).put(playerID, rank);
		return service.setMember(guildID, playerID, rank);
	}
	
	public static TranslatableResult<ResultType> updateMember(UUID guildID, Agent exec, UUID playerID, int rank) {
		if (!hasPermission(guildID, PermKey.SET_MEMBER_RANKS.rl, exec.refID))
			return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.guild.member.setrank.failure.permission");
		if (!getMembers(guildID).containsKey(playerID))
			return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.guild.member.remove.failure");
		getMembers(guildID).put(playerID, rank);
    	return service.setMember(guildID, playerID, rank);
    }
	
	public static TranslatableResult<ResultType> kickMember(UUID guildID, Agent exec, UUID playerID) {
		if (!hasPermission(guildID, PermKey.KICK_MEMBER.rl, exec.refID))
			return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.guild.member.remove.failure.permission");
		return removeMember(guildID, playerID);
	}
	
	public static TranslatableResult<ResultType> removeMember(UUID guildID, UUID playerID) {
		//check if member is last in the guild to delete guild
		if (nonInviteMemberCount(guildID) <= 1 && getMembers(guildID).get(playerID) >= 0) {return service.removeGuild(guildID);}
		if (getMembers(guildID).remove(playerID) == null) return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.guild.member.remove.failure");
		//check if not last member but last member of permission management rank, reduce permission management level
		int permittedCount = 0;
		int lowestRank = Integer.MAX_VALUE;
		for (Map.Entry<UUID, Integer> mbrs : getMembers(guildID).entrySet()) {
			if (hasPermission(guildID, PermKey.MANAGE_PERMISSIONS.rl, mbrs.getKey())) permittedCount++;
			if (mbrs.getValue() < lowestRank) lowestRank = mbrs.getValue();
		}
		if (permittedCount == 0) {setPermission(new RankPerms(guildID, PermKey.MANAGE_PERMISSIONS.rl, lowestRank, true));}    	   	
    	return service.removeMember(guildID, playerID);
    }	
	  
	public static TranslatableResult<ResultType> changePermission(RankPerms perm, Agent exec, boolean isAddition) {
		if (!hasPermission(perm.guildID, PermKey.MANAGE_PERMISSIONS.rl, exec.refID))
			return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.guild.permission.set.failure.permission");
		return isAddition ? setPermission(perm) : removePermission(perm);
	}
	
	private static TranslatableResult<ResultType> setPermission(RankPerms perm) {
		if (!perm.player.equals(ComVars.NIL) && getMembers(perm.guildID).getOrDefault(perm.player, -1) < 0)
			return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.guild.permission.set.failure.member");
		if (perm.rank != -2 && !getRanks(perm.guildID).containsKey(perm.rank))
			return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.guild.permission.set.failure.rank");
    	List<RankPerms> list = getPerms(perm.guildID).get(perm.key);
    	for (int i = 0; i < list.size(); i++) {
    		if (list.get(i).matches(perm)) return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.guild.permission.set.failure");
    	}
    	list.add(perm);
    	getPerms(perm.guildID).put(perm.key, list);
    	return service.setPermission(perm);
    }
		
	private static TranslatableResult<ResultType> removePermission(RankPerms perm) {
		List<RankPerms> list = getPerms(perm.guildID).get(perm.key);
		for (int i = 0; i < list.size(); i++) {
    		if (list.get(i).matches(perm)) {
    			getPerms(perm.guildID).get(perm.key).remove(i);
    			//checks if this was the last permission and adds in 
    			//a default permission for rank zero if true
    			if (list.size() == 0) {
    				RankPerms defaultPerm = new RankPerms(perm.guildID, perm.key, 0, true);
    				setPermission(defaultPerm);
    			}
    			return service.removePermission(perm);
    		}
    	}
		return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.guild.permission.remove.failure");
	}
		
	public static boolean hasPermission(UUID guildID, String key, UUID player) {
		int rank = getMembers(guildID).get(player);
		if (rank < 0 ) return false;
		List<RankPerms> list = getPerms(guildID).get(key);
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).player.equals(player) || list.get(i).rank == rank ||
					(rank < list.get(i).rank && list.get(i).cascades)) return true;
		}
		/* note to self when redsigning the perms gui.  cascades should highlight or display
		 * in some way to the user so that it is apparent the permission affects all lower ranks.
		 */
		return false;
	}
		
	public static TranslatableResult<ResultType> buyNewRank(UUID guildID, Agent exec, String title) {
		if (!hasPermission(guildID, PermKey.BUY_NEW_RANK.rl, exec.refID))
			return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.guild.rank.add.failure.permission");
		if (LogicMoney.getBalance(guildID, LogicMoney.agentType(Type.GUILD)) < ConfigCore.GUILD_RANK_ADD_COST)
			return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.guild.rank.add.failure.funds");
		LogicMoney.changeBalance(guildID, LogicMoney.agentType(Type.GUILD), -ConfigCore.GUILD_RANK_ADD_COST);
		return addRank(guildID, title);
	}
		
	private static TranslatableResult<ResultType> addRank(UUID guildID, String title) {
		int rank = getBottomRank(guildID)+1;
    	getRanks(guildID).put(rank, title);
    	service.addRank(guildID, rank, title);
    	return new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.guild.rank.add.success");
    }
	
	public static TranslatableResult<ResultType> setRankTitle(UUID guildID, Agent exec, int rank, String newTitle) {
		if (!hasPermission(guildID, PermKey.RANK_TITLE_CHANGE.rl, exec.refID)) 
			return new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.guild.rank.set.failure.permission");
		if (!getRanks(guildID).containsKey(rank)) {return new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.guild.rank.set.failure");}
		if (LogicMoney.getBalance(guildID, LogicMoney.AccountType.GUILD.rl) < ConfigCore.GUILD_NAME_CHANGE_COST) {
			return new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.guild.rank.set.failure");}
		LogicMoney.changeBalance(guildID, LogicMoney.AccountType.GUILD.rl, -(ConfigCore.GUILD_NAME_CHANGE_COST));
		getRanks(guildID).put(rank, newTitle);
		service.setRankTitle(guildID, rank, newTitle);
    	return new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.guild.rank.set.success");
    }
	
	public static TranslatableResult<ResultType> withdrawFromGuild(UUID guildID, Agent exec, double amount) {
		if (!hasPermission(guildID, PermKey.ACCOUNT_WITHDRAW.rl, exec.refID)) 
			return new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.guild.withdraw.failure.permission");
		return LogicMoney.transferFunds(guildID, LogicMoney.agentType(Type.GUILD), exec.refID, LogicMoney.agentType(exec.type), amount)
				? new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.guild.withdraw.success")
				: new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.guild.withdraw.failure");
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
	
	public static List<Guild> getJoinableGuilds(UUID player) {
		List<Guild> list = new ArrayList<Guild>();
		for (Map.Entry<UUID, Guild> guilds : getGuilds().entrySet()) {
			if (guilds.getValue().open) {
				list.add(guilds.getValue());
				continue;
			}
			else {
				if (getMembers(guilds.getKey()).getOrDefault(player, -2) == -1) {
					list.add(guilds.getValue());
					continue;
				}
			}
		}
		return list;
	}
	
	public static int nonInviteMemberCount(UUID guildID) {
		int count = 0;
		for (Map.Entry<UUID, Integer> mbrs : getMembers(guildID).entrySet()) {
			if (mbrs.getValue() >= 0) count++;
		}
		return count;
	}
	
	public static List<String> applyTaxes(Map<UUID, Duo<Integer, Double>> chunkCounts) {
		//TODO change return type to allow for a stream of messages that can be sent to appropriate entities
		List<String> outputStream = new ArrayList<String>();
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
		return outputStream;
	}
}
