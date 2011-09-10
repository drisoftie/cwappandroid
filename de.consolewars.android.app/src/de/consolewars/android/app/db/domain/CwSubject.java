package de.consolewars.android.app.db.domain;

import org.apache.commons.lang.builder.ToStringBuilder;

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
public abstract class CwSubject extends CwEntity {

	@DatabaseField(columnName = "article")
	private String article;
	@DatabaseField(columnName = "author")
	private String author;
	@DatabaseField(columnName = "comments")
	private int comments;
	@DatabaseField(columnName = "description")
	private String description;
	@DatabaseField(columnName = "mode")
	private String mode;
	@DatabaseField(columnName = "subjectId")
	private int subjectId;
	@DatabaseField(columnName = "title")
	private String title;
	@DatabaseField(columnName = "unixtime")
	private int unixtime;
	@DatabaseField(columnName = "url")
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
	 * @param comments
	 * @param description
	 * @param mode
	 * @param subjectId
	 * @param title
	 * @param unixtime
	 * @param url
	 */
	public CwSubject(String article, String author, int comments, String description, String mode, int subjectId,
			String title, int unixtime, String url) {
		super();
		this.article = article;
		this.author = author;
		this.comments = comments;
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
	 * @return the comments
	 */
	public int getComments() {
		return comments;
	}

	/**
	 * @param comments
	 *            the comments to set
	 */
	public void setComments(int comments) {
		this.comments = comments;
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
	public String toString() {
		return new ToStringBuilder(this).append(getId()).append(getTitle()).toString();
	}
}
