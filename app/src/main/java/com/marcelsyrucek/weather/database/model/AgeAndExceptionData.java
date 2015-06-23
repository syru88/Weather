package com.marcelsyrucek.weather.database.model;

import android.text.format.DateUtils;

import java.util.GregorianCalendar;

/**
 * Created by marcel on 18.6.2015.
 */
public class AgeAndExceptionData {

	/** 10 minutes */
	public static final int MAX_DIFF_IN_MINUTES = 1000 * 60 * 10;

	private long mReadTime;
	private boolean mIsError;
	private String mErrorText;

	public AgeAndExceptionData() {
		mReadTime = GregorianCalendar.getInstance().getTimeInMillis();
	}

	public boolean shoulReload() {
		if (GregorianCalendar.getInstance().getTimeInMillis() - mReadTime > MAX_DIFF_IN_MINUTES) {
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
}
