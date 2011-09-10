package de.consolewars.android.app.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import roboguice.util.Ln;
import android.content.Context;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.Where;

import de.consolewars.android.app.R;
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

	public List<CwNews> loadSavedNews() throws SQLException {
		return cwNewsDao.queryForAll();
	}

	/**
	 * Return null if no news were found.
	 * 
	 * @param subjectId
	 * @return
	 * @throws SQLException
	 */
	public CwNews loadSingleSavedNews(int subjectId) throws SQLException {
		List<CwNews> news = cwNewsDao.queryForEq("subjectId", subjectId);
		if (!news.isEmpty()) {
			return news.get(0);
		}
		return null;
	}

	public List<CwNews> loadSavedNews(int amount) throws SQLException {
		return cwNewsDao.queryBuilder().orderBy(context.getString(R.string.db_subjectid_attribute), false)
				.limit(amount).query();
	}

	public List<CwNews> loadSavedNews(int id, boolean below) throws SQLException {
		Where<CwNews, Integer> where = cwNewsDao.queryBuilder()
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
	public boolean createOrUpdateNews(CwNews news) throws SQLException {
		List<CwNews> matches = cwNewsDao.queryForEq(context.getString(R.string.db_subjectid_attribute),
				news.getSubjectId());
		if (!matches.isEmpty()) {
			CwNews savedNews = matches.get(0);
			savedNews.setArticle(news.getArticle());
			savedNews.setAuthor(news.getAuthor());
			savedNews.setCategory(news.getCategory());
			savedNews.setCategoryShort(news.getCategoryShort());
			savedNews.setComments(news.getComments());
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

	public CwUser getCwUser() {
		if (cwUser == null) {
			cwUser = new CwUser();
		}
		return cwUser;
	}
}
