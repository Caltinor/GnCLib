package dicemc.gnclib.money.dbref;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import dicemc.gnclib.configs.ConfigCore;
import dicemc.gnclib.util.IDatabase;

public class H2Impl implements IDBImplMoney, IDatabase{
	private final Map<tblMoney, String> map_Money = define_Money();
	private Connection con;
	
	public H2Impl(String saveName) {
		String port = ConfigCore.DB_PORT;
		String name = saveName + ConfigCore.DB_NAME;
		String url  = ConfigCore.DB_URL + "\\";
		String host = "jdbc:h2:" + url + port + name;;
		String user = ConfigCore.DB_USER;
		String pass = ConfigCore.DB_PASS;

		try {
			System.out.println("Attempting Account DB Connection");
			con = DriverManager.getConnection(host, user, pass);
			System.out.println("Account DB Connection Successful");
			for (Map.Entry<String, String> entry : defineTables().entrySet()) {
				String sql = "CREATE TABLE IF NOT EXISTS "+ entry.getKey() + entry.getValue();
				PreparedStatement st = con.prepareStatement(sql);
				executeUPDATE(st);
			}
		} catch (SQLException e) {e.printStackTrace();}
	}
	
	@Override
	public Map<String, String> defineTables() {
		Map<String, String> map = new HashMap<String, String>();
		String tbl = map_Money.get(tblMoney.TABLE_NAME);
		String sql = "("+ map_Money.get(tblMoney.ID) +" INT AUTO_INCREMENT PRIMARY KEY, " +
				map_Money.get(tblMoney.OWNER) +" UUID, " +
				map_Money.get(tblMoney.TYPE) +" VARCHAR, " +
				map_Money.get(tblMoney.BALANCE) +" DOUBLE);";
		map.put(tbl, sql);
		return map;
	}

	@Override
	public double getBalance(UUID owner, String ownerType) {
		String sql = "SELECT * FROM "+ map_Money.get(tblMoney.TABLE_NAME) +
				" WHERE "+ map_Money.get(tblMoney.OWNER) +" =? AND " +
				map_Money.get(tblMoney.TYPE) +" =?";
		PreparedStatement st = null;
		try {
			st = con.prepareStatement(sql);
			st.setObject(1, owner);
			st.setString(2, ownerType.toString());
		} catch (SQLException e1) {e1.printStackTrace();}
		

		ResultSet rs = executeSELECT(st);
		try {
			if (!rs.isBeforeFirst()) {return addAccount(owner, ownerType);}
			if (rs.next()) {return rs.getDouble("balance");			
		}
		} catch (SQLException e) {e.printStackTrace();}
		return -1;
	}

	@Override
	public boolean setBalance(UUID owner, String ownerType, double value) {
		String sql = "UPDATE "+ map_Money.get(tblMoney.TABLE_NAME) + " SET " +
				map_Money.get(tblMoney.BALANCE) + " =? WHERE "+
				map_Money.get(tblMoney.OWNER) +" =? AND "+
				map_Money.get(tblMoney.TYPE) +" =?;";
		PreparedStatement st = null;
		try {
			st = con.prepareStatement(sql);
			st.setObject(2, owner);
			st.setString(3, ownerType.toString());
			st.setDouble(1, value);
		} catch (SQLException e1) {e1.printStackTrace();}		
		return executeUPDATE(st) > 0;
	}

	@Override
	public boolean changeBalance(UUID owner, String ownerType, double value) {
		String sql = "SELECT * FROM "+ map_Money.get(tblMoney.TABLE_NAME) +
				" WHERE "+ map_Money.get(tblMoney.OWNER) +" =? AND " +
				map_Money.get(tblMoney.TYPE) +" =?";
		PreparedStatement st = null;
		try {
			st = con.prepareStatement(sql);
			st.setObject(1, owner);
			st.setString(2, ownerType.toString());
		} catch (SQLException e1) {e1.printStackTrace(); return false;}
		ResultSet rs = executeSELECT(st);
		double currentBal = 0d;
		try {
			if (!rs.isBeforeFirst()) return false;
			if (rs.next()) {
				currentBal = rs.getDouble("balance");
			}
		} catch (SQLException e) {e.printStackTrace(); return false;}
		currentBal += value;		
		return setBalance(owner, ownerType, currentBal);
	}

	@Override
	public boolean transferFunds(UUID ownerFrom, String ownerFromType, UUID ownerTo, String ownerToType, double value) {
		double from = getBalance(ownerFrom, ownerFromType);
		if (from >= value) {
			int check = 0;
			check += changeBalance(ownerFrom, ownerFromType, -(value)) ? 1 : 0;
			check += changeBalance(ownerTo, ownerToType, value) ? 1 : 0;
			return check == 2;
		}
		return false;
	}

	@Override
	public double addAccount(UUID owner, String ownerType) {
		String sql = "INSERT INTO "+ map_Money.get(tblMoney.TABLE_NAME) +" ("+
				map_Money.get(tblMoney.OWNER) +", "+
				map_Money.get(tblMoney.TYPE) +", "+
				map_Money.get(tblMoney.BALANCE) +") "+
				"VALUES (? ,? ,?)";
		PreparedStatement st = null;
		try {
			st = con.prepareStatement(sql);
			st.setObject(1, owner);
			st.setString(2, ownerType);
			st.setDouble(3, ConfigCore.STARTING_FUNDS);
		} catch (SQLException e1) {e1.printStackTrace();}
		
		executeUPDATE(st);
		return ConfigCore.STARTING_FUNDS;
	}
	
	private Map<tblMoney, String> define_Money() {
		Map<tblMoney, String> map = new HashMap<tblMoney, String>();
		map.put(tblMoney.TABLE_NAME, "TBL_MONEY_");
		map.put(tblMoney.ID, "ID");
		map.put(tblMoney.OWNER, "OWNER");
		map.put(tblMoney.TYPE, "TYPE");
		map.put(tblMoney.BALANCE, "BALANCE");
		return map;
	}

}
