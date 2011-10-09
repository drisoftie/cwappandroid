package de.consolewars.android.app.db;

import java.sql.SQLException;

import roboguice.util.Ln;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.google.inject.Inject;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import de.consolewars.android.app.R;
import de.consolewars.android.app.db.domain.CwBlog;
import de.consolewars.android.app.db.domain.CwComment;
import de.consolewars.android.app.db.domain.CwNews;
import de.consolewars.android.app.db.domain.CwPicture;
import de.consolewars.android.app.db.domain.CwUser;
import de.consolewars.android.app.db.domain.CwVideo;

/**
 * Custom helper to handle SQLite Database creation and management.
 * 
 * @author Alexander Dridiger
 */
public class CwSqliteOpenHelper extends OrmLiteSqliteOpenHelper {

	/**
	 * Sets the Android {@link Context} to the helper.
	 * 
	 * @param context
	 *            the Android {@link Context} to set
	 */
	@Inject
	public CwSqliteOpenHelper(Context context) {
		super(context, context.getString(R.string.db_name), null, Integer.valueOf(context
				.getString(R.string.db_version)));
	}

	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			TableUtils.createTable(connectionSource, CwUser.class);
			TableUtils.createTable(connectionSource, CwNews.class);
			TableUtils.createTable(connectionSource, CwBlog.class);
			TableUtils.createTable(connectionSource, CwComment.class);
			TableUtils.createTable(connectionSource, CwPicture.class);
			TableUtils.createTable(connectionSource, CwVideo.class);
		} catch (SQLException e) {
			Ln.e(e);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		try {
			TableUtils.dropTable(connectionSource, CwUser.class, true);
			TableUtils.dropTable(connectionSource, CwNews.class, true);
			TableUtils.dropTable(connectionSource, CwBlog.class, true);
			TableUtils.dropTable(connectionSource, CwComment.class, true);
			TableUtils.dropTable(connectionSource, CwPicture.class, true);
			TableUtils.dropTable(connectionSource, CwVideo.class, true);
			onCreate(db);
		} catch (SQLException e) {
			Ln.e(e);
		}
	}
}