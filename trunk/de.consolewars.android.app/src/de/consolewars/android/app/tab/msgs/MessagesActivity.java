package de.consolewars.android.app.tab.msgs;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import roboguice.activity.RoboActivity;
import android.app.ActivityGroup;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.inject.Inject;

import de.consolewars.android.app.APICaller;
import de.consolewars.android.app.CWApplication;
import de.consolewars.android.app.R;
import de.consolewars.android.app.util.StyleSpannableStringBuilder;
import de.consolewars.api.API;
import de.consolewars.api.data.Message;
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
 * Central Activity to handle the ui for messages.
 * 
 * @author Alexander Dridiger
 */
public class MessagesActivity extends RoboActivity {

	@Inject
	private CWApplication cwApplication;
	@Inject
	private APICaller apiCaller;
	@Inject
	private API api;

	private List<Message> msgs = new ArrayList<Message>();

	// remember last selected table row to draw the background
	private View selectedRow;

	private MsgFilter currentFilter = MsgFilter.INBOX;

	private StyleSpannableStringBuilder styleStringBuilder;

	private enum MsgFilter {
		INBOX(0, 0), OUTBOX(1, -1);

		private int position;
		private int filter;

		private MsgFilter(int position, int filter) {
			this.position = position;
			this.filter = filter;
		}

		public int getPosition() {
			return position;
		}

		public int getFilter() {
			return filter;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		new BuildMessagesAsyncTask().execute();
	}

