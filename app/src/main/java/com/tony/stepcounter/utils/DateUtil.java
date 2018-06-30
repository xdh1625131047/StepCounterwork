package com.tony.stepcounter.utils;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public final class DateUtil {
    /**
     * 获得今天的日期
     */
    public static String getTodayDate(){
        Date date=new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    /**
     * @param dateFormat 日期，格式为yyyy-MM-dd
     * @return 返回目标日期所属周的区间，例如：2017-12-10 ~ 2017-12-16
     */
    public static String getWeekSectionByDate(String dateFormat) {
        Date date = strToDate(dateFormat);
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        c.setTime(date);
        int dayOfWeek =c.get(Calendar.DAY_OF_WEEK);
        c.add(Calendar.DATE, 1 - dayOfWeek);
        String sunFormat = sdf.format(c.getTime());
        c.add(Calendar.DATE, 7);
        String saturdayFormat = sdf.format(c.getTime());
        return sunFormat + " ~ " + saturdayFormat;
    }

    /**
     * @param dateFormat 日期，格式为yyyy-MM-dd
     * @return 返回目标日期所属周的第一天日期，即周日日期，格式例如：2017-12-10
     */
    public static String getWeekStartByDate(String dateFormat) {
        Date date = strToDate(dateFormat);
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        c.setTime(date);
        int dayOfWeek =c.get(Calendar.DAY_OF_WEEK);
        c.add(Calendar.DATE, 1 - dayOfWeek);
        return sdf.format(c.getTime());
    }

    /**
     * @param dateFormat 日期，格式为yyyy-MM-dd
     * @return 返回目标日期所属周的最后一天日期，即周六日期，格式例如：2017-12-16
     */
    public static String getWeekEndByDate(String dateFormat) {
        Date date = strToDate(dateFormat);
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        c.setTime(date);
        int dayOfWeek =c.get(Calendar.DAY_OF_WEEK);
        c.add(Calendar.DATE, 7 - dayOfWeek);
        return sdf.format(c.getTime());
    }

    public static Date strToDate(String strDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        ParsePosition pos = new ParsePosition(0);
        Date strtodate = formatter.parse(strDate, pos);
        return strtodate;
    }

    public String getWeek(String sdate) {
        // 再转换为时间
        Date date = strToDate(sdate);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        // int hour=c.get(Calendar.DAY_OF_WEEK);
        // hour中存的就是星期几了，其范围 1~7
        // 1=星期日 7=星期六，其他类推
        return new SimpleDateFormat("EEEE").format(c.getTime());
    }
}