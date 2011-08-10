package de.consolewars.android.app.tab.blogs;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import de.consolewars.android.app.R;
import de.consolewars.android.app.tab.CwNavigationMainTabActivity;
import de.consolewars.android.app.util.StyleSpannableStringBuilder;
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
public class BlogsActivity extends Activity {

	private List<Blog> blogs = new ArrayList<Blog>();
	// remember last selected table row to draw the background
	private View selectedRow;
	// text styling
	private StyleSpannableStringBuilder styleStringBuilder;

	private ViewGroup blogs_layout;
	private Calendar oldestBlogsDate;
	private Calendar currentBlogsDate;

	private CwNavigationMainTabActivity mainTabs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		resetDates();

		/*
		 * TODO: Might become a source of error someday, if activity design changes. Would be better
		 * to handle it with intents.
		 */
		if (getParent().getParent() instanceof CwNavigationMainTabActivity) {
			mainTabs = (CwNavigationMainTabActivity) getParent().getParent();
		}
		blogs_layout = (ViewGroup) LayoutInflater.from(BlogsActivity.this.getParent()).inflate(
				R.layout.blogs_layout, null);
		setContentView(blogs_layout);
		new BuildBlogsAsyncTask().execute();
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

		singleBlogIntent.putExtra(BlogsActivity.class.getName(), id);

