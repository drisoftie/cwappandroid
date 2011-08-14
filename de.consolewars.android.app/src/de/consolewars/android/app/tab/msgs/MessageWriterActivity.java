package de.consolewars.android.app.tab.msgs;

import java.io.IOException;
import java.net.URLEncoder;

import org.htmlcleaner.XPatherException;

import roboguice.activity.RoboActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.inject.Inject;

import de.consolewars.android.app.CWApplication;
import de.consolewars.android.app.R;
import de.consolewars.android.app.tab.CwBasicActivityGroup;
import de.consolewars.android.app.util.HttpPoster;
import de.consolewars.android.app.util.MediaSnapper;

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
 * Central Activity to handle the ui for the shoutbox. <br>
 * NOTE: Currently not working.
 * 
 * @author Alexander Dridiger
 */
public class MessageWriterActivity extends RoboActivity {

	@Inject
	private CWApplication cwApplication;
	@Inject
	private HttpPoster httpPoster;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.messagewriter_layout);
		initSentBttn();
	}

	private void initSentBttn() {
		Button submit = (Button) findViewById(R.id.messagewriter_bttn_submit);
		submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new SendMessageAsyncTask().execute();
			}
		});
	}

	/**
	 * Asynchronous task to send a private message to a CW user.
	 * 
	 * @author Alexander Dridiger
	 */
	private class SendMessageAsyncTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			Toast.makeText(MessageWriterActivity.this, getResources().getString(R.string.messagewriter_sending),
					Toast.LENGTH_SHORT).show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			EditText receipt = (EditText) findViewById(R.id.messagewriter_edttxt_recipients);
			EditText title = (EditText) findViewById(R.id.messagewriter_edttxt_title);
			EditText message = (EditText) findViewById(R.id.messagewriter_edttxt_message);

			String securityToken = "";
			try {
				securityToken = MediaSnapper.snapWithCookies(
						MessageWriterActivity.this,
						getString(R.string.xpath_get_securitytoken),
						getString(R.string.value),
						getString(R.string.cw_getsecuritytoken_url),
						getString(R.string.cw_cookie_full, cwApplication.getAuthenticatedUser().getUid(), cwApplication
								.getAuthenticatedUser().getPasswordHash()));
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (XPatherException e1) {
				e1.printStackTrace();
			}

			try {
				httpPoster.sendPost(
						getString(R.string.cw_message_url),
						getString(R.string.cw_cookie_full, cwApplication.getAuthenticatedUser().getUid(), cwApplication
								.getAuthenticatedUser().getPasswordHash()),
						getString(R.string.cw_mssg_submit_data,
								URLEncoder.encode(message.getText().toString(), getString(R.string.utf8)),
								URLEncoder.encode(receipt.getText().toString(), getString(R.string.utf8)), 1,
								URLEncoder.encode(title.getText().toString(), getString(R.string.utf8)), 1,
								securityToken, 1, 1));
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			Toast.makeText(MessageWriterActivity.this, getResources().getString(R.string.messagewriter_sent),
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onBackPressed() {
		if (getParent() instanceof CwBasicActivityGroup) {
			((CwBasicActivityGroup) getParent()).back();
		}
	}
}
