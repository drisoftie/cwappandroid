package de.consolewars.android.app.tab.blogs;

import java.util.IllegalFormatException;

import roboguice.activity.RoboActivity;
import android.app.ActivityGroup;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.inject.Inject;

import de.consolewars.android.app.CWManager;
import de.consolewars.android.app.CWManager.CommentArea;
import de.consolewars.android.app.R;
import de.consolewars.android.app.tab.cmts.CommentsActivity;
import de.consolewars.android.app.util.DateUtility;
import de.consolewars.android.app.util.TextViewHandler;
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
 * Activity showing and handling a single blog.
 * 
 * @author Alexander Dridiger
 */
public class SingleBlogActivity extends RoboActivity {

	@Inject
	private CWManager cwManager;
	@Inject
	private ViewUtility viewUtility;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new BuildSingleBlogAsyncTask().execute();
	}

	/**
	 * Asynchronous task to receive a single news from the API and build up the ui.
	 * 
	 * @author Alexander Dridiger
	 */
	private class BuildSingleBlogAsyncTask extends AsyncTask<Void, Integer, View> {

		private ProgressBar progressBar;

		@Override
		protected void onPreExecute() {
			// first set progressbar view
			setContentView(R.layout.centered_progressbar);

			TextView text = (TextView) findViewById(R.id.centered_progressbar_text);
			text.setText(getString(R.string.loading, "Einzelblog"));

			progressBar = (ProgressBar) findViewById(R.id.centered_progressbar);
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

		private View createBlogView() {
			View blogView = LayoutInflater.from(getParent()).inflate(R.layout.singleblog_layout, null);

			int id = -1;

			// looking for the correct intent
			if (getIntent().hasExtra(getString(R.string.id))) {
				id = getIntent().getIntExtra(getString(R.string.id), -1);
			}

			Blog blog = null;

			if (id != -1) {
				try {
					blog = cwManager.getBlogById(id);
				} catch (ConsolewarsAPIException e) {
					e.printStackTrace();
				}
			}

			TextView text = (TextView) blogView.findViewById(R.id.singleblog_text_content);
			text.setMovementMethod(LinkMovementMethod.getInstance());
			if (id == -1 || blog == null) {
				text.setText("Fehler");
			} else {
				try {
					// String fString = String.format(blog.getArticle(), "");
					// CharSequence styledString = Html.fromHtml(fString);
					// text.setText(styledString);
					text.setText(Html.fromHtml(blog.getArticle(true),
							new TextViewHandler(SingleBlogActivity.this.getApplicationContext()), null));
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

				public void onClick(View v) {
					Intent commentsIntent = new Intent(SingleBlogActivity.this, CommentsActivity.class);

					Bundle extra = new Bundle();
					extra.putInt(getString(R.string.type), CommentArea.BLOGS.getValue());
					extra.putInt(getString(R.string.id), blog.getId());
					extra.putInt(getString(R.string.comments_amount), blog.getComments());

					commentsIntent.putExtras(extra);

					View view = ((ActivityGroup) getParent())
							.getLocalActivityManager()
							.startActivity(CommentsActivity.class.getSimpleName(),
									commentsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)).getDecorView();
					// replace the view
					((BlogsActivityGroup) getParent()).replaceView(view);
				}
			});
		}

		private void createHeader(View parent, Blog blog) {
			ImageView icon = (ImageView) parent.findViewById(R.id.singleblog_header_usericon);
			viewUtility.setUserIcon(icon, blog.getUid(), 60);

			TextView text = (TextView) parent.findViewById(R.id.singleblog_header_title);
			text.setText(getString(R.string.singleblogs_author, blog.getAuthor().toUpperCase()));

			TextView title = (TextView) parent.findViewById(R.id.header_descr_title);
			title.setText(blog.getTitle());
			TextView info = (TextView) parent.findViewById(R.id.header_descr_info);
			info.setText(createDate(blog.getUnixtime() * 1000L));
		}

		/**
		 * @param unixtime
		 * @return
		 */
		private CharSequence createDate(long unixtime) {
			return DateUtility.createDate(unixtime, getString(R.string.dateformat_detailed));
		}
	}
}
