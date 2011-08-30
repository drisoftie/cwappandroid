package de.consolewars.android.app.tab;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import roboguice.activity.RoboActivityGroup;
import roboguice.util.Ln;
import android.app.Activity;
import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.View;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import de.consolewars.android.app.CwManager;
import de.consolewars.android.app.R;
import de.consolewars.android.app.db.AppDataHandler;
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
 * Basic implementation of an {@link ActivityGroup} supporting {@link Activity}
 * switching and {@link View} caching.
 * 
 * @author Alexander Dridiger
 */
public abstract class CwBasicActivityGroup extends RoboActivityGroup implements ICwActivityGroup {

	@Inject
	private CwManager cwManager;
	@Inject
	private AppDataHandler appDataHandler;
	@Inject
	private Dao<CwUser, Integer> cwUserDao;

	private List<View> viewCache;

	public void replaceView(View view) {
		if (viewCache == null) {
			viewCache = new ArrayList<View>();
		}
		viewCache.add(view);
		setContentView(view);
	}

	public void back() {
		if (viewCache.size() > 1) {
			viewCache.remove(viewCache.size() - 1);
			setContentView(viewCache.get(viewCache.size() - 1));
		} else {
			AlertDialog.Builder dialog = new AlertDialog.Builder(this).setMessage(getString(R.string.close_app))
					.setCancelable(false).setPositiveButton(getString(R.string.yes), new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (getParent() instanceof CwNavigationMainTabActivity) {
								if (appDataHandler.loadCurrentUser()) {
									CwUser cwUser = appDataHandler.getCwUser();
									cwUser.setDate(new Date());
									cwUser.setLastBlogId(cwManager.getNewestBlog());
									cwUser.setLastNewsId(cwManager.getNewestNews());
									try {
										cwUserDao.update(cwUser);
									} catch (SQLException e) {
										Ln.e(e);
									}
								}
							}
							finish();
						}
					}).setNegativeButton(getString(R.string.no), new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
			dialog.create().show();
		}
	}

	/**
	 * Clears the View cache.
	 */
	public void resetCache() {
		viewCache = new ArrayList<View>();
	}

	@Override
	public void onBackPressed() {
		this.back();
	}

	public CwNavigationMainTabActivity getMainTabActivity() {
		if (getParent() instanceof CwNavigationMainTabActivity) {
			return (CwNavigationMainTabActivity) getParent();
		}
		return null;
	}
}
