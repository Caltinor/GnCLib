package dicemc.gnclib.guilds.dbref;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import dicemc.gnclib.configs.ConfigCore;
import dicemc.gnclib.guilds.entries.Guild;
import dicemc.gnclib.guilds.entries.RankPerms;
import dicemc.gnclib.util.IDatabase;
import dicemc.gnclib.util.ResultType;
import dicemc.gnclib.util.TranslatableResult;

public class H2Impl implements IDBImplGuild, IDatabase{
	public static final Map<tblGuilds, String> map_Guilds = define_Guilds();
	public static final Map<tblMembers, String> map_Members = define_Members();
	public static final Map<tblRanks, String> map_Ranks = define_Ranks();
	public static final Map<tblPerms, String> map_Perms = define_Perms();
	private Connection con;

	public H2Impl(String saveName) {
		String port = ConfigCore.DB_PORT;
		String name = saveName + ConfigCore.DB_NAME;
		String url  = ConfigCore.DB_URL +"\\";
		String host = "jdbc:h2://" + url + port + name;
		String user = ConfigCore.DB_USER;
		String pass = ConfigCore.DB_PASS;
		
		try {
			System.out.println("Attempting Guild DB Connection");
			con = DriverManager.getConnection(host, user, pass);
			System.out.println("Guild DB Connection Successful");
			//obtain table definitions
			Map<String, String> tables = defineTables();
			//create guild table first as parent table
			String sql = "CREATE TABLE IF NOT EXISTS "+ map_Guilds.get(tblGuilds.TABLE_NAME) + tables.get(map_Guilds.get(tblGuilds.TABLE_NAME));
			PreparedStatement st = con.prepareStatement(sql);
			executeUPDATE(st);
			tables.remove(map_Guilds.get(tblGuilds.TABLE_NAME));
			//Cycle through remaining constructed table definitions to create tables.
			for (Map.Entry<String, String> entry : tables.entrySet()) {
				sql = "CREATE TABLE IF NOT EXISTS "+ entry.getKey() + entry.getValue();
				st = con.prepareStatement(sql);
				executeUPDATE(st);
			}
		} catch (SQLException e) {e.printStackTrace();}	
	}

