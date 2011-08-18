package de.consolewars.android.app.tab.blogs;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import roboguice.activity.RoboActivity;
import android.app.ActivityGroup;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.inject.Inject;

import de.consolewars.android.app.CWApplication;
import de.consolewars.android.app.CWManager;
import de.consolewars.android.app.Filter;
import de.consolewars.android.app.R;
import de.consolewars.android.app.tab.CwBasicActivityGroup;
import de.consolewars.android.app.util.DateUtility;
import de.consolewars.android.app.util.StyleSpannableStringBuilder;
import de.consolewars.android.app.util.ViewUtility;
import de.consolewars.api.data.Blog;
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
 * Central Activity to handle the ui for blogs.
 * 
 * @author Alexander Dridiger
 */
public class BlogsActivity extends RoboActivity {

	@Inject
	private CWApplication cwApplication;
	@Inject
	private CWManager cwManager;
	@Inject
	private ViewUtility viewUtility;

	private List<Blog> blogs = new ArrayList<Blog>();
	// remember last selected table row to draw the background
	private View selectedRow;

	private Filter currentFilter = Filter.BLOGS_NORMAL;

	// text styling
	private StyleSpannableStringBuilder styleStringBuilder = new StyleSpannableStringBuilder();

	private ViewGroup blogs_layout;
	private Calendar oldestBlogsDate;
	private Calendar currentBlogsDate;

	private int count = 10;
	private boolean loading;
	BlogsSeparatorAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		resetDates();

