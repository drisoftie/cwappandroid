package de.consolewars.android.app.db.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Transient;

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
 * 
 * @author Alexander Dridiger
 */
@Root(name = "item", strict = false)
public class CwMessage extends CwEntity {

	@DatabaseField(columnName = "fromusername")
	@Element(name = "fromusername", required = false)
	private String fromUsername;
	@DatabaseField(columnName = "fromuserid")
	@Element(name = "fromuserid", required = false)
	private int fromUserId;
	@DatabaseField(columnName = "message")
	@Element(name = "message", required = false)
	private String message;
	@DatabaseField(columnName = "messageread")
	@Transient
	private boolean messageRead;
	@Element(name = "messageread", required = false)
	private int messageReadTemp;
	@DatabaseField(columnName = "mode")
	@Element(name = "mode", required = false)
	private String mode;
	@DatabaseField(columnName = "mid")
	@Element(name = "id", required = false)
	private int mid;
	@DatabaseField(columnName = "origmessage")
	@Element(name = "origmessage", required = false)
	private String origMessage;
	@DatabaseField(columnName = "tid")
	@Element(name = "tid", required = false)
	private String tid;
	@DatabaseField(columnName = "title")
	@Element(name = "title", required = false)
	private String title;
	@DatabaseField(columnName = "tousername")
	@Element(name = "tousername", required = false)
	private String toUsername;
	@DatabaseField(columnName = "touserid")
	@Element(name = "touserid", required = false)
	private int toUserId;
	@DatabaseField(columnName = "unixtime")
	@Element(name = "unixtime", required = false)
	private int unixtime;

	/**
	 * Basic contructor. Needed for children.
	 */
	public CwMessage() {
	}

	/**
	 * @return the fromUsername
	 */
	public String getFromUsername() {
		return fromUsername;
	}

	/**
	 * @param fromUsername
	 *            the fromUsername to set
	 */
	public void setFromUsername(String fromUsername) {
		this.fromUsername = fromUsername;
	}

	/**
	 * @return the fromUserId
	 */
	public int getFromUserId() {
		return fromUserId;
	}

	/**
	 * @param fromUserId
	 *            the fromUserId to set
	 */
	public void setFromUserId(int fromUserId) {
		this.fromUserId = fromUserId;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message
	 *            the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the messageRead
	 */
	public boolean isMessageRead() {
		if (messageReadTemp == 1) {
			messageRead = true;
		} else if (messageReadTemp == 0) {
			messageRead = false;
		}
		return messageRead;
	}

	/**
	 * @param messageRead
	 *            the messageRead to set
	 */
	public void setMessageRead(boolean messageRead) {
		this.messageRead = messageRead;
	}

	/**
	 * @return the messageReadTemp
	 */
	public int getMessageReadTemp() {
		return messageReadTemp;
	}

	/**
	 * @param messageReadTemp
	 *            the messageReadTemp to set
	 */
	public void setMessageReadTemp(int messageReadTemp) {
		this.messageReadTemp = messageReadTemp;
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
	 * @return the mid
	 */
	public int getMid() {
		return mid;
	}

	/**
	 * @param mid
	 *            the mid to set
	 */
	public void setMid(int mid) {
		this.mid = mid;
	}

	/**
	 * @return the origMessage
	 */
	public String getOrigMessage() {
		return origMessage;
	}

	/**
	 * @param origMessage
	 *            the origMessage to set
	 */
	public void setOrigMessage(String origMessage) {
		this.origMessage = origMessage;
	}

	/**
	 * @return the tid
	 */
	public String getTid() {
		return tid;
	}

	/**
	 * @param tid
	 *            the tid to set
	 */
	public void setTid(String tid) {
		this.tid = tid;
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
	 * @return the toUsername
	 */
	public String getToUsername() {
		return toUsername;
	}

	/**
	 * @param toUsername
	 *            the toUsername to set
	 */
	public void setToUsername(String toUsername) {
		this.toUsername = toUsername;
	}

	/**
	 * @return the toUserId
	 */
	public int getToUserId() {
		return toUserId;
	}

	/**
	 * @param toUserId
	 *            the toUserId to set
	 */
	public void setToUserId(int toUserId) {
		this.toUserId = toUserId;
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

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(mid).append(unixtime).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!this.getClass().isInstance(obj)) {
			return false;
		}
		CwMessage other = (CwMessage) obj;
		return new EqualsBuilder().append(getMid(), other.getMid()).isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(mid).toString();
	}
}
