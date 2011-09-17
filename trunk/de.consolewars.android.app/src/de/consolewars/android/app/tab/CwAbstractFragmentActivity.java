package de.consolewars.android.app.tab;

import java.sql.SQLException;
import java.util.Date;

import roboguice.util.Ln;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import com.j256.ormlite.dao.Dao;

import de.consolewars.android.app.CwApplication;
import de.consolewars.android.app.CwEntityManager;
import de.consolewars.android.app.R;
import de.consolewars.android.app.db.AppDataHandler;
import de.consolewars.android.app.db.domain.CwUser;
import de.consolewars.android.app.view.CwPagerAdapter;

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
 */
public abstract class CwAbstractFragmentActivity extends FragmentActivity implements ViewPager.OnPageChangeListener {

	protected CwEntityManager cwEntityManager;
	protected AppDataHandler cwAppDataHandler;
	protected Dao<CwUser, Integer> cwUserDao;

	protected CwPagerAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		cwEntityManager = CwApplication.cwEntityManager();
		cwAppDataHandler = CwApplication.cwAppDataHandler();
		cwUserDao = CwApplication.cwUserDao();
	}

	@Override
	public void onBackPressed() {
		if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
			AlertDialog.Builder dialog = new AlertDialog.Builder(this).setMessage(getString(R.string.close_app))
					.setCancelable(false).setPositiveButton(getString(R.string.yes), new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (getParent() instanceof CwNavigationMainTabActivity) {
								if (cwAppDataHandler.loadCurrentUser()) {
									CwUser cwUser = cwAppDataHandler.getCwUser();
									cwUser.setDate(new Date());
									cwUser.setLastBlogId(cwEntityManager.getNewestBlog());
									cwUser.setLastNewsId(cwEntityManager.getNewestNews());
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
		} else {
			getSupportFragmentManager().popBackStack();
		}
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		for (int i = 0; i < adapter.getCount(); i++) {
			if (i == position) {
				((CwAbstractFragment) adapter.getItem(i)).setSelected(true);
			} else {
				((CwAbstractFragment) adapter.getItem(i)).setSelected(false);
			}
		}
	}

	@Override
	public void onPageSelected(int position) {
	}

	@Override
	public void onPageScrollStateChanged(int state) {
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		for (int i = 0; i < adapter.getCount(); i++) {
			if (((CwAbstractFragment) adapter.getItem(i)).isSelected()) {
				adapter.getItem(i).onOptionsItemSelected(item);
			}
		}
		return super.onOptionsItemSelected(item);
	}
}