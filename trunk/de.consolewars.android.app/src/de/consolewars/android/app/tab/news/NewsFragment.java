package de.consolewars.android.app.tab.news;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import de.consolewars.android.app.CwApplication;
import de.consolewars.android.app.CwEntityManager.EntityRefinement;
import de.consolewars.android.app.CwManager.CommentArea;
import de.consolewars.android.app.Filter;
import de.consolewars.android.app.R;
import de.consolewars.android.app.db.domain.CwComment;
import de.consolewars.android.app.db.domain.CwNews;
import de.consolewars.android.app.tab.CwAbstractFragment;
import de.consolewars.android.app.tab.CwAbstractFragmentActivity;
import de.consolewars.android.app.tab.CwNavigationMainTabActivity;
import de.consolewars.android.app.tab.OnSubjectSelectedListener;
import de.consolewars.android.app.util.DateUtility;
import de.consolewars.android.app.util.StyleSpannableStringBuilder;
import de.consolewars.android.app.view.ActionBar;
import de.consolewars.android.app.view.ActionBar.Action;
import de.consolewars.android.app.view.ActionItem;
import de.consolewars.android.app.view.IScrollListener;
import de.consolewars.android.app.view.PullRefreshContainerView;
import de.consolewars.android.app.view.PullRefreshContainerView.MyTable;
import de.consolewars.android.app.view.PullRefreshContainerView.OnChangeStateListener;
import de.consolewars.android.app.view.QuickAction;
import de.consolewars.android.app.view.ScrollDetectorScrollView;

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
 * @author Alexander Dridiger
 */
public final class NewsFragment extends CwAbstractFragment {

	private OnSubjectSelectedListener listener;

	private LayoutInflater inflater;
	private ViewGroup news_layout;
	private PullRefreshContainerView pullRefreshContainer;
	private MyTable newsTable;
	private ScrollDetectorScrollView scroll;
	private TextView mRefreshHeader;

	// remember last selected table row to draw the background for that row
	private View selectedRow;
	private CwNews clickedNews;

	private BuildNewsTask task;

	private List<CwNews> tempList = new ArrayList<CwNews>();
	private int oldestNewsID = -1;

	// text styling
	private StyleSpannableStringBuilder styleStringBuilder;

	/**
	 * Mandatory constructor for creating a {@link Fragment}
	 */
	public NewsFragment() {
	}