	@Override
	public Map<String, String> defineTables() {
		Map<String, String> map = new HashMap<String, String>();
		String sql = " (" + map_Guilds.get(tblGuilds.ID)+" INT NOT NULL AUTO_INCREMENT, " +
				map_Guilds.get(tblGuilds.GUILD_ID)		+" UUID NOT NULL, " +
				map_Guilds.get(tblGuilds.NAME)			+" VARCHAR(30) NOT NULL, "+
				map_Guilds.get(tblGuilds.OPEN)			+" TINYINT(1) NOT NULL DEFAULT 0, " +
				map_Guilds.get(tblGuilds.ADMIN)			+" TINYINT(1) NOT NULL DEFAULT 0, " +
				map_Guilds.get(tblGuilds.TAX)			+" DOUBLE NOT NULL DEFAULT 0.0, " +
				map_Guilds.get(tblGuilds.TPX)			+" INT, " +
				map_Guilds.get(tblGuilds.TPY)			+" INT, " +
				map_Guilds.get(tblGuilds.TPZ)			+" INT, " +
				map_Guilds.get(tblGuilds.MARKET_SIZE)	+" INT NOT NULL DEFAULT 0, " +
				"PRIMARY KEY ("+ map_Guilds.get(tblGuilds.ID)+"));";
		map.put(map_Guilds.get(tblGuilds.TABLE_NAME), sql);
		sql = " (" + map_Members.get(tblMembers.ID)		+" INT NOT NULL AUTO_INCREMENT, " +
				map_Members.get(tblMembers.PLAYER_ID) 	+" UUID NOT NULL, " +
				map_Members.get(tblMembers.GUILD_ID)	+" UUID NOT NULL, " +
				map_Members.get(tblMembers.RANK)		+" INT NOT NULL, " +
				"PRIMARY KEY ("+ map_Members.get(tblMembers.ID) +"), " +
				"FOREIGN KEY ("+ map_Members.get(tblMembers.GUILD_ID) + 
				") REFERENCES "+ map_Guilds.get(tblGuilds.TABLE_NAME)+"("+map_Guilds.get(tblGuilds.GUILD_ID)+
				") ON DELETE CASCADE);";
		map.put(map_Members.get(tblMembers.TABLE_NAME), sql);
		sql = " (" + map_Ranks.get(tblRanks.ID) 		+" INT NOT NULL AUTO_INCREMENT, " +
				map_Ranks.get(tblRanks.GUILD_ID)		+" UUID NOT NULL, " +
				map_Ranks.get(tblRanks.TITLE)			+" VARCHAR(30) NOT NULL, " +
				map_Ranks.get(tblRanks.SEQUENCE)		+" INT NOT NULL, " +
				"PRIMARY KEY ("+ map_Ranks.get(tblRanks.ID) +"), " +
				"FOREIGN KEY ("+ map_Ranks.get(tblRanks.GUILD_ID) +
				") REFERENCES "+ map_Guilds.get(tblGuilds.TABLE_NAME)+"("+map_Guilds.get(tblGuilds.GUILD_ID) +
				") ON DELETE CASCADE);";
		map.put(map_Ranks.get(tblRanks.TABLE_NAME), sql);
		sql = " (" + map_Perms.get(tblPerms.ID) 		+" INT NOT NULL AUTO_INCREMENT, " +
				map_Perms.get(tblPerms.GUILD_ID)		+" UUID NOT NULL, " +
				map_Perms.get(tblPerms.PERM_KEY)		+" VARCHAR NOT NULL, " +
				map_Perms.get(tblPerms.PLAYER)			+" UUID NOT NULL, " +
				map_Perms.get(tblPerms.RANK)			+" INT NOT NULL, " +
				map_Perms.get(tblPerms.CASCADES)		+" TINYINT(1) DEFAULT 0, " +
				"PRIMARY KEY ("+ map_Perms.get(tblPerms.ID) +"), " +
				"FOREIGN KEY ("+ map_Perms.get(tblPerms.GUILD_ID) +
				") REFERENCES "+ map_Guilds.get(tblGuilds.TABLE_NAME)+"("+map_Guilds.get(tblGuilds.GUILD_ID) +
				") ON DELETE CASCADE);";
		map.put(map_Perms.get(tblPerms.TABLE_NAME), sql);
		return map;
	}

	@Override
	public void printAllTables() {
		List<String> tblNames = new ArrayList<String>();
		tblNames.add(map_Guilds.get(tblGuilds.TABLE_NAME));
		tblNames.add(map_Members.get(tblMembers.TABLE_NAME));
		tblNames.add(map_Ranks.get(tblRanks.TABLE_NAME));
		tblNames.add(map_Perms.get(tblPerms.TABLE_NAME));
		PreparedStatement st = null;
		for (int t = 0; t< tblNames.size(); t++) {
			String sql = "SELECT * FROM " +tblNames.get(t);
			System.out.println("==========="+tblNames.get(t)+"===========");
			try {
				st = con.prepareStatement(sql);
				ResultSet rs = executeSELECT(st);
				int cc = rs.getMetaData().getColumnCount();				
				while (rs.next()) {
					String output = "";
					for (int i = 1; i <= cc; i++) {
						output += rs.getMetaData().getColumnName(i)+":"+rs.getString(i) + ", ";
					}
					System.out.println(output);
				}
			} catch(SQLException e) {e.printStackTrace();}
		}
		
	}

