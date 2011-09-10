package de.consolewars.android.app.tab.news;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import de.consolewars.android.app.Filter;
import de.consolewars.android.app.R;
import de.consolewars.android.app.view.TitleProvider;

class CwNewsPagerAdapter extends FragmentPagerAdapter implements TitleProvider {

	private String[] newsFilter;

	public CwNewsPagerAdapter(FragmentManager fm, Context context) {
		super(fm);
		newsFilter = context.getResources().getStringArray(R.array.news_filter_options);
	}

	@Override
	public Fragment getItem(int position) {
		switch (position) {
		case 0:
			return new NewsFragment(Filter.NEWS_ALL);
		case 1:
			return new NewsFragment(Filter.NEWS_MS);
		case 2:
			return new NewsFragment(Filter.NEWS_NIN);
		case 3:
			return new NewsFragment(Filter.NEWS_SONY);
		default:
			break;
		}
		return new NewsFragment(Filter.NEWS_ALL);
	}

	@Override
	public int getCount() {
		return newsFilter.length;
	}

	@Override
	public String getTitle(int position) {
		return newsFilter[position];
	}
}