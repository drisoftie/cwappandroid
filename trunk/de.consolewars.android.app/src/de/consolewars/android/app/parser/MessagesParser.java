package de.consolewars.android.app.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import roboguice.inject.InjectResource;

import com.google.inject.Singleton;

import de.consolewars.android.app.R;
import de.consolewars.android.app.db.domain.CwMessage;
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
public class MessagesParser {

	@InjectResource(R.string.api_key)
	private String APIKey;

	public List<CwMessage> parse(int uid, String pass, int folder, int count) throws ConsolewarsAPIException,
			IOException {
		// name of the api-php file
		String apiname = "getmessages";

		URLCreator apiUrl = new URLCreator("http://www.consolewars.de/api/" + apiname + ".php");
		apiUrl.addArgument("apitoken", APIKey);
		apiUrl.addArgument("user", uid);
		apiUrl.addArgument("pass", pass);
		apiUrl.addArgument("folder", folder);
		apiUrl.addArgument("count", count);

		URL url = new URL(apiUrl.toString());

		String cookie = "cwbb_userid=" + uid + "; cwbb_password=" + pass;
		URLConnection localURLConnection = url.openConnection();
		localURLConnection.setRequestProperty("Cookie", cookie);

		localURLConnection.setRequestProperty("Content-Type", "text/plain; charset=utf-8");

		localURLConnection.connect();

		String encoding = localURLConnection.getContentEncoding();
		// if (encoding == null) {
		// encoding = "ISO-8859-1";
		encoding = "UTF-8";

		// }

		BufferedReader br = new BufferedReader(new InputStreamReader(localURLConnection.getInputStream(), encoding));
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
		MessagesRoot root = new MessagesRoot();
		try {
			root = serializer.read(MessagesRoot.class, sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return root.getMessages();
	}
}
