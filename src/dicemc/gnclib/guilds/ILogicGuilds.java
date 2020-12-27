package dicemc.gnclib.guilds;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import dicemc.gnclib.configs.ConfigCore;
import dicemc.gnclib.guilds.Guild.permKey;
import dicemc.gnclib.money.LogicMoney;
import dicemc.gnclib.money.LogicMoney.AccountType;
import dicemc.gnclib.util.ComVars;
import dicemc.gnclib.util.TranslatableResult;

public interface ILogicGuilds {
	public static enum GuildResult {SUCCESS, FAIL}
	public static enum guildUpdates {NAME, OPEN, TAX, PERMLVLS, PERMS, MEMBERS, PERMLVL_ADD}
	
	public Map<UUID, Guild> getGuilds();

	public default Guild getGuildByID(UUID guild) {
		return getGuilds().get(guild);
	}
	
    public default Guild getGuildByName(String guildName) {
		for (Map.Entry<UUID, Guild> entry : getGuilds().entrySet()) {
			if (entry.getValue().name.equalsIgnoreCase(guildName)) {return entry.getValue();}
		}
    	return null;
    }
    
    public default Guild getGuildByMember(UUID playerID) {
		for (Map.Entry<UUID, Guild> entry : getGuilds().entrySet()) {
			if (!entry.getValue().members.getOrDefault(playerID, ComVars.INV).equals(ComVars.INV)) {return entry.getValue();}
		}
    	return null;
    }
    
    public default TranslatableResult<GuildResult> setGuild(Guild guild, List<guildUpdates> changes) {
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
    		case PERMLVLS: { ogg.ranks = guild.ranks; break;}
    		case PERMS: { ogg.permissions = guild.permissions; break;}
    		case MEMBERS: { ogg.members = guild.members; break;}
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

	public default TranslatableResult<GuildResult> createGuild(String name, boolean isAdmin) {
		UUID id = ComVars.unrepeatedUUIDs(getGuilds());
    	getGuilds().put(id, new Guild(name, id, isAdmin));
    	LogicMoney.getBalance(id, ComVars.MOD_ID+":guild");
    	for (permKey perms : permKey.values()) {addPermission(id, perms, 0);}
    	return new TranslatableResult<GuildResult>(GuildResult.SUCCESS, "lib.guild.create.success");
	}
	
	public default TranslatableResult<GuildResult> addMember(UUID guildID, UUID playerID, UUID rank) {return updateMember(guildID, playerID, rank);}
	
	public default TranslatableResult<GuildResult> updateMember(UUID guildID, UUID playerID, UUID rank) {
    	if (!getGuilds().get(guildID).ranks.containsKey(rank)) return new TranslatableResult<GuildResult>(GuildResult.FAIL, "lib.guild.member.update.failure");
    	else {
    		getGuilds().get(guildID).members.put(playerID, rank);
    	}
    	return new TranslatableResult<GuildResult>(GuildResult.SUCCESS, "lib.guild.member.update.success");
    }
	
	public default TranslatableResult<GuildResult> removeMember(UUID guildID, UUID playerID) {
    	if (getGuilds().get(guildID).members.remove(playerID) != null) return new TranslatableResult<GuildResult>(GuildResult.SUCCESS, "lib.guild.member.remove.success");   	
    	else return new TranslatableResult<GuildResult>(GuildResult.FAIL, "lib.guild.member.remove.failure");
    }
	
	public default Map<UUID, UUID> getMembers(UUID guildID) {return getGuilds().get(guildID).members;}
	
	public default TranslatableResult<GuildResult> addPermission(UUID guildID, permKey permTag, int value) { return setPermission(guildID, permTag, value); }
    
	public default TranslatableResult<GuildResult> setPermission(UUID guildID, permKey permTag, int value) {
    	getGuilds().get(guildID).permissions.put(permTag, value);
    	return new TranslatableResult<GuildResult>(GuildResult.SUCCESS, "lib.guild.permission.set.success");
    }
	
	public default TranslatableResult<GuildResult> addRank(UUID guildID, String title) {
		Guild guild = getGuilds().get(guildID);
		Rank rank = new Rank(ComVars.unrepeatedUUIDs(guild.ranks), "title", guild.ranks.get(getBottomRank(guildID)).sequence + 1);
    	guild.ranks.put(ComVars.unrepeatedUUIDs(guild.ranks), rank);
    	return new TranslatableResult<GuildResult>(GuildResult.SUCCESS, "lib.guild.rank.add.success");
    }
	
	public default TranslatableResult<GuildResult> setRankTitle(UUID guildID, UUID rank, String newTitle) {
    	if (!getGuilds().get(guildID).ranks.containsKey(rank)) return new TranslatableResult<GuildResult>(GuildResult.FAIL, "lib.guild.rank.set.failure");
    	getGuilds().get(guildID).ranks.get(rank).title = newTitle;
    	return new TranslatableResult<GuildResult>(GuildResult.SUCCESS, "lib.guild.rank.set.success");
    }
	
	public default Map<UUID, Rank> getRanks(UUID guildID) {return getGuilds().get(guildID).ranks;}
	
	public default UUID getBottomRank(UUID guildID) {
		int sequence = 0;
		UUID result = null;
		outer:
		while (sequence >= 0) {
			for (Map.Entry<UUID, Rank> map : getGuilds().get(guildID).ranks.entrySet()) {
				if (map.getValue().sequence == sequence) {
					sequence++;
					result = map.getKey();
					continue outer;
				}				
			}
			sequence = -1;
		}
    	return result;
    }
}
