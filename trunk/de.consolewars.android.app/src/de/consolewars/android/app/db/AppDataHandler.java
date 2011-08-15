package de.consolewars.android.app.db;

import android.content.Context;
import android.database.Cursor;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.consolewars.android.app.R;

/*
 * Copyright [2010] [Alexander Dridiger]
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
 * Helper class providing access to app-specific data. Access to the db is also
 * leveraged by it.
 * 
 * @author Alexander Dridiger
 */
@Singleton
public class AppDataHandler {

	@Inject
	private Context context;
	@Inject
	private DatabaseManager databaseManager;

	private String userName = "";
	private String hashPw = "";
	private int cwUserId = -1;
	private int userDbId = -1;
	private long date = -1;
	private String stringDate = "";

	public boolean loadCurrentUser() {
		boolean existingUser = false;

		String tableName = getString(R.string.db_table_userdata_name);
		String columnId = getString(R.string.db_id_attribute);
		String columnUsername = getString(R.string.db_username_attribute);
		String columnPassw = getString(R.string.db_password_attribute);
		String columnDate = getString(R.string.db_date_attribute);

		databaseManager.openDatabase();
		Cursor cursor = databaseManager.fireQuery(tableName, new String[] { columnId, columnUsername, columnPassw,
				columnDate }, null, null, null, null, getString(R.string.db_id_desc));
		if (cursor.getCount() > 0 && cursor.moveToFirst()) {
			for (String columnName : cursor.getColumnNames()) {
				if (columnName.matches(columnUsername)) {
					userName = cursor.getString(cursor.getColumnIndex(columnName));
				} else if (columnName.matches(columnPassw)) {
					hashPw = cursor.getString(cursor.getColumnIndex(columnName));
				} else if (columnName.matches(columnId)) {
					userDbId = cursor.getInt(cursor.getColumnIndex(columnName));
				} else if (columnName.matches(columnDate)) {
					date = cursor.getLong(cursor.getColumnIndex(columnName));
					stringDate = cursor.getString(cursor.getColumnIndex(columnName));
				}
			}
			existingUser = true;
		}
		cursor.close();
		return existingUser;
	}

	private String getString(int id) {
		return context.getString(id);
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @return the hashPw
	 */
	public String getHashPw() {
		return hashPw;
	}

	/**
	 * @return the cwUserID
	 */
	public int getCwUserID() {
		return cwUserId;
	}

	/**
	 * @param cwUserID
	 */
	public void setCwUserID(int cwUserID) {
		this.cwUserId = cwUserID;
	}

	/**
	 * @return the id
	 */
	public int getUserDbId() {
		return userDbId;
	}

	/**
	 * @return the date
	 */
	public long getDate() {
		return date;
	}

	public String getStringDate() {
		return stringDate;
	}
}
