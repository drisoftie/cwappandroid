package de.consolewars.api.util;

import java.util.ArrayList;

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
 * this class is for comfortable creating of API-URLs
 * 
 * @author cerpin (arrewk@gmail.com)
 *
 */
public class URLCreator {
	
	private ArrayList<URLArgument> arguments;
	private String baseURL;
	
	public URLCreator(String baseURL) {
		this.arguments = new ArrayList<URLArgument>();
		this.baseURL = baseURL;
	}
	
	public void addArgument(String name, String value) {
		arguments.add(new URLArgument(name,value));
	}
	
	public void addArgument(String name, int value) {
		this.addArgument(name,value + "");
	}
	
	public void addArgument(String name, int[] values) {
		String strValues = "";
		for(int i = 0; i < values.length; i++) {
			strValues += values[i];
			if(i != values.length -1) {
				strValues += ",";
			}
		}
		arguments.add(new URLArgument(name,strValues));
	}
	
	public String toString() {
		String url = baseURL;
		if(arguments.size() > 0) {
			url += "?";
		}
		
		for(int i = 0; i < arguments.size(); i++) {
			url += arguments.get(i);
			if(i != arguments.size() - 1) {
				url += "&";
			}
		}
		
		return url;		
	}
	
	private class URLArgument {
		
		private String name;
		private String value;
		
		public URLArgument(String name, String value) {
			this.name = name;
			this.value = value;
		}
		
		@SuppressWarnings("unused")
		public String getName() {
			return name;
		}
		
		@SuppressWarnings("unused")
		public String getValue() {
			return value;
		}
		
		public String toString() {
			return name + "=" + value;
		}
	}
}
