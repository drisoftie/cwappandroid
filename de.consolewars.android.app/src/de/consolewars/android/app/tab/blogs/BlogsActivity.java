package de.consolewars.android.app.tab.blogs;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
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
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
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

	private List<Blog> blogs;

	// remember last selected table row to draw the background
	private View selectedRow;

	// text styling
	private StyleSpannableStringBuilder styleStringBuilder;

	private CwNavigationMainTabActivity mainTabs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.blogs_layout);

		/*
		 * TODO: Might become a source of error someday, if activity design changes. Would be better
		 * to handle it with intents.
		 */
		if (getParent().getParent() instanceof CwNavigationMainTabActivity) {
			mainTabs = (CwNavigationMainTabActivity) getParent().getParent();
		}

		loadBlogsTable();
	}

	/**
	 * Create the ui for displaying blogs in a table.
	 * 
	 */
	private void loadBlogsTable() {
		// create table based on current blogs
		TableLayout blogsTable = (TableLayout) findViewById(R.id.blogs_table);
		try {
			mainTabs.getApiCaller().authenticateOnCW();
			blogs = mainTabs.getApiCaller().getApi().getBlogsList(-1, 15, 0);
		} catch (ConsolewarsAPIException e) {
			Log.e(getString(R.string.exc_auth_tag), e.getMessage(), e);
			e.printStackTrace();
		}

		styleStringBuilder = new StyleSpannableStringBuilder();

		for (Blog blog : this.blogs) {
			// get the table row by an inflater and set the needed information
			final View tableRow = LayoutInflater.from(this).inflate(R.layout.blogs_row_layout,
					blogsTable, false);

			tableRow.setId(blog.getId());
			tableRow.setOnClickListener(new View.OnClickListener() {
				@Override
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
					.setImageBitmap(getUserPic(blog.getAuthor(), blog.getUid()));
			((TextView) tableRow.findViewById(R.id.blogs_row_title)).setText(createTitle(blog
					.getTitle()));
			((TextView) tableRow.findViewById(R.id.blogs_row_date)).setText(createDate(blog
					.getUnixtime() * 1000L));
			TextView author = (TextView) tableRow.findViewById(R.id.blogs_row_author_cmmnts);
			author.setSelected(true);
			author.setText(createCommentAndAuthor(blog.getComments(), blog.getAuthor()));

			blogsTable.addView(tableRow);
		}
		styleStringBuilder = null;
	}

	/**
	 * Changes the current activity to a {@link SingleBlogActivity} with the selected blog.
	 * 
	 * @param id
	 *            the blog id to find the selected blog
	 */
	private void getSingleBlog(int id) {
		// setting the correct intent awaited by the SingleBlogActivity
		Intent singleNewsIntent = new Intent(BlogsActivity.this, SingleBlogActivity.class);

		singleNewsIntent.putExtra(BlogsActivity.class.getName(), id);

		View view = ((ActivityGroup) getParent())
				.getLocalActivityManager()
				.startActivity(SingleBlogActivity.class.getSimpleName(),
						singleNewsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)).getDecorView();
		// replace the view
		((BlogsActivityGroup) getParent()).replaceView(view);
	}

	/**
	 * Downloads the user picture and decodes it into a {@link Bitmap} to be set into an ImageView.
	 * 
	 * @param author
	 *            the user the picture belongs to
	 * @param uid
	 *            the user id
	 * @return the picture
	 */
	private Bitmap getUserPic(String author, int uid) {
		URL newurl;
		Bitmap icon = null;
		try {
			newurl = new URL(getString(R.string.blogs_userpic_url, author, uid, 30));
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
	 * Creates a readable formatted string from unixtime.
	 * 
	 * @param unixtime
	 * @return a formatted {@link CharSequence}
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
	 * Formatted string of a blog title.
	 * 
	 * @param title
	 * @return a formatted {@link CharSequence}
	 */
	private CharSequence createTitle(String title) {
		// TODO text formatting
		return title;
	}
}
