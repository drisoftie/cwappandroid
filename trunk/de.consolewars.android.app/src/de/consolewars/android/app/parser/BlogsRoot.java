package de.consolewars.android.app.parser;

import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

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
 * @author Alexander Dridiger
 */
@Root(name = "root", strict = false)
public class BlogsRoot {

	@ElementList(inline = true)
	private List<CwBlog> blogs;

	/**
	 * @return the blogs
	 */
	public List<CwBlog> getBlogs() {
		return blogs;
	}

	/**
	 * @param blogs
	 *            the blogs to set
	 */
	public void setBlogs(List<CwBlog> blogs) {
		this.blogs = blogs;
	}
}
