package de.consolewars.android.app.tab.news;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

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
import de.consolewars.api.data.News;

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
	private Calendar oldestNewsDate;
	private Calendar currentNewsDate;

	// remember last selected table row to draw the background for that row
	private View selectedRow;

	private Filter currentFilter = Filter.NEWS_ALL;

	private StyleSpannableStringBuilder styleStringBuilder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		resetDates();
		news_layout = (ViewGroup) LayoutInflater.from(getParent()).inflate(R.layout.news_layout, null);
		setContentView(news_layout);
		new BuildNewsAsyncTask().execute();
	}

	private void resetDates() {
		// TimeZone zone = TimeZone.getTimeZone("ECT");
		TimeZone zone = TimeZone.getDefault();

		oldestNewsDate = Calendar.getInstance(zone, Locale.GERMANY);
		oldestNewsDate.setTimeInMillis(DateUtility.getDay(oldestNewsDate, 0).getTimeInMillis());
		currentNewsDate = Calendar.getInstance(zone, Locale.GERMANY);
		currentNewsDate.setTimeInMillis(DateUtility.getDay(currentNewsDate, 1).getTimeInMillis() - 1L);
	}

	/**
	 * Asynchronous task to receive news from the API and build up the ui.
	 * 
	 * @author Alexander Dridiger
	 */
	private class BuildNewsAsyncTask extends AsyncTask<Void, View, Void> {

		@Override
		protected void onPreExecute() {
			// first set progressbar
			ViewGroup progress_layout = viewUtility.getCenteredProgressBarLayout(
					LayoutInflater.from(NewsActivity.this.getParent()), R.string.tab_news_head);
			TableLayout newsTable = (TableLayout) news_layout.findViewById(R.id.news_table);
			newsTable.addView(progress_layout);
		}

		@Override
		protected Void doInBackground(Void... params) {
			oldestNewsDate.setTimeInMillis(DateUtility.getDay(oldestNewsDate, 0).getTimeInMillis());
			createNewsRows();
			return null;
		}

		@Override
		protected void onProgressUpdate(View... rows) {
			TableLayout newsTable = (TableLayout) news_layout.findViewById(R.id.news_table);
			newsTable.addView(rows[0], newsTable.getChildCount() - 1);
		}

		@Override
		protected void onPostExecute(Void result) {
			// sets the news view for this Activity
			TableLayout newsTable = (TableLayout) news_layout.findViewById(R.id.news_table);

			newsTable.removeViewAt(newsTable.getChildCount() - 1);
			initFilter(news_layout);
			initRefreshBttn(news_layout);

			ViewGroup lastrowLayout = (ViewGroup) LayoutInflater.from(NewsActivity.this.getParent()).inflate(
					R.layout.day_down_row_layout, null);
			Button downBttn = (Button) lastrowLayout.findViewById(R.id.day_down_row_bttn);
			downBttn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					currentNewsDate.setTimeInMillis(oldestNewsDate.getTimeInMillis() - 1L);
					oldestNewsDate.setTimeInMillis(DateUtility.getDay(oldestNewsDate, -1).getTimeInMillis());
					TableLayout newsTable = (TableLayout) news_layout.findViewById(R.id.news_table);
					newsTable.removeViewAt(newsTable.getChildCount() - 1);
					new BuildNewsAsyncTask().execute();
				}
			});
			newsTable.addView(lastrowLayout);
		}

		/**
		 * Create rows displaying single news to be displayed in a table.
		 */
		private void createNewsRows() {
			// create table based on current news
			TableLayout newsTable = (TableLayout) news_layout.findViewById(R.id.news_table);

			styleStringBuilder = new StyleSpannableStringBuilder();

			ViewGroup separator = (ViewGroup) LayoutInflater.from(NewsActivity.this.getParent()).inflate(
					R.layout.news_row_day_separator_layout, null);
			TextView separatorTxt = (TextView) separator.findViewById(R.id.news_row_day_separator_text);
			separatorTxt.setText(createDate(currentNewsDate.getTimeInMillis(), "EEEE, dd. MMMMM yyyy"));
			publishProgress(separator);

			Calendar tempCal = Calendar.getInstance(Locale.GERMANY);
			tempCal.setTimeInMillis(currentNewsDate.getTimeInMillis() + 1L);

			for (News newsToAdd : cwManager.getNews()) {
				if (DateUtility.getDay(tempCal, -1).getTimeInMillis() > newsToAdd.getUnixtime() * 1000L) {
					currentNewsDate.setTimeInMillis(currentNewsDate.getTimeInMillis() + 1L);
					currentNewsDate.setTimeInMillis(DateUtility.getDay(currentNewsDate, -1).getTimeInMillis() - 1L);
					tempCal.setTimeInMillis(currentNewsDate.getTimeInMillis() + 1L);
					separator = (ViewGroup) LayoutInflater.from(NewsActivity.this.getParent()).inflate(
							R.layout.news_row_day_separator_layout, null);
					separatorTxt = (TextView) separator.findViewById(R.id.news_row_day_separator_text);
					separatorTxt.setText(createDate(currentNewsDate.getTimeInMillis(), "EEEE, dd. MMMMM yyyy"));
					publishProgress(separator);
				} else if ((currentNewsDate.getTimeInMillis() >= newsToAdd.getUnixtime() * 1000L)
						&& (DateUtility.getDay(tempCal, -1).getTimeInMillis() <= newsToAdd.getUnixtime() * 1000L)) {
					// check if the news has to be filtered
					if (currentFilter.equals(Filter.NEWS_ALL) || matchesFilter(newsToAdd)) {
						// get the table row by an inflater and set the needed information
						final View tableRow = LayoutInflater.from(NewsActivity.this).inflate(R.layout.news_row_layout,
								newsTable, false);
						tableRow.setId(newsToAdd.getId());
						tableRow.setOnClickListener(new View.OnClickListener() {

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
				}
			}
		}

		private boolean matchesFilter(News news) {
			boolean matches = false;
			String cat = news.getCategoryshort().toLowerCase();
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
		private void initRefreshBttn(ViewGroup parent) {
			Button refresh = (Button) parent.findViewById(R.id.news_bttn_refresh);
			refresh.setOnClickListener(new OnClickListener() {

				public void onClick(View arg0) {
					TableLayout newsTable = (TableLayout) news_layout.findViewById(R.id.news_table);
					newsTable.removeAllViews();
					resetDates();
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
		private void initFilter(final View parentView) {
			Spinner spinner = (Spinner) parentView.findViewById(R.id.news_filter_spinner);
			ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getParent(),
					R.array.news_filter_options, android.R.layout.simple_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(adapter);
			spinner.setSelection(currentFilter.getPosition());
			spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

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
						currentFilter = selected;
						TableLayout newsTable = (TableLayout) parentView.findViewById(R.id.news_table);
						newsTable.removeAllViews();
						resetDates();
						new BuildNewsAsyncTask().execute();
					}
				}

				public void onNothingSelected(AdapterView<?> arg0) {
					// not needed
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
			styleStringBuilder.clear();
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
			styleStringBuilder.clear();
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
