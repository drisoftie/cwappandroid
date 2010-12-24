package de.consolewars.android.app.tab.blogs;

import java.util.IllegalFormatException;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;
import de.consolewars.android.app.R;
import de.consolewars.android.app.tab.CwNavigationMainTabActivity;
import de.consolewars.api.data.Blog;
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
 * Activity showing and handling a single blog.
 * 
 * @author Alexander Dridiger
 */
public class SingleBlogActivity extends Activity {

	private CwNavigationMainTabActivity mainTabs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.singleblog_layout);

		/*
		 * TODO: Might become a source of error someday, if activity design changes. Would be better
		 * to handle it with intents.
		 */
		if (getParent().getParent() instanceof CwNavigationMainTabActivity) {
			mainTabs = (CwNavigationMainTabActivity) getParent().getParent();
		}

		TextView text = (TextView) findViewById(R.id.singleblog_content);
		int id = -1;

		// looking for the correct intent
		if (getIntent().hasExtra(BlogsActivity.class.getName())) {
			id = getIntent().getIntExtra(BlogsActivity.class.getName(), -1);
		}

		Blog blog = null;

		try {
			blog = mainTabs.getApiCaller().getApi().getBlog(id);
		} catch (ConsolewarsAPIException e) {
			e.printStackTrace();
		}

		// up to now only the text of the blog is shown
		if (id == -1) {
			text.setText("Fehler");
		} else if (blog != null) {
			try {
				String fString = String.format(blog.getArticle(), "");
				CharSequence styledString = Html.fromHtml(fString);
				text.setText(styledString);
			} catch (IllegalFormatException ife) {
				// FIXME Wrong format handling
				text.setText(blog.getArticle());
			}
		}
	}
}
