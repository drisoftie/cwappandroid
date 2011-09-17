package de.consolewars.android.app.tab.blogs;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import de.consolewars.android.app.CwApplication;
import de.consolewars.android.app.CwEntityManager.EntityRefinement;
import de.consolewars.android.app.Filter;
import de.consolewars.android.app.R;
import de.consolewars.android.app.db.domain.CwBlog;
import de.consolewars.android.app.tab.CwAbstractFragment;
import de.consolewars.android.app.tab.OnSubjectSelectedListener;
import de.consolewars.android.app.util.DateUtility;
import de.consolewars.android.app.util.StyleSpannableStringBuilder;
import de.consolewars.android.app.view.IScrollListener;
import de.consolewars.android.app.view.ScrollDetectorScrollView;

/*
 * Copyright [2011] [Alexander Dridiger]
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
 * Fragment to handle the ui for blogs.
 * 
 * @author Alexander Dridiger
 */
public final class BlogsFragment extends CwAbstractFragment {

	private Context context;

	private OnSubjectSelectedListener listener;

	private LayoutInflater inflater;
	private ViewGroup blogs_layout;
	private TableLayout blogsTable;
	private Button refresh;
	private ScrollDetectorScrollView scroll;

	// remember last selected table row to draw the background for that row
	private View selectedRow;

	private BuildBlogsAsyncTask task;

	private List<CwBlog> tempList = new ArrayList<CwBlog>();

	private Filter currentFilter = Filter.BLOGS_NORMAL;
	private int oldestBlogsID = -1;

	// text styling
	private StyleSpannableStringBuilder styleStringBuilder;

	public BlogsFragment() {
		
	}
	
