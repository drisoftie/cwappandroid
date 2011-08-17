package de.consolewars.android.app.tab;

import roboguice.activity.RoboTabActivity;
import android.app.ActivityGroup;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TabHost;
import de.consolewars.android.app.R;
import de.consolewars.android.app.tab.blogs.BlogsActivityGroup;
import de.consolewars.android.app.tab.board.BoardActivityGroup;
import de.consolewars.android.app.tab.msgs.MessagesActivityGroup;
import de.consolewars.android.app.tab.news.NewsActivityGroup;
import de.consolewars.android.app.tab.overview.OverviewActivityGroup;
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
 * Activity to manage the main navigation of the app, i.e. its tabs. Initializes the tabs and their
 * corresponding {@link ActivityGroup}s and exposes the {@link TabHost} for tab management. The
 * {@link TabHost} is managed by the Android activity lifecycle. No switching or further application
 * logic is provided here.
 * 
 * @author Alexander Dridiger
 */
public class CwNavigationMainTabActivity extends RoboTabActivity {

	// unique tabhost of this activity
	private TabHost usedTabHost;

	/**
	 * The {@link TabHost} for this {@link TabActivity}. Before used, check for null since the
	 * activity might not have been created.
	 * 
	 * @return the unique {@link TabHost}
	 */
	public TabHost getUsedTabHost() {
		return usedTabHost;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main_tab_layout);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar_layout);

		usedTabHost = getTabHost();
		setTabs();

		RadioGroup tabs = (RadioGroup) findViewById(R.id.custom_tabs);

		tabs.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.tab_overview:
					getTabHost().setCurrentTab(0);
					break;
				case R.id.tab_news:
					getTabHost().setCurrentTab(1);
					break;
				case R.id.tab_blogs:
					getTabHost().setCurrentTab(2);
					break;
				case R.id.tab_msgs:
					getTabHost().setCurrentTab(3);
					break;
				case R.id.tab_board:
					getTabHost().setCurrentTab(4);
					break;
				case R.id.tab_shout:
					getTabHost().setCurrentTab(5);
					break;
				}
			}
		});
	}

	/**
	 * Delegate tab creation and adding.
	 */
	private void setTabs() {
		// add the necessary tabs
		addTab(R.string.tab_overv_tag, R.drawable.def_tab_overview, OverviewActivityGroup.class);
		addTab(R.string.tab_news_tag, R.drawable.def_tab_news, NewsActivityGroup.class);
		addTab(R.string.tab_blogs_tag, R.drawable.def_tab_blogs, BlogsActivityGroup.class);
		addTab(R.string.tab_msgs_tag, R.drawable.def_tab_msgs, MessagesActivityGroup.class);
		addTab(R.string.tab_board_tag, R.drawable.def_tab_board, BoardActivityGroup.class);
		addTab(R.string.tab_shout_tag, R.drawable.def_tab_shout, ShoutboxActivityGroup.class);
	}

	/**
	 * Create a tab with an Activity and add it to the TabHost
	 * 
	 * @param labelId
	 *            resource id of the string representing the tab label
	 * @param tagId
	 *            resource id of the string representing the tag for finding the tab
	 * @param drawableId
	 *            resource id of the icon for this tab
	 * @param activity
	 *            the activity to be added
	 */
	private void addTab(int tagId, int drawableId, Class<? extends ICwActivityGroup> activity) {
		// create an Intent to launch an Activity for the tab (to be reused)
		Intent intent = new Intent().setClass(this, activity);
		// initialize a TabSpec for each tab and add it to the TabHost
		TabHost.TabSpec spec = usedTabHost.newTabSpec(getString(tagId));
		// use layout inflater to get a view of the tab to be added
		View tabIndicator = getLayoutInflater().inflate(R.layout.tab_indicator, getTabWidget(), false);

		// add given information to view elements of the tab
		ImageView icon = (ImageView) tabIndicator.findViewById(R.id.icon);
		icon.setImageResource(drawableId);

		spec.setIndicator(tabIndicator);
		spec.setContent(intent);
		usedTabHost.addTab(spec);
	}
}
