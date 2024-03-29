package de.consolewars.android.app.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import roboguice.inject.InjectResource;

import com.google.inject.Singleton;

import de.consolewars.android.app.R;
import de.consolewars.api.exception.ConsolewarsAPIException;
import de.consolewars.api.util.URLCreator;

/*
 * Copyright [2011] [Alexander Dridiger]
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
@Singleton
public class CommentsParser {

	@InjectResource(R.string.api_key)
	private String APIKey;

	public CommentsRoot parse(int id, int area, int count, int talkback_viewpage, int talkback_lastpage)
			throws ConsolewarsAPIException, IOException {
		// name of the api-php file
		String apiname = "getcomments";

		URLCreator apiUrl = new URLCreator("http://www.consolewars.de/api/" + apiname + ".php");

		apiUrl.addArgument("apitoken", APIKey);
		apiUrl.addArgument("id", id);
		apiUrl.addArgument("area", area);
		apiUrl.addArgument("count", count);
		apiUrl.addArgument("talkback_viewpage", talkback_viewpage);
		if (talkback_lastpage != -1) {
			apiUrl.addArgument("talkback_lastpage", talkback_lastpage);
		}

		URL url = new URL(apiUrl.toString());

		URLConnection conn = url.openConnection();
		String encoding = "ISO-8859-1";

		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), encoding));
		StringBuilder sb = new StringBuilder(16384);
		try {
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append('\n');
			}
		} finally {
			br.close();
		}

		Serializer serializer = new Persister();
		CommentsRoot root = new CommentsRoot();
		try {
			root = serializer.read(CommentsRoot.class, sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!root.getComments().isEmpty()) {
			root.setMaxPage(root.getComments().iterator().next().getPagecount());
			root.getComments().remove(root.getComments().iterator().next());
		}
		return root;
	}
}
