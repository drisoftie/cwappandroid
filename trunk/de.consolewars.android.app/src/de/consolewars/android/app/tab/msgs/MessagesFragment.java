package de.consolewars.android.app.tab.msgs;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import de.consolewars.android.app.CwApplication;
import de.consolewars.android.app.Filter;
import de.consolewars.android.app.R;
import de.consolewars.android.app.db.domain.CwMessage;
import de.consolewars.android.app.tab.CwAbstractFragment;
import de.consolewars.android.app.tab.CwAbstractFragmentActivity;
import de.consolewars.android.app.tab.CwNavigationMainTabActivity;
import de.consolewars.android.app.util.DateUtility;
import de.consolewars.android.app.util.StyleSpannableStringBuilder;
import de.consolewars.android.app.view.ActionBar;
import de.consolewars.android.app.view.ActionBar.Action;

/*
 * Copyright [2011] [Alexander Dridiger]
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
 * Activity showing and handling options.
 * 
 * @author Alexander Dridiger
 */
public class MessagesFragment extends CwAbstractFragment {

	private List<CwMessage> msgs = new ArrayList<CwMessage>();

	ViewGroup msgs_layout;

	public MessagesFragment() {
	}

	public MessagesFragment(String title, int position) {
		super(title, position);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		msgs_layout = (ViewGroup) inflater.inflate(R.layout.msgs_layout, null);
		return msgs_layout;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (((CwAbstractFragmentActivity) getActivity()).lastPosition == getPosition()) {
			initActionBar();
		}
		new BuildMessagesAsyncTask().execute();
	}

	@Override
	public void refresh() {
		if (((CwAbstractFragmentActivity) getActivity()).lastPosition == getPosition()) {
			initActionBar();
		}
	}

	/**
	 * Asynchronous task to receive messages from the API and build up the ui.
	 * 
	 * @author Alexander Dridiger
	 */
	public class BuildMessagesAsyncTask extends AsyncTask<Void, Integer, List<CwMessage>> {

		@Override
		protected void onPreExecute() {
			getActionBar().setProgressBarVisibility(View.VISIBLE);
		}

		@Override
		protected List<CwMessage> doInBackground(Void... params) {
			msgs = CwApplication.cwManager().getMessages(getFilter(getPosition()), 10);
			return msgs;
		}

		@Override
		protected void onPostExecute(List<CwMessage> result) {
			// sets the messages view for this Activity
			ListView msgsListView = (ListView) msgs_layout.findViewById(R.id.msgs_list_view);
			msgsListView.setAdapter(new MessageRowAdapter(getActivity(), result));
			getActionBar().setProgressBarVisibility(View.INVISIBLE);
		}

		private void initSendMsgBttn(final View parentView) {
			// Button new_bttn = (Button) parentView.findViewById(R.id.msg_bttn_new_mssg);
			// new_bttn.setOnClickListener(new OnClickListener() {
			// @Override
			// public void onClick(View arg0) {
			// // Intent newMsgIntent = new Intent(MessagesActivity.this, MessageWriterActivity.class);
			// //
			// // View view = ((ActivityGroup) getParent())
			// // .getLocalActivityManager()
			// // .startActivity(MessageWriterActivity.class.getSimpleName(),
			// // newMsgIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)).getDecorView();
			// // // replace the view
			// // ((MessagesActivityGroup) getParent()).replaceView(view);
			// }
			// });
		}
	}

	public class MessageRowAdapter extends ArrayAdapter<CwMessage> {

		private LayoutInflater mInflater;

		public MessageRowAdapter(Context context, List<CwMessage> rows) {
			super(context, R.layout.msgs_row_layout, rows);
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			final CwMessage row = getItem(position);

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

			holder.icon.setImageResource(createMessageIcon(row.isMessageRead()));
			holder.title.setText(createTitle(row.getTitle()));
			holder.date.setText(createDate(row.getUnixtime() * 1000L));
			holder.author.setText(createFromUser(row.getFromUsername()));

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
		 * Changes the current activity to a {@link SingleMessageActivity} with the selected news.
		 * 
		 * @param text
		 */
		private void getSingleMessage(String text) {
			// Intent singleMessageIntent = new Intent(MessagesActivity.this, SingleMessageActivity.class);
			//
			// singleMessageIntent.putExtra(MessagesActivity.class.getName(), text);
			//
			// View view = ((ActivityGroup) getParent())
			// .getLocalActivityManager()
			// .startActivity(SingleMessageActivity.class.getSimpleName(),
			// singleMessageIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)).getDecorView();
			// // replace the view
			// ((MessagesActivityGroup) getParent()).replaceView(view);
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

	private void initActionBar() {
		if (getActivity() != null) {
			if (getActivity().getParent() instanceof CwNavigationMainTabActivity) {
				ActionBar actionBar = getActionBar();
				actionBar.removeAllActions();
				setHomeAction();
				actionBar.setTitle(getActivity().getString(R.string.messages_area));
				actionBar.setDisplayHomeAsUpEnabled(true);
				actionBar.addAction(new Action() {
					@Override
					public void performAction(View view) {
					}

					@Override
					public int getDrawable() {
						return R.drawable.def_bttn_new_mssg;
					}
				});
				actionBar.addAction(new Action() {
					@Override
					public void performAction(View view) {
						new BuildMessagesAsyncTask().execute();
					}

					@Override
					public int getDrawable() {
						return R.drawable.refresh_blue_bttn;
					}
				});
			}
		}
	}

	private static Filter getFilter(int position) {
		return (Filter.MSGS_INBOX.getPosition() == position) ? Filter.MSGS_INBOX : Filter.MSGS_OUTBOX;
	}

	@Override
	public void backPressed() {
	}
}
