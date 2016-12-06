package com.bakkenbaeck.sol.util;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;

import com.bakkenbaeck.sol.R;
import com.bakkenbaeck.sol.location.CurrentCity;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.Seconds;

public class DailyMessage {

    private final ThreeDayPhases threeDayPhases;
    private final CurrentCity currentCity;
    private final Context context;

    public DailyMessage(final Context context) {
        this.context = context;
        this.currentCity = new CurrentCity(context);
        this.threeDayPhases = new ThreeDayPhases();
    }

    public long getTomorrowsSunrise(final Location location) {
        this.threeDayPhases.init(location);
        return this.threeDayPhases.getTomorrowsSunrise();
    }

    public String generate(final Location location) {
        this.threeDayPhases.init(location);

        final Period dayLengthChange = this.threeDayPhases.getDayLengthChangeBetweenTodayAndYesterday();
        final String city = getNearestCity(location);
        final String diffText = getDiffText(dayLengthChange);
        final String numMinutesText = getNumberMinutesText(dayLengthChange);

        return getRawMessage()
                .replace("{city}", city)
                .replace("{numMinutes}", numMinutesText)
                .replace("{moreOrLess}", diffText);
    }

    private String getNearestCity(final Location location) {
        return this.currentCity.get(location.getLatitude(), location.getLongitude());
    }

    @NonNull
    private String getDiffText(final Period dayLengthChange) {
        final Seconds dayLengthChangeInSeconds = dayLengthChange.toStandardSeconds();

        return
                dayLengthChangeInSeconds.isGreaterThan(Seconds.ZERO)
                        ? context.getResources().getString(R.string.more)
                        : context.getResources().getString(R.string.less);
    }

    @NonNull
    private String getNumberMinutesText(final Period dayLengthChange) {
        final int numMinutes =
                Math.abs(dayLengthChange.getMinutes())
                        + Math.abs(dayLengthChange.getSeconds()) / 30;
        return this.context.getResources().getQuantityString(R.plurals.minutes, numMinutes, numMinutes);
    }

    private String getRawMessage() {
        final int dayOfYear = DateTime.now().dayOfYear().get();
        final String[] allMessages = this.context.getResources().getStringArray(R.array.daily_message);
        final int messagePosition = dayOfYear % allMessages.length;
        return allMessages[messagePosition];
    }
}