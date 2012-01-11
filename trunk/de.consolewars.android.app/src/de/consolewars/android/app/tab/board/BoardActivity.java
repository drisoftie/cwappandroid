package de.consolewars.android.app.tab.board;

import roboguice.activity.RoboActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebSettings.ZoomDensity;
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
 * Central Activity to handle the ui for the messageboard. <br>
 * 
 * @author Alexander Dridiger
 */
public class BoardActivity extends RoboActivity {

	@Inject
	private CwLoginManager cwLoginManager;

	private WebView webView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.board_layout);
		initActionBar();
		new OpenBoardAsyncTask().execute();
	}

	/**
	 * Asynchronous task to login into consolewars.de and open the messageboard.
	 * 
	 * @author Alexander Dridiger
	 */
	private class OpenBoardAsyncTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected void onPreExecute() {
			findViewById(R.id.progressbar).setVisibility(View.VISIBLE);
			findViewById(R.id.board).setVisibility(View.INVISIBLE);
		}

		@Override
		protected Void doInBackground(Void... params) {
			loginAndOpenBoard();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			findViewById(R.id.progressbar).setVisibility(View.INVISIBLE);
			findViewById(R.id.board).setVisibility(View.VISIBLE);
		}

		private void loginAndOpenBoard() {

			webView = (WebView) findViewById(R.id.board);

			webView.getSettings().setDefaultZoom(ZoomDensity.FAR);
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
					if (url.contains(getString(R.string.cw_board_prefix))
							&& !url.endsWith(getString(R.string.cw_board_standard_style_suffix))) {
						url += getString(R.string.cw_board_mobile_style_suffix);
					}
					view.loadUrl(url);
					return true;
				}
			});
			CookieManager cm = CookieManager.getInstance();
			if (!cwLoginManager.isLoggedIn()) {
				CookieSyncManager.createInstance(BoardActivity.this);
				cm.removeAllCookie();
				CookieSyncManager.getInstance().sync();
				webView.loadUrl(getString(R.string.cw_url_slash));
			} else {
				CookieSyncManager.createInstance(BoardActivity.this);
				String cookie = cm.getCookie(getString(R.string.cw_domain));
				if (cookie.contains(getString(R.string.cw_cookie_userid))
						&& cookie.contains(getString(R.string.cw_cookie_userid) + "="
								+ cwLoginManager.getAuthenticatedUser().getUid())
						&& cookie.contains(getString(R.string.cw_cookie_pw) + "="
								+ cwLoginManager.getAuthenticatedUser().getPasswordHash())) {
					webView.loadUrl(getString(R.string.cw_messageboard_mobile_url));
				} else {
					cm.removeAllCookie();
					cm.getCookie(getString(R.string.cw_domain)).contains(getString(R.string.cw_cookie_userid));
					cm.setCookie(getString(R.string.cw_domain), getString(R.string.cw_cookie_userid) + "="
							+ cwLoginManager.getAuthenticatedUser().getUid());
					cm.setCookie(getString(R.string.cw_domain), getString(R.string.cw_cookie_pw) + "="
							+ cwLoginManager.getAuthenticatedUser().getPasswordHash());
					CookieSyncManager.getInstance().sync();
					webView.loadUrl(getString(R.string.cw_messageboard_mobile_url));
				}
			}
		}
	}

	private void initActionBar() {
		ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.removeAllActions();
		actionBar.setTitle(getString(R.string.board));
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
				new OpenBoardAsyncTask().execute();
			}

			@Override
			public int getDrawable() {
				return R.drawable.refresh_blue_bttn;
			}
		});
	}

	@Override
	public void onBackPressed() {
		if (webView.canGoBack()) {
			webView.goBack();
		} else {
			if (getParent() instanceof CwBasicActivityGroup) {
				((CwBasicActivityGroup) getParent()).back();
			}
		}
	}
}
