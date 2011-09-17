package de.consolewars.android.app.tab.cmts;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
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
import de.consolewars.android.app.tab.CwAbstractFragment;
import de.consolewars.android.app.util.DateUtility;
import de.consolewars.android.app.util.TextViewHandler;

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
	private Context context;

	private List<CwComment> comments = new ArrayList<CwComment>();
	private ViewGroup cmmts_layout;
	private TableLayout cmmtsTable;
	private View rowToDelete;
	private int area;
	private int currpage = 1;
	private int maxpage = 1;

	private CwSubject subject;

	private BuildCommentsAsyncTask task;

	public CommentsFragment(CwSubject subject, String title) {
		super(title);
		setHasOptionsMenu(true);
		this.subject = subject;
		task = new BuildCommentsAsyncTask();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.inflater = inflater;
		context = getActivity();
		cmmts_layout = (ViewGroup) inflater.inflate(R.layout.comments_layout, null);
		cmmtsTable = (TableLayout) cmmts_layout.findViewById(R.id.comments_table);
		resolveBundle();
		return cmmts_layout;
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

		if (task.getStatus().equals(AsyncTask.Status.PENDING)) {
			task.execute();
		} else if (task.getStatus().equals(AsyncTask.Status.FINISHED)) {
			task = new BuildCommentsAsyncTask();
			task.execute();
		} else if (task.getStatus().equals(AsyncTask.Status.RUNNING)) {
			task.cancel(true);
		}
	}

	private void resolveBundle() {
		if (subject instanceof CwNews) {
			area = CommentArea.NEWS.getValue();
		} else if (subject instanceof CwBlog) {
			area = CommentArea.BLOGS.getValue();
		}
	}

	@Override
	public void backPressed() {
		if (task.getStatus().equals(AsyncTask.Status.RUNNING)) {
			task.cancel(true);
		}
	}

	/**
	 * @param unixtime
	 * @return
	 */
	private CharSequence createDate(long unixtime) {
		return DateUtility.createDate(unixtime, "dd.MM.yyyy, HH:mm 'Uhr'");
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
				comments = CwApplication.cwEntityManager().getComments(subject.getSubjectId(), area, 20, currpage);
				createCommentsRows();
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(View... rows) {
			if (!isCancelled()) {
				if (rowToDelete != null) {
					Button delete_bttn = (Button) rowToDelete.findViewById(R.id.cmts_bttn_delete);
					delete_bttn.setVisibility(View.VISIBLE);
				}
				cmmtsTable.addView(rows[0], cmmtsTable.getChildCount() - 1);
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			// sets the comments view for this Activity
			cmmtsTable.removeViewAt(cmmtsTable.getChildCount() - 1);
			checkButtons();
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			task = new BuildCommentsAsyncTask();
			task.execute();
		}

		/**
		 * Create rows displaying single comments to be displayed in a table.
		 */
		private void createCommentsRows() {
			// create table based on current comment
			for (final CwComment comment : comments) {
				if (!isCancelled()) {
					maxpage = comment.getPagecount();
					// get the table row by an inflater and set the needed
					// information
					final ViewGroup tableRow = (ViewGroup) inflater.inflate(R.layout.comments_row_layout, cmmtsTable,
							false);

					TextView usernameTxt = (TextView) tableRow.findViewById(R.id.cmts_username);
					usernameTxt.setText(comment.getUsername());
					usernameTxt.setSelected(true);
					CwApplication.cwViewUtil().setUserIcon(((ImageView) tableRow.findViewById(R.id.cmts_usericon)),
							comment.getUid(), 50);
					((TextView) tableRow.findViewById(R.id.cmts_date))
							.setText(createDate(comment.getUnixtime() * 1000L));
					((TextView) tableRow.findViewById(R.id.cmts_date)).setSelected(true);
					TextView content = (TextView) tableRow.findViewById(R.id.comment_content);
					CwApplication.cwViewUtil().setClickableTextView(content);
					content.setText(Html.fromHtml(comment.getStatement(), new TextViewHandler(context), null));

					if (CwApplication.cwLoginManager().getAuthenticatedUser().getUid() == comment.getUid()) {
						View delete_edit_bttn_layout = inflater.inflate(R.layout.delete_edit_bttn_layout, cmmtsTable,
								false);
						ViewGroup parent_layout = (ViewGroup) tableRow.findViewById(R.id.cmts_bttn_delete_edit_layout);
						parent_layout.addView(delete_edit_bttn_layout);

						Button delete_bttn = (Button) delete_edit_bttn_layout.findViewById(R.id.cmts_bttn_delete);

						// first ask the user, if he wants to delete a comment
						delete_bttn.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								AlertDialog.Builder dialog = new AlertDialog.Builder(context)
										.setMessage(context.getString(R.string.comment_delete_question))
										.setCancelable(false)
										.setPositiveButton(context.getString(R.string.yes),
												new DialogInterface.OnClickListener() {
													public void onClick(DialogInterface dialog, int which) {
														rowToDelete = tableRow;
														new DeleteCommentAsyncTask().execute(comment.getCid());
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

						Button edit_bttn = (Button) delete_edit_bttn_layout.findViewById(R.id.cmts_bttn_edit);
						edit_bttn.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								View edit_layout = inflater.inflate(R.layout.edit_layout, tableRow, false);
								EditText textToEdit = (EditText) edit_layout.findViewById(R.id.cmts_edtxt_edit);
								textToEdit.setText(comment.getStatement());
								ViewGroup parent_layout = (ViewGroup) tableRow
										.findViewById(R.id.comment_content_layout);
								parent_layout.addView(edit_layout);
								createEditSubmitBttn(edit_layout, tableRow, comment.getCid());
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

		private void initButtonsAndCheck() {
			Button refresh_bttn = (Button) cmmts_layout.findViewById(R.id.comments_bttn_refresh);
			refresh_bttn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					cancel(true);
					new BuildCommentsAsyncTask().execute();
				}
			});

			Button next_bttn = (Button) cmmts_layout.findViewById(R.id.comments_bttn_next);
			Button prev_bttn = (Button) cmmts_layout.findViewById(R.id.comments_bttn_prev);
			next_bttn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					cancel(true);
					currpage++;
					new BuildCommentsAsyncTask().execute();
				}
			});
			prev_bttn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					cancel(true);
					currpage--;
					new BuildCommentsAsyncTask().execute();
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

	/**
	 * Asynchronous task to submit a comment.
	 * 
	 * @author Alexander Dridiger
	 */
	private class SubmitCommentAsyncTask extends AsyncTask<Void, Void, Void> {

		private boolean doWork = false;

		@Override
		protected void onPreExecute() {
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
		}
	}

	private MenuInflater menuInflater;

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if (subject instanceof CwNews && isSelected()) {
			super.onCreateOptionsMenu(menu, inflater);
			menu.clear();
			inflater.inflate(R.menu.comments_menu, menu);
		} else if (subject instanceof CwBlog && isSelected()) {
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
		if (subject instanceof CwNews && isSelected()) {
			super.onPrepareOptionsMenu(menu);
			menu.clear();
			menuInflater.inflate(R.menu.comments_menu, menu);
		} else if (subject instanceof CwBlog && isSelected()) {
			super.onPrepareOptionsMenu(menu);
			menu.clear();
			menuInflater.inflate(R.menu.comments_blog_menu, menu);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (isSelected()) {
			super.onOptionsItemSelected(item);
			// Find which menu item has been selected
			switch (item.getItemId()) {
			// Check for each known menu item
			case (R.id.menu_cmts_write):
				cmmts_layout.findViewById(R.id.comments_write_layout).setVisibility(View.VISIBLE);
				((ScrollView) cmmts_layout.findViewById(R.id.comments_scroll_view)).fullScroll(ScrollView.FOCUS_UP);
				break;
			case (R.id.menu_cmts_refresh):
				if (task.getStatus().equals(AsyncTask.Status.RUNNING)) {
					task.cancel(true);
				}
				task = new BuildCommentsAsyncTask();
				task.execute();
				break;
			}
		}
		return true;
	}
}
