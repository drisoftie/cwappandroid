package de.consolewars.android.app.tab.blogs;

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

import de.consolewars.android.app.CWLoginManager;
import de.consolewars.android.app.CWManager;
import de.consolewars.android.app.Filter;
import de.consolewars.android.app.R;
import de.consolewars.android.app.tab.CwBasicActivityGroup;
import de.consolewars.android.app.util.DateUtility;
import de.consolewars.android.app.util.StyleSpannableStringBuilder;
import de.consolewars.android.app.util.ViewUtility;
import de.consolewars.android.app.view.IScrollListener;
import de.consolewars.android.app.view.ScrollDetectorScrollView;
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
	private CWLoginManager cwLoginManager;
	@Inject
	private CWManager cwManager;
	@Inject
	private ViewUtility viewUtility;

	private ViewGroup blogs_layout;
	private TableLayout blogsTable;
	private Button refresh;
	private ScrollDetectorScrollView scroll;
	private Spinner spinner;

	// remember last selected table row to draw the background
	private View selectedRow;

	private Filter currentFilter = Filter.BLOGS_NORMAL;
	private int oldestBlogsID = -1;
	private boolean initUI = false;;

	// text styling
	private StyleSpannableStringBuilder styleStringBuilder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		blogs_layout = (ViewGroup) LayoutInflater.from(getParent()).inflate(R.layout.blogs_layout, null);
		blogsTable = (TableLayout) blogs_layout.findViewById(R.id.blogs_table);
		refresh = (Button) blogs_layout.findViewById(R.id.blogs_bttn_refresh);
		scroll = (ScrollDetectorScrollView) blogs_layout.findViewById(R.id.blogs_scroll_view);
		spinner = (Spinner) blogs_layout.findViewById(R.id.blogs_filter_spinner);
		setContentView(blogs_layout);
		new BuildBlogsAsyncTask().execute();
	}

	/**
	 * Asynchronous task to receive blogs from the API and build up the ui.
	 * 
	 * @author Alexander Dridiger
	 */
	private class BuildBlogsAsyncTask extends AsyncTask<Boolean, View, Void> {

		@Override
		protected void onPreExecute() {
			// first set progressbar
			ViewGroup progress_layout = viewUtility.getCenteredProgressBarLayout(getLayoutInflater(),
					R.string.tab_blogs_head);
			blogsTable.addView(progress_layout);
			scroll.removeScrollListener();
			refresh.setClickable(false);
		}

		@Override
		protected Void doInBackground(Boolean... params) {
			if (params.length > 0 && params[0]) {
				if (!cwManager.getBlogs(currentFilter).isEmpty()) {
					if (currentFilter.equals(Filter.BLOGS_USER)) {
						cwManager.getBlogsAndStore(50, currentFilter, null);
					} else {
						try {
							cwManager.getBlogsByIDAndStore(
									cwManager.getBlogs(currentFilter)
											.get(cwManager.getBlogs(Filter.BLOGS_NORMAL).size() - 1).getId(), true);
						} catch (ConsolewarsAPIException e) {
							e.printStackTrace();
						}
					}
				}
			}
			createBlogRows();
			return null;
		}

		@Override
		protected void onProgressUpdate(View... rows) {
			if (!isCancelled()) {
				blogsTable.addView(rows[0], blogsTable.getChildCount() - 1);
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			blogsTable.removeViewAt(blogsTable.getChildCount() - 1);
			if (!initUI) {
				initFilter();
				initRefreshBttn();
			}
			refresh.setClickable(true);
			initScroll();
			initSendBlogBttn();
			initUI = true;
		}

		private void initSendBlogBttn() {
			Button new_bttn = (Button) blogs_layout.findViewById(R.id.blogs_bttn_new_blog);
			new_bttn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Intent newBlogIntent = new Intent(BlogsActivity.this, BlogsWriterActivity.class);

					View view = ((ActivityGroup) getParent())
							.getLocalActivityManager()
							.startActivity(BlogsWriterActivity.class.getSimpleName(),
									newBlogIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)).getDecorView();
					// replace the view
					((BlogsActivityGroup) getParent()).replaceView(view);
				}
			});
		}

		/**
		 * Create rows displaying single blogs to be displayed in a table.
		 */
		private void createBlogRows() {
			// create table based on current blogs
			ViewGroup separator = null;
			TextView separatorTxt = null;

			for (int i = 0; i < cwManager.getBlogs(currentFilter).size(); i++) {
				Blog blog = cwManager.getBlogs(currentFilter).get(i);
				if (!isCancelled() && (oldestBlogsID == -1 || blog.getId() < oldestBlogsID)) {
					if (i == 0
							|| DateUtility.getDay(
									DateUtility.createCalendarFromUnixtime(cwManager.getBlogs(currentFilter).get(i - 1)
											.getUnixtime() * 1000L), 0).getTimeInMillis() > blog.getUnixtime() * 1000L) {
						// current blog was not created on the same date as the last blog --> separator necessary
						separator = (ViewGroup) LayoutInflater.from(BlogsActivity.this.getParent()).inflate(
								R.layout.blogs_row_day_separator_layout, null);
						separatorTxt = (TextView) separator.findViewById(R.id.blogs_row_day_separator_text);
						separatorTxt.setText(createDate(
								DateUtility.getDay(DateUtility.createCalendarFromUnixtime(blog.getUnixtime() * 1000L),
										0).getTimeInMillis(), "EEEE, dd. MMMMM yyyy"));
						publishProgress(separator);
					}
					if (matchesFilter(blog.getUid(), blog.getMode())) {
						// get the table row by an inflater and set the needed information
						final View tableRow = LayoutInflater.from(BlogsActivity.this).inflate(
								R.layout.blogs_row_layout, blogsTable, false);
						tableRow.setId(blog.getId());
						tableRow.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								// set the correct background when a table row was
								// selected by the user
								if (selectedRow != null) {
									selectedRow.setBackgroundDrawable(getResources().getDrawable(
											R.drawable.table_cell_bg));
								}
								tableRow.setBackgroundDrawable(getResources().getDrawable(
										R.drawable.table_cell_bg_selected));
								selectedRow = tableRow;
								getSingleBlog(tableRow.getId());
							}
						});
						// set each table row with the given information from the
						// returned blogs
						viewUtility.setUserIcon(((ImageView) tableRow.findViewById(R.id.blogs_row_user_icon)),
								blog.getUid(), 40);
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
			}
		}

		private boolean matchesFilter(int uid, String mode) {
			boolean matches = false;
			if (currentFilter.equals(Filter.BLOGS_NEWS) && !mode.equals(getString(R.string.blogmode_normal))) {
				matches = true;
			} else if (currentFilter.equals(Filter.BLOGS_NORMAL) && mode.equals(getString(R.string.blogmode_normal))) {
				matches = true;
			} else if (currentFilter.equals(Filter.BLOGS_USER) && uid == cwLoginManager.getUser().getUid()) {
				matches = true;
			}
			return matches;
		}

		private void initRefreshBttn() {
			refresh.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					cancel(true);
					blogsTable.removeAllViews();
					oldestBlogsID = -1;
					new BuildBlogsAsyncTask().execute();
				}
			});
		}

		/**
		 * Filter ui and logic for filtering blogs.
		 * 
		 */
		private void initFilter() {
			ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getParent(),
					R.array.blogs_filter_options, android.R.layout.simple_spinner_item);
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
						cancel(true);
						currentFilter = selected;
						blogsTable.removeAllViews();
						oldestBlogsID = -1;
						new BuildBlogsAsyncTask().execute();
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
						if (currentFilter.equals(Filter.BLOGS_USER)) {
							blogsTable.removeAllViews();
						}
						new BuildBlogsAsyncTask().execute(true);
					}
				}
			});
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
		private CharSequence createAuthor(String author) {
			// TODO more text formatting
			// an empty author string means that the blog was not written by a
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
		 * Creates the string for the ui cell showing the author of a blog and the amount of comments.
		 * 
		 * @param commentAmount
		 * @param author
		 * @return a formatted {@link CharSequence}
		 */
		private CharSequence createCommentsAmount(int commentAmount) {
			// TODO more text formatting
			// an empty author string means that the blog was not written by a
			styleStringBuilder = new StyleSpannableStringBuilder();
			styleStringBuilder.appendWithStyle(new ForegroundColorSpan(0xFF7e6003), String.valueOf(commentAmount));

			return styleStringBuilder;
		}
	}

	@Override
	public void onBackPressed() {
		if (getParent() instanceof CwBasicActivityGroup) {
			((CwBasicActivityGroup) getParent()).back();
		}
	}
}
