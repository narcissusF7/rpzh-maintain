package com.rpzh.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateToStrUtil {

    private static final Logger logger = LoggerFactory.getLogger(DateToStrUtil.class);


    private final static DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00:00");

    private final static DateTimeFormatter df1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:59:59");

    private final static DateTimeFormatter df2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String getPreviousHourStart(){
        return df.format(LocalDateTime.now().minusHours(1));
    }

    public static String getPreviousHourEnd(){
        return df1.format(LocalDateTime.now().minusHours(1));
    }

    public static String getYesterday(){
        return df2.format(LocalDateTime.now().plusDays(-1));
    }

    public static String getNowDay(){
        return df2.format(LocalDateTime.now());
    }

    public static String getYesterdayTime(){
        return df2.format(LocalDateTime.now().plusDays(-1))+" 00:00:00";
    }

    public static String getNowDayTime(){
        return df2.format(LocalDateTime.now())+" 00:00:00";
    }

    /**
     * 将数字格式化为某一天一小时0分0秒
     * @param beginDate
     * @return
     */
    public static String  startDate(String beginDate){
        String YEAR = beginDate.substring(0,4);
        String MONTH = beginDate.substring(4, 6);
        String DAY = beginDate.substring(6, 8);
        String HOUR = beginDate.substring(8, 10);

        StringBuilder sb = new StringBuilder();
        sb.append(YEAR+"-");
        sb.append(MONTH+"-");
        sb.append(DAY+" ");
        sb.append(HOUR);
        sb.append(":00:00");

        return sb.toString();
    }


    /**
     * 将数字格式化为前一天59分59秒
     * @param endDate
     * @return
     */
    public static String  endDate(String endDate){
        String YEAR = endDate.substring(0,4);
        String MONTH = endDate.substring(4, 6);
        String DAY = endDate.substring(6, 8);
        String HOUR = endDate.substring(8, 10);

        StringBuilder sb = new StringBuilder();
        sb.append(YEAR+"-");
        sb.append(MONTH+"-");
        sb.append(DAY+" ");
        sb.append(HOUR);
        sb.append(":59:59");

        return sb.toString();
    }


    /**
     * 获得前一天凌晨
     * @return
     */
    public static String getBeginYesterday(){
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        LocalDateTime localDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).plusDays(-1);
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDateTime.atZone(zone).toInstant();
        Date date = Date.from(instant);

        return sdf.format(date);
    }


    /**
     * 获得前一天23:59分
     * @return
     */
    public static String getLastYesterday(){
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        LocalDateTime localDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MAX).plusDays(-1);
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDateTime.atZone(zone).toInstant();
        Date date = Date.from(instant);

        return sdf.format(date);
    }

    /**
     * 获取当前日期时分秒
     * @return
     */
    public static String getCurrentDayTime(){
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }


    /**
     * 将字符串日期转为Instant
     * @param str
     * @return
     */
    public static Instant strToInstant(String str,String pattern){
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        Date date = null;
        try {
            date = sdf.parse(str);
        } catch (ParseException e) {
            logger.info(e.getMessage());
        }
        TimeZone curTimeZone = TimeZone.getTimeZone("GMT+8");
        Calendar c = Calendar.getInstance(curTimeZone);
        c.setTime(date);
        Date z = c.getTime();

        return  z.toInstant();
    }

    public static void main(String[] args){

/*        String str = "2018072201";
        String minDate = startDate(str);
        String endDate = endDate(str);
        System.out.println(minDate+"=========="+endDate);*/
        /*System.out.println(getCurrentDayTime());*/
//        System.out.println(getNowDayTime());
/*        System.out.println(getBeginYesterday());
        System.out.println(getLastYesterday());*/

//        Instant instant = strToInstant("2018-07-26", "yyyy-MM-dd");
//        System.out.println(instant);
        String currentDate = getCurrentDate();
      /*  System.out.println(currentDate);*/
    }

    public static String getCurrentDate(){
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    public static String getDate(){
        return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    }

    //时间查询含头不含尾导致结束日期没查出来，处理
    public static Date getEndDate(Date endTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endTime);
        calendar.set(Calendar.HOUR_OF_DAY,23);
        calendar.set(Calendar.MINUTE,59);
        calendar.set(Calendar.SECOND,59);
        Date time = calendar.getTime();
        endTime=time;
        return endTime;
    }

}
