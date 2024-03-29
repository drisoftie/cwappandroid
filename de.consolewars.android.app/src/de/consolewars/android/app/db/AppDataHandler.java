package de.consolewars.android.app.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import roboguice.util.Ln;
import android.content.Context;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.stmt.Where;

import de.consolewars.android.app.R;
import de.consolewars.android.app.db.domain.CwBlog;
import de.consolewars.android.app.db.domain.CwComment;
import de.consolewars.android.app.db.domain.CwNews;
import de.consolewars.android.app.db.domain.CwOptions;
import de.consolewars.android.app.db.domain.CwPicture;
import de.consolewars.android.app.db.domain.CwUser;
import de.consolewars.android.app.db.domain.CwVideo;

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
 * Helper class providing access to app-specific data. Access to the db is also leveraged by it.
 * 
 * @author Alexander Dridiger
 */
@Singleton
public class AppDataHandler {

	@Inject
	private Dao<CwUser, Integer> cwUserDao;
	@Inject
	private Dao<CwNews, Integer> cwNewsDao;
	@Inject
	private Dao<CwBlog, Integer> cwBlogsDao;
	@Inject
	private Dao<CwComment, Integer> cwCommentsDao;
	@Inject
	private Dao<CwPicture, Integer> cwPicturesDao;
	@Inject
	private Dao<CwVideo, Integer> cwVideosDao;
	@Inject
	private Dao<CwOptions, Integer> cwOptionsDao;

	@Inject
	private Context context;

	private CwUser cwUser;
	private CwOptions options;

	/**
	 * @return
	 */
	public boolean loadCurrentUser() {
		List<CwUser> cwUsers = new ArrayList<CwUser>();
		try {
			cwUsers = cwUserDao.queryForAll();
		} catch (SQLException e) {
			Ln.e(e);
		}
		if (cwUsers.size() > 0) {
			cwUser = cwUsers.get(0);
			return true;
		}
		return false;
	}

	public List<CwNews> loadAllSavedNews() throws SQLException {
		return cwNewsDao.queryBuilder().orderBy(context.getString(R.string.db_subjectid_attribute), false).query();
	}

	/**
	 * Gets amount of news starting from the given id. News corresponding to the given id will be included into the
	 * result.
	 * 
	 * @param startSubjectId
	 * @param amount
	 * @return
	 * @throws SQLException
	 */
	public List<CwNews> pageThroughSavedNews(int startSubjectId, int amount) throws SQLException {
		return cwNewsDao.queryBuilder().limit(amount)
				.orderBy(context.getString(R.string.db_subjectid_attribute), false).where()
				.le(context.getString(R.string.db_subjectid_attribute), false).query();
	}

	/**
	 * Return null if no blogs were found.
	 * 
	 * @param subjectId
	 * @return found blog
	 * @throws SQLException
	 */
	public CwBlog loadSingleSavedBlog(int subjectId) throws SQLException {
		return cwBlogsDao.queryForFirst(cwBlogsDao.queryBuilder().where()
				.eq(context.getString(R.string.db_subjectid_attribute), subjectId).prepare());
	}

	/**
	 * Return null if no news were found.
	 * 
	 * @param subjectId
	 * @return
	 * @throws SQLException
	 */
	public CwNews loadSingleSavedNews(int subjectId) throws SQLException {
		CwNews cwNews = cwNewsDao.queryForFirst(cwNewsDao.queryBuilder().where()
				.eq(context.getString(R.string.db_subjectid_attribute), subjectId).prepare());
		if (cwNews != null) {
			for (CwComment comment : cwNews.getComments()) {
				cwCommentsDao.refresh(comment);
			}
			for (CwPicture pic : cwNews.getPictures()) {
				cwPicturesDao.refresh(pic);
			}
			for (CwVideo video : cwNews.getVideos()) {
				cwVideosDao.refresh(video);
			}
		}
		return cwNews;
	}

	/**
	 * Return null if no news were found.
	 * 
	 * @param subjectId
	 * @return
	 * @throws SQLException
	 */
	public CwNews loadNewsById(CwNews news) throws SQLException {
		return cwNewsDao.queryForId(news.getId());
	}

