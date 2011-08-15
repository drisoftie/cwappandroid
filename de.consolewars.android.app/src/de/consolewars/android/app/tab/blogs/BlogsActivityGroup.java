package de.consolewars.android.app.tab.blogs;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import de.consolewars.android.app.tab.CwBasicActivityGroup;
import de.consolewars.android.app.tab.CwNavigationMainTabActivity;

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
 * Manages activities belonging to blogs. It is used for facilitating the
 * exchange of activities within tabs. Switch blogs activities with this group.
 * 
 * @author Alexander Dridiger
 * @see CwNavigationMainTabActivity
 * 
 */
public class BlogsActivityGroup extends CwBasicActivityGroup {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Replace the view of this ActivityGroup
		replaceView(startBlogActivity());
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		// handling rotation
		resetCache();

		// reset the ui
		replaceView(startBlogActivity());
	}

	private View startBlogActivity() {
		// Start the root activity withing the group and get its view
		return getLocalActivityManager().startActivity(BlogsActivity.class.getSimpleName(),
				new Intent(this, BlogsActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)).getDecorView();
	}
}
