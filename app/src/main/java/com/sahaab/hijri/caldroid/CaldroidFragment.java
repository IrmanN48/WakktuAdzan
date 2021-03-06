package com.sahaab.hijri.caldroid;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;

import com.antonyt.infiniteviewpager.InfinitePagerAdapter;
import com.antonyt.infiniteviewpager.InfiniteViewPager;
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar;
import com.irman.waktuadzan.MainActivity;
import com.irman.waktuadzan.R;
import com.sahaab.hijricalendar.HijriCalendarDate;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import hirondelle.date4j.DateTime;

//import android.support.v4.app.DialogFragment;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentManager;
//import android.support.v4.view.ViewPager.OnPageChangeListener;

/**
 * Caldroid is a fragment that display calendar with dates in a month. Caldroid
 * can be used as embedded fragment, or as dialog fragment. <br/>
 * <br/>
 * Caldroid fragment includes 4 main parts:<br/>
 * <br/>
 * 1) Month title view: show the month and year (e.g MARCH, 2013) <br/>
 * <br/>
 * 2) Navigation arrows: to navigate to next month or previous month <br/>
 * <br/>
 * 3) Weekday gridview: contains only 1 row and 7 columns. To display
 * "SUN, MON, TUE, WED, THU, FRI, SAT" <br/>
 * <br/>
 * 4) An infinite view pager that allow user to swipe left/right to change
 * month. This library is taken from
 * https://github.com/antonyt/InfiniteViewPager <br/>
 * <br/>
 * This infinite view pager recycles 4 fragment, each fragment contains a grid
 * view with 7 columns to display the dates in month. Whenever user swipes
 * different screen, the date grid views are updated. <br/>
 * <br/>
 * Caldroid fragment supports setting min/max date, selecting dates in a range,
 * setting disabled dates, highlighting today. It includes convenient methods to
 * work with date and string, enable or disable the navigation arrows. User can
 * also  left/right to change months.<br/>
 * <br/>
 * Caldroid code is simple and clean partly because of powerful JODA DateTime
 * library!
 * 
 * @author thomasdao
 * 
 */

@SuppressWarnings("deprecation")
@SuppressLint("DefaultLocale")
public class CaldroidFragment extends DialogFragment {
	public String TAG = "CaldroidFragment";

	/**
	 * Weekday conventions
	 */
	public static int SUNDAY = 1;
	public static int MONDAY = 2;
	public static int TUESDAY = 3;
	public static int WEDNESDAY = 4;
	public static int THURSDAY = 5;
	public static int FRIDAY = 6;
	public static int SATURDAY = 7;

	/**
	 * Flags to display month
	 */
	private static final int MONTH_YEAR_FLAG = DateUtils.FORMAT_SHOW_DATE
			| DateUtils.FORMAT_NO_MONTH_DAY | DateUtils.FORMAT_SHOW_YEAR;

	/**
	 * First day of month time
	 */
	private Time firstMonthTime = new Time();

	/**
	 * Reuse formatter to print "MMMM yyyy" format
	 */
	private final StringBuilder monthYearStringBuilder = new StringBuilder(50);
	private Formatter monthYearFormatter = new Formatter(
			monthYearStringBuilder, Locale.getDefault());

	/**
	 * To customize the selected background drawable and text color
	 */
	public static int selectedBackgroundDrawable = -1;
	public static int selectedTextColor = Color.BLACK;

	public final static int NUMBER_OF_PAGES = 4;

	/**
	 * To customize the disabled background drawable and text color
	 */
	public static int disabledBackgroundDrawable = -1;
	public static int disabledTextColor = Color.GRAY;

	/**
	 * Caldroid view components
	 */
	private ImageView leftArrowButton;
	private ImageView rightArrowButton;
	private LinearLayout lay_cal;
	private TextView monthTitleTextView;
	private TextView hijrimonthTitleTextView;
	private GridView weekdayGridView;
	private InfiniteViewPager dateViewPager;
	private DatePageChangeListener pageChangeListener;
	private ArrayList<DateGridFragment> fragments;

	/**
	 * Initial params key
	 */
	public final static String DIALOG_TITLE = "dialogTitle";
	public final static String MONTH = "month";
	public final static String YEAR = "year";
	public final static String SHOW_NAVIGATION_ARROWS = "showNavigationArrows";
	public final static String SHOW_LANG = "showLang";
	
	public final static String DISABLE_DATES = "disableDates";
	public final static String SELECTED_DATES = "selectedDates";
	public final static String MIN_DATE = "minDate";
	public final static String MAX_DATE = "maxDate";
	public final static String ENABLE_SWIPE = "enableSwipe";
	public final static String START_DAY_OF_WEEK = "startDayOfWeek";
	public final static String MANUAL_CORRECTION = "manualCorrection";
	
	public final static String SIX_WEEKS_IN_CALENDAR = "sixWeeksInCalendar";
	public final static String ENABLE_CLICK_ON_DISABLED_DATES = "enableClickOnDisabledDates";

	/**
	 * For internal use
	 */
	public final static String _MIN_DATE_TIME = "_minDateTime";
	public final static String _MAX_DATE_TIME = "_maxDateTime";
	public final static String _BACKGROUND_FOR_DATETIME_MAP = "_backgroundForDateTimeMap";
	public final static String _TEXT_COLOR_FOR_DATETIME_MAP = "_textColorForDateTimeMap";

	/**
	 * Initial data
	 */
	protected String dialogTitle;
	protected int month = -1;
	protected int year = -1;
	protected ArrayList<DateTime> disableDates = new ArrayList<DateTime>();
	protected ArrayList<DateTime> selectedDates = new ArrayList<DateTime>();
	protected DateTime minDateTime;
	protected DateTime maxDateTime;
	protected ArrayList<DateTime> dateInMonthsList;

	/**
	 * caldroidData belongs to Caldroid
	 */
	protected HashMap<String, Object> caldroidData = new HashMap<String, Object>();

	/**
	 * extraData belongs to client
	 */
	protected HashMap<String, Object> extraData = new HashMap<String, Object>();

	/**
	 * backgroundForDateMap holds background resource for each date
	 */
	protected HashMap<DateTime, Integer> backgroundForDateTimeMap = new HashMap<DateTime, Integer>();

	/**
	 * textColorForDateMap holds color for text for each date
	 */
	protected HashMap<DateTime, Integer> textColorForDateTimeMap = new HashMap<DateTime, Integer>();

    /**
	 * First column of calendar is Sunday
	 */
	protected int startDayOfWeek = SUNDAY;
	protected int manualCorrection = 0;

	/**
	 * A calendar height is not fixed, it may have 5 or 6 rows. Set fitAllMonths
	 * to true so that the calendar will always have 6 rows
	 */
	private boolean sixWeeksInCalendar = false;

	/**
	 * datePagerAdapters hold 4 adapters, meant to be reused
	 */
	protected ArrayList<CaldroidGridAdapter> datePagerAdapters = new ArrayList<CaldroidGridAdapter>();

	/**
	 * To control the navigation
	 */
	protected boolean enableSwipe = true;
	protected boolean showNavigationArrows = true;
	protected String showLang = "en";
	protected boolean enableClickOnDisabledDates = false;

