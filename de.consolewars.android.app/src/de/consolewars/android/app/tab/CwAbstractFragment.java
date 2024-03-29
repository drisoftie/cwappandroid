package de.consolewars.android.app.tab;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import de.consolewars.android.app.R;
import de.consolewars.android.app.view.ActionBar;
import de.consolewars.android.app.view.ActionBar.Action;

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
 * A {@link Fragment} providing additional helper methods for using it within a {@link FragmentPagerAdapter} and a
 * {@link ViewPager}.
 * 
 * @author Alexander Dridiger
 */
public abstract class CwAbstractFragment extends Fragment {

	private String title;
	private int position;

	/**
	 * Mandatory constructor for creating a {@link Fragment}
	 */
	public CwAbstractFragment() {
	}

	/**
	 * A title should always be provided when a {@link CwAbstractFragment} is instantiated.
	 * 
	 * @param title
	 */
	public CwAbstractFragment(String title, int position) {
		this.title = title;
		setRetainInstance(true);
		this.position = position;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Delegation method that must be invoked by the parent {@link CwAbstractFragmentActivity} to notify if the back
	 * button was pressed.
	 */
	public abstract void backPressed();

	/**
	 * @return the position
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * @param position
	 *            the position to set
	 */
	public void setPosition(int position) {
		this.position = position;
	}

	public abstract void refresh();

	/**
	 * Sets a home action onto the {@link ActionBar}.
	 */
	public void setHomeAction() {
		ActionBar actionBar = getActionBar();
		actionBar.setHomeAction(new Action() {
			@Override
			public void performAction(View view) {
				if (getActivity().getParent() instanceof CwNavigationMainTabActivity) {
					((CwNavigationMainTabActivity) getActivity().getParent())
							.setTab(CwNavigationMainTabActivity.OVERVIEW_TAB);
				}
			}

			@Override
			public int getDrawable() {
				return R.drawable.home;
			}
		});
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	public ActionBar getActionBar() {
		ActionBar actionBar = (ActionBar) getActivity().findViewById(R.id.actionbar);
		return actionBar;
	}
}