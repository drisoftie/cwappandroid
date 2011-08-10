package de.consolewars.android.app;

import java.security.GeneralSecurityException;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;
import android.widget.ProgressBar;
import de.consolewars.android.app.tab.CwNavigationMainTabActivity;
import de.consolewars.android.app.util.HashEncrypter;
import de.consolewars.api.data.AuthenticatedUser;
import de.consolewars.api.exception.ConsolewarsAPIException;

/**
 * Activity handling the splash screen.
 * 
 * @author Alexander Dridiger
 */
public class SplashScreenActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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
			requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
			setContentView(R.layout.splash_layout);
			getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar_layout);

			progressBar = (ProgressBar) findViewById(R.id.splash_progressbar);
			// first set progressbar view
			progressBar.setProgress(0);
		}

		@Override
		protected Void doInBackground(Void... params) {
			CWApplication.getInstance().getDataHandler().loadCurrentUser();
			AuthenticatedUser user = null;
			try {
				user = CWApplication.getInstance().getApiCaller().getAuthUser(
						CWApplication.getInstance().getDataHandler().getUserName(),
						HashEncrypter.decrypt(getString(R.string.db_cry), CWApplication.getInstance()
								.getDataHandler().getHashPw()));
			} catch (ConsolewarsAPIException e) {
				e.printStackTrace();
			} catch (GeneralSecurityException e) {
				e.printStackTrace();
			}
			CWApplication.getInstance().setAuthenticatedUser(user);
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
