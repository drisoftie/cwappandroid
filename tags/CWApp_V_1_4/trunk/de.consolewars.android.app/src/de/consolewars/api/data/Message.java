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
public class Message implements IUnixtime {

	private String title;
	private int id;
	private int tid;
	private String mode;
	private String fromusername;
	private int fromuserid;
	private String tousername;
	private int touserid;
	private String message;
	private String origmessage;
	private int unixtime;
	private boolean messageread;

	public final static int FOLDER_INBOX = 0;
	public final static int FOLDER_SENT = -1;
	public final static int READ = 1;
	public final static int UNREAD = 0;

	public Message() {
	}

	public Message(String title, int id, int tid, String mode, String fromusername, int fromuserid,
			String tousername, int touserid, String message, String origmessage, int unixtime,
			boolean messageread) {
		super();
		this.title = title;
		this.id = id;
		this.tid = tid;
		this.mode = mode;
		this.fromusername = fromusername;
		this.fromuserid = fromuserid;
		this.tousername = tousername;
		this.touserid = touserid;
		this.message = message;
		this.origmessage = origmessage;
		this.unixtime = unixtime;
		this.messageread = messageread;
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

	public int getTid() {
		return tid;
	}

	public void setTid(int tid) {
		this.tid = tid;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getFromusername() {
		return fromusername;
	}

	public void setFromusername(String fromusername) {
		this.fromusername = fromusername;
	}

	public int getFromuserid() {
		return fromuserid;
	}

	public void setFromuserid(int fromuserid) {
		this.fromuserid = fromuserid;
	}

	public String getTousername() {
		return tousername;
	}

	public void setTousername(String tousername) {
		this.tousername = tousername;
	}

	public int getTouserid() {
		return touserid;
	}

	public void setTouserid(int touserid) {
		this.touserid = touserid;
	}

	public String getMessage() {
		if (message == null) {
			message = "";
		}
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getOrigmessage() {
		return origmessage;
	}

	public void setOrigmessage(String origmessage) {
		this.origmessage = origmessage;
	}

	public int getUnixtime() {
		return unixtime;
	}

	public void setUnixtime(int unixtime) {
		this.unixtime = unixtime;
	}

	public boolean isMessageread() {
		return messageread;
	}

	public void setMessageread(boolean messageread) {
		this.messageread = messageread;
	}

	public String toString() {
		return "from:\t" + fromusername + "\nto:\t" + tousername + "\nsubject: " + title;
	}

	public String getRelativeTime() {
		return DateUtil.timePassed(unixtime);
	}

}
