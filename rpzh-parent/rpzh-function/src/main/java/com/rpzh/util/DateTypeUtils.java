package com.rpzh.util;

import com.rpzh.vo.WarningOrDealVo;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author: zyq
 * @Date: 2022/06/13/15:40
 * @Description: 功能
 */
public class DateTypeUtils {
    public static Map<String,WarningOrDealVo> getDayCountList(Date start, Date end, Integer dayType){
        // 时间类型,1:日,2:周,3:月,4:年   默认为月
        Map<String,WarningOrDealVo> warningOrDealVos = new HashMap<>();
        SimpleDateFormat formatter;
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(start);
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(end);
        switch (dayType){
            case 1:
                int days = (int) ((end.getTime() - start.getTime()) / (1000*3600*24));
                formatter = new SimpleDateFormat("MMdd");
                for (int j=0;j<=days;j++){
                    String dateString = formatter.format(startCalendar.getTime());
                    WarningOrDealVo warningOrDealVo=new WarningOrDealVo();
                    warningOrDealVo.setDayType(dateString);
                    warningOrDealVo.setCount(0);
                    warningOrDealVos.put(dateString,warningOrDealVo);
                    startCalendar.add(Calendar.DAY_OF_MONTH,1);
                }
                break;
            case 2:
                startCalendar.setFirstDayOfWeek(Calendar.MONDAY);
                endCalendar.setFirstDayOfWeek(Calendar.MONDAY);
                int l = endCalendar.get(Calendar.WEEK_OF_YEAR) - startCalendar.get(Calendar.WEEK_OF_YEAR) + 1;
                for (int i=1;i<=l;i++){
                    WarningOrDealVo warningOrDealVo=new WarningOrDealVo();
                    warningOrDealVo.setDayType(i+"");
                    warningOrDealVo.setCount(0);
                    warningOrDealVos.put(i+"",warningOrDealVo);
                }
                break;
            case 3:
                int year = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
                int month = year * 12 + endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);
                formatter = new SimpleDateFormat("yyyyMM");
                for (int i=0 ;i<=month;i++){
                    String dateString = formatter.format(startCalendar.getTime());
                    WarningOrDealVo warningOrDealVo=new WarningOrDealVo();
                    warningOrDealVo.setDayType(dateString);
                    warningOrDealVo.setCount(0);
                    warningOrDealVos.put(dateString,warningOrDealVo);
                    startCalendar.add(Calendar.MONTH,1);
                }
                break;
            case 4:
                int years = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
                for (int i=0;i<=years;i++){
                    WarningOrDealVo warningOrDealVo=new WarningOrDealVo();
                    warningOrDealVo.setDayType(startCalendar.get(Calendar.YEAR) + "");
                    warningOrDealVo.setCount(0);
                    warningOrDealVos.put(startCalendar.get(Calendar.YEAR) + "",warningOrDealVo);
                    startCalendar.add(Calendar.YEAR,1);
                }
                break;
        }
        return warningOrDealVos;
    }

    public static Map<String,WarningOrDealVo> getIsDealCountList(Date start, Date end, Integer dayType){
        // 时间类型,1:日,2:周,3:月,4:年   默认为月
        Map<String,WarningOrDealVo> warningOrDealVos = new HashMap<>();
        SimpleDateFormat formatter;
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(start);
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(end);
        switch (dayType){
            case 1:
                int days = (int) ((end.getTime() - start.getTime()) / (1000*3600*24));
                formatter = new SimpleDateFormat("MMdd");
                for (int j=0;j<=days;j++){
                    String dateString = formatter.format(startCalendar.getTime());
                    WarningOrDealVo warningOrDealVo=new WarningOrDealVo();
                    warningOrDealVo.setDayType(dateString);
                    warningOrDealVo.setCount(0);
                    warningOrDealVo.setDeal(0);
                    warningOrDealVos.put(dateString+"0",warningOrDealVo);
                    warningOrDealVo=new WarningOrDealVo();
                    warningOrDealVo.setDayType(dateString);
                    warningOrDealVo.setCount(0);
                    warningOrDealVo.setDeal(1);
                    warningOrDealVos.put(dateString+"1",warningOrDealVo);
                    warningOrDealVo=new WarningOrDealVo();
                    warningOrDealVo.setDayType(dateString);
                    warningOrDealVo.setCount(0);
                    warningOrDealVo.setDeal(2);
                    warningOrDealVos.put(dateString+"2",warningOrDealVo);
                    startCalendar.add(Calendar.DAY_OF_MONTH,1);
                }
                break;
            case 2:
                startCalendar.setFirstDayOfWeek(Calendar.MONDAY);
                endCalendar.setFirstDayOfWeek(Calendar.MONDAY);
                int l = endCalendar.get(Calendar.WEEK_OF_YEAR) - startCalendar.get(Calendar.WEEK_OF_YEAR) + 1;
                for (int i=1;i<=l;i++){
                    WarningOrDealVo warningOrDealVo=new WarningOrDealVo();
                    warningOrDealVo.setDayType(i+"");
                    warningOrDealVo.setCount(0);
                    warningOrDealVo.setDeal(0);
                    warningOrDealVos.put(i+"0",warningOrDealVo);
                    warningOrDealVo=new WarningOrDealVo();
                    warningOrDealVo.setDayType(i+"");
                    warningOrDealVo.setCount(0);
                    warningOrDealVo.setDeal(1);
                    warningOrDealVos.put(i+"1",warningOrDealVo);
                    warningOrDealVo=new WarningOrDealVo();
                    warningOrDealVo.setDayType(i+"");
                    warningOrDealVo.setCount(0);
                    warningOrDealVo.setDeal(2);
                    warningOrDealVos.put(i+"2",warningOrDealVo);
                }
                break;
            case 3:
                int year = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
                int month = year * 12 + endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);
                formatter = new SimpleDateFormat("yyyyMM");
                for (int i=0 ;i<=month;i++){
                    String dateString = formatter.format(startCalendar.getTime());
                    WarningOrDealVo warningOrDealVo=new WarningOrDealVo();
                    warningOrDealVo.setDayType(dateString);
                    warningOrDealVo.setCount(0);
                    warningOrDealVo.setDeal(0);
                    warningOrDealVos.put(dateString+"0",warningOrDealVo);warningOrDealVo=new WarningOrDealVo();
                    warningOrDealVo.setDayType(dateString);
                    warningOrDealVo.setCount(0);
                    warningOrDealVo.setDeal(1);
                    warningOrDealVos.put(dateString+"1",warningOrDealVo);
                    warningOrDealVo.setDayType(dateString);
                    warningOrDealVo.setCount(0);
                    warningOrDealVo.setDeal(2);
                    warningOrDealVos.put(dateString+"2",warningOrDealVo);
                    startCalendar.add(Calendar.MONTH,1);
                }
                break;
            case 4:
                int years = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
                for (int i=0;i<=years;i++){
                    WarningOrDealVo warningOrDealVo=new WarningOrDealVo();
                    warningOrDealVo.setDayType(startCalendar.get(Calendar.YEAR) + "");
                    warningOrDealVo.setCount(0);
                    warningOrDealVo.setDeal(0);
                    warningOrDealVos.put(startCalendar.get(Calendar.YEAR)+"0",warningOrDealVo);
                    warningOrDealVo=new WarningOrDealVo();
                    warningOrDealVo.setDayType(startCalendar.get(Calendar.YEAR)  + "");
                    warningOrDealVo.setCount(0);
                    warningOrDealVo.setDeal(1);
                    warningOrDealVos.put(startCalendar.get(Calendar.YEAR)+"1",warningOrDealVo);
                    warningOrDealVo=new WarningOrDealVo();
                    warningOrDealVo.setDayType(startCalendar.get(Calendar.YEAR)  + "");
                    warningOrDealVo.setCount(0);
                    warningOrDealVo.setDeal(2);
                    warningOrDealVos.put(startCalendar.get(Calendar.YEAR)+"2",warningOrDealVo);
                    startCalendar.add(Calendar.YEAR,1);
                }
                break;
        }
        return warningOrDealVos;
    }

}
