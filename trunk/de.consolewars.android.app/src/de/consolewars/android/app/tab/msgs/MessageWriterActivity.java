package de.consolewars.android.app.tab.msgs;

import roboguice.activity.RoboActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.inject.Inject;

import de.consolewars.android.app.CWManager;
import de.consolewars.android.app.R;
import de.consolewars.android.app.tab.CwBasicActivityGroup;

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
 * Activity to handle message writing and sending.
 * 
 * @author Alexander Dridiger
 */
public class MessageWriterActivity extends RoboActivity {

	@Inject
	private CWManager cwManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.messagewriter_layout);
		initSendBttn();
	}

	private void initSendBttn() {
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
			EditText recipient = (EditText) findViewById(R.id.messagewriter_edttxt_recipients);
			EditText title = (EditText) findViewById(R.id.messagewriter_edttxt_title);
			EditText message = (EditText) findViewById(R.id.messagewriter_edttxt_message);
			CheckBox receipt = (CheckBox) findViewById(R.id.messagewriter_check_receipt);
			CheckBox copy = (CheckBox) findViewById(R.id.messagewriter_check_copy);
			CheckBox signature = (CheckBox) findViewById(R.id.messagewriter_check_signature);
			CheckBox parseurl = (CheckBox) findViewById(R.id.messagewriter_check_parseurl);
			CheckBox disablesmilies = (CheckBox) findViewById(R.id.messagewriter_check_disablesmilies);

			cwManager.sendMessage(message.getText().toString(), recipient.getText().toString(), title.getText()
					.toString(), copy.isChecked(), parseurl.isChecked(), signature.isChecked(), disablesmilies
					.isChecked(), receipt.isChecked());
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
