package de.consolewars.android.app.tab;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.View;
import de.consolewars.android.app.CWApplication;

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
 * Basic implementation of an {@link ActivityGroup} supporting {@link Activity} switching and
 * {@link View} caching.
 * 
 * @author Alexander Dridiger
 */
public abstract class CwBasicActivityGroup extends ActivityGroup implements ICwActivityGroup {

	private List<View> viewCache;

	public void replaceView(View view) {
		if (viewCache == null) {
			viewCache = new ArrayList<View>();
		}
		viewCache.add(view);
		setContentView(view);
	}

	public void back() {
		if (viewCache.size() > 1) {
			viewCache.remove(viewCache.size() - 1);
			setContentView(viewCache.get(viewCache.size() - 1));
		} else {
			AlertDialog.Builder dialog = new AlertDialog.Builder(this).setMessage("CW-App beenden?")
					.setCancelable(false).setPositiveButton("Ja", new OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							if (getParent() instanceof CwNavigationMainTabActivity) {
								if (CWApplication.getInstance().getDataHandler().loadCurrentUser()) {
									CWApplication.getInstance().getDataHandler().getDatabaseManager()
											.updateDate(
													CWApplication.getInstance().getDataHandler()
															.getUserDBId(),
													GregorianCalendar.getInstance().getTimeInMillis());
								}
								CWApplication.getInstance().getDataHandler().getDatabaseManager().closeDB();
							}
							finish();
						}
					}).setNegativeButton("Nein", new OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
			dialog.create().show();
		}
	}

	/**
	 * Clears the View cache.
	 */
	public void resetCache() {
		viewCache = new ArrayList<View>();
	}

	@Override
	public void onBackPressed() {
		this.back();
	}

	public CwNavigationMainTabActivity getMainTabActivity() {
		if (getParent() instanceof CwNavigationMainTabActivity) {
			return (CwNavigationMainTabActivity) getParent();
		}
		return null;
	}
}