	/**
	 * dateItemClickListener is fired when user click on the date cell
	 */
	private OnItemClickListener dateItemClickListener;

	/**
	 * dateItemLongClickListener is fired when user does a longclick on the date
	 * cell
	 */
	private OnItemLongClickListener dateItemLongClickListener;

	/**
	 * caldroidListener inform library client of the event happens inside
	 * Caldroid
	 */
	private CaldroidListener caldroidListener;

	public CaldroidListener getCaldroidListener() {
		return caldroidListener;
	}

	/**
	 * Meant to be subclassed. User who wants to provide custom view, need to
	 * provide custom adapter here
	 */
	public CaldroidGridAdapter getNewDatesGridAdapter(int month, int year) {
		return new CaldroidGridAdapter(MainActivity.mContext, month, year,
				getCaldroidData(), extraData);
	}

	/**
	 * Meant to be subclassed. User who wants to provide custom view, need to
	 * provide custom adapter here
	 */
	public WeekdayArrayAdapter getNewWeekdayAdapter(String lang) {
		return new WeekdayArrayAdapter(
				MainActivity.mContext, android.R.layout.simple_list_item_1,
				getDaysOfWeek(lang));
	}
	
	/**
	 * For client to customize the weekDayGridView
	 * 
	 * @return
	 */
	public GridView getWeekdayGridView() {
		return weekdayGridView;
	}

	/**
	 * For client to access array of rotating fragments
	 */
	public ArrayList<DateGridFragment> getFragments() {
		return fragments;
	}

	/**
	 * To let user customize the navigation buttons
	 */
	public ImageView getLeftArrowButton() {
		return leftArrowButton;
	}

	public ImageView getRightArrowButton() {
		return rightArrowButton;
	}

	public LinearLayout getLayCal() {
		return lay_cal;
	}

	/**
	 * To let client customize month title textview
	 */
	public TextView getMonthTitleTextView() {
		return monthTitleTextView;
	}

	public void setMonthTitleTextView(TextView monthTitleTextView) {
		this.monthTitleTextView = monthTitleTextView;
	}
	
	/**
	 * To let client customize hijri month title textview
	 */
	public TextView getHijriMonthTitleTextView() {
		return monthTitleTextView;
	}

	public void setHijriMonthTitleTextView(TextView monthTitleTextView) {
		this.hijrimonthTitleTextView = monthTitleTextView;
	}

	/**
	 * Get 4 adapters of the date grid views. Useful to set custom data and
	 * refresh date grid view
	 * 
	 * @return
	 */
	public ArrayList<CaldroidGridAdapter> getDatePagerAdapters() {
		return datePagerAdapters;
	}

	/**
	 * caldroidData return data belong to Caldroid
	 * 
	 * @return
	 */
	public HashMap<String, Object> getCaldroidData() {
		caldroidData.clear();
		caldroidData.put(DISABLE_DATES, disableDates);
		caldroidData.put(SELECTED_DATES, selectedDates);
		caldroidData.put(_MIN_DATE_TIME, minDateTime);
		caldroidData.put(_MAX_DATE_TIME, maxDateTime);
		caldroidData.put(START_DAY_OF_WEEK, Integer.valueOf(startDayOfWeek));
		caldroidData.put(MANUAL_CORRECTION, Integer.valueOf(manualCorrection));
		
		caldroidData.put(SIX_WEEKS_IN_CALENDAR,	Boolean.valueOf(sixWeeksInCalendar));
		caldroidData.put(SHOW_LANG, showLang);

		// For internal use
		caldroidData
				.put(_BACKGROUND_FOR_DATETIME_MAP, backgroundForDateTimeMap);
		caldroidData.put(_TEXT_COLOR_FOR_DATETIME_MAP, textColorForDateTimeMap);

		return caldroidData;
	}

	/**
	 * Extra data is data belong to Client
	 * 
	 * @return
	 */
	public HashMap<String, Object> getExtraData() {
		return extraData;
	}

	/**
	 * Client can set custom data in this HashMap
	 * 
	 * @param extraData
	 */
	public void setExtraData(HashMap<String, Object> extraData) {
		this.extraData = extraData;
	}

	/**
	 * Set backgroundForDateMap
	 */
	public void setBackgroundResourceForDates(
			HashMap<Date, Integer> backgroundForDateMap) {
		if (backgroundForDateMap == null || backgroundForDateMap.size() == 0) {
			return;
		}
		
		backgroundForDateTimeMap.clear();

		for (Date date : backgroundForDateMap.keySet()) {
			Integer resource = backgroundForDateMap.get(date);
			DateTime dateTime = CalendarHelper.convertDateToDateTime(date);
			backgroundForDateTimeMap.put(dateTime, resource);
		}
	}

	public void setBackgroundResourceForDateTimes(
			HashMap<DateTime, Integer> backgroundForDateTimeMap) {
		this.backgroundForDateTimeMap.putAll(backgroundForDateTimeMap);
	}

	public void setBackgroundResourceForDate(int backgroundRes, Date date) {
		DateTime dateTime = CalendarHelper.convertDateToDateTime(date);
		backgroundForDateTimeMap.put(dateTime, Integer.valueOf(backgroundRes));
	}

	public void setBackgroundResourceForDateTime(int backgroundRes,
			DateTime dateTime) {
		backgroundForDateTimeMap.put(dateTime, Integer.valueOf(backgroundRes));
	}

	/**
	 * Set textColorForDateMap
	 * 
	 * @return
	 */
	public void setTextColorForDates(HashMap<Date, Integer> textColorForDateMap) {
		if (textColorForDateMap == null || textColorForDateMap.size() == 0) {
			return;
		}

		textColorForDateTimeMap.clear();

		for (Date date : textColorForDateMap.keySet()) {
			Integer resource = textColorForDateMap.get(date);
			DateTime dateTime = CalendarHelper.convertDateToDateTime(date);
			textColorForDateTimeMap.put(dateTime, resource);
		}
	}

	public void setTextColorForDateTimes(
			HashMap<DateTime, Integer> textColorForDateTimeMap) {
		this.textColorForDateTimeMap.putAll(textColorForDateTimeMap);
	}

	public void setTextColorForDate(int textColorRes, Date date) {
		DateTime dateTime = CalendarHelper.convertDateToDateTime(date);
		textColorForDateTimeMap.put(dateTime, Integer.valueOf(textColorRes));
	}

	public void setTextColorForDateTime(int textColorRes, DateTime dateTime) {
		textColorForDateTimeMap.put(dateTime, Integer.valueOf(textColorRes));
	}