	@Override
	public Map<UUID, Guild> getAllGuilds() {
		Map<UUID, Guild> gmap = new HashMap<UUID, Guild>();
		PreparedStatement st = null;
		String sql = "SELECT * FROM "+map_Guilds.get(tblGuilds.TABLE_NAME);
		try {
			st = con.prepareStatement(sql);
			ResultSet rs = executeSELECT(st);
			if (!rs.isBeforeFirst()) return gmap;
			while (rs.next()) {
				int id = rs.getInt(map_Guilds.get(tblGuilds.ID));
				String name = rs.getString(map_Guilds.get(tblGuilds.NAME));
				UUID guildID = (UUID)rs.getObject(map_Guilds.get(tblGuilds.GUILD_ID));
				boolean open = rs.getBoolean(map_Guilds.get(tblGuilds.OPEN));
				boolean isAdmin = rs.getBoolean(map_Guilds.get(tblGuilds.ADMIN));
				double tax = rs.getDouble(map_Guilds.get(tblGuilds.TAX));
				int tpX = rs.getInt(map_Guilds.get(tblGuilds.TPX));
				int tpY = rs.getInt(map_Guilds.get(tblGuilds.TPY));
				int tpZ = rs.getInt(map_Guilds.get(tblGuilds.TPZ));
				int size = rs.getInt(map_Guilds.get(tblGuilds.MARKET_SIZE));
				Guild guild = new Guild(id, name, guildID, open, tax, isAdmin, tpX, tpY, tpZ, size);
				gmap.put(guildID, guild);
			}
		} catch(SQLException e) {e.printStackTrace();}		
		return gmap;
	}

	@Override
	public Guild addGuild(Guild guild) {
		PreparedStatement st = null;
		Guild outGuild = null;
		//EXECUTE SELECT to ensure the guild doesn't already exist
		String sql = "SELECT * FROM " + map_Guilds.get(tblGuilds.TABLE_NAME) +" WHERE " +
				map_Guilds.get(tblGuilds.GUILD_ID) 	+" =? OR " +
				map_Guilds.get(tblGuilds.NAME) 		+" =?;";
		try {
			st = con.prepareStatement(sql);
			st.setObject(1, guild.guildID);
			st.setString(2, guild.name);
			ResultSet rs = executeSELECT(st);
			if (rs.isBeforeFirst()) return outGuild;
		} catch(SQLException e) {e.printStackTrace();}
		//EXECTUE INSERT to create new record
		sql = "INSERT INTO " +map_Guilds.get(tblGuilds.TABLE_NAME) +" (" +
				map_Guilds.get(tblGuilds.GUILD_ID) +
				", " + map_Guilds.get(tblGuilds.NAME) +
				", " + map_Guilds.get(tblGuilds.ADMIN) +
				") VALUES (?, ?, ?);";
		try {
			st = con.prepareStatement(sql);
			st.setObject(1, guild.guildID);
			st.setString(2, guild.name);
			st.setBoolean(3, guild.isAdmin);
			if (executeUPDATE(st) == 0) return outGuild;
		} catch(SQLException e) {e.printStackTrace();}
		//EXECUTE SELECT to get guild with propper id variable and return
		sql = "SELECT * FROM "+map_Guilds.get(tblGuilds.TABLE_NAME) +" WHERE " +
				map_Guilds.get(tblGuilds.GUILD_ID) +" =?;";
		try {
			st = con.prepareStatement(sql);
			st.setObject(1, guild.guildID);
			ResultSet rs = executeSELECT(st);
			if (!rs.isBeforeFirst()) return outGuild;
			while (rs.next()) {
				int id = rs.getInt(map_Guilds.get(tblGuilds.ID));
				String name = rs.getString(map_Guilds.get(tblGuilds.NAME));
				UUID guildID = (UUID)rs.getObject(map_Guilds.get(tblGuilds.GUILD_ID));
				boolean open = rs.getBoolean(map_Guilds.get(tblGuilds.OPEN));
				boolean isAdmin = rs.getBoolean(map_Guilds.get(tblGuilds.ADMIN));
				double tax = rs.getDouble(map_Guilds.get(tblGuilds.TAX));
				int tpX = rs.getInt(map_Guilds.get(tblGuilds.TPX));
				int tpY = rs.getInt(map_Guilds.get(tblGuilds.TPY));
				int tpZ = rs.getInt(map_Guilds.get(tblGuilds.TPZ));
				int size = rs.getInt(map_Guilds.get(tblGuilds.MARKET_SIZE));
				outGuild = new Guild(id, name, guildID, open, tax, isAdmin, tpX, tpY, tpZ, size);
			}
		} catch(SQLException e) {e.printStackTrace();}	
		return outGuild;
	}

