package de.consolewars.android.app.view;

import java.util.List;

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

	/**
	 * @param fragments
	 *            the fragments to set
	 */
	public void setFragments(List<CwAbstractFragment> fragments) {
		this.fragments = fragments;
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
		fragmentProvider.initFragments();
	}

	/**
	 * Convenience method.
	 * 
	 * @param oldFragment
	 * @param newFragment
	 * @return new Fragment with state information of the old one.
	 */
	public CwAbstractFragment switchFragmentsInfo(CwAbstractFragment oldFragment, CwAbstractFragment newFragment) {
		if (oldFragment.isSelected()) {
			newFragment.setForeground(true);
		}
		if (oldFragment.isStartFragment()) {
			newFragment.setStartFragment(true);
		}
		newFragment.setTitle(oldFragment.getTitle());
		return newFragment;
	}

	/**
	 * Convenience method.
	 * 
	 * @param newFragment
	 * @param index
	 */
	public void switchFragmentsInfo(CwAbstractFragment newFragment, int index) {
		if (getFragments().get(index).isSelected()) {
			newFragment.setForeground(true);
		}
		if (getFragments().get(index).isStartFragment()) {
			newFragment.setStartFragment(true);
		}
		newFragment.setTitle(getFragments().get(index).getTitle());
		getFragments().set(index, newFragment);
	}

	/**
	 * <h1>Made under the following assumption:</h1><br>
	 * {@link FragmentPagerAdapter} only invokes {@link FragmentPagerAdapter#getItem(int)}, if it <b>really</b> needs a
	 * new {@link Fragment}, otherwise it uses internal caching. In our case it's necessary to return a new
	 * {@link Fragment} within {@link FragmentPagerAdapter#getItem(int)} and store it, because we need references to the
	 * fragments to handle back button clicks, menu button clicks, etc. <br>
	 * Therefore this provider returns new fragments on request and retains it for further processing.
	 */
	public interface FragmentProvider {
		public CwAbstractFragment requestFragment(int index);

		public void initFragments();
	}
}