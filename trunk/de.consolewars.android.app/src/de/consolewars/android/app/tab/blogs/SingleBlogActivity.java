package de.consolewars.android.app.tab.blogs;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.IllegalFormatException;
import java.util.Locale;
import java.util.TimeZone;

import android.app.Activity;
import android.app.ActivityGroup;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import de.consolewars.android.app.R;
import de.consolewars.android.app.tab.CwNavigationMainTabActivity;
import de.consolewars.android.app.tab.cmts.CommentsActivity;
import de.consolewars.android.app.util.TextViewHandler;
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
 * Activity showing and handling a single blog.
 * 
 * @author Alexander Dridiger
 */
public class SingleBlogActivity extends Activity {

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
		}
		new BuildSingleNewsAsyncTask().execute();
	}

	private View createBlogView() {
		View blogView = LayoutInflater.from(SingleBlogActivity.this.getParent()).inflate(
				R.layout.singleblog_layout, null);

		int id = -1;

		// looking for the correct intent
		if (getIntent().hasExtra(BlogsActivity.class.getName())) {
			id = getIntent().getIntExtra(BlogsActivity.class.getName(), -1);
		}

		Blog blog = null;

		try {
			blog = mainTabs.getApiCaller().getApi().getBlog(id);
		} catch (ConsolewarsAPIException e) {
			e.printStackTrace();
		}

		TextView text = (TextView) blogView.findViewById(R.id.singleblog_text_content);
		if (id == -1) {
			text.setText("Fehler");
		} else if (blog != null) {
			try {
				// String fString = String.format(blog.getArticle(), "");
				// CharSequence styledString = Html.fromHtml(fString);
				// text.setText(styledString);
				text.setText(Html.fromHtml(blog.getArticle(true), new TextViewHandler(SingleBlogActivity.this
						.getApplicationContext()), null));
			} catch (IllegalFormatException ife) {
				// FIXME Wrong format handling
				text.setText(blog.getArticle());
			}
			createCommentBttn(blogView, blog);
			createHeader(blogView, blog);
		}
		return blogView;
	}

	private void createCommentBttn(View blogView, final Blog blog) {
		Button bttn = (Button) blogView.findViewById(R.id.singleblog_comments_bttn);
		bttn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent commentsIntent = new Intent(SingleBlogActivity.this, CommentsActivity.class);

				Bundle extra = new Bundle();
				extra.putInt(getString(R.string.type), R.string.blog);
				extra.putInt(getString(R.string.id), blog.getId());
				extra.putInt(getString(R.string.comments_amount), blog.getComments());

				commentsIntent.putExtras(extra);

				View view = ((ActivityGroup) getParent()).getLocalActivityManager().startActivity(
						CommentsActivity.class.getSimpleName(),
						commentsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)).getDecorView();
				// replace the view
				((BlogsActivityGroup) getParent()).replaceView(view);
			}
		});
	}

	private void createHeader(View parent, Blog blog) {
		ImageView icon = (ImageView) parent.findViewById(R.id.singleblog_header_usericon);
		loadPicture(icon, blog.getUid());

		TextView text = (TextView) parent.findViewById(R.id.singleblog_header_title);
		text.setText(getString(R.string.singleblogs_author, blog.getAuthor().toUpperCase()));

		TextView title = (TextView) parent.findViewById(R.id.header_descr_title);
		title.setText(blog.getTitle());
		TextView info = (TextView) parent.findViewById(R.id.header_descr_info);
		info.setText(createDate(blog.getUnixtime() * 1000L));
	}

	/**
	 * Downloads the user picture and decodes it into a {@link Bitmap} to be set into an ImageView.
	 * 
	 * @param view
	 *            the ImageView
	 * @param uid
	 *            user id is needed to get the appropriate picture
	 */
	private void loadPicture(ImageView view, int uid) {
		URL newurl;
		Bitmap mIcon_val = null;
		try {
			newurl = new URL(getString(R.string.blogs_userpic_url, uid, 60));
			mIcon_val = BitmapFactory.decodeStream(newurl.openConnection().getInputStream());
			view.setImageBitmap(mIcon_val);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param unixtime
	 * @return
	 */
	private CharSequence createDate(long unixtime) {
		Date date = new Date(unixtime);
		TimeZone zone = TimeZone.getDefault();

		Calendar cal = Calendar.getInstance(zone, Locale.GERMANY);
		// formatting day of week in EEEE format like Sunday, Monday etc.
		SimpleDateFormat dateformat = new SimpleDateFormat("EEEE, dd. MMMMM yyyy 'um' HH:mm 'Uhr'",
				Locale.GERMANY);
		dateformat.setCalendar(cal);
		return dateformat.format(date);
	}

	/**
	 * Asynchronous task to receive a single news from the API and build up the ui.
	 * 
	 * @author Alexander Dridiger
	 */
	private class BuildSingleNewsAsyncTask extends AsyncTask<Void, Integer, View> {

		private ProgressBar progressBar;

		@Override
		protected void onPreExecute() {
			// first set progressbar view
			ViewGroup progress_layout = (ViewGroup) LayoutInflater.from(SingleBlogActivity.this.getParent())
					.inflate(R.layout.centered_progressbar, null);
			setContentView(progress_layout);

			TextView text = (TextView) progress_layout.findViewById(R.id.centered_progressbar_text);
			text.setText(getString(R.string.loading, "Einzelblog"));

			progressBar = (ProgressBar) progress_layout.findViewById(R.id.centered_progressbar);
			progressBar.setProgress(0);
		}

		@Override
		protected View doInBackground(Void... params) {
			return createBlogView();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			progressBar.setProgress(values[0]);
		}

		@Override
		protected void onPostExecute(View result) {
			setContentView(result);
		}
	}
}