	@Override
	public TranslatableResult<ResultType> setGuild(Guild guild) {
		PreparedStatement st = null;
		//EXECUTE SELECT to ensure the guild already exists
		String sql = "SELECT * FROM " + map_Guilds.get(tblGuilds.TABLE_NAME) +" WHERE " +
				map_Guilds.get(tblGuilds.GUILD_ID) 	+" =?;";
		try {
			st = con.prepareStatement(sql);
			st.setObject(1, guild.guildID);
			ResultSet rs = executeSELECT(st);
			if (!rs.isBeforeFirst()) return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.guild.update.failure.notfound");
		} catch(SQLException e) {e.printStackTrace();}
		//update the entry after confirmation of entry existence
		sql = "UPDATE " + map_Guilds.get(tblGuilds.TABLE_NAME) +" SET "+
				map_Guilds.get(tblGuilds.NAME) 			+" =?, "+
				map_Guilds.get(tblGuilds.OPEN) 			+" =?, "+
				map_Guilds.get(tblGuilds.ADMIN) 		+" =?, "+
				map_Guilds.get(tblGuilds.TAX)			+" =?, "+
				map_Guilds.get(tblGuilds.MARKET_SIZE) 	+" =?, "+
				map_Guilds.get(tblGuilds.TPX)			+" =?, "+
				map_Guilds.get(tblGuilds.TPY)			+" =?, "+
				map_Guilds.get(tblGuilds.TPZ)			+" =? WHERE "+
				map_Guilds.get(tblGuilds.ID) 			+" =?;";
		try {
			st = con.prepareStatement(sql);
			int f = 0;
			st.setString(++f, guild.name);
			st.setBoolean(++f, guild.open);
			st.setBoolean(++f, guild.isAdmin);
			st.setDouble(++f, guild.tax);
			st.setInt(++f, guild.getMarketSize());
			st.setInt(++f, guild.getTPX());
			st.setInt(++f, guild.getTPY());
			st.setInt(++f, guild.getTPZ());
			st.setInt(++f, guild.getID());
			if (executeUPDATE(st) == 0) return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.guild.update.failure.sql");
		} catch(SQLException e) {e.printStackTrace();}				
		return new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.guild.update.success");
	}

	@Override
	public TranslatableResult<ResultType> removeGuild(UUID guildID) {
		PreparedStatement st = null;
		String sql = "DELETE FROM " + map_Guilds.get(tblGuilds.TABLE_NAME) +" WHERE "+
				map_Guilds.get(tblGuilds.GUILD_ID) +" =?;";
		try {
			st = con.prepareStatement(sql);
			st.setObject(1, guildID);
			if (executeUPDATE(st) == 0) 
				return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.guild.delete.failure");
		} catch(SQLException e) {e.printStackTrace();}
		return new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.guild.delete.success");
	}

