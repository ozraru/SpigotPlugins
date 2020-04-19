package work.raru.spigot.rankmanager;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DatabaseManager {
	static Connection conn;
	static int init(File DataDirectory) {
		if (!initConnection(DataDirectory)) {
			return 1;
		}
		if (!initTable("LinkList("
				+ "minecraft CHAR(36) NOT NULL,"
				+ "discord BIGINT NOT NULL)")) {
			return 2;
		}
		if (!initTable("LinkToken("
				+ "minecraft CHAR(36) NOT NULL,"
				+ "token VARCHAR(12) NOT NULL,"
				+ "expireTimeStamp TIMESTAMP NOT NULL)")) {
			return 3;
		}
		return 0;
	}
	static boolean initConnection(File DataDirectory) {
		String path = DataDirectory.getAbsolutePath() + "/" + Main.config.getString("DatabaseName");
		try {
			conn = DriverManager.getConnection("jdbc:sqlite:" + path);
			conn.setAutoCommit(false);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	static boolean initTable(String tableSettings) {
		try {
			Statement stmt = conn.createStatement();
			stmt.execute("CREATE TABLE IF NOT EXISTS "+ tableSettings);
			stmt.close();
			conn.commit();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	static int cleanup() {
		try {
			Statement stmt = DatabaseManager.DELETE("LinkToken", "expireTimeStamp > '"+Timestamp.valueOf(LocalDateTime.now())+"'");
			int count = stmt.getUpdateCount();
			stmt.close();
			return count;
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}
	public static ResultSet SELECT(@Nonnull String Table, @Nonnull String[] columns, @Nullable String FILTER, @Nullable String ORDER) throws SQLException {
		String column = "";
		int count = 0;
		for (String c: columns) {
			count ++;
			if (count != 1) {
				column += ", ";
			}
			column += c;
		}
		return SELECT(Table, column, FILTER, ORDER);
	}
	public static ResultSet SELECT(@Nonnull String Table, @Nonnull String column, @Nullable String FILTER, @Nullable String ORDER) throws SQLException {
		Statement stmt;
		stmt = conn.createStatement();
		String SQL = "SELECT ";
		SQL += column;
		SQL += " FROM ";
		SQL += Table;
		if (FILTER != null) {
			SQL += " WHERE ";
			SQL += FILTER;
		}
		if (ORDER != null) {
			SQL += " ORDER BY ";
			SQL += ORDER;
		}
		return stmt.executeQuery(SQL);
	}
	public static Statement INSERT(@Nonnull String Table, @Nonnull String[] values, @Nullable String[] orders) throws SQLException {
		int count = 0;
		String value;
		value = "";
		if (values == null) {
			value =null;
		} else {
			count = 0;
			for (String v: values) {
				count ++;
				if (count > 1) {
					value += ",";
				}
				value += v;
			}
		}
		String order;
		if (orders == null) {
			order =null;
		} else {
			count = 0;
			order = "";
			for (String o: orders) {
				count ++;
				if (count > 1) {
					order += ",";
				}
				order += "'"+o+"'";
			}
		}
		return INSERT(Table, value, order);
	}
	public static Statement INSERT(@Nonnull String Table, @Nonnull String value, @Nullable String order) throws SQLException {
		Statement stmt;
		stmt = conn.createStatement();
		String SQL = "INSERT INTO "+Table;
		if (order != null) {
			SQL += " ("+order+")";
		}
		SQL += " VALUES ("+value+")";
		stmt.execute(SQL);
		return stmt;
	}
	public static Statement DELETE(@Nonnull String Table, @Nonnull String FILTER) throws SQLException {
		Statement stmt;
		stmt = conn.createStatement();
		String SQL = "DELETE FROM "+Table+" WHERE "+FILTER;
		stmt.execute(SQL);
		return stmt;
	}
	public static boolean confirm() {
		try {
			conn.commit();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	public static boolean cancel() {
		try {
			conn.rollback();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	public static boolean disconnect() {
		try {
			conn.rollback();
			conn.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}