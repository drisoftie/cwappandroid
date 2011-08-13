package de.consolewars.api.parser;

import de.consolewars.api.data.Blog;

/*
 * Copyright [2009] Dimitrios Kapanikis
 *
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
 * 
 */

/**
 * @author cerpin (arrewk@gmail.com)
 */
public class SAXBlogParser extends AbstractSAXParser<Blog> {

	public SAXBlogParser(String APIURL) {
		super(APIURL);
	}

	@Override
	protected Blog createTempItem() {
		return new Blog();
	}

	@Override
	protected boolean isValidItem() {
		return true;
	}

	@Override
	protected void parseItem(String uri, String localName, String qName) {
		if (!qName.equals(localName))
			qName = localName;

		if (qName.equals("title")) {
			getTempItem().setTitle(tempValue);
		} else if (qName.equals("id")) {
			getTempItem().setId(Integer.parseInt(tempValue));
		} else if (qName.equals("mode")) {
			getTempItem().setMode(tempValue);
		} else if (qName.equals("unixtime")) {
			getTempItem().setUnixtime(Integer.parseInt(tempValue));
		} else if (qName.equals("visible")) {
			if (Integer.parseInt(tempValue) == 1) {
				getTempItem().setVisible(false);
			}
		} else if (qName.equals("rating")) {
			getTempItem().setRating(Float.parseFloat(tempValue));
		} else if (qName.equals("author")) {
			getTempItem().setAuthor(tempValue);
		} else if (qName.equals("uid")) {
			getTempItem().setUid(Integer.parseInt(tempValue));
		} else if (qName.equals("comments")) {
			getTempItem().setComments(Integer.parseInt(tempValue));
		} else if (qName.equals("url")) {
			getTempItem().setUrl(tempValue);
		} else if (qName.equals("article")) {
			getTempItem().setArticle(tempValue);
		} else if (qName.equals("description")) {
			getTempItem().setDescription(tempValue);
		}
	}
}
