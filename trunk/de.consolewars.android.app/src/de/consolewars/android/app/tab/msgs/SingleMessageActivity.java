package de.consolewars.android.app.tab.msgs;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import de.consolewars.android.app.R;

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
 * Activity showing and handling a single message.
 * 
 * @author Alexander Dridiger
 */
public class SingleMessageActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.singlemsg_layout);

		TextView msg_view = (TextView) findViewById(R.id.singlemsg_content);
		String text = "";

		// looking for the correct intent
		if (getIntent().hasExtra(MessagesActivity.class.getName())) {
			text = getIntent().getStringExtra(MessagesActivity.class.getName());
		}

		// up to now only the text of the blog is shown
		if (text != null) {
			msg_view.setText(text);
		}
	}
}
