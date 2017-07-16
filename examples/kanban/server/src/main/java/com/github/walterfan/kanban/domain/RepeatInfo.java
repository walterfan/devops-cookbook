package com.github.walterfan.kanban.domain;



import org.apache.commons.lang3.time.DateUtils;

import java.sql.Timestamp;
import java.util.Calendar;

/*
 * Usage: PIMS has the following recurring types : 
 * Recurring type
 *  1) Daily 
 *      i. Every N days 
 *      ii. Every weekday 
 *      
 *  2) Weekly 
 *      i. One or more of Mon, Tue, Wed, Thu, Fri,Sat and Sun 
 *  
 *  3) Monthly 
 *      i. N-th of every N month 
 *      ii. 1st, 2nd, 3rd, 4th or last of Mon, Tue, Wed, Thu, Fri, Sat or Sun of every N month. 
 *      
 *  4) Yearly 
 *      i. Every single specific date in MM-DD form. 
 *      ii. 1st, 2nd, 3rd, 4th or last of Mon, Tue, Wed, Thu, Fri, Sat or Sun of one of 12 months. 
 *      
 *  Ending type: all recurring task have the following ending condition 
 *      1) No ending date. 
 *      2) End after a specific date in MM/DD form. 
 *      3) End after N session. 
 *  
 * 
 * */
public class RepeatInfo extends BaseObject {

    public static final String NO_END_DATE = "noEndDate";
    public static final String HAS_END_DATE = "hasEndDate";
    
        
    private int repeatID;
	private String repeatType;
	private Timestamp effectiveTime;
	private Timestamp expireTime;
	private int interval;
	private int startHour;
	private int startMinute;
	private int startSecond;
	private int daysInWeek ;
	private int daysInMonth ;
	private int weekInMonth ;
	private int monthInYear ;
	private int dateInYear;
	private int duration;
	private int alwaysRepeat = 0;
	
	public String getRepeatType() {
		return repeatType;
	}
	public void setRepeatType(String repeatType) {
		this.repeatType = repeatType;
	}
	public Timestamp getExpireTime() {
		return expireTime;
	}
	public void setExpireTime(Timestamp expireTime) {
		this.expireTime = expireTime;
	}
	public int getInterval() {
		return interval;
	}
	public void setInterval(int interval) {
		this.interval = interval;
	}
	public int getStartHour() {
		return startHour;
	}
	public void setStartHour(int startHour) {
		this.startHour = startHour;
	}
	public int getStartMinute() {
		return startMinute;
	}
	public void setStartMinute(int startMinute) {
		this.startMinute = startMinute;
	}
	public int getStartSecond() {
		return startSecond;
	}
	public void setStartSecond(int startSecond) {
		this.startSecond = startSecond;
	}
	public int getDaysInWeek() {
		return daysInWeek;
	}
	public void setDaysInWeek(int daysInWeek) {
		this.daysInWeek = daysInWeek;
	}
	public int getDaysInMonth() {
		return daysInMonth;
	}
	public void setDaysInMonth(int daysInMonth) {
		this.daysInMonth = daysInMonth;
	}
	public int getWeekInMonth() {
		return weekInMonth;
	}
	public void setWeekInMonth(int weekInMonth) {
		this.weekInMonth = weekInMonth;
	}
	public int getMonthInYear() {
		return monthInYear;
	}
	public void setMonthInYear(int monthInYear) {
		this.monthInYear = monthInYear;
	}
	public int getDateInYear() {
		return dateInYear;
	}
	public void setDateInYear(int dateInYear) {
		this.dateInYear = dateInYear;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
    
    /**
     * @return the repeatID
     */
    public int getRepeatID() {
        return repeatID;
    }
    
    /**
     * @param repeatID the repeatID to set
     */
    public void setRepeatID(int repeatID) {
        this.repeatID = repeatID;
    }
    
    /**
     * @return the alwaysRepeat
     */
    public int getAlwaysRepeat() {
        return alwaysRepeat;
    }
    
    /**
     * @param alwaysRepeat the alwaysRepeat to set
     */
    public void setAlwaysRepeat(int alwaysRepeat) {
        this.alwaysRepeat = alwaysRepeat;
    }
    
    /**
     * @return the effectiveTime
     */
    public Timestamp getEffectiveTime() {
        return effectiveTime;
    }
    
    /**
     * @param effectiveTime the effectiveTime to set
     */
    public void setEffectiveTime(Timestamp effectiveTime) {
        this.effectiveTime = effectiveTime;
    }
	
    public long getIntervalMS(Timestamp beginTime) {
        if ("daily".equalsIgnoreCase(repeatType)) {
            return getInterval()
                              * DateUtils.MILLIS_PER_DAY;
        } else if ("weekly".equalsIgnoreCase(repeatType)) {
            return getInterval() * 7
                              * DateUtils.MILLIS_PER_DAY;
        }  else if ("monthly".equalsIgnoreCase(repeatType)) {
            long days = DateUtils.getFragmentInDays(beginTime, Calendar.MONTH);
            return days * getInterval() * DateUtils.MILLIS_PER_DAY;

        }  else if ("yearly".equalsIgnoreCase(repeatType)) {
            long days = DateUtils.getFragmentInDays(beginTime, Calendar.YEAR);
            return days * getInterval() * DateUtils.MILLIS_PER_DAY;
        }
        return 0;
    }
	
}