		blogs_layout = (ViewGroup) LayoutInflater.from(getParent()).inflate(R.layout.blogs_layout, null);
		setContentView(blogs_layout);
		ListView lv = (ListView) findViewById(R.id.list);
		adapter = new BlogsSeparatorAdapter(BlogsActivity.this);
		lv.setAdapter(adapter);
		new BuildBlogsAsyncTask().execute();
	}

	private void resetDates() {
		// TimeZone zone = TimeZone.getTimeZone("ECT");
		TimeZone zone = TimeZone.getDefault();

		oldestBlogsDate = Calendar.getInstance(zone, Locale.GERMANY);
		currentBlogsDate = Calendar.getInstance(zone, Locale.GERMANY);

		if (Filter.BLOGS_USER.equals(currentFilter)) {
			oldestBlogsDate.setTimeInMillis(DateUtility.getDay(oldestBlogsDate, -30).getTimeInMillis());
			currentBlogsDate.setTimeInMillis(DateUtility.getDay(currentBlogsDate, 1).getTimeInMillis() - 1L);
		} else {
			oldestBlogsDate.setTimeInMillis(DateUtility.getDay(oldestBlogsDate, 0).getTimeInMillis());
			currentBlogsDate.setTimeInMillis(DateUtility.getDay(currentBlogsDate, 1).getTimeInMillis() - 1L);
		}
	}

	/**
	 * @param parent
	 */
	private void initRefreshBttn(ViewGroup parent) {
		Button refresh = (Button) parent.findViewById(R.id.blogs_bttn_refresh);
		refresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TableLayout newsTable = (TableLayout)
				// blogs_layout.findViewById(R.id.blogs_table);
				// newsTable.removeAllViews();
				// resetDates();
				// new BuildBlogsAsyncTask().execute();
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
		Spinner spinner = (Spinner) parentView.findViewById(R.id.blogs_filter_spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getParent(), R.array.blogs_filter_options,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setSelection(currentFilter.getPosition());
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> aView, View view, int position, long id) {
				Filter selected;
				if (Filter.BLOGS_NEWS.getPosition() == position) {
					selected = Filter.BLOGS_NEWS;
				} else if (Filter.BLOGS_USER.getPosition() == position) {
					selected = Filter.BLOGS_USER;
				} else {
					selected = Filter.BLOGS_NORMAL;
				}

				if (!currentFilter.equals(selected)) {
					currentFilter = selected;
					// TableLayout newsTable = (TableLayout)
					// parentView.findViewById(R.id.blogs_table);
					// newsTable.removeAllViews();
					resetDates();
					new BuildBlogsAsyncTask().execute();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// not needed
			}
		});
	}

	/**
	 * Asynchronous task to receive blogs from the API and build up the ui.
	 * 
	 * @author Alexander Dridiger
	 */
	private class BuildBlogsAsyncTask extends AsyncTask<Void, View, Void> {

		@Override
		protected void onPreExecute() {
			// ViewGroup progress_layout =
			// viewUtility.getCenteredProgressBarLayout(getLayoutInflater(),
			// R.string.tab_blogs_head);
			//
			// TableLayout blogsTable = (TableLayout) blogs_layout.findViewById(R.id.blogs_table);
			// blogsTable.addView(progress_layout);
		}

		@Override
		protected Void doInBackground(Void... params) {
			oldestBlogsDate.setTimeInMillis(DateUtility.getDay(oldestBlogsDate, 0).getTimeInMillis());

			try {
				if (Filter.BLOGS_USER.equals(currentFilter)) {
					blogs = cwManager.getUserBlogs(cwApplication.getAuthenticatedUser().getUid(), 50,
							oldestBlogsDate.getTime());
					int count = 0;
					while (blogs.isEmpty() && count < 6) {
						currentBlogsDate.setTimeInMillis(oldestBlogsDate.getTimeInMillis() - 1L);
						oldestBlogsDate.setTimeInMillis(DateUtility.getDay(oldestBlogsDate, -30).getTimeInMillis());
						count++;
						blogs = cwManager.getUserBlogs(cwApplication.getAuthenticatedUser().getUid(), 50,
								oldestBlogsDate.getTime());
					}
					// createUserBlogRows();
				} else {
					blogs = cwManager.getBlogs(count, currentFilter, null);
					count = count + 10;
					// createBlogRows();
				}
			} catch (ConsolewarsAPIException e) {
				e.printStackTrace();
				Log.e(getString(R.string.exc_auth_tag), e.getMessage(), e);
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(View... rows) {
			adapter.notifyDataSetChanged();
			loading = false;
			// TableLayout blogsTable = (TableLayout) blogs_layout.findViewById(R.id.blogs_table);
			// blogsTable.addView(rows[0], blogsTable.getChildCount() - 1);
		}

		@Override
		protected void onPostExecute(Void result) {
			ListView lv = (ListView) findViewById(R.id.list);
			lv.setOnScrollListener(new OnScrollListener() {
				@Override
				public void onScrollStateChanged(AbsListView view, int scrollState) {
					// do nothing
				}

				@Override
				public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
					if (firstVisibleItem + visibleItemCount >= totalItemCount && !loading) {
						loading = true;
						new BuildBlogsAsyncTask().execute();
					}
				}
			});
			// createLastRow();
		}

		private void createLastRow() {
			// sets the blogs view for this Activity
			TableLayout blogsTable = (TableLayout) blogs_layout.findViewById(/* R.id.blogs_table */1);

			blogsTable.removeViewAt(blogsTable.getChildCount() - 1);
			initFilter(blogs_layout);
			initRefreshBttn(blogs_layout);

			ViewGroup lastrowLayout = (ViewGroup) LayoutInflater.from(BlogsActivity.this.getParent()).inflate(
					R.layout.day_down_row_layout, null);
			Button downBttn = (Button) lastrowLayout.findViewById(R.id.day_down_row_bttn);
			downBttn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (Filter.BLOGS_USER.equals(currentFilter)) {
						currentBlogsDate.setTimeInMillis(oldestBlogsDate.getTimeInMillis() - 1L);
						oldestBlogsDate.setTimeInMillis(DateUtility.getDay(oldestBlogsDate, -30).getTimeInMillis());
					} else {
						currentBlogsDate.setTimeInMillis(oldestBlogsDate.getTimeInMillis() - 1L);
						oldestBlogsDate.setTimeInMillis(DateUtility.getDay(oldestBlogsDate, -1).getTimeInMillis());
					}
					TableLayout blogsTable = (TableLayout) blogs_layout.findViewById(/*
																					 * R.id.blogs_table
																					 */1);
					blogsTable.removeViewAt(blogsTable.getChildCount() - 1);
					new BuildBlogsAsyncTask().execute();
				}
			});
			blogsTable.addView(lastrowLayout);
		}

		/**
		 * Create rows displaying single user blogs to be displayed in a table.
		 */
		private void createUserBlogRows() {
			// create table based on current blogs
			TableLayout blogsTable = (TableLayout) blogs_layout.findViewById(/* R.id.blogs_table */1);

			styleStringBuilder = new StyleSpannableStringBuilder();
			Calendar tempCal = Calendar.getInstance(Locale.GERMANY);
			tempCal.setTimeInMillis(currentBlogsDate.getTimeInMillis() + 1L);

			for (Blog blog : blogs) {
				if (DateUtility.getDay(tempCal, -30).getTimeInMillis() > blog.getUnixtime() * 1000L) {
					currentBlogsDate.setTimeInMillis(currentBlogsDate.getTimeInMillis() + 1L);
					currentBlogsDate.setTimeInMillis(DateUtility.getDay(currentBlogsDate, -30).getTimeInMillis() - 1L);
					tempCal.setTimeInMillis(currentBlogsDate.getTimeInMillis() + 1L);
				} else if ((currentBlogsDate.getTimeInMillis() >= blog.getUnixtime() * 1000L)
						&& (DateUtility.getDay(tempCal, -30).getTimeInMillis() <= blog.getUnixtime() * 1000L)) {
					// get the table row by an inflater and set the needed
					// information
					final View tableRow = LayoutInflater.from(BlogsActivity.this).inflate(R.layout.blogs_row_layout,
							blogsTable, false);
					tableRow.setId(blog.getId());
					tableRow.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							// set the correct background when a table row was selected by the user
							if (selectedRow != null) {
								selectedRow.setBackgroundDrawable(getResources().getDrawable(R.drawable.table_cell_bg));
							}
							tableRow.setBackgroundDrawable(getResources()
									.getDrawable(R.drawable.table_cell_bg_selected));
							selectedRow = tableRow;
							getSingleBlog(tableRow.getId());
						}
					});
					// set each table row with the given information from the returned blogs
					viewUtility.setUserIcon(((ImageView) tableRow.findViewById(R.id.blogs_row_user_icon)),
							blog.getUid(), 30);
					((TextView) tableRow.findViewById(R.id.blogs_row_title)).setText(createTitle(blog.getTitle()));
					((TextView) tableRow.findViewById(R.id.blogs_row_date)).setText(createDate(
							blog.getUnixtime() * 1000L, "'um' HH:mm'Uhr'"));
					TextView amount = (TextView) tableRow.findViewById(R.id.blogs_row_cmmts_amount);
					amount.setText(createCommentsAmount(blog.getComments()));

					TextView author = (TextView) tableRow.findViewById(R.id.blogs_row_author);
					author.setText(createAuthor(blog.getAuthor()));
					author.setSelected(true);

					publishProgress(tableRow);
				}
			}
			styleStringBuilder = null;
		}

		/**
		 * Create rows displaying single blogs to be displayed in a table.
		 */
		private void createBlogRows() {
			// create table based on current blogs
			TableLayout blogsTable = (TableLayout) blogs_layout.findViewById(/* R.id.blogs_table */1);

			styleStringBuilder = new StyleSpannableStringBuilder();

			ViewGroup separator = (ViewGroup) LayoutInflater.from(BlogsActivity.this.getParent()).inflate(
					R.layout.blogs_row_day_separator_layout, null);
			TextView separatorTxt = (TextView) separator.findViewById(R.id.blogs_row_day_separator_text);
			separatorTxt.setText(createDate(currentBlogsDate.getTimeInMillis(), "EEEE, dd. MMMMM yyyy"));
			publishProgress(separator);

			Calendar tempCal = Calendar.getInstance(Locale.GERMANY);
			tempCal.setTimeInMillis(currentBlogsDate.getTimeInMillis() + 1L);

			for (Blog blog : blogs) {
				if (DateUtility.getDay(tempCal, -1).getTimeInMillis() > blog.getUnixtime() * 1000L) {
					currentBlogsDate.setTimeInMillis(currentBlogsDate.getTimeInMillis() + 1L);
					currentBlogsDate.setTimeInMillis(DateUtility.getDay(currentBlogsDate, -1).getTimeInMillis() - 1L);
					tempCal.setTimeInMillis(currentBlogsDate.getTimeInMillis() + 1L);
					separator = (ViewGroup) LayoutInflater.from(BlogsActivity.this.getParent()).inflate(
							R.layout.blogs_row_day_separator_layout, null);
					separatorTxt = (TextView) separator.findViewById(R.id.blogs_row_day_separator_text);
					separatorTxt.setText(createDate(currentBlogsDate.getTimeInMillis(), "EEEE, dd. MMMMM yyyy"));
					publishProgress(separator);
				} else if ((currentBlogsDate.getTimeInMillis() >= blog.getUnixtime() * 1000L)
						&& (DateUtility.getDay(tempCal, -1).getTimeInMillis() <= blog.getUnixtime() * 1000L)) {
					// get the table row by an inflater and set the needed
					// information
					final View tableRow = LayoutInflater.from(BlogsActivity.this).inflate(R.layout.blogs_row_layout,
							blogsTable, false);
					tableRow.setId(blog.getId());
					tableRow.setOnClickListener(new View.OnClickListener() {

						public void onClick(View v) {
							// set the correct background when a table row was
							// selected by the user
							if (selectedRow != null) {
								selectedRow.setBackgroundDrawable(getResources().getDrawable(R.drawable.table_cell_bg));
							}
							tableRow.setBackgroundDrawable(getResources()
									.getDrawable(R.drawable.table_cell_bg_selected));
							selectedRow = tableRow;
							getSingleBlog(tableRow.getId());
						}
					});
					// set each table row with the given information from the
					// returned blogs
					viewUtility.setUserIcon(((ImageView) tableRow.findViewById(R.id.blogs_row_user_icon)),
							blog.getUid(), 30);
					((TextView) tableRow.findViewById(R.id.blogs_row_title)).setText(createTitle(blog.getTitle()));
					((TextView) tableRow.findViewById(R.id.blogs_row_date)).setText(createDate(
							blog.getUnixtime() * 1000L, "'um' HH:mm'Uhr'"));
					TextView amount = (TextView) tableRow.findViewById(R.id.blogs_row_cmmts_amount);
					amount.setText(createCommentsAmount(blog.getComments()));

					TextView author = (TextView) tableRow.findViewById(R.id.blogs_row_author);
					author.setText(createAuthor(blog.getAuthor()));
					author.setSelected(true);

					publishProgress(tableRow);
				}
			}
			styleStringBuilder = null;
		}

		/**
		 * @param unixtime
		 * @return
		 */
		private CharSequence createDate(long unixtime, String format) {
			return DateUtility.createDate(unixtime, format);
		}

		/**
		 * Changes the current activity to a {@link SingleBlogActivity} with the selected blog.
		 * 
		 * @param id
		 *            the blog id to find the selected blog
		 */
		private void getSingleBlog(int id) {
			// setting the correct intent awaited by the SingleBlogActivity
			Intent singleBlogIntent = new Intent(BlogsActivity.this, SingleBlogActivity.class);

			singleBlogIntent.putExtra(getString(R.string.id), id);

			View view = ((ActivityGroup) getParent())
					.getLocalActivityManager()
					.startActivity(SingleBlogActivity.class.getSimpleName(),
							singleBlogIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)).getDecorView();
			// replace the view
			((BlogsActivityGroup) getParent()).replaceView(view);
		}

		/**
		 * Formatted string of a blog title.
		 * 
		 * @param title
		 * @return a formatted {@link CharSequence}
		 */
		public CharSequence createTitle(String title) {
			// TODO text formatting
			return title;
		}

		/**
		 * Creates the string for the ui cell showing the author of a blog and the amount of comments.
		 * 
		 * @param commentAmount
		 * @param author
		 * @return a formatted {@link CharSequence}
		 */
		private CharSequence createAuthor(String author) {
			// TODO more text formatting
			// an empty author string means that the blog was not written by a
			if (author.matches("")) {
				author = getString(R.string.news_author_unknown);
			}
			styleStringBuilder.clear();
			styleStringBuilder.appendWithStyle(new ForegroundColorSpan(0xFF007711), getString(R.string.news_author_by));
			styleStringBuilder.append(" ");
			styleStringBuilder.appendWithStyle(new ForegroundColorSpan(0xFF009933), author);

			return styleStringBuilder;
		}

	}

	/**
	 * Formatted string of a blog title.
	 * 
	 * @param title
	 * @return a formatted {@link CharSequence}
	 */
	private CharSequence createTitle(String title) {
		// TODO text formatting
		return title;
	}

	/**
	 * Creates the string for the ui cell showing the author of a blog and the amount of comments.
	 * 
	 * @param commentAmount
	 * @param author
	 * @return a formatted {@link CharSequence}
	 */
	private CharSequence createCommentsAmount(int commentAmount) {
		// TODO more text formatting
		// an empty author string means that the blog was not written by a
		styleStringBuilder.clear();
		styleStringBuilder.appendWithStyle(new ForegroundColorSpan(0xFF7e6003), String.valueOf(commentAmount));

		return styleStringBuilder;
	}

	/**
	 * Creates the string for the ui cell showing the author of a blog and the amount of comments.
	 * 
	 * @param commentAmount
	 * @param author
	 * @return a formatted {@link CharSequence}
	 */
	private CharSequence createAuthor(String author) {
		// TODO more text formatting
		// an empty author string means that the blog was not written by a
		if (author.matches("")) {
			author = getString(R.string.news_author_unknown);
		}
		styleStringBuilder.clear();
		styleStringBuilder.appendWithStyle(new ForegroundColorSpan(0xFF007711), getString(R.string.news_author_by));
		styleStringBuilder.append(" ");
		styleStringBuilder.appendWithStyle(new ForegroundColorSpan(0xFF009933), author);

		return styleStringBuilder;
	}

	protected class BlogsSeparatorAdapter extends BaseAdapter {

		private LayoutInflater inflater;
		private final int SEPERATOR = 0;
		private final int BLOGELEMENT = 1;

		public BlogsSeparatorAdapter(Context context) {
			inflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return blogs.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		@Override
		public int getItemViewType(int position) {
			int type = BLOGELEMENT;
			if (position == 0) {
				type = SEPERATOR;
			} else if (isSeparator(position)) {
				type = SEPERATOR;
			}
			return type;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			int type = getItemViewType(position);

			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				if (type == SEPERATOR) {
					convertView = inflater.inflate(R.layout.blogs_row_day_separator_item_layout, null);
					View separator = convertView.findViewById(R.id.blogs_separator);
					separator.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							// do nothing
						}
					});
					holder.separator = (TextView) separator.findViewById(R.id.blogs_row_day_separator_text);
				} else {
					convertView = inflater.inflate(R.layout.blogs_row_layout, null);
				}
				holder.usericon = (ImageView) convertView.findViewById(R.id.blogs_row_user_icon);
				holder.title = (TextView) convertView.findViewById(R.id.blogs_row_title);
				holder.date = (TextView) convertView.findViewById(R.id.blogs_row_date);
				holder.amount = (TextView) convertView.findViewById(R.id.blogs_row_author);
				holder.author = (TextView) convertView.findViewById(R.id.blogs_row_author);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			if (holder.separator != null) {
				holder.separator.setText(DateUtility.createDate(blogs.get(position).getUnixtime() * 1000L,
						"EEEE, dd. MMMMM yyyy"));
			}
			if (holder.usericon.getHeight() == 0) {
				Log.i("********HEIGHT********", holder.usericon.getHeight() + "");
				viewUtility.setUserIcon(holder.usericon, blogs.get(position).getUid(), 30);
			}
			holder.title.setText(createTitle(blogs.get(position).getTitle()));
			holder.date.setText(DateUtility.createDate(blogs.get(position).getUnixtime() * 1000L, "'um' HH:mm'Uhr'"));
			holder.amount.setText(createCommentsAmount(blogs.get(position).getComments()));
			holder.author.setText(createAuthor(blogs.get(position).getAuthor()));
			return convertView;
		}

		class ViewHolder {
			TextView separator;
			ImageView usericon;
			TextView title;
			TextView date;
			TextView amount;
			TextView author;
		}

		/**
		 * Check if the blog on the given position must be separated from the last blogs.
		 * 
		 * @param position
		 * @return
		 */
		private boolean isSeparator(int position) {
			boolean separator = false;
			// check if the last blog was created on the same date as the current blog
			if (DateUtility.getDay(
					DateUtility.createCalendarFromUnixtime(blogs.get(position - 1).getUnixtime() * 1000L), 0)
					.getTimeInMillis() > blogs.get(position).getUnixtime() * 1000L) {
				// current blog was not created on the same date as the last blog --> separator necessary
				separator = true;
			}
			return separator;
		}
	}

	@Override
	public void onBackPressed() {
		if (getParent() instanceof CwBasicActivityGroup) {
			((CwBasicActivityGroup) getParent()).back();
		}
	}

}
