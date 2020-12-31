package dicemc.gnclib.guilds.dbref;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import dicemc.gnclib.guilds.LogicGuilds.GuildResult;
import dicemc.gnclib.guilds.LogicGuilds.PermKey;
import dicemc.gnclib.guilds.entries.Guild;
import dicemc.gnclib.guilds.entries.RankPerms;
import dicemc.gnclib.util.IDatabase;
import dicemc.gnclib.util.TranslatableResult;

public class H2Impl implements IDBImplGuild, IDatabase{

	public H2Impl(String worldName) {}

	@Override
	public List<String> defineTables() {
		List<String> list = new ArrayList<String>();
		String sql = "guildTable"; //TODO Define table
		list.add(sql);
		sql = "memberTable"; //TODO Define table
		list.add(sql);
		sql = "rankTable"; //TODO Define table
		list.add(sql);
		sql = "rankPermsTable"; //TODO Define table
		list.add(sql);
		return list;
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
	public TranslatableResult<GuildResult> setGuild(Guild guild) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TranslatableResult<GuildResult> removeGuild(UUID guildID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<UUID, Integer> getGuildMembers(UUID guildID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TranslatableResult<GuildResult> addMember(UUID guildID, UUID playerID, int rank) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TranslatableResult<GuildResult> setMember(UUID guildID, UUID playerID, int rank) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TranslatableResult<GuildResult> removeMember(UUID guildID, UUID playerID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Integer, String> getGuildRanks(UUID guildID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TranslatableResult<GuildResult> addRank(UUID guildID, int rank, String title) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TranslatableResult<GuildResult> setRankTitle(UUID guildID, int rank, String title) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<RankPerms> getPermissionEntries(UUID guildID, PermKey key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TranslatableResult<GuildResult> setPermission(RankPerms perm) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TranslatableResult<GuildResult> removePermission(RankPerms perm) {
		// TODO Auto-generated method stub
		return null;
	}

}
