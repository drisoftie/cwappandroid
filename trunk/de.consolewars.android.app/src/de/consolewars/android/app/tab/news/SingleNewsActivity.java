package de.consolewars.android.app.tab.news;

import java.util.IllegalFormatException;

import roboguice.activity.RoboActivity;
import android.app.ActivityGroup;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.inject.Inject;

import de.consolewars.android.app.CWManager.CommentArea;
import de.consolewars.android.app.R;
import de.consolewars.android.app.tab.cmts.CommentsActivity;
import de.consolewars.android.app.util.DateUtility;
import de.consolewars.android.app.util.MediaSnapper;
import de.consolewars.android.app.util.TextViewHandler;
import de.consolewars.android.app.util.ViewUtility;
import de.consolewars.api.API;
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
public class SingleNewsActivity extends RoboActivity {

	@Inject
	private API api;
	@Inject
	private ViewUtility viewUtility;

	private ViewGroup newsView;

	private News news;

	private String vidURL = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		newsView = (ViewGroup) LayoutInflater.from(getParent()).inflate(R.layout.singlenews_layout, null);
		new BuildSingleNewsAsyncTask().execute();
	}

	/**
	 * Asynchronous task to receive a single news from the API and build up the
	 * ui.
	 * 
	 * @author Alexander Dridiger
	 */
	private class BuildSingleNewsAsyncTask extends AsyncTask<Void, Integer, View> {

		@Override
		protected void onPreExecute() {
			// first set progressbar view
			ViewGroup progress_layout = viewUtility.getCenteredProgressBarLayout(
					LayoutInflater.from(SingleNewsActivity.this.getParent()), R.string.singlenews);
			setContentView(progress_layout);
		}

		@Override
		protected View doInBackground(Void... params) {
			return createNewsView();
		}

		@Override
		protected void onPostExecute(View result) {
			setContentView(result);
		}

		private View createNewsView() {
			int id = -1;

			// looking for the correct intent
			if (getIntent().hasExtra(NewsActivity.class.getName())) {
				id = getIntent().getIntExtra(NewsActivity.class.getName(), -1);
			}
			news = null;
			try {
				news = api.getNews(id);
			} catch (ConsolewarsAPIException e) {
				e.printStackTrace();
			}
			TextView text = (TextView) newsView.findViewById(R.id.singlenews_newstext);

			if (id == -1) {
				text.setText(getString(R.string.failure));
			} else if (news != null) {
				try {
					text.setText(Html.fromHtml(news.getArticle(),
							new TextViewHandler(SingleNewsActivity.this.getApplicationContext()), null));
				} catch (IllegalFormatException ife) {
					// FIXME Wrong format handling
					text.setText(news.getArticle());
				}
				createHeader(newsView, news);
				createCommentBttn();
				vidURL = MediaSnapper.snapFromCleanedHTMLWithXPath(getString(R.string.cw_url, news.getUrl()),
						getString(R.string.xpath_get_video), getString(R.string.value));
				initVideos(news.getUrl());
			}
			return newsView;
		}

		private void createCommentBttn() {
			Button bttn = (Button) newsView.findViewById(R.id.singlenews_comments_bttn);
			bttn.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					Intent commentsIntent = new Intent(SingleNewsActivity.this, CommentsActivity.class);

					Bundle extra = new Bundle();
					extra.putInt(getString(R.string.type), CommentArea.NEWS.getValue());
					extra.putInt(getString(R.string.id), news.getId());
					extra.putInt(getString(R.string.comments_amount), news.getComments());

					commentsIntent.putExtras(extra);

					View view = ((ActivityGroup) getParent())
							.getLocalActivityManager()
							.startActivity(CommentsActivity.class.getSimpleName(),
									commentsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)).getDecorView();
					// replace the view
					((NewsActivityGroup) getParent()).replaceView(view);
				}
			});
		}

		/**
		 * Sets a video, if one exists in a news. Can't be invoked without
		 * calling Looper.prepare(). Don't use this within an AsyncTask!
		 * 
		 * @param url
		 */
		private void initVideos(String url) {
			if (vidURL != "") {
				WebView localWebView = (WebView) newsView.findViewById(R.id.singlenews_video);

				localWebView.getSettings().setUseWideViewPort(false);
				localWebView.getSettings().setPluginState(PluginState.ON);
				localWebView.getSettings().setPluginsEnabled(true);
				localWebView.getSettings().setJavaScriptEnabled(true);
				localWebView.getSettings().setBuiltInZoomControls(false);

				Log.i("******YOUTUBE*******", getString(R.string.youtube_embedding, 300, 200, vidURL));

				localWebView.loadDataWithBaseURL(getString(R.string.cw_url_slash),
						getString(R.string.youtube_embedding, 300, 200, vidURL), "text/html", "utf-8", null);
			}
		}

		private void createHeader(View parent, News news) {
			ImageView icon = (ImageView) parent.findViewById(R.id.header_icon);
			viewUtility.setCategoryIcon(icon, news.getCategoryshort());

			ImageView usericon = (ImageView) parent.findViewById(R.id.header_descr_usericon);
			loadPicture(usericon, news);

			TextView cattxt = (TextView) parent.findViewById(R.id.header_title);
			cattxt.setText(news.getCategory());
			TextView title = (TextView) parent.findViewById(R.id.header_descr_title);
			title.setText(news.getTitle());
			TextView info = (TextView) parent.findViewById(R.id.header_descr_info);
			info.setText(getString(R.string.singlenews_info, createDate(news.getUnixtime() * 1000L), news.getAuthor()));
			TextView descr = (TextView) parent.findViewById(R.id.header_descr);
			descr.setText(news.getDescription());
		}

		/**
		 * Downloads the user picture and decodes it into a {@link Bitmap} to be
		 * set into an ImageView.
		 * 
		 * @param view
		 *            the ImageView
		 * @param uid
		 *            user id is needed to get the appropriate picture
		 */
		private void loadPicture(ImageView view, News news) {
			int userID = -1;

			String picURL = MediaSnapper.snapFromCleanedHTMLWithXPath(getString(R.string.cw_url, news.getUrl()),
					getString(R.string.xpath_get_author), getString(R.string.href));
			userID = Integer.valueOf(picURL.subSequence(getString(R.string.prefix_userpic).length(), picURL.length())
					.toString());
			viewUtility.setUserIcon(view, userID, 50);
		}

		/**
		 * @param unixtime
		 * @return
		 */
		private CharSequence createDate(long unixtime) {
			return DateUtility.createDate(unixtime, getString(R.string.dateformat_detailed));
		}
	}

	@Override
	public void onBackPressed() {
		if (getParent() instanceof NewsActivityGroup) {
			((NewsActivityGroup) getParent()).back();
		}
	}
}
