package de.consolewars.api.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;
import de.consolewars.api.data.AuthStatus;
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
 * @author cerpin (arrewk@gmail.com)
 */
public abstract class AbstractSAXParser<T> extends DefaultHandler {

	private String APIURL;
	private T tempItem;

	protected String tempValue;

	private AuthStatus authStatus;

	private boolean isInAPISubtree = false;

	private ArrayList<T> items;

	public AbstractSAXParser(String APIURL) {
		this.APIURL = APIURL;
		items = new ArrayList<T>();
	}

	/**
	 * @return the aPIURL
	 */
	public String getAPIURL() {
		return APIURL;
	}

	/**
	 * @return the items
	 */
	public ArrayList<T> getItems() {
		return items;
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		if (!qName.equals(localName))
			qName = localName;

		tempValue = "";
		if (qName.equals("item")) {
			tempItem = createTempItem();
		}
		if (qName.equals("api")) {
			isInAPISubtree = true;
		}
	}

	public void endElement(String uri, String localName, String qName) {
		if (!qName.equals(localName))
			qName = localName;
		parseAuthStatus(uri, localName, qName);
		if (qName.equals("item") && isValidItem()) {
			items.add(tempItem);
		}
		parseItem(uri, localName, qName);
	}

	/**
	 * additional item-requirements in order to add it to the list
	 * 
	 * @author cerpin (arrewk@gmail.com)
	 * @return true
	 */
	protected abstract boolean isValidItem();

	/**
	 * parsing the item subtrees
	 * 
	 * @author cerpin (arrewk@gmail.com)
	 */
	protected abstract void parseItem(String uri, String localName, String qName);

	protected void parseAuthStatus(String uri, String localName, String qName) {
		if (!isInAPISubtree)
			return;

		if (!qName.equals(localName))
			qName = localName;

		if (qName.equals("authstatus")) {
			authStatus = new AuthStatus(tempValue);
		}
		if (qName.equals("reason") && isInAPISubtree) {
			authStatus.setReason(tempValue);
		}
		if (qName.equals("api")) {

			isInAPISubtree = false;
		}
	}

	public ArrayList<T> parseDocument() throws ConsolewarsAPIException {
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setValidating(false);
		URL url;
		try {
			url = new URL(APIURL);

			InputStream is = url.openConnection().getInputStream();
			Writer writer = new StringWriter();
			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}

			// 2nd attemp
			// HttpGet httpRequest = null;
			// httpRequest = new HttpGet(url.toURI());
			//
			// HttpClient httpclient = null;
			//
			// // sets up parameters
			// HttpParams params = new BasicHttpParams();
			// HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			// HttpProtocolParams.setContentCharset(params, "utf-8");
			// params.setBooleanParameter("http.protocol.expect-continue", false);
			//
			// // registers schemes for both http and https
			// SchemeRegistry registry = new SchemeRegistry();
			// registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			// final SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
			// sslSocketFactory
			// .setHostnameVerifier(SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
			// registry.register(new Scheme("https", sslSocketFactory, 443));
			//
			// ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(params,
			// registry);
			// httpclient = new DefaultHttpClient(manager, params);
			//
			// HttpResponse response = (HttpResponse) httpclient.execute(httpRequest);
			//
			// HttpEntity entity = response.getEntity();
			// BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
			// InputStream instream = bufHttpEntity.getContent();

			// 3rd attemp
			// InputStream instream = url.openConnection().getInputStream();
			//
			// BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
			// StringBuilder sb = new StringBuilder();
			// String line = null;
			// while ((line = reader.readLine()) != null) {
			// sb.append(line + "\n");
			// }
			// instream.close();

			Log.i("********XMLANFANG*******", APIURL);
			Log.i("********XML*******", writer.toString());
			Log.i("********XMLENDE*******", "STIER");

			// URLConnection connection = url.openConnection();
			// connection.connect();
			//
			// InputSource is = new InputSource();
			//
			// if
			// (sb.toString().toString().contains("<?xml version=\"1.0\" encoding=\"ISO-8859-1\""))
			// {
			// URLConnection localURLConnection = new URL(APIURL).openConnection();
			// localURLConnection.connect();
			// InputStream localInputStream = localURLConnection.getInputStream();
			// is = new InputSource(localInputStream);
			// is.setEncoding("ISO-8859-1");
			// } else {
			// Reader isr = new InputStreamReader(connection.getInputStream());
			// is.setCharacterStream(isr);
			// is.setEncoding("UTF-8");
			// }
			//
			// SAXParser parser = spf.newSAXParser();
			// parser.parse(is, this);
			URLConnection localURLConnection = new URL(APIURL).openConnection();
			localURLConnection.connect();
			SAXParser localSAXParser = spf.newSAXParser();
			InputStream localInputStream = localURLConnection.getInputStream();
			localSAXParser.parse(localInputStream, this);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
			return parseDocumentIso();
		} catch (MalformedURLException e1) {
			throw new ConsolewarsAPIException("Es sind Verbindungsprobleme aufgetreten", e1);
		} catch (IOException e) {
			throw new ConsolewarsAPIException("Ein-/Ausgabefehler", e);
		}
		return items;
	}

	private ArrayList<T> parseDocumentIso() throws ConsolewarsAPIException {
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setValidating(false);
		try {
			String str = this.APIURL;
			URLConnection localURLConnection = new URL(str).openConnection();
			localURLConnection.connect();
			SAXParser localSAXParser = spf.newSAXParser();
			InputStream localInputStream = localURLConnection.getInputStream();
			InputSource localInputSource = new InputSource(localInputStream);
			localInputSource.setEncoding("ISO-8859-1");
			localSAXParser.parse(localInputSource, this);
		} catch (ParserConfigurationException localParserConfigurationException) {
			localParserConfigurationException.printStackTrace();
		} catch (SAXException localSAXException) {
			localSAXException.printStackTrace();
		} catch (MalformedURLException localMalformedURLException) {
			throw new ConsolewarsAPIException("Es sind Verbindungsprobleme aufgetreten",
					localMalformedURLException);
		} catch (IOException localIOException) {
			throw new ConsolewarsAPIException("Ein-/Ausgabefehler", localIOException);
		}
		return this.items;
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		tempValue += new String(ch, start, length);
	}

	public AuthStatus getAuthStatus() throws ConsolewarsAPIException {
		if (authStatus == null) {
			parseDocument();
		}
		return authStatus;
	}

	public T getTempItem() {
		return tempItem;
	}

	/**
	 * creating a new item
	 * 
	 * @author cerpin (arrewk@gmail.com)
	 * @return
	 */
	protected abstract T createTempItem();
}
