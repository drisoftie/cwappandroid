package de.consolewars.android.app.tab.overview;

import java.sql.SQLException;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import de.consolewars.android.app.CwApplication;
import de.consolewars.android.app.R;
import de.consolewars.android.app.db.domain.CwOptions;
import de.consolewars.android.app.tab.CwAbstractFragment;
import de.consolewars.android.app.tab.CwAbstractFragmentActivity;
import de.consolewars.android.app.tab.CwNavigationMainTabActivity;
import de.consolewars.android.app.view.ActionBar;

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

	public OptionsFragment(String title, int position) {
		super(title, position);
		setHasOptionsMenu(true);
		// task = new BuildSingleNewsAsyncTask();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// see this issue http://code.google.com/p/android/issues/detail?id=5067
		context = getActivity();
		this.inflater = LayoutInflater.from(context);
		ViewGroup options_layout = (ViewGroup) inflater.inflate(R.layout.options_fragment_layout, null);
		Button save_bttn = (Button) options_layout.findViewById(R.id.bttn_opt_save);
		save_bttn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new SaveOptionsTask().execute();
			}
		});
		return options_layout;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (((CwAbstractFragmentActivity) getActivity()).lastPosition == getPosition()) {
			initActionBar();
		}
		refresh();
	}

	@Override
	public void refresh() {
		if (((CwAbstractFragmentActivity) getActivity()).lastPosition == getPosition()) {
			initActionBar();
		}
		initSeekBar(R.id.seek_opt_blogs_action, R.id.txt_opt_blogs_max_action_value, CwApplication.cwEntityManager()
				.getOptions().getMaxBlogsAction());
		initSeekBar(R.id.seek_opt_blogs_scroll, R.id.txt_opt_blogs_max_scroll_value, CwApplication.cwEntityManager()
				.getOptions().getMaxBlogsScroll());
		initSeekBar(R.id.seek_opt_cmts_max, R.id.txt_opt_cmts_max_value, CwApplication.cwEntityManager().getOptions()
				.getMaxCmts());
		initSeekBar(R.id.seek_opt_news_action, R.id.txt_opt_news_max_action_value, CwApplication.cwEntityManager()
				.getOptions().getMaxNewsAction());
		initSeekBar(R.id.seek_opt_news_scroll, R.id.txt_opt_news_max_scroll_value, CwApplication.cwEntityManager()
				.getOptions().getMaxNewsScroll());
	}

	private void initSeekBar(int seekId, int valueId, int value) {
		SeekBar seekBar = (SeekBar) getActivity().findViewById(seekId);
		seekBar.setProgress(value - 1);
		final TextView seekBarValue = (TextView) getActivity().findViewById(valueId);
		seekBarValue.setText(String.valueOf(value));
		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				seekBarValue.setText(String.valueOf(progress + 1));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});
	}

	private class SaveOptionsTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			getActionBar().setProgressBarVisibility(View.VISIBLE);
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			CwOptions options = CwApplication.cwEntityManager().getOptions();
			options.setMaxBlogsAction(((SeekBar) getActivity().findViewById(R.id.seek_opt_blogs_action)).getProgress() + 1);
			options.setMaxBlogsScroll(((SeekBar) getActivity().findViewById(R.id.seek_opt_blogs_scroll)).getProgress() + 1);
			options.setMaxCmts(((SeekBar) getActivity().findViewById(R.id.seek_opt_cmts_max)).getProgress() + 1);
			options.setMaxNewsAction(((SeekBar) getActivity().findViewById(R.id.seek_opt_news_action)).getProgress() + 1);
			options.setMaxNewsScroll(((SeekBar) getActivity().findViewById(R.id.seek_opt_news_scroll)).getProgress() + 1);
			try {
				CwApplication.cwAppDataHandler().createOrUpdateOptions(options);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			getActionBar().setProgressBarVisibility(View.GONE);
		}

	}

	private void initActionBar() {
		if (context != null) {
			if (getActivity().getParent() instanceof CwNavigationMainTabActivity) {
				ActionBar actionBar = getActionBar();
				actionBar.removeAllActions();
				actionBar.setTitle(context.getString(R.string.home));
			}
		}
	}

	@Override
	public void backPressed() {
	}

}
