package hudson.plugins.unityasset.scm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UnityDatabseUtil {

	private static UnityDatabseUtil instance;

	private static final String DRIVER = "org.postgresql.Driver";
	private static final String PREFIX = "jdbc:postgresql://";

	public static UnityDatabseUtil getInstance() {
		if (instance == null) {
			instance = new UnityDatabseUtil();
		}
		return instance;
	}

	private UnityDatabseUtil() {

	}

	public Connection getSqlConnection(String host, Integer port,
			String instance, String userName, String password)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, SQLException {

		if (port == 0)
			port = 10733;

		String url = PREFIX + host + ":" + port + "/" + instance;

		Class.forName(DRIVER).newInstance();
		Logger.getLogger("UnitAssetPlugin").log(Level.INFO,
				"Database Url: " + url);
		return DriverManager.getConnection(url, userName, password);

	}

}
