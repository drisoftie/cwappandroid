package de.consolewars.android.app.tab.shout;

import roboguice.activity.RoboActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.inject.Inject;

import de.consolewars.android.app.CwLoginManager;
import de.consolewars.android.app.R;
import de.consolewars.android.app.tab.CwBasicActivityGroup;
import de.consolewars.android.app.tab.CwNavigationMainTabActivity;
import de.consolewars.android.app.view.ActionBar;
import de.consolewars.android.app.view.ActionBar.Action;

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
 * 
 * @author Alexander Dridiger
 */
public class ShoutboxActivity extends RoboActivity {

	@Inject
	private CwLoginManager cwLoginManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.shoutbox_layout);
		initActionBar();

		new BuildShoutboxAsyncTask().execute();
	}

	/**
	 * Asynchronous task to login into consolewars.de and open the shoutbox.
	 * 
	 * @author Alexander Dridiger
	 */
	private class BuildShoutboxAsyncTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			findViewById(R.id.progressbar).setVisibility(View.VISIBLE);
			findViewById(R.id.shoutbox).setVisibility(View.INVISIBLE);
		}

		@Override
		protected Void doInBackground(Void... params) {
			loginAndOpenShoutbox();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			findViewById(R.id.progressbar).setVisibility(View.INVISIBLE);
			findViewById(R.id.shoutbox).setVisibility(View.VISIBLE);
		}

		private void loginAndOpenShoutbox() {
			WebView webView = (WebView) findViewById(R.id.shoutbox);

			webView.getSettings().setUseWideViewPort(false);
			webView.getSettings().setPluginState(PluginState.ON);
			webView.getSettings().setPluginsEnabled(true);
			webView.getSettings().setJavaScriptEnabled(true);
			webView.getSettings().setBuiltInZoomControls(true);

			webView.requestFocus(View.FOCUS_DOWN);
			webView.setOnTouchListener(new View.OnTouchListener() {
				@Override
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
			if (!cwLoginManager.isLoggedIn()) {
				CookieSyncManager.createInstance(ShoutboxActivity.this);
				CookieManager cookieManager = CookieManager.getInstance();
				cookieManager.removeAllCookie();
				CookieSyncManager.getInstance().sync();
				webView.loadUrl(getString(R.string.cw_url_slash));
			} else {
				CookieSyncManager.createInstance(ShoutboxActivity.this);
				CookieManager cookieManager = CookieManager.getInstance();
				// cookieManager.removeAllCookie();
				cookieManager.setCookie(getString(R.string.cw_domain), getString(R.string.cw_cookie_userid) + "="
						+ cwLoginManager.getAuthenticatedUser().getUid());
				cookieManager.setCookie(getString(R.string.cw_domain), getString(R.string.cw_cookie_pw) + "="
						+ cwLoginManager.getAuthenticatedUser().getPasswordHash());
				CookieSyncManager.getInstance().sync();
				webView.loadUrl(getString(R.string.cw_shoutbox));
			}
		}
	}

	private void initActionBar() {
		ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.removeAllActions();
		actionBar.setTitle(getString(R.string.shoutbox));
		actionBar.setDisplayHomeAsUpEnabled(true);

		actionBar.setHomeAction(new Action() {
			@Override
			public void performAction(View view) {
				((CwNavigationMainTabActivity) getParent().getParent()).getTabHost().setCurrentTab(
						CwNavigationMainTabActivity.OVERVIEW_TAB);
			}

			@Override
			public int getDrawable() {
				return R.drawable.home;
			}
		});
		actionBar.setDisplayHomeAsUpEnabled(true);

		actionBar.addAction(new Action() {
			@Override
			public void performAction(View view) {
				new BuildShoutboxAsyncTask().execute();
			}

			@Override
			public int getDrawable() {
				return R.drawable.refresh_blue_bttn;
			}
		});
	}

	@Override
	public void onBackPressed() {
		if (getParent() instanceof CwBasicActivityGroup) {
			((CwBasicActivityGroup) getParent()).back();
		}
	}
}
