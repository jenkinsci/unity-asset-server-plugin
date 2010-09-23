package hudson.plugins.unityasset.scm;

import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogParser;
import hudson.scm.ChangeLogSet;
import hudson.util.XStream2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;

public class UnityChangelogSet extends ChangeLogSet<UnityChangelog> {

	private List<UnityChangelog> logs;

	public UnityChangelogSet(AbstractBuild build, List<UnityChangelog> changes) {
		super(build);
		logs = new ArrayList<UnityChangelog>();

		logs.addAll(changes);
	}

	@Override
	public String getKind() {
		return "unity_scm";
	}

	@Override
	public boolean isEmptySet() {
		return logs.isEmpty();
	}

	public Iterator<UnityChangelog> iterator() {
		return Collections.unmodifiableList(logs).iterator();
	}

	public static class XMLSerializer extends ChangeLogParser {
		private transient XStream2 xstream;

		private Object readResolve() { // xstream field used to be serialized in
										// build.xml
			initXStream();
			return this;
		}

		public XMLSerializer() {
			initXStream();
		}

		private void initXStream() {
			xstream = new XStream2();
			xstream.alias("log", UnityChangelogSet.class);
			xstream.alias("changelog", UnityChangelog.class);

			xstream.omitField(ChangeLogSet.class, "build");

		}

		public UnityChangelogSet parse(AbstractBuild build, java.io.File file)
				throws FileNotFoundException {
			FileInputStream in = null;
			UnityChangelogSet out = null;
			try {
				in = new FileInputStream(file);
				out = (UnityChangelogSet) xstream.fromXML(in);
			} catch (Exception e) {
				e.printStackTrace();
			}

			finally {
				IOUtils.closeQuietly(in);
			}
			return out;
		}

		public void save(UnityChangelogSet changeLogSet, File file)
				throws FileNotFoundException {
			FileOutputStream out = null;
			try {
				out = new FileOutputStream(file);
				xstream.toXML(changeLogSet, out);
			} finally {
				IOUtils.closeQuietly(out);
			}
		}
	}
}
