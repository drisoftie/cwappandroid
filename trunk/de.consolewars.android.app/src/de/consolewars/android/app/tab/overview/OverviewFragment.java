package de.consolewars.android.app.tab.overview;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import de.consolewars.android.app.CwApplication;
import de.consolewars.android.app.CwEntityManager;
import de.consolewars.android.app.CwLoginManager;
import de.consolewars.android.app.CwManager;
import de.consolewars.android.app.Filter;
import de.consolewars.android.app.R;
import de.consolewars.android.app.tab.CwAbstractFragment;
import de.consolewars.android.app.tab.CwNavigationMainTabActivity;
import de.consolewars.android.app.util.ViewUtility;
import de.consolewars.android.app.view.ActionBar;
import de.consolewars.api.data.Message;

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
 * Activity handling the overview.
 * 
 * @author Alexander Dridiger
 */
public class OverviewFragment extends CwAbstractFragment {

	private Activity context;

	CwEntityManager cwEntityManager = CwApplication.cwEntityManager();
	CwManager cwManager = CwApplication.cwManager();
	CwLoginManager cwLoginManager = CwApplication.cwLoginManager();
	ViewUtility viewUtility = CwApplication.cwViewUtil();

	private LayoutInflater inflater;

	private ViewGroup overview_layout;
	private ViewGroup content;
	private ViewGroup overview_fragment;
	private ViewGroup progress_layout;

	private BuildOverviewAsyncTask task;

	/**
	 * Mandatory constructor for creating a {@link Fragment}
	 */
	public OverviewFragment() {
	}

