package de.consolewars.android.app.tab.overview;

import android.os.Bundle;
import de.consolewars.android.app.R;
import de.consolewars.android.app.tab.CwAbstractFragment;
import de.consolewars.android.app.tab.CwAbstractFragmentActivity;

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
	protected CwAbstractFragment getFragmentForIndex(int index) {
		switch (index) {
		case 0:
			return new OverviewFragment(getTitle(index), index);
		case 1:
			return new OptionsFragment(getTitle(index), index);
		}
		return null;
	}

	@Override
	public int getCount() {
		return 2;
	}

	@Override
	public String getTitle(int index) {
		switch (index) {
		case 0:
			return getString(R.string.overview);
		case 1:
			return getString(R.string.options);
		default:
			return getString(R.string.overview);
		}
	}

	@Override
	protected String getStartActionBarTitle() {
		return getString(R.string.home);
	}

	@Override
	protected boolean isHomeEnabled() {
		return false;
	}
}