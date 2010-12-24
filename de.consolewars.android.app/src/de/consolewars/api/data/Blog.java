package de.consolewars.api.data;

import de.consolewars.api.util.DateUtil;
import de.consolewars.api.util.PlainTextCreator;

/*
 * Copyright [2009] Dimitrios Kapanikis
 *
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
 * 
 */

/**
 * class representation of a blog
 * 
 * @author cerpin (arrewk@gmail.com)
 */
public class Blog implements IUnixtime {
	
	private String title;
	private int id;
	private String description;
	private String mode;
	private int unixtime;
	private boolean visible;
	private float rating;
	private String author;
	private int uid;
	private int comments;
	private String url;
	private String article;
	
	public static final int FILTER_NORMAL = 0;
	public static final int FILTER_NEWS = 1;
	public static final int FILTER_UID = 2;
	
	public Blog(String title,int id, String description, String mode, int unixtime,
				boolean visible, float rating, String author, int uid, int comments,
				String url, String article) {
		this.title = title;
		this.id = id;
		this.description = description;
		this.mode = mode;
		this.unixtime = unixtime;
		this.visible = visible;
		this.rating = rating;
		this.author = author;
		this.uid = uid;
		this.comments = comments;
		this.url = url;
		this.article = article;
	}
	
	public Blog(String title,int id, String description, String mode, int unixtime,
			boolean visible, float rating, String author, int uid, int comments,
			String url) {
		this(title,id,description,mode,unixtime,visible,
				rating,author,uid,comments,url,"");
	}
	
	public Blog() {
		// TODO Auto-generated constructor stub
	}

	public String getTitle() {
		return title;
	}
	public int getId() {
		return id;
	}
	public String getDescription() {
		return description;
	}
	public String getMode() {
		return mode;
	}
	public int getUnixtime() {
		return unixtime;
	}
	public float getRating() {
		return rating;
	}
	public String getAuthor() {
		return author;
	}
	public int getUid() {
		return uid;
	}
	public int getComments() {
		return comments;
	}
	public String getUrl() {
		return url;
	}
	public String getArticle() {
		return getArticle(false);
	}
	
	public String getArticle(boolean plaintext) {
		if(plaintext) return PlainTextCreator.getPlainText(article);
		return article;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public void setUnixtime(int unixtime) {
		this.unixtime = unixtime;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public void setRating(float rating) {
		this.rating = rating;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public void setComments(int comments) {
		this.comments = comments;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setArticle(String article) {
		this.article = article;
	}

	public String toString() {
		return author + ": " + title + " - " + getRelativeTime();
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	public String getRelativeTime() {
		return DateUtil.timePassed(unixtime);
	}
	
	public boolean equals(Object obj) {
		if(!(obj instanceof Blog)) return false;
		else {
			Blog blog = (Blog)obj; 
			return (this.id == blog.id && this.unixtime == blog.unixtime);
		}
	}

}
