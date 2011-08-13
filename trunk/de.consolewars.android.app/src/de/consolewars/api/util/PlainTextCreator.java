package de.consolewars.api.util;

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

public class PlainTextCreator {

	private final static String ahrefRegex = "<a href=\"([^\"]*)\" [^>]*>";
	private final static String imgRegex = "<img .?src=\"([^\"]*)\"[^>]*>";

	public static String getPlainText(String richtext) {
		richtext = richtext.replaceAll("<(p|b|u|strong)( class=\"[^\"]*\")?>", "");
		richtext = richtext.replaceAll("</(a|b|u|strong)>", "");
		richtext = richtext.replaceAll("&nbsp;", " ");
		richtext = richtext.replaceAll(ahrefRegex, "Link: $1\n");
		richtext = richtext.replaceAll(imgRegex, "Image: $1\n");
		richtext = richtext.replaceAll("</p>", "\n");
		richtext = replaceEmbedYouTubeLink(richtext);
		richtext = replaceSpecialChars(richtext);
		richtext = richtext.replaceAll("<!--.*-->", "");
		return richtext;
	}

	private static String replaceSpecialChars(String text) {
		text = text.replaceAll("&uuml;", "ü");
		text = text.replaceAll("&ouml;", "ö");
		text = text.replaceAll("&auml;", "ä");
		text = text.replaceAll("&szlig;", "ß");
		return text;
	}

	private static String replaceEmbedYouTubeLink(String text) {
		text = text.replaceAll("http://de.<object [^>]*>", "");
		text = text.replaceAll("<param [^>]*>", "");
		text = text.replaceAll("</(object|param|embed)>", "");
		text = text.replaceAll("<embed src=\"([^\"]*)\"[^>]*>", "youtube: $1");
		return text;
	}

}
