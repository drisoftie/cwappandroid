package de.consolewars.android.app.tab.shout;

import roboguice.activity.RoboActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.google.inject.Inject;

import de.consolewars.android.app.CWApplication;
import de.consolewars.android.app.R;
import de.consolewars.android.app.db.AppDataHandler;
import de.consolewars.android.app.util.ViewUtility;

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
public class ShoutboxActivity extends RoboActivity {

	@Inject
	private CWApplication cwApplication;
	@Inject
	private AppDataHandler appDataHandler;
	@Inject
	private ViewUtility viewUtility;

	private ViewGroup shoutbox_layout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		shoutbox_layout = (ViewGroup) LayoutInflater.from(ShoutboxActivity.this.getParent()).inflate(
				R.layout.shoutbox_layout, null);

		new BuildShoutboxAsyncTask().execute();
	}

	/**
	 * Asynchronous task to login into consolewars.de and open the shoutbox.
	 * 
	 * @author Alexander Dridiger
	 */
	private class BuildShoutboxAsyncTask extends AsyncTask<Void, Integer, View> {

		@Override
		protected void onPreExecute() {
			// first set progressbar view
			ViewGroup progress_layout = viewUtility.getCenteredProgressBarLayout(
					LayoutInflater.from(ShoutboxActivity.this.getParent()), R.string.shoutbox);
			setContentView(progress_layout);
		}

		@Override
		protected View doInBackground(Void... params) {
			return loginAndOpenShoutbox();
		}

		@Override
		protected void onPostExecute(View result) {
			setContentView(result);
			// WebView webView = (WebView) findViewById(R.id.shoutbox);
			// if
			// (!CWApplication.getInstance().getDataHandler().loadCurrentUser()
			// &&
			// CWApplication.getInstance().getDataHandler().getHashPw().matches("")
			// &&
			// CWApplication.getInstance().getDataHandler().getUserName().matches(""))
			// {
			// webView.loadUrl(getString(R.string.cw_url_slash));
			// } else {
			// if (CWApplication.getInstance().getAuthenticatedUser() != null
			// &&
			// CWApplication.getInstance().getAuthenticatedUser().getSuccess()
			// .matches(getString(R.string.success_yes))) {
			// CookieSyncManager.createInstance(ShoutboxActivity.this);
			// CookieManager cookieManager = CookieManager.getInstance();
			// cookieManager.removeAllCookie();
			// cookieManager.setCookie(getString(R.string.cw_domain),
			// getString(R.string.cw_cookie_userid) + "="
			// + CWApplication.getInstance().getAuthenticatedUser().getUid());
			// cookieManager.setCookie(getString(R.string.cw_domain),
			// getString(R.string.cw_cookie_pw)
			// + "=" +
			// CWApplication.getInstance().getAuthenticatedUser().getPasswordHash());
			// CookieSyncManager.getInstance().sync();
			// webView.loadUrl(getString(R.string.cw_shoutbox));
			// } else {
			// webView.loadUrl(getString(R.string.cw_url_slash));
			// }
			//
			// }
		}

		private View loginAndOpenShoutbox() {
			Button refresh = (Button) shoutbox_layout.findViewById(R.id.shoutbox_refresh);
			refresh.setOnClickListener(new OnClickListener() {

				public void onClick(View arg0) {
					// Log.i("********AllCookies**********",
					// CookieManager.getInstance().getCookie(
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
			if (!appDataHandler.loadCurrentUser() && appDataHandler.getHashPw().matches("")
					&& appDataHandler.getUserName().matches("")) {
				webView.loadUrl(getString(R.string.cw_url_slash));
			} else {
				if (cwApplication.getAuthenticatedUser() != null
						&& cwApplication.getAuthenticatedUser().getSuccess().matches(getString(R.string.success_yes))) {
					CookieSyncManager.createInstance(ShoutboxActivity.this);
					CookieManager cookieManager = CookieManager.getInstance();
					// cookieManager.removeAllCookie();
					cookieManager.setCookie(getString(R.string.cw_domain), getString(R.string.cw_cookie_userid) + "="
							+ cwApplication.getAuthenticatedUser().getUid());
					cookieManager.setCookie(getString(R.string.cw_domain), getString(R.string.cw_cookie_pw) + "="
							+ cwApplication.getAuthenticatedUser().getPasswordHash());
					CookieSyncManager.getInstance().sync();
					webView.loadUrl(getString(R.string.cw_shoutbox));
				} else {
					webView.loadUrl(getString(R.string.cw_url_slash));
				}

			}
			return shoutbox_layout;
		}
	}

	@Override
	public void onBackPressed() {
		if (getParent() instanceof ShoutboxActivityGroup) {
			((ShoutboxActivityGroup) getParent()).back();
		}
	}
}
