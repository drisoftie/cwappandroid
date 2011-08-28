package de.consolewars.android.app;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.htmlcleaner.XPatherException;

import android.content.Context;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.consolewars.android.app.db.AppDataHandler;
import de.consolewars.android.app.util.HttpPoster;
import de.consolewars.android.app.util.MediaSnapper;
import de.consolewars.api.API;
import de.consolewars.api.data.AuthenticatedUser;
import de.consolewars.api.data.Blog;
import de.consolewars.api.data.Comment;
import de.consolewars.api.data.Message;
import de.consolewars.api.data.News;
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
public class CWManager {

	@Inject
	private API api;
	@Inject
	private AppDataHandler appDataHandler;
	@Inject
	private CWLoginManager cwLoginManager;
	@Inject
	private HttpPoster httpPoster;
	@Inject
	private Context context;

	private List<News> news;
	private List<Blog> blogs;
	private List<Message> msgs;
	private int newestNews = -1;
	private int newestBlog = -1;

	public enum CommentArea {
		NEWS(Comment.AREA_NEWS), BLOGS(Comment.AREA_BLOGS);

		private int value;

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
	 * @throws ConsolewarsAPIException
	 */
	public AuthenticatedUser getAuthUser(String username, String password) throws ConsolewarsAPIException {
		return api.authenticate(username, password);
	}

	/**
	 * Gets entities (news, blogs, messages) from database or downloads them by {@link API} based on user information.
	 * 
	 * @throws ConsolewarsAPIException
	 */
	public void setupEntities() throws ConsolewarsAPIException {
		// if (appDataHandler.getLastNewsID() > 0 && getNews(1, Filter.NEWS_ALL, null).size() == 1) {
		// News newNews = getNews(1, Filter.NEWS_ALL, null).get(0);
		// this.newestNews = newNews.getId();
		// }
		getNewsAndStore(100, Filter.NEWS_ALL, null);
		getBlogsAndStore(100, Filter.BLOGS_NEWS, null);
		getMessagesAndStore(Filter.MSGS_INBOX, 100);
	}

	/**
	 * @return the blogs
	 */
	public List<Blog> getBlogs() {
		if (blogs == null) {
			blogs = new ArrayList<Blog>();
		}
		return blogs;
	}

	public List<Blog> getBlogsAndStore(int count, Filter filter, Date date) {
		try {
			blogs = getBlogs(count, filter, date);
		} catch (ConsolewarsAPIException e) {
			e.printStackTrace();
		}
		return getBlogs();
	}

	/**
	 * Method wrapper for {@link API.getBlogsList()}.
	 * 
	 * @param count
	 * @param filter
	 * @param date
	 * @return
	 * @throws ConsolewarsAPIException
	 */
	public List<Blog> getBlogs(int count, Filter filter, Date date) throws ConsolewarsAPIException {
		return api.getBlogsList(-1, count, filter.getFilter(), date);
	}

	/**
	 * Method wrapper for {@link API.getBlogsList()}.
	 * 
	 * @param count
	 * @param date
	 * @return
	 * @throws ConsolewarsAPIException
	 */
	public List<Blog> getUserBlogs(int userId, int count, Date date) throws ConsolewarsAPIException {
		return api.getBlogsList(userId, count, Filter.BLOGS_USER.getFilter(), date);
	}

	/**
	 * Method wrapper for {@link API.getBlog(int id)}.
	 * 
	 * @param blogId
	 * @return
	 * @throws ConsolewarsAPIException
	 */
	public Blog getBlogById(int blogId) throws ConsolewarsAPIException {
		return api.getBlog(blogId);
	}

	/**
	 * Method wrapper for {@link API.getComments()}.
	 * 
	 * @param objectId
	 * @param area
	 * @param count
	 * @param viewPage
	 * @return
	 * @throws ConsolewarsAPIException
	 */
	public List<Comment> getComments(int objectId, int area, int count, int viewPage) throws ConsolewarsAPIException {
		return api.getComments(objectId, area, count, viewPage, -1);
	}

	/**
	 * @return the news
	 */
	public List<News> getNews() {
		if (news == null) {
			news = new ArrayList<News>();
		}
		return news;
	}

	public List<News> getNewsAndStore(int count, Filter filter, Date date) {
		try {
			news = getNews(count, filter, date);
		} catch (ConsolewarsAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return getNews();
	}

	/**
	 * Method wrapper for {@link API.getNews()}.
	 * 
	 * @param count
	 * @param filter
	 * @param date
	 * @return
	 * @throws ConsolewarsAPIException
	 */
	public List<News> getNews(int count, Filter filter, Date date) throws ConsolewarsAPIException {
		return api.getNewsList(50, filter.getFilter(), date);
	}

	/**
	 * @return the msgs
	 */
	public List<Message> getMsgs() {
		if (msgs == null) {
			msgs = new ArrayList<Message>();
		}
		return msgs;
	}

	public List<Message> getMessagesAndStore(Filter filter, int count) {
		try {
			msgs = getMessages(filter, count);
		} catch (ConsolewarsAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return getMsgs();
	}

	/**
	 * Method wrapper for {@link API.getMessages()}.
	 * 
	 * @param authenticatedUser
	 * @param filter
	 * @param count
	 * @return
	 * @throws ConsolewarsAPIException
	 */
	public List<Message> getMessages(Filter filter, int count) throws ConsolewarsAPIException {
		if (cwLoginManager.isLoggedIn()) {
			return api.getMessages(cwLoginManager.getUser().getUid(), cwLoginManager.getUser().getPasswordHash(),
					filter.getFilter(), count);
		} else {
			return new ArrayList<Message>();
		}
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
			String cookies = context.getString(R.string.cw_cookie_full, cwLoginManager.getUser().getUid(),
					cwLoginManager.getUser().getPasswordHash());
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
			String cookies = context.getString(R.string.cw_cookie_full, cwLoginManager.getUser(), cwLoginManager
					.getUser().getPasswordHash());
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
							.getString(R.string.cw_cookie_full, cwLoginManager.getUser().getUid(), cwLoginManager
									.getUser().getPasswordHash()));
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
				String url = context.getString(R.string.cw_blogpost_url,
						URLEncoder.encode(cwLoginManager.getUser().getUsername(), context.getString(R.string.utf8)));
				String cookies = context.getString(R.string.cw_cookie_full, cwLoginManager.getUser().getUid(),
						cwLoginManager.getUser().getPasswordHash());
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
				String url = context.getString(R.string.cw_blogpost_url, cwLoginManager.getUser().getUsername());
				String cookies = context.getString(R.string.cw_cookie_full, cwLoginManager.getUser().getUid(),
						cwLoginManager.getUser().getPasswordHash());
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
						R.string.cw_cookie_full, cwLoginManager.getUser().getUid(), cwLoginManager.getUser()
								.getPasswordHash()), context.getString(R.string.cw_cmmt_submit_data, area, objectId,
						context.getString(R.string.cw_command_newentry),
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
						R.string.cw_cookie_full, cwLoginManager.getUser().getUid(), cwLoginManager.getUser()
								.getPasswordHash()), context.getString(R.string.cw_cmmt_delete_data, area, objectId,
						commentId, context.getString(R.string.cw_command_remove), 1));
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
						R.string.cw_cookie_full, cwLoginManager.getUser().getUid(), cwLoginManager.getUser()
								.getPasswordHash()), context.getString(R.string.cw_cmmt_edit_data, area, objectId,
						commentId, context.getString(R.string.cw_command_update),
						URLEncoder.encode(newText, context.getString(R.string.utf8)), 1));
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
}
