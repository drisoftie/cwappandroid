package de.consolewars.android.app;

import roboguice.activity.RoboActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ProgressBar;

import com.google.inject.Inject;

import de.consolewars.android.app.tab.CwNavigationMainTabActivity;

/**
 * Activity handling the splash screen.
 * 
 * @author Alexander Dridiger
 */
public class SplashScreenActivity extends RoboActivity {

	@Inject
	private CwLoginManager cwLoginManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_layout);
		// BugSenseHandler.setup(this, getString(R.string.bug_key));

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
			cwLoginManager.checkSavedUserAndLogin();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			SplashScreenActivity.this.startActivity(new Intent(SplashScreenActivity.this,
					CwNavigationMainTabActivity.class));
			finish();
		}
	}
}
