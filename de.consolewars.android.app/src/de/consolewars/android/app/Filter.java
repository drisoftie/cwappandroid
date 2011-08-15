package de.consolewars.android.app;

import de.consolewars.api.data.Blog;
import de.consolewars.api.data.Message;
import de.consolewars.api.data.News;

/**
 * Filter constants.
 * 
 * @author w4yn3
 */
public enum Filter {
	BLOGS_NORMAL(0, Blog.FILTER_NORMAL),
	BLOGS_NEWS(1, Blog.FILTER_NEWS),
	BLOGS_USER(2, Blog.FILTER_UID),
	MSGS_INBOX(0, Message.FOLDER_INBOX),
	MSGS_OUTBOX(1, Message.FOLDER_SENT),
	NEWS_ALL(0, 0),
	NEWS_MS(1, News.FILTER_MICROSOFT_ONLY),
	NEWS_NIN(2, News.FILTER_NINTENDO_ONLY),
	NEWS_SONY(3, News.FILTER_SONY_ONLY);

	private int position;
	private int filter;

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