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
 * News class representation
 * 
 * @author cerpin (arrewk@gmail.com)
 *
 */
public class News implements IUnixtime {
	
	private String title;
	private int id;
	private String description;
	private String mode;
	private int unixtime;
	private String category;
	private String categoryshort;
	private String author;
	private Picture[] piclist;
	private int comments;
	private String url;
	private int picid;
	private String article;
	
	public final static int FILTER_MICROSOFT_ONLY = 1;
	public final static int FILTER_NINTENDO_ONLY = 2;
	public final static int FILTER_SONY_ONLY = 3;
	
	public News() {
		this.title = "";
		this.id = 0;
		this.description = "";
		this.mode = "";
		this.unixtime = 0;
		this.category = "";
		this.categoryshort = "";
		this.author = "";
		this.piclist = new Picture[0];
		this.comments = 0;
		this.url = "";
		this.picid = 0;
		this.article = "";
	}
	
	public News(String title, int id, String description, String mode,
			int unixtime, String category, String categoryshort, String author,
			Picture[] piclist, int comments, String url, int picid,String article) {
		super();
		this.title = title;
		this.id = id;
		this.description = description;
		this.mode = mode;
		this.unixtime = unixtime;
		this.category = category;
		this.categoryshort = categoryshort;
		this.author = author;
		this.piclist = piclist;
		this.comments = comments;
		this.url = url;
		this.picid = picid;
		this.article = article;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public int getUnixtime() {
		return unixtime;
	}

	public void setUnixtime(int unixtime) {
		this.unixtime = unixtime;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getCategoryshort() {
		return categoryshort;
	}

	public void setCategoryshort(String categoryshort) {
		this.categoryshort = categoryshort;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Picture[] getPiclist() {
		return piclist;
	}

	public void setPiclist(Picture[] piclist) {
		this.piclist = piclist;
	}

	public int getComments() {
		return comments;
	}

	public void setComments(int comments) {
		this.comments = comments;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getPicid() {
		return picid;
	}

	public void setPicid(int picid) {
		this.picid = picid;
	}
	
	public String getArticle() {
		return getArticle(false);
	}
	
	public String getArticle(boolean plaintext) {
		if(plaintext) return PlainTextCreator.getPlainText(article);
		return article;
	}
	
	public void setArticle(String article) {
		this.article = article;
	}
	
	public String getRelativeTime() {
		return DateUtil.timePassed(unixtime);
	}
	
	public String toString() {
		return "[" + category + "] " + title + ", Autor: " + author + " - " + getRelativeTime(); 
	}
	
	public boolean equals(Object obj) {
		if(!(obj instanceof News)) return false;
		else {
			News news = (News)obj; 
			return (this.id == news.id && this.unixtime == news.unixtime);
		}
	}
}
