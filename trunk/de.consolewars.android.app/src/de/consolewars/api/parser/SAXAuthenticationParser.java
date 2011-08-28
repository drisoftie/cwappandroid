package de.consolewars.api.parser;

import android.util.Log;
import de.consolewars.api.data.AuthenticatedUser;

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
public class SAXAuthenticationParser extends AbstractSAXParser<AuthenticatedUser> {

	public SAXAuthenticationParser(String APIURL) {
		super(APIURL);
	}

	@Override
	protected AuthenticatedUser createTempItem() {
		return new AuthenticatedUser();
	}

	@Override
	protected void parseItem(String uri, String localName, String qName) {
		if (!qName.equals(localName))
			qName = localName;

		if (qName.equals("success")) {
			getTempItem().setSuccess(tempValue);
		} else if (qName.equals("uid")) {
			if (!tempValue.equals(""))
				getTempItem().setUid(Integer.parseInt(tempValue));
		} else if (qName.equals("user")) {
			if (!tempValue.equals(""))
				getTempItem().setUsername(tempValue);
		} else if (qName.equals("passwordhash")) {
			if (!tempValue.equals(""))
				getTempItem().setPasswordHash(tempValue);
		}
	}

	@Override
	protected boolean isValidItem() {
		return true;
	}
}
