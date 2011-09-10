package de.consolewars.android.app;

import java.util.List;

import roboguice.application.RoboApplication;

import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;

import de.consolewars.android.app.db.AppDataHandler;
import de.consolewars.android.app.db.CwSqliteOpenHelper;
import de.consolewars.android.app.db.domain.CwUser;
import de.consolewars.android.app.util.ViewUtility;

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
 * Application stands as a singleton for the whole app and provides access to underlying functionalities.
 * 
 * @author Alexander Dridiger
 */
@Singleton
public class CwApplication extends RoboApplication {

	private OrmLiteSqliteOpenHelper ormLiteSqliteOpenHelper;

	public static RoboApplication instance;

	@Inject
	public CwManager cwManager;
	@Inject
	public CwLoginManager cwLoginManager;
	@Inject
	public ViewUtility viewUtility;
	@Inject
	private AppDataHandler appDataHandler;
	@Inject
	private Dao<CwUser, Integer> cwUserDao;

	private static CwManager cwm;
	private static CwLoginManager cwlm;
	private static AppDataHandler adh;
	private static Dao<CwUser, Integer> udao;
	private static ViewUtility vu;

	public static CwManager cwManager() {
		return cwm;
	}

	public static CwLoginManager cwLoginManager() {
		return cwlm;
	}

	public static AppDataHandler cwAppDataHandler() {
		return adh;
	}

	public static Dao<CwUser, Integer> cwUserDao() {
		return udao;
	}

	public static ViewUtility cwViewUtil() {
		return vu;
	}

	@Override
	public void onCreate() {
		OpenHelperManager.setOpenHelperClass(CwSqliteOpenHelper.class);
		ormLiteSqliteOpenHelper = OpenHelperManager.getHelper(this);
		getInjector().injectMembers(ormLiteSqliteOpenHelper);
		instance = this;
		cwm = cwManager;
		cwlm = cwLoginManager;
		adh = appDataHandler;
		udao = cwUserDao;
		vu = viewUtility;
	}

	@Override
	public void onTerminate() {
		OpenHelperManager.releaseHelper();
	}

	@Override
	protected void addApplicationModules(List<Module> modules) {
		modules.add(new CwAndroidModule(ormLiteSqliteOpenHelper));
	}
}