	/**
	 * Get current saved sates of the Caldroid. Useful for handling rotation
	 */
	public Bundle getSavedStates() {
		Bundle bundle = new Bundle();
		bundle.putInt(MONTH, month);
		bundle.putInt(YEAR, year);

		if (dialogTitle != null) {
			bundle.putString(DIALOG_TITLE, dialogTitle);
		}

		if (selectedDates != null && selectedDates.size() > 0) {
			bundle.putStringArrayList(SELECTED_DATES,
					CalendarHelper.convertToStringList(selectedDates));
		}

		if (disableDates != null && disableDates.size() > 0) {
			bundle.putStringArrayList(DISABLE_DATES,
					CalendarHelper.convertToStringList(disableDates));
		}

		if (minDateTime != null) {
			bundle.putString(MIN_DATE, minDateTime.format("YYYY-MM-DD"));
		}

		if (maxDateTime != null) {
			bundle.putString(MAX_DATE, maxDateTime.format("YYYY-MM-DD"));
		}

		bundle.putBoolean(SHOW_NAVIGATION_ARROWS, showNavigationArrows);
		bundle.putBoolean(ENABLE_SWIPE, enableSwipe);
		bundle.putInt(START_DAY_OF_WEEK, startDayOfWeek);
		bundle.putInt(MANUAL_CORRECTION, manualCorrection);
		
		bundle.putBoolean(SIX_WEEKS_IN_CALENDAR, sixWeeksInCalendar);
		bundle.putString(SHOW_LANG, showLang);

		return bundle;
	}

	/**
	 * Save current state to bundle outState
	 * 
	 * @param outState
	 * @param key
	 */
	public void saveStatesToKey(Bundle outState, String key) {
		outState.putBundle(key, getSavedStates());
	}

	/**
	 * Restore current states from savedInstanceState
	 * 
	 * @param savedInstanceState
	 * @param key
	 */
	public void restoreStatesFromKey(Bundle savedInstanceState, String key) {
		if (savedInstanceState != null && savedInstanceState.containsKey(key)) {
			Bundle caldroidSavedState = savedInstanceState.getBundle(key);
			setArguments(caldroidSavedState);
		}
	}

	/**
	 * Restore state for dialog
	 * 
	 * @param savedInstanceState
	 * @param key
	 * @param dialogTag
	 */
	public void restoreDialogStatesFromKey(FragmentManager manager,
			Bundle savedInstanceState, String key, String dialogTag) {
		restoreStatesFromKey(savedInstanceState, key);

		CaldroidFragment existingDialog = (CaldroidFragment) manager
				.findFragmentByTag(dialogTag);
		if (existingDialog != null) {
			existingDialog.dismiss();
			show(manager, dialogTag);
		}
	}

	/**
	 * Get current virtual position of the month being viewed
	 */
	public int getCurrentVirtualPosition() {
		int currentPage = dateViewPager.getCurrentItem();
		return pageChangeListener.getCurrent(currentPage);
	}

	/**
	 * Move calendar to the specified date
	 * 
	 * @param date
	 */
	public void moveToDate(Date date) {
		moveToDateTime(CalendarHelper.convertDateToDateTime(date));
	}

	/**
	 * Move calendar to specified dateTime, with animation
	 * 
	 * @param dateTime
	 */
	public void moveToDateTime(DateTime dateTime) {

		DateTime firstOfMonth = new DateTime(year, month, 1, 0, 0, 0, 0);
		DateTime lastOfMonth = firstOfMonth.getEndOfMonth();

		// To create a swipe effect
		// Do nothing if the dateTime is in current month

		// Calendar swipe left when dateTime is in the past
		if (dateTime.lt(firstOfMonth)) {
			// Get next month of dateTime. When swipe left, month will
			// decrease
			DateTime firstDayNextMonth = dateTime.plus(0, 1, 0, 0, 0, 0, 0,
					DateTime.DayOverflow.LastDay);

			// Refresh adapters
			pageChangeListener.setCurrentDateTime(firstDayNextMonth);
			int currentItem = dateViewPager.getCurrentItem();
			pageChangeListener.refreshAdapters(currentItem);

			// Swipe left
			dateViewPager.setCurrentItem(currentItem - 1);
		}

		// Calendar swipe right when dateTime is in the future
		else if (dateTime.gt(lastOfMonth)) {
			// Get last month of dateTime. When swipe right, the month will
			// increase
			DateTime firstDayLastMonth = dateTime.minus(0, 1, 0, 0, 0, 0, 0,
					DateTime.DayOverflow.LastDay);

			// Refresh adapters
			pageChangeListener.setCurrentDateTime(firstDayLastMonth);
			int currentItem = dateViewPager.getCurrentItem();
			pageChangeListener.refreshAdapters(currentItem);

			// Swipe right
			dateViewPager.setCurrentItem(currentItem + 1);
		}

	}

	/**
	 * Set month and year for the calendar. This is to avoid naive
	 * implementation of manipulating month and year. All dates within same
	 * month/year give same result
	 * 
	 * @param date
	 */
	public void setCalendarDate(Date date) {
		setCalendarDateTime(CalendarHelper.convertDateToDateTime(date));
	}

	public void setCalendarDateTime(DateTime dateTime) {
		month = dateTime.getMonth();
		year = dateTime.getYear();

		// Notify listener
		if (caldroidListener != null) {
			caldroidListener.onChangeMonth(month, year);
		}

		refreshView();
	}

	/**
	 * Set calendar to previous month
	 */
	public void prevMonth() {
		dateViewPager.setCurrentItem(pageChangeListener.getCurrentPage() - 1);
	}

	/**
	 * Set calendar to next month
	 */
	public void nextMonth() {
		dateViewPager.setCurrentItem(pageChangeListener.getCurrentPage() + 1);
	}

	/**
	 * Clear all disable dates. Notice this does not refresh the calendar, need
	 * to explicitly call refreshView()
	 */
	public void clearDisableDates() {
		disableDates.clear();
	}

	/**
	 * Set disableDates from ArrayList of Date
	 * 
	 * @param disableDateList
	 */
	public void setDisableDates(ArrayList<Date> disableDateList) {
		if (disableDateList == null || disableDateList.size() == 0) {
			return;
		}
		
		disableDates.clear();

		for (Date date : disableDateList) {
			DateTime dateTime = CalendarHelper.convertDateToDateTime(date);
			disableDates.add(dateTime);
		}

	}

	/**
	 * Set disableDates from ArrayList of String. By default, the date formatter
	 * is yyyy-MM-dd. For e.g 2013-12-24
	 * 
	 * @param disableDateStrings
	 */
	public void setDisableDatesFromString(ArrayList<String> disableDateStrings) {
		setDisableDatesFromString(disableDateStrings, null);
	}

	/**
	 * Set disableDates from ArrayList of String with custom date format. For
	 * example, if the date string is 06-Jan-2013, use date format dd-MMM-yyyy.
	 * This method will refresh the calendar, it's not necessary to call
	 * refreshView()
	 * 
	 * @param disableDateStrings
	 * @param dateFormat
	 */
	public void setDisableDatesFromString(ArrayList<String> disableDateStrings,
			String dateFormat) {
		if (disableDateStrings == null) {
			return;
		}
		
		disableDates.clear();

		for (String dateString : disableDateStrings) {
			DateTime dateTime = CalendarHelper.getDateTimeFromString(
					dateString, dateFormat);
			disableDates.add(dateTime);
		}
	}

	/**
	 * To clear selectedDates. This method does not refresh view, need to
	 * explicitly call refreshView()
	 */
	public void clearSelectedDates() {
		selectedDates.clear();
	}

