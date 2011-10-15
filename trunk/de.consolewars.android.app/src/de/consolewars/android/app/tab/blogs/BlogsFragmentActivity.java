package de.consolewars.android.app.tab.blogs;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import de.consolewars.android.app.CwApplication;
import de.consolewars.android.app.Filter;
import de.consolewars.android.app.R;
import de.consolewars.android.app.db.domain.CwBlog;
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
 * @author Alexander Dridiger
 * 
 */
public class BlogsFragmentActivity extends CwAbstractFragmentActivity implements OnSubjectSelectedListener {

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
		CwApplication.cwEntityManager().setSelectedBlog((CwBlog) subject);
		if (getParent() instanceof CwNavigationMainTabActivity) {
			((CwNavigationMainTabActivity) getParent()).getTabHost().setCurrentTab(
					CwNavigationMainTabActivity.SINGLEBLOG_TAB);
			CwNavigationMainTabActivity.selectedBlogTab = CwNavigationMainTabActivity.SINGLEBLOG_TAB;
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
				switch (index) {
				case 0:
					return adapter.switchFragmentsInfo(fragments.set(index, new BlogsFragment(Filter.BLOGS_NORMAL,
							getString(R.string.blogs_filter_all))), fragments.get(index));
				case 1:
					return adapter.switchFragmentsInfo(fragments.set(index, new BlogsFragment(Filter.BLOGS_USER,
							getString(R.string.blogs_filter_own))), fragments.get(index));
				}
				throw new IllegalStateException("Not more than two fragments supported.");
			}

			@Override
			public void initFragments() {
				List<CwAbstractFragment> fragments = new ArrayList<CwAbstractFragment>();
				fragments.add(new BlogsFragment(Filter.BLOGS_NORMAL, getString(R.string.blogs_filter_all)));
				fragments.add(new BlogsFragment(Filter.BLOGS_USER, getString(R.string.blogs_filter_own)));
				adapter.setFragments(fragments);
			}
		};
		return fragmentProvider;
	}
}