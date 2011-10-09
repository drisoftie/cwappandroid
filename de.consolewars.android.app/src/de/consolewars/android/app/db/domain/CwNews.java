package de.consolewars.android.app.db.domain;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import de.consolewars.api.API;

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
 * Represents a persisted news and is also used as a DTO for the {@link API}.
 * 
 * @author Alexander Dridiger
 */
@DatabaseTable(tableName = "News")
public class CwNews extends CwSubject {

	@DatabaseField(columnName = "authorId")
	private int authorId;
	@DatabaseField(columnName = "category")
	private String category;
	@DatabaseField(columnName = "categoryshort")
	private String categoryShort;
	@DatabaseField(columnName = "picId")
	private int picId;

	@ForeignCollectionField()
	private ForeignCollection<CwComment> comments;
	@ForeignCollectionField()
	private ForeignCollection<CwPicture> pictures;

	@DatabaseField(persisted = false)
	private List<CwPicture> cachedPictures;
	@DatabaseField(persisted = false)
	private List<CwComment> cachedComments;

	/**
	 * Mandatory
	 */
	public CwNews() {
	}

	/**
	 * @param article
	 * @param author
	 * @param comments
	 * @param description
	 * @param mode
	 * @param subjectId
	 * @param title
	 * @param unixtime
	 * @param url
	 * @param category
	 * @param categoryShort
	 * @param picId
	 * @param pictures
	 */
	public CwNews(int authorId, String article, String author, int comments, String description, String mode, int subjectId,
			String title, int unixtime, String url, String category, String categoryShort, int picId,
			ForeignCollection<CwPicture> pictures) {
		super(article, author, comments, description, mode, subjectId, title, unixtime, url);
		this.setAuthorId(authorId);
		this.category = category;
		this.categoryShort = categoryShort;
		this.picId = picId;
		this.pictures = pictures;
	}

	/**
	 * @return the authorId
	 */
	public int getAuthorId() {
		return authorId;
	}

	/**
	 * @param authorId the authorId to set
	 */
	public void setAuthorId(int authorId) {
		this.authorId = authorId;
	}

	/**
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * @param category
	 *            the category to set
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * @return the categoryShort
	 */
	public String getCategoryShort() {
		return categoryShort;
	}

	/**
	 * @param categoryShort
	 *            the categoryShort to set
	 */
	public void setCategoryShort(String categoryShort) {
		this.categoryShort = categoryShort;
	}

	/**
	 * @return the picId
	 */
	public int getPicId() {
		return picId;
	}

	/**
	 * @param picId
	 *            the picId to set
	 */
	public void setPicId(int picId) {
		this.picId = picId;
	}

	/**
	 * @return the comments
	 */
	public ForeignCollection<CwComment> getComments() {
		return comments;
	}

	/**
	 * @param comments
	 *            the comments to set
	 */
	public void setComments(ForeignCollection<CwComment> comments) {
		this.comments = comments;
	}

	/**
	 * @return the pictures
	 */
	public ForeignCollection<CwPicture> getPictures() {
		return pictures;
	}

	/**
	 * @param pictures
	 *            the pictures to set
	 */
	public void setPictures(ForeignCollection<CwPicture> pictures) {
		this.pictures = pictures;
	}

	/**
	 * @return the cachedPictures
	 */
	public List<CwPicture> getCachedPictures() {
		return cachedPictures;
	}

	/**
	 * @param cachedPictures
	 *            the cachedPictures to set
	 */
	public void setCachedPictures(List<CwPicture> cachedPictures) {
		this.cachedPictures = cachedPictures;
	}

	/**
	 * @return the cachedComments
	 */
	public List<CwComment> getCachedComments() {
		if (cachedComments == null) {
			cachedComments = new ArrayList<CwComment>();
		}
		return cachedComments;
	}

	/**
	 * @param cachedComments
	 *            the cachedComments to set
	 */
	public void setCachedComments(List<CwComment> cachedComments) {
		this.cachedComments = cachedComments;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(getSubjectId()).toString();
	}
}
