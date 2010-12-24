package de.consolewars.api.data;

import de.consolewars.api.util.DateUtil;

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
 * @author cerpin (arrewk@gmail.com) 
 */
public class Comment implements IUnixtime {
	
	private int currpage; 	
	private int pagecount;
	private String mode; 	
	private int unixtime;
	private String statement;
	private String quote;
	private String username;
	private int uid;
	private String usertitle;
	private int commentcount;
	private int postcount;
	private int cid;
	
	public final static int AREA_NEWS = 11;
	public final static int AREA_BLOGS = 101;
	
	public Comment(int currpage, int pagecount, String mode, int unixtime,
			String statement, String quote, String username, int uid,
			String usertitle, int commentcount, int postcount, int cid) {
		
		this.currpage = currpage;
		this.pagecount = pagecount;
		this.mode = mode;
		this.unixtime = unixtime;
		this.statement = statement;
		this.quote = quote;
		this.username = username;
		this.uid = uid;
		this.usertitle = usertitle;
		this.commentcount = commentcount;
		this.postcount = postcount;
		this.cid = cid;
	}

	public Comment() {
		// TODO Auto-generated constructor stub
	}

	public int getCurrpage() {
		return currpage;
	}
	public int getPagecount() {
		return pagecount;
	}
	public String getMode() {
		return mode;
	}
	public int getUnixtime() {
		return unixtime;
	}
	public String getStatement() {
		return statement;
	}
	public String getQuote() {
		return quote;
	}
	public String getUsername() {
		return username;
	}
	public int getUid() {
		return uid;
	}
	public String getUsertitle() {
		return usertitle;
	}
	public int getCommentcount() {
		return commentcount;
	}
	public int getPostcount() {
		return postcount;
	}
	public int getCid() {
		return cid;
	}
	
	public void setCurrpage(int currpage) {
		this.currpage = currpage;
	}

	public void setPagecount(int pagecount) {
		this.pagecount = pagecount;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public void setUnixtime(int unixtime) {
		this.unixtime = unixtime;
	}

	public void setStatement(String statement) {
		this.statement = statement;
	}

	public void setQuote(String quote) {
		this.quote = quote;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public void setUsertitle(String usertitle) {
		this.usertitle = usertitle;
	}

	public void setCommentcount(int commentcount) {
		this.commentcount = commentcount;
	}

	public void setPostcount(int postcount) {
		this.postcount = postcount;
	}

	public void setCid(int cid) {
		this.cid = cid;
	}

	public String toString() {
		return username + ": " + statement;
	}
	
	public String getRelativeTime() {
		return DateUtil.timePassed(unixtime);
	}
}
