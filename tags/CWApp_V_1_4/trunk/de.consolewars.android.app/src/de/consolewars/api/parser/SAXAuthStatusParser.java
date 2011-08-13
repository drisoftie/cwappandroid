package de.consolewars.api.parser;

import de.consolewars.api.data.AuthStatus;

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
public class SAXAuthStatusParser extends AbstractSAXParser<AuthStatus> {

	public SAXAuthStatusParser(String APIURL) {
		super(APIURL);
	}

	@Override
	protected AuthStatus createTempItem() {
		return new AuthStatus();
	}

	@Override
	protected void parseItem(String uri, String localName, String name) {
		// nothing to do
	}

	@Override
	protected boolean isValidItem() {
		return false;
	}
}
