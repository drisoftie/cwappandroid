package de.consolewars.android.app.tab.cmts;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;

import roboguice.activity.RoboActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;

import de.consolewars.android.app.CWApplication;
import de.consolewars.android.app.R;
import de.consolewars.android.app.tab.CwBasicActivityGroup;
import de.consolewars.android.app.util.HttpPoster;
import de.consolewars.android.app.util.TextViewHandler;
import de.consolewars.api.API;
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
public class CommentsActivity extends RoboActivity {

	@Inject
	private CWApplication cwApplication;
	@Inject
	private HttpPoster httpPoster;
	@Inject
	private API api;
	private List<Comment> comments = new ArrayList<Comment>();
	private ViewGroup cmmts_layout;
	private View rowToDelete;
	private int area;
	private int id;
	private int currpage = 1;
	private int maxpage = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		resolveBundle();
		new BuildCommentsAsyncTask().execute();
	}

	private void initButtonsAndCheck() {
		Button refresh_bttn = (Button) cmmts_layout.findViewById(R.id.comments_bttn_refresh);

		refresh_bttn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				new BuildCommentsAsyncTask().execute();
			}
		});

		Button next_bttn = (Button) cmmts_layout.findViewById(R.id.comments_bttn_next);
		Button prev_bttn = (Button) cmmts_layout.findViewById(R.id.comments_bttn_prev);
		next_bttn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				currpage++;
				new BuildCommentsAsyncTask().execute();
			}
		});
		prev_bttn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				currpage--;
				new BuildCommentsAsyncTask().execute();
			}
		});

		Button submit_bttn = (Button) cmmts_layout.findViewById(R.id.comments_bttn_submit);
		submit_bttn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				new SubmitCommentAsyncTask().execute();
			}
		});
		checkButtons();
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

	private void resolveBundle() {
		Bundle extra = CommentsActivity.this.getIntent().getExtras();

		id = extra.getInt(getString(R.string.id), -1);

		// looking for the correct intent
		if (extra.getInt(getString(R.string.type)) == R.string.news) {
			area = Comment.AREA_NEWS;
		} else if (extra.getInt(getString(R.string.type)) == R.string.blog) {
			area = Comment.AREA_BLOGS;
		}
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
			newurl = new URL(getString(R.string.blogs_userpic_url, uid, 50));
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
		SimpleDateFormat dateformat = new SimpleDateFormat("dd.MM.yyyy, HH:mm 'Uhr'", Locale.GERMANY);
		dateformat.setCalendar(cal);
		return dateformat.format(date);
	}

	/**
	 * Asynchronous task to receive comments from the API and build up the ui.
	 * 
	 * @author Alexander Dridiger
	 */
	private class BuildCommentsAsyncTask extends AsyncTask<Void, View, Void> {

		private ProgressBar progressBar;

		@Override
		protected void onPreExecute() {
			cmmts_layout = (ViewGroup) LayoutInflater.from(CommentsActivity.this.getParent()).inflate(
					R.layout.comments_layout, null);
			setContentView(cmmts_layout);
			// first set progressbar
			ViewGroup progress_layout = (ViewGroup) LayoutInflater.from(CommentsActivity.this.getParent()).inflate(
					R.layout.centered_progressbar, null);
			TextView text = (TextView) progress_layout.findViewById(R.id.centered_progressbar_text);
			text.setText(getString(R.string.loading, getString(R.string.comments)));

			progressBar = (ProgressBar) progress_layout.findViewById(R.id.centered_progressbar);
			progressBar.setProgress(0);
			TableLayout comtsTable = (TableLayout) cmmts_layout.findViewById(R.id.comments_table);
			comtsTable.removeAllViews();
			comtsTable.addView(progress_layout);
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				comments = api.getComments(id, area, 10, currpage, -1);
			} catch (ConsolewarsAPIException e) {
				e.printStackTrace();
				Log.e(getString(R.string.exc_auth_tag), e.getMessage(), e);
			}
			createCommentsRows();
			return null;
		}

		@Override
		protected void onProgressUpdate(View... rows) {
			if (rowToDelete != null) {
				Button delete_bttn = (Button) rowToDelete.findViewById(R.id.cmts_bttn_delete);
				delete_bttn.setVisibility(View.VISIBLE);
			}
			TableLayout cmtsTable = (TableLayout) cmmts_layout.findViewById(R.id.comments_table);
			cmtsTable.addView(rows[0], cmtsTable.getChildCount() - 1);
		}

		@Override
		protected void onPostExecute(Void result) {
			initButtonsAndCheck();
			// sets the comments view for this Activity
			TableLayout cmtsTable = (TableLayout) cmmts_layout.findViewById(R.id.comments_table);
			cmtsTable.removeViewAt(cmtsTable.getChildCount() - 1);
		}

		/**
		 * Create rows displaying single comments to be displayed in a table.
		 */
		private void createCommentsRows() {
			// create table based on current comment
			TableLayout comtsTable = (TableLayout) cmmts_layout.findViewById(R.id.comments_table);

			for (final Comment comment : comments) {
				maxpage = comment.getPagecount();
				// get the table row by an inflater and set the needed
				// information
				final ViewGroup tableRow = (ViewGroup) LayoutInflater.from(CommentsActivity.this).inflate(
						R.layout.comments_row_layout, comtsTable, false);

				TextView usernameTxt = (TextView) tableRow.findViewById(R.id.cmts_username);
				usernameTxt.setText(comment.getUsername());
				usernameTxt.setSelected(true);
				((ImageView) tableRow.findViewById(R.id.cmts_usericon)).setImageBitmap(getUserPic(comment.getUid()));
				((TextView) tableRow.findViewById(R.id.cmts_date)).setText(createDate(comment.getUnixtime() * 1000L));
				((TextView) tableRow.findViewById(R.id.cmts_date)).setSelected(true);
				TextView content = (TextView) tableRow.findViewById(R.id.comment_content);
				content.setText(Html.fromHtml(comment.getStatement(),
						new TextViewHandler(CommentsActivity.this.getApplicationContext()), null));

				if (cwApplication.getAuthenticatedUser().getUid() == comment.getUid()) {
					View delete_edit_bttn_layout = LayoutInflater.from(CommentsActivity.this).inflate(
							R.layout.delete_edit_bttn_layout, comtsTable, false);
					ViewGroup parent_layout = (ViewGroup) tableRow.findViewById(R.id.cmts_bttn_delete_edit_layout);
					parent_layout.addView(delete_edit_bttn_layout);

					Button delete_bttn = (Button) delete_edit_bttn_layout.findViewById(R.id.cmts_bttn_delete);

					// first ask the user, if he wants to delete a comment
					delete_bttn.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							AlertDialog.Builder dialog = new AlertDialog.Builder(CommentsActivity.this.getParent())
									.setMessage(getString(R.string.comment_delete_question)).setCancelable(false)
									.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int which) {
											rowToDelete = tableRow;
											new DeleteCommentAsyncTask().execute(comment.getCid());
										}
									}).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
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
							View edit_layout = LayoutInflater.from(CommentsActivity.this).inflate(R.layout.edit_layout,
									tableRow, false);
							ViewGroup parent_layout = (ViewGroup) tableRow.findViewById(R.id.comment_content_layout);
							parent_layout.addView(edit_layout);
							createEditSubmitBttn(edit_layout, tableRow, comment.getCid());
						}
					});
				}
				publishProgress(tableRow);
			}
		}
	}

	private void createEditSubmitBttn(View parent, final View row, final int id) {
		Button edit_submit_bttn = (Button) parent.findViewById(R.id.cmts_bttn_edit_submit);
		edit_submit_bttn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new EditCommentAsyncTask().execute(id, row);
			}
		});
	}

	/**
	 * Asynchronous task to submit a comment.
	 * 
	 * @author Alexander Dridiger
	 */
	private class SubmitCommentAsyncTask extends AsyncTask<Void, Void, Void> {

		boolean dowork = false;

		@Override
		protected void onPreExecute() {
			EditText commenttxt = (EditText) cmmts_layout.findViewById(R.id.comments_edttxt_input);
			if (commenttxt.getText().toString().matches("")) {
				commenttxt.requestFocus();
				commenttxt.setError(getString(R.string.no_text_entered));
			} else if (StringUtils.isBlank(cwApplication.getAuthenticatedUser().getUsername())) {
				commenttxt.requestFocus();
				commenttxt.setError(getString(R.string.not_logged_in));
			} else {
				commenttxt.setError(null);
				Toast.makeText(CommentsActivity.this, getResources().getString(R.string.comment_sending),
						Toast.LENGTH_SHORT).show();
				dowork = true;
			}
		}

		@Override
		protected Void doInBackground(Void... params) {
			if (dowork) {
				EditText commenttxt = (EditText) cmmts_layout.findViewById(R.id.comments_edttxt_input);
				if (cwApplication.getAuthenticatedUser() != null) {
					try {
						httpPoster
								.sendPost(
										getString(R.string.cw_posting_url),
										getString(R.string.cw_cookie_full, cwApplication.getAuthenticatedUser()
												.getUid(), cwApplication.getAuthenticatedUser().getPasswordHash()),
										getString(R.string.cw_cmmt_submit_data, area, id,
												getString(R.string.cw_command_newentry), URLEncoder.encode(commenttxt
														.getText().toString(), getString(R.string.utf8)), 1));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (dowork) {
				Toast.makeText(CommentsActivity.this, getResources().getString(R.string.comment_sent),
						Toast.LENGTH_SHORT).show();
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
			Toast.makeText(CommentsActivity.this, getResources().getString(R.string.comment_deleting),
					Toast.LENGTH_SHORT).show();
		}

		@Override
		protected Void doInBackground(Integer... ids) {
			if (cwApplication.getAuthenticatedUser() != null) {
				try {
					httpPoster.sendPost(
							getString(R.string.cw_posting_url),
							getString(R.string.cw_cookie_full, cwApplication.getAuthenticatedUser().getUid(),
									cwApplication.getAuthenticatedUser().getPasswordHash()),
							getString(R.string.cw_cmmt_delete_data, area, id, ids[0],
									getString(R.string.cw_command_remove), 1));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (rowToDelete.getParent() instanceof TableLayout) {
				TableLayout comtsTable = (TableLayout) rowToDelete.getParent();
				comtsTable.removeView(rowToDelete);
			}
			Toast.makeText(CommentsActivity.this, getResources().getString(R.string.comment_deleted),
					Toast.LENGTH_SHORT).show();
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
			Toast.makeText(CommentsActivity.this, getResources().getString(R.string.comment_editing),
					Toast.LENGTH_SHORT).show();
		}

		@Override
		protected View doInBackground(Object... params) {
			View row = (View) params[1];
			if (cwApplication.getAuthenticatedUser() != null) {
				try {
					EditText textToEdit = (EditText) row.findViewById(R.id.cmts_edtxt_edit);
					httpPoster.sendPost(
							getString(R.string.cw_posting_url),
							getString(R.string.cw_cookie_full, cwApplication.getAuthenticatedUser().getUid(),
									cwApplication.getAuthenticatedUser().getPasswordHash()),
							getString(R.string.cw_cmmt_edit_data, area, id, (Integer) params[0],
									getString(R.string.cw_command_update),
									URLEncoder.encode(textToEdit.getText().toString(), getString(R.string.utf8)), 1));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return row;
		}

		@Override
		protected void onPostExecute(View row) {
			ViewGroup parent_layout = (ViewGroup) row.findViewById(R.id.comment_content_layout);
			EditText textToEdit = (EditText) row.findViewById(R.id.cmts_edtxt_edit);
			TextView content = (TextView) row.findViewById(R.id.comment_content);
			content.setText(Html.fromHtml(textToEdit.getText().toString(),
					new TextViewHandler(CommentsActivity.this.getApplicationContext()), null));
			parent_layout.removeViewAt(1);
			Toast.makeText(CommentsActivity.this, getResources().getString(R.string.comment_edited), Toast.LENGTH_SHORT)
					.show();
		}
	}

	@Override
	public void onBackPressed() {
		if (getParent() instanceof CwBasicActivityGroup) {
			((CwBasicActivityGroup) getParent()).back();
		}
	}
}
