package de.consolewars.android.app.pics;

import java.util.Collection;

import android.app.Activity;
import android.content.res.TypedArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import de.consolewars.android.app.CwApplication;
import de.consolewars.android.app.R;
import de.consolewars.android.app.db.domain.CwPicture;

public class LoaderAdapter extends BaseAdapter {

	private Activity activity;
	private Collection<CwPicture> pictures;

	int galItemBg;

	public LoaderAdapter(Activity a, Collection<CwPicture> pictures) {
		activity = a;
		this.pictures = pictures;

		TypedArray typArray = activity.obtainStyledAttributes(R.styleable.GalleryTheme);
		galItemBg = typArray.getResourceId(R.styleable.GalleryTheme_android_galleryItemBackground, 0);
		typArray.recycle();
	}

	public int getCount() {
		return pictures != null ? pictures.size() : 0;
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView vi = (ImageView) convertView;
		if (convertView == null) {
			vi = new ImageView(activity);
			vi.setBackgroundResource(galItemBg);
		}

		CwApplication.cwImageLoader().displayImage(pictures.toArray(new CwPicture[0])[position].getThumbUrl(),
				activity, vi, true, R.drawable.cw_logo_thumb);
		return vi;
	}
}