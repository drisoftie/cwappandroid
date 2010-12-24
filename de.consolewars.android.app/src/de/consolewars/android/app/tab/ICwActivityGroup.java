package de.consolewars.android.app.tab;

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
 * Interface providing access to functionalities of an {@link ActivityGroup} and specific behavior
 * necessary for this app.
 * 
 * @author Alexander Dridiger
 */
public interface ICwActivityGroup {	

	/**
	 * Replaces the current view of the current activity with another view of another activity.
	 * 
	 * @param view
	 *            the view that replaces the current view
	 */
	public void replaceView(View view);

	/**
	 * Used to implement necessary logic, if the back button is pressed.
	 */
	public void back();
}
