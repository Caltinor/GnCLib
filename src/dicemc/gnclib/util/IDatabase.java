package dicemc.gnclib.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public interface IDatabase {
	Map<String, String> defineTables();
	
	default ResultSet executeSELECT(PreparedStatement sql) {
		try {return sql.executeQuery(); } catch (SQLException e) {e.printStackTrace();}
		return null;
	}
	
	default int executeUPDATE(PreparedStatement sql) {
		try {return sql.executeUpdate(); } catch (SQLException e) {e.printStackTrace();}
		return 0;
	}
}
