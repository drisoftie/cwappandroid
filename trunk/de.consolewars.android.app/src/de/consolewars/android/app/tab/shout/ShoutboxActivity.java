package de.consolewars.android.app.tab.shout;

import java.security.GeneralSecurityException;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import de.consolewars.android.app.R;
import de.consolewars.android.app.tab.CwNavigationMainTabActivity;
import de.consolewars.android.app.util.HashEncrypter;
import de.consolewars.api.data.AuthenticatedUser;
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
 * Central Activity to handle the ui for the shoutbox. <br>
 * NOTE: Currently not working.
 * 
 * @author Alexander Dridiger
 */
public class ShoutboxActivity extends Activity {

	private CwNavigationMainTabActivity mainTabs;

	private ViewGroup shoutbox_layout;

	private AuthenticatedUser user;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getParent().getParent() instanceof CwNavigationMainTabActivity) {
			mainTabs = (CwNavigationMainTabActivity) getParent().getParent();
		}
		shoutbox_layout = (ViewGroup) LayoutInflater.from(ShoutboxActivity.this.getParent())
				.inflate(R.layout.shoutbox_layout, null);

		new BuildShoutboxAsyncTask().execute();
	}

	private View loginAndOpenShoutbox() {
		Button refresh = (Button) shoutbox_layout.findViewById(R.id.shoutbox_refresh);
		refresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				new BuildShoutboxAsyncTask().execute();
			}
		});
		WebView webView = (WebView) shoutbox_layout.findViewById(R.id.shoutbox);

		webView.getSettings().setUseWideViewPort(false);
		webView.getSettings().setPluginState(PluginState.ON);
		webView.getSettings().setPluginsEnabled(true);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setBuiltInZoomControls(false);

		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
		});
		if (mainTabs.getDataHandler().loadCurrentUser()
				&& mainTabs.getDataHandler().getHashPw().matches("")
				&& mainTabs.getDataHandler().getUserName().matches("")) {
			webView.loadUrl(getString(R.string.cw_url_slash));
		} else {
			try {
				user = mainTabs
						.getApiCaller()
						.getApi()
						.authenticate(
								mainTabs.getDataHandler().getUserName(),
								HashEncrypter.decrypt(getString(R.string.db_cry), mainTabs
										.getDataHandler().getHashPw()));
			} catch (GeneralSecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ConsolewarsAPIException e) {
				e.printStackTrace();
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
			ViewGroup progress_layout = (ViewGroup) LayoutInflater.from(
					ShoutboxActivity.this.getParent()).inflate(R.layout.centered_progressbar, null);
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
			WebView webView = (WebView) ShoutboxActivity.this.findViewById(R.id.shoutbox);
			if (user.getSuccess().matches("YES")) {
				webView.postUrl(getString(R.string.cw_url_slash),
						getString(R.string.cw_login, user.getPasswordHash(), user.getUsername())
								.getBytes());
				webView.loadUrl(getString(R.string.cw_shoutbox));
			} else {
				webView.loadUrl(getString(R.string.cw_url_slash));
			}
		}
	}
}