	@Override
	public Map<UUID, Integer> getGuildMembers(UUID guildID) {
		Map<UUID, Integer> map = new HashMap<UUID, Integer>();
		PreparedStatement st = null;
		String sql = "SELECT * FROM " +map_Members.get(tblMembers.TABLE_NAME) +" WHERE " +
				map_Members.get(tblMembers.GUILD_ID) +" = ?;";
		try {
			st = con.prepareStatement(sql);
			st.setObject(1, guildID);
			ResultSet rs = executeSELECT(st);
			if (!rs.isBeforeFirst()) return map;
			while (rs.next()) {
				UUID player = (UUID)rs.getObject(map_Members.get(tblMembers.PLAYER_ID));
				int rank = rs.getInt(map_Members.get(tblMembers.RANK));
				map.put(player, rank);
			}
		} catch(SQLException e) {e.printStackTrace();}
		return map;
	}

	@Override
	public TranslatableResult<ResultType> addMember(UUID guildID, UUID playerID, int rank) {
		PreparedStatement st = null;
		String sql = "INSERT INTO " +map_Members.get(tblMembers.TABLE_NAME) +" ("+
				map_Members.get(tblMembers.PLAYER_ID) 	+", "+
				map_Members.get(tblMembers.GUILD_ID) 	+", "+
				map_Members.get(tblMembers.RANK) 		+
				") VALUES (?, ?, ?);";
		try {
			st = con.prepareStatement(sql);
			st.setObject(1, playerID);
			st.setObject(2, guildID);
			st.setInt(3, rank);
			if (executeUPDATE(st) == 0) return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.guild.member.add.failure");
		} catch(SQLException e) {e.printStackTrace();}
		return new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.guild.member.add.success");
	}

	@Override
	public TranslatableResult<ResultType> setMember(UUID guildID, UUID playerID, int rank) {
		PreparedStatement st = null;
		String sql = "SELECT * FROM " +map_Members.get(tblMembers.TABLE_NAME)+ " WHERE "+
				map_Members.get(tblMembers.GUILD_ID)	+" =? AND "+
				map_Members.get(tblMembers.PLAYER_ID)	+" =?;";
		try {
			st = con.prepareStatement(sql);
			st.setObject(1, guildID);
			st.setObject(2, playerID);
			ResultSet rs = executeSELECT(st);
			if (!rs.isBeforeFirst()) return addMember(guildID, playerID, rank);
			rs.next();
			int id = rs.getInt(map_Members.get(tblMembers.ID));
			sql = "UPDATE " + map_Members.get(tblMembers.TABLE_NAME)+ " SET "+
					map_Members.get(tblMembers.RANK)	+" =? WHERE "+
					map_Members.get(tblMembers.ID)		+" =?";
			st = con.prepareStatement(sql);
			st.setInt(1, rank);
			st.setInt(2, id);
			if (executeUPDATE(st) == 0) return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.guild.member.update.failure");
		} catch(SQLException e) {e.printStackTrace();}
		return new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.guild.member.update.success");
	}

	@Override
	public TranslatableResult<ResultType> removeMember(UUID guildID, UUID playerID) {
		PreparedStatement st = null;
		String sql = "DELETE FROM "+ map_Members.get(tblMembers.TABLE_NAME) +" WHERE "+
				map_Members.get(tblMembers.GUILD_ID) 	+" =? AND "+
				map_Members.get(tblMembers.PLAYER_ID)	+" =?";
		try {
			st = con.prepareStatement(sql);
			st.setObject(1, guildID);
			st.setObject(2, playerID);
			if (executeUPDATE(st) == 0) 
				return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.guild.member.remove.failure");
		} catch(SQLException e) {e.printStackTrace();}
		return new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.guild.member.remove.success");
	}

	@Override
	public Map<Integer, String> getGuildRanks(UUID guildID) {
		Map<Integer, String> map = new HashMap<Integer, String>();
		PreparedStatement st = null;
		String sql = "SELECT * FROM "+ map_Ranks.get(tblRanks.TABLE_NAME) +" WHERE "+
				map_Ranks.get(tblRanks.GUILD_ID) 	+" =?;";
		try {
			st = con.prepareStatement(sql);
			st.setObject(1, guildID);
			ResultSet rs = executeSELECT(st);
			if (!rs.isBeforeFirst()) return map;
			while (rs.next()) {
				int sequence = rs.getInt(map_Ranks.get(tblRanks.SEQUENCE));
				String title = rs.getString(map_Ranks.get(tblRanks.TITLE));
				map.put(sequence, title);
			}
		} catch(SQLException e) {e.printStackTrace();}
		return map;
	}

