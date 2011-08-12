package de.consolewars.android.app;

import java.util.List;

import roboguice.application.RoboApplication;
import android.content.res.Configuration;

import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Singleton;

import de.consolewars.android.app.db.DatabaseManager;
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
 * Application stands as a singleton for the whole app and provides access to
 * underlying functionalities.
 * 
 * @author Alexander Dridiger
 */
@Singleton
public class CWApplication extends RoboApplication {

	@Inject
	private DatabaseManager databaseManager;
	private AuthenticatedUser user;

	@Override
	protected void addApplicationModules(List<Module> modules) {
		modules.add(new CWAndroidModule());
	}

	@Override
	public final void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		databaseManager.openDatabase();
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
