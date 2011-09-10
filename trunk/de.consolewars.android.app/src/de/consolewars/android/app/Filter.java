package de.consolewars.android.app;

import de.consolewars.api.data.Message;

/**
 * Filter constants.
 * 
 * @author w4yn3
 */
public enum Filter {
	BLOGS_NORMAL(0, Filter.FILTER_NORMAL),
	BLOGS_NEWS(1, Filter.FILTER_NEWS),
	BLOGS_USER(2, Filter.FILTER_UID),
	MSGS_INBOX(0, Message.FOLDER_INBOX),
	MSGS_OUTBOX(1, Message.FOLDER_SENT),
	NEWS_ALL(0, 0),
	NEWS_MS(1, Filter.FILTER_MICROSOFT_ONLY),
	NEWS_NIN(2, Filter.FILTER_NINTENDO_ONLY),
	NEWS_SONY(3, Filter.FILTER_SONY_ONLY);

	private int position;
	private int filter;
	
	public final static int FILTER_MICROSOFT_ONLY = 1;
	public final static int FILTER_NINTENDO_ONLY = 2;
	public final static int FILTER_SONY_ONLY = 3;
	
	public final static int FILTER_NORMAL = 0;
	public final static int FILTER_NEWS = 1;
	public final static int FILTER_UID = 2;

	private Filter(int position, int filter) {
		this.position = position;
		this.filter = filter;
	}

	public int getPosition() {
		return position;
	}

	public int getFilter() {
		return filter;
	}
}