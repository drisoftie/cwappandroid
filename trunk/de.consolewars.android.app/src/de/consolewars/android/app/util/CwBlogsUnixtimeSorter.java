package de.consolewars.android.app.util;

import java.util.Comparator;

import de.consolewars.android.app.db.domain.CwBlog;

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
 * Compares two {@link CwBlog} entities by their subjectId.
 * 
 * @author Alexander Dridiger
 */
public class CwBlogsUnixtimeSorter implements Comparator<CwBlog> {

	@Override
	public int compare(CwBlog sub1, CwBlog sub2) {
		return sub1.getUnixtime() - sub2.getUnixtime();
	}
}
