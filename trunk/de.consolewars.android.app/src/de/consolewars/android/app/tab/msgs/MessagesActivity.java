package de.consolewars.android.app.tab.msgs;

import java.util.ArrayList;
import java.util.List;

import roboguice.activity.RoboActivity;
import android.app.ActivityGroup;
import android.content.Context;
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
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.inject.Inject;

import de.consolewars.android.app.CWManager;
import de.consolewars.android.app.Filter;
import de.consolewars.android.app.R;
import de.consolewars.android.app.util.DateUtility;
import de.consolewars.android.app.util.StyleSpannableStringBuilder;
import de.consolewars.android.app.util.ViewUtility;
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
	private CWManager cwManager;
	@Inject
	private ViewUtility viewUtility;

	private List<Message> msgs = new ArrayList<Message>();

	private Filter currentFilter = Filter.MSGS_INBOX;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		new BuildMessagesAsyncTask().execute();
	}

	/**
	 * Asynchronous task to receive messages from the API and build up the ui.
	 * 
	 * @author Alexander Dridiger
	 */
	public class BuildMessagesAsyncTask extends AsyncTask<Void, Integer, List<Message>> {

		@Override
		protected void onPreExecute() {
			// first set progressbar view
			ViewGroup progress_layout = viewUtility.getCenteredProgressBarLayout(
					LayoutInflater.from(MessagesActivity.this.getParent()), R.string.tab_msgs_head);
			setContentView(progress_layout);
		}

		@Override
		protected List<Message> doInBackground(Void... params) {
			try {
				msgs = cwManager.getMessages(currentFilter, 10);
			} catch (ConsolewarsAPIException e) {
				e.printStackTrace();
				Log.e(getString(R.string.exc_auth_tag), e.getMessage(), e);
				return new ArrayList<Message>();
			}
			return msgs;
		}

		@Override
		protected void onPostExecute(List<Message> result) {
			// sets the messages view for this Activity
			ViewGroup msgs_layout = (ViewGroup) LayoutInflater.from(MessagesActivity.this.getParent()).inflate(
					R.layout.msgs_layout, null);
			initFilter(msgs_layout);
			initRefreshBttn(msgs_layout);
			initSendMsgBttn(msgs_layout);
			ListView msgsListView = (ListView) msgs_layout.findViewById(R.id.msgs_list_view);
			msgsListView.setAdapter(new MessageRowAdapter(MessagesActivity.this, result));
			setContentView(msgs_layout);
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
					Filter selected;
					if (position == Filter.MSGS_OUTBOX.getPosition()) {
						selected = Filter.MSGS_OUTBOX;
					} else {
						selected = Filter.MSGS_INBOX;
					}
					if (!currentFilter.equals(selected)) {
						currentFilter = selected;
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

				public void onClick(View view) {
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
	}

	public class MessageRowAdapter extends ArrayAdapter<Message> {

		private LayoutInflater mInflater;
		private List<Message> rows;

		public MessageRowAdapter(Context context, List<Message> rows) {
			super(context, R.layout.msgs_row_layout, rows);
			mInflater = LayoutInflater.from(context);
			this.rows = rows;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			final Message row = rows.get(position);

			ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.msgs_row_layout, null);
				holder = new ViewHolder();
				holder.icon = (ImageView) convertView.findViewById(R.id.msgs_row_read_icon);
				holder.title = (TextView) convertView.findViewById(R.id.msgs_row_title);
				holder.date = (TextView) convertView.findViewById(R.id.msgs_row_date);
				holder.author = (TextView) convertView.findViewById(R.id.msgs_row_author);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.icon.setImageResource(createMessageIcon(row.isMessageread()));
			holder.title.setText(createTitle(row.getTitle()));
			holder.date.setText(createDate(row.getUnixtime() * 1000L));
			holder.author.setText(createFromUser(row.getFromusername()));

			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					getSingleMessage(row.getMessage());
				}
			});

			return convertView;
		}

		class ViewHolder {
			ImageView icon;
			TextView title;
			TextView date;
			TextView author;
		}

		/**
		 * Changes the current activity to a {@link SingleMessageActivity} with
		 * the selected news.
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
			int imageResource;
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
			return DateUtility.createDate(unixtime, "HH:mm dd.MM.yyyy");
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
			StyleSpannableStringBuilder styleStringBuilder = new StyleSpannableStringBuilder();
			styleStringBuilder.clear();
			styleStringBuilder.appendWithStyle(new ForegroundColorSpan(0xFF007711), getString(R.string.news_author_by));
			styleStringBuilder.append(" ");
			styleStringBuilder.appendWithStyle(new ForegroundColorSpan(0xFF009933), author);

			return styleStringBuilder;
		}
	}
}
