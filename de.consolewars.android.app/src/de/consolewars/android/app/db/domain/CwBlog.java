package de.consolewars.android.app.db.domain;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Transient;

import com.j256.ormlite.field.DatabaseField;
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
 * Represents a persisted blog and is also used as a DTO for the {@link API}.
 * 
 * @author Alexander Dridiger
 */
@DatabaseTable(tableName = "Blog")
public class CwBlog extends CwSubject {

	@DatabaseField(columnName = "rating")
	@Element(name = "rating", required = false)
	private float rating;
	@DatabaseField(columnName = "uid")
	@Element(name = "uid", required = false)
	private int uid;
	@DatabaseField(columnName = "visible")
	@Transient
	private boolean visible;
	@Element(name = "visible", required = false)
	private int visibleTemp;

	/**
	 * Mandatory
	 */
	public CwBlog() {
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
	 * @param rating
	 * @param uid
	 * @param visible
	 * @param visibleTemp
	 */
	public CwBlog(String article, String author, int comments, String description, String mode, int subjectId,
			String title, int unixtime, String url, float rating, int uid, boolean visible) {
		super(article, author, comments, description, mode, subjectId, title, unixtime, url);
		this.rating = rating;
		this.uid = uid;
		this.visible = visible;
	}

	/**
	 * @return the rating
	 */
	public float getRating() {
		return rating;
	}

	/**
	 * @param rating
	 *            the rating to set
	 */
	public void setRating(float rating) {
		this.rating = rating;
	}

	/**
	 * @return the uid
	 */
	public int getUid() {
		return uid;
	}

	/**
	 * @param uid
	 *            the uid to set
	 */
	public void setUid(int uid) {
		this.uid = uid;
	}

	/**
	 * @return the visible
	 */
	public boolean isVisible() {
		if (visibleTemp == 1) {
			visible = true;
		} else if (visibleTemp == 0) {
			visible = false;
		}
		return visible;
	}

	/**
	 * @param visible
	 *            the visible to set
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * @return the visibleTemp
	 */
	public int getVisibleTemp() {
		return visibleTemp;
	}

	/**
	 * @param visibleTemp
	 *            the visibleTemp to set
	 */
	public void setVisibleTemp(int visibleTemp) {
		this.visibleTemp = visibleTemp;
	}
}
