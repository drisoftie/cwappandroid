package de.consolewars.android.app.tab.cmts;

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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import de.consolewars.android.app.R;
import de.consolewars.android.app.tab.CwNavigationMainTabActivity;
import de.consolewars.android.app.tab.blogs.SingleBlogActivity;
import de.consolewars.android.app.tab.news.SingleNewsActivity;
import de.consolewars.android.app.util.TextViewHandler;
import de.consolewars.api.data.Comment;
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
 * @author Alexander Dridiger
 * 
 */
public class CommentsActivity extends Activity {

	private List<Comment> comments;
	private CwNavigationMainTabActivity mainTabs;
	private ViewGroup cmmts_layout;
	private int area;
	private int id;

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
		new BuildCommentsAsyncTask().execute();
	}

	private void resolveID() {
		id = -1;
		// looking for the correct intent
		if (getIntent().hasExtra(SingleBlogActivity.class.getName())) {
			id = getIntent().getIntExtra(SingleBlogActivity.class.getName(), -1);
			area = Comment.AREA_BLOGS;
		} else if (getIntent().hasExtra(SingleNewsActivity.class.getName())) {
			id = getIntent().getIntExtra(SingleNewsActivity.class.getName(), -1);
			area = Comment.AREA_NEWS;
		}
	}

	/**
	 * Create rows displaying single comments to be displayed in a table.
	 */
	private List<View> createCommentsRows() {
		cmmts_layout = (ViewGroup) LayoutInflater.from(CommentsActivity.this.getParent()).inflate(
				R.layout.comments_layout, null);

		List<View> rows = new ArrayList<View>();

		// create table based on current comment
		TableLayout comtsTable = (TableLayout) cmmts_layout.findViewById(R.id.comments_table);

		for (Comment comment : comments) {
			// get the table row by an inflater and set the needed information
			final View tableRow = LayoutInflater.from(CommentsActivity.this).inflate(
					R.layout.comments_row_layout, comtsTable, false);

			((TextView) tableRow.findViewById(R.id.cmts_username)).setText(comment.getUsername());
			((ImageView) tableRow.findViewById(R.id.cmts_usericon))
					.setImageBitmap(getUserPic(comment.getUid()));
			((TextView) tableRow.findViewById(R.id.cmts_date)).setText(createDate(comment
					.getUnixtime() * 1000L));
			TextView content = (TextView) tableRow.findViewById(R.id.comment_content);
			content.setText(Html.fromHtml(comment.getStatement(), new TextViewHandler(
					CommentsActivity.this.getApplicationContext()), null));
			//
			// TextView author = (TextView) tableRow.findViewById(R.id.blogs_row_author);
			// author.setText(createAuthor(blog.getAuthor()));
			// author.setSelected(true);
			rows.add(tableRow);
		}
		return rows;
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
	 * @param unixtime
	 * @return
	 */
	private CharSequence createDate(long unixtime) {
		Date date = new Date(unixtime);
		TimeZone zone = TimeZone.getDefault();

		Calendar cal = Calendar.getInstance(zone, Locale.GERMANY);
		SimpleDateFormat dateformat = new SimpleDateFormat("dd.MM.yyyy 'um' HH:mm 'Uhr'",
				Locale.GERMANY);
		dateformat.setCalendar(cal);
		return dateformat.format(date);
	}

	/**
	 * Asynchronous task to receive comments from the API and build up the ui.
	 * 
	 * @author Alexander Dridiger
	 */
	private class BuildCommentsAsyncTask extends AsyncTask<Void, Integer, List<View>> {

		private ProgressBar progressBar;

		@Override
		protected void onPreExecute() {
			// first set progressbar view
			ViewGroup progress_layout = (ViewGroup) LayoutInflater.from(
					CommentsActivity.this.getParent()).inflate(R.layout.centered_progressbar, null);
			setContentView(progress_layout);

			TextView text = (TextView) progress_layout.findViewById(R.id.centered_progressbar_text);
			text.setText(getString(R.string.loading, getString(R.string.comments)));

			progressBar = (ProgressBar) progress_layout.findViewById(R.id.centered_progressbar);
			progressBar.setProgress(0);
		}

		@Override
		protected List<View> doInBackground(Void... params) {
			resolveID();
			try {
				mainTabs.getApiCaller().authenticateOnCW();
				Log.i("********ComID*******", id + " " + area);
				comments = mainTabs.getApiCaller().getApi().getComments(id, area, 10, 1, -1);
				Log.i("********Comments*******", "Anzahl: " + comments.size());
			} catch (ConsolewarsAPIException e) {
				e.printStackTrace();
				Log.e(getString(R.string.exc_auth_tag), e.getMessage(), e);
				return new ArrayList<View>();
			}
			return createCommentsRows();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			progressBar.setProgress(values[0]);
		}

		@Override
		protected void onPostExecute(List<View> result) {
			// sets the comments view for this Activity
			TableLayout cmtsTable = (TableLayout) cmmts_layout.findViewById(R.id.comments_table);
			for (View row : result) {
				cmtsTable.addView(row);
			}
			setContentView(cmmts_layout);
		}
	}
}