	/**
	 * Gets amount of news starting from the given id. News corresponding to the given id will be included into the
	 * result.
	 * 
	 * @param startSubjectId
	 * @param amount
	 * @return
	 * @throws SQLException
	 */
	public int loadNewestNews() throws SQLException {
		int newestNewsId = -1;
		GenericRawResults<Object[]> rawResults = cwNewsDao.queryRaw(
				context.getString(R.string.db_newest_subjectId, context.getString(R.string.db_table_news_name)),
				new DataType[] { DataType.INTEGER });
		for (Object[] resultArray : rawResults) {
			if (resultArray.length > 0) {
				newestNewsId = (Integer) resultArray[0];
			}
		}
		rawResults.close();
		return newestNewsId;
	}

	/**
	 * @param amount
	 * @return
	 * @throws SQLException
	 */
	public List<CwBlog> loadSavedBlogs(int amount) throws SQLException {
		return cwBlogsDao.queryBuilder().orderBy(context.getString(R.string.db_subjectid_attribute), false)
				.limit(amount).query();
	}

	/**
	 * @param amount
	 * @return
	 * @throws SQLException
	 */
	public List<CwNews> loadSavedNews(int amount) throws SQLException {
		List<CwNews> news = cwNewsDao.queryBuilder().orderBy(context.getString(R.string.db_subjectid_attribute), false)
				.limit(amount).query();
		for (CwNews cwNews : news) {
			for (CwComment comment : cwNews.getComments()) {
				cwCommentsDao.refresh(comment);
			}
			for (CwPicture pic : cwNews.getPictures()) {
				cwPicturesDao.refresh(pic);
			}
			for (CwVideo video : cwNews.getVideos()) {
				cwVideosDao.refresh(video);
			}
		}
		return news;
	}

	/**
	 * @param id
	 * @param below
	 * @return
	 * @throws SQLException
	 */
	public List<CwBlog> loadSavedBlogs(int id, boolean below, int amount) throws SQLException {
		Where<CwBlog, Integer> where = cwBlogsDao.queryBuilder().limit(amount)
				.orderBy(context.getString(R.string.db_subjectid_attribute), false).where();
		if (below) {
			where.lt(context.getString(R.string.db_subjectid_attribute), id);
		} else {
			where.gt(context.getString(R.string.db_subjectid_attribute), id);
		}
		return where.query();
	}

	/**
	 * @param id
	 * @param below
	 * @return
	 * @throws SQLException
	 */
	public List<CwNews> loadSavedNews(int id, boolean below, int amount) throws SQLException {
		Where<CwNews, Integer> where = cwNewsDao.queryBuilder().limit(amount)
				.orderBy(context.getString(R.string.db_subjectid_attribute), false).where();
		if (below) {
			where.lt(context.getString(R.string.db_subjectid_attribute), id);
		} else {
			where.gt(context.getString(R.string.db_subjectid_attribute), id);
		}
		List<CwNews> news = where.query();
		for (CwNews cwNews : news) {
			for (CwComment comment : cwNews.getComments()) {
				cwCommentsDao.refresh(comment);
			}
			for (CwPicture pic : cwNews.getPictures()) {
				cwPicturesDao.refresh(pic);
			}
			for (CwVideo video : cwNews.getVideos()) {
				cwVideosDao.refresh(video);
			}
		}
		return news;
	}

	/**
	 * Creates or updates a news based on its news id (not database id).
	 * 
	 * @param news
	 * @return created = true; updated = false
	 * @throws SQLException
	 */
	public boolean createOrUpdateBlog(CwBlog blog) throws SQLException {
		CwBlog match = cwBlogsDao.queryForFirst(cwBlogsDao.queryBuilder().where()
				.eq(context.getString(R.string.db_subjectid_attribute), blog.getSubjectId()).prepare());
		if (match != null) {
			match.setArticle(blog.getArticle());
			match.setAuthor(blog.getAuthor());
			match.setCommentsAmount(blog.getCommentsAmount());
			match.setDescription(blog.getDescription());
			match.setMode(blog.getMode());
			match.setRating(blog.getRating());
			match.setSubjectId(blog.getSubjectId());
			match.setTitle(blog.getTitle());
			match.setUid(blog.getUid());
			match.setUnixtime(blog.getUnixtime());
			match.setUrl(blog.getUrl());
			match.setVisible(blog.isVisible());
			cwBlogsDao.update(match);
			return false;
		}
		return cwBlogsDao.create(blog) == 1;
	}

