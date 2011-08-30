package de.consolewars.android.app.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import roboguice.util.Ln;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.j256.ormlite.dao.Dao;

import de.consolewars.android.app.db.domain.CwUser;

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
 * Helper class providing access to app-specific data. Access to the db is also
 * leveraged by it.
 * 
 * @author Alexander Dridiger
 */
@Singleton
public class AppDataHandler {

	@Inject
	private Dao<CwUser, Integer> cwUserDao;

	private CwUser cwUser;

	/**
	 * @return
	 */
	public boolean loadCurrentUser() {
		List<CwUser> cwUsers = new ArrayList<CwUser>();
		try {
			cwUsers = cwUserDao.queryForAll();
		} catch (SQLException e) {
			Ln.e(e);
		}

		if (cwUsers.size() > 0) {
			cwUser = cwUsers.get(0);
			return true;
		}

		return false;
	}

	public void loadSavedNews() {

	}

	public CwUser getCwUser() {
		return cwUser;
	}
}
