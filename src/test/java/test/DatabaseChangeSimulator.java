package test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseChangeSimulator {

	public static void main(String... args) {

		String prefix = "jdbc:postgresql://";
		int port = 0;

		try {

			Connection conn = getSqlConnection(prefix + "localhost"
					+ (port == 0 ? "" : (":" + port)) + "/" + "postgres",
					"org.postgresql.Driver", "postgres", "qweasd");

			StringBuilder insertSql = new StringBuilder(
					"INSERT INTO changeset(serial, commit_time, creator, description)VALUES (?, ?, ?, ?)");

			GregorianCalendar now = new GregorianCalendar();
			now.setTimeInMillis(new Date().getTime());
			GregorianCalendar nextHour = new GregorianCalendar();
			nextHour.add(GregorianCalendar.HOUR, 1);
			int i = 0;
			int assetVersionIndex = 0;
			while (now.before(nextHour)) {

				PreparedStatement stmt = conn.prepareStatement(insertSql
						.toString());

				stmt.setInt(1, i);
				stmt.setTimestamp(2, new Timestamp(now.getTimeInMillis()));
				stmt.setInt(3, new Random().nextInt(4) + 1);
				stmt.setString(4,
						"A change every 10 minutes is made to test the plugin! Random number: ["
								+ new Random().nextInt(1000) + "]");

				stmt.execute();
				stmt.close();

				int randomChanges = new Random().nextInt(10) + 1;

				for (int j = 0; j < randomChanges; j++) {
					assetVersionIndex++;
					StringBuilder sql = new StringBuilder(
							"INSERT INTO changesetcontents(changeset, assetversion)VALUES (?, ?)");

					stmt = conn.prepareStatement(sql.toString());

					stmt.setInt(1, i);
					stmt.setInt(2, assetVersionIndex);

					stmt.execute();
					stmt.close();

					sql = new StringBuilder(
							"INSERT INTO assetversion(name, serial, created_in, revision, asset)VALUES (?, ?, ?, ?, ?)");

					stmt = conn.prepareStatement(sql.toString());

					stmt.setString(1,
							"some random stuff[" + new Random().nextInt(1000)
									+ "]");
					stmt.setInt(2, assetVersionIndex);
					stmt.setTimestamp(3, new Timestamp(now.getTimeInMillis()));
					stmt.setInt(4, new Random().nextInt(3));
					stmt.setString(5, "More stuff");

					stmt.execute();
					stmt.close();

				}

				i++;
				now.add(GregorianCalendar.MINUTE, 10);
			}

			conn.commit();
			conn.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static Connection getSqlConnection(String url, String driver,
			String userName, String password) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException {

		Class.forName(driver).newInstance();
		Logger.getLogger("UnitAssetPlugin").log(Level.INFO,
				"Database Url: " + url);
		return DriverManager.getConnection(url, userName, password);

	}

}
