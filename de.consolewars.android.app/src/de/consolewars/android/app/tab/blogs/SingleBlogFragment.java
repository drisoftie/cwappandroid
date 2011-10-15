package de.consolewars.android.app.tab.blogs;

import java.util.IllegalFormatException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import de.consolewars.android.app.CwApplication;
import de.consolewars.android.app.R;
import de.consolewars.android.app.db.domain.CwBlog;
import de.consolewars.android.app.tab.CwAbstractFragment;
import de.consolewars.android.app.tab.CwNavigationMainTabActivity;
import de.consolewars.android.app.util.DateUtility;
import de.consolewars.android.app.util.TextViewHandler;
import de.consolewars.android.app.view.ActionBar;
import de.consolewars.android.app.view.ActionBar.Action;

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
 * Fragment showing and handling a single blog.
 * 
 * @author Alexander Dridiger
 */
public class SingleBlogFragment extends CwAbstractFragment {

	private Activity context;

	private LayoutInflater inflater;

	private ViewGroup singleblog_layout;
	private ViewGroup content;
	private ViewGroup singleblog_fragment_layout;
	private ViewGroup progress_layout;

	private CwBlog blog;

	private BuildSingleBlogAsyncTask task;

	/**
	 * Mandatory constructor for creating a {@link Fragment}
	 */
	public SingleBlogFragment() {
	}

	public SingleBlogFragment(String title) {
		super(title);
		setHasOptionsMenu(true);
		task = new BuildSingleBlogAsyncTask();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// see this issue http://code.google.com/p/android/issues/detail?id=5067
		context = getActivity();
		this.inflater = LayoutInflater.from(context);
		singleblog_layout = (ViewGroup) inflater.inflate(R.layout.fragment_progress_layout, null);
		singleblog_layout.setBackgroundColor(context.getResources().getColor(R.color.singleblog_bg));
		progress_layout = CwApplication.cwViewUtil().getCenteredProgressBarLayout(inflater, R.string.singleblog);
		ViewGroup progress = (ViewGroup) singleblog_layout.findViewById(R.id.progressbar);
		progress.addView(progress_layout);
		progress_layout.setVisibility(View.GONE);
		return singleblog_layout;
	}

	@Override
	public void onResume() {
		super.onResume();
		blog = CwApplication.cwEntityManager().getSelectedBlog();
		refreshView();
		if (isSelected()) {
			initActionBar();
		}
	}