		View view = ((ActivityGroup) getParent()).getLocalActivityManager().startActivity(
				SingleBlogActivity.class.getSimpleName(),
				singleBlogIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)).getDecorView();
		// replace the view
		((BlogsActivityGroup) getParent()).replaceView(view);
	}

	/**
	 * Downloads the user picture and decodes it into a {@link Bitmap} to be set into an ImageView.
	 * 
	 * @param uid
	 *            the user id
	 * @return the picture
	 */
	private Bitmap getUserPic(int uid) {
		URL newurl;
		Bitmap icon = null;
		try {
			newurl = new URL(getString(R.string.blogs_userpic_url, uid, 30));
			icon = BitmapFactory.decodeStream(newurl.openConnection().getInputStream());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return icon;
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
		styleStringBuilder.appendWithStyle(new ForegroundColorSpan(0xFF007711),
				getString(R.string.news_author_by));
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
	private CharSequence createCommentsamount(int commentAmount) {
		// TODO more text formatting
		// an empty author string means that the blog was not written by a
		styleStringBuilder.clear();
		styleStringBuilder
				.appendWithStyle(new ForegroundColorSpan(0xFF7e6003), String.valueOf(commentAmount));

		return styleStringBuilder;
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

	private Calendar createCalendarFromUnixtime(long unixtime) {
		Date date = new Date(unixtime);
		TimeZone zone = TimeZone.getDefault();

		Calendar cal = Calendar.getInstance(zone, Locale.GERMANY);
		// Log.i("****TIMEZONE*****", zone.getDisplayName());
		cal.setTimeInMillis(date.getTime());

		return cal;
	}

	/**
	 * @param unixtime
	 * @return
	 */
	private CharSequence createDate(long unixtime, String format) {
		SimpleDateFormat dateformat = new SimpleDateFormat(format, Locale.GERMANY);
		Calendar cal = createCalendarFromUnixtime(unixtime);
		dateformat.setTimeZone(cal.getTimeZone());
		dateformat.setCalendar(cal);
		return dateformat.format(cal.getTime());
	}

	private void resetDates() {
		// TimeZone zone = TimeZone.getTimeZone("ECT");
		TimeZone zone = TimeZone.getDefault();

		oldestBlogsDate = Calendar.getInstance(zone, Locale.GERMANY);
		oldestBlogsDate.setTimeInMillis(getDay(oldestBlogsDate, 0).getTimeInMillis());
		currentBlogsDate = Calendar.getInstance(zone, Locale.GERMANY);
		currentBlogsDate.setTimeInMillis(getDay(currentBlogsDate, 1).getTimeInMillis() - 1L);
	}

	private Calendar getDay(Calendar date, int days) {
		Calendar cal = Calendar.getInstance(Locale.GERMANY);
		cal.setTimeInMillis(date.getTimeInMillis());
		cal.add(Calendar.DATE, days);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		return cal;
	}

	/**
	 * Asynchronous task to receive blogs from the API and build up the ui.
	 * 
	 * @author Alexander Dridiger
	 */
	private class BuildBlogsAsyncTask extends AsyncTask<Void, View, Void> {

		private ProgressBar progressBar;

		@Override
		protected void onPreExecute() {
			// first set progressbar
			ViewGroup progress_layout = (ViewGroup) LayoutInflater.from(BlogsActivity.this.getParent())
					.inflate(R.layout.centered_progressbar, null);

			TextView text = (TextView) progress_layout.findViewById(R.id.centered_progressbar_text);
			text.setText(getString(R.string.loading, getString(R.string.tab_blogs_head)));

			progressBar = (ProgressBar) progress_layout.findViewById(R.id.centered_progressbar);
			progressBar.setProgress(0);
			TableLayout blogsTable = (TableLayout) blogs_layout.findViewById(R.id.blogs_table);
			blogsTable.addView(progress_layout);
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				oldestBlogsDate.setTimeInMillis(getDay(oldestBlogsDate, 0).getTimeInMillis());
				mainTabs.getApiCaller().authenticateOnCW();
				blogs = mainTabs.getApiCaller().getApi().getBlogsList(-1, 50, 0, oldestBlogsDate.getTime());
			} catch (ConsolewarsAPIException e) {
				e.printStackTrace();
				Log.e(getString(R.string.exc_auth_tag), e.getMessage(), e);
			}
			createBlogRows();
			return null;
		}

		@Override
		protected void onProgressUpdate(View... rows) {
			TableLayout blogsTable = (TableLayout) blogs_layout.findViewById(R.id.blogs_table);
			blogsTable.addView(rows[0], blogsTable.getChildCount() - 1);
		}

		@Override
		protected void onPostExecute(Void result) {
			// sets the blogs view for this Activity
			TableLayout blogsTable = (TableLayout) blogs_layout.findViewById(R.id.blogs_table);

			blogsTable.removeViewAt(blogsTable.getChildCount() - 1);
			ViewGroup lastrowLayout = (ViewGroup) LayoutInflater.from(BlogsActivity.this.getParent())
					.inflate(R.layout.day_down_row_layout, null);
			Button downBttn = (Button) lastrowLayout.findViewById(R.id.day_down_row_bttn);
			downBttn.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					currentBlogsDate.setTimeInMillis(oldestBlogsDate.getTimeInMillis() - 1L);
					oldestBlogsDate.setTimeInMillis(getDay(oldestBlogsDate, -1).getTimeInMillis());
					TableLayout blogsTable = (TableLayout) blogs_layout.findViewById(R.id.blogs_table);
					blogsTable.removeViewAt(blogsTable.getChildCount() - 1);
					new BuildBlogsAsyncTask().execute();
				}
			});
			blogsTable.addView(lastrowLayout);
		}

		/**
		 * Create rows displaying single blogs to be displayed in a table.
		 */
		private void createBlogRows() {
			// create table based on current blogs
			TableLayout blogsTable = (TableLayout) blogs_layout.findViewById(R.id.blogs_table);

			styleStringBuilder = new StyleSpannableStringBuilder();

			ViewGroup separator = (ViewGroup) LayoutInflater.from(BlogsActivity.this.getParent()).inflate(
					R.layout.blogs_row_day_separator_layout, null);
			TextView separatorTxt = (TextView) separator.findViewById(R.id.blogs_row_day_separator_text);
			separatorTxt.setText(createDate(currentBlogsDate.getTimeInMillis(), "EEEE, dd. MMMMM yyyy"));
			publishProgress(separator);

			Calendar tempCal = Calendar.getInstance(Locale.GERMANY);
			tempCal.setTimeInMillis(currentBlogsDate.getTimeInMillis() + 1L);

			for (Blog blog : blogs) {
				if (getDay(tempCal, -1).getTimeInMillis() > blog.getUnixtime() * 1000L) {
					currentBlogsDate.setTimeInMillis(currentBlogsDate.getTimeInMillis() + 1L);
					currentBlogsDate.setTimeInMillis(getDay(currentBlogsDate, -1).getTimeInMillis() - 1L);
					tempCal.setTimeInMillis(currentBlogsDate.getTimeInMillis() + 1L);
					separator = (ViewGroup) LayoutInflater.from(BlogsActivity.this.getParent()).inflate(
							R.layout.blogs_row_day_separator_layout, null);
					separatorTxt = (TextView) separator.findViewById(R.id.blogs_row_day_separator_text);
					separatorTxt.setText(createDate(currentBlogsDate.getTimeInMillis(),
							"EEEE, dd. MMMMM yyyy"));
					publishProgress(separator);
				} else if ((currentBlogsDate.getTimeInMillis() >= blog.getUnixtime() * 1000L)
						&& (getDay(tempCal, -1).getTimeInMillis() <= blog.getUnixtime() * 1000L)) {
					// get the table row by an inflater and set the needed information
					final View tableRow = LayoutInflater.from(BlogsActivity.this).inflate(
							R.layout.blogs_row_layout, blogsTable, false);
					tableRow.setId(blog.getId());
					tableRow.setOnClickListener(new View.OnClickListener() {

						public void onClick(View v) {
							// set the correct background when a table row was selected by the user
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
					// set each table row with the given information from the returned blogs
					((ImageView) tableRow.findViewById(R.id.blogs_row_user_icon))
							.setImageBitmap(getUserPic(blog.getUid()));
					((TextView) tableRow.findViewById(R.id.blogs_row_title)).setText(createTitle(blog
							.getTitle()));
					((TextView) tableRow.findViewById(R.id.blogs_row_date)).setText(createDate(blog
							.getUnixtime() * 1000L, "'um' HH:mm'Uhr'"));
					TextView amount = (TextView) tableRow.findViewById(R.id.blogs_row_cmmts_amount);
					amount.setText(createCommentsamount(blog.getComments()));

					TextView author = (TextView) tableRow.findViewById(R.id.blogs_row_author);
					author.setText(createAuthor(blog.getAuthor()));
					author.setSelected(true);

					publishProgress(tableRow);
				}
			}
			styleStringBuilder = null;
		}
	}
}