	@Override
	public TranslatableResult<ResultType> addRank(UUID guildID, int rank, String title) {
		PreparedStatement st = null;
		String sql = "INSERT INTO " +map_Ranks.get(tblRanks.TABLE_NAME) +" ("+
				map_Ranks.get(tblRanks.GUILD_ID)+", "+
				map_Ranks.get(tblRanks.TITLE) 	+", "+
				map_Ranks.get(tblRanks.SEQUENCE)+
				") VALUES (?, ?, ?);";
		try {
			st = con.prepareStatement(sql);
			st.setObject(1, guildID);
			st.setString(2, title);
			st.setInt(3, rank);
			if (executeUPDATE(st) == 0) return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.guild.rank.add.failure.sql");
		} catch(SQLException e) {e.printStackTrace();}
		return new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.guild.rank.add.success");
	}

	@Override
	public TranslatableResult<ResultType> setRankTitle(UUID guildID, int rank, String title) {
		PreparedStatement st = null;
		String sql = "UPDATE " +map_Ranks.get(tblRanks.TABLE_NAME)+" SET "+
				map_Ranks.get(tblRanks.TITLE)+" =? WHERE " +
				map_Ranks.get(tblRanks.GUILD_ID) +" =? AND "+
				map_Ranks.get(tblRanks.SEQUENCE) +" =?;";
		try {
			st = con.prepareStatement(sql);
			st.setString(1, title);
			st.setObject(2, guildID);
			st.setInt(3, rank);
			if (executeUPDATE(st) == 0) return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.guild.rank.set.failure.sql");
		} catch(SQLException e) {e.printStackTrace();}
		return new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.guild.rank.set.success");
	}

	@Override
	public List<RankPerms> getPermissionEntries(UUID guildID, String key) {
		List<RankPerms> list = new ArrayList<RankPerms>();
		PreparedStatement st = null;
		String sql = "SELECT * FROM " +map_Perms.get(tblPerms.TABLE_NAME) +" WHERE "+
				map_Perms.get(tblPerms.GUILD_ID) 	+" =? AND "+
				map_Perms.get(tblPerms.PERM_KEY)   	+" =?;";
		try {
			st = con.prepareStatement(sql);
			st.setObject(1, guildID);
			st.setString(2, key);
			ResultSet rs = executeSELECT(st);
			if (!rs.isBeforeFirst()) return list;
			while (rs.next()) {
				int id = rs.getInt(map_Perms.get(tblPerms.ID));
				UUID playerID = (UUID)rs.getObject(map_Perms.get(tblPerms.PLAYER));
				int rank = rs.getInt(map_Perms.get(tblPerms.RANK));
				boolean cascades = rs.getBoolean(map_Perms.get(tblPerms.CASCADES));
				list.add(new RankPerms(id, guildID, key, playerID, rank, cascades));
			}
		} catch(SQLException e) {e.printStackTrace();}
		return list;
	}

	@Override
	public TranslatableResult<ResultType> setPermission(RankPerms perm) {
		PreparedStatement st = null;
		String sql = "INSERT INTO " +map_Perms.get(tblPerms.TABLE_NAME) +" ("+
				map_Perms.get(tblPerms.GUILD_ID) 	+", "+
				map_Perms.get(tblPerms.PERM_KEY) 	+", "+
				map_Perms.get(tblPerms.PLAYER) 		+", "+
				map_Perms.get(tblPerms.RANK)  		+", "+
				map_Perms.get(tblPerms.CASCADES)+
				") VALUES (?, ?, ?, ?, ?)";
		try {
			st = con.prepareStatement(sql);
			st.setObject(1, perm.guildID);
			st.setString(2, perm.key);
			st.setObject(3, perm.player);
			st.setInt(4, perm.rank);
			st.setBoolean(5, perm.cascades);
			if (executeUPDATE(st) == 0) return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.guild.permission.set.failure.sql");
		} catch(SQLException e) {e.printStackTrace();}
		return new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.guild.permission.set.success");
	}