	private void refreshView() {
		singleblog_layout = (ViewGroup) getView();
		content = (ViewGroup) singleblog_layout.findViewById(R.id.content);
		content.removeAllViews();
		singleblog_fragment_layout = (ViewGroup) inflater.inflate(R.layout.singleblog_fragment_layout, null);
		progress_layout.setVisibility(View.GONE);

		if (task != null && task.getStatus().equals(AsyncTask.Status.PENDING)) {
			task.execute();
		} else if (task == null || task.getStatus().equals(AsyncTask.Status.FINISHED)) {
			task = new BuildSingleBlogAsyncTask();
			task.execute();
		} else if (task.getStatus().equals(AsyncTask.Status.RUNNING)) {
			task.cancel(true);
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if (task != null) {
			task.cancel(true);
			task = null;
		}
	}

	private void initActionBar() {
		if (context != null) {
			if (context.getParent() instanceof CwNavigationMainTabActivity) {
				ActionBar actionBar = getActionBar();
				actionBar.removeAllActions();
				setHomeAction();
				actionBar.setTitle(context.getString(R.string.singleblog_area));
				actionBar.addAction(new Action() {
					@Override
					public void performAction(View view) {
					}

					@Override
					public int getDrawable() {
						return R.drawable.download_bttn;
					}
				});
				actionBar.addAction(new Action() {
					@Override
					public void performAction(View view) {
						refreshView();
					}

					@Override
					public int getDrawable() {
						return R.drawable.refresh_bttn;
					}
				});
			}
		}
	}

	/**
	 * Asynchronous task to receive a single blog and build up the ui.
	 * 
	 * @author Alexander Dridiger
	 */
	private class BuildSingleBlogAsyncTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected void onPreExecute() {
			getActionBar().setProgressBarVisibility(View.VISIBLE);
			progress_layout.setVisibility(View.VISIBLE);
			singleblog_fragment_layout.setVisibility(View.GONE);
		}

		@Override
		protected Void doInBackground(Void... params) {
			if (!isCancelled()) {
				createBlogView();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			progress_layout.setVisibility(View.GONE);
			singleblog_fragment_layout.setVisibility(View.VISIBLE);
			content.removeView(singleblog_fragment_layout);
			content.addView(singleblog_fragment_layout);
			getActionBar().setProgressBarVisibility(View.GONE);
		}

		private void createBlogView() {
			if (!isCancelled()) {
				if (blog.getArticle() == null) {
					CwApplication.cwEntityManager().setSelectedBlog(
							CwApplication.cwEntityManager().getBlogSingle(blog.getSubjectId(), true));
					blog = CwApplication.cwEntityManager().getSelectedBlog();
				}

				TextView text = (TextView) singleblog_fragment_layout.findViewById(R.id.singleblog_text_content);
				CwApplication.cwViewUtil().setClickableTextView(text);
				if (blog != null && blog.getArticle() != null) {
					try {
						text.setText(Html.fromHtml(blog.getArticle(), new TextViewHandler(context), null));
					} catch (IllegalFormatException ife) {
						// FIXME Wrong format handling
						text.setText(blog.getArticle());
					}
					createEditBttn();
					createDeleteBttn(blog);
					createHeader(blog);
				} else if (blog == null || blog.getArticle() == null) {
					text.setText(context.getString(R.string.failure));
				}
			} else {
				cancel(true);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			if (getView() != null) {
				singleblog_layout = (ViewGroup) getView();
				content = (ViewGroup) singleblog_layout.findViewById(R.id.content);
				content.removeAllViews();
			}
			singleblog_fragment_layout = (ViewGroup) inflater.inflate(R.layout.singleblog_fragment_layout, null);
			progress_layout.setVisibility(View.GONE);
			task = new BuildSingleBlogAsyncTask();
			task.execute();
		}

		private void createEditBttn() {
			if (CwApplication.cwLoginManager().isLoggedIn()
					&& blog.getUid() == CwApplication.cwLoginManager().getAuthenticatedUser().getUid()) {
				Button bttn = (Button) singleblog_fragment_layout.findViewById(R.id.singleblog_edit_bttn);
				bttn.setVisibility(View.VISIBLE);
				bttn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {

					}
				});
			}
		}

		private void createDeleteBttn(final CwBlog blog) {
			if (CwApplication.cwLoginManager().isLoggedIn()
					&& blog.getUid() == CwApplication.cwLoginManager().getAuthenticatedUser().getUid()) {
				Button bttn = (Button) singleblog_fragment_layout.findViewById(R.id.singleblog_delete_bttn);
				bttn.setVisibility(View.VISIBLE);
				bttn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						AlertDialog.Builder dialog = new AlertDialog.Builder(SingleBlogFragment.this.getActivity())
								.setMessage(context.getString(R.string.blog_delete_question))
								.setCancelable(false)
								.setPositiveButton(context.getString(R.string.yes),
										new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog, int which) {
												new DeleteBlogAsyncTask().execute(blog.getSubjectId());
											}
										})
								.setNegativeButton(context.getString(R.string.no),
										new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog, int which) {
												dialog.cancel();
											}
										});
						dialog.create().show();
					}
				});
			}
		}

		private void createHeader(CwBlog blog) {
			ImageView icon = (ImageView) singleblog_fragment_layout.findViewById(R.id.singleblog_header_usericon);
			CwApplication.cwImageLoader().displayImage(context.getString(R.string.userpic_url, blog.getUid(), 60),
					context, (ImageView) icon, false, R.drawable.user_stub);

			TextView text = (TextView) singleblog_fragment_layout.findViewById(R.id.singleblog_header_title);
			text.setText(context.getString(R.string.singleblogs_author, blog.getAuthor().toUpperCase()));

			TextView title = (TextView) singleblog_fragment_layout.findViewById(R.id.header_descr_title);
			title.setText(blog.getTitle());
			TextView info = (TextView) singleblog_fragment_layout.findViewById(R.id.header_descr_info);
			info.setText(createDate(blog.getUnixtime() * 1000L));
		}

		/**
		 * @param unixtime
		 * @return
		 */
		private CharSequence createDate(long unixtime) {
			return DateUtility.createDate(unixtime, context.getString(R.string.dateformat_detailed));
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
			getActionBar().setProgressBarVisibility(View.VISIBLE);
			Toast.makeText(SingleBlogFragment.this.getActivity(), context.getString(R.string.blog_deleting),
					Toast.LENGTH_SHORT).show();
		}

		@Override
		protected Void doInBackground(Integer... params) {
			CwApplication.cwManager().deleteBlog(params[0], context.getString(R.string.cw_command_blogdelete));
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			Toast.makeText(SingleBlogFragment.this.getActivity(), context.getString(R.string.blog_deleted),
					Toast.LENGTH_SHORT).show();
			getActionBar().setProgressBarVisibility(View.GONE);
			getFragmentManager().popBackStack();
		}
	}

	private MenuInflater menuInflater;

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if (isSelected()) {
			super.onCreateOptionsMenu(menu, inflater);
			menu.clear();
			inflater.inflate(R.menu.singleblog_menu, menu);
		}
		menuInflater = inflater;
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		if (isSelected()) {
			super.onPrepareOptionsMenu(menu);
			menu.clear();
			menuInflater.inflate(R.menu.singleblog_menu, menu);
		}
	}

	@Override
	public void backPressed() {
		if (task != null && task.getStatus().equals(AsyncTask.Status.RUNNING)) {
			task.cancel(true);
		}
	}

}
