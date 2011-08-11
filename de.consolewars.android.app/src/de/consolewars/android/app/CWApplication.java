package de.consolewars.android.app;

import android.app.Application;
import android.content.res.Configuration;
import de.consolewars.android.app.db.AppDataHandler;
import de.consolewars.android.app.db.DatabaseManager;
import de.consolewars.android.app.util.HttpPoster;
import de.consolewars.api.data.AuthenticatedUser;

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
 * Application stands as a singleton for the whole app and provides access to underlying
 * functionalities.
 * 
 * @author Alexander Dridiger
 */
public class CWApplication extends Application {

	private static CWApplication singleton;

	private APICaller apiCaller;
	private AppDataHandler dataHandler;
	private HttpPoster poster;
	private AuthenticatedUser user;

	/**
	 * Singleton method.
	 * 
	 * @return CWApplication singleton
	 */
	public static CWApplication getInstance() {
		return singleton;
	}

	@Override
	public final void onCreate() {
		super.onCreate();
		singleton = this;

		initHelper();
	}

	@Override
	public final void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		dataHandler.getDatabaseManager().openDatabase();
	}

	/**
	 * Initialize helper classes like the {@link DatabaseManager}.
	 */
	private void initHelper() {
		dataHandler = new AppDataHandler(getApplicationContext());
		apiCaller = new APICaller(getApplicationContext());
		poster = new HttpPoster(getApplicationContext());
	}

	/**
	 * @return the api wrapper
	 */
	public APICaller getApiCaller() {
		return apiCaller;
	}

	/**
	 * @return data handler
	 */
	public AppDataHandler getDataHandler() {
		return dataHandler;
	}

	/**
	 * @return the poster
	 */
	public HttpPoster getHttpPoster() {
		return poster;
	}

	/**
	 * @return the poster
	 */
	public AuthenticatedUser getAuthenticatedUser() {
		if (user == null) {
			user = new AuthenticatedUser();
		}
		return user;
	}

	/**
	 * @return the poster
	 */
	public void setAuthenticatedUser(AuthenticatedUser user) {
		this.user = user;
	}
}
