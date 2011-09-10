package de.consolewars.android.app.tab;

import java.sql.SQLException;
import java.util.Date;

import roboguice.util.Ln;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.j256.ormlite.dao.Dao;

import de.consolewars.android.app.CwApplication;
import de.consolewars.android.app.CwManager;
import de.consolewars.android.app.R;
import de.consolewars.android.app.db.AppDataHandler;
import de.consolewars.android.app.db.domain.CwUser;

public abstract class CwAbstractFragmentActivity extends FragmentActivity {

	protected CwManager cwManager;
	protected AppDataHandler cwAppDataHandler;
	protected Dao<CwUser, Integer> cwUserDao;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		cwManager = CwApplication.cwManager();
		cwAppDataHandler = CwApplication.cwAppDataHandler();
		cwUserDao = CwApplication.cwUserDao();
	}

	@Override
	public void onBackPressed() {
		if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
			AlertDialog.Builder dialog = new AlertDialog.Builder(this).setMessage(getString(R.string.close_app))
					.setCancelable(false).setPositiveButton(getString(R.string.yes), new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (getParent() instanceof CwNavigationMainTabActivity) {
								if (cwAppDataHandler.loadCurrentUser()) {
									CwUser cwUser = cwAppDataHandler.getCwUser();
									cwUser.setDate(new Date());
									cwUser.setLastBlogId(cwManager.getNewestBlog());
									cwUser.setLastNewsId(cwManager.getNewestNews());
									try {
										cwUserDao.update(cwUser);
									} catch (SQLException e) {
										Ln.e(e);
									}
								}
							}
							finish();
						}
					}).setNegativeButton(getString(R.string.no), new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
			dialog.create().show();
		} else {
			getSupportFragmentManager().popBackStack();
		}
	}
}