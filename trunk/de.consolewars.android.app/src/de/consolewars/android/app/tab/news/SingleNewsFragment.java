package de.consolewars.android.app.tab.news;

import java.util.IllegalFormatException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import de.consolewars.android.app.CwApplication;
import de.consolewars.android.app.R;
import de.consolewars.android.app.db.domain.CwNews;
import de.consolewars.android.app.tab.CwAbstractFragment;
import de.consolewars.android.app.util.DateUtility;
import de.consolewars.android.app.util.MediaSnapper;
import de.consolewars.android.app.util.TextViewHandler;

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
public class SingleNewsFragment extends CwAbstractFragment {

	private Activity context;

	private LayoutInflater inflater;

	private ViewGroup singlenews_layout;
	private ViewGroup content;
	private ViewGroup singlenews_fragment;
	private ViewGroup progress_layout;

	private CwNews news;

	private BuildSingleNewsAsyncTask task;

	private String vidURL = "";

	public SingleNewsFragment() {

	}

	public SingleNewsFragment(String title) {
		super(title);
		setHasOptionsMenu(true);
		task = new BuildSingleNewsAsyncTask();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// see this issue http://code.google.com/p/android/issues/detail?id=5067
		context = getActivity();
		this.inflater = LayoutInflater.from(context);
		singlenews_layout = (ViewGroup) inflater.inflate(R.layout.fragment_progress_layout, null);
		singlenews_layout.setBackgroundColor(context.getResources().getColor(R.color.singlenews_bg));
		progress_layout = CwApplication.cwViewUtil().getCenteredProgressBarLayout(inflater, R.string.singlenews);
		ViewGroup progress = (ViewGroup) singlenews_layout.findViewById(R.id.progressbar);
		progress.addView(progress_layout);
		progress_layout.setVisibility(View.GONE);
		return singlenews_layout;
	}

