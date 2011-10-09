package de.consolewars.android.app.tab.news;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import de.consolewars.android.app.CwApplication;
import de.consolewars.android.app.Filter;
import de.consolewars.android.app.R;
import de.consolewars.android.app.db.domain.CwNews;
import de.consolewars.android.app.db.domain.CwSubject;
import de.consolewars.android.app.tab.CwAbstractFragment;
import de.consolewars.android.app.tab.CwAbstractFragmentActivity;
import de.consolewars.android.app.tab.CwNavigationMainTabActivity;
import de.consolewars.android.app.tab.OnSubjectSelectedListener;
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
 * Central Activity to handle the ui for news.
 * 
 * @author Alexander Dridiger
 */
public class NewsFragmentActivity extends CwAbstractFragmentActivity implements OnSubjectSelectedListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onSubjectSelected(CwSubject subject) {
		CwApplication.cwEntityManager().setSelectedNews((CwNews) subject);
		if (getParent() instanceof CwNavigationMainTabActivity) {
			((CwNavigationMainTabActivity) getParent()).getTabHost().setCurrentTab(
					CwNavigationMainTabActivity.SINGLENEWS_TAB);
			CwNavigationMainTabActivity.selectedNewsTab = CwNavigationMainTabActivity.SINGLENEWS_TAB;
		}
	}

	@Override
	protected int getInitialFragmentSelection() {
		return 0;
	}

	@Override
	protected FragmentProvider getFragmentProvider() {
		fragmentProvider = new FragmentProvider() {
			@Override
			public CwAbstractFragment requestFragment(int index) {
				List<CwAbstractFragment> fragments = adapter.getFragments();
				// first set the new fragment with List.set() and at the same time return the old one, then get the new
				// fragment with List.get()
				switch (index) {
				case 0:
					return adapter.switchFragmentsInfo(fragments.set(index, new NewsFragment(Filter.NEWS_ALL,
							getString(R.string.news_filter_all))), fragments.get(index));
				case 1:
					return adapter.switchFragmentsInfo(fragments.set(index, new NewsFragment(Filter.NEWS_MS,
							getString(R.string.news_filter_only_ms))), fragments.get(index));
				case 2:
					return adapter.switchFragmentsInfo(fragments.set(index, new NewsFragment(Filter.NEWS_NIN,
							getString(R.string.news_filter_only_nin))), fragments.get(index));
				case 3:
					return adapter.switchFragmentsInfo(fragments.set(index, new NewsFragment(Filter.NEWS_SONY,
							getString(R.string.news_filter_only_sony))), fragments.get(index));
				}
				throw new IllegalStateException("Not more than two fragments supported.");
			}

			@Override
			public void initFragments() {
				List<CwAbstractFragment> fragments = new ArrayList<CwAbstractFragment>();
				fragments.add(new NewsFragment(Filter.NEWS_ALL, getString(R.string.news_filter_all)));
				fragments.add(new NewsFragment(Filter.NEWS_MS, getString(R.string.news_filter_only_ms)));
				fragments.add(new NewsFragment(Filter.NEWS_NIN, getString(R.string.news_filter_only_nin)));
				fragments.add(new NewsFragment(Filter.NEWS_SONY, getString(R.string.news_filter_only_sony)));
				adapter.setFragments(fragments);
			}
		};
		return fragmentProvider;
	}
}