	public OverviewFragment(String title) {
		super(title);
		// setHasOptionsMenu(true);
		task = new BuildOverviewAsyncTask();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		context = getActivity();
		this.inflater = LayoutInflater.from(context);

		overview_layout = (ViewGroup) inflater.inflate(R.layout.fragment_progress_layout, null);

		progress_layout = viewUtility.getCenteredProgressBarLayout(inflater, R.string.overview);
		ViewGroup progress = (ViewGroup) overview_layout.findViewById(R.id.progressbar);
		progress.addView(progress_layout);
		progress_layout.setVisibility(View.GONE);
		return overview_layout;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		overview_layout = (ViewGroup) getView();
		content = (ViewGroup) overview_layout.findViewById(R.id.content);
		content.removeAllViews();
		overview_fragment = (ViewGroup) inflater.inflate(R.layout.overview_fragment_layout, null);
		progress_layout.setVisibility(View.GONE);

		if (!isDetached()) {
			if (task != null && task.getStatus().equals(AsyncTask.Status.PENDING)) {
				task.execute();
			} else if (task == null || task.getStatus().equals(AsyncTask.Status.FINISHED)) {
				task = new BuildOverviewAsyncTask();
				task.execute();
			} else if (task != null && task.getStatus().equals(AsyncTask.Status.RUNNING)) {
				task.cancel(true);
			}
			if (isSelected()) {
				initActionBar();
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	/**
	 * Downloads the user picture and decodes it into a {@link Bitmap} to be set into an ImageView.
	 * 
	 * @param view
	 *            the ImageView
	 * @param uid
	 *            user id is needed to get the appropriate picture
	 */
	private void loadPicture(ImageView view, int uid) {
		CwApplication.cwImageLoader().displayImage(context.getString(R.string.userpic_url, uid, 60), context,
				(ImageView) view, false, R.drawable.user_stub);
	}

	private void initActionBar() {
		if (context != null) {
			if (context.getParent() instanceof CwNavigationMainTabActivity) {
				ActionBar actionBar = getActionBar();
				actionBar.removeAllActions();
				actionBar.setTitle(context.getString(R.string.home));
			}
		}
	}

	/**
	 * Asynchronous task to receive news from the API and build up the ui.
	 * 
	 * @author Alexander Dridiger
	 */
	private class BuildOverviewAsyncTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected void onPreExecute() {
			progress_layout.setVisibility(View.VISIBLE);
			overview_fragment.setVisibility(View.GONE);
		}

		@Override
		protected Void doInBackground(Void... params) {
			if (!isCancelled()) {
				buildUserView();
				createBanner();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void params) {
			progress_layout.setVisibility(View.GONE);
			overview_fragment.setVisibility(View.VISIBLE);
			content.removeView(overview_fragment);
			content.addView(overview_fragment);
		}

		/**
		 * Builds and fills the view for displaying user data.
		 * 
		 * @param parent
		 */
		private void buildUserView() {
			final ViewGroup userLoggedLayout = (ViewGroup) overview_fragment
					.findViewById(R.id.overview_logged_user_layout);
			final TextView usrnmTxt = (TextView) overview_fragment.findViewById(R.id.overview_username);
			final ImageView icon = (ImageView) overview_fragment.findViewById(R.id.overview_usericon);
			Button loginBttn = (Button) overview_fragment.findViewById(R.id.overview_login_user_bttn);
			Button logoutBttn = (Button) overview_fragment.findViewById(R.id.overview_logout_user_bttn);
			if (cwLoginManager.isLoggedIn()) {
				loadPicture(icon, cwLoginManager.getAuthenticatedUser().getUid());
				setVisibility(View.INVISIBLE, userLoggedLayout, loginBttn);
				setVisibility(View.VISIBLE, logoutBttn);
				usrnmTxt.setText(cwLoginManager.getAuthenticatedUser().getUsername());
			} else {
				setVisibility(View.VISIBLE, userLoggedLayout, loginBttn);
				setVisibility(View.INVISIBLE, logoutBttn);
			}

			loginBttn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					new LoginUserAsyncTask().execute();
				}
			});

			logoutBttn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					new LogoutUserAsyncTask().execute();
				}
			});
		}

		/**
		 * @param parent
		 */
		private void createBanner() {
			ViewFlipper flipper = (ViewFlipper) overview_fragment.findViewById(R.id.overview_banner_flipper);
			int newsAmount = 0;
			int blogsAmount = 0;
			int msgsAmount = 0;
			if (cwEntityManager.getCwUser() != null) {
				newsAmount = 0;
				if (cwEntityManager.getCachedNews().size() > 0) {
					if (cwEntityManager.getCwUser().getLastNewsId() > 0
							&& cwEntityManager.getCachedNews().get(0).getSubjectId() > cwEntityManager.getCwUser()
									.getLastNewsId()) {
						newsAmount = cwEntityManager.getCachedNews().get(0).getSubjectId()
								- cwEntityManager.getCwUser().getLastNewsId();
					}
				}
				blogsAmount = 0;
				if (cwEntityManager.getCachedBlogs(Filter.BLOGS_NORMAL).size() > 0) {
					if (cwEntityManager.getCwUser().getLastBlogId() > 0
							&& cwEntityManager.getCachedBlogs(Filter.BLOGS_NORMAL).get(0).getSubjectId() > cwEntityManager
									.getCwUser().getLastBlogId()) {
						blogsAmount = cwEntityManager.getCachedBlogs(Filter.BLOGS_NORMAL).get(0).getSubjectId()
								- cwEntityManager.getCwUser().getLastBlogId();
					}
				}
				List<Message> msgs = cwManager.getMessages(Filter.MSGS_INBOX, 5);
				msgs.addAll(cwManager.getMessages(Filter.MSGS_OUTBOX, 5));
				for (Message msg : msgs) {
					if (msg.getUnixtime() > cwEntityManager.getCwUser().getDate().getTime()) {
						msgsAmount++;
					}
				}
			}
			flipper.addView(createBannerCell(getString(R.string.tab_news_tag), R.drawable.banner_news,
					getString(R.string.overview_banner_news_title),
					getString(R.string.overview_banner_details, newsAmount, "News")));
			flipper.addView(createBannerCell(getString(R.string.tab_blogs_tag), R.drawable.banner_blogs,
					getString(R.string.overview_banner_blogs_title),
					getString(R.string.overview_banner_details, blogsAmount, "Blogs")));
			flipper.addView(createBannerCell(getString(R.string.tab_msgs_tag), R.drawable.banner_msgs,
					getString(R.string.overview_banner_msgs_title),
					getString(R.string.overview_banner_details, msgsAmount, "Nachrichten")));
			flipper.setInAnimation(AnimationUtils.loadAnimation(context, R.anim.overview_marquee_in));
			flipper.setOutAnimation(AnimationUtils.loadAnimation(context, R.anim.overview_marquee_out));
			flipper.setFlipInterval(3000);
			flipper.startFlipping();
		}

		/**
		 * @param tag
		 * @param icon
		 * @param title
		 * @param details
		 * @return
		 */
		private ViewGroup createBannerCell(final String tag, int icon, String title, String details) {
			ViewGroup cell = (ViewGroup) inflater.inflate(R.layout.overview_banner_cell_layout, null);
			((ImageView) cell.findViewById(R.id.overview_banner_icon)).setImageResource(icon);
			((TextView) cell.findViewById(R.id.overview_banner_title)).setText(title);
			((TextView) cell.findViewById(R.id.overview_banner_details)).setText(details);

			cell.setTag(tag);
			cell.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (v.getTag() instanceof String && ((String) v.getTag()).matches(tag)) {
						((CwNavigationMainTabActivity) getActivity().getParent()).getTabHost().setCurrentTabByTag(tag);
					}
				}
			});
			return cell;
		}
	}

	/**
	 * Asynchronous task to login a user.
	 * 
	 * @author Alexander Dridiger
	 */
	private class LoginUserAsyncTask extends AsyncTask<Void, Void, Bitmap> {

		boolean doWork = false;

		@Override
		protected void onPreExecute() {
			EditText usrnmEdttxt = (EditText) overview_fragment.findViewById(R.id.overview_edttxt_username);
			EditText passwEdttxt = (EditText) overview_fragment.findViewById(R.id.overview_edttxt_passw);
			if (StringUtils.isBlank(usrnmEdttxt.getText().toString())) {
				usrnmEdttxt.requestFocus();
				usrnmEdttxt.setError(getString(R.string.no_username_entered));
			} else if (StringUtils.isBlank(passwEdttxt.getText().toString())) {
				passwEdttxt.requestFocus();
				passwEdttxt.setError(getString(R.string.no_password_entered));
			} else {
				usrnmEdttxt.setError(null);
				passwEdttxt.setError(null);
				Toast.makeText(context, getResources().getString(R.string.try_login), Toast.LENGTH_SHORT).show();
				doWork = true;
			}
		}

		@Override
		protected Bitmap doInBackground(Void... params) {
			if (doWork) {
				EditText usrnmEdttxt = (EditText) overview_fragment.findViewById(R.id.overview_edttxt_username);
				EditText passwEdttxt = (EditText) overview_fragment.findViewById(R.id.overview_edttxt_passw);
				cwLoginManager.saveAndLoginUser(usrnmEdttxt.getText().toString(), passwEdttxt.getText().toString(),
						cwEntityManager.getCachedNews().size() > 0 ? cwEntityManager.getCachedNews().get(0)
								.getSubjectId() : -1,
						cwEntityManager.getCachedBlogs(Filter.BLOGS_NORMAL).size() > 0 ? cwEntityManager
								.getCachedBlogs(Filter.BLOGS_NORMAL).get(0).getSubjectId() : -1);
			}
			return viewUtility.getUserIcon(cwLoginManager.getAuthenticatedUser().getUid(), 60);
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			ViewGroup userLoggedLayout = (ViewGroup) overview_fragment.findViewById(R.id.overview_logged_user_layout);
			TextView usrnmTxt = (TextView) overview_fragment.findViewById(R.id.overview_username);
			Button loginBttn = (Button) overview_fragment.findViewById(R.id.overview_login_user_bttn);
			Button logoutBttn = (Button) overview_fragment.findViewById(R.id.overview_logout_user_bttn);
			ImageView icon = (ImageView) overview_fragment.findViewById(R.id.overview_usericon);
			if (doWork && cwLoginManager.isLoggedIn()) {
				usrnmTxt.setText(cwLoginManager.getAuthenticatedUser().getUsername());
				icon.setImageBitmap(result);
				setVisibility(View.INVISIBLE, userLoggedLayout, loginBttn);
				setVisibility(View.VISIBLE, logoutBttn);
				Toast.makeText(context, context.getString(R.string.logged_in), Toast.LENGTH_SHORT).show();
			} else if (doWork) {
				icon.setImageBitmap(result);
				setVisibility(View.VISIBLE, userLoggedLayout, loginBttn);
				setVisibility(View.INVISIBLE, logoutBttn);
				Toast.makeText(context, context.getString(R.string.login_failed), Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * Asynchronous task to logout a user.
	 * 
	 * @author Alexander Dridiger
	 */
	private class LogoutUserAsyncTask extends AsyncTask<Void, Void, Bitmap> {

		@Override
		protected void onPreExecute() {
			Toast.makeText(context, context.getString(R.string.logging_out), Toast.LENGTH_SHORT).show();
		}

		@Override
		protected Bitmap doInBackground(Void... params) {
			cwLoginManager.logoutUser();
			return viewUtility.getUserIcon(-1, 60);
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			TextView usrnmTxt = (TextView) overview_fragment.findViewById(R.id.overview_username);
			ViewGroup userLoggedLayout = (ViewGroup) overview_fragment.findViewById(R.id.overview_logged_user_layout);
			ImageView icon = (ImageView) overview_fragment.findViewById(R.id.overview_usericon);
			Button loginBttn = (Button) overview_fragment.findViewById(R.id.overview_login_user_bttn);
			Button logoutBttn = (Button) overview_fragment.findViewById(R.id.overview_logout_user_bttn);
			icon.setImageBitmap(result);
			usrnmTxt.setText("");
			setVisibility(View.VISIBLE, userLoggedLayout, loginBttn);
			setVisibility(View.INVISIBLE, logoutBttn);
			Toast.makeText(context, context.getString(R.string.logged_out), Toast.LENGTH_SHORT).show();
		}
	}

	private void setVisibility(int visibility, View... views) {
		for (View view : views) {
			view.setVisibility(visibility);
		}
	}
	
	@Override
	public void setForeground(boolean isSelected) {
		super.setForeground(isSelected);
		if (isSelected) {
			initActionBar();
		}
	}

	@Override
	public void backPressed() {
	}

	// private MenuInflater menuInflater;
	//
	// @Override
	// public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	// if (isSelected()) {
	// super.onCreateOptionsMenu(menu, inflater);
	// menu.clear();
	// inflater.inflate(R.menu.singlenews_menu, menu);
	// }
	// menuInflater = inflater;
	// }
	//
	// @Override
	// public void onPrepareOptionsMenu(Menu menu) {
	// if (isSelected()) {
	// super.onPrepareOptionsMenu(menu);
	// menu.clear();
	// menuInflater.inflate(R.menu.singlenews_menu, menu);
	// }
	// }
}
