package de.consolewars.android.app.tab.overview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import de.consolewars.android.app.R;
import de.consolewars.android.app.tab.CwAbstractFragment;

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
 * Activity showing and handling options.
 * 
 * @author Alexander Dridiger
 */
public class OptionsFragment extends CwAbstractFragment {

	private Context context;

	private LayoutInflater inflater;

	public OptionsFragment() {
	}

	public OptionsFragment(String title) {
		super(title);
		setHasOptionsMenu(true);
		// task = new BuildSingleNewsAsyncTask();
	}

	// @Override
	// public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	// // see this issue http://code.google.com/p/android/issues/detail?id=5067
	// context = getActivity();
	// this.inflater = LayoutInflater.from(context);
	// singlenews_layout = (ViewGroup) inflater.inflate(R.layout.fragment_progress_layout, null);
	// singlenews_layout.setBackgroundColor(context.getResources().getColor(R.color.singlenews_bg));
	// progress_layout = CwApplication.cwViewUtil().getCenteredProgressBarLayout(inflater, R.string.singlenews);
	// ViewGroup progress = (ViewGroup) singlenews_layout.findViewById(R.id.progressbar);
	// progress.addView(progress_layout);
	// progress_layout.setVisibility(View.GONE);
	// return singlenews_layout;
	// }

	@Override
	public void onPause() {
		super.onPause();
		// if (getActivity().findViewById(R.id.singlenews_video) != null) {
		// WebView localWebView = (WebView) getActivity().findViewById(R.id.singlenews_video);
		// localWebView.loadUrl("about:blank");
		// }
	}

	@Override
	public void onResume() {
		super.onResume();
		// news = CwApplication.cwEntityManager().getSelectedNews();
		// singlenews_layout = (ViewGroup) getView();
		// content = (ViewGroup) singlenews_layout.findViewById(R.id.content);
		// content.removeAllViews();
		// singlenews_fragment = (ViewGroup) inflater.inflate(R.layout.singlenews_fragment_layout, null);
		// progress_layout.setVisibility(View.GONE);
		//
		// if (task.getStatus().equals(AsyncTask.Status.PENDING)) {
		// task.execute();
		// } else if (task.getStatus().equals(AsyncTask.Status.FINISHED)) {
		// task = new BuildSingleNewsAsyncTask();
		// task.execute();
		// } else if (task.getStatus().equals(AsyncTask.Status.RUNNING)) {
		// task.cancel(true);
		// }
	}

	@Override
	public void backPressed() {
		// if (task.getStatus().equals(AsyncTask.Status.RUNNING)) {
		// task.cancel(true);
		// }
	}

	private MenuInflater menuInflater;

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if (isSelected()) {
			super.onCreateOptionsMenu(menu, inflater);
			menu.clear();
			inflater.inflate(R.menu.singlenews_menu, menu);
		}
		menuInflater = inflater;
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		if (isSelected()) {
			super.onPrepareOptionsMenu(menu);
			menu.clear();
			menuInflater.inflate(R.menu.singlenews_menu, menu);
		}
	}
}
