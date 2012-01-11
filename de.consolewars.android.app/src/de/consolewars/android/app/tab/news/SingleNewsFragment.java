package de.consolewars.android.app.tab.news;

import java.util.IllegalFormatException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import de.consolewars.android.app.CwApplication;
import de.consolewars.android.app.R;
import de.consolewars.android.app.db.domain.CwComment;
import de.consolewars.android.app.db.domain.CwNews;
import de.consolewars.android.app.db.domain.CwPicture;
import de.consolewars.android.app.db.domain.CwVideo;
import de.consolewars.android.app.tab.CwAbstractFragment;
import de.consolewars.android.app.tab.CwAbstractFragmentActivity;
import de.consolewars.android.app.tab.CwNavigationMainTabActivity;
import de.consolewars.android.app.util.DateUtility;
import de.consolewars.android.app.util.TextViewHandler;
import de.consolewars.android.app.view.ActionBar;
import de.consolewars.android.app.view.ActionBar.Action;
import de.consolewars.android.app.view.ActionItem;
import de.consolewars.android.app.view.QuickAction;

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
 * A {@link CwAbstractFragment} showing and handling a single news.
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

	/**
	 * Mandatory constructor for creating a {@link Fragment}
	 */
	public SingleNewsFragment() {
	}

	public SingleNewsFragment(String title, int position) {
		super(title, position);
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
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (((CwAbstractFragmentActivity) getActivity()).lastPosition == getPosition()) {
			initActionBar();
		}
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
		refreshView();
		if (((CwAbstractFragmentActivity) getActivity()).lastPosition == getPosition()) {
			initActionBar();
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if (task != null) {
			task.cancel(true);
			task = null;
		}
	}

	@Override
	public void backPressed() {
		if (task.getStatus().equals(AsyncTask.Status.RUNNING)) {
			task.cancel(true);
		}
	}

	private void refreshView() {
		singlenews_layout = (ViewGroup) getView();
		content = (ViewGroup) singlenews_layout.findViewById(R.id.content);
		content.removeAllViews();
		singlenews_fragment = (ViewGroup) inflater.inflate(R.layout.singlenews_fragment_layout, null);
		progress_layout.setVisibility(View.GONE);

		if (task != null && task.getStatus().equals(AsyncTask.Status.PENDING)) {
			task.execute();
		} else if (task == null || task.getStatus().equals(AsyncTask.Status.FINISHED)) {
			task = new BuildSingleNewsAsyncTask();
			task.execute();
		} else if (task != null && task.getStatus().equals(AsyncTask.Status.RUNNING)) {
			task.cancel(true);
		}
	}

	private void initActionBar() {
		if (context != null) {
			if (context.getParent() instanceof CwNavigationMainTabActivity) {
				ActionBar actionBar = getActionBar();
				actionBar.removeAllActions();
				setHomeAction();
				actionBar.setTitle(context.getString(R.string.singlenews_area));
				actionBar.addAction(new Action() {
					@Override
					public void performAction(View view) {
					}

					@Override
					public int getDrawable() {
						return R.drawable.download_bttn;
					}
				});
				actionBar.addAction(new Action() {
					@Override
					public void performAction(View view) {
						refreshView();
					}

					@Override
					public int getDrawable() {
						return R.drawable.refresh_blue_bttn;
					}
				});
			}
		}
	}

	/**
	 * Asynchronous task to receive a single news and build up the ui.
	 * 
	 * @author Alexander Dridiger
	 */
	private class BuildSingleNewsAsyncTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected void onPreExecute() {
			getActionBar().setProgressBarVisibility(View.VISIBLE);
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
			getActionBar().setProgressBarVisibility(View.GONE);
		}

		private void createNewsView() {
			if (!isCancelled() && news != null) {
				if (news.getArticle() == null) {
					CwApplication.cwEntityManager().setSelectedNews(
							CwApplication.cwEntityManager().getNewsSingle(news.getSubjectId(), true));
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
					CwApplication.cwEntityManager().getFullNews(news,
							context.getString(R.string.cw_url_append, news.getUrl()));
					createHeader(singlenews_fragment, news);
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
			if (singlenews_layout != null) {
				content = (ViewGroup) singlenews_layout.findViewById(R.id.content);
				content.removeAllViews();
			}
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
			if (!news.getVideos().isEmpty()) {

				WebView localWebView = (WebView) singlenews_fragment.findViewById(R.id.singlenews_video);
				localWebView.getSettings().setUseWideViewPort(false);
				localWebView.getSettings().setPluginState(PluginState.ON);
				localWebView.getSettings().setPluginsEnabled(true);
				localWebView.getSettings().setJavaScriptEnabled(true);
				localWebView.getSettings().setBuiltInZoomControls(false);
				for (CwVideo video : news.getVideos()) {
					Log.i("******YOUTUBE*******", video.getHtmlEmbeddedSnippet());
					localWebView.loadDataWithBaseURL(context.getString(R.string.cw_url_slash),
							video.getHtmlEmbeddedSnippet(), "text/html", "utf-8", null);
					break;
				}

				TextView moreVids = (TextView) singlenews_fragment.findViewById(R.id.singlenews_morevids);
				StringBuffer moreVidsText = new StringBuffer();
				moreVidsText.append("alle Videos:<br>");
				for (CwVideo video : news.getVideos()) {
					moreVidsText.append("<a href=\"").append(video.getUrl()).append("\">").append(video.getUrl())
							.append("</a><br>");
				}
				try {
					moreVids.setText(Html.fromHtml(moreVidsText.toString(), new TextViewHandler(context), null));
				} catch (IllegalFormatException ife) {
					moreVids.setText(moreVidsText.toString());
				}
				CwApplication.cwViewUtil().setClickableTextView(moreVids);
			}

		}

		private void createHeader(View parent, CwNews news) {
			ImageView icon = (ImageView) parent.findViewById(R.id.header_icon);
			CwApplication.cwImageLoader().displayImage(context.getString(R.string.catpic_url, news.getCategoryShort()),
					context, (ImageView) icon, false, R.drawable.cat_stub);

			final ImageView usericon = (ImageView) parent.findViewById(R.id.header_descr_usericon);
			loadPicture(usericon, news);

			// final QuickAction action = initQuickAction();
			// action.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
			// @Override
			// public void onItemClick(int pos) {
			// if (pos == 0) {
			// } else if (pos == 1) {
			// }
			// }
			// });
			// usericon.setOnClickListener(new OnClickListener() {
			// @Override
			// public void onClick(View v) {
			// action.show(usericon);
			//
			// }
			// });

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
			CwApplication.cwImageLoader().displayImage(context.getString(R.string.userpic_url, news.getAuthorId(), 50),
					context, (ImageView) view, false, R.drawable.user_stub);
		}

		/**
		 * @param unixtime
		 * @return
		 */
		private CharSequence createDate(long unixtime) {
			return DateUtility.createDate(unixtime, context.getString(R.string.dateformat_detailed));
		}

		private QuickAction initQuickAction() {
			final QuickAction mQuickAction = new QuickAction(getActivity());
			mQuickAction.addActionItem(createAction(R.string.news_sync, R.drawable.ic_add));
			// mQuickAction.addActionItem(createAction(R.string.news_fav, R.drawable.ic_accept));
			mQuickAction.addActionItem(createAction(R.string.news_save, R.drawable.ic_up));
			return mQuickAction;
		}

		private ActionItem createAction(int titleId, int iconId) {
			ActionItem addAction = new ActionItem();
			addAction.setTitle(getString(titleId));
			addAction.setIcon(getResources().getDrawable(iconId));
			return addAction;
		}
	}

	private class RefreshSingleNewsTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			getActionBar().setProgressBarVisibility(View.VISIBLE);
			Toast.makeText(getActivity(), getString(R.string.news_syncing), Toast.LENGTH_SHORT).show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			CwApplication.cwEntityManager().getFullNews(news, context.getString(R.string.cw_url_append, news.getUrl()));
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			Toast.makeText(getActivity(), getString(R.string.news_synced), Toast.LENGTH_SHORT).show();
			getActionBar().setProgressBarVisibility(View.GONE);
		}
	}

	/**
	 * Asynchronous task to save a news.
	 * 
	 * @author Alexander Dridiger
	 */
	private class SaveNewsTask extends AsyncTask<CwNews, Void, Void> {

		@Override
		protected void onPreExecute() {
			getActionBar().setProgressBarVisibility(View.VISIBLE);
			Toast.makeText(getActivity(), getString(R.string.news_single_saving), Toast.LENGTH_SHORT).show();
		}

		@Override
		protected Void doInBackground(CwNews... newsParams) {
			CwNews news = newsParams[0];
			for (CwComment comment : news.getComments()) {
				comment.setCwNews(news);
			}
			for (CwPicture pic : news.getPictures()) {
				pic.setCwNews(news);
			}
			for (CwVideo video : news.getVideos()) {
				video.setCwNews(news);
			}
			CwApplication.cwEntityManager().replaceOrSetNews(news);
			CwApplication.cwEntityManager().saveLoadNews(news);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			Toast.makeText(getActivity(), getString(R.string.news_single_saved), Toast.LENGTH_SHORT).show();
			getActionBar().setProgressBarVisibility(View.GONE);
		}
	}

	private MenuInflater menuInflater;

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if (((CwAbstractFragmentActivity) getActivity()).lastPosition == getPosition()) {
			super.onCreateOptionsMenu(menu, inflater);
			menu.clear();
			inflater.inflate(R.menu.singlenews_menu, menu);
		}
		menuInflater = inflater;
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		if (((CwAbstractFragmentActivity) getActivity()).lastPosition == getPosition()) {
			super.onPrepareOptionsMenu(menu);
			menu.clear();
			menuInflater.inflate(R.menu.singlenews_menu, menu);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (((CwAbstractFragmentActivity) getActivity()).lastPosition == getPosition()) {
			// Find which menu item has been selected
			switch (item.getItemId()) {
			// Check for each known menu item
			case (R.id.menu_singlenews_refresh):
				new RefreshSingleNewsTask().execute();
				break;
			case (R.id.menu_singlenews_save):
				new SaveNewsTask().execute(news);
				break;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void refresh() {
		initActionBar();
	}
}
