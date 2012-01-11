package de.consolewars.android.app;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import android.content.Context;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.consolewars.android.app.CwManager.CommentArea;
import de.consolewars.android.app.db.AppDataHandler;
import de.consolewars.android.app.db.domain.CwBlog;
import de.consolewars.android.app.db.domain.CwComment;
import de.consolewars.android.app.db.domain.CwMessage;
import de.consolewars.android.app.db.domain.CwNews;
import de.consolewars.android.app.db.domain.CwOptions;
import de.consolewars.android.app.db.domain.CwPicture;
import de.consolewars.android.app.db.domain.CwUser;
import de.consolewars.android.app.db.domain.CwVideo;
import de.consolewars.android.app.parser.CommentsRoot;
import de.consolewars.android.app.util.CwBlogsIdSorter;
import de.consolewars.android.app.util.CwBlogsUnixtimeSorter;
import de.consolewars.android.app.util.CwCommentsIdSorter;
import de.consolewars.android.app.util.CwNewsIdSorter;
import de.consolewars.android.app.util.MediaSnapper;

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
	private List<CwNews> downloadedNews;
	private List<CwNews> savedNews;
	private List<CwBlog> blogs;
	private List<CwBlog> downloadedBlogs;
	private List<CwBlog> savedBlogs;
	private List<CwBlog> userBlogs;
	private List<CwBlog> downloadedUserBlogs;
	private List<CwMessage> msgs;
	private int newestNewsId = -1;
	private int newestBlogId = -1;

	private CwNews selectedNews;
	private CwBlog selectedBlog;

	private CwNewsIdSorter cwNewsIdSorter = new CwNewsIdSorter();
	private CwBlogsIdSorter cwBlogsIdSorter = new CwBlogsIdSorter();
	private CwBlogsUnixtimeSorter cwBlogsUnixtimeSorter = new CwBlogsUnixtimeSorter();
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
	public CwBlog getBlogSingle(int id, boolean fromSavedBlogs) {
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

	public CwNews getNewsSingle(int id, boolean fromSavedNews) {
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
		news = getNewsSingle(id);
		if (news != null) {
			try {
				appDataHandler.createOrUpdateNews(news);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return news;
	}

	public CwNews getNewsSingle(int id) {
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
		if (Filter.BLOGS_USER.equals(filter)) {
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
		if (Filter.BLOGS_USER.equals(filter)) {
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

	/**
	 * @param filter
	 * @return
	 */
	public List<CwBlog> getDownloadedBlogs(Filter filter) {
		if (Filter.BLOGS_USER.equals(filter)) {
			if (downloadedUserBlogs == null) {
				downloadedUserBlogs = new ArrayList<CwBlog>();
			}
			return downloadedUserBlogs;
		} else {
			if (downloadedBlogs == null) {
				downloadedBlogs = new ArrayList<CwBlog>();
			}
			return downloadedBlogs;
		}
	}

	public List<CwBlog> getSavedBlogs() {
		if (savedBlogs == null) {
			savedBlogs = new ArrayList<CwBlog>();
		}
		return savedBlogs;
	}

	public void setSavedBlogs(List<CwBlog> savedBlogs) {
		this.savedBlogs = savedBlogs;
	}

	/**
	 * @return the downloadedNews
	 */
	public List<CwNews> getDownloadedNews() {
		if (downloadedNews == null) {
			downloadedNews = new ArrayList<CwNews>();
		}
		return downloadedNews;
	}

	/**
	 * @return the savedNews
	 */
	public List<CwNews> getSavedNews() {
		if (savedNews == null) {
			savedNews = new ArrayList<CwNews>();
		}
		return savedNews;
	}

	public List<CwBlog> getBlogsNewest(Filter filter) {
		if (getCachedBlogs(filter).isEmpty()) {
			return getBlogsNext(EntityRefinement.MIXED, filter);
		}
		int newestBlog = calculateNewestBlogId();
		int currentBlog = getCachedBlogs(filter).get(0).getSubjectId();
		if (newestBlog > currentBlog) {
			int amount = newestBlog - currentBlog;
			int runs;
			if (amount % 50 == 0) {
				runs = amount / 50;
			} else {
				runs = (amount / 50) + 1;
			}
			for (int i = 0; i < runs; i++) {
				if (i == runs - 1) {
					int lastAmount = amount - (i * 50);
					setCachedBlogs(
							mergeBlogsLists(getCachedBlogs(filter),
									getBlogsByIDAndCache(filter, (currentBlog + 1) + i * 50, true, lastAmount)), filter);
				} else {
					setCachedBlogs(
							mergeBlogsLists(getCachedBlogs(filter),
									getBlogsByIDAndCache(filter, (currentBlog + 1) + i * 50, true, 50)), filter);
				}
			}
		}
		Collections.sort(getCachedBlogs(filter), Collections.reverseOrder(cwBlogsUnixtimeSorter));
		return getCachedBlogs(filter);
	}

	public List<CwBlog> getBlogsNext(EntityRefinement refinement, Filter filter) {
		if (refinement == EntityRefinement.CACHE_ONLY) {
			// TODO: Paging
		} else if (refinement.equals(EntityRefinement.MIXED)) {
			getBlogsMixed(appDataHandler.getOptions().getMaxBlogsAction(), filter);
		} else if (refinement.equals(EntityRefinement.SAVED_ONLY)) {
			getBlogsSaved(appDataHandler.getOptions().getMaxBlogsAction(), filter);
		}
		return getCachedBlogs(filter);
	}

	/**
	 * Downloads news and merges them with the ones from the database.
	 * 
	 * @return
	 */
	public List<CwBlog> getBlogsMixed(int amount, Filter filter) {
		// FIXME: Current implementation only looks for oldest downloaded blogs
		int oldestBlog = getDownloadedBlogs(filter).isEmpty() ? -1 : getDownloadedBlogs(filter).get(
				getDownloadedBlogs(filter).size() - 1).getSubjectId();
		if (oldestBlog > -1) {
			// first the download
			setCachedBlogs(
					mergeBlogsLists(getCachedBlogs(filter), getBlogsByIDAndCache(filter, oldestBlog, true, amount)),
					filter);
			// then the database
			if (appDataHandler.hasBlogsData()) {
				try {
					setSavedBlogs(mergeBlogsLists(getSavedBlogs(), appDataHandler.loadSavedBlogs(
							getSavedBlogs().get(getSavedBlogs().size() - 1).getSubjectId(), true, amount)));
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			setCachedBlogs(mergeBlogsLists(getCachedBlogs(filter), getSavedBlogs()), filter);
		} else {
			setCachedBlogs(
					mergeBlogsLists(getCachedBlogs(filter),
							getBlogsByIDAndCache(filter, calculateNewestBlogId(), true, amount)), filter);
			List<CwBlog> loadedBlogs = new ArrayList<CwBlog>();
			if (!getSavedBlogs().isEmpty()) {
				try {
					loadedBlogs = appDataHandler.loadSavedBlogs(getSavedBlogs().get(getSavedBlogs().size() - 1)
							.getSubjectId(), true, amount);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} else {
				try {
					loadedBlogs = appDataHandler.loadSavedBlogs(amount);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			setSavedBlogs(mergeBlogsLists(getSavedBlogs(), loadedBlogs));
			Collections.sort(getSavedBlogs(), Collections.reverseOrder(cwBlogsUnixtimeSorter));
			if (loadedBlogs != null) {
				setCachedBlogs(mergeBlogsLists(getCachedBlogs(filter), getSavedBlogs()), filter);
			}
		}
		setCachedBlogs(mergeBlogsLists(getCachedBlogs(filter), getCachedBlogs(filter)), filter);
		Collections.sort(getCachedBlogs(filter), Collections.reverseOrder(cwBlogsUnixtimeSorter));
		return getCachedBlogs(filter);
	}

	public List<CwNews> getBlogsSaved(int amount, Filter filter) {
		int oldestNews = news.isEmpty() ? -1 : news.get(news.size() - 1).getSubjectId();
		if (appDataHandler.hasNewsData()) {
			if (oldestNews > -1) {
				try {
					return appDataHandler.loadSavedNews(oldestNews, true, appDataHandler.getOptions()
							.getMaxBlogsAction());
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

	public List<CwNews> getNewsNewest() {
		if (getDownloadedNews().isEmpty()) {
			return getNewsNext(EntityRefinement.MIXED);
		}
		int newestNewst = calculateNewestNewsId();
		int currentNews = getDownloadedNews().get(0).getSubjectId();
		if (newestNewst > currentNews) {
			int amount = newestNewst - currentNews;
			int runs;
			if (amount % 50 == 0) {
				runs = amount / 50;
			} else {
				runs = (amount / 50) + 1;
			}
			for (int i = 0; i < runs; i++) {
				if (i == runs - 1) {
					int lastAmount = amount - (i * 50);
					getNewsByIDAndCache((currentNews + 1) + i * 50, true, lastAmount);
				} else {
					getNewsByIDAndCache((currentNews + 1) + i * 50, true, 50);
				}
			}
		}
		news = mergeNewsLists(Arrays.asList(getDownloadedNews().toArray(new CwNews[0])), news);
		Collections.sort(news, Collections.reverseOrder(cwNewsIdSorter));
		return news;
	}

	public List<CwNews> getNewsNext(EntityRefinement refinement) {
		if (refinement.equals(EntityRefinement.CACHE_ONLY)) {
			// TODO: Paging
		} else if (refinement.equals(EntityRefinement.MIXED)) {
			getNewsMixed(appDataHandler.getOptions().getMaxNewsAction());
		} else if (refinement.equals(EntityRefinement.SAVED_ONLY)) {
			return getNewsSaved(appDataHandler.getOptions().getMaxNewsAction());
		}
		return getCachedNews();
	}

	/**
	 * Downloads news and merges them with the ones from the database.
	 * 
	 * @return
	 */
	public List<CwNews> getNewsMixed(int amount) {
		// FIXME: Current implementation only looks for oldest downloaded news
		int oldestNews = getDownloadedNews().isEmpty() ? -1 : getDownloadedNews().get(getDownloadedNews().size() - 1)
				.getSubjectId();
		if (oldestNews > -1) {
			// first the download
			getNewsByIDAndCache(oldestNews, true, amount);
			news = mergeNewsLists(Arrays.asList(news.toArray(new CwNews[0])),
					Arrays.asList(getDownloadedNews().toArray(new CwNews[0])));
			// then the database
			if (appDataHandler.hasNewsData()) {
				try {
					savedNews = mergeNewsLists(Arrays.asList(getSavedNews().toArray(new CwNews[0])),
							appDataHandler.loadSavedNews(oldestNews, true, amount));
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			news = mergeNewsLists(Arrays.asList(news.toArray(new CwNews[0])),
					Arrays.asList(getSavedNews().toArray(new CwNews[0])));
		} else {
			getNewsByIDAndCache(calculateNewestNewsId(), true, amount);
			news = mergeNewsLists(Arrays.asList(news.toArray(new CwNews[0])),
					Arrays.asList(getDownloadedNews().toArray(new CwNews[0])));
			List<CwNews> loadedNews = new ArrayList<CwNews>();
			if (!getSavedNews().isEmpty()) {
				try {
					loadedNews = appDataHandler.loadSavedNews(getSavedNews().get(getSavedNews().size() - 1)
							.getSubjectId(), true, amount);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} else {
				try {
					loadedNews = appDataHandler.loadSavedNews(amount);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			savedNews = mergeNewsLists(savedNews, Arrays.asList(loadedNews.toArray(new CwNews[0])));
			Collections.sort(savedNews, Collections.reverseOrder(cwNewsIdSorter));
			if (loadedNews != null) {
				news = mergeNewsLists(Arrays.asList(news.toArray(new CwNews[0])), savedNews);
			}
		}
		news = mergeNewsLists(Arrays.asList(news.toArray(new CwNews[0])), news);
		Collections.sort(news, Collections.reverseOrder(cwNewsIdSorter));
		return news;
	}

	public List<CwNews> getNewsSaved(int amount) {
		int oldestNews = getSavedNews().isEmpty() ? -1 : getSavedNews().get(getSavedNews().size() - 1).getSubjectId();
		if (appDataHandler.hasNewsData()) {
			if (oldestNews > -1) {
				try {
					savedNews = mergeNewsLists(Arrays.asList(getSavedNews().toArray(new CwNews[0])),
							appDataHandler.loadSavedNews(oldestNews, true, appDataHandler.getOptions()
									.getMaxNewsAction()));
					Collections.sort(savedNews, Collections.reverseOrder(cwNewsIdSorter));
					return getSavedNews();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} else {
				try {

					savedNews = mergeNewsLists(Arrays.asList(getSavedNews().toArray(new CwNews[0])),
							appDataHandler.loadSavedNews(amount));
					Collections.sort(savedNews, Collections.reverseOrder(cwNewsIdSorter));
					return getSavedNews();
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

	public List<CwBlog> getBlogsByIDAndCache(Filter filter, int startID, boolean desc, int amount) {
		if (startID < amount && startID > 0 && desc) {
			amount = startID;
		}
		int[] ids = computeIds(amount, startID, true);
		if (desc) {
			getDownloadedBlogs(filter).addAll(cwManager.getBlogsByIds(ids));
		} else {
			getDownloadedBlogs(filter).addAll(0, cwManager.getBlogsByIds(ids));
		}
		return getDownloadedBlogs(filter);
	}

	public void getNewsByIDAndCache(int startId, boolean desc, int amount) {
		if (startId < amount && startId > 0 && desc) {
			amount = startId;
		}
		int[] ids = computeIds(amount, startId, desc);
		if (desc) {
			getDownloadedNews().addAll(cwManager.getNewsByIds(ids));
		} else {
			getDownloadedNews().addAll(0, cwManager.getNewsByIds(ids));
		}
	}

	/**
	 * @return the msgs
	 */
	public List<CwMessage> getMsgs() {
		if (msgs == null) {
			msgs = new ArrayList<CwMessage>();
		}
		return msgs;
	}

	public List<CwMessage> getMessagesAndCache(Filter filter, int count) {
		msgs = cwManager.getMessages(filter, count);
		return getMsgs();
	}

	public void loadSavedNews() {

	}

	public int saveAllNews() {
		int savecounter = 0;
		for (CwNews newsToSave : news) {
			try {
				for (CwComment comment : newsToSave.getComments()) {
					comment.setCwNews(newsToSave);
				}
				for (CwPicture pic : newsToSave.getPictures()) {
					pic.setCwNews(newsToSave);
				}
				for (CwVideo video : newsToSave.getVideos()) {
					video.setCwNews(newsToSave);
				}
				appDataHandler.createOrUpdateNews(newsToSave);
				savecounter++;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return savecounter;
	}

	public int saveAllBlogs(Filter filter) {
		int savecounter = 0;
		for (CwBlog blog : getCachedBlogs(filter)) {
			try {
				appDataHandler.createOrUpdateBlog(blog);
				savecounter++;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return savecounter;
	}

	public int saveWholeCw() {
		int id = calculateNewestNewsId();
		int count = 0;
		while (id > 0) {
			count += saveWholeCwNews(id);
			id -= 50;
		}
		return count;
	}

	public int saveWholeCwNews(int startId) {
		int amount = 50;
		if (startId < 50 && startId > 0) {
			amount = startId;
		}
		int[] ids = new int[amount];
		for (int i = 0; i < amount; i++) {
			ids[i] = startId - i;
		}
		List<CwNews> saveNews = cwManager.getNewsByIds(ids);
		int savecounter = 0;
		for (CwNews newsToSave : saveNews) {
			try {
				// getFullNews(newsToSave, context.getString(R.string.cw_url_append, newsToSave.getUrl()));
				newsToSave.setComments(getComments(newsToSave.getSubjectId(), CommentArea.AREA_NEWS,
						newsToSave.getCommentsAmount(), 0).getComments());
				for (CwComment comment : newsToSave.getComments()) {
					comment.setCwNews(newsToSave);
				}
				// for (CwPicture pic : newsToSave.getPictures()) {
				// pic.setCwNews(newsToSave);
				// }
				// for (CwVideo video : newsToSave.getVideos()) {
				// video.setCwNews(newsToSave);
				// }
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
	public CommentsRoot getComments(int objectId, int area, int count, int viewPage) {
		CommentsRoot root = cwManager.getComments(objectId, area, count, viewPage);
		List<CwComment> list1 = new ArrayList<CwComment>();
		list1.addAll(root.getComments());
		List<CwComment> list2 = Arrays.asList(list1.toArray(new CwComment[0]));
		list1 = mergeCommentsLists(list1, list2);
		Collections.sort(list1, cwCommentsIdSorter);
		root.setComments(list1);
		return root;
	}

	public CwComment getRefreshedComment(CwComment comment) {
		try {
			appDataHandler.refreshComment(comment);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return comment;
	}

	public void discardAllNews() {
		news = new ArrayList<CwNews>();
		downloadedNews = new ArrayList<CwNews>();
		savedNews = new ArrayList<CwNews>();
	}

	public void discardAllBlogs() {
		blogs = new ArrayList<CwBlog>();
		userBlogs = new ArrayList<CwBlog>();
		downloadedBlogs = new ArrayList<CwBlog>();
		downloadedUserBlogs = new ArrayList<CwBlog>();
		savedBlogs = new ArrayList<CwBlog>();
	}

	public CwNews getFullNews(CwNews news, String url) {
		if (!news.getVideos().isEmpty() || !news.getPictures().isEmpty() || news.getAuthorId() > 0) {
			return news;
		}
		Map<String, String> pathAttrPairs = new HashMap<String, String>();
		pathAttrPairs.put(context.getString(R.string.xpath_get_author), context.getString(R.string.href));
		pathAttrPairs.put(context.getString(R.string.xpath_get_video), context.getString(R.string.value));

		Map<String, String> pathPicsfilterPairs = new HashMap<String, String>();
		pathPicsfilterPairs.put(context.getString(R.string.xpath_get_pictures),
				context.getString(R.string.xpath_pictures_filter));

		Map<String, String> results = MediaSnapper.snapAllFromHTML(context, url, pathAttrPairs, pathPicsfilterPairs);
		boolean vidFound = false;
		for (String key : results.keySet().toArray(new String[0])) {
			if (key.equals(context.getString(R.string.href))) {
				String picUrl = results.get(key);
				int userID = Integer.valueOf(picUrl.subSequence(context.getString(R.string.prefix_userpic).length(),
						picUrl.length()).toString());
				news.setAuthorId(userID);
			} else if (key.startsWith(context.getString(R.string.cw_video_tag))) {
				if (!vidFound) {
					news.setVideos(new ArrayList<CwVideo>());
					vidFound = true;
				}
				String vidUrl = results.get(key);
				CwVideo video = new CwVideo();
				video.setUrl(vidUrl);
				video.setHtmlEmbeddedSnippet(context.getString(R.string.youtube_embedding, 300, 200, vidUrl));
				video.setCwNews(news);
				news.getVideos().add(video);
			} else if (key.startsWith(context.getString(R.string.cw_pic_tag))) {
				String pictureUrls = results.get(key);
				if (!pictureUrls.equals("")) {
					news.setPictures(new ArrayList<CwPicture>());
					// first get the string between both seperators, trim it, remove all "'"
					String stringUrls = StringUtils.remove(
							StringUtils.trim(StringUtils.substringBefore(
									StringUtils.substringAfter(pictureUrls,
											context.getString(R.string.xpath_pictures_filter)), ");")), "'");
					String[] urls = StringUtils.split(stringUrls, ",");
					for (String urlpart : urls) {
						CwPicture picture = new CwPicture();
						picture.setThumbUrl(context.getString(R.string.cw_url_append, urlpart));
						picture.setUrl(context.getString(R.string.cw_url_append,
								StringUtils.substringBefore(urlpart, "&database")));
						news.getPictures().add(picture);
						picture.setCwNews(news);
					}
				}
			}
		}
		return news;
	}

	/**
	 * Replaces or sets new news into the cache.
	 * 
	 * @param news
	 */
	public void replaceOrSetNews(CwNews... news) {
		this.news = mergeNewsLists(Arrays.asList(news), this.news);
		Collections.sort(this.news, Collections.reverseOrder(cwNewsIdSorter));
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
	 * @return the newestNewsId
	 */
	public int getNewestNewsId() {
		return newestNewsId;
	}

	/**
	 * @return the newestBlogId
	 */
	public int getNewestBlogId() {
		return newestBlogId;
	}

	public CwOptions getOptions() {
		return appDataHandler.getOptions();
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
