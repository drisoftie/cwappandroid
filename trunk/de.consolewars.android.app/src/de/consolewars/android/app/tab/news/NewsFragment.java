package de.consolewars.android.app.tab.news;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import de.consolewars.android.app.CwApplication;
import de.consolewars.android.app.Filter;
import de.consolewars.android.app.R;
import de.consolewars.android.app.db.domain.CwNews;
import de.consolewars.android.app.util.DateUtility;
import de.consolewars.android.app.util.StyleSpannableStringBuilder;
import de.consolewars.android.app.view.IScrollListener;
import de.consolewars.android.app.view.ScrollDetectorScrollView;
import de.consolewars.api.exception.ConsolewarsAPIException;

public final class NewsFragment extends Fragment {

	private LayoutInflater inflater;
	private ViewGroup news_layout;
	private TableLayout newsTable;
	private Button refresh;
	private ScrollDetectorScrollView scroll;

	// remember last selected table row to draw the background for that row
	private View selectedRow;

	private List<CwNews> tempList = new ArrayList<CwNews>();
	private Filter currentFilter = Filter.NEWS_ALL;
	private int oldestNewsID = -1;

	// text styling
	private StyleSpannableStringBuilder styleStringBuilder;

	public NewsFragment() {
		super();
	}

	public NewsFragment(Filter filter) {
		super();
		setHasOptionsMenu(true);
		this.currentFilter = filter;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
		// mContent = savedInstanceState.getString(KEY_CONTENT);
		// }
		this.inflater = inflater;
		news_layout = (ViewGroup) inflater.inflate(R.layout.news_fragment_layout, null);
		newsTable = (TableLayout) news_layout.findViewById(R.id.news_table);
		refresh = (Button) news_layout.findViewById(R.id.news_bttn_refresh);
		scroll = (ScrollDetectorScrollView) news_layout.findViewById(R.id.news_scroll_view);
		return news_layout;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (newsTable.getChildCount() == 0) {
			new BuildNewsAsyncTask().execute();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// outState.putString(KEY_CONTENT, mContent);
	}

	/**
	 * Asynchronous task to receive news from the API and build up the ui.
	 * 
	 * @author Alexander Dridiger
	 */
	private class BuildNewsAsyncTask extends AsyncTask<Boolean, View, Void> {

		boolean doesWork = false;

		@Override
		protected void onPreExecute() {
			doesWork = true;
			// first set progressbar
			ViewGroup progress_layout = CwApplication.cwViewUtil().getCenteredProgressBarLayout(inflater,
					R.string.tab_news_head);
			newsTable.addView(progress_layout);
			scroll.removeScrollListener();
			refresh.setClickable(false);
		}

		@Override
		protected Void doInBackground(Boolean... params) {
			if (params.length > 0 && params[0]) {
				if (!CwApplication.cwManager().getCwNews().isEmpty()) {
					try {
						CwApplication
								.cwManager()
								.getCwNewsByIDAndStore(
										CwApplication.cwManager().getCwNews()
												.get(CwApplication.cwManager().getCwNews().size() - 1).getSubjectId() - 1,
										true);
					} catch (ConsolewarsAPIException e) {
						e.printStackTrace();
					}
				}
			}
			refreshList();
			createNewsRows();
			return null;
		}

		@Override
		protected void onProgressUpdate(View... rows) {
			if (!isCancelled()) {
				newsTable.addView(rows[0], newsTable.getChildCount() - 1);
				oldestNewsID = rows[0].getId();
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			newsTable.removeViewAt(newsTable.getChildCount() - 1);
			initRefreshBttn();
			refresh.setClickable(true);
			initScroll();
			if (newsTable.getChildCount() == 0) {
				oldestNewsID = -1;
			}
			doesWork = false;
			news_layout.invalidate();
		}

		/**
		 * Create rows displaying single news to be displayed in a table.
		 */
		private void createNewsRows() {
			// create table based on current news
			ViewGroup separator = null;
			TextView separatorTxt = null;
			for (int i = 0; i < tempList.size(); i++) {
				final CwNews newsToAdd = tempList.get(i);
				if (!isCancelled() && (oldestNewsID == -1 || newsToAdd.getSubjectId() < oldestNewsID)) {
					// check if the news has to be filtered
					if (currentFilter.equals(Filter.NEWS_ALL) || matchesFilter(newsToAdd.getCategoryShort())) {
						// get the table row by an inflater and set the needed information
						final View tableRow = inflater.inflate(R.layout.news_row_layout, newsTable, false);
						tableRow.setId(newsToAdd.getSubjectId());
						tableRow.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								if (selectedRow != null) {
									selectedRow.setBackgroundDrawable(getResources().getDrawable(
											R.drawable.table_cell_bg));
								}
								tableRow.setBackgroundDrawable(getResources().getDrawable(
										R.drawable.table_cell_bg_selected));
								selectedRow = tableRow;
								getSingleNews(newsToAdd);
							}
						});
						CwApplication.cwViewUtil().setCategoryIcon(
								((ImageView) tableRow.findViewById(R.id.news_row_category_icon)),
								newsToAdd.getCategoryShort());
						((TextView) tableRow.findViewById(R.id.news_row_title)).setText(createTitle(newsToAdd
								.getTitle()));
						((TextView) tableRow.findViewById(R.id.news_row_date)).setText(createDate(
								newsToAdd.getUnixtime() * 1000L, "'um' HH:mm'Uhr'"));
						TextView cmtAmount = (TextView) tableRow.findViewById(R.id.news_row_cmmts_amount);
						cmtAmount.setText(createAmount(newsToAdd.getComments()));
						TextView picAmount = (TextView) tableRow.findViewById(R.id.news_row_pics_amount);
						picAmount.setText(createAmount((newsToAdd.getPictures() != null) ? newsToAdd.getPictures()
								.size() : 0));
						TextView author = (TextView) tableRow.findViewById(R.id.news_row_author);
						author.setText(createAuthor(newsToAdd.getAuthor()));
						author.setSelected(true);
						if (i == 0
								|| DateUtility
										.getDay(DateUtility.createCalendarFromUnixtime(tempList.get(i - 1)
												.getUnixtime() * 1000L), 0).getTimeInMillis() > newsToAdd.getUnixtime() * 1000L) {
							// current news was not created on the same date as the last news --> separator necessary
							separator = (ViewGroup) inflater.inflate(R.layout.news_row_day_separator_layout, null);
							separatorTxt = (TextView) separator.findViewById(R.id.news_row_day_separator_text);
							separatorTxt.setText(createDate(
									DateUtility.getDay(
											DateUtility.createCalendarFromUnixtime(newsToAdd.getUnixtime() * 1000L), 0)
											.getTimeInMillis(), "EEEE, dd. MMMMM yyyy"));
							publishProgress(separator);
						}
						publishProgress(tableRow);
					}
				}
			}
		}

