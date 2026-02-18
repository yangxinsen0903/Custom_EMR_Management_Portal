package com.sunbox.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Created by shanks on 15/4/21.
 */
public class DateUtil {
    private static final Logger logger = LoggerFactory.getLogger(DateUtil.class);
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /*
     *获取当天是本年第几周
     */
    public static int getCurrentWeek() {

        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.setTime(new Date());
        return calendar.get(Calendar.WEEK_OF_YEAR) - 2;
    }

    public static int getStartDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        date = calendar.getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        return Integer.parseInt(df.format(date));

    }

    public static int getendDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        date = calendar.getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        return Integer.parseInt(df.format(date));

    }

    //获取当前日期的字符串表达式
    public static String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        String sdate = df.format(date);
        return sdate;
    }

    //获取当前日期的字符串表达式
    public static String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        SimpleDateFormat df = new SimpleDateFormat("HHmmss");
        String sdate = df.format(date);
        return sdate;
    }

    //获取当前日期的字符串表达式
    public static String getCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM");
        String sdate = df.format(date);
        return sdate;
    }

    //获取当前日期的字符串表达式
    public static String getCurrentMonthDataCable() {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM");
        String sdate = df.format(date);
        return sdate;
    }

    //获取当前日期的字符串表达式
    public static String getCurrentSimpleDate() {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String ndate = df.format(date);
        return ndate;
    }

    //获取当前日期的字符串表达式
    public static String getNowDate() {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String ndate = df.format(date);
        return ndate;
    }

    /**
     * 返回明天
     *
     * @return
     */
    public static String getTomorrowDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + 1);
        Date date = calendar.getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String ndate = df.format(date);
        return ndate;
    }

    //获取当前日期的字符串表达式
    public static String getCurrentSimpleDateNoFormat() {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        String ndate = df.format(date);
        return ndate;
    }

    //获取当前日期的字符串表达式
    public static String getCurrentSimpleDateCable() {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String ndate = df.format(date);
        return ndate;
    }

    //获取当前日期的字符串表达式
    public static String getCurrentSimpleDateMD() {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        String ndate = df.format(date);
        return ndate;
    }

    //获取上个月的第一天
    public static String getAdvanceMonth() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        Date strDateTo = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        return format.format(strDateTo);
    }

    //获取上个月的最后一天
    public static String getAdvanceMonthLast() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date strDateTo = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        return format.format(strDateTo);
    }

    //当前月第一天
    public static String getCurrentMonthFirstDay() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        Date strDateTo = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        return format.format(strDateTo);
    }

    /**
     * 得到几天前的时间
     *
     * @param d
     * @param day
     * @return
     */
    public static String getDateTimeBefore(Date d, int day) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar now = Calendar.getInstance();
        now.setTime(d);
        now.set(Calendar.DATE, now.get(Calendar.DATE) - day);
        return sdf.format(now.getTime());
    }


    /**
     * 得到几天后的时间
     *
     * @param d
     * @param day
     * @return
     */
    public static String getDateTimeAfter(Date d, int day) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar now = Calendar.getInstance();
        now.setTime(d);
        now.set(Calendar.DATE, now.get(Calendar.DATE) + day);
        return sdf.format(now.getTime());
    }

    /**
     * 得到几天前
     *
     * @param d
     * @param day
     * @return
     */
    public static String getDateBefore(Date d, int day) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar now = Calendar.getInstance();
        now.setTime(d);
        now.set(Calendar.DATE, now.get(Calendar.DATE) - day);
        return sdf.format(now.getTime());
    }

    //获取当前日期的字符串表达式
    public static Date getDate() {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        return date;
    }

    //指定日期格式参数，给定字符串的日期参数，转换成该格式的日期
    public static Date toDate(String format, String time) {
        SimpleDateFormat df = new SimpleDateFormat(format);
//	   	 System.out.println(format);
        Date date = null;

        if (time == null || "".equals(time)) {
            return null;
        }
        try {
            date = df.parse(time);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return date;
    }

    //日期格式化为时间 形如:20150818215722
    public static Date stringToDate(String dateString, String format) {
        Date date = null;
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        String newdate = dateString.substring(0, 4) + "-" + dateString.substring(4, 6) + "-" + dateString.substring(6, 8) + " " + dateString.substring(8, 10) + ":" + dateString.substring(10, 12) + ":" + dateString.substring(12, 14);
        try {
            date = sdf.parse(newdate);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return date;
    }

    //日期格式化为时间  形如:2015-08-18
    public static Date stringToShortDate(String dateString, String format) {
        Date date = null;
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        String newdate = dateString.substring(0, 4) + "-" + dateString.substring(5, 7) + "-" + dateString.substring(8, 10);
        try {
            date = sdf.parse(newdate);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return date;
    }

    //日期格式化为时间  形如:7/27/2015 4:28:48 PM ===》 2015-08-18
    public static String formtDate(String dateString) {
        String[] str = dateString.split(" ");
        String[] str1 = str[0].split("/");
        String date = str1[2] + "-" + str1[0] + "-" + str1[1];
        return date;
    }

    //日期格式化为时间  形如:7/27/2015 4:28:48 PM ===》 2015-08-18
    public static String formtDate2(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.ENGLISH);
            Date date = sdf.parse(dateString);
            sdf = new SimpleDateFormat("yyyy-MM-dd");
            String date2 = sdf.format(date);
            return date2;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return dateString;
    }

    /**
     * 获取当前日期，精确到日
     *
     * @return
     * @author niudan
     */
    public static Date getCurrDate() {
        Calendar calendar = Calendar.getInstance();
        Date currdate = calendar.getTime();
        DateFormat dateFormat = DateFormat.getDateInstance();
//		System.out.println("当前日期：" + formatDate("yyyy-MM-dd", currdate));
        return new Date(currdate.getYear(), currdate.getMonth(), currdate.getDate());
    }

    /**
     * 获取增加天数后的日期
     *
     * @param date
     * @param days
     * @return
     * @author niudan
     */
    public static Date addDays(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, days);
        Date currdate = calendar.getTime();
        return new Date(currdate.getYear(), currdate.getMonth(), currdate.getDate());
    }

    public static Date addSeconds(Date date,int seconds){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, seconds);
        return calendar.getTime();
    }


    /**
     * 返回时间间隔天数
     *
     * @param day
     * @return
     */
    public static int dayNum(String day) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        try {
            c1.setTime(df.parse(getCurrentSimpleDate()));
            c2.setTime(df.parse(day));
        } catch (ParseException e) {
            System.err.println("格式不正确");
        }
        int result = c1.compareTo(c2);
        return result;
    }

    public static Date getCurdateOrigin() {
        Date date = new Date();
        return date;
    }

    /**
     * 格式化日期
     *
     * @param format
     * @param date
     * @return
     * @author niudan
     */
    public static String formatDate(String format, Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }

    /**
     * 判断当前时间（时分）是否在某一时间区间
     *
     * @param dur
     * @return
     */
    public static boolean inTimeZones(String dur) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        String current = dateFormat.format(new Date());
        String[] dateArr = dur.split("-");
        boolean flag = false;
        try {
            Date currDate = dateFormat.parse(current);
            Date startDate = dateFormat.parse(dateArr[0]);
            Date endDate = dateFormat.parse(dateArr[1]);
            if (startDate.before(endDate)) {
                if (currDate.after(startDate) && currDate.before(endDate))
                    flag = true;
            } else {
                if (currDate.after(startDate) || currDate.before(endDate))
                    flag = true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return flag;
    }

    public static int compare_date(String DATE1, String DATE2) {

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date dt1 = df.parse(DATE1);
            Date dt2 = df.parse(DATE2);
            if (dt1.getTime() > dt2.getTime()) {
                return 1;
            } else if (dt1.getTime() < dt2.getTime()) {
                return -1;
            } else {
                return 0;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取某个月的第一天
     *
     * @param date 某个月的时间
     */
    public static Date currMonthFirstDay(Date date) {
        Calendar cal = Calendar.getInstance();
        if (date != null) {
            cal.setTime(date);
        } else {
            cal.setTime(new Date());
        }
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 0, 0, 0);
        return cal.getTime();
    }

    /**
     * 获取某个月的最后一天
     *
     * @param date 某个月的时间
     */
    public static Date currMonthLastDay(Date date) {
        Calendar cal = Calendar.getInstance();
        if (date != null) {
            cal.setTime(date);
        }
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 23, 59, 59);
        return cal.getTime();
    }

    /**
     * 日期月份处理
     *
     * @param d     时间
     * @param month 相加的月份，正数则加，负数则减
     * @return
     */
    public static Date timeMonthManage(Date d, int month) {
        Calendar rightNow = Calendar.getInstance();
        rightNow.setTime(d);
        rightNow.add(Calendar.MONTH, month);
        return rightNow.getTime();
    }

    /**
     * 获取昨天
     *
     * @param
     * @return
     */
    public static String getYesterday(String type) {
        try {
            Date date = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            //获取昨天
            calendar.add(Calendar.DAY_OF_MONTH, -1);

            date = calendar.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat(type);
            return sdf.format(sdf.parse(sdf.format(date)));
        } catch (Exception e) {
            return null;
        }
    }

    public static String toString(Date date, String formatString) {
        if (null == date) return null;
        SimpleDateFormat df = new SimpleDateFormat(formatString);
        return df.format(date);
    }

    /**
     * 判断是否是本月第一天或者最后一天
     *
     * @param date
     * @return
     */
    public static boolean isFirstOrLastDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTime(date);
        int firstDay = calendar.getActualMinimum(calendar.DAY_OF_MONTH);
        int lastDay = calendar.getActualMaximum(calendar.DAY_OF_MONTH);
        int now = calendar.get(Calendar.DAY_OF_MONTH);
        if (now == firstDay) {
            return true;
        } else if (now == lastDay) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * 获取今天或昨天
     *
     * @param type yesterday 获取昨天
     * @return 格式化后的日期 yyyy-MM-dd
     */
    public static String getDay(String type) {
        try {
            Date date = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            if ("yesterday".equals(type)) {
                //获取昨天
                calendar.add(Calendar.DAY_OF_MONTH, -1);
            }
            date = calendar.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            return sdf.format(sdf.parse(sdf.format(date)));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取当前年份一月的时间
     *
     * @param date
     * @return
     */
    public static Date getOneMonth(Date date) {
        Calendar cd = Calendar.getInstance();
        cd.setTime(date);
        cd.set(Calendar.MONTH, 0);
        cd.set(Calendar.HOUR_OF_DAY, 0);
        cd.set(Calendar.MINUTE, 0);
        cd.set(Calendar.SECOND, 0);
        cd.set(Calendar.MILLISECOND, 0);

        return cd.getTime();
    }

    public static Date dateAddOrSubHour(Date date, int hour) {
        Calendar cd = Calendar.getInstance();
        cd.setTime(date);
        cd.add(Calendar.HOUR, hour);
        cd.set(Calendar.SECOND, 0);
        cd.set(Calendar.MILLISECOND, 0);
        return cd.getTime();
    }

    public static Date longTime(String str) {
        long time = Long.parseLong(str);
        String result1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(time);
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(result1);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 日期转换
     *
     * @param time
     * @return
     */
    public static Date string2Date(String time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = sdf.parse(time);
            return date;
        } catch (Exception e) {
            return null;
        }
    }

    public static String dateToStr(Date date,String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.format(date);
        } catch (Exception e) {
            return null;
        }
    }

    public static void main(String[] args) {

        longTime("1568128900");
        List<String> futureDateList = getFutureDateList(7);
        System.out.println(futureDateList);


//		System.out.println(getDateTimeBefore(getDate(), 3));
//		System.out.println(getDateBefore(getDate(),1));
		/*HashMap<String ,Object>  x=new HashMap<String, Object>();
		x.put("a","123");
		x.put("b","456");

		System.out.println(x.containsKey("a"));*/

	/*	System.out.println(getCurrentDate());
		System.out.println(getDate());
		System.out.println(stringToDate(getCurrentSimpleDateNoFormat(), "yyyy-MM-dd HH:mm:ss"));
		System.out.println(getCurrentSimpleDateNoFormat());*/
		/*System.out.println(getCurrDate());
		System.out.println(Calendar.getInstance().getTime());*/
//		System.out.println(formatDate("yyyy-MM-dd",getCurrDate()));
//		System.out.println(formatDate("yyyy-MM-dd", addDays(getCurrDate(),30)));
        /*System.out.println(DateUtil.getDateBefore(DateUtil.getDate(), 1));*/

	/*	String date = "7/27/2015 4:28:48 PM";
		System.out.println(getTomorrowDate());*/
    }

    /**
     * 判断当前日期是不是本月第一天 如果不是返回当前月的日期
     * 如果是返回上个月的日期
     */
    public static Date thisDateOne(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Calendar c = Calendar.getInstance();

        String dateStr = sdf.format(date);
        System.out.println(dateStr);
        if (dateStr.substring(6, 8).equals("01")) {
            String lastday;
            //SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            Calendar cale = null;
            cale = Calendar.getInstance();
            cale.add(Calendar.MONTH, 0);
            cale.set(Calendar.DAY_OF_MONTH, 0);
            lastday = sdf.format(cale.getTime());
            try {
                return sdf.parse(lastday);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            return date;
        }
        return date;
    }

    /**
     * 通过时间秒毫秒数判断两个时间的间隔
     *
     * @param dateStr 结束日期, 被减数
     * @return dateStr减当前日期的差值
     */
    public static int differentDaysByMillisecond(String dateStr) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int days = -1;
        try {
            Date date2 = format.parse(dateStr);
            // 当前日期
            Date date1 = new Date();
            days = (int) ((date2.getTime() - date1.getTime()) / (1000 * 3600 * 24));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return days;
    }

    /**
     * 获取当前日期到num天的所有日期
     *
     * @param num
     * @return
     */
    public static List<String> getFutureDateList(int num) {
        List<String> list = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        // 获取当前日期
        Date currDate = getCurrDate();
        list.add(dateFormat.format(currDate));
        for (int i = 1; i < num; i++) {
            Date date = addDays(currDate, i);
            list.add(dateFormat.format(date));
        }
        return list;
    }

    /**
     * 检查给定的时间戳是否在最近的5分钟内。
     *
     * @param time 要检查的时间，格式为yyyy-MM-dd HH:mm:ss
     * @return 如果时间戳在最近的5分钟内返回true，否则返回false
     */
    public static Boolean check(String time) {
        try {
            // 定义日期时间格式化器
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
            LocalDateTime dateTime = LocalDateTime.parse(time, formatter);
            LocalDateTime now = LocalDateTime.now();
            long minutesBetween = ChronoUnit.MINUTES.between(dateTime, now);
            logger.info("dateTime:{},minutesBetween：{}" , dateTime,minutesBetween);
            // 检查差值是否小于或等于5分钟
            return minutesBetween >= 0 && minutesBetween <= 5;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 检查给定的时间是否在最近的num 单位之前。 比如是否为30分钟之前的.
     * @param date
     * @param unit
     * @param num
     * @return
     */
    public static Boolean checkTime(Date date, String format, ChronoUnit unit, int num) {
        try {
            String time = formatDate(format, date);
            // 定义日期时间格式化器
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            LocalDateTime dateTime = LocalDateTime.parse(time, formatter);
            LocalDateTime now = LocalDateTime.now();
            // now - dateTime
            long minutesBetween = unit.between(dateTime, now);
            return minutesBetween > num;
        } catch (Exception e) {
            logger.error("checkTime2", e);
            throw new RuntimeException(e);
        }
    }

    public static String generateFutureTime() {
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        // 添加24小时
        LocalDateTime twentyFourHoursLater = now.plus(Duration.ofHours(24));
        // 定义日期时间格式化器
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        String formattedFutureTime = twentyFourHoursLater.format(formatter);
        return formattedFutureTime;
    }

    /**
     * 检查给定的时间是否在最近的num 单位之前。 比如是否为30分钟之前的.
     * @param time
     * @param unit
     * @param num
     * @return
     */
    public static Boolean checkTime(LocalDateTime time, ChronoUnit unit, int num) {
        try {
            // 定义日期时间格式化器
            LocalDateTime now = LocalDateTime.now();
//            // now - dateTime
            long minutesBetween = unit.between(time, now);
            return   minutesBetween > num;
        }catch (Exception e){
            logger.error("checkTime3", e);
            throw new RuntimeException(e);
        }
    }

}
