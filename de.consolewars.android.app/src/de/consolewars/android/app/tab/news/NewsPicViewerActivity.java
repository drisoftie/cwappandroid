package de.consolewars.android.app.tab.news;

import roboguice.activity.RoboActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import de.consolewars.android.app.R;

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
 * @author Alexander Dridiger
 * 
 */
public class NewsPicViewerActivity extends RoboActivity {
	private Gallery gallery;
	private ImageView imgView;

	private Integer[] imgIds = { R.drawable.splash_bg, R.drawable.cw_logo_skeletal, R.drawable.cw_logo,
			R.drawable.cw_logo_splash, R.drawable.cw_white_logo, R.drawable.icon, R.drawable.titlebar_bg };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.news_pic_viewer);

		imgView = (ImageView) findViewById(R.id.ImageView01);
		imgView.setImageResource(imgIds[0]);

		gallery = (Gallery) findViewById(R.id.examplegallery);
		gallery.setAdapter(new AddImageAdapter(this));

		gallery.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				imgView.setImageResource(imgIds[position]);
			}
		});
	}

	public class AddImageAdapter extends BaseAdapter {
		// private int GalItemBg;
		private Context context;

		public AddImageAdapter(Context c) {
			context = c;
			// TypedArray typArray =
			// obtainStyledAttributes(R.styleable.GalleryTheme);
			// GalItemBg = typArray.getResourceId(
			// R.styleable.GalleryTheme_android_galleryItemBackground, 0);
			// typArray.recycle();
		}

		public int getCount() {
			return imgIds.length;
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imgView = new ImageView(context);

			imgView.setImageResource(imgIds[position]);
			imgView.setScaleType(ImageView.ScaleType.FIT_XY);
			imgView.setLayoutParams(new Gallery.LayoutParams(100, 100));
			// imgView.setBackgroundResource(GalItemBg);

			return imgView;
		}

		public float getScale(boolean focused, int offset) {
			/* Formula: 1 / (2 ^ offset) */
			return Math.max(0, 1.0f / (float) Math.pow(2, Math.abs(offset)));
		}
	}
}
