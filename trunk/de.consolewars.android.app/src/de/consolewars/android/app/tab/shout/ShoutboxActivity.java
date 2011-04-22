package de.consolewars.android.app.tab.shout;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shoutbox_layout);

		WebView webView = (WebView) findViewById(R.id.shoutbox);
		// webView.loadUrl("http://www.consolewars.de/chat");

		// WebSettings webSettings = webView.getSettings();
		// webSettings.setSavePassword(false);
		// webSettings.setSaveFormData(false);
		// webSettings.setJavaScriptEnabled(true);

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

		webView.loadUrl("http://www.consolewars.de/chat/");

		// switch(mode) {
		// case MODE_LOGIN:
		// web.loadUrl("http://www.consolewars.de");
		// break;
		//
		// case MODE_SHOUTBOX:
		// web.loadUrl("http://www.consolewars.de/chat/popup.php");
		// break;

		// webView.setWebViewClient(new WebViewClient() {
		// @Override
		// public boolean shouldOverrideUrlLoading(WebView view, String url) {
		// view.loadUrl(url);
		// return false;
		// }
		// });
	}
}