		private void refreshList() {
			if (!currentFilter.equals(Filter.NEWS_ALL)) {
				tempList.clear();
				for (CwNews news : CwApplication.cwManager().getCwNews()) {
					if (matchesFilter(news.getCategoryShort())) {
						tempList.add(news);
					}
				}
			} else {
				tempList = CwApplication.cwManager().getCwNews();
			}
		}

		private boolean matchesFilter(String cat) {
			boolean matches = false;
			if (currentFilter.equals(Filter.NEWS_MS) && !cat.contains("ps") && !cat.contains("vita")
					&& !cat.contains("son") && !StringUtils.contains(cat, 'w') && !cat.contains("nin")
					&& !cat.contains("ds") && !cat.contains("n64") && !cat.contains("ngc") && !cat.contains("gb")
					&& !cat.contains("snes")) {
				matches = true;
			} else if (currentFilter.equals(Filter.NEWS_NIN) && !cat.contains("ps") && !cat.contains("vita")
					&& !cat.contains("son") && !StringUtils.contains(cat, 'x') && !cat.contains("ms")
					&& !cat.contains("360")) {
				matches = true;
			} else if (currentFilter.equals(Filter.NEWS_SONY) && !StringUtils.contains(cat, 'w')
					&& !cat.contains("nin") && !cat.contains("ds") && !cat.contains("n64") && !cat.contains("ngc")
					&& !cat.contains("gb") && !cat.contains("snes") && !StringUtils.contains(cat, 'x')
					&& !cat.contains("ms") && !cat.contains("360")) {
				matches = true;
			}
			return matches;
		}

