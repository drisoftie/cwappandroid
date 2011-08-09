package de.consolewars.android.app.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.XMLReader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Html.ImageGetter;
import android.text.Html.TagHandler;

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
public class TextViewHandler implements ImageGetter, TagHandler {

	private Context context;

	public TextViewHandler(Context context) {
		this.context = context;
	}

	@Override
	public Drawable getDrawable(String source) {
		try {
			if (source.startsWith("/")) {
				source = "http://www.consolewars.de" + source;
			}
			// Log.i("****PIC******", source);
			URL newurl = new URL(source);
			URLConnection con = newurl.openConnection();

			// Log.i("****PICSIZE******", String.valueOf(con.getContentLength()));
			/*
			 * 1st approach // Drawable pic = Drawable.createFromStream(newurl.openStream(), "src");
			 * Bitmap bm = BitmapFactory.decodeFile(myJpgPath, options);
			 */
			/*
			 * 2nd approach // BitmapFactory.Options options = new BitmapFactory.Options(); //
			 * options.inSampleSize = 16; // if (con.getContentLength() > 262144.0) { // Double
			 * dLength = Double.valueOf(con.getContentLength()); // Double quot =
			 * Math.log(Math.ceil(dLength / 262144.0)) / Math.log(2.0); // } Bitmap bmp =
			 * BitmapFactory.decodeStream(con.getInputStream(), null, options);
			 */
			/*
			 * 3rd approach // BufferedInputStream bis = new
			 * BufferedInputStream(con.getInputStream(), 8190); // // ByteArrayBuffer baf = new
			 * ByteArrayBuffer(50); // int current = 0; // while ((current = bis.read()) != -1) { //
			 * baf.append((byte) current); // } // byte[] imageData = baf.toByteArray(); Bitmap bmp
			 * = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
			 */
			/*
			 * 4th approach // // Decode image size // BitmapFactory.Options o = new
			 * BitmapFactory.Options(); // o.inJustDecodeBounds = true; //
			 * BitmapFactory.decodeStream(con.getInputStream(), null, o); // // // // The new size
			 * we want to scale to // final int REQUIRED_SIZE = 70; // // // Find the correct scale
			 * value. It should be the power of 2. // int width_tmp = o.outWidth, height_tmp =
			 * o.outHeight; // int scale = 1; // while (true) { // if (width_tmp / 2 < REQUIRED_SIZE
			 * || height_tmp / 2 < REQUIRED_SIZE) // break; // width_tmp /= 2; // height_tmp /= 2;
			 * // scale *= 2; // } // // // Decode with inSampleSize // BitmapFactory.Options o2 =
			 * new BitmapFactory.Options(); // o2.inSampleSize = scale; // Bitmap bmp =
			 * BitmapFactory.decodeStream(newurl.openConnection().getInputStream(), // null, // o2);
			 * Log.i("****PICLOAD******", (bmp != null) ? (bmp.toString()) : ("null"));
			 */

			// 5th approach like in:
			// http://stackoverflow.com/questions/1630258/android-problem-bug-with-threadsafeclientconnmanager-downloading-images
			Double scale = 1.0;
			BitmapFactory.Options options = new BitmapFactory.Options();

			// picture > 100kB ? scale down!
			if (con.getContentLength() > 100.0 * 1024.0) {
				Double dLength = Double.valueOf(con.getContentLength());
				// scale quotient determined by logarithm of base 2 and the relation of size to
				// 100kB
				Double quot = Math.log(Math.ceil(dLength / (100.0 * 1024.0))) / Math.log(2.0);
				if (quot > 1.0) {
					// scaling based on 2^scale quotient; if size is two and a half times bigger ->
					// scaling is 2; 25 times bigger -> scaling is 32; etc...
					scale = Math.pow(2, Math.round(quot));
					// Log.i("****RESIZE******", String.valueOf(scale));
				}
			}

			HttpGet httpRequest = null;
			httpRequest = new HttpGet(newurl.toURI());

			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response = (HttpResponse) httpclient.execute(httpRequest);

			HttpEntity entity = response.getEntity();
			BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
			InputStream instream = bufHttpEntity.getContent();
			options = new BitmapFactory.Options();
			options.inSampleSize = scale.intValue();
			Bitmap bmp = BitmapFactory.decodeStream(instream, null, options);

			Drawable pic = new BitmapDrawable(bmp);
			int width = pic.getIntrinsicWidth();
			int height = pic.getIntrinsicHeight();
			if (pic.getIntrinsicWidth() > 300) {
				Float fWidth = Float.valueOf(width);
				Float fHeight = Float.valueOf(height);
				float temp = 300f / fWidth;
				fHeight = fHeight * temp;
				height = fHeight.intValue();
				width = 300;
			}
			con.getInputStream().close();
			con = null;
			newurl = null;
			httpRequest = null;
			httpclient = null;
			response = null;
			entity = null;
			bufHttpEntity = null;
			bufHttpEntity = null;
			bmp = null;

			pic.setBounds(0, 0, width, height);
			return pic;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
		// if (tag.equalsIgnoreCase("strike") || tag.equals("s")) {
		// processStrike(opening, output);
		// }
	}

	// private void processStrike(boolean opening, Editable output) {
	// int len = output.length();
	// if (opening) {
	// output.setSpan(new StrikethroughSpan(), len, len, Spannable.SPAN_MARK_MARK);
	// } else {
	// Object obj = getLast(output, StrikethroughSpan.class);
	// int where = output.getSpanStart(obj);
	//
	// output.removeSpan(obj);
	//
	// if (where != len) {
	// output.setSpan(new StrikethroughSpan(), where, len,
	// Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	// }
	// }
	// }
	//
	// private Object getLast(Editable text, Class kind) {
	// Object[] objs = text.getSpans(0, text.length(), kind);
	//
	// if (objs.length == 0) {
	// return null;
	// } else {
	// for (int i = objs.length; i > 0; i--) {
	// if (text.getSpanFlags(objs[i - 1]) == Spannable.SPAN_MARK_MARK) {
	// return objs[i - 1];
	// }
	// }
	// return null;
	// }
	// }

}
