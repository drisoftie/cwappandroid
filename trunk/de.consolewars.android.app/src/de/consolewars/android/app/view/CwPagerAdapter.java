package de.consolewars.android.app.view;

import java.util.List;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import de.consolewars.android.app.tab.CwAbstractFragment;

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

	private List<CwAbstractFragment> fragments;

	public CwPagerAdapter(FragmentManager fm, Context context, List<CwAbstractFragment> fragments) {
		super(fm);
		this.fragments = fragments;
	}

	@Override
	public Fragment getItem(int position) {
		return fragments.get(position);
	}

	@Override
	public int getCount() {
		return fragments.size();
	}

	@Override
	public String getTitle(int position) {
		return fragments.get(position).getTitle();
	}

	/**
	 * @return the fragments
	 */
	public List<CwAbstractFragment> getFragments() {
		return fragments;
	}
}