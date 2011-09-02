package de.consolewars.android.app.tab.blogs;

import java.util.ArrayList;
import java.util.List;

import roboguice.activity.RoboActivity;
import android.app.ActivityGroup;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.inject.Inject;

import de.consolewars.android.app.CwManager;
import de.consolewars.android.app.Filter;
import de.consolewars.android.app.R;
import de.consolewars.android.app.tab.CwBasicActivityGroup;
import de.consolewars.android.app.util.DateUtility;
import de.consolewars.android.app.util.StyleSpannableStringBuilder;
import de.consolewars.android.app.util.ViewUtility;
import de.consolewars.api.data.Blog;
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
 * Central Activity to handle the ui for blogs.
 * 
 * @author Alexander Dridiger
 */
public class BlogsActivity extends RoboActivity {

	@Inject
	private CwManager cwManager;
	@Inject
	private ViewUtility viewUtility;

	private List<Blog> blogs = new ArrayList<Blog>();
	private int oldestBlogsID = -1;

	private Filter currentFilter = Filter.BLOGS_NORMAL;

	private LayoutInflater inflater;
	private ViewGroup blogs_layout;
	private ListView list;
	private Button refresh;
	private Spinner spinner;
	private View progress;

	BlogsSeparatorAdapter adapter;

	private boolean initUI = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		inflater = LayoutInflater.from(getParent());
		blogs_layout = (ViewGroup) inflater.inflate(R.layout.blogs_layout, null);
		setContentView(blogs_layout);
		refresh = (Button) blogs_layout.findViewById(R.id.blogs_bttn_refresh);
		spinner = (Spinner) blogs_layout.findViewById(R.id.blogs_filter_spinner);
		progress = blogs_layout.findViewById(R.id.blogs_progressbar);
		list = (ListView) blogs_layout.findViewById(R.id.list);

