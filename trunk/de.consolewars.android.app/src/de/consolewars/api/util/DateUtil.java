package de.consolewars.api.util;

import java.util.Date;

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
 * date and time operations
 * 
 * @author cerpin (arrewk@gmail.com)
 *
 */
public class DateUtil {
	
	public final static int SECOND = 1000;
	public final static int MINUTE = SECOND * 60;
	public final static long HOUR = MINUTE * 60;
	public final static long DAY = 24 * HOUR;
	public final static long WEEK = 7 * DAY;
	public final static long MONTH = 30 * DAY;
	
	/**
	 * relative time passed (current version with german time text)
	 * Example output: vor 34 Minuten (34 minutes ago)
	 * 
	 * @author cerpin (arrewk@gmail.com)
	 * @param date
	 * @return
	 */
	public static String timePassed(Date date) {
		Date now = new Date();
		long secondsPassed = now.getTime() - date.getTime();
		if(secondsPassed > MONTH) return getTimeText(secondsPassed / MONTH,MONTH);
		if(secondsPassed > WEEK) return getTimeText(secondsPassed / WEEK,WEEK);
		if(secondsPassed > DAY) return getTimeText(secondsPassed / DAY,DAY);
		if(secondsPassed > HOUR) return getTimeText(secondsPassed / HOUR,HOUR); 
		if(secondsPassed <= HOUR - MINUTE) return getTimeText(secondsPassed / MINUTE,MINUTE);
		
		// if(secondsPassed < MINUTE)
		return getTimeText(secondsPassed / SECOND,SECOND);
	}
	
	/**
	 * see timePassed(Date date)
	 * 
	 * @author cerpin (arrewk@gmail.com)
	 * @param unixtime timestamp/unixtime in seconds
	 * @return
	 */
	public static String timePassed(long unixtime) {
		return timePassed(new Date(unixtime * SECOND));
	}
	
	private static String getTimeText(long time, long period) {
		if(period == MONTH) {
			if(time == 1) return "vor einem Monat";
			else return "vor " + time + " Monate";
		}
		else if(period == WEEK) {
			if(time == 1) return "vor einer Woche";
			else return "vor " + time + " Wochen";
		}
		else if(period == DAY) {
			if(time == 1) return "vor einem Tag";
			else return "vor " + time  + " Tagen";
		}
		else if(period == HOUR) {
			if(time == 1) return "vor einer Stunde";
			else return "vor " + time  + " Stunden";
		}
		else if(period == MINUTE) {
			if(time == 1) return "vor einer Minute";
			else return "vor " + time  + " Minuten";
		}
		else {
			if(time < 30) return "jetzt";
			else return "vor " + time  + " Sekunden";
		}
	}

}