		/**
		 * @param parent
		 */
		private void initRefreshBttn() {
			refresh.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					cancel(true);
					newsTable.removeAllViews();
					oldestNewsID = -1;
					new BuildNewsAsyncTask().execute();
				}
			});
		}

		private void initScroll() {
			scroll.setOnScrollListener(new IScrollListener() {
				@Override
				public void onScrollChanged(ScrollDetectorScrollView scrollView, int x, int y, int oldx, int oldy) {
					if (!doesWork) {
						// Grab the last child placed in the ScrollView, we need it to determinate the bottom position.
						View view = (View) scrollView.getChildAt(scrollView.getChildCount() - 1);

						// Calculate the scrolldiff
						int diff = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));

						// if diff is zero, then the bottom has been reached
						if (scrollView.getChildCount() == 0 || diff == 0) {
							new BuildNewsAsyncTask().execute(true);
						}
					}
				}
			});
		}

		/**
		 * Changes the current activity to a {@link SingleNewsFragment} with the selected news.
		 * 
		 * @param id
		 */
		private void getSingleNews(CwNews news) {
			// Create new fragment and transaction
			Fragment singleNewsFragment = new SingleNewsFragment(news);
			FragmentTransaction transaction = getFragmentManager().beginTransaction();

			// Replace whatever is in the fragment_container view with this fragment,
			// and add the transaction to the back stack
			transaction.add(R.id.news_root, singleNewsFragment);
			transaction.addToBackStack(null);

			// Commit the transaction
			transaction.commit();
		}

		/**
		 * Creates the string for the ui cell showing the author of a news and the amount of comments.
		 * 
		 * @param commentAmount
		 * @param author
		 * @return a formatted {@link CharSequence}
		 */
		private CharSequence createAuthor(String author) {
			// TODO more text formatting
			// an empty author string means that the news was not written by a
			if (author.matches("")) {
				author = getString(R.string.news_author_unknown);
			}
			styleStringBuilder = new StyleSpannableStringBuilder();
			styleStringBuilder.appendWithStyle(new ForegroundColorSpan(0xFF007711), getString(R.string.news_author_by));
			styleStringBuilder.append(" ");
			styleStringBuilder.appendWithStyle(new ForegroundColorSpan(0xFF009933), author);

			return styleStringBuilder;
		}

		/**
		 * Creates the string for the ui cell showing the author of a news and the amount of comments.
		 * 
		 * @param commentAmount
		 * @param author
		 * @return a formatted {@link CharSequence}
		 */
		private CharSequence createAmount(int commentAmount) {
			// TODO more text formatting
			// an empty author string means that the news was not written by a
			styleStringBuilder = new StyleSpannableStringBuilder();
			styleStringBuilder.appendWithStyle(new ForegroundColorSpan(0xFF7e6003), String.valueOf(commentAmount));

			return styleStringBuilder;
		}

		/**
		 * @param unixtime
		 * @return
		 */
		private CharSequence createDate(long unixtime, String format) {
			return DateUtility.createDate(unixtime, format);
		}

		/**
		 * @param title
		 * @return
		 */
		private CharSequence createTitle(String title) {
			// TODO text formatting
			return title;
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		menu.clear();
		inflater.inflate(R.menu.news_menu, menu);
	}
}
