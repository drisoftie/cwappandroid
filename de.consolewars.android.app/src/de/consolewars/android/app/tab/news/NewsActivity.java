package de.consolewars.android.app.tab.news;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import android.app.Activity;
import android.app.ActivityGroup;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TableLayout;
import android.widget.TextView;
import de.consolewars.android.app.R;
import de.consolewars.android.app.tab.CwNavigationMainTabActivity;
import de.consolewars.android.app.util.StyleSpannableStringBuilder;
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
public class NewsActivity extends Activity {

	private List<News> news;

	// remember last selected table row to draw the background
	private View selectedRow;

	private int currentFilter = 0;
	private final int NOFILTER = 0;
	private final int MSFILTER = 1;
	private final int NINFILTER = 2;
	private final int SONYFILTER = 3;

	private StyleSpannableStringBuilder styleStringBuilder;

	private CwNavigationMainTabActivity mainTabs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*
		 * TODO: Might become a source of error someday, if activity design changes. Would be better
		 * to handle it with intents.
		 */
		if (getParent().getParent() instanceof CwNavigationMainTabActivity) {
			mainTabs = (CwNavigationMainTabActivity) getParent().getParent();
			mainTabs.getUsedTabHost().setOnTabChangedListener(new OnTabChangeListener() {
				@Override
				public void onTabChanged(String tabId) {
				}
			});
		}
		new BuildNewsAsyncTask().execute();
	}

	private void initRefreshBttn(ViewGroup parent) {
		Button refresh = (Button) parent.findViewById(R.id.news_bttn_refresh);
		refresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				new BuildNewsAsyncTask().execute();
			}
		});
	}

	/**
	 * Create rows displaying single news to be displayed in a table.
	 */
	private List<View> createNewsRows() {
		// create table based on current news
		View newsView = LayoutInflater.from(NewsActivity.this.getParent()).inflate(
				R.layout.news_layout, null);
		TableLayout newsTable = (TableLayout) newsView.findViewById(R.id.news_table);

		styleStringBuilder = new StyleSpannableStringBuilder();

		List<View> rows = new ArrayList<View>();

		for (News news : this.news) {
			// get the table row by an inflater and set the needed information
			final View tableRow = LayoutInflater.from(this).inflate(R.layout.news_row_layout,
					newsTable, false);
			tableRow.setId(news.getId());
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
			((ImageView) tableRow.findViewById(R.id.news_row_category_icon))
					.setImageResource(createCategoryIcon(news.getCategoryshort()));
			((TextView) tableRow.findViewById(R.id.news_row_title)).setText(createTitle(news
					.getTitle()));
			((TextView) tableRow.findViewById(R.id.news_row_date)).setText(createDate(news
					.getUnixtime() * 1000L));
			TextView author = (TextView) tableRow.findViewById(R.id.news_row_author_cmmnts);
			author.setSelected(true);
			author.setText(createCommentAndAuthor(news.getComments(), news.getAuthor()));

			rows.add(tableRow);
		}
		styleStringBuilder = null;
		return rows;
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
		spinner.setAdapter(adapter);
		spinner.setSelection(currentFilter);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> aView, View view, int position, long id) {
				int selected;
				switch (position) {
				case NOFILTER:
					selected = NOFILTER;
					break;
				case MSFILTER:
					selected = MSFILTER;
					break;
				case NINFILTER:
					selected = NINFILTER;
					break;
				case SONYFILTER:
					selected = SONYFILTER;
					break;
				default:
					selected = NOFILTER;
					break;
				}
				if (currentFilter != selected) {
					currentFilter = selected;
					TableLayout newsTable = (TableLayout) parentView.findViewById(R.id.news_table);
					newsTable.removeAllViews();
					new BuildNewsAsyncTask().execute();
				}
			}

			@Override
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
	private CharSequence createCommentAndAuthor(int commentAmount, String author) {
		// TODO more text formatting
		// an empty author string means that the news was not written by a
		if (author.matches("")) {
			author = getString(R.string.news_author_unknown);
		}
		styleStringBuilder.clear();
		styleStringBuilder.appendWithStyle(new ForegroundColorSpan(0xFF007711),
				getString(R.string.news_author_by));
		styleStringBuilder.append(" ");
		styleStringBuilder.appendWithStyle(new ForegroundColorSpan(0xFF009933), author);
		styleStringBuilder.append(" ");
		styleStringBuilder.appendWithStyle(new ForegroundColorSpan(0xFFf3d135),
				getString(R.string.comments_abrv));
		styleStringBuilder.append(" ");
		styleStringBuilder.appendWithStyle(new ForegroundColorSpan(0xFF7e6003),
				String.valueOf(commentAmount));

		return styleStringBuilder;
	}

	/**
	 * @param unixtime
	 * @return
	 */
	private CharSequence createDate(long unixtime) {
		Date date = new Date(unixtime);
		TimeZone zone = TimeZone.getDefault();

		Calendar cal = Calendar.getInstance(zone, Locale.GERMANY);
		SimpleDateFormat dateformat = new SimpleDateFormat("HH:mm dd.MM.yyyy");
		dateformat.setCalendar(cal);
		return dateformat.format(date);
	}

	/**
	 * @param title
	 * @return
	 */
	private CharSequence createTitle(String title) {
		// TODO text formatting
		return title;
	}

	/**
	 * Simply checks for categories and assigns the corresponding icon to the given view.
	 * 
	 * @param category
	 *            the category to be checked
	 * @param icon
	 *            the view that holds the icon
	 * @return the view assigned with the icon
	 */
	private int createCategoryIcon(String category) {
		// TODO default icon is "sidewatch" so far, get perhaps another one
		int imageResource = R.drawable.cat_side;
		if (category.matches(getString(R.string.cat_360))) {
			imageResource = R.drawable.cat_360;
		} else if (category.matches(getString(R.string.cat_3ds))) {
			imageResource = R.drawable.cat_3ds;
		} else if (category.matches(getString(R.string.cat_cmmt))) {
			imageResource = R.drawable.cat_cmmt;
		} else if (category.matches(getString(R.string.cat_con))) {
			imageResource = R.drawable.cat_con;
		} else if (category.matches(getString(R.string.cat_dc))) {
			imageResource = R.drawable.cat_dc;
		} else if (category.matches(getString(R.string.cat_ds))) {
			imageResource = R.drawable.cat_ds;
		} else if (category.matches(getString(R.string.cat_emu))) {
			imageResource = R.drawable.cat_emu;
		} else if (category.matches(getString(R.string.cat_fun))) {
			imageResource = R.drawable.cat_fun;
		} else if (category.matches(getString(R.string.cat_gba))) {
			imageResource = R.drawable.cat_gba;
		} else if (category.matches(getString(R.string.cat_int))) {
			imageResource = R.drawable.cat_int;
		} else if (category.matches(getString(R.string.cat_iphn))) {
			imageResource = R.drawable.cat_iphn;
		} else if (category.matches(getString(R.string.cat_mov))) {
			imageResource = R.drawable.cat_mov;
		} else if (category.matches(getString(R.string.cat_ms))) {
			imageResource = R.drawable.cat_ms;
		} else if (category.matches(getString(R.string.cat_ngc))) {
			imageResource = R.drawable.cat_ngc;
		} else if (category.matches(getString(R.string.cat_nin))) {
			imageResource = R.drawable.cat_nin;
		} else if (category.matches(getString(R.string.cat_ps))) {
			imageResource = R.drawable.cat_ps;
		} else if (category.matches(getString(R.string.cat_ps2))) {
			imageResource = R.drawable.cat_ps2;
		} else if (category.matches(getString(R.string.cat_ps3))) {
			imageResource = R.drawable.cat_ps3;
		} else if (category.matches(getString(R.string.cat_ps4))) {
			imageResource = R.drawable.cat_ps4;
		} else if (category.matches(getString(R.string.cat_psn))) {
			imageResource = R.drawable.cat_psn;
		} else if (category.matches(getString(R.string.cat_psp))) {
			imageResource = R.drawable.cat_psp;
		} else if (category.matches(getString(R.string.cat_sega))) {
			imageResource = R.drawable.cat_sega;
		} else if (category.matches(getString(R.string.cat_side))) {
			imageResource = R.drawable.cat_side;
		} else if (category.matches(getString(R.string.cat_son))) {
			imageResource = R.drawable.cat_son;
		} else if (category.matches(getString(R.string.cat_wii))) {
			imageResource = R.drawable.cat_wii;
		} else if (category.matches(getString(R.string.cat_ww))) {
			imageResource = R.drawable.cat_ww;
		} else if (category.matches(getString(R.string.cat_xbla))) {
			imageResource = R.drawable.cat_xbla;
		} else if (category.matches(getString(R.string.cat_xbox))) {
			imageResource = R.drawable.cat_xbox;
		}
		return imageResource;
	}

	/**
	 * Asynchronous task to receive news from the API and build up the ui.
	 * 
	 * @author Alexander Dridiger
	 */
	private class BuildNewsAsyncTask extends AsyncTask<Void, Integer, List<View>> {

		private ProgressBar progressBar;

		@Override
		protected void onPreExecute() {
			// first set progressbar view
			ViewGroup progress_layout = (ViewGroup) LayoutInflater.from(
					NewsActivity.this.getParent()).inflate(R.layout.centered_progressbar, null);
			setContentView(progress_layout);

			TextView text = (TextView) progress_layout.findViewById(R.id.centered_progressbar_text);
			text.setText(getString(R.string.loading, "News"));

			progressBar = (ProgressBar) progress_layout.findViewById(R.id.centered_progressbar);
			progressBar.setProgress(0);
		}

		@Override
		protected List<View> doInBackground(Void... params) {
			try {
				mainTabs.getApiCaller().authenticateOnCW();
				news = mainTabs.getApiCaller().getApi().getNewsList(15, currentFilter);
			} catch (ConsolewarsAPIException e) {
				e.printStackTrace();
				Log.e(getString(R.string.exc_auth_tag), e.getMessage(), e);
			}
			return createNewsRows();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			progressBar.setProgress(values[0]);
		}

		@Override
		protected void onPostExecute(List<View> result) {
			// sets the news view for this Activity
			ViewGroup news_layout = (ViewGroup) LayoutInflater.from(NewsActivity.this.getParent())
					.inflate(R.layout.news_layout, null);

			TableLayout newsTable = (TableLayout) news_layout.findViewById(R.id.news_table);
			for (View row : result) {
				newsTable.addView(row);
			}
			initFilter(news_layout);
			initRefreshBttn(news_layout);
			setContentView(news_layout);
		}
	}
}
