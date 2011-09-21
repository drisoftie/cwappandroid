package de.consolewars.android.app;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.htmlcleaner.XPatherException;

import android.content.Context;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.consolewars.android.app.db.AppDataHandler;
import de.consolewars.android.app.db.domain.CwBlog;
import de.consolewars.android.app.db.domain.CwComment;
import de.consolewars.android.app.db.domain.CwNews;
import de.consolewars.android.app.db.domain.CwPicture;
import de.consolewars.android.app.db.domain.CwUser;
import de.consolewars.android.app.util.CwBlogsIdSorter;
import de.consolewars.android.app.util.CwCommentsIdSorter;
import de.consolewars.android.app.util.CwNewsIdSorter;
import de.consolewars.android.app.util.MediaSnapper;
import de.consolewars.api.data.Message;

/*
 * Copyright [2011] [Alexander Dridiger]
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
 * Consolidates the {@link CwManager} and the {@link AppDataHandler}. It also merges entities coming from the
 * {@link CwManager} and database.
 * 
 * @author Alexander Dridiger
 */
@Singleton
public class CwEntityManager {

	@Inject
	private Context context;
	@Inject
	private CwManager cwManager;
	@Inject
	private CwLoginManager cwLoginManager;
	@Inject
	private AppDataHandler appDataHandler;

	private List<CwNews> news;
	private List<CwBlog> blogs;
	private List<CwBlog> userBlogs;
	private List<Message> msgs;
	private int newestNews = -1;
	private int newestBlog = -1;

	private CwNews selectedNews;
	private CwBlog selectedBlog;

	private CwNewsIdSorter cwNewsIdSorter = new CwNewsIdSorter();
	private CwBlogsIdSorter cwBlogsIdSorter = new CwBlogsIdSorter();
	private CwCommentsIdSorter cwCommentsIdSorter = new CwCommentsIdSorter();

	/**
	 * Determines how retaining of entities is handled.
	 */
	public enum EntityRefinement {
		/**
		 * Only entities from the cache are retained.
		 */
		CACHE_ONLY,
		/**
		 * Saved and cached entities are retained.
		 */
		MIXED,
		/**
		 * Only saved entities are retained.
		 */
		SAVED_ONLY;
	}

