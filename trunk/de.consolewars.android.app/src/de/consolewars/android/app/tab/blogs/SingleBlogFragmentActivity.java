package de.consolewars.android.app.tab.blogs;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import de.consolewars.android.app.CwApplication;
import de.consolewars.android.app.R;
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
 * 
 * 
 * @author Alexander Dridiger
 */
public class SingleBlogFragmentActivity extends CwAbstractFragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
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
					return adapter.switchFragmentsInfo(
							fragments.set(index, new SingleBlogFragment(getString(R.string.blog))),
							fragments.get(index));
				case 1:
					return adapter.switchFragmentsInfo(fragments.set(index, new CommentsFragment(CwApplication
							.cwEntityManager().getSelectedBlog(), getString(R.string.comments))), fragments.get(index));
				}
				throw new IllegalStateException("Not more than two fragments supported.");
			}

			@Override
			public void initFragments() {
				List<CwAbstractFragment> fragments = new ArrayList<CwAbstractFragment>();
				fragments.add(new SingleBlogFragment(getString(R.string.blog)));
				fragments.add(new CommentsFragment(CwApplication.cwEntityManager().getSelectedBlog(),
						getString(R.string.comments)));
				adapter.setFragments(fragments);
			}
		};
		return fragmentProvider;
	}

	@Override
	public void onBackPressed() {
		for (int i = 0; i < adapter.getCount(); i++) {
			if (adapter.getFragments().get(i) instanceof SingleBlogFragment) {
				((SingleBlogFragment) adapter.getFragments().get(i)).backPressed();
			} else if (adapter.getFragments().get(i) instanceof CommentsFragment) {
				((CommentsFragment) adapter.getFragments().get(i)).backPressed();
			}
		}
		if (getParent() instanceof CwNavigationMainTabActivity) {
			((CwNavigationMainTabActivity) getParent()).getTabHost().setCurrentTab(
					CwNavigationMainTabActivity.BLOGS_TAB);
			CwNavigationMainTabActivity.selectedBlogTab = CwNavigationMainTabActivity.BLOGS_TAB;
		}
	}
}