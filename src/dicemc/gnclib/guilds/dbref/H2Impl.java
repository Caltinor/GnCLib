package dicemc.gnclib.guilds.dbref;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import dicemc.gnclib.guilds.entries.Guild;
import dicemc.gnclib.guilds.entries.RankPerms;
import dicemc.gnclib.util.IDatabase;
import dicemc.gnclib.util.ResultType;
import dicemc.gnclib.util.TranslatableResult;

public class H2Impl implements IDBImplGuild, IDatabase{

	public H2Impl(String worldName) {}

	@Override
	public Map<String, String> defineTables() {
		Map<String, String> map = new HashMap<String, String>();
		String tbl = "";
		String sql = "guildTable"; //TODO Define table
		map.put(tbl, sql);
		tbl = "";
		sql = "memberTable"; //TODO Define table
		map.put(tbl, sql);
		tbl = "";
		sql = "rankTable"; //TODO Define table
		map.put(tbl, sql);
		tbl = "";
		sql = "rankPermsTable"; //TODO Define table
		map.put(tbl, sql);
		return map;
	}

	@Override
	public void printAllTables() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<UUID, Guild> getAllGuilds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Guild addGuild(Guild guild) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TranslatableResult<ResultType> setGuild(Guild guild) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TranslatableResult<ResultType> removeGuild(UUID guildID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<UUID, Integer> getGuildMembers(UUID guildID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TranslatableResult<ResultType> addMember(UUID guildID, UUID playerID, int rank) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TranslatableResult<ResultType> setMember(UUID guildID, UUID playerID, int rank) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TranslatableResult<ResultType> removeMember(UUID guildID, UUID playerID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Integer, String> getGuildRanks(UUID guildID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TranslatableResult<ResultType> addRank(UUID guildID, int rank, String title) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TranslatableResult<ResultType> setRankTitle(UUID guildID, int rank, String title) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<RankPerms> getPermissionEntries(UUID guildID, String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TranslatableResult<ResultType> setPermission(RankPerms perm) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TranslatableResult<ResultType> removePermission(RankPerms perm) {
		// TODO Auto-generated method stub
		return null;
	}
	

}
