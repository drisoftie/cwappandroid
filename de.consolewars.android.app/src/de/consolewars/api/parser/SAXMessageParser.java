package de.consolewars.api.parser;

import de.consolewars.api.data.Message;

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
	
	public SAXMessageParser(String APIURL) {
		super(APIURL);
	}

	@Override
	protected Message createTempItem() {
		return new Message();
	}

	@Override
	protected boolean isValidItem() {
		return true;
	}

	@Override
	protected void parseItem(String uri, String localName, String qName) {
		if(!qName.equals(localName))
			qName = localName; 
		
		if(qName.equals("title")) {
			getTempItem().setTitle(tempValue);
		}
		else if(qName.equals("id")) {
			getTempItem().setId(Integer.parseInt(tempValue));
		}
		else if(qName.equals("tid")) {
			getTempItem().setTid(Integer.parseInt(tempValue));
		}
		else if(qName.equals("mode")) {
			getTempItem().setMode(tempValue);
		}
		else if(qName.equals("fromusername")) {
			getTempItem().setFromusername(tempValue);
		}
		else if(qName.equals("fromuserid")) {
			getTempItem().setFromuserid(Integer.parseInt(tempValue));
		}
		else if(qName.equals("tousername")) {
			getTempItem().setTousername(tempValue);
		}
		else if(qName.equals("touserid")) {
			getTempItem().setTouserid(Integer.parseInt(tempValue));
		}
		else if(qName.equals("message")) {
			getTempItem().setMessage(tempValue);
		}
		else if(qName.equals("origmessage")) {
			getTempItem().setOrigmessage(tempValue);
		}
		else if(qName.equals("unixtime")) {
			getTempItem().setUnixtime(Integer.parseInt(tempValue));
		}
		else if(qName.equals("messageread")) {
			boolean messageRead = Integer.parseInt(tempValue) >= 1;
			getTempItem().setMessageread(messageRead);
		}
	}
	
}
