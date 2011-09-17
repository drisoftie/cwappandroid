package de.consolewars.android.app;

import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.GregorianCalendar;

import android.content.Context;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.j256.ormlite.dao.Dao;

import de.consolewars.android.app.db.AppDataHandler;
import de.consolewars.android.app.db.domain.CwUser;
import de.consolewars.android.app.util.HashEncrypter;
import de.consolewars.api.data.AuthenticatedUser;

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
/**
 * @author Alexander Dridiger
 *
 */
@Singleton
public class CwLoginManager {

	@Inject
	private CwManager cwManager;
	@Inject
	private Context context;
	@Inject
	private AppDataHandler appDataHandler;
	@Inject
	private Dao<CwUser, Integer> cwUserDao;

	private boolean isLoggedIn = false;
	private AuthenticatedUser user;

	public boolean saveAndLoginUser(String userName, String passw, int lastNewsId, int lastBlogId) {
		CwUser cwUser;
		if (appDataHandler.getCwUser() != null) {
			cwUser = appDataHandler.getCwUser();
		} else {
			cwUser = new CwUser();
		}
		cwUser.setName(userName);
		try {
			cwUser.setHashPassword(HashEncrypter.encrypt(context.getString(R.string.db_cry), passw));
		} catch (GeneralSecurityException e1) {
			e1.printStackTrace();
		}
		cwUser.setDate(GregorianCalendar.getInstance().getTime());
		cwUser.setLastBlogId(lastBlogId);
		cwUser.setLastNewsId(lastNewsId);
		try {
			cwUserDao.createOrUpdate(cwUser);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return checkSavedUserAndLogin();
	}

	/**
	 * @return
	 */
	public boolean checkSavedUserAndLogin() {
		if (appDataHandler.loadCurrentUser()) {
			user = new AuthenticatedUser();
			try {
				user = cwManager.getAuthUser(appDataHandler.getCwUser().getName(), HashEncrypter.decrypt(
						context.getString(R.string.db_cry), appDataHandler.getCwUser().getHashPassword()));
			} catch (GeneralSecurityException e) {
				isLoggedIn = false;
				e.printStackTrace();
			}
			if (user != null && context.getString(R.string.success_yes).equals(user.getSuccess())
					&& user.getUsername().length() > 0) {
				isLoggedIn = true;
			} else {
				isLoggedIn = false;
			}
		} else {
			isLoggedIn = false;
		}
		return isLoggedIn;
	}

	/**
	 * @return the isLoggedIn
	 */
	public boolean isLoggedIn() {
		return isLoggedIn;
	}

	public AuthenticatedUser getAuthenticatedUser() {
		if (user == null) {
			user = new AuthenticatedUser();
		}
		return user;
	}

	public void logoutUser() {
		user = new AuthenticatedUser();
		isLoggedIn = false;
	}
}
