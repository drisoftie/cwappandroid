package de.consolewars.android.app.tab.news;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.IllegalFormatException;
import java.util.Locale;
import java.util.TimeZone;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

		/*
		 * TODO: Might become a source of error someday, if activity design changes. Would be better
		 * to handle it with intents.
		 */
		if (getParent().getParent() instanceof CwNavigationMainTabActivity) {
			mainTabs = (CwNavigationMainTabActivity) getParent().getParent();
		}
		new BuildSingleNewsAsyncTask().execute();
	}

	private View createNewsView() {
		View newsView = LayoutInflater.from(SingleNewsActivity.this.getParent()).inflate(
				R.layout.singlenews_layout, null);

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

		TextView text = (TextView) newsView.findViewById(R.id.singlenews_newstext);
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
			createHeader(newsView, news);
		}
		return newsView;
	}

	private void createHeader(View parent, News news) {
		ImageView icon = (ImageView) parent.findViewById(R.id.header_icon);
		icon.setImageResource(this.getResources().getIdentifier(
				getString(R.string.cat_drawable, news.getCategoryshort()),
				getString(R.string.drawable), getApplicationContext().getPackageName()));

		ImageView usericon = (ImageView) parent.findViewById(R.id.header_descr_usericon);
		loadPicture(usericon, news);
		TextView cattxt = (TextView) parent.findViewById(R.id.header_title);
		cattxt.setText(news.getCategory());
		TextView title = (TextView) parent.findViewById(R.id.header_descr_title);
		title.setText(news.getTitle());
		TextView info = (TextView) parent.findViewById(R.id.header_descr_info);
		info.setText(createDate(news.getUnixtime() * 1000L) + " von " + news.getAuthor());
		TextView descr = (TextView) parent.findViewById(R.id.header_descr);
		descr.setText(news.getDescription());
	}

	/**
	 * Downloads the user picture and decodes it into a {@link Bitmap} to be set into an ImageView.
	 * 
	 * @param view
	 *            the ImageView
	 * @param uid
	 *            user id is needed to get the appropriate picture
	 */
	private void loadPicture(ImageView view, News news) {
		URL newurl;
		Bitmap mIcon_val = null;
		try {
			newurl = new URL(getString(R.string.general_picture, news.getPicid(), 60));
			mIcon_val = BitmapFactory.decodeStream(newurl.openConnection().getInputStream());
			view.setImageBitmap(mIcon_val);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param unixtime
	 * @return
	 */
	private CharSequence createDate(long unixtime) {
		Date date = new Date(unixtime);
		TimeZone zone = TimeZone.getDefault();

		Calendar cal = Calendar.getInstance(zone, Locale.GERMANY);
		SimpleDateFormat dateformat = new SimpleDateFormat("HH:mm dd.MM.yyyy");
		dateformat.setCalendar(cal);
		String formattedDate = dateformat.format(date);
		return (formattedDate.matches("")) ? ("")
				: (formattedDate.subSequence(0, 5) + " Uhr " + formattedDate.subSequence(6,
						formattedDate.length()));
	}

	/**
	 * Asynchronous task to receive a single news from the API and build up the ui.
	 * 
	 * @author Alexander Dridiger
	 */
	private class BuildSingleNewsAsyncTask extends AsyncTask<Void, Integer, View> {

		private ProgressBar progressBar;

		@Override
		protected void onPreExecute() {
			// first set progressbar view
			ViewGroup progress_layout = (ViewGroup) LayoutInflater.from(
					SingleNewsActivity.this.getParent()).inflate(R.layout.centered_progressbar,
					null);
			setContentView(progress_layout);

			TextView text = (TextView) progress_layout.findViewById(R.id.centered_progressbar_text);
			text.setText(getString(R.string.loading, "Einzelnews"));

			progressBar = (ProgressBar) progress_layout.findViewById(R.id.centered_progressbar);
			progressBar.setProgress(0);
		}

		@Override
		protected View doInBackground(Void... params) {
			return createNewsView();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			progressBar.setProgress(values[0]);
		}

		@Override
		protected void onPostExecute(View result) {
			setContentView(result);
		}
	}
}
