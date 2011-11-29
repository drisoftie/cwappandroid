package de.consolewars.android.app;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.htmlcleaner.XPatherException;

import android.content.Context;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.consolewars.android.app.db.domain.CwBlog;
import de.consolewars.android.app.db.domain.CwMessage;
import de.consolewars.android.app.db.domain.CwNews;
import de.consolewars.android.app.parser.BlogsParser;
import de.consolewars.android.app.parser.CommentsParser;
import de.consolewars.android.app.parser.CommentsRoot;
import de.consolewars.android.app.parser.MessagesParser;
import de.consolewars.android.app.parser.NewsParser;
import de.consolewars.android.app.util.HttpPoster;
import de.consolewars.android.app.util.MediaSnapper;
import de.consolewars.api.API;
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
@Singleton
public class CwManager {

	@Inject
	private API api;
	@Inject
	private NewsParser newsParser;
	@Inject
	private BlogsParser blogsParser;
	@Inject
	private CommentsParser commentsParser;
	@Inject
	private MessagesParser messagesParser;
	@Inject
	private CwLoginManager cwLoginManager;
	@Inject
	private HttpPoster httpPoster;
	@Inject
	private Context context;

	public enum CommentArea {
		NEWS(CommentArea.AREA_NEWS), BLOGS(CommentArea.AREA_BLOGS);

		private int value;

		public final static int AREA_NEWS = 11;
		public final static int AREA_BLOGS = 101;