	@Override
	public void onPause() {
		super.onPause();
		if (getActivity().findViewById(R.id.singlenews_video) != null) {
			WebView localWebView = (WebView) getActivity().findViewById(R.id.singlenews_video);
			localWebView.loadUrl("about:blank");
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		news = CwApplication.cwEntityManager().getSelectedNews();
		singlenews_layout = (ViewGroup) getView();
		content = (ViewGroup) singlenews_layout.findViewById(R.id.content);
		content.removeAllViews();
		singlenews_fragment = (ViewGroup) inflater.inflate(R.layout.singlenews_fragment_layout, null);
		progress_layout.setVisibility(View.GONE);

		if (task.getStatus().equals(AsyncTask.Status.PENDING)) {
			task.execute();
		} else if (task.getStatus().equals(AsyncTask.Status.FINISHED)) {
			task = new BuildSingleNewsAsyncTask();
			task.execute();
		} else if (task.getStatus().equals(AsyncTask.Status.RUNNING)) {
			task.cancel(true);
		}
	}

	@Override
	public void backPressed() {
		if (task.getStatus().equals(AsyncTask.Status.RUNNING)) {
			task.cancel(true);
		}
	}

	/**
	 * Asynchronous task to receive a single news from the API and build up the ui.
	 * 
	 * @author Alexander Dridiger
	 */
	private class BuildSingleNewsAsyncTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected void onPreExecute() {
			progress_layout.setVisibility(View.VISIBLE);
			singlenews_fragment.setVisibility(View.GONE);
		}

		@Override
		protected Void doInBackground(Void... params) {
			if (!isCancelled()) {
				createNewsView();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void param) {
			progress_layout.setVisibility(View.GONE);
			singlenews_fragment.setVisibility(View.VISIBLE);
			content.removeView(singlenews_fragment);
			content.addView(singlenews_fragment);
		}

		private void createNewsView() {
			if (!isCancelled()) {
				if (news.getArticle() == null) {
					CwApplication.cwEntityManager().setSelectedNews(
							CwApplication.cwEntityManager().getSingleNews(news.getSubjectId(), true));
					news = CwApplication.cwEntityManager().getSelectedNews();
				}
				TextView text = (TextView) singlenews_fragment.findViewById(R.id.singlenews_newstext);
				CwApplication.cwViewUtil().setClickableTextView(text);
				if (news != null && news.getArticle() != null) {
					try {
						text.setText(Html.fromHtml(news.getArticle(), new TextViewHandler(context), null));
					} catch (IllegalFormatException ife) {
						// FIXME Wrong format handling
						text.setText(news.getArticle());
					}
					createHeader(singlenews_fragment, news);
					vidURL = MediaSnapper.snapFromCleanedHTMLWithXPath(
							context.getString(R.string.cw_url_append, news.getUrl()),
							context.getString(R.string.xpath_get_video), context.getString(R.string.value));
					initVideos(news.getUrl());
				} else if (news == null || news.getArticle() == null) {
					text.setText(context.getString(R.string.failure));
				}
			} else {
				cancel(true);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			singlenews_layout = (ViewGroup) getView();
			content = (ViewGroup) singlenews_layout.findViewById(R.id.content);
			content.removeAllViews();
			singlenews_fragment = (ViewGroup) inflater.inflate(R.layout.singlenews_fragment_layout, null);
			progress_layout.setVisibility(View.GONE);
			task = new BuildSingleNewsAsyncTask();
			task.execute();
		}

		/**
		 * Sets a video, if one exists in a news. Can't be invoked without calling Looper.prepare(). Don't use this
		 * within an AsyncTask!
		 * 
		 * @param url
		 */
		private void initVideos(String url) {
			if (vidURL != "") {
				WebView localWebView = (WebView) singlenews_fragment.findViewById(R.id.singlenews_video);

				localWebView.getSettings().setUseWideViewPort(false);
				localWebView.getSettings().setPluginState(PluginState.ON);
				localWebView.getSettings().setPluginsEnabled(true);
				localWebView.getSettings().setJavaScriptEnabled(true);
				localWebView.getSettings().setBuiltInZoomControls(false);

				Log.i("******YOUTUBE*******", context.getString(R.string.youtube_embedding, 300, 200, vidURL));

				localWebView.loadDataWithBaseURL(context.getString(R.string.cw_url_slash),
						context.getString(R.string.youtube_embedding, 300, 200, vidURL), "text/html", "utf-8", null);
			}
		}

		private void createHeader(View parent, CwNews news) {
			ImageView icon = (ImageView) parent.findViewById(R.id.header_icon);
			CwApplication.cwImageLoader().displayImage(context.getString(R.string.catpic_url, news.getCategoryShort()),
					context, (ImageView) icon, false, R.drawable.cat_stub);

			ImageView usericon = (ImageView) parent.findViewById(R.id.header_descr_usericon);
			loadPicture(usericon, news);

			TextView cattxt = (TextView) parent.findViewById(R.id.header_title);
			cattxt.setText(news.getCategory());
			TextView title = (TextView) parent.findViewById(R.id.header_descr_title);
			title.setText(news.getTitle());
			TextView info = (TextView) parent.findViewById(R.id.header_descr_info);
			info.setText(context.getString(R.string.singlenews_info, createDate(news.getUnixtime() * 1000L),
					news.getAuthor()));
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
		private void loadPicture(ImageView view, CwNews news) {
			int userID = -1;

			String picURL = MediaSnapper.snapFromCleanedHTMLWithXPath(
					context.getString(R.string.cw_url_append, news.getUrl()),
					context.getString(R.string.xpath_get_author), context.getString(R.string.href));
			if (!picURL.matches("")) {
				userID = Integer.valueOf(picURL.subSequence(context.getString(R.string.prefix_userpic).length(),
						picURL.length()).toString());

				CwApplication.cwImageLoader().displayImage(context.getString(R.string.userpic_url, userID, 50),
						context, (ImageView) view, false, R.drawable.user_stub);
			}
		}

		/**
		 * @param unixtime
		 * @return
		 */
		private CharSequence createDate(long unixtime) {
			return DateUtility.createDate(unixtime, context.getString(R.string.dateformat_detailed));
		}
	}

	private MenuInflater menuInflater;

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if (isSelected()) {
			super.onCreateOptionsMenu(menu, inflater);
			menu.clear();
			inflater.inflate(R.menu.singlenews_menu, menu);
		}
		menuInflater = inflater;
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		if (isSelected()) {
			super.onPrepareOptionsMenu(menu);
			menu.clear();
			menuInflater.inflate(R.menu.singlenews_menu, menu);
		}
	}
}