	/**
	 * Select the dates from fromDate to toDate. By default the background color
	 * is holo_blue_light, and the text color is black. You can customize the
	 * background by changing CaldroidFragment.selectedBackgroundDrawable, and
	 * change the text color CaldroidFragment.selectedTextColor before call this
	 * method. This method does not refresh view, need to call refreshView()
	 * 
	 * @param fromDate
	 * @param toDate
	 */
	public void setSelectedDates(Date fromDate, Date toDate) {
		// Ensure fromDate is before toDate
		if (fromDate == null || toDate == null || fromDate.after(toDate)) {
			return;
		}

		selectedDates.clear();

		DateTime fromDateTime = CalendarHelper.convertDateToDateTime(fromDate);
		DateTime toDateTime = CalendarHelper.convertDateToDateTime(toDate);

		DateTime dateTime = fromDateTime;
		while (dateTime.lt(toDateTime)) {
			selectedDates.add(dateTime);
			dateTime = dateTime.plusDays(1);
		}
		selectedDates.add(toDateTime);
	}

	/**
	 * Convenient method to select dates from String
	 * 
	 * @param fromDateString
	 * @param toDateString
	 * @param dateFormat
	 * @throws ParseException
	 */
	public void setSelectedDateStrings(String fromDateString,
			String toDateString, String dateFormat) throws ParseException {

		Date fromDate = CalendarHelper.getDateFromString(fromDateString,
				dateFormat);
		Date toDate = CalendarHelper
				.getDateFromString(toDateString, dateFormat);
		setSelectedDates(fromDate, toDate);
	}

	/**
	 * Check if the navigation arrow is shown
	 * 
	 * @return
	 */
	public boolean isShowNavigationArrows() {
		return showNavigationArrows;
	}

	/**
	 * Show or hide the navigation arrows
	 * 
	 * @param showNavigationArrows
	 */
	public void setShowNavigationArrows(boolean showNavigationArrows) {
		this.showNavigationArrows = showNavigationArrows;
		if (showNavigationArrows) {
			leftArrowButton.setVisibility(View.VISIBLE);
			rightArrowButton.setVisibility(View.VISIBLE);
		} else {
			leftArrowButton.setVisibility(View.INVISIBLE);
			rightArrowButton.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * Enable / Disable swipe to navigate different months
	 * 
	 * @return
	 */
	public boolean isEnableSwipe() {
		return enableSwipe;
	}

	public void setEnableSwipe(boolean enableSwipe) {
		this.enableSwipe = enableSwipe;
		dateViewPager.setEnabled(enableSwipe);
	}

	/**
	 * Set min date. This method does not refresh view
	 * 
	 * @param minDate
	 */
	public void setMinDate(Date minDate) {
		if (minDate == null) {
			minDateTime = null;
		} else {
			minDateTime = CalendarHelper.convertDateToDateTime(minDate);
		}
	}

	public boolean isSixWeeksInCalendar() {
		return sixWeeksInCalendar;
	}

	public void setSixWeeksInCalendar(boolean sixWeeksInCalendar) {
		this.sixWeeksInCalendar = sixWeeksInCalendar;
		dateViewPager.setSixWeeksInCalendar(sixWeeksInCalendar);
	}

	/**
	 * Convenient method to set min date from String. If dateFormat is null,
	 * default format is yyyy-MM-dd
	 * 
	 * @param minDateString
	 * @param dateFormat
	 */
	public void setMinDateFromString(String minDateString, String dateFormat) {
		if (minDateString == null) {
			setMinDate(null);
		} else {
			minDateTime = CalendarHelper.getDateTimeFromString(minDateString,
					dateFormat);
		}
	}

	/**
	 * Set max date. This method does not refresh view
	 * 
	 * @param maxDate
	 */
	public void setMaxDate(Date maxDate) {
		if (maxDate == null) {
			maxDateTime = null;
		} else {
			maxDateTime = CalendarHelper.convertDateToDateTime(maxDate);
		}
	}

	/**
	 * Convenient method to set max date from String. If dateFormat is null,
	 * default format is yyyy-MM-dd
	 * 
	 * @param maxDateString
	 * @param dateFormat
	 */
	public void setMaxDateFromString(String maxDateString, String dateFormat) {
		if (maxDateString == null) {
			setMaxDate(null);
		} else {
			maxDateTime = CalendarHelper.getDateTimeFromString(maxDateString,
					dateFormat);
		}
	}

	/**
	 * Set caldroid listener when user click on a date
	 * 
	 * @param caldroidListener
	 */
	public void setCaldroidListener(CaldroidListener caldroidListener) {
		this.caldroidListener = caldroidListener;
	}

	/**
	 * Callback to listener when date is valid (not disable, not outside of
	 * min/max date)
	 * 
	 * @return
	 */
	private OnItemClickListener getDateItemClickListener() {
		if (dateItemClickListener == null) {
			dateItemClickListener = new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {

					DateTime dateTime = dateInMonthsList.get(position);

					if (caldroidListener != null) {
						if (!enableClickOnDisabledDates) {
							if ((minDateTime != null && dateTime
									.lt(minDateTime))
									|| (maxDateTime != null && dateTime
											.gt(maxDateTime))
									|| (disableDates != null && disableDates
											.indexOf(dateTime) != -1)) {
								return;
							}
						}

						Date date = CalendarHelper
								.convertDateTimeToDate(dateTime);
						caldroidListener.onSelectDate(date, view);
					}
				}
			};
		}

		return dateItemClickListener;
	}

	/**
	 * Callback to listener when date is valid (not disable, not outside of
	 * min/max date)
	 * 
	 * @return
	 */
	private OnItemLongClickListener getDateItemLongClickListener() {
		if (dateItemLongClickListener == null) {
			dateItemLongClickListener = new OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent,
						View view, int position, long id) {

					DateTime dateTime = dateInMonthsList.get(position);

					if (caldroidListener != null) {
						if (!enableClickOnDisabledDates) {
							if ((minDateTime != null && dateTime
									.lt(minDateTime))
									|| (maxDateTime != null && dateTime
											.gt(maxDateTime))
									|| (disableDates != null && disableDates
											.indexOf(dateTime) != -1)) {
								return false;
							}
						}
						Date date = CalendarHelper
								.convertDateTimeToDate(dateTime);
						caldroidListener.onLongClickDate(date, view);
					}

					return true;
				}
			};
		}

