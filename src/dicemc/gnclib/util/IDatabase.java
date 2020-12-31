package dicemc.gnclib.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface IDatabase {
	List<String> defineTables();
	
	public default ResultSet executeSELECT(PreparedStatement sql) {
		try {return sql.executeQuery(); } catch (SQLException e) {e.printStackTrace();}
		return null;
	}
	
	public default int executeUPDATE(PreparedStatement sql) {
		try {return sql.executeUpdate(); } catch (SQLException e) {e.printStackTrace();}
		return 0;
	}
}
