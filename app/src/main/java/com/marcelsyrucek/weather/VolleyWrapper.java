package com.marcelsyrucek.weather;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.ImageLoader;
import com.facebook.stetho.okhttp.StethoInterceptor;
import com.marcelsyrucek.weather.utility.OkHttpStack;
import com.squareup.okhttp.OkHttpClient;

/**
 * Created by marcel on 17.6.2015.
 */
public class VolleyWrapper {

	private static VolleyWrapper mInstance;
	private RequestQueue mRequestQueue;
	private ImageLoader mImageLoader;
	private static Context mCtx;

	private VolleyWrapper(Context context) {
		mCtx = context;
		mRequestQueue = getRequestQueue();

		mImageLoader = new ImageLoader(mRequestQueue,
				new ImageLoader.ImageCache() {
					private final LruCache<String, Bitmap>
							cache = new LruCache<String, Bitmap>(20);

					@Override
					public Bitmap getBitmap(String url) {
						return cache.get(url);
					}

					@Override
					public void putBitmap(String url, Bitmap bitmap) {
						cache.put(url, bitmap);
					}
				});
	}

	public static synchronized VolleyWrapper getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new VolleyWrapper(context.getApplicationContext());
		}
		return mInstance;
	}

	public RequestQueue getRequestQueue() {
		if (mRequestQueue == null) {
			// getApplicationContext() is key, it keeps you from leaking the
			// Activity or BroadcastReceiver if someone passes one in.
			OkHttpClient client = new OkHttpClient();
			client.networkInterceptors().add(new StethoInterceptor());
			mRequestQueue = com.android.volley.toolbox.Volley.newRequestQueue(mCtx.getApplicationContext(), new OkHttpStack(client));
		}
		return mRequestQueue;
	}

	public <T> void addToRequestQueue(Request<T> req) {
		getRequestQueue().add(req);
	}

	public ImageLoader getImageLoader() {
		return mImageLoader;
	}
}

