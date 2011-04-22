package de.consolewars.android.app.util;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

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
public class XMLHandler extends DefaultHandler {
	private StringBuilder builder;

	private String userID;

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		super.characters(ch, start, length);
		builder.append(ch, start, length);
	}

	boolean author = false;

	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {
		super.endElement(uri, localName, name);
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		builder = new StringBuilder();
	}

	@Override
	public void startElement(String uri, String localName, String name, Attributes attributes)
			throws SAXException {
		super.startElement(uri, localName, name, attributes);
		if (localName.equalsIgnoreCase("div") && attributes.getValue("class").matches("author")) {
			author = true;
		} else if (author) {
			if (localName.equalsIgnoreCase("a")
					&& attributes.getValue("href").startsWith("/userpages/?uid=")) {
				userID = attributes.getValue("href").subSequence("/userpages/?uid=".length(),
						attributes.getValue("href").length()).toString();
				author = false;
			}
		}
	}

	public String getUserID() {
		return userID;
	}
}
