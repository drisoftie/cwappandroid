package de.consolewars.android.app.tab.news;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
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
				List<CwAbstractFragment> fragments = adapter.getFragments();
				switch (index) {
				case 0:
					return adapter.switchFragmentsInfo(
							fragments.set(index, new PicsFragment(getString(R.string.gallery))), fragments.get(index));
				case 1:
					return adapter.switchFragmentsInfo(
							fragments.set(index, new SingleNewsFragment(getString(R.string.news))),
							fragments.get(index));
				case 2:
					return adapter.switchFragmentsInfo(fragments.set(index, new CommentsFragment(CwApplication
							.cwEntityManager().getSelectedNews(), getString(R.string.comments))), fragments.get(index));
				}
				throw new IllegalStateException("Not more than two fragments supported.");
			}

			@Override
			public void initFragments() {
				List<CwAbstractFragment> fragments = new ArrayList<CwAbstractFragment>();
				fragments.add(new PicsFragment(getString(R.string.gallery)));
				fragments.add(new SingleNewsFragment(getString(R.string.news)));
				fragments.add(new CommentsFragment(CwApplication.cwEntityManager().getSelectedNews(),
						getString(R.string.comments)));
				adapter.setFragments(fragments);
			}
		};
		return fragmentProvider;
	}

	@Override
	public void onBackPressed() {
		for (int i = 0; i < adapter.getCount(); i++) {
			adapter.getFragments().get(i).backPressed();
		}
		if (getParent() instanceof CwNavigationMainTabActivity) {
			((CwNavigationMainTabActivity) getParent()).getTabHost()
					.setCurrentTab(CwNavigationMainTabActivity.NEWS_TAB);
			CwNavigationMainTabActivity.selectedNewsTab = CwNavigationMainTabActivity.NEWS_TAB;
		}
	}
}