		adapter = new BlogsSeparatorAdapter(BlogsActivity.this, new ArrayList<UIBlog>());
		list.setAdapter(adapter);
		progress.setVisibility(View.GONE);
		new BuildBlogsAsyncTask().execute();
	}

	/**
	 * Asynchronous task to receive blogs from the API and build up the ui.
	 * 
	 * @author Alexander Dridiger
	 */
	private class BuildBlogsAsyncTask extends AsyncTask<Boolean, UIBlog, Void> {

		boolean doesWork = false;

		@Override
		protected void onPreExecute() {
			doesWork = true;
			progress.setVisibility(View.VISIBLE);
		}

		@Override
		protected Void doInBackground(Boolean... params) {
			if ((params.length > 0 && params[0]) || blogs.isEmpty()) {
				if (!cwManager.getBlogs(currentFilter).isEmpty()) {
					if (currentFilter.equals(Filter.BLOGS_USER)) {
						blogs = cwManager.getBlogsAndStore(50, currentFilter, null);
					} else {
						try {
							blogs = cwManager.getBlogsByIDAndStore(
									cwManager.getBlogs(currentFilter)
											.get(cwManager.getBlogs(Filter.BLOGS_NORMAL).size() - 1).getId(), true);
						} catch (ConsolewarsAPIException e) {
							e.printStackTrace();
						}
					}
				}
			}
			for (int i = 0; i < blogs.size(); i++) {
				if (!isCancelled() && (oldestBlogsID == -1 || blogs.get(i).getId() < oldestBlogsID)) {
					Blog blog = blogs.get(i);
					UIBlog uiblog = new UIBlog();
					uiblog.blog = blog;
					if (i == 0 || isSeparator(blogs.get(i - 1), blog)) {
						uiblog.seperatorTxt = DateUtility
								.createDate(blog.getUnixtime() * 1000L, "EEEE, dd. MMMMM yyyy");
					}
					uiblog.icon = viewUtility.getUserIcon(blog.getUid(), 40);
					uiblog.titleTxt = createTitle(blog.getTitle());
					uiblog.dateTxt = DateUtility.createDate(blog.getUnixtime() * 1000L, "'um' HH:mm'Uhr'");
					uiblog.amountTxt = createCommentsAmount(blog.getComments());
					uiblog.authorTxt = createAuthor(blog.getAuthor());
					if (i == blogs.size() - 1) {
						oldestBlogsID = blog.getId();
					}
					publishProgress(uiblog);
				}
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(UIBlog... rows) {
			if (!isCancelled()) {
				adapter.add(rows[0]);
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			if (!initUI) {
				initFilter();
				initRefreshBttn();
				initScroll();
			}
			initUI = true;
			progress.setVisibility(View.GONE);
			doesWork = false;
		}

		private void initScroll() {
			list.setOnScrollListener(new OnScrollListener() {
				@Override
				public void onScrollStateChanged(AbsListView view, int scrollState) {
					// do nothing
				}

				@Override
				public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
					if (!doesWork && !blogs.isEmpty() && firstVisibleItem + visibleItemCount >= totalItemCount - 1) {
						new BuildBlogsAsyncTask().execute(true);
					}
				}
			});

		}

		/**
		 * Check if the blog on the given position must be separated from the last blogs.
		 * 
		 * @param position
		 * @return
		 */
		private boolean isSeparator(Blog formerBlog, Blog newerBlog) {
			boolean separator = false;
			// check if the last blog was created on the same date as the current blog
			if (DateUtility.getDay(DateUtility.createCalendarFromUnixtime(formerBlog.getUnixtime() * 1000L), 0)
					.getTimeInMillis() > newerBlog.getUnixtime() * 1000L) {
				// current blog was not created on the same date as the last blog --> separator necessary
				separator = true;
			}
			return separator;
		}

		/**
		 * @param parent
		 */
		private void initRefreshBttn() {
			refresh.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					cancel(true);
					oldestBlogsID = -1;
					new BuildBlogsAsyncTask().execute();
				}
			});
		}

		/**
		 * Filter ui and logic for filtering news.
		 * 
		 * @param parentView
		 *            to find the inflated view elements.
		 */
		private void initFilter() {
			ArrayAdapter<CharSequence> spinneradapter = ArrayAdapter.createFromResource(getParent(),
					R.array.blogs_filter_options, android.R.layout.simple_spinner_item);
			spinneradapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(spinneradapter);
			spinner.setSelection(currentFilter.getPosition());
			spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> aView, View view, int position, long id) {
					Filter selected;
					if (Filter.BLOGS_NEWS.getPosition() == position) {
						selected = Filter.BLOGS_NEWS;
					} else if (Filter.BLOGS_USER.getPosition() == position) {
						selected = Filter.BLOGS_USER;
					} else {
						selected = Filter.BLOGS_NORMAL;
					}

					if (!currentFilter.equals(selected)) {
						cancel(true);
						currentFilter = selected;
						oldestBlogsID = -1;
						new BuildBlogsAsyncTask().execute();
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// not needed
				}
			});
		}

		/**
		 * Formatted string of a blog title.
		 * 
		 * @param title
		 * @return a formatted {@link CharSequence}
		 */
		public CharSequence createTitle(String title) {
			// TODO text formatting
			return title;
		}

		ForegroundColorSpan brown = new ForegroundColorSpan(0xFF007711);

		/**
		 * Creates the string for the ui cell showing the author of a blog and the amount of comments.
		 * 
		 * @param commentAmount
		 * @param author
		 * @return a formatted {@link CharSequence}
		 */
		private CharSequence createCommentsAmount(int commentAmount) {
			// TODO more text formatting
			// an empty author string means that the blog was not written by a
			StyleSpannableStringBuilder styleStringBuilder = new StyleSpannableStringBuilder();
			styleStringBuilder.appendWithStyle(brown, String.valueOf(commentAmount));

			return styleStringBuilder;
		}

		ForegroundColorSpan green1 = new ForegroundColorSpan(0xFF007711);
		ForegroundColorSpan green2 = new ForegroundColorSpan(0xFF009933);

		/**
		 * Creates the string for the ui cell showing the author of a blog and the amount of comments.
		 * 
		 * @param commentAmount
		 * @param author
		 * @return a formatted {@link CharSequence}
		 */
		private CharSequence createAuthor(String author) {
			// TODO more text formatting
			// an empty author string means that the blog was not written by a
			if (author.matches("")) {
				author = getString(R.string.news_author_unknown);
			}
			StyleSpannableStringBuilder styleStringBuilder = new StyleSpannableStringBuilder();
			styleStringBuilder.appendWithStyle(green1, getString(R.string.news_author_by));
			styleStringBuilder.append(" ");
			styleStringBuilder.appendWithStyle(green2, author);

			return styleStringBuilder;
		}

	}

	protected class BlogsSeparatorAdapter extends ArrayAdapter<UIBlog> {

		private final int SEPERATOR = 0;
		private final int BLOGELEMENT = 1;

		public BlogsSeparatorAdapter(Context context, List<UIBlog> rows) {
			super(context, R.layout.blogs_row_layout, rows);
		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		@Override
		public int getItemViewType(int position) {
			if (getItem(position).seperatorTxt != null) {
				return SEPERATOR;
			} else {
				return BLOGELEMENT;
			}
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			Log.i("*********COUNT**********", getCount() + "");
			Log.i("*********POS**********", position + "");
			UIBlog blog = getItem(position);
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				if (getItemViewType(position) == SEPERATOR) {
					convertView = inflater.inflate(R.layout.blogs_row_day_separator_item_layout, null);
					View separator = convertView.findViewById(R.id.blogs_separator);
					separator.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							// do nothing
						}
					});
					holder.separator = (TextView) separator.findViewById(R.id.blogs_row_day_separator_text);
				} else {
					convertView = inflater.inflate(R.layout.blogs_row_layout, null);
				}
				holder.usericon = (ImageView) convertView.findViewById(R.id.blogs_row_user_icon);
				holder.title = (TextView) convertView.findViewById(R.id.blogs_row_title);
				holder.date = (TextView) convertView.findViewById(R.id.blogs_row_date);
				holder.amount = (TextView) convertView.findViewById(R.id.blogs_row_cmmts_amount);
				holder.author = (TextView) convertView.findViewById(R.id.blogs_row_author);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			if (holder.separator != null) {
				if (blog.seperatorTxt == null) {
					View separator = convertView.findViewById(R.id.blogs_separator);
					separator.setVisibility(View.GONE);
				}
				holder.separator.setText(blog.seperatorTxt);
			}
			holder.usericon.setImageBitmap(blog.icon);
			holder.title.setText(blog.titleTxt);
			holder.date.setText(blog.dateTxt);
			holder.amount.setText(blog.amountTxt);
			holder.author.setText(blog.authorTxt);

			convertView.findViewById(R.id.blogs_row).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					getSingleBlog(getItem(position).blog.getId());
				}
			});
			return convertView;
		}

		/**
		 * Changes the current activity to a {@link SingleBlogActivity} with the selected blog.
		 * 
		 * @param id
		 *            the blog id to find the selected blog
		 */
		private void getSingleBlog(int id) {
			// setting the correct intent awaited by the SingleBlogActivity
			Intent singleBlogIntent = new Intent(BlogsActivity.this, SingleBlogActivity.class);

			singleBlogIntent.putExtra(getString(R.string.id), id);

			View view = ((ActivityGroup) getParent())
					.getLocalActivityManager()
					.startActivity(SingleBlogActivity.class.getSimpleName(),
							singleBlogIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)).getDecorView();
			// replace the view
			((BlogsActivityGroup) getParent()).replaceView(view);
		}

		class ViewHolder {
			TextView separator;
			ImageView usericon;
			TextView title;
			TextView date;
			TextView amount;
			TextView author;
		}

	}

	private class UIBlog {
		Blog blog;
		CharSequence seperatorTxt;
		Bitmap icon;
		CharSequence titleTxt;
		CharSequence dateTxt;
		CharSequence amountTxt;
		CharSequence authorTxt;
	}

	@Override
	public void onBackPressed() {
		if (getParent() instanceof CwBasicActivityGroup) {
			((CwBasicActivityGroup) getParent()).back();
		}
	}
}