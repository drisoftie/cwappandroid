package de.consolewars.android.app;

import java.util.List;

import roboguice.application.RoboApplication;

import com.google.inject.Module;
import com.google.inject.Singleton;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;

import de.consolewars.android.app.db.CwSqliteOpenHelper;

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
public class CwApplication extends RoboApplication {

	private OrmLiteSqliteOpenHelper ormLiteSqliteOpenHelper;

	@Override
	public void onCreate() {
		OpenHelperManager.setOpenHelperClass(CwSqliteOpenHelper.class);
		ormLiteSqliteOpenHelper = OpenHelperManager.getHelper(this);
		getInjector().injectMembers(ormLiteSqliteOpenHelper);
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
