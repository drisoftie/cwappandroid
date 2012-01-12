package de.consolewars.android.app.view;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import de.consolewars.android.app.tab.FragmentProvider;

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
public class CwPagerAdapter extends FragmentPagerAdapter implements TitleProvider {

	private FragmentProvider fragmentProvider;

	public CwPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int position) {
		return fragmentProvider.requestFragment(position);
	}

	@Override
	public int getCount() {
		return fragmentProvider.getCount();
	}

	@Override
	public String getTitle(int position) {
		return fragmentProvider.getTitle(position);
	}

	/**
	 * @return the fragmentProvider
	 */
	public FragmentProvider getFragmentProvider() {
		return fragmentProvider;
	}

	/**
	 * @param fragmentProvider
	 *            the fragmentProvider to set
	 */
	public void setFragmentProvider(FragmentProvider fragmentProvider) {
		this.fragmentProvider = fragmentProvider;
	}
}