	public NewsFragment(Filter filter, String title, int position) {
		super(title, position);
		setHasOptionsMenu(true);
		task = new BuildNewsTask();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			listener = (OnSubjectSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnSubjectSelectedListener");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
		// mContent = savedInstanceState.getString(KEY_CONTENT);
		// }
		this.inflater = inflater;
		news_layout = (ViewGroup) inflater.inflate(R.layout.news_fragment_layout, null);
		pullRefreshContainer = (PullRefreshContainerView) news_layout.findViewById(R.id.container);
		scroll = pullRefreshContainer.getList();
		newsTable = (MyTable) scroll.getChildAt(0);

		mRefreshHeader = new TextView(getActivity());
		mRefreshHeader.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		mRefreshHeader.setGravity(Gravity.CENTER);
		mRefreshHeader.setText(getString(R.string.pull_to_load));

		pullRefreshContainer.setRefreshHeader(mRefreshHeader);

		pullRefreshContainer.setOnChangeStateListener(new OnChangeStateListener() {
			@Override
			public void onChangeState(PullRefreshContainerView container, int state) {
				switch (state) {
				case PullRefreshContainerView.STATE_IDLE:
				case PullRefreshContainerView.STATE_PULL:
					mRefreshHeader.setText(getString(R.string.pull_to_load));
					break;
				case PullRefreshContainerView.STATE_RELEASE:
					mRefreshHeader.setText(getString(R.string.release_to_load));
					break;
				case PullRefreshContainerView.STATE_LOADING:
					mRefreshHeader.setText(getString(R.string.loading, getString(R.string.news)));
					new NewsNewestTask().execute();
					break;
				}
			}
		});

		return news_layout;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (((CwAbstractFragmentActivity) getActivity()).lastPosition == getPosition()) {
			initActionBar();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (((CwAbstractFragmentActivity) getActivity()).lastPosition == getPosition()) {
			initActionBar();
		}
		if (!isDetached() && newsTable.getChildCount() == 0) {
			if (task != null && task.getStatus().equals(AsyncTask.Status.PENDING)) {
				task.execute(true);
			} else if (task == null || task.getStatus().equals(AsyncTask.Status.FINISHED)) {
				task = new BuildNewsTask();
				task.execute();
			}
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
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// outState.putString(KEY_CONTENT, mContent);
	}

	private void initActionBar() {
		if (getActivity() != null) {
			if (getActivity().getParent() instanceof CwNavigationMainTabActivity) {
				ActionBar actionBar = getActionBar();
				actionBar.removeAllActions();
				setHomeAction();
				actionBar.setTitle(getActivity().getString(R.string.news_area));
				actionBar.setDisplayHomeAsUpEnabled(true);
				actionBar.addAction(new Action() {
					@Override
					public void performAction(View view) {
						if (task.getStatus().equals(AsyncTask.Status.RUNNING)) {
							task.cancel(true);
						}
						task = new BuildNewsTask();
						task.execute(true);
					}

					@Override
					public int getDrawable() {
						return R.drawable.download_bttn;
					}
				});
				actionBar.addAction(new Action() {
					@Override
					public void performAction(View view) {
						if (task != null) {
							task.cancel(true);
						}
						newsTable.removeAllViews();
						oldestNewsID = -1;
						task = new BuildNewsTask();
						task.execute();
					}

					@Override
					public int getDrawable() {
						return R.drawable.refresh_bttn;
					}
				});
			}
		}
	}

	/**
	 * Asynchronous task to receive news from the API and build up the ui.
	 * 
	 * @author Alexander Dridiger
	 */
	private class BuildNewsTask extends AsyncTask<Boolean, View, Void> {

		boolean doesWork = false;

		@Override
		protected void onPreExecute() {
			doesWork = true;
			// first set progressbar
			ViewGroup progress_layout = CwApplication.cwViewUtil().getCenteredProgressBarLayout(inflater,
					R.string.tab_news_head);
			newsTable.addView(progress_layout);
			scroll.removeScrollListener();
			getActionBar().setProgressBarVisibility(View.VISIBLE);
		}

		@Override
		protected Void doInBackground(Boolean... params) {
			if (params.length > 0 && params[0]) {
				CwApplication.cwEntityManager().getNewsNext(EntityRefinement.MIXED);
			}
			refreshList();
			createNewsRows();
			return null;
		}

		@Override
		protected void onProgressUpdate(View... rows) {
			if (!isCancelled()) {
				newsTable.addView(rows[0], newsTable.getChildCount() - 1);
				oldestNewsID = rows[0].getId();
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			newsTable.removeViewAt(newsTable.getChildCount() - 1);
			initScroll();
			if (newsTable.getChildCount() == 0) {
				oldestNewsID = -1;
			}
			doesWork = false;
			news_layout.invalidate();
			getActionBar().setProgressBarVisibility(View.GONE);
		}

		/**
		 * Create rows displaying single news to be displayed in a table.
		 */
		private void createNewsRows() {
			// create table based on current news
			ViewGroup separator = null;
			TextView separatorTxt = null;
			for (int i = 0; i < tempList.size(); i++) {
				final CwNews newsToAdd = tempList.get(i);
				if (!isCancelled() && (oldestNewsID == -1 || newsToAdd.getSubjectId() < oldestNewsID)) {
					// check if the news has to be filtered
					if (getFilter(getPosition()).equals(Filter.NEWS_ALL) || matchesFilter(newsToAdd.getCategoryShort())) {
						// get the table row by an inflater and set the needed information
						final View tableRow = inflater.inflate(R.layout.news_row_layout, newsTable, false);
						tableRow.setId(newsToAdd.getSubjectId());
						tableRow.setTag(newsToAdd);
						final QuickAction quickAction = initQuickAction();
						// setup the action item click listener
						quickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
							@Override
							public void onItemClick(int pos) {
								if (pos == 0) {
									new SyncNewsTask().execute(clickedNews);
									// } else if (pos == 1) {
									// Toast.makeText(getActivity(), "Noch nicht möglich", Toast.LENGTH_SHORT).show();
								} else if (pos == 1) {
									new SaveNewsTask().execute(clickedNews);
								}
							}
						});
						tableRow.setOnTouchListener(new OnTouchListener() {
							@Override
							public boolean onTouch(View v, MotionEvent event) {
								Log.i("MOTIONEVENT", String.valueOf(event.getAction()));
								switch (event.getAction()) {
								case MotionEvent.ACTION_DOWN:
									if (selectedRow != null) {
										selectedRow.setBackgroundDrawable(getActivity().getResources().getDrawable(
												R.drawable.table_cell_bg));
									}
									v.setBackgroundDrawable(getActivity().getResources().getDrawable(
											R.drawable.table_cell_bg_selected));
									return true;
								case MotionEvent.ACTION_CANCEL:
									v.setBackgroundDrawable(getActivity().getResources().getDrawable(
											R.drawable.table_cell_bg));
								case MotionEvent.ACTION_MOVE:
									if (Math.abs(event.getX() - event.getX(0)) > 2.0f
											|| Math.abs(event.getY() - event.getY(0)) > 2.0f) {
										v.setBackgroundDrawable(getActivity().getResources().getDrawable(
												R.drawable.table_cell_bg));
										return true;
									}
									v.setBackgroundDrawable(getActivity().getResources().getDrawable(
											R.drawable.table_cell_bg));
									return true;
								case MotionEvent.ACTION_UP:
									if (event.getEventTime() - event.getDownTime() < 500) {
										tableRow.setBackgroundDrawable(getActivity().getResources().getDrawable(
												R.drawable.table_cell_bg_selected));
										selectedRow = tableRow;
										listener.onSubjectSelected(newsToAdd);
										return true;
									}
									if (Math.abs(event.getDownTime() - event.getEventTime()) > 1000) {
										clickedNews = newsToAdd;
										quickAction.show(tableRow);
										v.setBackgroundDrawable(getActivity().getResources().getDrawable(
												R.drawable.table_cell_bg));
										return true;
									}
									v.setBackgroundDrawable(getActivity().getResources().getDrawable(
											R.drawable.table_cell_bg));
								}
								return true;
							}
						});
						CwApplication.cwImageLoader().displayImage(
								getActivity().getString(R.string.catpic_url, newsToAdd.getCategoryShort()),
								getActivity(), (ImageView) tableRow.findViewById(R.id.news_row_category_icon), false,
								R.drawable.cat_stub);
						((TextView) tableRow.findViewById(R.id.news_row_category)).setText(newsToAdd.getCategory());
						((TextView) tableRow.findViewById(R.id.news_row_title)).setText(createTitle(newsToAdd
								.getTitle()));
						((TextView) tableRow.findViewById(R.id.news_row_descr)).setText(newsToAdd.getDescription());
						((TextView) tableRow.findViewById(R.id.news_row_date)).setText(createDate(
								newsToAdd.getUnixtime() * 1000L, "'um' HH:mm'Uhr'"));
						TextView cmtAmount = (TextView) tableRow.findViewById(R.id.news_row_cmmts_amount);
						cmtAmount.setText(createAmount(newsToAdd.getCommentsAmount()));
						TextView author = (TextView) tableRow.findViewById(R.id.news_row_author);
						author.setText(createAuthor(newsToAdd.getAuthor()));
						author.setSelected(true);
						if (i == 0
								|| DateUtility
										.getDay(DateUtility.createCalendarFromUnixtime(tempList.get(i - 1)
												.getUnixtime() * 1000L), 0).getTimeInMillis() > newsToAdd.getUnixtime() * 1000L) {
							// current news was not created on the same date as the last news --> separator necessary
							separator = (ViewGroup) inflater.inflate(R.layout.news_row_day_separator_layout, null);
							separatorTxt = (TextView) separator.findViewById(R.id.news_row_day_separator_text);
							separatorTxt.setText(createDate(
									DateUtility.getDay(
											DateUtility.createCalendarFromUnixtime(newsToAdd.getUnixtime() * 1000L), 0)
											.getTimeInMillis(), "EEEE, dd. MMMMM yyyy"));
							publishProgress(separator);
						}
						publishProgress(tableRow);
					}
				}
			}
		}

		private void refreshList() {
			if (!getFilter(getPosition()).equals(Filter.NEWS_ALL)) {
				tempList.clear();
				for (CwNews news : CwApplication.cwEntityManager().getCachedNews()) {
					if (matchesFilter(news.getCategoryShort())) {
						tempList.add(news);
					}
				}
			} else {
				tempList = CwApplication.cwEntityManager().getCachedNews();
			}
		}

		private boolean matchesFilter(String cat) {
			boolean matches = false;
			if (getFilter(getPosition()).equals(Filter.NEWS_MS) && !cat.contains("ps") && !cat.contains("vita")
					&& !cat.contains("son") && !StringUtils.contains(cat, 'w') && !cat.contains("nin")
					&& !cat.contains("ds") && !cat.contains("n64") && !cat.contains("ngc") && !cat.contains("gb")
					&& !cat.contains("snes")) {
				matches = true;
			} else if (getFilter(getPosition()).equals(Filter.NEWS_NIN) && !cat.contains("ps") && !cat.contains("vita")
					&& !cat.contains("son") && !StringUtils.contains(cat, 'x') && !cat.contains("ms")
					&& !cat.contains("360")) {
				matches = true;
			} else if (getFilter(getPosition()).equals(Filter.NEWS_SONY) && !StringUtils.contains(cat, 'w')
					&& !cat.contains("nin") && !cat.contains("ds") && !cat.contains("n64") && !cat.contains("ngc")
					&& !cat.contains("gb") && !cat.contains("snes") && !StringUtils.contains(cat, 'x')
					&& !cat.contains("ms") && !cat.contains("360")) {
				matches = true;
			}
			return matches;
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

		private void initScroll() {
			scroll.setOnScrollListener(new IScrollListener() {
				@Override
				public void onScrollChanged(ScrollDetectorScrollView scrollView, int x, int y, int oldx, int oldy) {
					if (!doesWork) {
						// Grab the last child placed in the ScrollView, we need it to determinate the bottom position.
						View view = (View) scrollView.getChildAt(scrollView.getChildCount() - 1);

						// Calculate the scrolldiff
						int diff = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));

						// if diff is zero, then the bottom has been reached
						if (scrollView.getChildCount() == 0 || diff == 0) {
							task = new BuildNewsTask();
							task.execute(true);
						}
					}
				}
			});
		}

		/**
		 * Creates the string for the ui cell showing the author of a news and the amount of comments.
		 * 
		 * @param commentAmount
		 * @param author
		 * @return a formatted {@link CharSequence}
		 */
		private CharSequence createAuthor(String author) {
			// TODO more text formatting
			// an empty author string means that the news was not written by a
			if (author.matches("")) {
				author = getActivity().getString(R.string.news_author_unknown);
			}
			styleStringBuilder = new StyleSpannableStringBuilder();
			styleStringBuilder.appendWithStyle(new ForegroundColorSpan(0xFF007711),
					getActivity().getString(R.string.news_author_by));
			styleStringBuilder.append(" ");
			styleStringBuilder.appendWithStyle(new ForegroundColorSpan(0xFF009933), author);

			return styleStringBuilder;
		}

		/**
		 * Creates the string for the ui cell showing the author of a news and the amount of comments.
		 * 
		 * @param commentAmount
		 * @param author
		 * @return a formatted {@link CharSequence}
		 */
		private CharSequence createAmount(int commentAmount) {
			// TODO more text formatting
			// an empty author string means that the news was not written by a
			styleStringBuilder = new StyleSpannableStringBuilder();
			styleStringBuilder.appendWithStyle(new ForegroundColorSpan(0xFF7e6003), String.valueOf(commentAmount));

			return styleStringBuilder;
		}

		/**
		 * @param unixtime
		 * @return
		 */
		private CharSequence createDate(long unixtime, String format) {
			return DateUtility.createDate(unixtime, format);
		}

		/**
		 * @param title
		 * @return
		 */
		private CharSequence createTitle(String title) {
			// TODO text formatting
			return title;
		}
	}

	/**
	 * Asynchronous task to bump up the newest news.
	 * 
	 * @author Alexander Dridiger
	 */
	private class NewsNewestTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			getActionBar().setProgressBarVisibility(View.VISIBLE);
		}

