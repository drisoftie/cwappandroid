package de.consolewars.android.app.db.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

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
 * Options made by a user.
 * 
 * @author Alexander Dridiger
 */
@DatabaseTable(tableName = "Options")
public class CwOptions extends CwEntity {

	@DatabaseField(columnName = "savePicsOnSdCard")
	private boolean savePicsOnSdCard;

	@DatabaseField(columnName = "relativeSdCardUrl")
	private String relativeSdCardUrl;

	@DatabaseField(columnName = "maxNewsScroll")
	private int maxNewsScroll;

	@DatabaseField(columnName = "maxNewsAction")
	private int maxNewsAction;

	@DatabaseField(columnName = "maxBlogsScroll")
	private int maxBlogsScroll;

	@DatabaseField(columnName = "maxBlogsAction")
	private int maxBlogsAction;

	@DatabaseField(columnName = "maxCmts")
	private int maxCmts;

	/**
	 * Mandatory
	 */
	public CwOptions() {
	}

	public boolean isSavePicsOnSdCard() {
		return savePicsOnSdCard;
	}

	public void setSavePicsOnSdCard(boolean savePicsOnSdCard) {
		this.savePicsOnSdCard = savePicsOnSdCard;
	}

	public String getRelativeSdCardUrl() {
		return relativeSdCardUrl;
	}

	public void setRelativeSdCardUrl(String relativeSdCardUrl) {
		this.relativeSdCardUrl = relativeSdCardUrl;
	}

	public int getMaxNewsScroll() {
		return maxNewsScroll;
	}

	public void setMaxNewsScroll(int maxNewsScroll) {
		this.maxNewsScroll = maxNewsScroll;
	}

	public int getMaxNewsAction() {
		return maxNewsAction;
	}

	public void setMaxNewsAction(int maxNewsAction) {
		this.maxNewsAction = maxNewsAction;
	}

	public int getMaxBlogsScroll() {
		return maxBlogsScroll;
	}

	public void setMaxBlogsScroll(int maxBlogsScroll) {
		this.maxBlogsScroll = maxBlogsScroll;
	}

	public int getMaxBlogsAction() {
		return maxBlogsAction;
	}

	public void setMaxBlogsAction(int maxBlogsAction) {
		this.maxBlogsAction = maxBlogsAction;
	}

	public int getMaxCmts() {
		return maxCmts;
	}

	public void setMaxCmts(int maxCmts) {
		this.maxCmts = maxCmts;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(getId()).toString();
	}
}
