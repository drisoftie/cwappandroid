package de.consolewars.android.app.db.domain;

import org.apache.commons.lang.builder.ToStringBuilder;

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

	@DatabaseField(columnName = "savePicsOnSD")
	private boolean savePicsOnSD;
	@DatabaseField(columnName = "relativeSDUrl")
	private String relativeSDUrl;

	/**
	 * Mandatory
	 */
	public CwOptions() {
	}

	/**
	 * @param savePicsOnSD
	 * @param relativeSDUrl
	 */
	public CwOptions(boolean savePicsOnSD, String relativeSDUrl) {
		super();
		this.savePicsOnSD = savePicsOnSD;
		this.relativeSDUrl = relativeSDUrl;
	}

	/**
	 * @return the savePicsOnSD
	 */
	public boolean isSavePicsOnSD() {
		return savePicsOnSD;
	}

	/**
	 * @param savePicsOnSD
	 *            the savePicsOnSD to set
	 */
	public void setSavePicsOnSD(boolean savePicsOnSD) {
		this.savePicsOnSD = savePicsOnSD;
	}

	/**
	 * @return the relativeSDUrl
	 */
	public String getRelativeSDUrl() {
		return relativeSDUrl;
	}

	/**
	 * @param relativeSDUrl
	 *            the relativeSDUrl to set
	 */
	public void setRelativeSDUrl(String relativeSDUrl) {
		this.relativeSDUrl = relativeSDUrl;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(getId()).toString();
	}
}