	@Override
	public TranslatableResult<ResultType> removePermission(RankPerms perm) {
		PreparedStatement st = null;
		String sql = "DELETE FROM " +map_Perms.get(tblPerms.TABLE_NAME) +" WHERE "+
				map_Perms.get(tblPerms.GUILD_ID)	+" =? AND "+
				map_Perms.get(tblPerms.PLAYER) 		+" =? AND "+
				map_Perms.get(tblPerms.PERM_KEY)	+" =? AND "+
				map_Perms.get(tblPerms.RANK)		+" =?;";
		try {
			st = con.prepareStatement(sql);
			st.setObject(1, perm.guildID);
			st.setObject(2, perm.player);
			st.setString(3, perm.key);
			st.setInt(4, perm.rank);
			if (executeUPDATE(st) == 0) return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.guild.permission.remove.failure");
		} catch(SQLException e) {e.printStackTrace();}
		return new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.guild.permission.remove.success");
	}
	
	private static Map<tblGuilds, String> define_Guilds() {
		Map<tblGuilds, String> map = new HashMap<tblGuilds, String>();
		map.put(tblGuilds.TABLE_NAME, "TBL_GUILDS");
		map.put(tblGuilds.ID, "ID");
		map.put(tblGuilds.GUILD_ID, "GUILD_ID");
		map.put(tblGuilds.NAME, "NAME");
		map.put(tblGuilds.OPEN, "OPEN");
		map.put(tblGuilds.ADMIN, "ADMIN");
		map.put(tblGuilds.TAX, "TAX");
		map.put(tblGuilds.TPX, "TPX");
		map.put(tblGuilds.TPY, "TPY");
		map.put(tblGuilds.TPZ, "TPZ");
		map.put(tblGuilds.MARKET_SIZE, "MARKET_SIZE");
		return map;
	}
	
	private static Map<tblMembers, String> define_Members() {
		Map<tblMembers, String> map = new HashMap<tblMembers, String>();
		map.put(tblMembers.TABLE_NAME, "TBL_MEMBERS");
		map.put(tblMembers.ID, "ID");
		map.put(tblMembers.PLAYER_ID, "PLAYER_ID");
		map.put(tblMembers.GUILD_ID, "GUILD_ID");
		map.put(tblMembers.RANK, "RANK");
		return map;
	}
	
	private static Map<tblRanks, String> define_Ranks() {
		Map<tblRanks, String> map = new HashMap<tblRanks, String>();
		map.put(tblRanks.TABLE_NAME, "TBL_RANKS");
		map.put(tblRanks.ID, "ID");
		map.put(tblRanks.GUILD_ID, "GUILD_ID");
		map.put(tblRanks.TITLE, "TITLE");
		map.put(tblRanks.SEQUENCE, "SEQUENCE");
		return map;
	}
	
	private static Map<tblPerms, String> define_Perms() {
		Map<tblPerms, String> map = new HashMap<tblPerms, String>();
		map.put(tblPerms.TABLE_NAME, "TBL_PERMS");
		map.put(tblPerms.ID, "ID");
		map.put(tblPerms.GUILD_ID, "GUILD_ID");
		map.put(tblPerms.PERM_KEY, "KEY");
		map.put(tblPerms.PLAYER, "PLAYER_ID");
		map.put(tblPerms.RANK, "RANK");
		map.put(tblPerms.CASCADES, "CASCADES");
		return map;
	}
}
