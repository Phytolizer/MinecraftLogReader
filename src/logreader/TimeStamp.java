package logreader;

import java.time.Duration;
import java.time.temporal.TemporalAmount;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.IllegalFormatException;

/**
 * Created by mario on 3/14/2017.
 */
public class TimeStamp {
	public int year;
	public int month;
	public int day;
	public int hour;
	public int minute;
	public int second;
	public TimeStamp(String time) throws IllegalFormatException{
		if(time.length() != 19)
			rejectInput();
		year = Integer.parseInt(time.substring(0, 4));
		if(year < 0)
			rejectInput();
		month = Integer.parseInt(time.substring(5, 7));
		if(month < 1 || month > 12)
			rejectInput();
		day = Integer.parseInt(time.substring(8, 10));
		Calendar calendar = new GregorianCalendar(year, month - 1, day);
		int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		if(day < 1 || day > maxDay)
			rejectInput();
		hour = Integer.parseInt(time.substring(11, 13));
		if(hour < 0 || hour > 23)
			rejectInput();
		minute = Integer.parseInt(time.substring(14, 16));
		if(minute < 0 || minute > 59)
			rejectInput();
		second = Integer.parseInt(time.substring(17, 19));
		if(second < 0 || second > 59)
			rejectInput();
	}
	public Duration subtractFrom(TimeStamp endDate) {
		int newDay = endDate.day - day;
		int newHour = endDate.hour - hour;
		int newMinute = endDate.minute - minute;
		int newSecond = endDate.second - second;
		Duration out = Duration.ZERO;
		out = out.plusDays(newDay);
		out = out.plusHours(newHour);
		out = out.plusMinutes(newMinute);
		out = out.plusSeconds(newSecond);
		return out;
	}
	private void rejectInput() throws IllegalFormatException{
		throw new NumberFormatException("Bad time format! Correct format is YYYY-MM-DD hh:mm:ss");
	}
}