	/**
	 * Create the ui for displaying messages in a table.
	 */
	private List<View> createMsgsRows() {
		// create table based on current messages
		View msgsView = LayoutInflater.from(MessagesActivity.this.getParent()).inflate(R.layout.msgs_layout, null);
		TableLayout msgsTable = (TableLayout) msgsView.findViewById(R.id.msgs_table);

		styleStringBuilder = new StyleSpannableStringBuilder();

		List<View> rows = new ArrayList<View>();

		for (final Message msg : this.msgs) {
			// get the table row by an inflater and set the needed information
			final View tableRow = LayoutInflater.from(this).inflate(R.layout.msgs_row_layout, msgsTable, false);

			tableRow.setId(msg.getId());
			tableRow.setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) {
					if (selectedRow != null) {
						selectedRow.setBackgroundDrawable(getResources().getDrawable(R.drawable.table_cell_bg));
					}
					tableRow.setBackgroundDrawable(getResources().getDrawable(R.drawable.table_cell_bg_selected));
					selectedRow = tableRow;
					getSingleMessage(msg.getMessage());
				}
			});
			((ImageView) tableRow.findViewById(R.id.msgs_row_read_icon)).setImageResource(createMessageIcon(msg
					.isMessageread()));
			((TextView) tableRow.findViewById(R.id.msgs_row_title)).setText(createTitle(msg.getTitle()));
			((TextView) tableRow.findViewById(R.id.msgs_row_date)).setText(createDate(msg.getUnixtime() * 1000L));
			((TextView) tableRow.findViewById(R.id.msgs_row_author)).setText(createFromUser(msg.getFromusername()));
			rows.add(tableRow);
		}
		styleStringBuilder = null;
		return rows;
	}

	/**
	 * Sets the newest messages of the logged in user.
	 * 
	 * @throws ConsolewarsAPIException
	 */
	private void setCurrentMessages() throws ConsolewarsAPIException {
		if (cwApplication.getAuthenticatedUser() != null) {
			msgs = api.getMessages(cwApplication.getAuthenticatedUser().getUid(), cwApplication.getAuthenticatedUser()
					.getPasswordHash(), currentFilter.getFilter(), 10);
		} else {
			msgs = new ArrayList<Message>();
		}
	}

	/**
	 * Changes the current activity to a {@link SingleMessageActivity} with the selected news.
	 * 
	 * @param text
	 */
	private void getSingleMessage(String text) {
		Intent singleMessageIntent = new Intent(MessagesActivity.this, SingleMessageActivity.class);

		singleMessageIntent.putExtra(MessagesActivity.class.getName(), text);

		View view = ((ActivityGroup) getParent())
				.getLocalActivityManager()
				.startActivity(SingleMessageActivity.class.getSimpleName(),
						singleMessageIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)).getDecorView();
		// replace the view
		((MessagesActivityGroup) getParent()).replaceView(view);
	}

	private int createMessageIcon(boolean read) {
		int imageResource = R.drawable.cat_side;
		if (read) {
			imageResource = R.drawable.pm_old;
		} else {
			imageResource = R.drawable.pm_new;
		}
		return imageResource;
	}

	/**
	 * Formatted string of a message title.
	 * 
	 * @param title
	 * @return a formatted {@link CharSequence}
	 */
	private CharSequence createTitle(String title) {
		// TODO text formatting
		return title;
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
	 * Formatted string of a sender of a message.
	 * 
	 * @param author
	 *            sender of a message
	 * @return a formatted {@link CharSequence}
	 */
	private CharSequence createFromUser(String author) {
		// TODO more text formatting

		if (author == null || author.matches("")) {
			author = getString(R.string.news_author_unknown);
		}
		styleStringBuilder.clear();
		styleStringBuilder.appendWithStyle(new ForegroundColorSpan(0xFF007711), getString(R.string.news_author_by));
		styleStringBuilder.append(" ");
		styleStringBuilder.appendWithStyle(new ForegroundColorSpan(0xFF009933), author);

		return styleStringBuilder;
	}

	/**
	 * Filter ui and logic for filtering news.
	 * 
	 * @param parentView
	 *            to find the inflated view elements.
	 */
	private void initFilter(final View parentView) {

		Spinner spinner = (Spinner) parentView.findViewById(R.id.msg_filter_spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(MessagesActivity.this,
				R.array.msgs_filter_options, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setSelection(currentFilter.getPosition());
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> aView, View view, int position, long id) {
				MsgFilter selected;
				if (position == MsgFilter.OUTBOX.getPosition()) {
					selected = MsgFilter.OUTBOX;
				} else {
					selected = MsgFilter.INBOX;
				}
				if (!currentFilter.equals(selected)) {
					currentFilter = selected;
					removeAllMessages(parentView);
					new BuildMessagesAsyncTask().execute();
				}
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}

	private void initRefreshBttn(final View parentView) {
		Button refresh = (Button) parentView.findViewById(R.id.msg_bttn_refresh);
		refresh.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				removeAllMessages(parentView);
				new BuildMessagesAsyncTask().execute();
			}
		});
	}

	private void initSendMsgBttn(final View parentView) {
		Button new_bttn = (Button) parentView.findViewById(R.id.msg_bttn_new_mssg);
		new_bttn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent newMsgIntent = new Intent(MessagesActivity.this, MessageWriterActivity.class);

				View view = ((ActivityGroup) getParent())
						.getLocalActivityManager()
						.startActivity(MessageWriterActivity.class.getSimpleName(),
								newMsgIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)).getDecorView();
				// replace the view
				((MessagesActivityGroup) getParent()).replaceView(view);
			}
		});
	}

	private void removeAllMessages(View parentView) {
		TableLayout msgsTable = (TableLayout) parentView.findViewById(R.id.msgs_table);
		msgsTable.removeAllViews();
	}

	/**
	 * Asynchronous task to receive messages from the API and build up the ui.
	 * 
	 * @author Alexander Dridiger
	 */
	public class BuildMessagesAsyncTask extends AsyncTask<Void, Integer, List<View>> {

		private ProgressBar progressBar;

		@Override
		protected void onPreExecute() {
			// first set progressbar view
			ViewGroup progress_layout = (ViewGroup) LayoutInflater.from(MessagesActivity.this.getParent()).inflate(
					R.layout.centered_progressbar, null);
			setContentView(progress_layout);

			TextView text = (TextView) progress_layout.findViewById(R.id.centered_progressbar_text);
			text.setText(getString(R.string.loading, getString(R.string.tab_msgs_head)));

			progressBar = (ProgressBar) progress_layout.findViewById(R.id.centered_progressbar);
			progressBar.setProgress(0);
		}

		@Override
		protected List<View> doInBackground(Void... params) {
			try {
				apiCaller.authenticateOnCW();
				setCurrentMessages();
			} catch (ConsolewarsAPIException e) {
				e.printStackTrace();
				Log.e(getString(R.string.exc_auth_tag), e.getMessage(), e);
				return new ArrayList<View>();
			}
			return createMsgsRows();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			progressBar.setProgress(values[0]);
		}

		@Override
		protected void onPostExecute(List<View> result) {
			// sets the messages view for this Activity
			ViewGroup msgs_layout = (ViewGroup) LayoutInflater.from(MessagesActivity.this.getParent()).inflate(
					R.layout.msgs_layout, null);
			initFilter(msgs_layout);
			initRefreshBttn(msgs_layout);
			initSendMsgBttn(msgs_layout);
			TableLayout msgsTable = (TableLayout) msgs_layout.findViewById(R.id.msgs_table);
			for (View row : result) {
				msgsTable.addView(row);
			}
			setContentView(msgs_layout);
		}
	}
}
