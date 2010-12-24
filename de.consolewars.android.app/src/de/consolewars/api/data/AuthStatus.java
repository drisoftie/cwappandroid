package de.consolewars.api.data;

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
 */
public class AuthStatus {
	
	private String status;
	private String reason;
	
	public AuthStatus(String status, String reason) {
		this.status = status;
		this.reason = "";
	}
	
	public AuthStatus(String status) {
		this(status,"");
	}
	
	public AuthStatus() {
		this("","");
	}
	
	public String getReason() {
		return reason;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setReason(String reason) {
		this.reason = reason;
	}
	
	public String toString() {
		String sAuthstatus = "status: " + status;
		if(reason.length() > 0) {
			sAuthstatus += ", reason: " + reason;
		}
		return sAuthstatus;
	}

}
