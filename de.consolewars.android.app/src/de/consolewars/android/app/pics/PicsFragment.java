package de.consolewars.android.app.pics;

import java.util.List;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery;
import android.widget.ImageView;
import de.consolewars.android.app.CwApplication;
import de.consolewars.android.app.R;
import de.consolewars.android.app.db.domain.CwNews;
import de.consolewars.android.app.db.domain.CwPicture;
import de.consolewars.android.app.tab.CwAbstractFragment;

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
 * 
 */
public class PicsFragment extends CwAbstractFragment {

	private Activity context;
	private LayoutInflater inflater;

	private Gallery gallery;
	private ImageView imgView;
	private LazyAdapter adapter;

	private ViewGroup content;
	private ViewGroup pic_layout;
	private ViewGroup pic_fragment_layout;
	private ViewGroup progress_layout;

	private SetPicTask task;

	private List<CwPicture> pictures;

	public PicsFragment() {
	}

	public PicsFragment(String title) {
		super(title);
		setHasOptionsMenu(true);
		task = new SetPicTask();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		context = activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		pic_layout = (ViewGroup) inflater.inflate(R.layout.pic_fragment_layout, null);
		this.inflater = LayoutInflater.from(context);
		pic_layout = (ViewGroup) inflater.inflate(R.layout.fragment_progress_layout, null);
		progress_layout = CwApplication.cwViewUtil().getCenteredProgressBarLayout(inflater, R.string.gallery);
		ViewGroup progress = (ViewGroup) pic_layout.findViewById(R.id.progressbar);
		progress.addView(progress_layout);
		progress_layout.setVisibility(View.GONE);
		return pic_layout;
	}

	@Override
	public void onResume() {
		super.onResume();
		// if (CwApplication.cwEntityManager().getSelectedNews().getPictures() != null) {
		// pictures = Arrays.asList(CwApplication.cwEntityManager().getSelectedNews().getPictures()
		// .toArray(new CwPicture[0]));
		// } else {
		// pictures = new ArrayList<CwPicture>();
		// }

		pic_layout = (ViewGroup) getView();
		content = (ViewGroup) pic_layout.findViewById(R.id.content);
		content.removeAllViews();
		pic_fragment_layout = (ViewGroup) inflater.inflate(R.layout.pic_fragment_layout, null);
		progress_layout.setVisibility(View.GONE);

		if (task != null && task.getStatus().equals(AsyncTask.Status.PENDING)) {
			task.execute();
		} else if (task == null || task.getStatus().equals(AsyncTask.Status.FINISHED)) {
			task = new SetPicTask();
			task.execute();
		} else if (task != null && task.getStatus().equals(AsyncTask.Status.RUNNING)) {
			task.cancel(true);
		}
	}

	// public OnClickListener listener = new OnClickListener() {
	// @Override
	// public void onClick(View arg0) {
	// adapter.imageLoader.clearCache();
	// adapter.notifyDataSetChanged();
	// }
	// };

	private class SetPicTask extends AsyncTask<String, Void, Void> {

		@Override
		protected void onPreExecute() {
			progress_layout.setVisibility(View.VISIBLE);
			pic_fragment_layout.setVisibility(View.GONE);
		}

		@Override
		protected Void doInBackground(String... params) {
			if (!isCancelled()) {
				CwNews news = CwApplication.cwEntityManager().getSelectedNews();
				pictures = CwApplication.cwEntityManager().getPictures(
						context.getString(R.string.cw_url_append, news.getUrl()));
				// for (CwPicture pic : CwApplication.cwEntityManager().getPictures(
				// context.getString(R.string.cw_url_append, news.getUrl()))) {
				// if (news.getPictures() != null) {
				// news.getPictures().add(pic);
				// } else {
				// CwApplication.cwEntityManager().saveLoadNews(news);
				// news.getPictures().add(pic);
				// }
				// }
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// pictures = Arrays.asList(CwApplication.cwEntityManager().getSelectedNews().getPictures()
			// .toArray(new CwPicture[0]));
			content.removeView(pic_fragment_layout);
			content.addView(pic_fragment_layout);
			adapter = new LazyAdapter(context, pictures);

			imgView = (ImageView) pic_layout.findViewById(R.id.pic_view);
			imgView.setImageResource(R.drawable.cw_logo_thumb);

			gallery = (Gallery) pic_layout.findViewById(R.id.gallery);
			gallery.setAdapter(adapter);

			gallery.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
					if (pictures != null && pictures.size() > 0) {
						CwApplication.cwImageLoader().displayImage(pictures.get(position).getUrl(), context, imgView,
								false, R.drawable.cw_logo_thumb);
					}
				}
			});
			if (gallery.getChildCount() > 0) {
				gallery.setSelection(0);
			}
			progress_layout.setVisibility(View.GONE);
			pic_fragment_layout.setVisibility(View.VISIBLE);
		}

	}

	private MenuInflater menuInflater;

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if (menuInflater == null) {
			menuInflater = inflater;
		}
		if (isSelected()) {
			super.onCreateOptionsMenu(menu, inflater);
			menu.clear();
			menuInflater.inflate(R.menu.pics_menu, menu);
		}
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		if (menuInflater == null) {
			menuInflater = getActivity().getMenuInflater();
		}
		if (isSelected()) {
			super.onPrepareOptionsMenu(menu);
			menu.clear();
			menuInflater.inflate(R.menu.pics_menu, menu);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (isSelected()) {
			// Find which menu item has been selected
			switch (item.getItemId()) {
			// Check for each known menu item
			case (R.id.menu_pics_discard):
				CwApplication.cwImageLoader().clearCache();
				if (adapter != null) {
					adapter.notifyDataSetChanged();
				}
				if (task != null) {
					task.cancel(true);
				}
				task = new SetPicTask();
				break;
			case (R.id.menu_pics_refresh):
				if (task != null && task.getStatus().equals(AsyncTask.Status.PENDING)) {
					task.execute();
				} else if (task == null || task.getStatus().equals(AsyncTask.Status.FINISHED)) {
					task = new SetPicTask();
					task.execute();
				} else if (task != null && task.getStatus().equals(AsyncTask.Status.RUNNING)) {
					task.cancel(true);
				}
				break;

			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void backPressed() {
		if (task.getStatus().equals(AsyncTask.Status.RUNNING)) {
			task.cancel(true);
		}

	}

}