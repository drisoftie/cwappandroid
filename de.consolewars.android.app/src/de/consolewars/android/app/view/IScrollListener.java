package de.consolewars.android.app.view;

import android.widget.ScrollView;

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
 * Interface used for callbacks in {@link ScrollDetectorScrollView}.
 * 
 * @author Alexander Dridiger
 */
public interface IScrollListener {

	/**
	 * Called when the scroll state of the {@link ScrollView} has changed.
	 * 
	 * @param scrollView
	 *            The {@link ScrollView} whose state has changed.
	 * @param x
	 *            new x position
	 * @param y
	 *            new y position
	 * @param oldx
	 * @param oldy
	 */
	public void onScrollChanged(ScrollDetectorScrollView scrollView, int x, int y, int oldx, int oldy);
}
