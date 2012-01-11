package de.consolewars.android.app.tab.cmts;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import de.consolewars.android.app.CwApplication;
import de.consolewars.android.app.CwManager.CommentArea;
import de.consolewars.android.app.R;
import de.consolewars.android.app.db.domain.CwBlog;
import de.consolewars.android.app.db.domain.CwComment;
import de.consolewars.android.app.db.domain.CwNews;
import de.consolewars.android.app.db.domain.CwSubject;
import de.consolewars.android.app.parser.CommentsRoot;
import de.consolewars.android.app.tab.CwAbstractFragment;
import de.consolewars.android.app.tab.CwAbstractFragmentActivity;
import de.consolewars.android.app.tab.CwNavigationMainTabActivity;
import de.consolewars.android.app.util.DateUtility;
import de.consolewars.android.app.util.TextViewHandler;
import de.consolewars.android.app.view.ActionBar;
import de.consolewars.android.app.view.ActionBar.Action;
import de.consolewars.android.app.view.ActionItem;
import de.consolewars.android.app.view.QuickAction;

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
public class CommentsFragment extends CwAbstractFragment {

	private LayoutInflater inflater;
	private Activity context;

	private List<CwComment> comments = new ArrayList<CwComment>();
	private CommentsRoot root;
	private ViewGroup cmmts_layout;
	private TableLayout cmmtsTable;
	private View rowToDelete;
	private int area;
	private int currpage = 1;
	private int maxpage = 1;

	private CwSubject subject;

	private BuildCommentsTask task;

	/**
	 * Mandatory constructor for creating a {@link Fragment}
	 */
	public CommentsFragment() {
	}

