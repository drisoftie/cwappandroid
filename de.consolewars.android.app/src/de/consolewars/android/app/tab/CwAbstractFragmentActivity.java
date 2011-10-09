package de.consolewars.android.app.tab;

import java.sql.SQLException;
import java.util.Date;

import roboguice.util.Ln;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import de.consolewars.android.app.view.CwPagerAdapter.FragmentProvider;
import de.consolewars.android.app.view.TitlePageIndicator;
import de.consolewars.android.app.view.TitlePageIndicator.IndicatorStyle;

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
 * Basic implementation of an {@link Activity} used to handle {@link Fragment}s within a {@link ViewPager}.
 * 
 * @author Alexander Dridiger
 */
public abstract class CwAbstractFragmentActivity extends FragmentActivity implements ViewPager.OnPageChangeListener {

	protected CwEntityManager cwEntityManager;
	protected AppDataHandler cwAppDataHandler;
	protected Dao<CwUser, Integer> cwUserDao;

	protected CwPagerAdapter adapter;
	protected FragmentProvider fragmentProvider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		cwEntityManager = CwApplication.cwEntityManager();
		cwAppDataHandler = CwApplication.cwAppDataHandler();
		cwUserDao = CwApplication.cwUserDao();

		setContentView(R.layout.fragment_pager_layout);

		// View layout = LayoutInflater.from(this).inflate(R.layout.fragment_pager_layout, null);

		adapter = new CwPagerAdapter(getSupportFragmentManager());
		adapter.setFragmentProvider(getFragmentProvider());

		ViewPager pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(adapter);

		TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.indicator);
		indicator.setViewPager(pager);
		indicator.setFooterIndicatorStyle(IndicatorStyle.Underline);

		// We set this on the indicator, NOT the pager
		indicator.setOnPageChangeListener(this);
		indicator.setCurrentItem(getInitialFragmentSelection());

		// setContentView(layout);

		((CwAbstractFragment) adapter.getFragments().get(getInitialFragmentSelection())).setStartFragment(true);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// ViewPager pager = (ViewPager) findViewById(R.id.pager);
		// pager.invalidate();
		adapter.notifyDataSetChanged();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// FIXME: Save state!
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		cwEntityManager = null;
		cwAppDataHandler = null;
		cwUserDao = null;
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
									cwUser.setLastBlogId(cwEntityManager.getNewestBlogId());
									cwUser.setLastNewsId(cwEntityManager.getNewestNewsId());
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
	}

	@Override
	public void onPageSelected(int position) {
		for (int i = 0; i < adapter.getCount(); i++) {
			if (i == position) {
				((CwAbstractFragment) adapter.getFragments().get(i)).setForeground(true);
			} else {
				((CwAbstractFragment) adapter.getFragments().get(i)).setForeground(false);
			}
		}
	}

	@Override
	public void onPageScrollStateChanged(int state) {
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		for (int i = 0; i < adapter.getCount(); i++) {
			if (((CwAbstractFragment) adapter.getFragments().get(i)).isSelected()) {
				adapter.getFragments().get(i).onOptionsItemSelected(item);
			}
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * @return
	 */
	protected abstract FragmentProvider getFragmentProvider();

	/**
	 * @return
	 */
	protected abstract int getInitialFragmentSelection();
}