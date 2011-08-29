package de.consolewars.android.app.view;

import android.content.Context;
import android.util.AttributeSet;
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
 * @author Alexander Dridiger
 * 
 */
public class ScrollDetectorScrollView extends ScrollView {

	private IScrollListener scrollListener = null;

	public ScrollDetectorScrollView(Context context) {
		super(context);
	}

	public ScrollDetectorScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ScrollDetectorScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setOnScrollListener(IScrollListener scrollListener) {
		this.scrollListener = scrollListener;
	}

	@Override
	protected void onScrollChanged(int x, int y, int oldx, int oldy) {
		super.onScrollChanged(x, y, oldx, oldy);
		if (scrollListener != null) {
			scrollListener.onScrollChanged(this, x, y, oldx, oldy);
		}
	}

	public void removeScrollListener() {
		scrollListener = null;
	}
}
