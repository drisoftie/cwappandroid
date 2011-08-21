package de.consolewars.android.app;

import java.security.GeneralSecurityException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.content.Context;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.consolewars.android.app.db.AppDataHandler;
import de.consolewars.android.app.db.DatabaseManager;
import de.consolewars.android.app.util.HashEncrypter;
import de.consolewars.api.data.AuthenticatedUser;
import de.consolewars.api.exception.ConsolewarsAPIException;

/*
 * Copyright [2011] [Alexander Dridiger]
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

@Singleton
public class CWLoginManager {

	@Inject
	private CWManager cwManager;
	@Inject
	private DatabaseManager databaseManager;
	@Inject
	private Context context;
	@Inject
	private AppDataHandler appDataHandler;

	private boolean isLoggedIn;

	private AuthenticatedUser user;

	/**
	 * @return the isLoggedIn
	 */
	public boolean isLoggedIn() {
		return isLoggedIn;
	}

	/**
	 * @return the user
	 */
	public AuthenticatedUser getUser() {
		if (user == null) {
			user = new AuthenticatedUser();
		}
		return user;
	}

	public void logoutUser() {
		user = new AuthenticatedUser();
		isLoggedIn = false;
	}

	public boolean saveAndLoginUser(String username, String passw) {
		Calendar calendar = GregorianCalendar.getInstance();
		if (appDataHandler.getUserDbId() != -1) {
			try {
				databaseManager.updateUserData(appDataHandler.getUserDbId(), username,
						HashEncrypter.encrypt(context.getString(R.string.db_cry), passw), calendar.getTimeInMillis());
			} catch (GeneralSecurityException e) {
				e.printStackTrace();
			}
		} else {
			try {
				databaseManager.insertUserData(username,
						HashEncrypter.encrypt(context.getString(R.string.db_cry), passw), calendar.getTimeInMillis());
			} catch (GeneralSecurityException e) {
				e.printStackTrace();
			}
		}
		return checkSavedUserAndLogin();
	}

	/**
	 * @return
	 */
	public boolean checkSavedUserAndLogin() {
		if (appDataHandler.loadCurrentUser()) {
			AuthenticatedUser user = null;
			try {
				user = cwManager.getAuthUser(appDataHandler.getUserName(),
						HashEncrypter.decrypt(context.getString(R.string.db_cry), appDataHandler.getHashPw()));
			} catch (ConsolewarsAPIException e) {
				e.printStackTrace();
			} catch (GeneralSecurityException e) {
				e.printStackTrace();
			}
			if (user.getSuccess().matches(context.getString(R.string.success_yes))) {
				this.user = user;
				isLoggedIn = true;
			}
		} else {
			isLoggedIn = false;
		}
		return isLoggedIn;
	}

}
