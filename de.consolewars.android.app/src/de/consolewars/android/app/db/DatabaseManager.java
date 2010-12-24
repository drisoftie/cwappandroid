package de.consolewars.android.app.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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
 * Manager for the used SQLite database.
 * 
 * @author Alexander Dridiger
 */
public class DatabaseManager {

	private String userdata_table;
	private SQLiteDatabase db;
	private Context context;

	/**
	 * Constructor to get an Android {@link Context} and initializing the database.
	 * 
	 * @param context
	 *            needed for {@link SQLiteOpenHelper}
	 */
	public DatabaseManager(Context context) {
		this.context = context;
		userdata_table = context.getString(R.string.db_table_userdata_name);
		CwSQLiteOpenHelper openHelper = new CwSQLiteOpenHelper(context);
		this.db = openHelper.getWritableDatabase();
	}

	/**
	 * Convenient method to insert some user data. Every attribute is mandatory.
	 * 
	 * @param username
	 *            username (nickname) of the user; mandatory
	 * @param hashPw
	 *            password of the user as a hash; mandatory
	 * @param date
	 *            mandatory
	 * 
	 * @return the row ID of the newly inserted row, or -1 if an error occurred
	 */
	public long insertUserData(String username, String hashPw, long date) {
		ContentValues insertVal = new ContentValues();
		insertVal.put(context.getString(R.string.db_username_attribute), username);
		insertVal.put(context.getString(R.string.db_password_attribute), hashPw);
		insertVal.put(context.getString(R.string.db_date_attribute), date);
		return db.insert(userdata_table, "", insertVal);
	}

	/**
	 * Convenient method to update some user data.
	 * 
	 * @param id
	 *            the id of the entity to be updated; mandatory
	 * @param username
	 *            username (nickname) of the user; mandatory
	 * @param hashPw
	 *            password of the user as a hash; mandatory
	 * @param date
	 *            mandatory
	 * @return the number of rows affected
	 */
	public int updateUserData(int id, String username, String hashPw, long date) {
		ContentValues update = new ContentValues();
		update.put(context.getString(R.string.db_username_attribute), username);
		update.put(context.getString(R.string.db_password_attribute), hashPw);
		update.put(context.getString(R.string.db_date_attribute), date);
		return db.update(userdata_table, update, context.getString(R.string.db_wherearg_id),
				new String[] { Long.toString(id) });
	}

	/**
	 * Wrapping the query method of the actual database {@link SQLiteDatabase.query()}.
	 * 
	 * @param table
	 * @param columns
	 * @param selection
	 * @param selectionArgs
	 * @param groupBy
	 * @param having
	 * @param orderBy
	 * @return
	 */
	public Cursor fireQuery(String table, String[] columns, String selection,
			String[] selectionArgs, String groupBy, String having, String orderBy) {
		Cursor cursor = this.db.query(table, columns, selection, selectionArgs, groupBy, having,
				orderBy);
		return cursor;
	}

	/**
	 * Custom helper to handle SQLite Database creation and management.
	 * 
	 * @author Alexander Dridiger
	 */
	private class CwSQLiteOpenHelper extends SQLiteOpenHelper {

		/**
		 * Sets the Android {@link Context} to the helper.
		 * 
		 * @param context
		 *            the Android {@link Context} to set
		 */
		public CwSQLiteOpenHelper(Context context) {
			super(context, DatabaseManager.this.context.getString(R.string.db_name), null, Integer
					.valueOf(DatabaseManager.this.context.getString(R.string.db_version)));
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(context.getString(R.string.db_create_table_userdata, userdata_table));
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL(context.getString(R.string.db_drop_table, userdata_table));
			onCreate(db);
		}
	}
}
