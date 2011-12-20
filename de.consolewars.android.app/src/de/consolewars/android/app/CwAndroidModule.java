package de.consolewars.android.app;

import roboguice.config.AbstractAndroidModule;

import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;

import de.consolewars.android.app.db.AppDataHandler;
import de.consolewars.android.app.db.DaoProvider;
import de.consolewars.android.app.db.domain.CwBlog;
import de.consolewars.android.app.db.domain.CwComment;
import de.consolewars.android.app.db.domain.CwNews;
import de.consolewars.android.app.db.domain.CwOptions;
import de.consolewars.android.app.db.domain.CwPicture;
import de.consolewars.android.app.db.domain.CwUser;
import de.consolewars.android.app.db.domain.CwVideo;

public class CwAndroidModule extends AbstractAndroidModule {

	private OrmLiteSqliteOpenHelper ormLiteSqliteOpenHelper;

	public CwAndroidModule(OrmLiteSqliteOpenHelper ormLiteSqliteOpenHelper) {
		super();
		this.ormLiteSqliteOpenHelper = ormLiteSqliteOpenHelper;
	}

	@Override
	protected void configure() {
		bind(OrmLiteSqliteOpenHelper.class).toInstance(ormLiteSqliteOpenHelper);

		requestStaticInjection(AppDataHandler.class, SplashScreenActivity.class);

		bind(new TypeLiteral<Dao<CwUser, Integer>>() {
		}).toProvider(new DaoProvider<CwUser, Integer>(ormLiteSqliteOpenHelper.getConnectionSource(), CwUser.class))
				.in(Singleton.class);
		bind(new TypeLiteral<Dao<CwNews, Integer>>() {
		}).toProvider(new DaoProvider<CwNews, Integer>(ormLiteSqliteOpenHelper.getConnectionSource(), CwNews.class))
				.in(Singleton.class);
		bind(new TypeLiteral<Dao<CwBlog, Integer>>() {
		}).toProvider(new DaoProvider<CwBlog, Integer>(ormLiteSqliteOpenHelper.getConnectionSource(), CwBlog.class))
				.in(Singleton.class);
		bind(new TypeLiteral<Dao<CwComment, Integer>>() {
		}).toProvider(
				new DaoProvider<CwComment, Integer>(ormLiteSqliteOpenHelper.getConnectionSource(), CwComment.class))
				.in(Singleton.class);
		bind(new TypeLiteral<Dao<CwPicture, Integer>>() {
		}).toProvider(
				new DaoProvider<CwPicture, Integer>(ormLiteSqliteOpenHelper.getConnectionSource(), CwPicture.class))
				.in(Singleton.class);
		bind(new TypeLiteral<Dao<CwVideo, Integer>>() {
		}).toProvider(new DaoProvider<CwVideo, Integer>(ormLiteSqliteOpenHelper.getConnectionSource(), CwVideo.class))
				.in(Singleton.class);
		bind(new TypeLiteral<Dao<CwOptions, Integer>>() {
		}).toProvider(
				new DaoProvider<CwOptions, Integer>(ormLiteSqliteOpenHelper.getConnectionSource(), CwOptions.class))
				.in(Singleton.class);
	}
}
