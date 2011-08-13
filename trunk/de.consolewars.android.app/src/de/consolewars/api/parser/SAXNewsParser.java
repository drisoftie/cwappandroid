package de.consolewars.api.parser;

import java.util.StringTokenizer;

import de.consolewars.api.data.News;
import de.consolewars.api.data.Picture;

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
 * parsing news
 * 
 * @author cerpin (arrewk@gmail.com)
 * 
 */
public class SAXNewsParser extends AbstractSAXParser<News> {

	public SAXNewsParser(String APIURL) {
		super(APIURL);
	}

	@Override
	protected News createTempItem() {
		return new News();
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
		} else if (qName.equals("description")) {
			getTempItem().setDescription(tempValue);
		} else if (qName.equals("mode")) {
			getTempItem().setMode(tempValue);
		} else if (qName.equals("unixtime")) {
			getTempItem().setUnixtime(Integer.parseInt(tempValue));
		} else if (qName.equals("category")) {
			getTempItem().setCategory(tempValue);
		} else if (qName.equals("categoryshort")) {
			getTempItem().setCategoryshort(tempValue);
		} else if (qName.equals("author")) {
			getTempItem().setAuthor(tempValue);
		} else if (qName.equals("piclist")) {
			getTempItem().setPiclist(fetchPicList(tempValue));
		} else if (qName.equals("comments")) {
			getTempItem().setComments(Integer.parseInt(tempValue));
		} else if (qName.equals("url")) {
			getTempItem().setUrl(tempValue);
		} else if (qName.equals("picid")) {
			getTempItem().setPicid(Integer.parseInt(tempValue));
		} else if (qName.equals("article")) {
			getTempItem().setArticle(tempValue);
		}
	}

	private Picture[] fetchPicList(String piclist) {
		StringTokenizer piclistTokenizer = new StringTokenizer(piclist, ",");
		int picscount = piclistTokenizer.countTokens();
		Picture[] list = new Picture[picscount];
		for (int i = 0; piclistTokenizer.hasMoreTokens(); i++) {
			StringTokenizer picTokenizer = new StringTokenizer(piclistTokenizer.nextToken(), "/");

			int id = Integer.parseInt(picTokenizer.nextToken());
			int width = Integer.parseInt(picTokenizer.nextToken());
			int height = Integer.parseInt(picTokenizer.nextToken());
			list[i] = new Picture(id, width, height);
		}
		return list;
	}

}
