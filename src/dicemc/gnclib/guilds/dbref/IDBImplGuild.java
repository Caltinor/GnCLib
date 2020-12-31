package dicemc.gnclib.guilds.dbref;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import dicemc.gnclib.guilds.LogicGuilds.GuildResult;
import dicemc.gnclib.guilds.LogicGuilds.PermKey;
import dicemc.gnclib.guilds.entries.Guild;
import dicemc.gnclib.guilds.entries.RankPerms;
import dicemc.gnclib.util.TranslatableResult;

public interface IDBImplGuild {	
	Map<UUID, Guild> getAllGuilds();
	/** Creates a new guild using the dat from the
	 * provided guild.  Only the id is ignored.  
	 * once the guild is inserted into the DB, the
	 * guild object is reconstructed with the proper
	 * ID and returned.
	 * 
	 * @param guild generic guild object with new ID and Title
	 * @return Guild object as reflected in the database
	 */
	Guild addGuild(Guild guild);
	TranslatableResult<GuildResult> setGuild(Guild guild);
	TranslatableResult<GuildResult> removeGuild(UUID guildID);
	
	Map<UUID, Integer> getGuildMembers(UUID guildID);
	TranslatableResult<GuildResult> addMember(UUID guildID, UUID playerID, int rank);
	TranslatableResult<GuildResult> setMember(UUID guildID, UUID playerID, int rank);
	TranslatableResult<GuildResult> removeMember(UUID guildID, UUID playerID);
	
	Map<Integer, String> getGuildRanks(UUID guildID);
	TranslatableResult<GuildResult> addRank(UUID guildID, int rank, String title);
	TranslatableResult<GuildResult> setRankTitle(UUID guildID, int rank, String title);
	
	List<RankPerms> getPermissionEntries(UUID guildID, PermKey key);
	TranslatableResult<GuildResult> setPermission(RankPerms perm);
	TranslatableResult<GuildResult> removePermission(RankPerms perm);
}
