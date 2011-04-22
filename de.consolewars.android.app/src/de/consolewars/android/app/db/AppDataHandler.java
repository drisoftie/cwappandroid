package de.consolewars.android.app.db;

import android.content.Context;
import android.database.Cursor;
import de.consolewars.android.app.R;

public class AppDataHandler {

	private Context context;
	private DatabaseManager db;

	private String userName = "";
	private String hashPw = "";
	private int cwUserID = -1;
	private int id = -1;
	private long date = -1;
	private String stringDate = "";

	public AppDataHandler(Context applicationContext) {
		context = applicationContext;
		db = new DatabaseManager(applicationContext);
	}

	public DatabaseManager getDatabaseManager() {
		return db;
	}

	public boolean loadCurrentUser() {
		boolean existingUser = false;

		String tableName = getString(R.string.db_table_userdata_name);
		String columnId = getString(R.string.db_id_attribute);
		String columnUsername = getString(R.string.db_username_attribute);
		String columnPassw = getString(R.string.db_password_attribute);
		String columnDate = getString(R.string.db_date_attribute);

		Cursor cursor = db.fireQuery(tableName, new String[] { columnId, columnUsername,
				columnPassw, columnDate }, null, null, null, null, getString(R.string.db_id_desc));
		if (cursor.getCount() > 0 && cursor.moveToFirst()) {
			for (String columnName : cursor.getColumnNames()) {
				if (columnName.matches(columnUsername)) {
					userName = cursor.getString(cursor.getColumnIndex(columnName));
				} else if (columnName.matches(columnPassw)) {
					hashPw = cursor.getString(cursor.getColumnIndex(columnName));
				} else if (columnName.matches(columnId)) {
					id = cursor.getInt(cursor.getColumnIndex(columnName));
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
		return cwUserID;
	}

	/**
	 * @param cwUserID
	 */
	public void setCwUserID(int cwUserID) {
		this.cwUserID = cwUserID;
	}

	/**
	 * @return the id
	 */
	public int getUserDBId() {
		return id;
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
