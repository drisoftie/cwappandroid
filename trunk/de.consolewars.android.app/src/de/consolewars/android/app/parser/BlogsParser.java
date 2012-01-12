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
import de.consolewars.android.app.db.domain.CwBlog;
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
public class BlogsParser {

	@InjectResource(R.string.api_key)
	private String APIKey;

	public List<CwBlog> parse(int id) throws ConsolewarsAPIException, IOException {
		return parse(new int[] { id });
	}

	public List<CwBlog> parse(int[] id) throws ConsolewarsAPIException, IOException {
		// name of the api-php file
		String apiname = "getblogs";

		URLCreator newslistURL = new URLCreator("http://www.consolewars.de/api/" + apiname + ".php");

		newslistURL.addArgument("apitoken", APIKey);
		newslistURL.addArgument("id", id);

		URL url = new URL(newslistURL.toString());

		URLConnection conn = url.openConnection();
		String encoding = "UTF-8";

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
		BlogsRoot root = null;
		try {
			root = serializer.read(BlogsRoot.class, sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (root != null && root.getBlogs() != null) {
			return root.getBlogs();
		}
		return null;
	}
}