		return dateItemLongClickListener;
	}

	/**
	 * Refresh month title text view when user swipe
	 */
	protected void refreshMonthTitleTextView() {
		// Refresh title view
		firstMonthTime.year = year;
		firstMonthTime.month = month - 1;
		firstMonthTime.monthDay = 1;
		long millis = firstMonthTime.toMillis(true);

		// This is the method used by the platform Calendar app to get a
		// correctly localized month name for display on a wall calendar
		monthYearStringBuilder.setLength(0);
		String monthTitle = DateUtils.formatDateRange(MainActivity.mContext,
				monthYearFormatter, millis, millis, MONTH_YEAR_FLAG).toString();
		
		Calendar Qurancal = Calendar.getInstance();
		Qurancal.setTimeInMillis(millis);
		
		String hijrimonthtitle = "";
		if (HijriCalendarDate.getSimpleDateMonthx(Qurancal, manualCorrection, showLang).contains("BGS")) {
			int thnx = Integer.valueOf(HijriCalendarDate.getSimpleDateYear(Qurancal, 0)) + 1;
			hijrimonthtitle = HijriCalendarDate.getSimpleDateMonthx(Qurancal, manualCorrection, showLang).replace("BGS", "") + " " + thnx + "H";
		}else {
			hijrimonthtitle = HijriCalendarDate.getSimpleDateMonthx(Qurancal, manualCorrection, showLang) + " " + HijriCalendarDate.getSimpleDateYear(Qurancal, 0) + "H";
		}

		monthTitleTextView.setText(monthTitle);
		hijrimonthTitleTextView.setText(hijrimonthtitle);
	}

	/**
	 * Refresh view when parameter changes. You should always change all
	 * parameters first, then call this method.
	 */
	public void refreshView() {
		// If month and year is not yet initialized, refreshView doesn't do
		// anything
		if (month == -1 || year == -1) {
			return;
		}

		refreshMonthTitleTextView();

		// Refresh the date grid views
		for (CaldroidGridAdapter adapter : datePagerAdapters) {
			// Reset caldroid data
			adapter.setCaldroidData(getCaldroidData());

			// Reset extra data
			adapter.setExtraData(extraData);
			
			// Update today variable
			adapter.updateToday();

			// Refresh view
			adapter.notifyDataSetChanged();
		}
	}

	/**
	 * Retrieve initial arguments to the fragment Data can include: month, year,
	 * dialogTitle, showNavigationArrows,(String) disableDates, selectedDates,
	 * minDate, maxDate
	 */
	protected void retrieveInitialArgs() {
		// Get arguments
		Bundle args = getArguments();
		if (args != null) {
			// Get month, year
			month = args.getInt(MONTH, -1);
			year = args.getInt(YEAR, -1);
			dialogTitle = args.getString(DIALOG_TITLE);
			Dialog dialog = getDialog();
			if (dialog != null) {
				if (dialogTitle != null) {
					dialog.setTitle(dialogTitle);
				} else {
					// Don't display title bar if user did not supply
					// dialogTitle
					dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				}
			}

			// Get start day of Week. Default calendar first column is SUNDAY
			startDayOfWeek = args.getInt(START_DAY_OF_WEEK, 1);
			if (startDayOfWeek > 7) {
				startDayOfWeek = startDayOfWeek % 7;
			}
			manualCorrection = args.getInt(MANUAL_CORRECTION, 0);
			

			// Should show arrow
			showNavigationArrows = args
					.getBoolean(SHOW_NAVIGATION_ARROWS, true);

			showLang = args
					.getString(SHOW_LANG, "en");

			// Should enable swipe to change month
			enableSwipe = args.getBoolean(ENABLE_SWIPE, true);

			// Get sixWeeksInCalendar
			sixWeeksInCalendar = args.getBoolean(SIX_WEEKS_IN_CALENDAR, false);

			// Get clickable setting
			enableClickOnDisabledDates = args.getBoolean(
					ENABLE_CLICK_ON_DISABLED_DATES, false);

			// Get disable dates
			ArrayList<String> disableDateStrings = args
					.getStringArrayList(DISABLE_DATES);
			if (disableDateStrings != null && disableDateStrings.size() > 0) {
				disableDates.clear();
				for (String dateString : disableDateStrings) {
					DateTime dt = CalendarHelper.getDateTimeFromString(
							dateString, "yyyy-MM-dd");
					disableDates.add(dt);
				}
			}

			// Get selected dates
			ArrayList<String> selectedDateStrings = args
					.getStringArrayList(SELECTED_DATES);
			if (selectedDateStrings != null && selectedDateStrings.size() > 0) {
				selectedDates.clear();
				for (String dateString : selectedDateStrings) {
					DateTime dt = CalendarHelper.getDateTimeFromString(
							dateString, "yyyy-MM-dd");
					selectedDates.add(dt);
				}
			}

			// Get min date and max date
			String minDateTimeString = args.getString(MIN_DATE);
			if (minDateTimeString != null) {
				minDateTime = CalendarHelper.getDateTimeFromString(
						minDateTimeString, null);
			}

			String maxDateTimeString = args.getString(MAX_DATE);
			if (maxDateTimeString != null) {
				maxDateTime = CalendarHelper.getDateTimeFromString(
						maxDateTimeString, null);
			}

		}
		if (month == -1 || year == -1) {
			DateTime dateTime = DateTime.today(TimeZone.getDefault());
			month = dateTime.getMonth();
			year = dateTime.getYear();
		}
	}

	/**
	 * To support faster init
	 * 
	 * @param dialogTitle
	 * @param month
	 * @param year
	 * @return
	 */
	public static CaldroidFragment newInstance(String dialogTitle, int month,
			int year) {
		CaldroidFragment f = new CaldroidFragment();

		// Supply num input as an argument.
		Bundle args = new Bundle();
		args.putString(DIALOG_TITLE, dialogTitle);
		args.putInt(MONTH, month);
		args.putInt(YEAR, year);

		f.setArguments(args);

		return f;
	}

	/**
	 * Below code fixed the issue viewpager disappears in dialog mode on
	 * orientation change
	 * 
	 * Code taken from Andy Dennie and Zsombor Erdody-Nagy
	 * http://stackoverflow.com/questions/8235080/fragments-dialogfragment
	 * -and-screen-rotation
	 */
	@Override
	public void onDestroyView() {
		if (getDialog() != null && getRetainInstance()) {
			getDialog().setDismissMessage(null);
		}
		super.onDestroyView();
	}

	/**
	 * Setup view
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		retrieveInitialArgs();

		// To support keeping instance for dialog
		if (getDialog() != null) {
			setRetainInstance(true);
		}

		// Inflate layout
		View view = inflater.inflate(R.layout.calendar_view, container, false);
		

		// For the hijrimonthTitleTextView
		hijrimonthTitleTextView = view
				.findViewById(R.id.hijri_month_year_textview);

		// For the monthTitleTextView
		monthTitleTextView = view
				.findViewById(R.id.calendar_month_year_textview);
		
		// For the left arrow button
		leftArrowButton = view.findViewById(R.id.calendar_left_arrow);
		rightArrowButton = view.findViewById(R.id.calendar_right_arrow);
			leftArrowButton.setColorFilter(MainActivity.colormatrix_green());
			rightArrowButton.setColorFilter(MainActivity.colormatrix_green());
			hijrimonthTitleTextView.setTextColor(ContextCompat.getColor(MainActivity.mContext, R.color.green1));
			monthTitleTextView.setTextColor(ContextCompat.getColor(MainActivity.mContext, R.color.green1));
		lay_cal = view.findViewById(R.id.lay_cal);
		
		// Navigate to previous month when user click
		leftArrowButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				prevMonth();prevMonth();prevMonth();
				nextMonth();nextMonth();
			}
		});

		// Navigate to next month when user click
		rightArrowButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				nextMonth();nextMonth();nextMonth();
				prevMonth();prevMonth();
			}
		});

		lay_cal.setOnClickListener(new OnClickListener() {
			boolean isok = true;
			private String[] bulanhijr;
			private NumberPicker thnhijri;
			private NumberPicker blnhijri;
			private NumberPicker tglhijri;
			private ToggleButton swcalendar;
			private DatePicker datePickerMasehi;
			private LinearLayout datePickerHijriyyah;
			private void togglehijrimasehi() {
				isok = true;
				carimaxharihijri();
				if (swcalendar.isChecked()) {
					datePickerMasehi.setVisibility(View.GONE);
					datePickerHijriyyah.setVisibility(View.VISIBLE);

					try {
						GregorianCalendar gCal = new GregorianCalendar(datePickerMasehi.getYear(), datePickerMasehi.getMonth(), datePickerMasehi.getDayOfMonth());
						Calendar uCal = new UmmalquraCalendar();
						uCal.setTime(gCal.getTime());
						uCal.add(Calendar.DAY_OF_MONTH, MainActivity.manual_hijriyyah - 2);

						tglhijri.setValue(uCal.get(Calendar.DAY_OF_MONTH));
						blnhijri.setValue(uCal.get(Calendar.MONTH));       
						thnhijri.setValue(uCal.get(Calendar.YEAR));
					} catch (Exception e) {
						MainActivity.toast_info(MainActivity.mContext, getResources().getString(R.string.str_mindate));
						isok = false;
					}
				}else {
					datePickerMasehi.setVisibility(View.VISIBLE);
					datePickerHijriyyah.setVisibility(View.GONE);

					try {
						Calendar uCal = new UmmalquraCalendar(thnhijri.getValue(), blnhijri.getValue(), tglhijri.getValue());
						GregorianCalendar gCal = new GregorianCalendar();
						gCal.setTime(uCal.getTime());
						gCal.add(Calendar.DAY_OF_MONTH, -1 * (MainActivity.manual_hijriyyah - 2));

						datePickerMasehi.updateDate(gCal.get(Calendar.YEAR), gCal.get(Calendar.MONTH), gCal.get(Calendar.DAY_OF_MONTH));
					} catch (Exception e) {
						MainActivity.toast_info(MainActivity.mContext, getResources().getString(R.string.str_mindate));
						isok = false;
					}
				}
			}
			private void carimaxharihijri() {
				try {
					int xval = tglhijri.getValue();
					int maxval = UmmalquraCalendar.lengthOfMonth(thnhijri.getValue(), blnhijri.getValue());
					tglhijri.setMaxValue(maxval);
					tglhijri.setValue(xval);
				} catch (Exception e) {
					isok = false;
				}
			}
			
			@Override
			public void onClick(View v) {
				GregorianCalendar gcal = new GregorianCalendar();
				gcal.add(GregorianCalendar.DAY_OF_MONTH, MainActivity.added_day);
				Calendar uCal = new UmmalquraCalendar();
				uCal.setTime(gcal.getTime());
				uCal.add(Calendar.DAY_OF_MONTH, MainActivity.manual_hijriyyah - 2);
				
				int thnarab = uCal.get(Calendar.YEAR);         // 1433
				int blnarab = uCal.get(Calendar.MONTH);         
				int tglarab = uCal.get(Calendar.DAY_OF_MONTH); // 20d
				final Dialog dialogpesan = new Dialog(v.getContext());
				dialogpesan.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialogpesan.setContentView(R.layout.layout_dialog_datepicker);
				dialogpesan.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				dialogpesan.getWindow().getAttributes().windowAnimations = R.style.MenuLeft;
				dialogpesan.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
				dialogpesan.setCancelable(true);
				dialogpesan.setCanceledOnTouchOutside(true);

				//				int hour = Integer.valueOf(edWaktuPagi.getText().toString().split(":")[0].trim());
				//				int minute = Integer.valueOf(edWaktuPagi.getText().toString().split(":")[1].trim());

				datePickerHijriyyah = dialogpesan.findViewById(R.id.datePickerHijriyyah);
				tglhijri = dialogpesan.findViewById(R.id.tglhijri);
				tglhijri.setMinValue(1);
				tglhijri.setValue(tglarab);
				tglhijri.setFormatter(new NumberPicker.Formatter() {
					@SuppressLint("DefaultLocale")
					@Override
					public String format(int value) {
						return String.format("%02d", value);
					}
				});
				tglhijri.setWrapSelectorWheel(true);

					bulanhijr = new String[]{
						"Muharram",
						"Shafar",
						"Rabi'ul Awwal",
						"Rabi'ul Tsani",
						"Jumaadal Ula",
						"Jumaadal Tsaniyah",
						"Rajab",
						"Sya'ban",
						"Ramadhan",
						"Syawwal",
						"Dzulqa'dah",
				"Dzulhijjah"};
				blnhijri = dialogpesan.findViewById(R.id.blnhijri);
				blnhijri.setMinValue(0); //from array first value
				blnhijri.setMaxValue(bulanhijr.length-1); //to array last value
				blnhijri.setDisplayedValues(bulanhijr);
				blnhijri.setValue(blnarab);
				blnhijri.setWrapSelectorWheel(true);
				blnhijri.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
					@Override
					public void onValueChange(NumberPicker picker, int oldVal, int newVal){
						//Display the newly selected number from picker
						//			                tv.setText("Selected Number : " + newVal);
						//						MainActivity.toast_info(MainActivity.this, thnhijri.getValue()+"-");//+UmmalquraCalendar.lengthOfMonth(thnhijri.getValue(), newVal + 1));
						carimaxharihijri();
					}
				});

				thnhijri = dialogpesan.findViewById(R.id.thnhijri);
				thnhijri.setMinValue(1300);
				thnhijri.setMaxValue(1500);
				thnhijri.setValue(thnarab);
				thnhijri.setWrapSelectorWheel(true);
				thnhijri.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
					@Override
					public void onValueChange(NumberPicker picker, int oldVal, int newVal){
						//Display the newly selected number from picker
						//						MainActivity.toast_info(MainActivity.this, blnhijri.getValue()+"-");//+UmmalquraCalendar.lengthOfMonth(newVal, blnhijri.getValue()));
						carimaxharihijri();
					}
				});
				carimaxharihijri();
				
				datePickerMasehi = dialogpesan.findViewById(R.id.datePickerMasehi);
				swcalendar = dialogpesan.findViewById(R.id.swcalendar);
				swcalendar.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						togglehijrimasehi();
					}

				});
				swcalendar.setChecked(true);
				togglehijrimasehi();
				
				TextView thijri = dialogpesan.findViewById(R.id.thijri);
				thijri.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						swcalendar.setChecked(!swcalendar.isChecked());
						togglehijrimasehi();
					}

				});
				
				TextView tmasehi = dialogpesan.findViewById(R.id.tmasehi);
				tmasehi.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						swcalendar.setChecked(!swcalendar.isChecked());
						togglehijrimasehi();
					}
				});				
				Button btn2 = dialogpesan.findViewById(R.id.btn2);
				btn2.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						dialogpesan.dismiss();

					}
				});

				Button btn1 = dialogpesan.findViewById(R.id.btn1);
				btn1.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						clearDisableDates();
						clearSelectedDates();
						refreshView();
						
						if (swcalendar.isChecked()) {
							swcalendar.setChecked(false);
							togglehijrimasehi();
						}

						GregorianCalendar gcal = new GregorianCalendar();
						gcal.set(GregorianCalendar.DAY_OF_MONTH, datePickerMasehi.getDayOfMonth());
						gcal.set(GregorianCalendar.MONTH, datePickerMasehi.getMonth()); 
						gcal.set(GregorianCalendar.YEAR, datePickerMasehi.getYear());
						GregorianCalendar gcal2 = new GregorianCalendar();
						gcal2.set(GregorianCalendar.DAY_OF_MONTH, 31);
						gcal2.set(GregorianCalendar.MONTH, GregorianCalendar.DECEMBER);
						gcal2.set(GregorianCalendar.YEAR, 1937);

						//							Calendar cal = Calendar.getInstance();
						//							cal.set(Calendar.YEAR, datePickerMasehi.getYear());
						//							cal.set(Calendar.MONTH, datePickerMasehi.getMonth() - 2);
						//							cal.set(Calendar.DAY_OF_MONTH, datePickerMasehi.getDayOfMonth());

						if (gcal.after(gcal2)) {
							gcal.add(GregorianCalendar.MONTH, -2);
							moveToDate(new Date(gcal.getTimeInMillis()));
							nextMonth();
							nextMonth();
							dialogpesan.dismiss();
						}else {
							dialogpesan.dismiss();
							MainActivity.toast_info(MainActivity.mContext, getResources().getString(R.string.str_mindate));
						}
						
						//						MainActivity.toast_info(MainActivity.this, datePickerMasehi.getDayOfMonth() + "-" + (datePickerMasehi.getMonth() + 1) + "-" + datePickerMasehi.getYear());
					}
				});

				dialogpesan.show();
			}
		});
		
		// Show navigation arrows depend on initial arguments
		setShowNavigationArrows(showNavigationArrows);

		// For the weekday gridview ("SUN, MON, TUE, WED, THU, FRI, SAT")
		weekdayGridView = view.findViewById(R.id.weekday_gridview);
		WeekdayArrayAdapter weekdaysAdapter = getNewWeekdayAdapter(showLang);
		weekdayGridView.setAdapter(weekdaysAdapter);

		// Setup all the pages of date grid views. These pages are recycled
		setupDateGridPages(view);

		// Refresh view
		refreshView();

		// Inform client that all views are created and not null
		// Client should perform customization for buttons and textviews here
		if (caldroidListener != null) {
			caldroidListener.onCaldroidViewCreated();
		}

		return view;
	}

	/**
	 * Setup 4 pages contain date grid views. These pages are recycled to use
	 * memory efficient
	 * 
	 * @param view
	 */
	private void setupDateGridPages(View view) {
		// Get current date time
		DateTime currentDateTime = new DateTime(year, month, 1, 0, 0, 0, 0);

		// Set to pageChangeListener
		pageChangeListener = new DatePageChangeListener();
		pageChangeListener.setCurrentDateTime(currentDateTime);

		// Setup adapters for the grid views
		// Current month
		CaldroidGridAdapter adapter0 = getNewDatesGridAdapter(
				currentDateTime.getMonth(), currentDateTime.getYear());

		// Setup dateInMonthsList
		dateInMonthsList = adapter0.getDatetimeList();

		// Next month
		DateTime nextDateTime = currentDateTime.plus(0, 1, 0, 0, 0, 0, 0,
				DateTime.DayOverflow.LastDay);
		CaldroidGridAdapter adapter1 = getNewDatesGridAdapter(
				nextDateTime.getMonth(), nextDateTime.getYear());

		// Next 2 month
		DateTime next2DateTime = nextDateTime.plus(0, 1, 0, 0, 0, 0, 0,
				DateTime.DayOverflow.LastDay);
		CaldroidGridAdapter adapter2 = getNewDatesGridAdapter(
				next2DateTime.getMonth(), next2DateTime.getYear());

		// Previous month
		DateTime prevDateTime = currentDateTime.minus(0, 1, 0, 0, 0, 0, 0,
				DateTime.DayOverflow.LastDay);
		CaldroidGridAdapter adapter3 = getNewDatesGridAdapter(
				prevDateTime.getMonth(), prevDateTime.getYear());

		// Add to the array of adapters
		datePagerAdapters.add(adapter0);
		datePagerAdapters.add(adapter1);
		datePagerAdapters.add(adapter2);
		datePagerAdapters.add(adapter3);

		// Set adapters to the pageChangeListener so it can refresh the adapter
		// when page change
		pageChangeListener.setCaldroidGridAdapters(datePagerAdapters);

		// Setup InfiniteViewPager and InfinitePagerAdapter. The
		// InfinitePagerAdapter is responsible
		// for reuse the fragments
		dateViewPager = view
				.findViewById(R.id.months_infinite_pager);

		// Set enable swipe
		dateViewPager.setEnabled(enableSwipe);

		// Set if viewpager wrap around particular month or all months (6 rows)
		dateViewPager.setSixWeeksInCalendar(sixWeeksInCalendar);

		// Set the numberOfDaysInMonth to dateViewPager so it can calculate the
		// height correctly
		dateViewPager.setDatesInMonth(dateInMonthsList);

		// MonthPagerAdapter actually provides 4 real fragments. The
		// InfinitePagerAdapter only recycles fragment provided by this
		// MonthPagerAdapter
		final MonthPagerAdapter pagerAdapter = new MonthPagerAdapter(
				getChildFragmentManager());

		// Provide initial data to the fragments, before they are attached to
		// view.
		fragments = pagerAdapter.getFragments();
		for (int i = 0; i < NUMBER_OF_PAGES; i++) {
			DateGridFragment dateGridFragment = fragments.get(i);
			CaldroidGridAdapter adapter = datePagerAdapters.get(i);
			dateGridFragment.setGridAdapter(adapter);
			dateGridFragment.setOnItemClickListener(getDateItemClickListener());
			dateGridFragment
					.setOnItemLongClickListener(getDateItemLongClickListener());
		}

		// Setup InfinitePagerAdapter to wrap around MonthPagerAdapter
		InfinitePagerAdapter infinitePagerAdapter = new InfinitePagerAdapter(
				pagerAdapter);

		// Use the infinitePagerAdapter to provide data for dateViewPager
		dateViewPager.setAdapter(infinitePagerAdapter);

		// Setup pageChangeListener
		dateViewPager.setOnPageChangeListener(pageChangeListener);
	}

	/**
	 * To display the week day title
	 * 
	 * @return "SUN, MON, TUE, WED, THU, FRI, SAT"
	 * @return "Ahad, Ithnin, Thulatha, Arbaa, Khams, Jumuah, Sabt"
	 */
	protected ArrayList<String> getDaysOfWeek(String lang) {
		ArrayList<String> list = new ArrayList<String>();

		/*		SimpleDateFormat fmt = new SimpleDateFormat("EEE", Locale.getDefault());

		// 17 Feb 2013 is Sunday
		DateTime sunday = new DateTime(2013, 2, 17, 0, 0, 0, 0);
		DateTime nextDay = sunday.plusDays(startDayOfWeek - SUNDAY);

		for (int i = 0; i < 7; i++) {
			Date date = CalendarHelper.convertDateTimeToDate(nextDay);
			list.add(fmt.format(date).toUpperCase());
			nextDay = nextDay.plusDays(1);
		} */
	    
	    if (lang.equals("en")) {
			if (startDayOfWeek == SUNDAY){
				list.add("Al-Ahad");
				list.add("Al-lthnayn");
				list.add("Ats-Thalatha'");
				list.add("Al-Arbi'a'");
				list.add("Al-Khamees");
				list.add("Al-Jum'ah");
				list.add("As-Sabt");
			} else {
				list.add("Al-lthnayn");
				list.add("Ats-Thalatha'");
				list.add("Al-Arbi'a'");
				list.add("Al-Khamees");
				list.add("Al-Jum'ah");
				list.add("As-Sabt");
				list.add("Al-Ahad");
			}
	    }else {
			if (startDayOfWeek == SUNDAY){
				list.add("Al-Ahad");
				list.add("Al-Itsnain");
				list.add("Ats-Tsulaatsaa'");
				list.add("Al-Arbi'a'");
				list.add("Al-Khamiis");
				list.add("Al-Jumu'ah");
				list.add("As-Sabt");
			} else {
				list.add("Al-Itsnain");
				list.add("Ats-Tsulaatsaa'");
				list.add("Al-Arbi'a'");
				list.add("Al-Khamiis");
				list.add("Al-Jumu'ah");
				list.add("As-Sabt");
				list.add("Al-Ahad");
			}
	    }

		return list;
	}

	/**
	 * DatePageChangeListener refresh the date grid views when user swipe the
	 * calendar
	 * 
	 * @author thomasdao
	 * 
	 */
	public class DatePageChangeListener implements OnPageChangeListener {
		private int currentPage = InfiniteViewPager.OFFSET;
		private DateTime currentDateTime;
		private ArrayList<CaldroidGridAdapter> caldroidGridAdapters;

		/**
		 * Return currentPage of the dateViewPager
		 * 
		 * @return
		 */
		public int getCurrentPage() {
			return currentPage;
		}

		public void setCurrentPage(int currentPage) {
			this.currentPage = currentPage;
		}

		/**
		 * Return currentDateTime of the selected page
		 * 
		 * @return
		 */
		public DateTime getCurrentDateTime() {
			return currentDateTime;
		}

		public void setCurrentDateTime(DateTime dateTime) {
			this.currentDateTime = dateTime;
			setCalendarDateTime(currentDateTime);
		}

		/**
		 * Return 4 adapters
		 * 
		 * @return
		 */
		public ArrayList<CaldroidGridAdapter> getCaldroidGridAdapters() {
			return caldroidGridAdapters;
		}

		public void setCaldroidGridAdapters(
				ArrayList<CaldroidGridAdapter> caldroidGridAdapters) {
			this.caldroidGridAdapters = caldroidGridAdapters;
		}

		/**
		 * Return virtual next position
		 * 
		 * @param position
		 * @return
		 */
		private int getNext(int position) {
			return (position + 1) % CaldroidFragment.NUMBER_OF_PAGES;
		}

		/**
		 * Return virtual previous position
		 * 
		 * @param position
		 * @return
		 */
		private int getPrevious(int position) {
			return (position + 3) % CaldroidFragment.NUMBER_OF_PAGES;
		}

		/**
		 * Return virtual current position
		 * 
		 * @param position
		 * @return
		 */
		public int getCurrent(int position) {
			return position % CaldroidFragment.NUMBER_OF_PAGES;
		}

		@Override
		public void onPageScrollStateChanged(int position) {
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		public void refreshAdapters(int position) {
			// Get adapters to refresh
			CaldroidGridAdapter currentAdapter = caldroidGridAdapters
					.get(getCurrent(position));
			CaldroidGridAdapter prevAdapter = caldroidGridAdapters
					.get(getPrevious(position));
			CaldroidGridAdapter nextAdapter = caldroidGridAdapters
					.get(getNext(position));

			if (position == currentPage) {
				// Refresh current adapter

				currentAdapter.setAdapterDateTime(currentDateTime);
				currentAdapter.notifyDataSetChanged();

				// Refresh previous adapter
				prevAdapter.setAdapterDateTime(currentDateTime.minus(0, 1, 0,
						0, 0, 0, 0, DateTime.DayOverflow.LastDay));
				prevAdapter.notifyDataSetChanged();

				// Refresh next adapter
				nextAdapter.setAdapterDateTime(currentDateTime.plus(0, 1, 0, 0,
						0, 0, 0, DateTime.DayOverflow.LastDay));
				nextAdapter.notifyDataSetChanged();
			}
			// Detect if swipe right or swipe left
			// Swipe right
			else if (position > currentPage) {
				// Update current date time to next month
				currentDateTime = currentDateTime.plus(0, 1, 0, 0, 0, 0, 0,
						DateTime.DayOverflow.LastDay);

				// Refresh the adapter of next gridview
				nextAdapter.setAdapterDateTime(currentDateTime.plus(0, 1, 0, 0,
						0, 0, 0, DateTime.DayOverflow.LastDay));
				nextAdapter.notifyDataSetChanged();

			}
			// Swipe left
			else {
				// Update current date time to previous month
				currentDateTime = currentDateTime.minus(0, 1, 0, 0, 0, 0, 0,
						DateTime.DayOverflow.LastDay);

				// Refresh the adapter of previous gridview
				prevAdapter.setAdapterDateTime(currentDateTime.minus(0, 1, 0,
						0, 0, 0, 0, DateTime.DayOverflow.LastDay));
				prevAdapter.notifyDataSetChanged();
			}

			// Update current page
			currentPage = position;
		}

		/**
		 * Refresh the fragments
		 */
		@Override
		public void onPageSelected(int position) {
			refreshAdapters(position);

			// Update current date time of the selected page
			setCalendarDateTime(currentDateTime);

			// Update all the dates inside current month
			CaldroidGridAdapter currentAdapter = caldroidGridAdapters
					.get(position % CaldroidFragment.NUMBER_OF_PAGES);

			// Refresh dateInMonthsList
			dateInMonthsList.clear();
			dateInMonthsList.addAll(currentAdapter.getDatetimeList());
		}

	}

//	@Override
//	public void onDetach() {
//		super.onDetach();
//
//		try {
//			try {
//				try {
//					Field childFragmentManager = Fragment.class
//							.getDeclaredField("mChildFragmentManager");
//					childFragmentManager.setAccessible(true);
//					childFragmentManager.set(this, null);
//
//				} catch (NoSuchFieldException e) {
//					throw new RuntimeException(e);
//				} catch (IllegalAccessException e) {
//					throw new RuntimeException(e);
//				}
//			} catch (RuntimeException e) {
//			}
//		} catch (Exception e) {
//		}
//	} not needed anymore on androidx
}
