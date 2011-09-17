package de.consolewars.android.app.db.domain;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "Picture")
public class CwPicture extends CwEntity {

	@DatabaseField(columnName = "picId")
	private String picId;
	@DatabaseField(columnName = "saved")
	private boolean saved;
	@DatabaseField(columnName = "storageUrl")
	private String storageUrl;
	@DatabaseField(columnName = "url", canBeNull = false)
	private String url;

	@DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "news_id")
	private CwNews cwNews;

	/**
	 * Mandatory
	 */
	public CwPicture() {
	}

	public CwPicture(String picId, boolean saved, String storageUrl, String url) {
		this.picId = picId;
		this.saved = saved;
		this.setStorageUrl(storageUrl);
		this.url = url;
	}

	/**
	 * @return the picId
	 */
	public String getPicId() {
		return picId;
	}

	/**
	 * @param picId
	 *            the picId to set
	 */
	public void setPicId(String picId) {
		this.picId = picId;
	}

	/**
	 * @return the saved
	 */
	public boolean isSaved() {
		return saved;
	}

	/**
	 * @param saved
	 *            the saved to set
	 */
	public void setSaved(boolean saved) {
		this.saved = saved;
	}

	/**
	 * @return the storageUrl
	 */
	public String getStorageUrl() {
		return storageUrl;
	}

	/**
	 * @param storageUrl
	 *            the storageUrl to set
	 */
	public void setStorageUrl(String storageUrl) {
		this.storageUrl = storageUrl;
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

	/**
	 * @return the cwNews
	 */
	public CwNews getCwNews() {
		return cwNews;
	}

	/**
	 * @param cwNews
	 *            the cwNews to set
	 */
	public void setCwNews(CwNews cwNews) {
		this.cwNews = cwNews;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(getId()).append(getUrl()).toString();
	}
}
