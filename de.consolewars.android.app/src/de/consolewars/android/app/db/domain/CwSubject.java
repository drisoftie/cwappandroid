package de.consolewars.android.app.db.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import com.j256.ormlite.field.DatabaseField;

/*
 * Copyright [2011] [Alexander Dridiger]
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Same attributes of {@link CwNews} and {@link CwBlog} are unified in this abstract class.
 * 
 * @author Alexander Dridiger
 */
@Root(name = "item", strict = false)
public abstract class CwSubject extends CwEntity {

	@DatabaseField(columnName = "article")
	@Element(name = "article", required = false)
	private String article;
	@DatabaseField(columnName = "author")
	@Element(name = "author", required = false)
	private String author;
	@DatabaseField(columnName = "commentsAmount")
	@Element(name = "comments", required = false)
	private int commentsAmount;
	@DatabaseField(columnName = "description")
	@Element(name = "description", required = false)
	private String description;
	@DatabaseField(columnName = "mode")
	@Element(name = "mode", required = false)
	private String mode;
	@DatabaseField(columnName = "subjectId")
	@Element(name = "id", required = false)
	private int subjectId;
	@DatabaseField(columnName = "title")
	@Element(name = "title", required = false)
	private String title;
	@DatabaseField(columnName = "unixtime")
	@Element(name = "unixtime", required = false)
	private int unixtime;
	@DatabaseField(columnName = "url")
	@Element(name = "url", required = false)
	private String url;

	/**
	 * Basic contructor. Needed for children.
	 */
	public CwSubject() {
	}

	/**
	 * Only used for delegation by its children.
	 * 
	 * @param article
	 * @param author
	 * @param commentsAmount
	 * @param description
	 * @param mode
	 * @param subjectId
	 * @param title
	 * @param unixtime
	 * @param url
	 */
	public CwSubject(String article, String author, int commentsAmount, String description, String mode, int subjectId,
			String title, int unixtime, String url) {
		super();
		this.article = article;
		this.author = author;
		this.commentsAmount = commentsAmount;
		this.description = description;
		this.mode = mode;
		this.subjectId = subjectId;
		this.title = title;
		this.unixtime = unixtime;
		this.url = url;
	}

	/**
	 * @return the article
	 */
	public String getArticle() {
		return article;
	}

	/**
	 * @param article
	 *            the article to set
	 */
	public void setArticle(String article) {
		this.article = article;
	}

	/**
	 * @return the author
	 */
	public String getAuthor() {
		if (author == null) {
			author = "";
		}
		return author;
	}

	/**
	 * @param author
	 *            the author to set
	 */
	public void setAuthor(String author) {
		this.author = author;
	}

	/**
	 * @return the commentsAmount
	 */
	public int getCommentsAmount() {
		return commentsAmount;
	}

	/**
	 * @param commentsAmount
	 *            the commentsAmount to set
	 */
	public void setCommentsAmount(int commentsAmount) {
		this.commentsAmount = commentsAmount;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the mode
	 */
	public String getMode() {
		return mode;
	}

	/**
	 * @param mode
	 *            the mode to set
	 */
	public void setMode(String mode) {
		this.mode = mode;
	}

	/**
	 * @return the subjectId
	 */
	public int getSubjectId() {
		return subjectId;
	}

	/**
	 * @param subjectId
	 *            the subjectId to set
	 */
	public void setSubjectId(int subjectId) {
		this.subjectId = subjectId;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the unixtime
	 */
	public int getUnixtime() {
		return unixtime;
	}

	/**
	 * @param unixtime
	 *            the unixtime to set
	 */
	public void setUnixtime(int unixtime) {
		this.unixtime = unixtime;
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
	public int hashCode() {
		return new HashCodeBuilder().append(subjectId).append(unixtime).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!this.getClass().isInstance(obj)) {
			return false;
		}
		CwSubject other = (CwSubject) obj;
		return new EqualsBuilder().append(getSubjectId(), other.getSubjectId()).isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(getSubjectId()).toString();
	}
}
