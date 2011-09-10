package de.consolewars.android.app.tab.news;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import de.consolewars.android.app.R;
import de.consolewars.android.app.tab.CwAbstractFragmentActivity;
import de.consolewars.android.app.view.TitlePageIndicator;
import de.consolewars.android.app.view.TitlePageIndicator.IndicatorStyle;

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
 * 
 * 
 * @author Alexander Dridiger
 */
public class CwNewsFragmentActivity extends CwAbstractFragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.news_layout);

		ViewPager pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(new CwNewsPagerAdapter(getSupportFragmentManager(), getApplicationContext()));

		TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.indicator);
		indicator.setViewPager(pager);
		indicator.setFooterIndicatorStyle(IndicatorStyle.Underline);

		// We set this on the indicator, NOT the pager
		indicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			}

			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});
	}
}