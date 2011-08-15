package de.consolewars.android.app;

import java.security.GeneralSecurityException;

import roboguice.activity.RoboActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;
import android.widget.ProgressBar;

import com.google.inject.Inject;

import de.consolewars.android.app.db.AppDataHandler;
import de.consolewars.android.app.tab.CwNavigationMainTabActivity;
import de.consolewars.android.app.util.HashEncrypter;
import de.consolewars.api.data.AuthenticatedUser;
import de.consolewars.api.exception.ConsolewarsAPIException;

/**
 * Activity handling the splash screen.
 * 
 * @author Alexander Dridiger
 */
public class SplashScreenActivity extends RoboActivity {

	@Inject
	private CWApplication cwApplication;
	@Inject
	private CWManager cwManager;
	@Inject
	private AppDataHandler appDataHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.splash_layout);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar_layout);

		new SplashAsyncTask().execute();
	}

	/**
	 * Asynchronous task to initialize questions.
	 * 
	 * @author Alexander Dridiger
	 */
	private class SplashAsyncTask extends AsyncTask<Void, Integer, Void> {

		private ProgressBar progressBar;

		@Override
		protected void onPreExecute() {
			progressBar = (ProgressBar) findViewById(R.id.splash_progressbar);
			// first set progressbar view
			progressBar.setProgress(0);
		}

		@Override
		protected Void doInBackground(Void... params) {
			appDataHandler.loadCurrentUser();
			AuthenticatedUser user = null;
			try {
				user = cwManager.getAuthUser(appDataHandler.getUserName(),
						HashEncrypter.decrypt(getString(R.string.db_cry), appDataHandler.getHashPw()));
			} catch (ConsolewarsAPIException e) {
				e.printStackTrace();
			} catch (GeneralSecurityException e) {
				e.printStackTrace();
			}
			cwApplication.setAuthenticatedUser(user);
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			progressBar.setProgress(values[0]);
		}

		protected boolean _active = true;
		protected int _splashTime = 2000;

		@Override
		protected void onPostExecute(Void result) {
			// thread for displaying the SplashScreen
			Thread splashThread = new Thread() {
				@Override
				public void run() {
					try {
						int waited = 0;
						while (_active && (waited < _splashTime)) {
							sleep(100);
							if (_active) {
								waited += 100;
							}
						}
					} catch (InterruptedException e) {
						// do nothing
					} finally {
						finish();
						SplashScreenActivity.this.startActivity(new Intent(SplashScreenActivity.this,
								CwNavigationMainTabActivity.class));
					}
				}
			};
			splashThread.start();
		}
	}
}
