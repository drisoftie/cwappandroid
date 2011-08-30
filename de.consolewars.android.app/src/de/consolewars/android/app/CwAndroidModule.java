package de.consolewars.android.app;

import roboguice.config.AbstractAndroidModule;

import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;

import de.consolewars.android.app.db.AppDataHandler;
import de.consolewars.android.app.db.DaoProvider;
import de.consolewars.android.app.db.domain.CwUser;

public class CwAndroidModule extends AbstractAndroidModule {

	private OrmLiteSqliteOpenHelper ormLiteSqliteOpenHelper;

	public CwAndroidModule(OrmLiteSqliteOpenHelper ormLiteSqliteOpenHelper) {
		super();
		this.ormLiteSqliteOpenHelper = ormLiteSqliteOpenHelper;
	}

	@Override
	protected void configure() {
		bind(OrmLiteSqliteOpenHelper.class).toInstance(ormLiteSqliteOpenHelper);

		requestStaticInjection(AppDataHandler.class);
		requestStaticInjection(SplashScreenActivity.class);

		bind(new TypeLiteral<Dao<CwUser, Integer>>() {
		}).toProvider(new DaoProvider<CwUser, Integer>(ormLiteSqliteOpenHelper.getConnectionSource(), CwUser.class))
				.in(Singleton.class);
	}
}
