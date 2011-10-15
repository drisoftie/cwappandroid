package de.consolewars.android.app.tab.overview;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import de.consolewars.android.app.R;
import de.consolewars.android.app.tab.CwAbstractFragment;
import de.consolewars.android.app.tab.CwAbstractFragmentActivity;
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
 * Central Activity to handle the ui the overview
 * 
 * @author Alexander Dridiger
 */
public class OverviewFragmentActivity extends CwAbstractFragmentActivity {

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
							fragments.set(index, new OverviewFragment(getString(R.string.overview))),
							fragments.get(index));
				}
				throw new IllegalStateException("Not more than one fragment supported.");
			}

			@Override
			public void initFragments() {
				List<CwAbstractFragment> fragments = new ArrayList<CwAbstractFragment>();
				fragments.add(new OverviewFragment(getString(R.string.overview)));
				adapter.setFragments(fragments);
			}
		};
		return fragmentProvider;
	}
}