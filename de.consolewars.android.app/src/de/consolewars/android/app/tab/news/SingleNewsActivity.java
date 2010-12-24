package de.consolewars.android.app.tab.news;

import java.util.IllegalFormatException;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;
import de.consolewars.android.app.R;
import de.consolewars.android.app.tab.CwNavigationMainTabActivity;
import de.consolewars.api.data.News;
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
public class SingleNewsActivity extends Activity {

	private CwNavigationMainTabActivity mainTabs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.singlenews_layout);

		/*
		 * TODO: Might become a source of error someday, if activity design changes. Would be better
		 * to handle it with intents.
		 */
		if (getParent().getParent() instanceof CwNavigationMainTabActivity) {
			mainTabs = (CwNavigationMainTabActivity) getParent().getParent();
		}

		TextView text = (TextView) findViewById(R.id.singlenews_content);
		int id = -1;

		// looking for the correct intent
		if (getIntent().hasExtra(NewsActivity.class.getName())) {
			id = getIntent().getIntExtra(NewsActivity.class.getName(), -1);
		}

		News news = null;

		try {
			news = mainTabs.getApiCaller().getApi().getNews(id);
		} catch (ConsolewarsAPIException e) {
			e.printStackTrace();
		}

		// up to now only the text of the blog is shown
		if (id == -1) {
			text.setText("Fehler");
		} else if (news != null) {
			try {
				String fString = String.format(news.getArticle(), "");
				CharSequence styledString = Html.fromHtml(fString);
				text.setText(styledString);
			} catch (IllegalFormatException ife) {
				// FIXME Wrong format handling
				text.setText(news.getArticle());
			}
		}
	}
}
