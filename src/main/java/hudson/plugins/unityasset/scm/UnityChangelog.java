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

import hudson.model.User;
import hudson.scm.ChangeLogSet.AffectedFile;
import hudson.scm.ChangeLogSet.Entry;
import hudson.scm.ChangeLogSet.AffectedFile;
import hudson.scm.EditType;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean(defaultVisibility = 999)
public class UnityChangelog extends Entry implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String author;
	private Date date;
	private String message;
	private Integer serial;
	private List<UnityItem> items = new LinkedList<UnityItem>();

	public UnityChangelog() {
		author = "";
		date = new Date();
		message = "";
	}

	public UnityChangelog(String author, Date date, String message,
			Integer serial) {
		super();
		this.author = author;
		this.date = date;
		this.message = message;
		this.serial = serial;
	}

	public List<UnityItem> getItems() {
		return items;
	}

	public void setItems(List<UnityItem> items) {
		this.items = items;
	}

	public Integer getSerial() {
		return serial;
	}

	@Override
	public String getMsg() {

		return DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
				DateFormat.MEDIUM).format(date)
				+ " - " + message;

	}

	@Override
	public User getAuthor() {

		return User.get(author);
	}

	@Override
	public Collection<? extends AffectedFile> getAffectedFiles() {

		return items;
	}

	@Override
	public Collection<String> getAffectedPaths() {

		ArrayList<String> paths = new ArrayList<String>();

		for (UnityItem item : items) {
			paths.add(item.getPath());
		}

		return paths;
	}

	public static class UnityItem implements AffectedFile {

		private Integer action;
		private String message;

		public UnityItem(String message, Integer action) {
			super();
			this.action = action;
			this.message = message;
		}

		public String getPath() {

			return message;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		@Exported
		public EditType getEditType() {
			if (action == 1) {
				return EditType.ADD;
			}
			if (action == 0) {
				return EditType.EDIT;
			}

			if (action == 2) {
				return EditType.DELETE;
			}
			return EditType.EDIT;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((author == null) ? 0 : author.hashCode());
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UnityChangelog other = (UnityChangelog) obj;
		if (author == null) {
			if (other.author != null)
				return false;
		} else if (!author.equals(other.author))
			return false;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		return true;
	}

}