	public CommentsFragment(CwSubject subject, String title, int position) {
		super(title, position);
		setHasOptionsMenu(true);
		this.subject = subject;
		task = new BuildCommentsTask();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.inflater = inflater;
		context = getActivity();
		cmmts_layout = (ViewGroup) inflater.inflate(R.layout.comments_layout, null);
		cmmtsTable = (TableLayout) cmmts_layout.findViewById(R.id.comments_table);
		checkBundle();
		return cmmts_layout;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (((CwAbstractFragmentActivity) getActivity()).lastPosition == getPosition()) {
			initActionBar();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (subject instanceof CwNews) {
			subject = CwApplication.cwEntityManager().getSelectedNews();
		} else if (subject instanceof CwBlog) {
			subject = CwApplication.cwEntityManager().getSelectedBlog();
		}

		cmmts_layout = (ViewGroup) getView();

		if (task != null && task.getStatus().equals(AsyncTask.Status.PENDING)
				&& task.viewtask.getStatus().equals(AsyncTask.Status.PENDING)) {
			task.execute();
		} else if (task != null && task.getStatus().equals(AsyncTask.Status.FINISHED)
				&& task.viewtask.getStatus().equals(AsyncTask.Status.FINISHED)) {
			task = new BuildCommentsTask();
			task.execute();
		} else if (task != null && task.getStatus().equals(AsyncTask.Status.RUNNING)
				&& task.viewtask.getStatus().equals(AsyncTask.Status.RUNNING)) {
			task.cancel(true);
		}
		if (((CwAbstractFragmentActivity) getActivity()).lastPosition == getPosition()) {
			initActionBar();
		}
	}

	private void checkBundle() {
		if (subject instanceof CwNews) {
			area = CommentArea.NEWS.getValue();
		} else if (subject instanceof CwBlog) {
			area = CommentArea.BLOGS.getValue();
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

	@Override
	public void backPressed() {
		if (task != null && task.getStatus().equals(AsyncTask.Status.RUNNING)) {
			task.cancel(true);
		}
		currpage = 1;
		maxpage = 1;
	}

	/**
	 * @param unixtime
	 * @return
	 */
	private CharSequence createDate(long unixtime) {
		return DateUtility.createDate(unixtime, "dd.MM.yyyy, HH:mm 'Uhr'");
	}

	private void initActionBar() {
		if (context != null) {
			if (context.getParent() instanceof CwNavigationMainTabActivity) {
				ActionBar actionBar = getActionBar();
				actionBar.removeAllActions();
				setHomeAction();
				if (subject instanceof CwNews) {
					actionBar.setTitle(context.getString(R.string.singlenews_area));
				} else if (subject instanceof CwBlog) {
					actionBar.setTitle(context.getString(R.string.singleblog_area));
				}
				actionBar.setDisplayHomeAsUpEnabled(true);
				actionBar.addAction(new Action() {
					@Override
					public void performAction(View view) {
						if (task != null
								&& (task.getStatus().equals(AsyncTask.Status.RUNNING) || task.viewtask.getStatus()
										.equals(AsyncTask.Status.RUNNING))) {
							task.cancel(true);
						}
						task = new BuildCommentsTask();
						task.execute();
					}

					@Override
					public int getDrawable() {
						return R.drawable.refresh_blue_bttn;
					}
				});
			}
		}
	}

	private class BuildCommentsTask extends AsyncTask<Void, CwComment, Void> {

		List<ViewGroup> inflatedRows;
		BuildCommentsAsyncTask viewtask = new BuildCommentsAsyncTask();

		@Override
		protected void onPreExecute() {
			getActionBar().setProgressBarVisibility(View.VISIBLE);
		}

		@Override
		protected Void doInBackground(Void... params) {
			if (!isCancelled()) {
				int results = CwApplication.cwEntityManager().getOptions().getMaxCmts();

				inflatedRows = new ArrayList<ViewGroup>();
				if (subject instanceof CwNews && !((CwNews) subject).getComments().isEmpty()) {
					CwNews news = (CwNews) subject;
					if (news.getComments().size() % results == 0) {
						maxpage = news.getComments().size() / results;
					} else {
						maxpage = (news.getComments().size() / results) + 1;
					}
					if (currpage == 1) {
						comments.clear();
						comments.addAll(news.getComments());
						comments = comments.subList(0, news.getComments().size() > results ? results : news
								.getComments().size());
					} else {
						if (news.getComments().size() - ((currpage - 1) * results) >= 0) {
							int start = (currpage - 1) * results;
							int end;
							if (news.getComments().size() - ((currpage - 1) * results) > results) {
								end = currpage * results;
							} else {
								end = news.getComments().size();
							}
							comments.clear();
							comments.addAll(news.getComments());
							comments = comments.subList(start, end);
						}
					}
					for (CwComment comment : comments) {
						if ((comment.getId() != null && comment.getUsername() == null)) {
							comment = CwApplication.cwEntityManager().getRefreshedComment(comment);
						}
					}
				} else if (subject != null) {
					root = CwApplication.cwEntityManager().getComments(subject.getSubjectId(), area, results, currpage);
					maxpage = root.getMaxPage() != 1 ? root.getMaxPage() : 1;
					comments.clear();
					comments.addAll(root.getComments());
				}
			}
			if (!isCancelled()) {
				for (CwComment comment : comments) {
					publishProgress(comment);
				}
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(CwComment... values) {
			ViewGroup tableRow = (ViewGroup) inflater.inflate(R.layout.comments_row_layout, cmmtsTable, false);
			inflatedRows.add(tableRow);
		}

		@Override
		protected void onPostExecute(Void result) {
			getActionBar().setProgressBarVisibility(View.GONE);
			viewtask = new BuildCommentsAsyncTask();
			viewtask.execute();
		}

		/**
		 * Asynchronous task to receive comments from the API and build up the ui.
		 * 
		 * @author Alexander Dridiger
		 */
		private class BuildCommentsAsyncTask extends AsyncTask<Void, View, Void> {

			@Override
			protected void onPreExecute() {
				// first set progressbar
				getActionBar().setProgressBarVisibility(View.VISIBLE);
				ViewGroup progress_layout = CwApplication.cwViewUtil().getCenteredProgressBarLayout(inflater,
						R.string.comments);
				cmmtsTable.removeAllViews();
				cmmtsTable.addView(progress_layout);
				initButtonsAndCheck();
				checkButtons();
			}

			@Override
			protected Void doInBackground(Void... params) {
				if (!isCancelled()) {
					createCommentsRows();
				}
				return null;
			}

			@Override
			protected void onProgressUpdate(View... rows) {
				if (!isCancelled()) {
					cmmtsTable.addView(rows[0], cmmtsTable.getChildCount() - 1);
				}
			}

			@Override
			protected void onPostExecute(Void result) {
				// sets the comments view for this Activity
				cmmtsTable.removeViewAt(cmmtsTable.getChildCount() - 1);
				checkButtons();
				getActionBar().setProgressBarVisibility(View.GONE);
			}

			@Override
			protected void onCancelled() {
				getActionBar().setProgressBarVisibility(View.GONE);
				super.onCancelled();
				task = new BuildCommentsTask();
				task.execute();
			}

			/**
			 * Create rows displaying single comments to be displayed in a table.
			 */
			private void createCommentsRows() {
				Button close_bttn = (Button) cmmts_layout.findViewById(R.id.comments_bttn_close);
				close_bttn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						cmmts_layout.findViewById(R.id.comments_write_layout).setVisibility(View.GONE);
					}
				});
				// create table based on current comment
				for (int i = 0; i < comments.size(); i++) {
					if (!isCancelled()) {
						final CwComment comment = comments.get(i);
						final ViewGroup tableRow = inflatedRows.get(i);
						TextView usernameTxt = (TextView) tableRow.findViewById(R.id.cmts_username);
						usernameTxt.setText(comment.getUsername());
						usernameTxt.setSelected(true);

						CwApplication.cwImageLoader().displayImage(
								context.getString(R.string.userpic_url, comment.getUid(), 50), context,
								(ImageView) tableRow.findViewById(R.id.cmts_usericon), false, R.drawable.user_stub);

						((TextView) tableRow.findViewById(R.id.cmts_date))
								.setText(createDate(comment.getUnixtime() * 1000L));
						((TextView) tableRow.findViewById(R.id.cmts_date)).setSelected(true);
						TextView content = (TextView) tableRow.findViewById(R.id.comment_content);
						CwApplication.cwViewUtil().setClickableTextView(content);
						content.setText(Html.fromHtml(comment.getStatement(), new TextViewHandler(context), null));

						if (CwApplication.cwLoginManager().getAuthenticatedUser().getUid() == comment.getUid()) {
							final QuickAction quickActionHeader = initUserQuickAction();
							// setup the action item click listener
							quickActionHeader.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
								@Override
								public void onItemClick(int pos) {
									if (pos == 0) {
										cmmts_layout.findViewById(R.id.comments_write_layout).setVisibility(
												View.VISIBLE);
										((ScrollView) cmmts_layout.findViewById(R.id.comments_scroll_view))
												.fullScroll(ScrollView.FOCUS_UP);
										((EditText) cmmts_layout.findViewById(R.id.comments_edttxt_input))
												.setText(comment.getQuote());
									} else if (pos == 1) {
										View edit_layout = inflater.inflate(R.layout.edit_layout, tableRow, false);
										EditText textToEdit = (EditText) edit_layout.findViewById(R.id.cmts_edtxt_edit);
										textToEdit.setText(comment.getStatement());
										ViewGroup parent_layout = (ViewGroup) tableRow
												.findViewById(R.id.comment_content_layout);
										parent_layout.addView(edit_layout);
										createEditSubmitBttn(edit_layout, tableRow, comment.getCid());
									} else if (pos == 2) {
										rowToDelete = tableRow;
										new DeleteCommentAsyncTask().execute(comment.getCid());
									} else if (pos == 3) {
										ClipboardManager clipboardManager = (ClipboardManager) getActivity()
												.getSystemService(Activity.CLIPBOARD_SERVICE);
										clipboardManager.setPrimaryClip(ClipData.newPlainText("Comment",
												comment.getStatement()));
										Toast.makeText(getActivity(), context.getString(R.string.copying_text),
												Toast.LENGTH_LONG).show();
									} else if (pos == 4) {
										Toast.makeText(getActivity(), "Noch nicht möglich", Toast.LENGTH_SHORT).show();
									}
								}
							});
							View cell = tableRow.findViewById(R.id.cmts_cell_layout);
							cell.setBackgroundResource(R.drawable.def_cmnt_body_user_selector);
							cell.setOnLongClickListener(new OnLongClickListener() {
								@Override
								public boolean onLongClick(View v) {
									quickActionHeader.show(v);
									return false;
								}
							});
						} else {
							final QuickAction quickActionHeader = initQuickAction();
							// setup the action item click listener
							quickActionHeader.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
								@Override
								public void onItemClick(int pos) {
									if (pos == 0) {
										cmmts_layout.findViewById(R.id.comments_write_layout).setVisibility(
												View.VISIBLE);
										((ScrollView) cmmts_layout.findViewById(R.id.comments_scroll_view))
												.fullScroll(ScrollView.FOCUS_UP);
										((EditText) cmmts_layout.findViewById(R.id.comments_edttxt_input))
												.setText(comment.getQuote());
									} else if (pos == 1) {
										Toast.makeText(getActivity(), "Noch nicht möglich", Toast.LENGTH_SHORT).show();
									} else if (pos == 2) {
										ClipboardManager clipboardManager = (ClipboardManager) getActivity()
												.getSystemService(Activity.CLIPBOARD_SERVICE);
										clipboardManager.setPrimaryClip(ClipData.newPlainText("Comment",
												comment.getStatement()));
										Toast.makeText(getActivity(), context.getString(R.string.copying_text),
												Toast.LENGTH_LONG).show();
									}
								}
							});
							View cell = tableRow.findViewById(R.id.cmts_cell_layout);
							cell.setOnLongClickListener(new OnLongClickListener() {
								@Override
								public boolean onLongClick(View v) {
									quickActionHeader.show(v);
									return false;
								}
							});
						}
						publishProgress(tableRow);
					}
				}
			}

