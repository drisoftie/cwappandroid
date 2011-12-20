package de.consolewars.android.app.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.ContentNode;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import android.content.Context;
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
 * @author Alexander Dridiger
 * 
 */
public class MediaSnapper {

	public static <Path, Subject> Map<String, String> snapAllFromHTML(Context context, String stringURL,
			Map<Path, Subject> pathAttrPairs, Map<Path, Subject> pathPicsfilterPairs) {
		Map<String, String> snaps = new HashMap<String, String>();

		// create an instance of HtmlCleaner
		HtmlCleaner cleaner = new HtmlCleaner();

		// take default cleaner properties
		CleanerProperties props = cleaner.getProperties();

		props.setAllowHtmlInsideAttributes(true);
		props.setAllowMultiWordAttributes(true);
		props.setRecognizeUnicodeChars(true);
		props.setOmitComments(true);

		// open a connection to the desired URL
		URL url;
		try {
			url = new URL(stringURL);
			URLConnection conn = url.openConnection();

			// use the cleaner to "clean" the HTML and return it as a TagNode object
			TagNode root = cleaner.clean(new InputStreamReader(conn.getInputStream()));

			for (String xpath : pathAttrPairs.keySet().toArray(new String[0])) {
				Object[] foundNodes = root.evaluateXPath(xpath);
				if (foundNodes.length > 0) {
					int counter = 1;
					if (xpath.equals(context.getString(R.string.xpath_get_video))) {
						for (Object object : foundNodes) {
							TagNode foundNode = (TagNode) object;
							snaps.put(context.getString(R.string.cw_video_tag) + counter,
									foundNode.getAttributeByName((String) pathAttrPairs.get(xpath)));
							counter++;
						}
					} else {
						// casted to a TagNode
						TagNode foundNode = (TagNode) foundNodes[0];
						snaps.put((String) pathAttrPairs.get(xpath),
								foundNode.getAttributeByName((String) pathAttrPairs.get(xpath)));
					}
				}
			}

			for (String xpath : pathPicsfilterPairs.keySet().toArray(new String[0])) {
				Object[] foundNodes = root.evaluateXPath(xpath);
				if (foundNodes != null) {
					boolean found = false;
					for (Object object : foundNodes) {
						if (!found) {
							int counter = 1;
							TagNode foundNode = (TagNode) object;
							for (Object child : foundNode.getChildren()) {
								if (child instanceof ContentNode) {
									ContentNode content = (ContentNode) child;
									if (content.getContent().toString()
											.contains((CharSequence) pathPicsfilterPairs.get(xpath))) {
										snaps.put(context.getString(R.string.cw_pic_tag) + counter, content
												.getContent().toString());
										found = true;
										counter++;
									}
								}
							}
						}
					}
				}
			}
			root = null;
			conn = null;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XPatherException e) {
			e.printStackTrace();
		}
		cleaner = null;
		props = null;
		url = null;
		return snaps;
	}

	public static String snapWithCookies(Context context, String xPath, String attrToStrip, String urlString,
			String cookies) throws IOException, XPatherException {
		String snap = "";

		// create an instance of HtmlCleaner
		HtmlCleaner cleaner = new HtmlCleaner();

		// take default cleaner properties
		CleanerProperties props = cleaner.getProperties();

		props.setAllowHtmlInsideAttributes(true);
		props.setAllowMultiWordAttributes(true);
		props.setRecognizeUnicodeChars(true);
		props.setOmitComments(true);

		URL url = new URL(urlString);

		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setDoOutput(true);

		connection.setRequestProperty(context.getString(R.string.cookie), cookies);
		connection.connect();

		// use the cleaner to "clean" the HTML and return it as a TagNode object
		TagNode root = cleaner.clean(new InputStreamReader(connection.getInputStream()));

		Object[] foundNodes = root.evaluateXPath(xPath);

		if (foundNodes.length > 0) {
			TagNode foundNode = (TagNode) foundNodes[0];
			snap = foundNode.getAttributeByName(attrToStrip);
		}

		// Log.i("**********RESPONSEURLCONN!!!!!!!!********", getData);

		return snap;
	}
}
