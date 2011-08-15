package de.consolewars.android.app.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.consolewars.android.app.R;

/**
 * Utility for getting views.
 * 
 * @author w4yn3
 */
@Singleton
public class ViewUtility {
	@Inject
	private Context context;

	public ViewGroup getCenteredProgressBarLayout(LayoutInflater layoutInflater, int textId) {
		ViewGroup progress_layout = (ViewGroup) layoutInflater.inflate(R.layout.centered_progressbar, null);

		TextView text = (TextView) progress_layout.findViewById(R.id.centered_progressbar_text);
		text.setText(context.getString(R.string.loading, context.getString(textId)));

		return progress_layout;
	}

	public void setUserIcon(ImageView view, String requestUrl) {
		URL newurl;
		Bitmap mIcon_val = null;
		try {
			newurl = new URL(requestUrl);
			mIcon_val = BitmapFactory.decodeStream(newurl.openConnection().getInputStream());
			view.setImageBitmap(mIcon_val);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
