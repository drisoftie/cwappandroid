package de.consolewars.android.app;

import java.util.List;

import android.content.Context;

import com.google.inject.Inject;

import de.consolewars.android.app.db.AppDataHandler;
import de.consolewars.android.app.db.domain.CwBlog;
import de.consolewars.android.app.db.domain.CwNews;
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
public class CwEntityManager {

	@Inject
	private CwLoginManager cwLoginManager;
	@Inject
	private AppDataHandler appDataHandler;
	@Inject
	private Context context;

	private List<CwNews> news;
	private List<CwBlog> blogs;
	private List<CwBlog> userBlogs;
	private List<Message> msgs;
	private int newestNews = -1;
	private int newestBlog = -1;

}
