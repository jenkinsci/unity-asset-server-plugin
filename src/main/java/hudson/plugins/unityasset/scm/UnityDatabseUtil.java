/*The MIT License

Copyright (c) 2010, Bigpoint GmbH, Marcelo Adriano Brunken, Nils Hofmeister

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.*/

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
