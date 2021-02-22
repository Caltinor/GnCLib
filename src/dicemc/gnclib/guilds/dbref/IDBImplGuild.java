package dicemc.gnclib.guilds.dbref;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import dicemc.gnclib.util.ResultType;
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
	TranslatableResult<ResultType> setGuild(Guild guild);
	TranslatableResult<ResultType> removeGuild(UUID guildID);
	
	Map<UUID, Integer> getGuildMembers(UUID guildID);
	TranslatableResult<ResultType> addMember(UUID guildID, UUID playerID, int rank);
	TranslatableResult<ResultType> setMember(UUID guildID, UUID playerID, int rank);
	TranslatableResult<ResultType> removeMember(UUID guildID, UUID playerID);
	
	Map<Integer, String> getGuildRanks(UUID guildID);
	TranslatableResult<ResultType> addRank(UUID guildID, int rank, String title);
	TranslatableResult<ResultType> setRankTitle(UUID guildID, int rank, String title);
	
	List<RankPerms> getPermissionEntries(UUID guildID, String key);
	TranslatableResult<ResultType> setPermission(RankPerms perm);
	TranslatableResult<ResultType> removePermission(RankPerms perm);
}
