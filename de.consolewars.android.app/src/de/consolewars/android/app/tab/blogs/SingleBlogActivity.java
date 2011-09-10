package de.consolewars.android.app.tab.blogs;

import java.util.IllegalFormatException;

import roboguice.activity.RoboActivity;
import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;

import de.consolewars.android.app.CwLoginManager;
import de.consolewars.android.app.CwManager;
import de.consolewars.android.app.CwManager.CommentArea;
import de.consolewars.android.app.R;
import de.consolewars.android.app.db.domain.CwBlog;
import de.consolewars.android.app.tab.CwBasicActivityGroup;
import de.consolewars.android.app.tab.cmts.CommentsFragment;
import de.consolewars.android.app.util.DateUtility;
import de.consolewars.android.app.util.TextViewHandler;
import de.consolewars.android.app.util.ViewUtility;
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
	private CwLoginManager cwLoginManager;
	@Inject
	private CwManager cwManager;
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

			CwBlog blog = null;

			if (id != -1) {
				try {
					blog = cwManager.getCwBlogById(id);
				} catch (ConsolewarsAPIException e) {
					e.printStackTrace();
				}
			}

			TextView text = (TextView) blogView.findViewById(R.id.singleblog_text_content);
			viewUtility.setClickableTextView(text);
			if (id == -1 || blog == null) {
				text.setText("Fehler");
			} else {
				try {
					// String fString = String.format(blog.getArticle(), "");
					// CharSequence styledString = Html.fromHtml(fString);
					// text.setText(styledString);
					text.setText(Html.fromHtml(blog.getArticle(),
							new TextViewHandler(SingleBlogActivity.this.getApplicationContext()), null));
				} catch (IllegalFormatException ife) {
					// FIXME Wrong format handling
					text.setText(blog.getArticle());
				}
				createCommentBttn(blogView, blog);
				createEditBttn(blogView, blog);
				createDeleteBttn(blogView, blog);
				createHeader(blogView, blog);
			}
			return blogView;
		}

		private void createCommentBttn(View blogView, final CwBlog blog) {
			Button bttn = (Button) blogView.findViewById(R.id.singleblog_comments_bttn);
			bttn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent commentsIntent = new Intent(SingleBlogActivity.this, CommentsFragment.class);

					Bundle extra = new Bundle();
					extra.putInt(getString(R.string.type), CommentArea.BLOGS.getValue());
					extra.putInt(getString(R.string.id), blog.getSubjectId());
					extra.putInt(getString(R.string.comments_amount), blog.getComments());

					commentsIntent.putExtras(extra);

					View view = ((ActivityGroup) getParent())
							.getLocalActivityManager()
							.startActivity(CommentsFragment.class.getSimpleName(),
									commentsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)).getDecorView();
					// replace the view
					((BlogsActivityGroup) getParent()).replaceView(view);
				}
			});

		}

		private void createEditBttn(View blogView, final CwBlog blog) {
			if (cwLoginManager.isLoggedIn() && blog.getUid() == cwLoginManager.getAuthenticatedUser().getUid()) {
				Button bttn = (Button) blogView.findViewById(R.id.singleblog_edit_bttn);
				bttn.setVisibility(View.VISIBLE);
				bttn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent blogswriterIntent = new Intent(SingleBlogActivity.this, BlogsWriterActivity.class);

						Bundle extra = new Bundle();
						extra.putInt(getString(R.string.id), blog.getSubjectId());

						blogswriterIntent.putExtras(extra);

						View view = ((ActivityGroup) getParent())
								.getLocalActivityManager()
								.startActivity(BlogsWriterActivity.class.getSimpleName(),
										blogswriterIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)).getDecorView();
						// replace the view
						((BlogsActivityGroup) getParent()).replaceView(view);
					}
				});
			}
		}

		private void createDeleteBttn(View blogView, final CwBlog blog) {
			if (cwLoginManager.isLoggedIn() && blog.getUid() == cwLoginManager.getAuthenticatedUser().getUid()) {
				Button bttn = (Button) blogView.findViewById(R.id.singleblog_delete_bttn);
				bttn.setVisibility(View.VISIBLE);
				bttn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						AlertDialog.Builder dialog = new AlertDialog.Builder(SingleBlogActivity.this.getParent())
								.setMessage(getString(R.string.blog_delete_question)).setCancelable(false)
								.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {
										new DeleteBlogAsyncTask().execute(blog.getSubjectId());
									}
								}).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {
										dialog.cancel();
									}
								});
						dialog.create().show();
					}
				});
			}
		}

		private void createHeader(View parent, CwBlog blog) {
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

	/**
	 * Asynchronous task to delete a blog.
	 * 
	 * @author Alexander Dridiger
	 */
	private class DeleteBlogAsyncTask extends AsyncTask<Integer, Void, Void> {

		@Override
		protected void onPreExecute() {
			Toast.makeText(SingleBlogActivity.this, getResources().getString(R.string.blog_deleting),
					Toast.LENGTH_SHORT).show();
		}

		@Override
		protected Void doInBackground(Integer... params) {
			cwManager.deleteBlog(params[0], getString(R.string.cw_command_blogdelete));
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			Toast.makeText(SingleBlogActivity.this, getResources().getString(R.string.blog_deleted), Toast.LENGTH_SHORT)
					.show();
			if (SingleBlogActivity.this.getParent() instanceof CwBasicActivityGroup) {
				((CwBasicActivityGroup) SingleBlogActivity.this.getParent()).back();
			}
		}
	}

	@Override
	public void onBackPressed() {
		if (getParent() instanceof CwBasicActivityGroup) {
			((CwBasicActivityGroup) getParent()).back();
		}
	}
}
