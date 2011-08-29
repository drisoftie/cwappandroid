package de.consolewars.android.app.tab.news;

import org.apache.commons.lang.StringUtils;

import roboguice.activity.RoboActivity;
import android.app.ActivityGroup;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.inject.Inject;

import de.consolewars.android.app.CWManager;
import de.consolewars.android.app.Filter;
import de.consolewars.android.app.R;
import de.consolewars.android.app.tab.CwBasicActivityGroup;
import de.consolewars.android.app.util.DateUtility;
import de.consolewars.android.app.util.StyleSpannableStringBuilder;
import de.consolewars.android.app.util.ViewUtility;
import de.consolewars.android.app.view.IScrollListener;
import de.consolewars.android.app.view.ScrollDetectorScrollView;
import de.consolewars.api.data.News;
import de.consolewars.api.exception.ConsolewarsAPIException;

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
 * Central Activity to handle the ui for news.
 * 
 * @author Alexander Dridiger
 */
public class NewsActivity extends RoboActivity {

	@Inject
	private CWManager cwManager;
	@Inject
	private ViewUtility viewUtility;

	private ViewGroup news_layout;
	private TableLayout newsTable;
	private Button refresh;
	private ScrollDetectorScrollView scroll;
	private Spinner spinner;

	// remember last selected table row to draw the background for that row
	private View selectedRow;

	private Filter currentFilter = Filter.NEWS_ALL;
	private int oldestNewsID = -1;
	private boolean initUI = false;;

