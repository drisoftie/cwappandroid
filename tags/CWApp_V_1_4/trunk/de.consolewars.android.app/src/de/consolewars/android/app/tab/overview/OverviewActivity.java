package de.consolewars.android.app.tab.overview;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import roboguice.activity.RoboActivity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.inject.Inject;

import de.consolewars.android.app.CWApplication;
import de.consolewars.android.app.R;
import de.consolewars.android.app.db.AppDataHandler;
import de.consolewars.android.app.db.DatabaseManager;
import de.consolewars.android.app.tab.CwNavigationMainTabActivity;
import de.consolewars.android.app.util.HashEncrypter;
import de.consolewars.api.API;
import de.consolewars.api.data.AuthenticatedUser;
import de.consolewars.api.data.Message;
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
 * Central Activity to handle the ui for the overvieew.
 * 
 * @author Alexander Dridiger
 */
public class OverviewActivity extends RoboActivity {

	@Inject
	private CWApplication cwApplication;
	@Inject
	private AppDataHandler appDataHandler;
	@Inject
	private DatabaseManager databaseManager;
	@Inject
	private API api;

	private CwNavigationMainTabActivity mainTabs;

	ViewGroup overview_layout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*
		 * TODO: Might become a source of error someday, if activity design
		 * changes. Would be better to handle it with intents.
		 */
		if (getParent().getParent() instanceof CwNavigationMainTabActivity) {
			mainTabs = (CwNavigationMainTabActivity) getParent().getParent();
		}
		overview_layout = (ViewGroup) LayoutInflater.from(getParent()).inflate(R.layout.overview_layout, null);
		new BuildOverviewAsyncTask().execute();
	}

	private void createBanner(final View parent) {
		ViewFlipper flipper = (ViewFlipper) parent.findViewById(R.id.overview_banner_flipper);
		int newsAmount = 0;
		int blogsAmount = 0;
		int msgsAmount = 0;
		if (appDataHandler.getDate() != -1) {
			try {
				Date date = new Date(appDataHandler.getDate());
				newsAmount = api.getNewsList(5, 0, date).size();
				blogsAmount = api.getBlogsList(-1, 5, 0, date).size();
				blogsAmount += api.getBlogsList(-1, 5, 1, date).size();
				blogsAmount += api.getBlogsList(-1, 5, 2, date).size();
				AuthenticatedUser user = cwApplication.getAuthenticatedUser();
				for (Message msg : api.getMessages(user.getUid(), user.getPasswordHash(), 0, 5)) {
					if (msg.getUnixtime() > appDataHandler.getDate()) {
						msgsAmount++;
					}
				}
				for (Message msg : api.getMessages(user.getUid(), user.getPasswordHash(), -1, 5)) {
					if (msg.getUnixtime() > appDataHandler.getDate()) {
						msgsAmount++;
					}
				}
			} catch (ConsolewarsAPIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
		flipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.overview_marquee_in));
		flipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.overview_marquee_out));
		flipper.setFlipInterval(3000);
		flipper.startFlipping();
	}

	private ViewGroup createBannerCell(final String tag, int icon, String title, String details) {
		ViewGroup cell = (ViewGroup) LayoutInflater.from(getParent()).inflate(R.layout.overview_banner_cell_layout,
				null);
		((ImageView) cell.findViewById(R.id.overview_banner_icon)).setImageResource(icon);
		((TextView) cell.findViewById(R.id.overview_banner_title)).setText(title);
		((TextView) cell.findViewById(R.id.overview_banner_details)).setText(details);

		cell.setTag(tag);
		cell.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (v.getTag() instanceof String && ((String) v.getTag()).matches(tag)) {
					mainTabs.getUsedTabHost().setCurrentTabByTag(tag);
				}
			}
		});
		return cell;
	}

	/**
	 * Builds and fills the view for displaying user data.
	 * 
	 * @param parent
	 */
	private void buildUserView(final View parent) {
		final EditText usrnmEdttxt = (EditText) parent.findViewById(R.id.overview_edttxt_username);
		final ImageView icon = (ImageView) parent.findViewById(R.id.overview_usericon);
		if (appDataHandler.getHashPw() != null) {
			loadPicture(icon, cwApplication.getAuthenticatedUser().getUid());
		}
		if (appDataHandler.getUserName() != null) {
			usrnmEdttxt.setText(appDataHandler.getUserName());
		} else {
			usrnmEdttxt.setText("Kein User gespeichert");
		}

		Button confirmBttn = (Button) parent.findViewById(R.id.overview_set_user_bttn);
		confirmBttn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				new LoginUserAsyncTask().execute();
			}
		});
	}

	private void persistUser(String userName, String pw) {
		Calendar calendar = GregorianCalendar.getInstance();
		if (appDataHandler.getUserDbId() != -1) {
			try {
				databaseManager.updateUserData(appDataHandler.getUserDbId(), userName,
						HashEncrypter.encrypt(getString(R.string.db_cry), pw), calendar.getTimeInMillis());
				appDataHandler.loadCurrentUser();
			} catch (GeneralSecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				databaseManager.insertUserData(userName, HashEncrypter.encrypt(getString(R.string.db_cry), pw),
						calendar.getTimeInMillis());
				appDataHandler.loadCurrentUser();
			} catch (GeneralSecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Downloads the user picture and decodes it into a {@link Bitmap} to be set
	 * into an ImageView.
	 * 
	 * @param view
	 *            the ImageView
	 * @param uid
	 *            user id is needed to get the appropriate picture
	 */
	private void loadPicture(ImageView view, int uid) {
		URL newurl;
		Bitmap mIcon_val = null;
		try {
			newurl = new URL(getString(R.string.blogs_userpic_url, uid, 60));
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
	 * Asynchronous task to receive news from the API and build up the ui.
	 * 
	 * @author Alexander Dridiger
	 */
	private class BuildOverviewAsyncTask extends AsyncTask<Void, Integer, Void> {

		private ProgressBar progressBar;

		@Override
		protected void onPreExecute() {
			// first set progressbar view
			ViewGroup progress_layout = (ViewGroup) LayoutInflater.from(OverviewActivity.this.getParent()).inflate(
					R.layout.centered_progressbar, null);
			setContentView(progress_layout);

			TextView text = (TextView) progress_layout.findViewById(R.id.centered_progressbar_text);
			text.setText(getString(R.string.loading, getString(R.string.tab_overv_head)));

			progressBar = (ProgressBar) progress_layout.findViewById(R.id.centered_progressbar);
			progressBar.setProgress(0);
		}

		@Override
		protected Void doInBackground(Void... params) {
			appDataHandler.loadCurrentUser();
			buildUserView(overview_layout);
			createBanner(overview_layout);
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			progressBar.setProgress(values[0]);
		}

		@Override
		protected void onPostExecute(Void params) {
			setContentView(overview_layout);
		}
	}

	/**
	 * Asynchronous task to login a user.
	 * 
	 * @author Alexander Dridiger
	 */
	private class LoginUserAsyncTask extends AsyncTask<Void, Void, Void> {

		boolean dowork = false;

		@Override
		protected void onPreExecute() {
			EditText usrnmEdttxt = (EditText) overview_layout.findViewById(R.id.overview_edttxt_username);
			EditText passwEdttxt = (EditText) overview_layout.findViewById(R.id.overview_edttxt_passw);
			if (usrnmEdttxt.getText().toString().matches("")) {
				usrnmEdttxt.requestFocus();
				usrnmEdttxt.setError(getString(R.string.no_username_entered));
			} else if (passwEdttxt.getText().toString().matches("")) {
				passwEdttxt.requestFocus();
				passwEdttxt.setError(getString(R.string.no_password_entered));
			} else {
				usrnmEdttxt.setError(null);
				passwEdttxt.setError(null);
				Toast.makeText(OverviewActivity.this, getResources().getString(R.string.try_login), Toast.LENGTH_SHORT)
						.show();
				dowork = true;
			}
		}

		@Override
		protected Void doInBackground(Void... params) {
			if (dowork) {
				EditText usrnmEdttxt = (EditText) overview_layout.findViewById(R.id.overview_edttxt_username);
				EditText passwEdttxt = (EditText) overview_layout.findViewById(R.id.overview_edttxt_passw);
				persistUser(usrnmEdttxt.getText().toString(), passwEdttxt.getText().toString());
				AuthenticatedUser authUser = null;
				try {
					authUser = api.authenticate(appDataHandler.getUserName(), passwEdttxt.getText().toString());
					cwApplication.setAuthenticatedUser(authUser);
				} catch (ConsolewarsAPIException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (dowork && cwApplication.getAuthenticatedUser().getSuccess().matches(getString(R.string.success_yes))) {
				ImageView icon = (ImageView) overview_layout.findViewById(R.id.overview_usericon);
				loadPicture(icon, cwApplication.getAuthenticatedUser().getUid());
				Toast.makeText(OverviewActivity.this, getResources().getString(R.string.logged_in), Toast.LENGTH_SHORT)
						.show();
			} else if (dowork) {
				Toast.makeText(OverviewActivity.this, getResources().getString(R.string.login_failed),
						Toast.LENGTH_SHORT).show();
			}
		}
	}
}
