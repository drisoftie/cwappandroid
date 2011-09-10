package de.consolewars.android.app.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.method.LinkMovementMethod;
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

	private Map<String, Bitmap> picCache;

	public Map<String, Bitmap> getPicCache() {
		if (picCache == null) {
			picCache = new HashMap<String, Bitmap>();
		}
		return picCache;
	}

	public ViewUtility() {

	}

	public ViewUtility(Context context) {
		this.context = context;
	}

	public ViewGroup getCenteredProgressBarLayout(LayoutInflater layoutInflater, int textId) {
		ViewGroup progress_layout = (ViewGroup) layoutInflater.inflate(R.layout.centered_progressbar, null);

		TextView text = (TextView) progress_layout.findViewById(R.id.centered_progressbar_text);
		text.setText(context.getString(R.string.loading, context.getString(textId)));

		return progress_layout;
	}

	/**
	 * Sets HTML links in the given TextView as clickable.
	 * 
	 * @param view
	 * @param url
	 */
	public void setClickableTextView(TextView view) {
		view.setMovementMethod(LinkMovementMethod.getInstance());
	}

	/**
	 * Returns a user icon as a Bitmap with the given size (longest edge).
	 * 
	 * @param userId
	 *            to get the icon from
	 * @param iconSize
	 *            longest edge of the returned icon
	 * @return
	 */
	public Bitmap getUserIcon(int userId, int iconSize) {
		return getBitmap(context.getString(R.string.userpic_url, userId, iconSize));
	}

	/**
	 * Sets a user icon to the given {@link ImageView}.
	 * 
	 * @param view
	 * @param userId
	 *            to get the icon from
	 * @param iconSize
	 *            longest edge of the icon
	 */
	public void setUserIcon(ImageView view, int userId, int iconSize) {
		setIcon(view, context.getString(R.string.userpic_url, userId, iconSize));
	}

	/**
	 * Sets a category icon to the given {@link ImageView}.
	 * 
	 * @param view
	 * @param categoryShort
	 *            name of the category to get the icon from
	 */
	public void setCategoryIcon(ImageView view, String categoryShort) {
		setIcon(view, context.getString(R.string.catpic_url, categoryShort));
	}

	/**
	 * Downloads a picture from the given url and returns it as a Bitmap.
	 * 
	 * @param url
	 * @return
	 */
	private Bitmap getBitmap(String url) {
		if (getPicCache().get(url) != null) {
			return getPicCache().get(url);
		}
		Bitmap bitmap = null;
		URL newurl = null;
		try {
			newurl = new URL(url);
			bitmap = BitmapFactory.decodeStream(newurl.openConnection().getInputStream());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		newurl = null;
		getPicCache().put(url, bitmap);
		return bitmap;
	}

	public void cacheBitmap(String url, Bitmap bitmap) {
		if (bitmap != null && url != null) {
			getPicCache().put(url, bitmap);
		}
	}

	public Bitmap getCachedBitmap(String url) {
		if (getPicCache().get(url) != null) {
			return getPicCache().get(url);
		}
		return null;
	}

	/**
	 * Sets an icon to the given {@link ImageView}.
	 * 
	 * @param view
	 * @param url
	 */
	private void setIcon(ImageView view, String url) {
		view.setImageBitmap(getBitmap(url));
	}
}
