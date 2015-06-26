package com.marcelsyrucek.weather.database;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.marcelsyrucek.weather.utility.Logcat;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by marcel on 22.6.2015.
 */
public class GeneralDatabase<T extends JsonRecord> {

	public final String TAG;

	final Class<T> mTypeClass;

	private Context mContext;
	private SharedPreferences mSharedPreferences;
	private SharedPreferences.Editor mEditor;

	public GeneralDatabase(Context context, String databaseName, Class<T> typeClass) {
		mContext = context.getApplicationContext();
		TAG = "DATABASE_" + databaseName;
		mTypeClass = typeClass;
		mSharedPreferences = context.getSharedPreferences(databaseName, Context.MODE_PRIVATE);
		mEditor = mSharedPreferences.edit();
	}

	/**
	 * Add entry to database.
	 * @param entry
	 * @return
	 */
	public boolean addEntry(T entry) {
		return addEntry(entry.getId(), entry);
	}

	/**
	 * Add entry with special id (current city, displayed city...) to database.
	 * @param id
	 * @param entry
	 * @return
	 */
	public boolean addEntry(String id, T entry) {
		String databaseEntry = mSharedPreferences.getString(id, null);
		if (databaseEntry != null) {
			Logcat.i(TAG, "The entry " + entry.getName() + " is already in database.");
			return false;
		}

		Gson gson = new Gson();
		mEditor.putString(id, gson.toJson(entry));
		mEditor.commit();

		return true;
	}

	/**
	 *
	 * @param entry
	 * @return true if the entry was edited, false if the entry was added
	 */
	public boolean editEntry(T entry) {
		return editEntry(entry.getId(), entry);
	}

	/**
	 *
	 * @param key
	 * @param entry
	 * @return true if the entry was edited, false if the entry was added
	 */
	public boolean editEntry(String key, T entry) {
		String databaseEntry = mSharedPreferences.getString(key, null);
		if (databaseEntry == null) {
			Logcat.i(TAG, "editEntry hasn't found, so add it: " + key);
			addEntry(key, entry);
			return false;
		} else {
			Gson gson = new Gson();
			mEditor.putString(key, gson.toJson(entry));
			mEditor.commit();
			return true;
		}
	}

	public boolean removeEntryWithId(String key) {
		String databaseEntry = mSharedPreferences.getString(key, null);
		if (databaseEntry == null) {
			Logcat.i(TAG, "The entry with key" + key + " wasn't found in database.");
			return false;
		}
		mEditor.remove(key);
		mEditor.commit();

		return true;
	}

	public boolean removeEntry(T entry) {
		String databaseEntry = mSharedPreferences.getString(entry.getId(), null);
		if (databaseEntry == null) {
			Logcat.i(TAG, "The entry " + entry.getName() + " wasn't found in database.");
			return false;
		}

		mEditor.remove(entry.getId());
		mEditor.commit();

		return true;
	}

	public T getEntryWithId(String id) {
		String databaseEntry = mSharedPreferences.getString(id, null);
		if (databaseEntry == null) {
			Logcat.i(TAG, "The entry with id: " + id + " couldn't be found");
			return null;
		}

		Gson gson = new Gson();

		return gson.fromJson(databaseEntry, this.mTypeClass);
	}

	public boolean isEntryInDatabase(String id) {
		String databaseEntry = mSharedPreferences.getString(id, null);
		return databaseEntry == null ? false : true;
	}

	public ArrayList<T> getEntries() {
		Map<String, ?> allJsonEntries = mSharedPreferences.getAll();
		ArrayList<T> entries = new ArrayList<>();

		Gson gson = new Gson();
		String json;

		for (Map.Entry<String, ?> entry : allJsonEntries.entrySet()) {
			json = mSharedPreferences.getString(entry.getKey(), "");
			Logcat.i(TAG, "key: " + entry.getKey() + ", json: " + json);
			entries.add(gson.fromJson(json, this.mTypeClass));
		}


		return entries;
	}
}
