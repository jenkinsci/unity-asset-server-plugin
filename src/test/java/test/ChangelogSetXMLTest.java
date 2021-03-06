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


package test;

import static org.junit.Assert.assertEquals;
import hudson.plugins.unityasset.scm.UnityChangelog;
import hudson.plugins.unityasset.scm.UnityChangelogSet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ChangelogSetXMLTest {

	UnityChangelogSet changeLogSet;

	@Before
	public void setupChangeLogSet() {

		List<UnityChangelog> changes = new ArrayList<UnityChangelog>();
		changes.add(new UnityChangelog("Pom Bär", new Date(), "CHANGE OMG", 1));
		changes.add(new UnityChangelog("Nils Hofmeister", new Date(),
				"CHANGE OMG", 2));
		changes.add(new UnityChangelog("Sebastian Eins", new Date(),
				"CHANGE OMG", 3));

		for (UnityChangelog change : changes) {
			for (int i = 0; i < 3; i++) {
				change.getItems().add(
						new UnityChangelog.UnityItem("Test change " + i,1));
			}
		}

		changeLogSet = new UnityChangelogSet(null, changes);
	}

	@Test
	public void testToAndFromXML() throws IOException {
		UnityChangelogSet.XMLSerializer handler = new UnityChangelogSet.XMLSerializer();
		File tmp = File.createTempFile("xstream", null);

		handler.save(changeLogSet, tmp);

		UnityChangelogSet out = handler.parse(null, tmp);

		Iterator<UnityChangelog> it = out.iterator();
		while (it.hasNext()) {

			UnityChangelog log = it.next();
			System.out.println(log.getMsg() + "\n");
			for (String str : log.getAffectedPaths()) {
				System.out.println(str + "\n");
			}
		}

		it = changeLogSet.iterator();

		System.out.println("Results ===============");

		while (it.hasNext()) {

			UnityChangelog log = it.next();
			System.out.println(log.getMsg() + "\n");
			for (String str : log.getAffectedPaths()) {
				System.out.println(str + "\n");
			}
		}
	}
}