	public BlogsFragment(Filter filter, String titel) {
		super(titel);
		setHasOptionsMenu(true);
		this.currentFilter = filter;
		task = new BuildBlogsAsyncTask();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		context = activity;
		try {
			listener = (OnSubjectSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
		// mContent = savedInstanceState.getString(KEY_CONTENT);
		// }
		this.inflater = inflater;
		blogs_layout = (ViewGroup) inflater.inflate(R.layout.blogs_fragment_layout, null);
		blogsTable = (TableLayout) blogs_layout.findViewById(R.id.blogs_table);
		refresh = (Button) blogs_layout.findViewById(R.id.blogs_bttn_refresh);
		scroll = (ScrollDetectorScrollView) blogs_layout.findViewById(R.id.blogs_scroll_view);
		return blogs_layout;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (blogsTable.getChildCount() == 0) {
			if (task.getStatus().equals(AsyncTask.Status.PENDING)) {
				task.execute(true);
			} else if (task.getStatus().equals(AsyncTask.Status.FINISHED)) {
				task = new BuildBlogsAsyncTask();
				task.execute();
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// outState.putString(KEY_CONTENT, mContent);
	}

	/**
	 * Asynchronous task to receive blogs from the API and build up the ui.
	 * 
	 * @author Alexander Dridiger
	 */
	private class BuildBlogsAsyncTask extends AsyncTask<Boolean, View, Void> {

		boolean doesWork = false;

		@Override
		protected void onPreExecute() {
			doesWork = true;
			// first set progressbar
			ViewGroup progress_layout = CwApplication.cwViewUtil().getCenteredProgressBarLayout(inflater,
					R.string.tab_blogs_head);
			blogsTable.addView(progress_layout);
			scroll.removeScrollListener();
			refresh.setClickable(false);
		}

		@Override
		protected Void doInBackground(Boolean... params) {
			if (params.length > 0 && params[0]) {
				if (currentFilter.equals(Filter.BLOGS_USER)) {
					CwApplication.cwEntityManager().getUserBlogsAndCache(
							CwApplication.cwLoginManager().getAuthenticatedUser().getUid(), 50, null);
				} else {
					CwApplication.cwEntityManager().getNextBlogs(EntityRefinement.MIXED, currentFilter);
				}
			}
			refreshList();
			createBlogRows();
			return null;
		}

		@Override
		protected void onProgressUpdate(View... rows) {
			if (!isCancelled()) {
				blogsTable.addView(rows[0], blogsTable.getChildCount() - 1);
				oldestBlogsID = rows[0].getId();
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			blogsTable.removeViewAt(blogsTable.getChildCount() - 1);
			initRefreshBttn();
			refresh.setClickable(true);
			initScroll();
			if (blogsTable.getChildCount() == 0) {
				oldestBlogsID = -1;
			}
			doesWork = false;
			blogs_layout.invalidate();
		}

		/**
		 * Create rows displaying single blogs to be displayed in a table.
		 */
		private void createBlogRows() {
			// create table based on current blogs
			ViewGroup separator = null;
			TextView separatorTxt = null;

			for (int i = 0; i < tempList.size(); i++) {
				final CwBlog blog = tempList.get(i);
				if (!isCancelled() && (oldestBlogsID == -1 || blog.getSubjectId() < oldestBlogsID)) {

					if (matchesFilter(blog.getUid(), blog.getMode())) {
						// get the table row by an inflater and set the needed information
						final View tableRow = inflater.inflate(R.layout.blogs_row_layout, blogsTable, false);
						tableRow.setId(blog.getSubjectId());
						tableRow.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								// set the correct background when a table row was selected by the user
								if (selectedRow != null) {
									selectedRow.setBackgroundDrawable(context.getResources().getDrawable(
											R.drawable.table_cell_bg));
								}
								tableRow.setBackgroundDrawable(context.getResources().getDrawable(
										R.drawable.table_cell_bg_selected));
								selectedRow = tableRow;
								listener.onSubjectSelected(blog);
							}
						});
						// set each table row with the given information from the
						// returned blogs
						CwApplication.cwViewUtil().setUserIcon(
								((ImageView) tableRow.findViewById(R.id.blogs_row_user_icon)), blog.getUid(), 40);
						((TextView) tableRow.findViewById(R.id.blogs_row_title)).setText(createTitle(blog.getTitle()));
						((TextView) tableRow.findViewById(R.id.blogs_row_date)).setText(createDate(
								blog.getUnixtime() * 1000L, "'um' HH:mm'Uhr'"));
						TextView amount = (TextView) tableRow.findViewById(R.id.blogs_row_cmmts_amount);
						amount.setText(createCommentsAmount(blog.getCommentsAmount()));

						TextView author = (TextView) tableRow.findViewById(R.id.blogs_row_author);
						author.setText(createAuthor(blog.getAuthor()));
						author.setSelected(true);
						RatingBar rating = (RatingBar) tableRow.findViewById(R.id.blogs_row_rating);
						rating.setRating(blog.getRating());
						if (i == 0
								|| DateUtility
										.getDay(DateUtility.createCalendarFromUnixtime(tempList.get(i - 1)
												.getUnixtime() * 1000L), 0).getTimeInMillis() > blog.getUnixtime() * 1000L) {
							// current blog was not created on the same date as the last blog --> separator necessary
							separator = (ViewGroup) inflater.inflate(R.layout.blogs_row_day_separator_layout, null);
							separatorTxt = (TextView) separator.findViewById(R.id.blogs_row_day_separator_text);
							separatorTxt.setText(createDate(
									DateUtility.getDay(
											DateUtility.createCalendarFromUnixtime(blog.getUnixtime() * 1000L), 0)
											.getTimeInMillis(), "EEEE, dd. MMMMM yyyy"));
							publishProgress(separator);
						}
						publishProgress(tableRow);
					}
				}
			}
		}

		private void refreshList() {
			tempList.clear();
			for (CwBlog blog : CwApplication.cwEntityManager().getCachedBlogs(currentFilter)) {
				if (matchesFilter(blog.getUid(), blog.getMode())) {
					tempList.add(blog);
				}
			}
		}

		private boolean matchesFilter(int uid, String mode) {
			boolean matches = false;
			if (currentFilter.equals(Filter.BLOGS_NEWS) && !mode.equals(context.getString(R.string.blogmode_normal))) {
				matches = true;
			} else if (currentFilter.equals(Filter.BLOGS_NORMAL)
					&& mode.equals(context.getString(R.string.blogmode_normal))) {
				matches = true;
			} else if (currentFilter.equals(Filter.BLOGS_USER)
					&& uid == CwApplication.cwLoginManager().getAuthenticatedUser().getUid()) {
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
					task = new BuildBlogsAsyncTask();
					task.execute();
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
							task = new BuildBlogsAsyncTask();
							task.execute(true);
						}
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
				author = context.getString(R.string.news_author_unknown);
			}
			styleStringBuilder = new StyleSpannableStringBuilder();
			styleStringBuilder.appendWithStyle(new ForegroundColorSpan(0xFF007711),
					context.getString(R.string.news_author_by));
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

	/**
	 * Asynchronous task to save blogs.
	 * 
	 * @author Alexander Dridiger
	 */
	private class SaveAllBlogsTask extends AsyncTask<Boolean, Void, Integer> {

		@Override
		protected void onPreExecute() {
			Toast.makeText(context, context.getString(R.string.blogs_saving), Toast.LENGTH_SHORT).show();
		}

		@Override
		protected Integer doInBackground(Boolean... params) {
			return CwApplication.cwEntityManager().saveAllBlogs();
		}

		@Override
		protected void onPostExecute(Integer result) {
			Toast.makeText(context, context.getString(R.string.blogs_saved, result), Toast.LENGTH_SHORT).show();
		}
	}

	private MenuInflater menuInflater;

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		if (menuInflater == null) {
			menuInflater = getActivity().getMenuInflater();
		}
		if (isSelected()) {
			super.onPrepareOptionsMenu(menu);
			menu.clear();
			if (currentFilter.equals(Filter.BLOGS_USER) && CwApplication.cwLoginManager().isLoggedIn()) {
				menuInflater.inflate(R.menu.ownblogs_menu, menu);
			} else {
				menuInflater.inflate(R.menu.blogs_menu, menu);
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (isSelected()) {
			super.onOptionsItemSelected(item);
			// Find which menu item has been selected
			switch (item.getItemId()) {
			// Check for each known menu item
			case (R.id.menu_blogs_discard):
				CwApplication.cwEntityManager().discardAllBlogs();
				break;
			case (R.id.menu_blogs_load_next):
				if (task.getStatus().equals(AsyncTask.Status.RUNNING)) {
					task.cancel(true);
				}
				task = new BuildBlogsAsyncTask();
				task.execute(true);
				break;
			case (R.id.menu_blogs_load_saved):

				break;
			case (R.id.menu_blogs_refresh):

				break;
			case (R.id.menu_blogs_save_all):
				new SaveAllBlogsTask().execute();
				break;
			}
		}
		return true;
	}

	@Override
	public void backPressed() {
	}
}
