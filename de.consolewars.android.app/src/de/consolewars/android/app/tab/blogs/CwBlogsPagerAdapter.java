package de.consolewars.android.app.tab.blogs;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import de.consolewars.android.app.Filter;
import de.consolewars.android.app.R;
import de.consolewars.android.app.view.TitleProvider;

class CwBlogsPagerAdapter extends FragmentPagerAdapter implements TitleProvider {

	private String[] blogsFilter;

	public CwBlogsPagerAdapter(FragmentManager fm, Context context) {
		super(fm);
		blogsFilter = context.getResources().getStringArray(R.array.blogs_filter_options);
	}

	@Override
	public Fragment getItem(int position) {
		switch (position) {
		case 0:
			return new BlogsFragment(Filter.BLOGS_NORMAL);
		case 1:
			return new BlogsFragment(Filter.BLOGS_NEWS);
		case 2:
			return new BlogsFragment(Filter.BLOGS_USER);
		default:
			break;
		}
		return new BlogsFragment(Filter.BLOGS_NORMAL);
	}

	@Override
	public int getCount() {
		return blogsFilter.length;
	}

	@Override
	public String getTitle(int position) {
		return blogsFilter[position];
	}
}