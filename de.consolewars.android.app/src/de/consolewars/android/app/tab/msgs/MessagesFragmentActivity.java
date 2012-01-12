package de.consolewars.android.app.tab.msgs;

import android.os.Bundle;
import de.consolewars.android.app.CwApplication;
import de.consolewars.android.app.R;
import de.consolewars.android.app.db.domain.CwNews;
import de.consolewars.android.app.db.domain.CwSubject;
import de.consolewars.android.app.tab.CwAbstractFragment;
import de.consolewars.android.app.tab.CwAbstractFragmentActivity;
import de.consolewars.android.app.tab.CwNavigationMainTabActivity;
import de.consolewars.android.app.tab.OnSubjectSelectedListener;

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
public class MessagesFragmentActivity extends CwAbstractFragmentActivity implements OnSubjectSelectedListener {

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
			((CwNavigationMainTabActivity) getParent()).setTab(CwNavigationMainTabActivity.SINGLEMESSAGES_TAB);
			CwNavigationMainTabActivity.selectedMsgsTab = CwNavigationMainTabActivity.SINGLEMESSAGES_TAB;
		}
	}

	@Override
	protected int getInitialFragmentSelection() {
		return 0;
	}

	@Override
	protected CwAbstractFragment getFragmentForIndex(int index) {
		switch (index) {
		case 0:
			return new MessagesFragment(getTitle(index), index);
		case 1:
			return new MessagesFragment(getTitle(index), index);
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
			return getString(R.string.messages_filter_inbox);
		default:
			return getString(R.string.messages_filter_outbox);
		}
	}

	@Override
	protected String getStartActionBarTitle() {
		return getString(R.string.messages_area);
	}

	@Override
	protected boolean isHomeEnabled() {
		return true;
	}
}