	/**
	 * Creates or updates a news based on its news id (not database id).
	 * 
	 * @param news
	 * @return created = true; updated = false
	 * @throws SQLException
	 */
	public boolean createOrUpdateNews(CwNews news) throws SQLException {
		boolean created = false;
		// List<CwNews> matches = cwNewsDao.queryForEq(context.getString(R.string.db_subjectid_attribute),
		// news.getSubjectId());
		// if (!matches.isEmpty()) {
		// CwNews match = matches.get(0);
		// news.setId(match.getId());
		// cwNewsDao.update(match);
		// created = false;
		// } else {
		cwNewsDao.create(news);
		created = true;
		// }
		for (CwComment comment : news.getComments()) {
			createOrUpdateComment(comment);
		}
		for (CwPicture pic : news.getPictures()) {
			createOrUpdatePicture(pic);
		}
		for (CwVideo video : news.getVideos()) {
			createOrUpdateVideo(video);
		}
		return created;
	}

	public boolean createOrUpdateComment(CwComment comment) throws SQLException {
		boolean created = false;
		List<CwComment> matches = cwCommentsDao.queryForEq(context.getString(R.string.db_commentid_attribute),
				comment.getCid());
		if (!matches.isEmpty()) {
			CwComment match = matches.get(0);
			comment.setId(match.getId());
			created = false;
		} else {
			cwCommentsDao.create(comment);
			created = true;
		}
		return created;
	}

	public boolean createOrUpdatePicture(CwPicture picture) throws SQLException {
		boolean created = false;
		List<CwPicture> matches = cwPicturesDao.queryForEq(context.getString(R.string.db_picurl_attribute),
				picture.getUrl());
		if (!matches.isEmpty()) {
			CwPicture match = matches.get(0);
			picture.setId(match.getId());
			created = false;
		} else {
			cwPicturesDao.create(picture);
			created = true;
		}
		return created;
	}

	public boolean createOrUpdateVideo(CwVideo video) throws SQLException {
		boolean created = false;
		List<CwVideo> matches = cwVideosDao.queryForEq(context.getString(R.string.db_videourl_attribute),
				video.getUrl());
		if (!matches.isEmpty()) {
			CwVideo match = matches.get(0);
			video.setId(match.getId());
			created = false;
		} else {
			cwVideosDao.create(video);
			created = true;
		}
		return created;
	}

	public boolean refreshComment(CwComment commentToRefresh) throws SQLException {
		return cwCommentsDao.refresh(commentToRefresh) == 1;
	}

	public boolean refreshComment(CwComment commentToRefresh, CwComment comment) {

		return true;
	}

	public void createOrUpdateOptions(CwOptions options) throws SQLException {
		if (cwOptionsDao.countOf() == 0) {
			cwOptionsDao.create(options);
		} else {
			options.setId(cwOptionsDao.queryForAll().get(0).getId());
			cwOptionsDao.update(options);
		}
	}

	public CwOptions getOptions() {
		if (options == null) {
			if (!hasOptionsData()) {
				CwOptions options = new CwOptions();
				options.setMaxBlogsAction(10);
				options.setMaxBlogsScroll(10);
				options.setMaxCmts(30);
				options.setMaxNewsAction(10);
				options.setMaxNewsScroll(10);
				try {
					createOrUpdateOptions(options);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			try {
				options = cwOptionsDao.queryForAll().get(0);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return options;
	}

	public boolean hasNewsData() {
		try {
			if (cwNewsDao.countOf() > 0) {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean hasBlogsData() {
		try {
			if (cwBlogsDao.countOf() > 0) {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean hasOptionsData() {
		try {
			if (cwOptionsDao.countOf() > 0) {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public CwUser getCwUser() {
		if (cwUser == null) {
			cwUser = new CwUser();
		}
		return cwUser;
	}
}
