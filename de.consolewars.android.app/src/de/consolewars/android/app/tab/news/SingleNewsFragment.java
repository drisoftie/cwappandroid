package de.consolewars.android.app.tab.news;

import java.util.IllegalFormatException;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import de.consolewars.android.app.CwApplication;
import de.consolewars.android.app.R;
import de.consolewars.android.app.db.domain.CwNews;
import de.consolewars.android.app.tab.cmts.CommentsFragment;
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
public class SingleNewsFragment extends Fragment {

	private Context context;

	private LayoutInflater inflater;

	private ViewGroup singlenews_layout;
	private ViewGroup singlenews_fragment_layout;
	private ViewGroup progress_layout;

	private CwNews news;

	private String vidURL = "";

	public SingleNewsFragment() {
		super();
	}

	public SingleNewsFragment(CwNews news) {
		super();
		setHasOptionsMenu(true);
		this.news = news;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		context = getActivity().getApplicationContext();
		this.inflater = LayoutInflater.from(context);
		singlenews_layout = (ViewGroup) inflater.inflate(R.layout.singlenews_layout, null);
		singlenews_fragment_layout = (ViewGroup) inflater.inflate(R.layout.singlenews_fragment_layout, null);
		progress_layout = CwApplication.cwViewUtil().getCenteredProgressBarLayout(inflater, R.string.singlenews);
		ViewGroup progress = (ViewGroup) singlenews_layout.findViewById(R.id.singlenews_progressbar);
		progress.addView(progress_layout);
		progress_layout.setVisibility(View.GONE);
		new BuildSingleNewsAsyncTask().execute();
		return singlenews_layout;
	}

	@Override
	public void onPause() {
		if (singlenews_fragment_layout != null
				&& singlenews_fragment_layout.findViewById(R.id.singlenews_video) != null) {
			WebView localWebView = (WebView) singlenews_fragment_layout.findViewById(R.id.singlenews_video);
			localWebView.loadUrl("about:blank");
		}
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	/**
	 * Asynchronous task to receive a single news from the API and build up the ui.
	 * 
	 * @author Alexander Dridiger
	 */
	private class BuildSingleNewsAsyncTask extends AsyncTask<Void, Integer, View> {

		@Override
		protected void onPreExecute() {
			progress_layout.setVisibility(View.VISIBLE);
		}

		@Override
		protected View doInBackground(Void... params) {
			return createNewsView();
		}

		@Override
		protected void onPostExecute(View result) {
			progress_layout.setVisibility(View.GONE);
			ViewGroup content = (ViewGroup) singlenews_layout.findViewById(R.id.singlenews_content);
			content.addView(result);
		}

		private View createNewsView() {
			if (news.getArticle() == null) {
				news = CwApplication.cwManager().getSingleCwNews(news.getSubjectId(), true);
			}
			TextView text = (TextView) singlenews_fragment_layout.findViewById(R.id.singlenews_newstext);
			CwApplication.cwViewUtil().setClickableTextView(text);
			if (news != null && news.getArticle() != null) {
				try {
					text.setText(Html.fromHtml(news.getArticle(), new TextViewHandler(context), null));
				} catch (IllegalFormatException ife) {
					// FIXME Wrong format handling
					text.setText(news.getArticle());
				}
				createHeader(singlenews_fragment_layout, news);
				createCommentBttn();
				vidURL = MediaSnapper.snapFromCleanedHTMLWithXPath(
						context.getString(R.string.cw_url_append, news.getUrl()),
						context.getString(R.string.xpath_get_video), context.getString(R.string.value));
				initVideos(news.getUrl());
			} else if (news == null || news.getArticle() == null) {
				text.setText(context.getString(R.string.failure));
			}
			return singlenews_fragment_layout;
		}

		private void createCommentBttn() {
			Button bttn = (Button) singlenews_fragment_layout.findViewById(R.id.singlenews_comments_bttn);
			bttn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// Create new fragment and transaction
					Fragment commentsFragment = new CommentsFragment(news);
					FragmentTransaction transaction = getFragmentManager().beginTransaction();

					transaction.add(R.id.news_root, commentsFragment);
					transaction.addToBackStack(null);

					// Commit the transaction
					transaction.commit();
					WebView localWebView = (WebView) singlenews_fragment_layout.findViewById(R.id.singlenews_video);
					localWebView.loadUrl("about:blank");
				}
			});
		}

		/**
		 * Sets a video, if one exists in a news. Can't be invoked without calling Looper.prepare(). Don't use this
		 * within an AsyncTask!
		 * 
		 * @param url
		 */
		private void initVideos(String url) {
			if (vidURL != "") {
				WebView localWebView = (WebView) singlenews_fragment_layout.findViewById(R.id.singlenews_video);

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
			CwApplication.cwViewUtil().setCategoryIcon(icon, news.getCategoryShort());

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
				CwApplication.cwViewUtil().setUserIcon(view, userID, 50);
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

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		menu.clear();
		inflater.inflate(R.menu.singlenews_menu, menu);
	}
}
