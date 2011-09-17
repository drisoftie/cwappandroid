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
import de.consolewars.android.app.db.domain.CwNews;
import de.consolewars.android.app.db.domain.CwUser;

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
	private Context context;

	private CwUser cwUser;

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
		return cwNewsDao.queryForFirst(cwNewsDao.queryBuilder().where()
				.eq(context.getString(R.string.db_subjectid_attribute), subjectId).prepare());
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
		return cwNewsDao.queryBuilder().orderBy(context.getString(R.string.db_subjectid_attribute), false)
				.limit(amount).query();
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
		return where.query();
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
		List<CwNews> matches = cwNewsDao.queryForEq(context.getString(R.string.db_subjectid_attribute),
				news.getSubjectId());
		if (!matches.isEmpty()) {
			CwNews savedNews = matches.get(0);
			savedNews.setArticle(news.getArticle());
			savedNews.setAuthor(news.getAuthor());
			savedNews.setCategory(news.getCategory());
			savedNews.setCategoryShort(news.getCategoryShort());
			savedNews.setCommentsAmount(news.getCommentsAmount());
			savedNews.setDescription(news.getDescription());
			savedNews.setMode(news.getMode());
			savedNews.setSubjectId(news.getSubjectId());
			savedNews.setPicId(news.getPicId());
			savedNews.setTitle(news.getTitle());
			savedNews.setUnixtime(news.getUnixtime());
			savedNews.setUrl(news.getUrl());
			cwNewsDao.update(savedNews);
			return false;
		}
		return cwNewsDao.create(news) == 1;
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

	public CwUser getCwUser() {
		if (cwUser == null) {
			cwUser = new CwUser();
		}
		return cwUser;
	}
}
