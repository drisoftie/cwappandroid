package de.consolewars.android.app.tab;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ActivityGroup;
import android.view.View;

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
 * Basic implementation of an {@link ActivityGroup} supporting {@link Activity} switching and
 * {@link View} caching.
 * 
 * @author Alexander Dridiger
 */
public abstract class CwBasicActivityGroup extends ActivityGroup implements ICwActivityGroup {

	private List<View> viewCache;

	@Override
	public void replaceView(View view) {
		if (viewCache == null) {
			viewCache = new ArrayList<View>();
		}
		viewCache.add(view);
		setContentView(view);
	}

	@Override
	public void back() {
		if (viewCache.size() > 1) {
			viewCache.remove(viewCache.size() - 1);
			setContentView(viewCache.get(viewCache.size() - 1));
		} else {
			finish();
		}
	}

	/**
	 * Clears the View cache.
	 */
	public void resetCache() {
		viewCache = new ArrayList<View>();
	}

	@Override
	public void onBackPressed() {
		this.back();
	}
}