package de.consolewars.api.parser;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import de.consolewars.api.data.Message;
import de.consolewars.api.exception.ConsolewarsAPIException;

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
public class SAXMessageParser extends AbstractSAXParser<Message> {

	private int uid;
	private String pass;

	public SAXMessageParser(String APIURL, int uid, String pass) {
		super(APIURL);
		this.uid = uid;
		this.pass = pass;
	}

	@Override
	protected Message createTempItem() {
		return new Message();
	}

	@Override
	protected boolean isValidItem() {
		return true;
	}

	/**
	 * Modified parser.
	 */
	@Override
	public ArrayList<Message> parseDocument() throws ConsolewarsAPIException {
		String cookie = "cwbb_userid=" + uid + "; cwbb_password=" + pass;
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setValidating(false);
		try {
			URLConnection localURLConnection = new URL(getAPIURL()).openConnection();
			localURLConnection.setRequestProperty("Cookie", cookie);
			
			localURLConnection.setRequestProperty("Content-type", "application/x-www-form-urlencoded;charset=windows-1251");
			
			localURLConnection.setRequestProperty("Accept-Charset","UTF-8");

			localURLConnection.connect();
			SAXParser localSAXParser = spf.newSAXParser();
			InputStream localInputStream = localURLConnection.getInputStream();
			localSAXParser.parse(localInputStream, this);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (MalformedURLException e1) {
			throw new ConsolewarsAPIException("Es sind Verbindungsprobleme aufgetreten", e1);
		} catch (IOException e) {
			throw new ConsolewarsAPIException("Ein-/Ausgabefehler", e);
		}
		return getItems();
	}

	@Override
	protected void parseItem(String uri, String localName, String qName) {
		if (!qName.equals(localName))
			qName = localName;

		if (qName.equals("title")) {
			getTempItem().setTitle(tempValue);
		} else if (qName.equals("id")) {
			getTempItem().setId(Integer.parseInt(tempValue));
		} else if (qName.equals("tid")) {
			getTempItem().setTid(Integer.parseInt(tempValue));
		} else if (qName.equals("mode")) {
			getTempItem().setMode(tempValue);
		} else if (qName.equals("fromusername")) {
			getTempItem().setFromusername(tempValue);
		} else if (qName.equals("fromuserid")) {
			getTempItem().setFromuserid(Integer.parseInt(tempValue));
		} else if (qName.equals("tousername")) {
			getTempItem().setTousername(tempValue);
		} else if (qName.equals("touserid")) {
			getTempItem().setTouserid(Integer.parseInt(tempValue));
		} else if (qName.equals("message")) {
			getTempItem().setMessage(tempValue);
		} else if (qName.equals("origmessage")) {
			getTempItem().setOrigmessage(tempValue);
		} else if (qName.equals("unixtime")) {
			getTempItem().setUnixtime(Integer.parseInt(tempValue));
		} else if (qName.equals("messageread")) {
			boolean messageRead = Integer.parseInt(tempValue) >= 1;
			getTempItem().setMessageread(messageRead);
		}
	}
}
