package de.consolewars.android.app.tab;

import roboguice.activity.RoboTabActivity;
import android.app.ActivityGroup;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;
import de.consolewars.android.app.R;
import de.consolewars.android.app.tab.blogs.BlogsFragmentActivity;
import de.consolewars.android.app.tab.blogs.SingleBlogFragmentActivity;
import de.consolewars.android.app.tab.board.BoardActivityGroup;
import de.consolewars.android.app.tab.msgs.MessagesFragmentActivity;
import de.consolewars.android.app.tab.news.NewsFragmentActivity;
import de.consolewars.android.app.tab.news.SingleNewsFragmentActivity;
import de.consolewars.android.app.tab.overview.OverviewFragmentActivity;
import de.consolewars.android.app.tab.shout.ShoutboxActivityGroup;

/*
 * Copyright [2010] [Alexander Dridiger]
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
 * Activity to manage the main navigation of the app, i.e. its tabs. Initializes the tabs and their corresponding
 * {@link ActivityGroup}s or {@link FragmentActivity} and exposes the {@link TabHost} for tab management. The
 * {@link TabHost} is managed by the Android activity lifecycle.
 * 
 * @author Alexander Dridiger
 */
public class CwNavigationMainTabActivity extends RoboTabActivity {

	// unique tabhost of this activity
	private TabHost usedTabHost;

	public static final int OVERVIEW_TAB = 0;
	public static final int NEWS_TAB = 1;
	public static final int BLOGS_TAB = 2;
	public static final int MESSAGES_TAB = 3;
	public static final int BOARD_TAB = 4;
	public static final int SHOUTBOX_TAB = 5;
	public static final int SINGLENEWS_TAB = 6;
	public static final int SINGLEBLOG_TAB = 7;
	public static final int SINGLEMESSAGES_TAB = 8;

	public static int selectedNewsTab = NEWS_TAB;
	public static int selectedBlogTab = BLOGS_TAB;
	public static int selectedMsgsTab = MESSAGES_TAB;

	/**
	 * The {@link TabHost} for this {@link TabActivity}. Before used, check for null since the activity might not have
	 * been created.
	 * 
	 * @return the unique {@link TabHost}
	 */
	public TabHost getUsedTabHost() {
		return usedTabHost;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main_tab_layout);
		// getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar_layout);

		usedTabHost = getTabHost();
		setTabs();
	}

	/**
	 * Delegate tab creation and adding.
	 */
	private void setTabs() {
		// add the necessary tabs
		addTab(R.string.tab_overv_tag, OverviewFragmentActivity.class);
		addTab(R.string.tab_news_tag, NewsFragmentActivity.class);
		addTab(R.string.tab_blogs_tag, BlogsFragmentActivity.class);
		addTab(R.string.tab_msgs_tag, MessagesFragmentActivity.class);
		addTab(R.string.tab_board_tag, BoardActivityGroup.class);
		addTab(R.string.tab_shout_tag, ShoutboxActivityGroup.class);
		// invisible tabs
		addTab(R.string.tab_singlenews_tag, SingleNewsFragmentActivity.class);
		addTab(R.string.tab_singleblog_tag, SingleBlogFragmentActivity.class);
	}

	/**
	 * Create a tab with an Activity and add it to the TabHost
	 * 
	 * @param tagId
	 *            resource id of the string representing the tag for finding the tab
	 * @param activity
	 *            the activity to be added
	 */
	private void addTab(int tagId, Class<?> activity) {
		// create an Intent to launch an Activity for the tab (to be reused)
		Intent intent = new Intent().setClass(this, activity);
		// initialize a TabSpec for each tab and add it to the TabHost
		TabHost.TabSpec spec = usedTabHost.newTabSpec(getString(tagId));

		spec.setIndicator(getString(tagId));
		spec.setContent(intent);
		usedTabHost.addTab(spec);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_tab_menu, menu);
		return true;
	}

	public void setTab(int tabId) {
		getUsedTabHost().setCurrentTab(tabId);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		// Find which menu item has been selected
		switch (item.getItemId()) {
		// Check for each known menu item
		case (R.id.menu_overview):
			getUsedTabHost().setCurrentTab(OVERVIEW_TAB);
			break;
		case (R.id.menu_news):
			if (selectedNewsTab == SINGLENEWS_TAB) {
				getUsedTabHost().setCurrentTab(SINGLENEWS_TAB);
			} else {
				getUsedTabHost().setCurrentTab(NEWS_TAB);
				selectedNewsTab = NEWS_TAB;
			}
			break;
		case (R.id.menu_blogs):
			if (selectedBlogTab == SINGLEBLOG_TAB) {
				getUsedTabHost().setCurrentTab(SINGLEBLOG_TAB);
			} else {
				getUsedTabHost().setCurrentTab(BLOGS_TAB);
			}
			break;
		case (R.id.menu_msgs):
			getUsedTabHost().setCurrentTab(MESSAGES_TAB);
			break;
		case (R.id.menu_board):
			getUsedTabHost().setCurrentTab(BOARD_TAB);
			break;
		case (R.id.menu_shoutbox):
			getUsedTabHost().setCurrentTab(SHOUTBOX_TAB);
			break;
		}
		return true;
	}
}
