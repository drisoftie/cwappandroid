package de.consolewars.android.app.tab;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;

/* 
 * Copyright [yyyy] [name of copyright owner]
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
/**
 * <h1>Made under the following assumption:</h1><br>
 * {@link FragmentPagerAdapter} only invokes {@link FragmentPagerAdapter#getItem(int)}, if it <b>really</b> needs a new
 * {@link Fragment}, otherwise it uses internal caching. In our case it's necessary to return a new {@link Fragment}
 * within {@link FragmentPagerAdapter#getItem(int)} and store it, because we need references to the fragments to handle
 * back button clicks, menu button clicks, etc. <br>
 * Therefore this provider returns new fragments on request and retains it for further processing.
 * 
 * @author Alexander Dridiger
 * 
 */
public interface FragmentProvider {
	public CwAbstractFragment requestFragment(int index);

	public int getCount();

	public String getTitle(int index);
}
