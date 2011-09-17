package de.consolewars.android.app.tab.news;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import de.consolewars.android.app.CwApplication;
import de.consolewars.android.app.R;
import de.consolewars.android.app.tab.CwAbstractFragment;
import de.consolewars.android.app.tab.CwAbstractFragmentActivity;
import de.consolewars.android.app.tab.CwNavigationMainTabActivity;
import de.consolewars.android.app.tab.cmts.CommentsFragment;
import de.consolewars.android.app.view.CwPagerAdapter;
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
 * Activity showing and handling a single news.
 * 
 * @author Alexander Dridiger
 */
public class SingleNewsFragmentActivity extends CwAbstractFragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.fragment_pager_layout);

		List<CwAbstractFragment> fragments = new ArrayList<CwAbstractFragment>();
		fragments.add(new SingleNewsFragment(getString(R.string.news)));
		fragments.add(new CommentsFragment(CwApplication.cwEntityManager().getSelectedNews(),
				getString(R.string.comments)));

		adapter = new CwPagerAdapter(getSupportFragmentManager(), getApplicationContext(), fragments);

		ViewPager newsPager = (ViewPager) findViewById(R.id.pager);
		newsPager.setAdapter(adapter);

		TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.indicator);
		indicator.setViewPager(newsPager);
		indicator.setFooterIndicatorStyle(IndicatorStyle.Underline);

		// We set this on the indicator, NOT the pager
		indicator.setOnPageChangeListener(this);

		((CwAbstractFragment) adapter.getItem(0)).setSelected(true);
	}

	@Override
	public void onBackPressed() {
		for (int i = 0; i < adapter.getCount(); i++) {
			if (adapter.getItem(i) instanceof SingleNewsFragment) {
				((SingleNewsFragment) adapter.getItem(i)).backPressed();
			} else if (adapter.getItem(i) instanceof CommentsFragment) {
				((CommentsFragment) adapter.getItem(i)).backPressed();
			}
		}
		if (getParent() instanceof CwNavigationMainTabActivity) {
			((CwNavigationMainTabActivity) getParent()).getTabHost()
					.setCurrentTab(CwNavigationMainTabActivity.NEWS_TAB);
			CwNavigationMainTabActivity.selectedSubjectTab = CwNavigationMainTabActivity.NEWS_TAB;
		}
	}
}