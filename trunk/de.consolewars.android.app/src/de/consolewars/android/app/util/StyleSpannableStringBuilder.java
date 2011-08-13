package de.consolewars.android.app.util;

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;

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
 * Easy to use {@link SpannableStringBuilder} to format text.
 * 
 * @author Alexander Dridiger
 */
public class StyleSpannableStringBuilder extends SpannableStringBuilder {

	/**
	 * Sets a style for the given {@link CharSequence}.
	 * 
	 * @param c
	 *            the style to set
	 * @param text
	 *            text to be styled
	 * @return the string builder containing the styled text
	 */
	public StyleSpannableStringBuilder appendWithStyle(CharacterStyle c, CharSequence text) {
		super.append(text);
		int startPos = length() - text.length();
		setSpan(c, startPos, length(), 0);
		return this;
	}

	/**
	 * Convenient method to append bold style to a given text.
	 * 
	 * @param text
	 *            the text to become bold
	 * @return the string builder containing the styled text
	 */
	public StyleSpannableStringBuilder appendBold(CharSequence text) {
		return appendWithStyle(new StyleSpan(Typeface.BOLD), text);
	}
}
