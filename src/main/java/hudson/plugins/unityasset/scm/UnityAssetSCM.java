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

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.scm.ChangeLogParser;
import hudson.scm.SCMDescriptor;
import hudson.scm.SCM;
import hudson.util.FormValidation;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class UnityAssetSCM extends SCM {

	private String databaseUrl;
	private Integer databasePort;
	private String instance;
	private String user;
	private String password;

	@DataBoundConstructor
	public UnityAssetSCM(String databaseUrl, Integer databasePort,
			String instance, String user, String password) {
		super();
		this.databaseUrl = databaseUrl;
		this.databasePort = databasePort;
		this.instance = instance;
		this.user = user;
		this.password = password;
	}

	public String getDatabaseUrl() {
		return databaseUrl;
	}

	public void setDatabaseUrl(String databaseUrl) {
		this.databaseUrl = databaseUrl;
	}

	public Integer getDatabasePort() {
		return databasePort;
	}

	public void setDatabasePort(Integer databasePort) {
		this.databasePort = databasePort;
	}

	public String getInstance() {
		return instance;
	}

	public void setInstance(String instance) {
		this.instance = instance;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public UnityAssetSCM() {
		super();

	}

	private List<UnityChangelog> logChangesDetails(List<UnityChangelog> changes)
			throws Exception {

		Connection conn = UnityDatabseUtil.getInstance().getSqlConnection(
				getDatabaseUrl(), getDatabasePort(), getInstance(), getUser(),
				getPassword());

		for (UnityChangelog entry : changes) {

			StringBuilder sql = new StringBuilder(
					"SELECT a.name, a.serial, a.created_in, a.revision, a.asset FROM assetversion a ,changesetcontents c ");
			sql.append("WHERE a.serial = c.assetversion AND c.changeset = ?");

			PreparedStatement stmt = conn.prepareStatement(sql.toString());
			stmt.setInt(1, entry.getSerial());

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {

				String message = rs.getString("name");

				entry.getItems().add(
						new UnityChangelog.UnityItem(message, rs
								.getInt("revision")));

			}

			rs.close();
			stmt.close();

		}

		conn.close();

		return changes;
	}

	@Override
	public boolean pollChanges(AbstractProject project, Launcher launcher,
			FilePath workspace, TaskListener listener) throws IOException,
			InterruptedException {

		Calendar calendar = Calendar.getInstance(TimeZone.getDefault());

		if (project.getLastSuccessfulBuild() != null) {
			calendar = project.getLastSuccessfulBuild().getTimestamp();
		}

		List<UnityChangelog> changes = getChangeList(calendar);
		System.out.println(changes.size()
				+ " Changes Found since "
				+ DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
						DateFormat.MEDIUM).format(calendar.getTime()));

		return changes.size() != 0;
	}

	@Override
	public boolean checkout(AbstractBuild build, Launcher launcher,
			FilePath workspace, BuildListener listener, File changelogFile)
			throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		try {

			Calendar calendar = Calendar.getInstance(TimeZone.getDefault());

			if (build.getProject().getLastSuccessfulBuild() != null) {
				calendar = build.getProject().getLastSuccessfulBuild()
						.getTimestamp();
			}

			List<UnityChangelog> changes = getChangeList(calendar);
			changes = logChangesDetails(changes);

			UnityChangelogSet.XMLSerializer handler = new UnityChangelogSet.XMLSerializer();
			UnityChangelogSet changeLogSet = new UnityChangelogSet(build,
					changes);
			handler.save(changeLogSet, changelogFile);

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			listener.error(e.getMessage());
			return false;
		}

	}

	@Override
	public ChangeLogParser createChangeLogParser() {
		return new UnityChangelogSet.XMLSerializer();
	}

	@Override
	public DescriptorImpl getDescriptor() {

		return (DescriptorImpl) super.getDescriptor();
	}

	private List<UnityChangelog> getChangeList(Calendar calendar) {
		List<UnityChangelog> changes = new LinkedList<UnityChangelog>();

		StringBuilder sql = new StringBuilder();

		sql.append("SELECT p.username,c.commit_time, c.serial,c.description,c.creator FROM changeset c,person p WHERE c.creator = p.serial ");
		sql.append("AND commit_time >= ? ");
		sql.append("ORDER BY commit_time DESC ");

		try {

			Connection conn = UnityDatabseUtil.getInstance().getSqlConnection(
					getDatabaseUrl(), getDatabasePort(), getInstance(),
					getUser(), getPassword());

			PreparedStatement stmt = conn.prepareStatement(sql.toString());

			stmt.setTimestamp(1, new Timestamp(calendar.getTimeInMillis()));

			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {

				UnityChangelog entry = new UnityChangelog(
						rs.getString("username"), new Date(rs.getTimestamp(
								"commit_time").getTime()),
						rs.getString("description"), rs.getInt("serial"));

				changes.add(entry);

			}

			rs.close();
			conn.close();

		} catch (InstantiationException e) {
			Logger.getLogger("UnitAssetPlugin").log(Level.SEVERE,
					e.getMessage());

		} catch (IllegalAccessException e) {

			Logger.getLogger("UnitAssetPlugin").log(Level.SEVERE,
					e.getMessage());
		} catch (ClassNotFoundException e) {

			Logger.getLogger("UnitAssetPlugin").log(Level.SEVERE,
					e.getMessage());
		} catch (SQLException e) {

			Logger.getLogger("UnitAssetPlugin").log(Level.SEVERE,
					e.getMessage());
		}

		return changes;
	}

	@Extension
	public static class DescriptorImpl extends SCMDescriptor<UnityAssetSCM> {

		public FormValidation doTestConfiguration(
				@QueryParameter("databaseUrl") final String databaseUrl,
				@QueryParameter("databasePort") final Integer databasePort,
				@QueryParameter("instance") final String instance,
				@QueryParameter("user") final String user,
				@QueryParameter("password") final String password) {

			if (databaseUrl == null || databaseUrl.length() == 0) {
				return FormValidation.error("Server Url not set");
			}

			if (instance == null || instance.length() == 0) {
				return FormValidation.error("Database Instance not set");
			}
			if (user == null || user.length() == 0) {
				return FormValidation.error("User not set");
			}
			if (password == null || password.length() == 0) {
				return FormValidation.error("Password not set");
			}

			int port = 0;
			if (databasePort != null) {
				port = databasePort;

			} else {
				port = 10733;
			}

			try {

				Connection con = UnityDatabseUtil.getInstance()
						.getSqlConnection(databaseUrl, port, instance, user,
								password);
				con.close();
			} catch (Exception e) {
				return FormValidation.error(e.getMessage());
			}

			return FormValidation.ok();

		}

		public DescriptorImpl() {
			super(UnityAssetSCM.class, null);
			load();
		}

		@Override
		public String getDisplayName() {

			return "Unity Asset Server";
		}

	}

}
