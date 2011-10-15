package de.consolewars.android.app.tab.blogs;

import java.util.Calendar;

import roboguice.activity.RoboActivity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.inject.Inject;

import de.consolewars.android.app.CwEntityManager;
import de.consolewars.android.app.CwManager;
import de.consolewars.android.app.R;
import de.consolewars.android.app.db.domain.CwBlog;
import de.consolewars.android.app.tab.CwBasicActivityGroup;
import de.consolewars.android.app.util.DateUtility;

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
 * Activity to handle blogs writing and editing.
 * 
 * @author Alexander Dridiger
 */
public class BlogsWriterActivity extends RoboActivity {

	@Inject
	private CwManager cwManager;
	@Inject
	private CwEntityManager cwEntityManager;
	
	private View blogswriter_layout;

	private int id = -1;

	private int mYear;
	private int mMonth;
	private int mDay;
	private int mHour;
	private int mMinute;

	private static final int DATE_DIALOG_ID = 0;
	private static final int TIME_DIALOG_ID = 1;

	private TextView dateView;
	private TextView timeView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		blogswriter_layout = (View) LayoutInflater.from(getParent()).inflate(R.layout.blogswriter_layout, null);
		new BuildBlogAsyncTask().execute();
	}

	private void initView() {
		dateView = (TextView) blogswriter_layout.findViewById(R.id.blogswriter_txt_showdate);
		timeView = (TextView) blogswriter_layout.findViewById(R.id.blogswriter_txt_showtime);

		Button datePicker = (Button) blogswriter_layout.findViewById(R.id.blogswriter_bttn_date);
		datePicker.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(DATE_DIALOG_ID);
			}
		});

		Button timePicker = (Button) blogswriter_layout.findViewById(R.id.blogswriter_bttn_time);
		timePicker.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(TIME_DIALOG_ID);
			}
		});

		Button submit = (Button) blogswriter_layout.findViewById(R.id.blogswriter_bttn_submit);
		submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new SubmitBlogAsyncTask().execute();
			}
		});

		// get the current date
		final Calendar c = Calendar.getInstance();
		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);
		mHour = c.get(Calendar.HOUR_OF_DAY);
		mMinute = c.get(Calendar.MINUTE);
		setDateView();
		setTimeView();
	}

	private void setDateView() {
		dateView.setText(new StringBuilder()
		// Month is 0 based so add 1
				.append(mDay).append(".").append(pad(mMonth + 1)).append(".").append(mYear));
	}

	private void setTimeView() {
		timeView.setText(new StringBuilder().append(pad(mHour)).append(":").append(pad(mMinute)));
	}

	private String pad(int c) {
		if (c >= 10)
			return String.valueOf(c);
		else
			return "0" + String.valueOf(c);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIALOG_ID:
			return new DatePickerDialog(getParent(), new OnDateSetListener() {
				@Override
				public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
					mYear = year;
					mMonth = monthOfYear;
					mDay = dayOfMonth;
					setDateView();
				}
			}, mYear, mMonth, mDay);
		case TIME_DIALOG_ID:
			return new TimePickerDialog(getParent(), new OnTimeSetListener() {
				@Override
				public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
					mHour = hourOfDay;
					mMinute = minute;
					setTimeView();
				}
			}, mHour, mMinute, false);
		}
		return null;
	}

	/**
	 * Asynchronous task to submit a blog.
	 * 
	 * @author Alexander Dridiger
	 */
	private class BuildBlogAsyncTask extends AsyncTask<Void, Void, Void> {

		private ProgressBar progressBar;

		@Override
		protected void onPreExecute() {
			setContentView(R.layout.centered_progressbar);

			TextView text = (TextView) findViewById(R.id.centered_progressbar_text);
			text.setText(getString(R.string.loading, "Einzelblog"));

			progressBar = (ProgressBar) findViewById(R.id.centered_progressbar);
			progressBar.setProgress(0);
		}

		@Override
		protected Void doInBackground(Void... params) {
			initView();
			if (BlogsWriterActivity.this.getIntent().hasExtra(getString(R.string.id))
					&& BlogsWriterActivity.this.getIntent().getExtras().getInt(getString(R.string.id), -1) != -1) {
				id = BlogsWriterActivity.this.getIntent().getExtras().getInt(getString(R.string.id));
				CwBlog blog = null;
				blog = cwEntityManager.getBlogSingle(id, false);
				EditText title = (EditText) blogswriter_layout.findViewById(R.id.blogswriter_edttxt_title);
				title.setText(blog.getTitle());

				EditText content = (EditText) blogswriter_layout.findViewById(R.id.blogswriter_edttxt_content);
				content.setText(blog.getArticle());

				CheckBox visible = (CheckBox) blogswriter_layout.findViewById(R.id.blogswriter_check_visible);
				visible.setChecked(blog.isVisible());

				CheckBox isnewsblog = (CheckBox) blogswriter_layout.findViewById(R.id.blogswriter_check_newsblog);
				isnewsblog.setChecked(blog.getMode().matches("NORMAL") ? false : true);

				String date = DateUtility.createDate(blog.getUnixtime() * 1000L, "dd.MM.yyyy,HH:mm").toString();
				String[] temp = date.split(",");
				dateView.setText(temp[0]);
				timeView.setText(temp[1]);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			setContentView(blogswriter_layout);
		}
	}

	/**
	 * Asynchronous task to submit a blog.
	 * 
	 * @author Alexander Dridiger
	 */
	private class SubmitBlogAsyncTask extends AsyncTask<Void, Void, Void> {

		private boolean dowork = false;

		@Override
		protected void onPreExecute() {
			Toast.makeText(BlogsWriterActivity.this, getResources().getString(R.string.blogswriter_sending),
					Toast.LENGTH_SHORT).show();
			EditText content = (EditText) findViewById(R.id.blogswriter_edttxt_content);
			if (content.getText().length() >= 150) {
				dowork = true;
			}
		}

		@Override
		protected Void doInBackground(Void... params) {
			if (dowork) {
				EditText title = (EditText) findViewById(R.id.blogswriter_edttxt_title);
				EditText content = (EditText) findViewById(R.id.blogswriter_edttxt_content);
				EditText tags = (EditText) findViewById(R.id.blogswriter_edttxt_tags);
				CheckBox visible = (CheckBox) findViewById(R.id.blogswriter_check_visible);
				CheckBox cmts = (CheckBox) findViewById(R.id.blogswriter_check_comments_allowed);
				CheckBox isnewsblog = (CheckBox) findViewById(R.id.blogswriter_check_newsblog);
				cwManager.sendBlog(title.getText().toString(), content.getText().toString(), dateView.getText()
						.toString(), timeView.getText().toString(), cmts.isChecked(), tags.getText().toString(),
						visible.isChecked(), isnewsblog.isChecked(), id);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (dowork) {
				Toast.makeText(BlogsWriterActivity.this, getResources().getString(R.string.blogswriter_sent),
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(BlogsWriterActivity.this, getResources().getString(R.string.blogswriter_too_short),
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public void onBackPressed() {
		if (getParent() instanceof CwBasicActivityGroup) {
			((CwBasicActivityGroup) getParent()).back();
		}
	}
}
