package de.consolewars.android.app.db;

import java.sql.SQLException;

import roboguice.util.Ln;

import com.google.inject.Provider;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

import de.consolewars.android.app.db.domain.CwEntity;

public class DaoProvider<T extends CwEntity, ID> implements Provider<Dao<T, ID>> {
	private ConnectionSource conn;
	private Class<T> clazz;

	public DaoProvider(ConnectionSource conn, Class<T> clazz) {
		this.conn = conn;
		this.clazz = clazz;
	}

	@Override
	public Dao<T, ID> get() {
		try {
			return DaoManager.createDao(conn, clazz);
		} catch (SQLException e) {
			Ln.e(e);
		}
		return null;
	}
}