			private void createEditSubmitBttn(View parent, final View row, final int commentId) {
				Button edit_submit_bttn = (Button) parent.findViewById(R.id.cmts_bttn_edit_submit);
				edit_submit_bttn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						new EditCommentAsyncTask().execute(commentId, row);
					}
				});
			}

			private QuickAction initQuickAction() {
				final QuickAction mQuickAction = new QuickAction(getActivity());
				mQuickAction.addActionItem(createAction(R.string.quote, R.drawable.ic_add));
				mQuickAction.addActionItem(createAction(R.string.comment_fav, R.drawable.ic_accept));
				mQuickAction.addActionItem(createAction(R.string.copy, R.drawable.ic_accept));
				return mQuickAction;
			}

			private QuickAction initUserQuickAction() {
				final QuickAction mQuickAction = new QuickAction(getActivity());
				mQuickAction.addActionItem(createAction(R.string.quote, R.drawable.ic_add));
				mQuickAction.addActionItem(createAction(R.string.edit, R.drawable.def_bttn_edit));
				mQuickAction.addActionItem(createAction(R.string.delete, R.drawable.def_bttn_del));
				mQuickAction.addActionItem(createAction(R.string.copy, R.drawable.ic_accept));
				mQuickAction.addActionItem(createAction(R.string.comment_fav, R.drawable.ic_accept));
				return mQuickAction;
			}

			private ActionItem createAction(int titleId, int iconId) {
				ActionItem addAction = new ActionItem();
				addAction.setTitle(getString(titleId));
				addAction.setIcon(getResources().getDrawable(iconId));
				return addAction;
			}

			private void initButtonsAndCheck() {
				Button next_bttn = (Button) cmmts_layout.findViewById(R.id.comments_bttn_next);
				Button prev_bttn = (Button) cmmts_layout.findViewById(R.id.comments_bttn_prev);
				next_bttn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						currpage++;
						if (task != null
								&& (task.getStatus().equals(AsyncTask.Status.RUNNING) || task.viewtask.getStatus()
										.equals(AsyncTask.Status.RUNNING))) {
							task.cancel(true);
						}
						task = new BuildCommentsTask();
						task.execute();
					}
				});
				prev_bttn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						currpage--;
						cancel(true);
						if (task != null
								&& (task.getStatus().equals(AsyncTask.Status.RUNNING) || task.viewtask.getStatus()
										.equals(AsyncTask.Status.RUNNING))) {
							task.cancel(true);
						}
						task = new BuildCommentsTask();
						task.execute();
					}
				});

				Button submit_bttn = (Button) cmmts_layout.findViewById(R.id.comments_bttn_submit);
				submit_bttn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						new SubmitCommentAsyncTask().execute();
					}
				});
			}

			private void checkButtons() {
				Button next_bttn = (Button) cmmts_layout.findViewById(R.id.comments_bttn_next);
				Button prev_bttn = (Button) cmmts_layout.findViewById(R.id.comments_bttn_prev);
				if (currpage <= 1) {
					prev_bttn.setEnabled(false);
				} else {
					prev_bttn.setEnabled(true);
				}
				if (currpage == maxpage) {
					next_bttn.setEnabled(false);
				} else {
					next_bttn.setEnabled(true);
				}
			}
		}
	}

	/**
	 * Asynchronous task to submit a comment.
	 * 
	 * @author Alexander Dridiger
	 */
	private class SubmitCommentAsyncTask extends AsyncTask<Void, Void, Void> {

		private boolean doWork = false;

		@Override
		protected void onPreExecute() {
			getActionBar().setProgressBarVisibility(View.VISIBLE);
			EditText commenttxt = (EditText) cmmts_layout.findViewById(R.id.comments_edttxt_input);
			if (StringUtils.isBlank(commenttxt.getText().toString())) {
				commenttxt.requestFocus();
				commenttxt.setError(context.getString(R.string.no_text_entered));
			} else if (StringUtils.isBlank(CwApplication.cwLoginManager().getAuthenticatedUser().getUsername())) {
				commenttxt.requestFocus();
				commenttxt.setError(context.getString(R.string.not_logged_in));
			} else {
				commenttxt.setError(null);
				Toast.makeText(context, context.getString(R.string.comment_sending), Toast.LENGTH_SHORT).show();
				doWork = true;
			}
		}

		@Override
		protected Void doInBackground(Void... params) {
			if (doWork) {
				EditText commenttxt = (EditText) cmmts_layout.findViewById(R.id.comments_edttxt_input);
				CwApplication.cwManager().sendComment(commenttxt.getText().toString(), subject.getSubjectId(), area);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (doWork) {
				Toast.makeText(context, context.getString(R.string.comment_sent), Toast.LENGTH_SHORT).show();
				cmmts_layout.findViewById(R.id.comments_write_layout).setVisibility(View.GONE);
			}
			getActionBar().setProgressBarVisibility(View.GONE);
		}
	}

	/**
	 * Asynchronous task to delete a comment.
	 * 
	 * @author Alexander Dridiger
	 */
	private class DeleteCommentAsyncTask extends AsyncTask<Integer, Void, Void> {

		@Override
		protected void onPreExecute() {
			getActionBar().setProgressBarVisibility(View.VISIBLE);
			Toast.makeText(context, context.getString(R.string.comment_deleting), Toast.LENGTH_SHORT).show();
		}

		@Override
		protected Void doInBackground(Integer... ids) {
			CwApplication.cwManager().deleteComment(ids[0], subject.getSubjectId(), area);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (rowToDelete.getParent() instanceof TableLayout) {
				cmmtsTable.removeView(rowToDelete);
			}
			Toast.makeText(context, context.getString(R.string.comment_deleted), Toast.LENGTH_SHORT).show();
			getActionBar().setProgressBarVisibility(View.GONE);
		}
	}

	/**
	 * Asynchronous task to edit a comment.
	 * 
	 * @author Alexander Dridiger
	 */
	private class EditCommentAsyncTask extends AsyncTask<Object, Void, View> {

		@Override
		protected void onPreExecute() {
			getActionBar().setProgressBarVisibility(View.VISIBLE);
			Toast.makeText(context, context.getString(R.string.comment_editing), Toast.LENGTH_SHORT).show();
		}

		@Override
		protected View doInBackground(Object... params) {
			View row = (View) params[1];
			EditText textToEdit = (EditText) row.findViewById(R.id.cmts_edtxt_edit);
			CwApplication.cwManager().updateComment(textToEdit.getText().toString(), (Integer) params[0],
					subject.getSubjectId(), area);
			return row;
		}

		@Override
		protected void onPostExecute(View row) {
			ViewGroup parent_layout = (ViewGroup) row.findViewById(R.id.comment_content_layout);
			EditText textToEdit = (EditText) row.findViewById(R.id.cmts_edtxt_edit);
			TextView content = (TextView) row.findViewById(R.id.comment_content);
			content.setText(Html.fromHtml(textToEdit.getText().toString(), new TextViewHandler(context), null));
			parent_layout.removeViewAt(1);
			Toast.makeText(context, context.getString(R.string.comment_edited), Toast.LENGTH_SHORT).show();
			getActionBar().setProgressBarVisibility(View.GONE);
		}
	}

	private MenuInflater menuInflater;

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if (subject instanceof CwNews && ((CwAbstractFragmentActivity) getActivity()).lastPosition == getPosition()) {
			super.onCreateOptionsMenu(menu, inflater);
			menu.clear();
			inflater.inflate(R.menu.comments_news_menu, menu);
		} else if (subject instanceof CwBlog
				&& ((CwAbstractFragmentActivity) getActivity()).lastPosition == getPosition()) {
			super.onCreateOptionsMenu(menu, inflater);
			menu.clear();
			inflater.inflate(R.menu.comments_blog_menu, menu);
		}
		menuInflater = inflater;
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		if (menuInflater == null) {
			menuInflater = getActivity().getMenuInflater();
		}
		if (subject instanceof CwNews && ((CwAbstractFragmentActivity) getActivity()).lastPosition == getPosition()) {
			super.onPrepareOptionsMenu(menu);
			menu.clear();
			menuInflater.inflate(R.menu.comments_news_menu, menu);
		} else if (subject instanceof CwBlog
				&& ((CwAbstractFragmentActivity) getActivity()).lastPosition == getPosition()) {
			super.onPrepareOptionsMenu(menu);
			menu.clear();
			menuInflater.inflate(R.menu.comments_blog_menu, menu);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (((CwAbstractFragmentActivity) getActivity()).lastPosition == getPosition()) {
			super.onOptionsItemSelected(item);
			// Find which menu item has been selected
			switch (item.getItemId()) {
			// Check for each known menu item
			case (R.id.menu_cmts_write):
				cmmts_layout.findViewById(R.id.comments_write_layout).setVisibility(View.VISIBLE);
				((ScrollView) cmmts_layout.findViewById(R.id.comments_scroll_view)).fullScroll(ScrollView.FOCUS_UP);
				break;
			case (R.id.menu_cmts_refresh):
				if (task != null
						&& (task.getStatus().equals(AsyncTask.Status.RUNNING) || task.viewtask.getStatus().equals(
								AsyncTask.Status.RUNNING))) {
					task.cancel(true);
				}
				task = new BuildCommentsTask();
				task.execute();
				break;
			}
		}
		return true;
	}

	@Override
	public void refresh() {
		initActionBar();
	}
}
