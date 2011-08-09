package de.consolewars.android.app;

import android.content.Context;
import android.util.Log;
import de.consolewars.api.API;
import de.consolewars.api.data.AuthStatus;
import de.consolewars.api.data.AuthenticatedUser;
import de.consolewars.api.exception.ConsolewarsAPIException;

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
 * CW-API wrapper. Should be used instead the actual {@link API}.
 * 
 * @author Alexander Dridiger
 */
public class APICaller {

	private Context context;
	private API api;

	/**
	 * Constructor to get an Android {@link Context}.
	 * 
	 * @param context
	 */
	public APICaller(Context context) {
		this.context = context;
		api = new API(context.getString(R.string.api_key));
	}

	/**
	 * Returns the actual CW API,´.
	 * 
	 * @return
	 */
	public API getApi() {
		return api;
	}

	/**
	 * Passes on the CW API Key to check receive authentication.
	 * 
	 * @throws ConsolewarsAPIException
	 */
	public void authenticateOnCW() throws ConsolewarsAPIException {
		AuthStatus status = api.checkAPIToken();
		Log.d(context.getString(R.string.log_authstatus_tag), status.getStatus());
	}

	/**
	 * Method wrapper for {@link API.authenticate(String username, String password)}.
	 * 
	 * @param username
	 * @param password
	 * @return
	 * @throws ConsolewarsAPIException
	 */
	public AuthenticatedUser getAuthUser(String username, String password) throws ConsolewarsAPIException {
		return api.authenticate(username, password);
	}
}
