package de.consolewars.android.app.db.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "Video")
public class CwVideo extends CwEntity {

	@DatabaseField(columnName = "htmlEmbeddedSnippet")
	private String htmlEmbeddedSnippet;
	@DatabaseField(columnName = "url", canBeNull = false)
	private String url;

	/**
	 * Mandatory
	 */
	public CwVideo() {
	}

	public CwVideo(String htmlEmbeddedSnippet, String url) {
		super();
		this.htmlEmbeddedSnippet = htmlEmbeddedSnippet;
		this.url = url;
	}

	/**
	 * @return the htmlEmbeddedSnippet
	 */
	public String getHtmlEmbeddedSnippet() {
		return htmlEmbeddedSnippet;
	}

	/**
	 * @param htmlEmbeddedSnippet
	 *            the htmlEmbeddedSnippet to set
	 */
	public void setHtmlEmbeddedSnippet(String htmlEmbeddedSnippet) {
		this.htmlEmbeddedSnippet = htmlEmbeddedSnippet;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url
	 *            the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(getId()).append(getUrl()).toString();
	}
}