	/**
	 * @param id
	 * @param fromSavedBlogs
	 * @return
	 */
	public CwBlog getSingleBlog(int id, boolean fromSavedBlogs) {
		CwBlog blog = null;
		if (fromSavedBlogs) {
			for (CwBlog n : getCachedBlogs(Filter.BLOGS_NEWS)) {
				if (n.getSubjectId() == id && n.getArticle() != null) {
					return n;
				}
			}
			for (CwBlog n : getCachedBlogs(Filter.BLOGS_USER)) {
				if (n.getSubjectId() == id && n.getArticle() != null) {
					return n;
				}
			}
			try {
				blog = appDataHandler.loadSingleSavedBlog(id);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			if (blog != null && blog.getArticle() != null) {
				return blog;
			}
		}
		blog = cwManager.getBlogById(id);
		if (blog != null) {
			try {
				appDataHandler.createOrUpdateBlog(blog);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return blog;
	}

	public CwNews getSingleNews(int id, boolean fromSavedNews) {
		CwNews news = null;
		if (fromSavedNews) {
			for (CwNews n : getCachedNews()) {
				if (n.getSubjectId() == id && n.getArticle() != null) {
					return n;
				}
			}
			try {
				news = appDataHandler.loadSingleSavedNews(id);
				if (news != null && news.getArticle() != null) {
					return news;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		news = getSingleNews(id);
		if (news != null) {
			try {
				appDataHandler.createOrUpdateNews(news);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return news;
	}

	public CwNews getSingleNews(int id) {
		CwNews news = null;
		news = cwManager.getNewsById(id);
		return news;
	}

	public List<CwBlog> getBlogsAndCache(int count, Filter filter, Date date) {
		if (filter.equals(Filter.BLOGS_USER)) {
			userBlogs = getUserBlogsAndCache(cwLoginManager.getAuthenticatedUser().getUid(), count, date);
			return userBlogs;
		}
		blogs = cwManager.getBlogs(count, filter, date);
		return getCachedBlogs(filter);
	}

	public List<CwBlog> getUserBlogsAndCache(int userId, int count, Date date) {
		userBlogs = cwManager.getUserBlogs(userId, count, date);
		return getCachedBlogs(Filter.BLOGS_USER);
	}

	/**
	 * @return the blogs
	 */
	public List<CwBlog> getCachedBlogs(Filter filter) {
		if (filter.equals(Filter.BLOGS_USER)) {
			if (userBlogs == null) {
				userBlogs = new ArrayList<CwBlog>();
			}
			return userBlogs;
		} else {
			if (blogs == null) {
				blogs = new ArrayList<CwBlog>();
			}
			return blogs;
		}
	}

	/**
	 * @return the blogs
	 */
	public List<CwBlog> setCachedBlogs(List<CwBlog> setList, Filter filter) {
		if (filter.equals(Filter.BLOGS_USER)) {
			userBlogs = setList;
			return userBlogs;
		} else {
			blogs = setList;
			return blogs;
		}
	}

	/**
	 * @return the news
	 */
	public List<CwNews> getCachedNews() {
		if (news == null) {
			news = new ArrayList<CwNews>();
		}
		return news;
	}

	public List<CwBlog> getNextBlogs(EntityRefinement refinement, Filter filter) {
		if (refinement == EntityRefinement.CACHE_ONLY) {
			// TODO: Paging
		} else if (refinement.equals(EntityRefinement.MIXED)) {
			getBlogsMixed(10, filter);
		} else if (refinement.equals(EntityRefinement.SAVED_ONLY)) {
			getBlogsSaved(10, filter);
		}
		return getCachedBlogs(filter);
	}

	/**
	 * Downloads news and merges them with the ones from the database.
	 * 
	 * @return
	 */
	public List<CwBlog> getBlogsMixed(int amount, Filter filter) {
		int oldestBlog = getCachedBlogs(filter).isEmpty() ? -1 : getCachedBlogs(filter).get(
				getCachedBlogs(filter).size() - 1).getSubjectId();
		if (oldestBlog > -1) {
			// first the download
			setCachedBlogs(mergeBlogsLists(getCachedBlogs(filter), getBlogsByIDAndCache(oldestBlog, true, amount)),
					filter);
			// then the database
			if (appDataHandler.hasBlogsData()) {
				try {
					setCachedBlogs(
							mergeBlogsLists(getCachedBlogs(filter), appDataHandler.loadSavedBlogs(oldestBlog, true, 10)),
							filter);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} else {
			setCachedBlogs(
					mergeBlogsLists(getCachedBlogs(filter), getBlogsByIDAndCache(calculateNewestBlogId(), true, amount)),
					filter);
			List<CwBlog> loadedBlogs = null;
			try {
				loadedBlogs = appDataHandler.loadSavedBlogs(amount);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			if (loadedBlogs != null) {
				setCachedBlogs(mergeBlogsLists(getCachedBlogs(filter), loadedBlogs), filter);
			}
		}
		Collections.sort(getCachedBlogs(filter), Collections.reverseOrder(cwBlogsIdSorter));
		return getCachedBlogs(filter);
	}

	public List<CwNews> getBlogsSaved(int amount, Filter filter) {
		int oldestNews = news.isEmpty() ? -1 : news.get(news.size() - 1).getSubjectId();
		if (appDataHandler.hasNewsData()) {
			if (oldestNews > -1) {
				try {
					return appDataHandler.loadSavedNews(oldestNews, true, 10);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} else {
				try {
					return appDataHandler.loadSavedNews(amount);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public List<CwNews> getNextNews(EntityRefinement refinement) {
		if (refinement.equals(EntityRefinement.CACHE_ONLY)) {
			// TODO: Paging
		} else if (refinement.equals(EntityRefinement.MIXED)) {
			getNewsMixed(10);
		} else if (refinement.equals(EntityRefinement.SAVED_ONLY)) {
			return getNewsSaved(10);
		}
		return getCachedNews();
	}

	/**
	 * Downloads news and merges them with the ones from the database.
	 * 
	 * @return
	 */
	public List<CwNews> getNewsMixed(int amount) {
		int oldestNews = news.isEmpty() ? -1 : news.get(news.size() - 1).getSubjectId();
		if (oldestNews > -1) {
			// first the download
			getNewsByIDAndCache(oldestNews, true, amount);
			// then the database
			if (appDataHandler.hasNewsData()) {
				try {
					news = mergeNewsLists(Arrays.asList(news.toArray(new CwNews[0])),
							appDataHandler.loadSavedNews(oldestNews, true, 10));
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} else {
			getNewsByIDAndCache(calculateNewestNewsId(), true, amount);
			List<CwNews> loadedNews = null;
			try {
				loadedNews = appDataHandler.loadSavedNews(amount);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			if (loadedNews != null) {
				news = mergeNewsLists(Arrays.asList(news.toArray(new CwNews[0])), loadedNews);
			}
		}
		news = mergeNewsLists(Arrays.asList(news.toArray(new CwNews[0])), news);
		Collections.sort(news, Collections.reverseOrder(cwNewsIdSorter));
		return news;
	}

	public List<CwNews> getNewsSaved(int amount) {
		int oldestNews = news.isEmpty() ? -1 : news.get(news.size() - 1).getSubjectId();
		if (appDataHandler.hasNewsData()) {
			if (oldestNews > -1) {
				try {
					return appDataHandler.loadSavedNews(oldestNews, true, 10);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} else {
				try {
					return appDataHandler.loadSavedNews(amount);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	/**
	 * @param count
	 * @param filter
	 * @param date
	 * @return
	 */
	public List<CwNews> getNewsAndStore(int count, Filter filter, Date date) {
		news = cwManager.getNews(count, filter, date);
		if (news != null) {
			for (CwNews n : news) {
				try {
					appDataHandler.createOrUpdateNews(n);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return getCachedNews();
	}

	public List<CwBlog> getBlogsByIDAndCache(int startID, boolean desc, int amount) {
		if (startID < amount && startID > 0 && desc) {
			amount = startID;
		}
		int[] ids = computeIds(amount, startID, true);
		if (desc) {
			getCachedBlogs(Filter.BLOGS_NORMAL).addAll(cwManager.getBlogsByIds(ids));
		} else {
			getCachedBlogs(Filter.BLOGS_NORMAL).addAll(0, cwManager.getBlogsByIds(ids));
		}
		return getCachedBlogs(Filter.BLOGS_NORMAL);
	}

	public void getNewsByIDAndCache(int startId, boolean desc, int amount) {
		if (startId < amount && startId > 0 && desc) {
			amount = startId;
		}
		int[] ids = computeIds(amount, startId, desc);
		if (desc) {
			getCachedNews().addAll(cwManager.getNewsByIds(ids));
		} else {
			getCachedNews().addAll(0, cwManager.getNewsByIds(ids));
		}
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

	public List<Message> getMessagesAndCache(Filter filter, int count) {
		msgs = cwManager.getMessages(filter, count);
		return getMsgs();
	}

	public void loadSavedNews() {

	}

	public int saveAllNews() {
		int savecounter = 0;
		for (CwNews newsToSave : news) {
			try {
				appDataHandler.createOrUpdateNews(newsToSave);
				savecounter++;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return savecounter;
	}

	public int saveAllBlogs() {
		int savecounter = 0;
		List<CwBlog> blogsToSave = blogs;
		blogsToSave = mergeBlogsLists(blogsToSave, userBlogs);
		for (CwBlog blog : blogsToSave) {
			try {
				appDataHandler.createOrUpdateBlog(blog);
				savecounter++;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return savecounter;
	}

	public int saveWholeCwNews(int startId) {
		int amount = 0;
		if (startId < 50 && startId > 0) {
			amount = startId;
		}
		int[] ids = new int[amount];
		for (int i = 0; i < amount; i++) {
			// if descending, reduce -1 to the startID etc., else add +1
			ids[i] = startId;
		}
		List<CwNews> saveNews = cwManager.getNewsByIds(ids);
		int savecounter = 0;
		for (CwNews newsToSave : saveNews) {
			try {
				appDataHandler.createOrUpdateNews(newsToSave);
				savecounter++;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return savecounter;
	}

	public CwNews saveLoadNews(CwNews news) {
		try {
			appDataHandler.createOrUpdateNews(news);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			return appDataHandler.loadSingleSavedNews(news.getSubjectId());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return news;
	}

	/**
	 * @param objectId
	 * @param area
	 * @param count
	 * @param viewPage
	 * @return
	 */
	public List<CwComment> getComments(int objectId, int area, int count, int viewPage) {
		List<CwComment> list1 = cwManager.getComments(objectId, area, count, viewPage);
		List<CwComment> list2 = Arrays.asList(list1.toArray(new CwComment[0]));
		list1 = mergeCommentsLists(list1, list2);
		Collections.sort(list1, cwCommentsIdSorter);
		return list1;
	}

	public void discardAllNews() {
		news = new ArrayList<CwNews>();
	}

	public void discardAllBlogs() {
		blogs = new ArrayList<CwBlog>();
		userBlogs = new ArrayList<CwBlog>();
	}

	public List<CwPicture> getPictures(String url) {
		List<CwPicture> pictures = new ArrayList<CwPicture>();
		String pictureUrls = "";
		try {
			pictureUrls = MediaSnapper.snapPicsUrl(url, context.getString(R.string.xpath_get_pictures),
					context.getString(R.string.xpath_pictures_filter));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XPatherException e) {
			e.printStackTrace();
		}
		if (!pictureUrls.equals("")) {
			// first get the string between both seperators, trim it, remove all "'"
			String stringUrls = StringUtils.remove(StringUtils.trim(StringUtils.substringBefore(
					StringUtils.substringAfter(pictureUrls, context.getString(R.string.xpath_pictures_filter)), ");")),
					"'");
			String[] urls = StringUtils.split(stringUrls, ",");
			for (String urlpart : urls) {
				CwPicture picture = new CwPicture();
				picture.setThumbUrl(context.getString(R.string.cw_url_append, urlpart));
				picture.setUrl(context.getString(R.string.cw_url_append,
						StringUtils.substringBefore(urlpart, "&database")));
				pictures.add(picture);
			}
		}
		return pictures;
	}

	/**
	 * @return the selectedNews
	 */
	public CwNews getSelectedNews() {
		return selectedNews;
	}

	/**
	 * @param selectedNews
	 *            the selectedNews to set
	 */
	public void setSelectedNews(CwNews selectedNews) {
		this.selectedNews = selectedNews;
	}

	/**
	 * @return the selectedBlog
	 */
	public CwBlog getSelectedBlog() {
		return selectedBlog;
	}

	/**
	 * @param selectedBlog
	 *            the selectedBlog to set
	 */
	public void setSelectedBlog(CwBlog selectedBlog) {
		this.selectedBlog = selectedBlog;
	}

	/**
	 * @return the newestNews
	 */
	public int getNewestNews() {
		return newestNews;
	}

	/**
	 * @return the newestBlog
	 */
	public int getNewestBlog() {
		return newestBlog;
	}

	/**
	 * Merges two news lists possibly containing duplicates into one list without any duplicates.
	 * 
	 * @param list1
	 * @param list2
	 * @return list1 with all merged entities
	 */
	private List<CwNews> mergeNewsLists(List<CwNews> list1, List<CwNews> list2) {
		Set<CwNews> setboth = new HashSet<CwNews>(list1);
		setboth.addAll(list2);
		list1 = new ArrayList<CwNews>();
		list1.addAll(setboth);
		return list1;
	}

	/**
	 * Merges two blogs lists possibly containing duplicates into one list without any duplicates.
	 * 
	 * @param list1
	 * @param list2
	 * @return list1 with all merged entities
	 */
	private List<CwBlog> mergeBlogsLists(List<CwBlog> list1, List<CwBlog> list2) {
		Set<CwBlog> setboth = new HashSet<CwBlog>(list1);
		setboth.addAll(list2);
		list1 = new ArrayList<CwBlog>();
		list1.addAll(setboth);
		return list1;
	}

	/**
	 * Merges two comments lists possibly containing duplicates into one list without any duplicates.
	 * 
	 * @param list1
	 * @param list2
	 * @return list1 with all merged entities
	 */
	private List<CwComment> mergeCommentsLists(List<CwComment> list1, List<CwComment> list2) {
		Set<CwComment> setboth = new HashSet<CwComment>(list1);
		setboth.addAll(list2);
		list1 = new ArrayList<CwComment>();
		list1.addAll(setboth);
		return list1;
	}

	public int calculateNewestBlogId() {
		int id = 0;
		List<CwBlog> newsBlogs = cwManager.getBlogs(1, Filter.BLOGS_NEWS, null);
		if (!newsBlogs.isEmpty()) {
			id = newsBlogs.get(0).getSubjectId();
		}
		List<CwBlog> blogs = cwManager.getBlogs(1, Filter.BLOGS_NORMAL, null);
		if (!blogs.isEmpty()) {
			if (blogs.get(0).getSubjectId() > id) {
				id = blogs.get(0).getSubjectId();
			}
		}
		return id;
	}

	public int calculateNewestNewsId() {
		int id = 0;
		List<CwNews> news = cwManager.getNews(1, Filter.NEWS_ALL, null);
		if (!news.isEmpty()) {
			id = news.get(0).getSubjectId();
		}
		return id;
	}

	public int[] computeIds(int amount, int startId, boolean desc) {
		int[] ids = new int[amount];
		for (int i = 0; i < amount; i++) {
			// if descending, reduce -1 to the startID etc., else add +1
			ids[i] = (desc) ? (startId - i) : (startId + i);
		}
		return ids;
	}

	/**
	 * @return
	 * @see de.consolewars.android.app.db.AppDataHandler#getCwUser()
	 */
	public CwUser getCwUser() {
		return appDataHandler.getCwUser();
	}
}
