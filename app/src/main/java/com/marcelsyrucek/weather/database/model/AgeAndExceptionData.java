package com.marcelsyrucek.weather.database.model;

import android.text.format.DateUtils;

import com.marcelsyrucek.weather.WeatherConfig;

import java.util.GregorianCalendar;

/**
 * Created by marcel on 21.6.2015.
 */
public class AgeAndExceptionData {

	private long mReadTime;
	private boolean mIsError;
	private String mErrorText;

	protected String mId;

	public AgeAndExceptionData() {
		mReadTime = GregorianCalendar.getInstance().getTimeInMillis();
	}

	public boolean shoulReload() {
		if (GregorianCalendar.getInstance().getTimeInMillis() - mReadTime > WeatherConfig.DATA_VALID_FOR_IN_MILLIS) {
			return true;
		} else {
			return false;
		}
	}

	public String getAgo() {
		return String.valueOf(DateUtils.getRelativeTimeSpanString(mReadTime));
	}

	public boolean isError() {
		return mIsError;
	}

	public void setIsError(boolean isError) {
		mIsError = isError;
	}

	public String getErrorText() {
		return mErrorText;
	}

	public void setErrorText(String errorText) {
		mErrorText = errorText;
	}

	public String getId() {
		return mId;
	}

	public void setId(String id) {
		mId = id;
	}
}
