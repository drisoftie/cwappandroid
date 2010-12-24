package de.consolewars.android.app.tab.overview;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import de.consolewars.android.app.R;
import de.consolewars.android.app.tab.CwNavigationMainTabActivity;
import de.consolewars.android.app.util.HashEncrypter;
import de.consolewars.android.app.util.StyleSpannableStringBuilder;
import de.consolewars.api.data.AuthenticatedUser;
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
public class OverviewActivity extends Activity {

	private CwNavigationMainTabActivity mainTabs;

	private StyleSpannableStringBuilder styleStringBuilder;
	private String userName;
	private String hashpw;
	private int id = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/*
		 * TODO: Might become a source of error someday, if activity design changes. Would be better
		 * to handle it with intents.
		 */
		if (getParent().getParent() instanceof CwNavigationMainTabActivity) {
			mainTabs = (CwNavigationMainTabActivity) getParent().getParent();
		}

		styleStringBuilder = new StyleSpannableStringBuilder();

		ViewGroup overview = (ViewGroup) LayoutInflater.from(getParent()).inflate(
				R.layout.overview_layout, null);

		buildUsernameView(overview);

		TextView scrollText = (TextView) overview.findViewById(R.id.overview_info_text);
		scrollText
				.setText(styleStringBuilder
						.appendWithStyle(new ForegroundColorSpan(0xFFFF0000),
								"REST COMING SOON REST COMMING SOON REST COMMING SOON REST COMMING SOON REST COMMING SOON"));
		scrollText.setSelected(true);

		setContentView(overview);
	}

	/**
	 * Needs to be rewritten. Does too much.
	 * 
	 * @param parent
	 * @deprecated
	 */
	private void buildUsernameView(final View parent) {

		userName = "Kein User gespeichert";

		final EditText usrnmEdttxt = (EditText) parent.findViewById(R.id.overview_edttxt_username);
		final EditText passwEdttxt = (EditText) parent.findViewById(R.id.overview_edttxt_passw);
		final ImageView icon = (ImageView) parent.findViewById(R.id.overview_usericon);

		String tableName = getString(R.string.db_table_userdata_name);
		String columnId = getString(R.string.db_id_attribute);
		String columnUsername = getString(R.string.db_username_attribute);
		String columnPassw = getString(R.string.db_password_attribute);

		Cursor cursor = mainTabs.getDatabaseManager().fireQuery(tableName,
				new String[] { columnId, columnUsername, columnPassw }, null, null, null, null,
				getString(R.string.db_id_desc));

		if (cursor.getCount() > 0 && cursor.moveToFirst()) {
			for (String columnName : cursor.getColumnNames()) {
				if (columnName.matches(columnUsername)) {
					userName = cursor.getString(cursor.getColumnIndex(columnName));
				} else if (columnName.matches(columnPassw)) {
					try {
						hashpw = HashEncrypter.decrypt(getString(R.string.db_cry),
								cursor.getString(cursor.getColumnIndex(columnName)));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (columnName.matches(columnId)) {
					id = cursor.getInt(cursor.getColumnIndex(columnName));
				}
			}
			AuthenticatedUser authUser = null;
			try {
				authUser = mainTabs.getApiCaller().getApi().authenticate(userName, hashpw);
			} catch (ConsolewarsAPIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			loadPicture(icon, authUser.getUid());
		}
		cursor.close();

		usrnmEdttxt.setText(userName);

		Button confirmBttn = (Button) parent.findViewById(R.id.overview_set_user_bttn);
		confirmBttn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				userName = usrnmEdttxt.getText().toString();
				hashpw = passwEdttxt.getText().toString();

				Calendar calendar = GregorianCalendar.getInstance();

				if (id != -1) {
					try {
						mainTabs.getDatabaseManager().updateUserData(id, userName,
								HashEncrypter.encrypt(getString(R.string.db_cry), hashpw),
								calendar.getTimeInMillis());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					try {
						mainTabs.getDatabaseManager().insertUserData(userName,
								HashEncrypter.encrypt(getString(R.string.db_cry), hashpw),
								calendar.getTimeInMillis());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				try {
					// TODO bad, please rewrite
					AuthenticatedUser authUser = mainTabs.getApiCaller().getApi()
							.authenticate(userName, hashpw);
					loadPicture(icon, authUser.getUid());
				} catch (ConsolewarsAPIException e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Downloads the user picture and decodes it into a {@link Bitmap} to be set into an ImageView.
	 * 
	 * @param view
	 *            the ImageView
	 * @param uid
	 *            user id is needed to get the appropriate picture
	 */
	private void loadPicture(ImageView view, int uid) {
		URL newurl;
		Bitmap mIcon_val = null;
		try {
			newurl = new URL(getString(R.string.blogs_userpic_url, userName, uid, 100));
			mIcon_val = BitmapFactory.decodeStream(newurl.openConnection().getInputStream());
			view.setImageBitmap(mIcon_val);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Method to check if a user exists in the database.
	 * 
	 * @param user
	 *            the user
	 * @return
	 */
	@SuppressWarnings("unused")
	private int existsUser(String user) {

		String tableName = getString(R.string.db_table_userdata_name);
		String columnId = getString(R.string.db_id_attribute);
		String columnUsername = getString(R.string.db_username_attribute);
		String columnPassw = getString(R.string.db_password_attribute);

		Cursor cursor = mainTabs.getDatabaseManager().fireQuery(tableName,
				new String[] { columnId, columnUsername, columnPassw }, null, null, null, null,
				getString(R.string.db_id_desc));
		if (cursor.getCount() > 0 && cursor.moveToFirst()) {
			do {
				for (String columnName : cursor.getColumnNames()) {
					if (columnName.matches(columnUsername)) {
						if (cursor.getString(cursor.getColumnIndex(columnName)).matches(user)) {
							int id = cursor.getInt(cursor.getColumnIndex(columnName));
							cursor.close();
							return id;
						}
					}
				}
			} while (cursor.moveToNext());
		}
		cursor.close();
		return -1;
	}
}
