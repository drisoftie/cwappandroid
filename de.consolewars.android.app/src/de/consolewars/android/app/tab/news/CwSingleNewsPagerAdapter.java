package de.consolewars.android.app.tab.news;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import de.consolewars.android.app.R;
import de.consolewars.android.app.db.domain.CwNews;
import de.consolewars.android.app.tab.cmts.CommentsFragment;
import de.consolewars.android.app.view.TitleProvider;

class CwSingleNewsPagerAdapter extends FragmentPagerAdapter implements TitleProvider {

	private CwNews news;
	private String[] paging_titles;

	public CwSingleNewsPagerAdapter(FragmentManager fm, Context context, CwNews news) {
		super(fm);
		this.news = news;
		paging_titles = context.getResources().getStringArray(R.array.singlenews_paging);
	}

	@Override
	public Fragment getItem(int position) {
		switch (position) {
		case 0:
			return new SingleNewsFragment(news);
		case 1:
			return new CommentsFragment(news);
		default:
			break;
		}
		return new SingleNewsFragment(news);
	}

	@Override
	public int getCount() {
		return paging_titles.length;
	}

	@Override
	public String getTitle(int position) {
		return paging_titles[position];
	}
}