package de.consolewars.api.parser;

import de.consolewars.android.app.db.domain.CwComment;

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
 * 
 * @author cerpin (arrewk@gmail.com)
 * 
 */
public class SAXCommentParser extends AbstractSAXParser<CwComment> {

	private boolean isComment = true;

	private int pagecount;
	private int currpage;

	public SAXCommentParser(String APIURL) {
		super(APIURL);
	}

	@Override
	protected CwComment createTempItem() {
		return new CwComment();
	}

	@Override
	protected boolean isValidItem() {
		return isComment;
	}

	@Override
	protected void parseItem(String uri, String localName, String qName) {
		if (!qName.equals(localName))
			qName = localName;

		if (qName.equals("navi")) {
			// todo
		} else if (qName.equals("currpage")) {
			currpage = Integer.parseInt(tempValue);
		} else if (qName.equals("pagecount")) {
			pagecount = Integer.parseInt(tempValue);
		} else if (qName.equals("mode")) {
			if (tempValue.equalsIgnoreCase("comment")) {
				isComment = true;
				getTempItem().setMode(tempValue);
			} else {
				isComment = false;
			}
		} else if (qName.equals("unixtime")) {
			getTempItem().setUnixtime(Integer.parseInt(tempValue));
		} else if (qName.equals("statement")) {
			getTempItem().setStatement(tempValue);
		} else if (qName.equals("quote")) {
			getTempItem().setQuote(tempValue);
		} else if (qName.equals("username")) {
			getTempItem().setUsername(tempValue);
			getTempItem().setPagecount(pagecount);
			getTempItem().setCurrpage(currpage);
		} else if (qName.equals("uid")) {
			getTempItem().setUid(Integer.parseInt(tempValue));
		} else if (qName.equals("usertitle")) {
			getTempItem().setUsertitle(tempValue);
		} else if (qName.equals("commentcount")) {
			getTempItem().setCommentcount(Integer.parseInt(tempValue));
		} else if (qName.equals("postcount")) {
			getTempItem().setPostcount(Integer.parseInt(tempValue));
		} else if (qName.equals("cid")) {
			getTempItem().setCid(Integer.parseInt(tempValue));
		}
	}
}