		@Override
		protected Void doInBackground(Void... newsParams) {
			if (!isCancelled()) {
				CwApplication.cwEntityManager().getNewsNewest();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			getActionBar().setProgressBarVisibility(View.GONE);
			if (task != null) {
				task.cancel(true);
			}
			pullRefreshContainer.completeRefresh();
			newsTable.removeAllViews();
			oldestNewsID = -1;
			task = new BuildNewsTask();
			task.execute();
		}
	}

	/**
	 * Asynchronous task to sync a news.
	 * 
	 * @author Alexander Dridiger
	 */
	private class SyncNewsTask extends AsyncTask<CwNews, Void, Void> {

		@Override
		protected void onPreExecute() {
			getActionBar().setProgressBarVisibility(View.VISIBLE);
			Toast.makeText(getActivity(), getString(R.string.news_syncing), Toast.LENGTH_SHORT).show();
		}

		@Override
		protected Void doInBackground(CwNews... newsParams) {
			CwNews news = newsParams[0];
			if (news.getArticle() == null) {
				news = CwApplication.cwEntityManager().getNewsSingle(news.getSubjectId());
			}
			CwApplication.cwEntityManager().getFullNews(news,
					getActivity().getString(R.string.cw_url_append, news.getUrl()));
			news.setComments(CwApplication.cwEntityManager()
					.getComments(news.getSubjectId(), CommentArea.AREA_NEWS, news.getCommentsAmount(), 0).getComments());
			CwApplication.cwEntityManager().replaceOrSetNews(news);
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

	/**
	 * Asynchronous task to save news.
	 * 
	 * @author Alexander Dridiger
	 */
	private class SaveAllNewsTask extends AsyncTask<Boolean, Void, Integer> {

		@Override
		protected void onPreExecute() {
			getActionBar().setProgressBarVisibility(View.VISIBLE);
			Toast.makeText(getActivity(), getActivity().getString(R.string.news_saving), Toast.LENGTH_SHORT).show();
		}

		@Override
		protected Integer doInBackground(Boolean... params) {
			return CwApplication.cwEntityManager().saveAllNews();
		}

		@Override
		protected void onPostExecute(Integer result) {
			Toast.makeText(getActivity(), getActivity().getString(R.string.news_saved, result), Toast.LENGTH_SHORT)
					.show();
			getActionBar().setProgressBarVisibility(View.GONE);
		}
	}

	private MenuInflater menuInflater;

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if (menuInflater == null) {
			menuInflater = inflater;
		}
		if (((CwAbstractFragmentActivity) getActivity()).lastPosition == getPosition()) {
			super.onCreateOptionsMenu(menu, inflater);
			menu.clear();
			menuInflater.inflate(R.menu.news_menu, menu);
		}
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		if (menuInflater == null) {
			menuInflater = getActivity().getMenuInflater();
		}
		if (((CwAbstractFragmentActivity) getActivity()).lastPosition == getPosition()) {
			super.onPrepareOptionsMenu(menu);
			menu.clear();
			menuInflater.inflate(R.menu.news_menu, menu);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (((CwAbstractFragmentActivity) getActivity()).lastPosition == getPosition()) {
			// Find which menu item has been selected
			switch (item.getItemId()) {
			// Check for each known menu item
			case (R.id.menu_news_discard):
				CwApplication.cwEntityManager().discardAllNews();
				break;
			case (R.id.menu_news_load_next):
				if (task.getStatus().equals(AsyncTask.Status.RUNNING)) {
					task.cancel(true);
				}
				task = new BuildNewsTask();
				task.execute(true);
				break;
			case (R.id.menu_news_load_saved):

				break;
			case (R.id.menu_news_refresh):

				break;
			case (R.id.menu_news_save_all):
				new SaveAllNewsTask().execute();
				break;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void backPressed() {
	}

	@Override
	public void refresh() {
		initActionBar();
	}

	private static Filter getFilter(int position) {
		switch (position) {
		case 0:
			return Filter.NEWS_ALL;
		case 1:
			return Filter.NEWS_MS;
		case 2:
			return Filter.NEWS_NIN;
		case 3:
			return Filter.NEWS_SONY;
		}
		return Filter.NEWS_ALL;
	}

	/**
	 * Custom GestureDetector to handle clicks.
	 * 
	 * @author Alexander Dridiger
	 */
	class SwipeGestureDetector extends SimpleOnGestureListener {

		@Override
		public void onLongPress(MotionEvent e) {
			// TODO Auto-generated method stub
			super.onLongPress(e);
		}
	}
}
