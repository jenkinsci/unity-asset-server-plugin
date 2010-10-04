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
