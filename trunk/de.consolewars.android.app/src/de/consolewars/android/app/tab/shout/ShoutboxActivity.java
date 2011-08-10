package de.consolewars.android.app.tab.shout;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings.PluginState;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import de.consolewars.android.app.CWApplication;
import de.consolewars.android.app.R;

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
 * Central Activity to handle the ui for the shoutbox. <br>
 * NOTE: Currently not working.
 * 
 * @author Alexander Dridiger
 */
public class ShoutboxActivity extends Activity {

	private ViewGroup shoutbox_layout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		shoutbox_layout = (ViewGroup) LayoutInflater.from(ShoutboxActivity.this.getParent()).inflate(
				R.layout.shoutbox_layout, null);

		new BuildShoutboxAsyncTask().execute();
	}

	private View loginAndOpenShoutbox() {
		Button refresh = (Button) shoutbox_layout.findViewById(R.id.shoutbox_refresh);
		refresh.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				// Log.i("********AllCookies**********", CookieManager.getInstance().getCookie(
				// getString(R.string.cw_domain)));
				// String[] keyValueSets =
				// CookieManager.getInstance().getCookie(getString(R.string.cw_domain))
				// .split(";");
				// for (String cookie : keyValueSets) {
				// String[] keyValue = cookie.split("=");
				// String key = keyValue[0];
				// String value = "";
				// if (keyValue.length > 1)
				// value = keyValue[1];
				// mainTabs.getHttpClient().getCookieStore().addCookie(
				// new BasicClientCookie(key.trim(), value.trim()));
				// }
				new BuildShoutboxAsyncTask().execute();
			}
		});

		WebView webView = (WebView) shoutbox_layout.findViewById(R.id.shoutbox);

		webView.getSettings().setUseWideViewPort(false);
		webView.getSettings().setPluginState(PluginState.ON);
		webView.getSettings().setPluginsEnabled(true);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setBuiltInZoomControls(true);

		webView.requestFocus(View.FOCUS_DOWN);
		webView.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_UP:
					if (!v.hasFocus()) {
						v.requestFocus();
					}
					break;
				}
				return false;
			}
		});

		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
		});
		if (!CWApplication.getInstance().getDataHandler().loadCurrentUser()
				&& CWApplication.getInstance().getDataHandler().getHashPw().matches("")
				&& CWApplication.getInstance().getDataHandler().getUserName().matches("")) {
			webView.loadUrl(getString(R.string.cw_url_slash));
		} else {
			if (CWApplication.getInstance().getAuthenticatedUser() != null
					&& CWApplication.getInstance().getAuthenticatedUser().getSuccess().matches(
							getString(R.string.success_yes))) {
				CookieManager cookieManager = CookieManager.getInstance();
				cookieManager.removeAllCookie();
				cookieManager.setCookie(getString(R.string.cw_domain), getString(R.string.cw_cookie_userid)
						+ "=" + CWApplication.getInstance().getAuthenticatedUser().getUid());
				cookieManager.setCookie(getString(R.string.cw_domain), getString(R.string.cw_cookie_pw) + "="
						+ CWApplication.getInstance().getAuthenticatedUser().getPasswordHash());
				webView.loadUrl(getString(R.string.cw_shoutbox));
			} else {
				webView.loadUrl(getString(R.string.cw_url_slash));
			}

		}
		return shoutbox_layout;
	}

	/**
	 * Asynchronous task to login into consolewars.de and open the shoutbox.
	 * 
	 * @author Alexander Dridiger
	 */
	private class BuildShoutboxAsyncTask extends AsyncTask<Void, Integer, View> {

		private ProgressBar progressBar;

		@Override
		protected void onPreExecute() {
			// first set progressbar view
			ViewGroup progress_layout = (ViewGroup) LayoutInflater.from(ShoutboxActivity.this.getParent())
					.inflate(R.layout.centered_progressbar, null);
			setContentView(progress_layout);

			TextView text = (TextView) progress_layout.findViewById(R.id.centered_progressbar_text);
			text.setText(getString(R.string.loading, getString(R.string.shoutbox)));

			progressBar = (ProgressBar) progress_layout.findViewById(R.id.centered_progressbar);
			progressBar.setProgress(0);
		}

		@Override
		protected View doInBackground(Void... params) {
			return loginAndOpenShoutbox();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			progressBar.setProgress(values[0]);
		}

		@Override
		protected void onPostExecute(View result) {
			setContentView(result);
		}
	}

	@Override
	public void onBackPressed() {
		if (getParent() instanceof ShoutboxActivityGroup) {
			((ShoutboxActivityGroup) getParent()).back();
		}
	}
}
