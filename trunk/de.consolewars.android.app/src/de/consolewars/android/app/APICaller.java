package de.consolewars.android.app;

import java.util.Date;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.Singleton;

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
public class APICaller {

	@Inject
	private API api;

	public enum CommentArea {
		NEWS(Comment.AREA_NEWS),
		BLOGS(Comment.AREA_BLOGS);

		private int value;

		private CommentArea(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	/**
	 * Method wrapper for {@link API.authenticate(String username, String
	 * password)}.
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
	 * Method wrapper for {@link API.getMessages()}.
	 * 
	 * @param authenticatedUser
	 * @param filter
	 * @param count
	 * @return
	 * @throws ConsolewarsAPIException
	 */
	public List<Message> getMessages(AuthenticatedUser authenticatedUser, Filter filter, int count)
			throws ConsolewarsAPIException {
		return api.getMessages(authenticatedUser.getUid(), authenticatedUser.getPasswordHash(), filter.getFilter(),
				count);
	}
}
