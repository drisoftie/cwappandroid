package de.consolewars.android.app.tab.blogs;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import de.consolewars.android.app.R;
import de.consolewars.android.app.tab.CwAbstractFragmentActivity;
import de.consolewars.android.app.view.TitlePageIndicator;
import de.consolewars.android.app.view.TitlePageIndicator.IndicatorStyle;

public class CwBlogsFragmentActivity extends CwAbstractFragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.blogs_layout);

		ViewPager pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(new CwBlogsPagerAdapter(getSupportFragmentManager(), getApplicationContext()));

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

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menu.clear();
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.news_menu, menu);
		return true;
	}
}