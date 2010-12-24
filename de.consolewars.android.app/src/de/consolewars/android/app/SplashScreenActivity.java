package de.consolewars.android.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ProgressBar;
import de.consolewars.android.app.tab.CwNavigationMainTabActivity;

/**
 * Activity handling the splash screen.
 * 
 * @author Alexander Dridiger
 */
public class SplashScreenActivity extends Activity {

	private int progress = 0;
	private ProgressBar progressBar;
	private boolean started = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_layout);

		// FIXME missing useful implementation

		progressBar = (ProgressBar) findViewById(R.id.splash_progressbar);

		// thread for displaying the SplashScreen
		Thread splashTread = new Thread() {
			@Override
			public void run() {
				while (progress < 100) {
					try {
						splashHandler.sendMessage(splashHandler.obtainMessage());
						Thread.sleep(50);
					} catch (InterruptedException ie) {
						Log.e(getString(R.string.splash_exception_thread_tag),
								getString(R.string.splash_exception_thread_msg), ie);
					}
				}
				finish();
				if (!started) {
					startActivity(new Intent(SplashScreenActivity.this,
							CwNavigationMainTabActivity.class));
				}
			}
		};
		splashTread.start();
	}

	private Handler splashHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// just increment the progress value and pass it to the progress bar
			progress++;
			progressBar.setProgress(progress);
		}
	};

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// if screen is touched, start app immediately
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			startActivity(new Intent(SplashScreenActivity.this, CwNavigationMainTabActivity.class));
			// indicate, that the app was already started by the user
			started = true;
			finish();
		}
		return true;
	}
}