		private CommentArea(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	/**
	 * Method wrapper for {@link API.authenticate(String username, String password)}.
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	public AuthenticatedUser getAuthUser(String username, String password) {
		try {
			return api.authenticate(username, password);
		} catch (ConsolewarsAPIException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Method wrapper for {@link API.getBlogs()}.
	 * 
	 * @param id
	 * @return
	 * @see de.consolewars.api.API#getBlogs(int[])
	 */
	public List<CwBlog> getBlogsByIds(int[] id) {
		try {
			return blogsParser.parse(id);
		} catch (ConsolewarsAPIException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ArrayList<CwBlog>();
	}

	/**
	 * Method wrapper for {@link API.getBlogsList()}.
	 * 
	 * @param count
	 * @param filter
	 * @param date
	 * @return
	 */
	public List<CwBlog> getBlogs(int count, Filter filter, Date date) {
		try {
			return api.getBlogsList(-1, count, filter.getFilter(), date);
		} catch (ConsolewarsAPIException e) {
			e.printStackTrace();
		}
		return new ArrayList<CwBlog>();
	}

	/**
	 * Method wrapper for {@link API.getBlogsList()}.
	 * 
	 * @param count
	 * @param date
	 * @return
	 */
	public List<CwBlog> getUserBlogs(int userId, int count, Date date) {
		try {
			return api.getBlogsList(userId, count, Filter.BLOGS_USER.getFilter(), date);
		} catch (ConsolewarsAPIException e) {
			e.printStackTrace();
		}
		return new ArrayList<CwBlog>();
	}

	/**
	 * Method wrapper for {@link API.getBlog(int id)}.
	 * 
	 * @param blogId
	 * @return
	 */
	public CwBlog getBlogById(int blogId) {
		try {
			return api.getBlog(blogId);
		} catch (ConsolewarsAPIException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Method wrapper for {@link API.getComments()}.
	 * 
	 * @param objectId
	 * @param area
	 * @param count
	 * @param viewPage
	 * @return
	 */
	public CommentsRoot getComments(int objectId, int area, int count, int viewPage) {
		try {
			return commentsParser.parse(objectId, area, count, viewPage, -1);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ConsolewarsAPIException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Method wrapper for {@link API.getNewsList()}.
	 * 
	 * @param count
	 * @param filter
	 * @param date
	 * @return
	 */
	public List<CwNews> getNews(int count, Filter filter, Date date) {
		try {
			return api.getNewsList(count, filter.getFilter(), date);
		} catch (ConsolewarsAPIException e) {
			e.printStackTrace();
		}
		return new ArrayList<CwNews>();
	}

	/**
	 * Method wrapper for {@link API.getNews()}.
	 * 
	 * @param id
	 * @return
	 * @see de.consolewars.api.API#getNews(int)
	 */
	public CwNews getNewsById(int id) {
		try {
			return api.getNews(id);
		} catch (ConsolewarsAPIException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Method wrapper for {@link API.getNews()}.
	 * 
	 * @param id
	 * @return
	 * @see de.consolewars.api.API#getNews(int[])
	 */
	public List<CwNews> getNewsByIds(int[] id) {
		try {
			return newsParser.parse(id);
		} catch (ConsolewarsAPIException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ArrayList<CwNews>();
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param filter
	 * @param count
	 * @return
	 */
	public List<CwMessage> getMessages(Filter filter, int count) {
		if (cwLoginManager.isLoggedIn()) {
			try {
				return messagesParser.parse(cwLoginManager.getAuthenticatedUser().getUid(), cwLoginManager
						.getAuthenticatedUser().getPasswordHash(), filter.getFilter(), count);
			} catch (ConsolewarsAPIException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			return new ArrayList<CwMessage>();
		}
		return null;
	}

	/**
	 * @param message
	 * @param recipient
	 * @param title
	 * @param saveCopy
	 * @param parseUrl
	 * @param showSignature
	 * @param disableSmilies
	 * @param receipt
	 * @return
	 */
	public boolean sendMessage(String message, String recipient, String title, boolean saveCopy, boolean parseUrl,
			boolean showSignature, boolean disableSmilies, boolean receipt) {
		String securityToken = getSecurityToken();

		try {
			String url = context.getString(R.string.cw_message_url);
			String cookies = context.getString(R.string.cw_cookie_full, cwLoginManager.getAuthenticatedUser().getUid(),
					cwLoginManager.getAuthenticatedUser().getPasswordHash());
			String data = context.getString(R.string.cw_mssg_submit_data,
					URLEncoder.encode(message, context.getString(R.string.utf8)),
					URLEncoder.encode(recipient, context.getString(R.string.utf8)), saveCopy ? 1 : 0,
					URLEncoder.encode(title, context.getString(R.string.utf8)), parseUrl ? 1 : 0, securityToken,
					showSignature ? 1 : 0, disableSmilies ? 1 : 0, receipt ? 1 : 0);
			httpPoster.sendPost(url, cookies, data);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * @param messageId
	 * @return
	 */
	public boolean deleteMessage(int messageId) {
		return deleteMessages(Arrays.asList(new Integer[] { messageId }));
	}

	/**
	 * @param messageIds
	 * @return
	 */
	public boolean deleteMessages(List<Integer> messageIds) {
		String securityToken = getSecurityToken();

		try {
			String url = context.getString(R.string.cw_message_url);
			String cookies = context.getString(R.string.cw_cookie_full, cwLoginManager.getAuthenticatedUser(),
					cwLoginManager.getAuthenticatedUser().getPasswordHash());
			List<String> pms = new ArrayList<String>();
			for (Integer messageId : messageIds) {
				pms.add(context.getString(R.string.cw_mssg_delete_param, messageId));
			}
			String data = context.getString(R.string.cw_mssg_delete_data, StringUtils.join(pms, "&"), securityToken);
			httpPoster.sendPost(url, cookies, data);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * @return
	 */
	private String getSecurityToken() {
		String securityToken = "";
		try {
			securityToken = MediaSnapper.snapWithCookies(context, context.getString(R.string.xpath_get_securitytoken),
					context.getString(R.string.value), context.getString(R.string.cw_getsecuritytoken_url), context
							.getString(R.string.cw_cookie_full, cwLoginManager.getAuthenticatedUser().getUid(),
									cwLoginManager.getAuthenticatedUser().getPasswordHash()));
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (XPatherException e1) {
			e1.printStackTrace();
		}
		return securityToken;
	}

	/**
	 * @param title
	 * @param content
	 * @param date
	 * @param time
	 * @param allowCmts
	 * @param tags
	 * @param isVisible
	 * @param isNewsblog
	 * @param blogID
	 *            optional
	 * @return
	 */
	public boolean sendBlog(String title, String content, String date, String time, boolean allowCmts, String tags,
			boolean isVisible, boolean isNewsblog, int blogID) {
		if (cwLoginManager.isLoggedIn()) {
			try {
				String url = context.getString(
						R.string.cw_blogpost_url,
						URLEncoder.encode(cwLoginManager.getAuthenticatedUser().getUsername(),
								context.getString(R.string.utf8)));
				String cookies = context.getString(R.string.cw_cookie_full, cwLoginManager.getAuthenticatedUser()
						.getUid(), cwLoginManager.getAuthenticatedUser().getPasswordHash());
				String data = context.getString(R.string.cw_blog_submit_data, URLEncoder.encode(title,
						context.getString(R.string.utf8)),
						URLEncoder.encode(content, context.getString(R.string.utf8)), URLEncoder.encode(date,
								context.getString(R.string.utf8)), URLEncoder.encode(time,
								context.getString(R.string.utf8)), allowCmts ? 1 : 0, URLEncoder.encode(tags,
								context.getString(R.string.utf8)), isVisible ? 1 : 0, isNewsblog ? 1 : 0, blogID);
				httpPoster.sendPost(url, cookies, data);
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public boolean deleteBlog(int id, String command) {
		if (cwLoginManager.isLoggedIn()) {
			try {
				String url = context.getString(R.string.cw_blogpost_url, cwLoginManager.getAuthenticatedUser()
						.getUsername());
				String cookies = context.getString(R.string.cw_cookie_full, cwLoginManager.getAuthenticatedUser()
						.getUid(), cwLoginManager.getAuthenticatedUser().getPasswordHash());
				String data = context.getString(R.string.cw_blog_delete_data, id,
						URLEncoder.encode(command, context.getString(R.string.utf8)));
				httpPoster.sendPost(url, cookies, data);
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * @param comment
	 * @param objectId
	 * @param area
	 * @return
	 */
	public boolean sendComment(String comment, int objectId, int area) {
		if (cwLoginManager.isLoggedIn()) {
			try {
				httpPoster.sendPost(context.getString(R.string.cw_posting_url), context.getString(
						R.string.cw_cookie_full, cwLoginManager.getAuthenticatedUser().getUid(), cwLoginManager
								.getAuthenticatedUser().getPasswordHash()), context.getString(
						R.string.cw_cmmt_submit_data, area, objectId, context.getString(R.string.cw_command_newentry),
						URLEncoder.encode(comment, context.getString(R.string.utf8)), 1));
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * @param commentId
	 * @param objectId
	 * @param area
	 * @return
	 */
	public boolean deleteComment(int commentId, int objectId, int area) {
		if (cwLoginManager.isLoggedIn()) {
			try {
				httpPoster.sendPost(context.getString(R.string.cw_posting_url), context.getString(
						R.string.cw_cookie_full, cwLoginManager.getAuthenticatedUser().getUid(), cwLoginManager
								.getAuthenticatedUser().getPasswordHash()), context.getString(
						R.string.cw_cmmt_delete_data, area, objectId, commentId,
						context.getString(R.string.cw_command_remove), 1));
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * @param newText
	 * @param commentId
	 * @param objectId
	 * @param area
	 * @return
	 */
	public boolean updateComment(String newText, int commentId, int objectId, int area) {
		if (cwLoginManager.isLoggedIn()) {
			try {
				httpPoster.sendPost(context.getString(R.string.cw_posting_url), context.getString(
						R.string.cw_cookie_full, cwLoginManager.getAuthenticatedUser().getUid(), cwLoginManager
								.getAuthenticatedUser().getPasswordHash()), context.getString(
						R.string.cw_cmmt_edit_data, area, objectId, commentId,
						context.getString(R.string.cw_command_update),
						URLEncoder.encode(newText, context.getString(R.string.utf8)), 1));
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
}
