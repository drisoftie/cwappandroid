package de.consolewars.android.app.tab.blogs;

import java.util.IllegalFormatException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import de.consolewars.android.app.tab.cmts.CommentsFragment;
import de.consolewars.android.app.util.DateUtility;
import de.consolewars.android.app.util.TextViewHandler;
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
 * Activity showing and handling a single news.
 * 
 * @author Alexander Dridiger
 */
public class SingleBlogFragment extends Fragment {

	private Context context;

	private LayoutInflater inflater;

	private ViewGroup singleblog_layout;
	private ViewGroup singleblog_fragment_layout;
	private ViewGroup progress_layout;

	private CwBlog blog;

	public SingleBlogFragment() {
		super();
	}

	public SingleBlogFragment(CwBlog blog) {
		super();
		setHasOptionsMenu(true);
		this.blog = blog;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		context = getActivity().getApplicationContext();
		this.inflater = LayoutInflater.from(context);
		singleblog_layout = (ViewGroup) inflater.inflate(R.layout.singleblog_layout, null);
		singleblog_fragment_layout = (ViewGroup) inflater.inflate(R.layout.singleblog_fragment_layout, null);
		progress_layout = CwApplication.cwViewUtil().getCenteredProgressBarLayout(inflater, R.string.singleblog);
		ViewGroup progress = (ViewGroup) singleblog_layout.findViewById(R.id.singleblog_progressbar);
		progress.addView(progress_layout);
		progress_layout.setVisibility(View.GONE);
		new BuildSingleBlogAsyncTask().execute();
		return singleblog_layout;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	/**
	 * Asynchronous task to receive a single news from the API and build up the ui.
	 * 
	 * @author Alexander Dridiger
	 */
	private class BuildSingleBlogAsyncTask extends AsyncTask<Void, Integer, View> {

		@Override
		protected void onPreExecute() {
			progress_layout.setVisibility(View.VISIBLE);
		}

		@Override
		protected View doInBackground(Void... params) {
			return createBlogView();
		}

		@Override
		protected void onPostExecute(View result) {
			progress_layout.setVisibility(View.GONE);
			ViewGroup content = (ViewGroup) singleblog_layout.findViewById(R.id.singleblog_content);
			content.addView(result);
		}

		private View createBlogView() {
			if (blog.getArticle() == null) {
				try {
					blog = CwApplication.cwManager().getCwBlogById(blog.getSubjectId());
				} catch (ConsolewarsAPIException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			TextView text = (TextView) singleblog_fragment_layout.findViewById(R.id.singleblog_text_content);
			CwApplication.cwViewUtil().setClickableTextView(text);
			if (blog != null && blog.getArticle() != null) {
				try {
					// String fString = String.format(blog.getArticle(), "");
					// CharSequence styledString = Html.fromHtml(fString);
					// text.setText(styledString);
					text.setText(Html.fromHtml(blog.getArticle(), new TextViewHandler(context), null));
				} catch (IllegalFormatException ife) {
					// FIXME Wrong format handling
					text.setText(blog.getArticle());
				}
				createCommentBttn(blog);
				createEditBttn();
				createDeleteBttn(blog);
				createHeader(blog);
			} else if (blog == null || blog.getArticle() == null) {
				text.setText(context.getString(R.string.failure));
			}
			return singleblog_fragment_layout;
		}

		private void createCommentBttn(final CwBlog blog) {
			Button bttn = (Button) singleblog_fragment_layout.findViewById(R.id.singleblog_comments_bttn);
			bttn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// Create new fragment and transaction
					Fragment commentsFragment = new CommentsFragment(blog);
					FragmentTransaction transaction = getFragmentManager().beginTransaction();

					transaction.add(R.id.blogs_root, commentsFragment);
					transaction.addToBackStack(null);

					// Commit the transaction
					transaction.commit();
				}
			});
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
			CwApplication.cwViewUtil().setUserIcon(icon, blog.getUid(), 60);

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
			getFragmentManager().popBackStack();
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		menu.clear();
		inflater.inflate(R.menu.singleblog_menu, menu);
	}
}
