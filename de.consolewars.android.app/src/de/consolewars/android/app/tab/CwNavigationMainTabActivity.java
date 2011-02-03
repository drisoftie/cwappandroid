package de.consolewars.android.app.tab;

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import de.consolewars.android.app.APICaller;
import de.consolewars.android.app.R;
import de.consolewars.android.app.db.AppDataHandler;
import de.consolewars.android.app.db.DatabaseManager;
import de.consolewars.android.app.tab.blogs.BlogsActivityGroup;
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
 * {@link TabHost} is managed by the Android activity lifecycle. It also grants access to the
 * {@link APICaller} and {@link AppDataHandler} to facilitate Consolewars-API calls and database
 * handling. No switching or further application logic is provided here.
 * 
 * @author Alexander Dridiger
 */
public class CwNavigationMainTabActivity extends TabActivity {

	// unique tabhost of this activity
	private TabHost usedTabHost;

	private APICaller apiCaller;
	private AppDataHandler dataHandler;

	/**
	 * The {@link TabHost} for this {@link TabActivity}. Before used, check for null since the
	 * activity might not have been created.
	 * 
	 * @return the unique {@link TabHost}
	 */
	public TabHost getUsedTabHost() {
		return usedTabHost;
	}

	/**
	 * @return the api wrapper
	 */
	public APICaller getApiCaller() {
		return apiCaller;
	}

	/**
	 * @return data handler
	 */
	public AppDataHandler getDataHandler() {
		return dataHandler;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initHelper();

		setContentView(R.layout.main_tab_layout);
		usedTabHost = getTabHost();
		setTabs();
	}

	/**
	 * Initialize helper classes like the {@link DatabaseManager}.
	 */
	private void initHelper() {
		dataHandler = new AppDataHandler(getApplicationContext());
		apiCaller = new APICaller(getApplicationContext());
	}

	/**
	 * Delegate tab creation and adding.
	 */
	private void setTabs() {
		// add the necessary tabs
		addTab(R.string.tab_overv_head, R.string.tab_overv_tag, R.drawable.def_tab_overview,
				OverviewActivityGroup.class);
		addTab(R.string.tab_news_head, R.string.tab_news_tag, R.drawable.def_tab_news,
				NewsActivityGroup.class);
		addTab(R.string.tab_blogs_head, R.string.tab_blogs_tag, R.drawable.def_tab_blogs,
				BlogsActivityGroup.class);
		addTab(R.string.tab_msgs_head, R.string.tab_msgs_tag, R.drawable.def_tab_msg,
				MessagesActivityGroup.class);
		addTab(R.string.tab_shout_head, R.string.tab_shout_tag, R.drawable.def_tab_shout,
				ShoutboxActivityGroup.class);
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
	private void addTab(int labelId, int tagId, int drawableId, Class<? extends Activity> activity) {
		// create an Intent to launch an Activity for the tab (to be reused)
		Intent intent = new Intent().setClass(this, activity);
		// initialize a TabSpec for each tab and add it to the TabHost
		TabHost.TabSpec spec = usedTabHost.newTabSpec(getString(tagId));
		// use layout inflater to get a view of the tab to be added
		View tabIndicator = LayoutInflater.from(this).inflate(R.layout.tab_indicator,
				getTabWidget(), false);

		// add given information to view elements of the tab
		TextView title = (TextView) tabIndicator.findViewById(R.id.title);
		title.setText(labelId);
		ImageView icon = (ImageView) tabIndicator.findViewById(R.id.icon);
		icon.setImageResource(drawableId);

		spec.setIndicator(tabIndicator);
		spec.setContent(intent);
		usedTabHost.addTab(spec);
	}
}
