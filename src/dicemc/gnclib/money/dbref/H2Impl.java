package dicemc.gnclib.money.dbref;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import dicemc.gnclib.configs.ConfigCore;
import dicemc.gnclib.util.IDatabase;

public class H2Impl implements IDBImplMoney, IDatabase{
	private Connection con;
	
	public H2Impl(String saveName) {
		String port = ConfigCore.DB_PORT;
		String name = saveName + ConfigCore.DB_NAME;
		String url  = ConfigCore.DB_URL;
		String host = "jdbc:h2:" + url + port + name;;
		String user = ConfigCore.DB_USER;
		String pass = ConfigCore.DB_PASS;

		try {
			System.out.println("Attempting Account DB Connection");
			con = DriverManager.getConnection(host, user, pass);
			Statement stmt = con.createStatement();
			System.out.println("DB Connection Successful");
			stmt.execute("CREATE TABLE IF NOT EXISTS tblAccounts (" +
					"ID INT AUTO_INCREMENT PRIMARY KEY, " +
					"Owner UUID, " +
					"type VARCHAR, " +
					"balance DOUBLE);");
		} catch (SQLException e) {e.printStackTrace();}
	}
	
	@Override
	public List<String> defineTables() {return new ArrayList<String>();}

	@Override
	public double getBalance(UUID owner, String ownerType) {
		String sql = "SELECT * FROM tblAccounts WHERE Owner=? AND type=?";
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
		return 0;
	}

	@Override
	public boolean setBalance(UUID owner, String ownerType, double value) {
		String sql = "UPDATE tblAccounts SET balance=? WHERE Owner=? AND type=?";
		PreparedStatement st = null;
		try {
			st = con.prepareStatement(sql);
			st.setObject(2, owner);
			st.setString(3, ownerType.toString());
			st.setDouble(1, value);
		} catch (SQLException e1) {e1.printStackTrace();}
		int out = executeUPDATE(st);
		
		return out > 0;
	}

	@Override
	public boolean changeBalance(UUID owner, String ownerType, double value) {
		String sql = "SELECT * FROM tblAccounts WHERE Owner=? AND type=?";
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
		setBalance(owner, ownerType, currentBal);
		return true;
	}

	@Override
	public boolean transferFunds(UUID ownerFrom, String ownerFromType, UUID ownerTo, String ownerToType, double value) {
		double from = getBalance(ownerFrom, ownerFromType);
		if (from >= value) {
			int check = 0;
			check += changeBalance(ownerFrom, ownerFromType, -(value)) ? 1: 0;
			check += changeBalance(ownerTo, ownerToType, value) ? 1 : 0;
			return check == 2;
		}
		return false;
	}

	@Override
	public double addAccount(UUID owner, String ownerType) {
		String sql = "INSERT INTO tblAccounts (Owner, type, balance) " +
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

}
