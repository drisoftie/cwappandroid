package de.consolewars.android.app;

import de.consolewars.android.app.db.AppDataHandler;
import de.consolewars.android.app.db.DatabaseManager;
import roboguice.config.AbstractAndroidModule;

public class CWAndroidModule extends AbstractAndroidModule {

	@Override
	protected void configure() {
		requestStaticInjection(DatabaseManager.class);
		requestStaticInjection(AppDataHandler.class);
		requestStaticInjection(SplashScreenActivity.class);
	}
}
