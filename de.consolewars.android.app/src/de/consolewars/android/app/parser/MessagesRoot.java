package de.consolewars.android.app.parser;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import de.consolewars.android.app.db.domain.CwMessage;

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
 */
@Root(name = "root", strict = false)
public class MessagesRoot {

	@ElementList(inline = true)
	private List<CwMessage> messages;

	/**
	 * @return the messages
	 */
	public List<CwMessage> getMessages() {
		if (messages == null) {
			messages = new ArrayList<CwMessage>();
		}
		return messages;
	}

	/**
	 * @param messages
	 *            the messages to set
	 */
	public void setMessages(List<CwMessage> messages) {
		this.messages = messages;
	}
}