	// text styling
	private StyleSpannableStringBuilder styleStringBuilder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		news_layout = (ViewGroup) LayoutInflater.from(getParent()).inflate(R.layout.news_layout, null);
		newsTable = (TableLayout) news_layout.findViewById(R.id.news_table);
		refresh = (Button) news_layout.findViewById(R.id.news_bttn_refresh);
		scroll = (ScrollDetectorScrollView) news_layout.findViewById(R.id.news_scroll_view);
		spinner = (Spinner) news_layout.findViewById(R.id.news_filter_spinner);
		setContentView(news_layout);
		new BuildNewsAsyncTask().execute();
	}

	/**
	 * Asynchronous task to receive news from the API and build up the ui.
	 * 
	 * @author Alexander Dridiger
	 */
	private class BuildNewsAsyncTask extends AsyncTask<Boolean, View, Void> {

		@Override
		protected void onPreExecute() {
			// first set progressbar
			ViewGroup progress_layout = viewUtility.getCenteredProgressBarLayout(
					LayoutInflater.from(NewsActivity.this.getParent()), R.string.tab_news_head);
			newsTable.addView(progress_layout);
			scroll.removeScrollListener();
			refresh.setClickable(false);
		}

		@Override
		protected Void doInBackground(Boolean... params) {
			if (params.length > 0 && params[0]) {
				if (!cwManager.getNews().isEmpty()) {
					try {
						cwManager.getNewsByIDAndStore(cwManager.getNews().get(cwManager.getNews().size() - 1).getId(),
								true);
					} catch (ConsolewarsAPIException e) {
						e.printStackTrace();
					}
				}
			}
			createNewsRows();
			return null;
		}

		@Override
		protected void onProgressUpdate(View... rows) {
			if (!isCancelled()) {
				newsTable.addView(rows[0], newsTable.getChildCount() - 1);
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			newsTable.removeViewAt(newsTable.getChildCount() - 1);
			if (!initUI) {
				initFilter();
				initRefreshBttn();
			}
			refresh.setClickable(true);
			initScroll();
			initUI = true;
		}

		/**
		 * Create rows displaying single news to be displayed in a table.
		 */
		private void createNewsRows() {
			// create table based on current news
			ViewGroup separator = null;
			TextView separatorTxt = null;

			for (int i = 0; i < cwManager.getNews().size(); i++) {
				News newsToAdd = cwManager.getNews().get(i);
				if (!isCancelled() && (oldestNewsID == -1 || newsToAdd.getId() < oldestNewsID)) {
					if (i == 0
							|| DateUtility
									.getDay(DateUtility.createCalendarFromUnixtime(cwManager.getNews().get(i - 1)
											.getUnixtime() * 1000L), 0).getTimeInMillis() > newsToAdd.getUnixtime() * 1000L) {
						// current news was not created on the same date as the last news --> separator necessary
						separator = (ViewGroup) LayoutInflater.from(NewsActivity.this.getParent()).inflate(
								R.layout.news_row_day_separator_layout, null);
						separatorTxt = (TextView) separator.findViewById(R.id.news_row_day_separator_text);
						separatorTxt.setText(createDate(
								DateUtility.getDay(
										DateUtility.createCalendarFromUnixtime(newsToAdd.getUnixtime() * 1000L), 0)
										.getTimeInMillis(), "EEEE, dd. MMMMM yyyy"));
						publishProgress(separator);
					}
					// check if the news has to be filtered
					if (currentFilter.equals(Filter.NEWS_ALL) || matchesFilter(newsToAdd.getCategoryshort())) {
						// get the table row by an inflater and set the needed information
						final View tableRow = LayoutInflater.from(NewsActivity.this).inflate(R.layout.news_row_layout,
								newsTable, false);
						tableRow.setId(newsToAdd.getId());
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
								getSingleNews(tableRow.getId());
							}
						});
						viewUtility.setCategoryIcon(((ImageView) tableRow.findViewById(R.id.news_row_category_icon)),
								newsToAdd.getCategoryshort());
						((TextView) tableRow.findViewById(R.id.news_row_title)).setText(createTitle(newsToAdd
								.getTitle()));
						((TextView) tableRow.findViewById(R.id.news_row_date)).setText(createDate(
								newsToAdd.getUnixtime() * 1000L, "'um' HH:mm'Uhr'"));
						TextView cmtAmount = (TextView) tableRow.findViewById(R.id.news_row_cmmts_amount);
						cmtAmount.setText(createAmount(newsToAdd.getComments()));
						TextView picAmount = (TextView) tableRow.findViewById(R.id.news_row_pics_amount);
						picAmount.setText(createAmount(newsToAdd.getPiclist().length));
						TextView author = (TextView) tableRow.findViewById(R.id.news_row_author);
						author.setText(createAuthor(newsToAdd.getAuthor()));
						author.setSelected(true);

						publishProgress(tableRow);
					}
					if (i == cwManager.getNews().size() - 1) {
						oldestNewsID = newsToAdd.getId();
					}
				}
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

		/**
		 * Filter ui and logic for filtering news.
		 * 
		 * @param parentView
		 *            to find the inflated view elements.
		 */
		private void initFilter() {
			ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getParent(),
					R.array.news_filter_options, android.R.layout.simple_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(adapter);
			spinner.setSelection(currentFilter.getPosition());
			spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> aView, View view, int position, long id) {
					Filter selected;
					if (Filter.NEWS_MS.getPosition() == position) {
						selected = Filter.NEWS_MS;
					} else if (Filter.NEWS_NIN.getPosition() == position) {
						selected = Filter.NEWS_NIN;
					} else if (Filter.NEWS_SONY.getPosition() == position) {
						selected = Filter.NEWS_SONY;
					} else {
						selected = Filter.NEWS_ALL;
					}

					if (!currentFilter.equals(selected)) {
						cancel(true);
						currentFilter = selected;
						newsTable.removeAllViews();
						oldestNewsID = -1;
						new BuildNewsAsyncTask().execute();
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// not needed
				}
			});
		}

		private void initScroll() {
			scroll.setOnScrollListener(new IScrollListener() {
				@Override
				public void onScrollChanged(ScrollDetectorScrollView scrollView, int x, int y, int oldx, int oldy) {
					// Grab the last child placed in the ScrollView, we need it to determinate the bottom position.
					View view = (View) scrollView.getChildAt(scrollView.getChildCount() - 1);

					// Calculate the scrolldiff
					int diff = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));

					// if diff is zero, then the bottom has been reached
					if (diff == 0) {
						new BuildNewsAsyncTask().execute(true);
					}
				}
			});
		}

		/**
		 * Changes the current activity to a {@link SingleNewsActivity} with the selected news.
		 * 
		 * @param id
		 */
		private void getSingleNews(int id) {
			Intent singleNewsIntent = new Intent(NewsActivity.this, SingleNewsActivity.class);

			singleNewsIntent.putExtra(NewsActivity.class.getName(), id);

			View view = ((ActivityGroup) getParent())
					.getLocalActivityManager()
					.startActivity(SingleNewsActivity.class.getSimpleName(),
							singleNewsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)).getDecorView();
			// replace the view
			((NewsActivityGroup) getParent()).replaceView(view);
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
	public void onBackPressed() {
		if (getParent() instanceof CwBasicActivityGroup) {
			((CwBasicActivityGroup) getParent()).back();
		}
	}
}
