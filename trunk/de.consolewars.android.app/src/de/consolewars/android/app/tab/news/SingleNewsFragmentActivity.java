package de.consolewars.android.app.tab.news;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import de.consolewars.android.app.CwApplication;
import de.consolewars.android.app.R;
import de.consolewars.android.app.pics.PicsFragment;
import de.consolewars.android.app.tab.CwAbstractFragment;
import de.consolewars.android.app.tab.CwAbstractFragmentActivity;
import de.consolewars.android.app.tab.CwNavigationMainTabActivity;
import de.consolewars.android.app.tab.cmts.CommentsFragment;
import de.consolewars.android.app.view.CwPagerAdapter.FragmentProvider;

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
 * Activity showing and handling a single news.
 * 
 * @author Alexander Dridiger
 */
public class SingleNewsFragmentActivity extends CwAbstractFragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected int getInitialFragmentSelection() {
		return 1;
	}

	@Override
	protected FragmentProvider getFragmentProvider() {
		fragmentProvider = new FragmentProvider() {
			@Override
			public CwAbstractFragment requestFragment(int index) {
				switch (index) {
				case 0:
					return new PicsFragment(getTitle(index), index);
				case 1:
					return new SingleNewsFragment(getTitle(index), index);
				case 2:
					return new CommentsFragment(CwApplication.cwEntityManager().getSelectedNews(), getTitle(index),
							index);
				}
				throw new IllegalStateException("Not more than three fragments supported.");
			}

			@Override
			public int getCount() {
				return 3;
			}

			@Override
			public String getTitle(int index) {
				switch (index) {
				case 0:
					return getString(R.string.gallery);
				case 1:
					return getString(R.string.news);
				case 2:
					return getString(R.string.comments);
				}
				throw new IllegalStateException("Not more than three fragments supported.");
			}
		};
		return fragmentProvider;
	}

	@Override
	public void onBackPressed() {
		Fragment f = getSupportFragmentManager().findFragmentByTag(
				"android:switcher:" + R.id.pager + ":" + lastPosition);
		if (f != null && f instanceof CwAbstractFragment) {
			((CwAbstractFragment) f).setPosition(lastPosition);
			((CwAbstractFragment) f).backPressed();
		}
		if (getParent() instanceof CwNavigationMainTabActivity) {
			((CwNavigationMainTabActivity) getParent()).getTabHost()
					.setCurrentTab(CwNavigationMainTabActivity.NEWS_TAB);
			CwNavigationMainTabActivity.selectedNewsTab = CwNavigationMainTabActivity.NEWS_TAB;
		}
	}
}