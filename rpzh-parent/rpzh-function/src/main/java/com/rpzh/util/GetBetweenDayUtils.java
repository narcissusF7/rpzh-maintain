package com.rpzh.util;

import cn.hutool.core.date.DateUtil;
import com.rpzh.dto.AlarmValueSettingDto;
import com.rpzh.entity.ToolUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author: zyq
 * @Date: 2022/07/08/17:07
 * @Description: 获取两个日期之间的所有日期/月份/年份
 */
public class GetBetweenDayUtils {
    /**
     * 获取两个日期之间的所有日期 (年月日)
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return
     */
    public static List<String> getBetweenDays(String startTime, String endTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        // 声明保存日期集合
        List<String> list = new ArrayList<String>();
        try {
            // 转化成日期类型
            Date startDate = sdf.parse(startTime);
            Date endDate = sdf.parse(endTime);
            //用Calendar 进行日期比较判断
            Calendar calendar = Calendar.getInstance();
            while (startDate.getTime() <= endDate.getTime()) {
                // 把日期添加到集合
                list.add(sdf.format(startDate));
                // 设置日期
                calendar.setTime(startDate);
                //把日期增加一天
                calendar.add(Calendar.DATE, 1);
                // 获取增加后的日期
                startDate = calendar.getTime();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 获取两个日期之间的所有周数 (每周的周一)
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return
     */
    public static List<String> getBetweenWeek(String startTime, String endTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        // 声明保存日期集合
        List<String> list = new ArrayList<String>();
        try {
            // 转化成日期类型
            Date startDate = sdf.parse(startTime);
            Date endDate = sdf.parse(endTime);
            //用Calendar 进行日期比较判断
            Calendar startCalendar = Calendar.getInstance();
            startCalendar.setTime(startDate);
            Calendar endCalendar = Calendar.getInstance();
            endCalendar.setTime(endDate);
            startCalendar.set(Calendar.DAY_OF_WEEK,Calendar.MONDAY);
            endCalendar.set(Calendar.DAY_OF_WEEK,Calendar.MONDAY);
            while (startCalendar.getTime().getTime() <= endCalendar.getTime().getTime()) {
                // 把日期添加到集合
                list.add(sdf.format(startCalendar.getTime()));
                startCalendar.add(Calendar.DATE, 7);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 获取两个日期之间的所有月份 (年月)
     *
     * @param startTime
     * @param endTime
     * @return：YYYY-MM
     */
    public static List<String> getMonthBetweenDate(String startTime, String endTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        // 声明保存日期集合
        List<String> list = new ArrayList<String>();
        try {
            // 转化成日期类型
            Date startDate = sdf.parse(startTime);
            Date endDate = sdf.parse(endTime);

            //用Calendar 进行日期比较判断
            Calendar calendar = Calendar.getInstance();
            while (startDate.getTime() <= endDate.getTime()) {
                // 把日期添加到集合
                list.add(sdf.format(startDate));
                // 设置日期
                calendar.setTime(startDate);
                //把日期增加一天
                calendar.add(Calendar.MONTH, 1);
                // 获取增加后的日期
                startDate = calendar.getTime();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 获取两个日期之间的所有年
     *
     * @param startTime
     * @param endTime
     * @return：YYYY
     */
    public static List<String> getYearBetweenDate(String startTime, String endTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        // 声明保存日期集合
        List<String> list = new ArrayList<String>();
        try {
            // 转化成日期类型
            Date startDate = sdf.parse(startTime);
            Date endDate = sdf.parse(endTime);

            //用Calendar 进行日期比较判断
            Calendar calendar = Calendar.getInstance();
            while (startDate.getTime() <= endDate.getTime()) {
                // 把日期添加到集合
                list.add(sdf.format(startDate));
                // 设置日期
                calendar.setTime(startDate);
                //把日期增加一天
                calendar.add(Calendar.YEAR, 1);
                // 获取增加后的日期
                startDate = calendar.getTime();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static Map<String,String> get(String startTime,String endTime){

        //当前时间
        String nowTime = DateUtil.dateNew(new Date()).toString();
        //如果开始时间和结束时间都为空,日期向前推一周
        if (ToolUtil.isEmpty(startTime) && ToolUtil.isEmpty(endTime)) {
            startTime = DateUtil.lastWeek().toString();
            endTime = nowTime;
        }
        //如果开始时间不为空,结束时间为空,结束时间向后推一周
        if (ToolUtil.isNotEmpty(startTime) && ToolUtil.isEmpty(endTime)) {
            if(!startTime.contains(":")){
                startTime = startTime + " 00:00:00";
            }
            Date date = DateUtil.parse(startTime);
            endTime = DateUtil.offsetDay(date, 7).toString();
        }
        //如果开始时间为空,结束时间不为空,开始时间为向前推一周
        if (ToolUtil.isEmpty(startTime) && ToolUtil.isNotEmpty(endTime)) {
            if(!endTime.contains(":")){
                endTime = endTime + " 00:00:00";
            }
            Date date = DateUtil.parse(endTime);
            startTime = DateUtil.offsetDay(date, -7).toString();
        }
        Map<String,String> map = new HashMap<>();
        map.put("startTime",startTime);
        map.put("endTime",endTime);
        return map;
    }

    public static Map<String,Date> getMonth(Date startTime,Date endTime){
        //结束时间不为空
        if(ToolUtil.isNotEmpty(endTime)){
            endTime = new Date(endTime.getTime() + 24 * 60 * 60 * 1000 - 1);
        }
        //开始时间和结束时间为空时，开始时间为今天,结束时间为上个月的今天
        if(ToolUtil.isEmpty(startTime) && ToolUtil.isEmpty(endTime)){
            Calendar calendar = Calendar.getInstance();
            startTime = new Date();
            calendar.setTime(startTime);
            calendar.add(Calendar.MONTH, -1);
            endTime = new Date(calendar.getTime().getTime());
        }
        Map<String,Date> map = new HashMap<>();
        map.put("startTime",startTime);
        map.put("endTime",endTime);
        return map;
    }

    /*
     * 判断填写的预警值是否有少写
     */
    public static boolean getFlag(AlarmValueSettingDto alarmValueSettingDto){
        return alarmValueSettingDto.getStackOxygenUpperOne() == null ||
                alarmValueSettingDto.getStackOxygenUpperThree() == null ||
                alarmValueSettingDto.getStackOxygenUpperTwo() == null ||
                alarmValueSettingDto.getStackOxygenLower() == null ||
                alarmValueSettingDto.getStackTempUpperOne() == null ||
                alarmValueSettingDto.getStackTempUpperTwo() == null ||
                alarmValueSettingDto.getStackTempUpperThree() == null ||
                alarmValueSettingDto.getStackTempLower() == null ||
                alarmValueSettingDto.getStackMoistureUpperOne() == null ||
                alarmValueSettingDto.getStackMoistureUpperThree() == null ||
                alarmValueSettingDto.getStackMoistureUpperTwo() == null ||
                alarmValueSettingDto.getStackMoistureLower() == null ||
                alarmValueSettingDto.getTempCoreChipUpperOne() == null ||
                alarmValueSettingDto.getTempCoreChipUpperThree() == null ||
                alarmValueSettingDto.getTempCoreChipUpperTwo() == null ||
                alarmValueSettingDto.getTempCoreChipLower() == null ||
                alarmValueSettingDto.getCoreTempHighEnvTempOne() == null ||
                alarmValueSettingDto.getCoreTempHighEnvTempThree() == null ||
                alarmValueSettingDto.getCoreTempHighEnvTempTwo() == null ||
                alarmValueSettingDto.getCoreTempLowEnvTemp() ==null;
    }
}
