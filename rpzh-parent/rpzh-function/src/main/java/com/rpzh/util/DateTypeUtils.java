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
        switch (dayType){
            case 1:
                int days = (int) ((end.getTime() - start.getTime()) / (1000*3600*24));
                formatter = new SimpleDateFormat("MMdd");
                for (int j=0;j<=days;j++){
                    start.setDate(start.getDate()+j);
                    String dateString = formatter.format(start);
                    WarningOrDealVo warningOrDealVo=new WarningOrDealVo();
                    warningOrDealVo.setDayType(dateString);
                    warningOrDealVo.setCount(0);
                    warningOrDealVos.put(dateString,warningOrDealVo);
                }
                break;
            case 2:
                int l = Integer.parseInt((end.getTime() - start.getTime()) / (1000 * 3600 * 24 * 7) + "");
                long k =(end.getTime() - start.getTime()) % (1000 * 3600 * 24 * 7);
                SimpleDateFormat dateFm = new SimpleDateFormat("EEEE");
                String endFormat = dateFm.format(end);
                Integer endWeek = get(endFormat);
                String startFormat = dateFm.format(start);
                Integer startWeek = get(startFormat);
                if(endWeek<=startWeek || k!=0){
                    l = l + 1 ;
                }
                for (int i=1;i<=l;i++){
                    WarningOrDealVo warningOrDealVo=new WarningOrDealVo();
                    warningOrDealVo.setDayType(i+"");
                    warningOrDealVo.setCount(0);
                    warningOrDealVos.put(i+"",warningOrDealVo);
                }
                break;
            case 3:
                Calendar startCalendar = Calendar.getInstance();
                startCalendar.setTime(start);
                Calendar endCalendar = Calendar.getInstance();
                endCalendar.setTime(end);
                int year = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
                int month = year * 12 + endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);
                formatter = new SimpleDateFormat("yyyyMM");
                for (int i=0 ;i<=month;i++){
                    start.setMonth(start.getMonth()+i);
                    String dateString = formatter.format(start);
                    WarningOrDealVo warningOrDealVo=new WarningOrDealVo();
                    warningOrDealVo.setDayType(dateString);
                    warningOrDealVo.setCount(0);
                    warningOrDealVos.put(dateString,warningOrDealVo);
                }
                break;
            case 4:
                int years = end.getYear() - start.getYear();
                for (int i=0;i<=years;i++){
                    WarningOrDealVo warningOrDealVo=new WarningOrDealVo();
                    warningOrDealVo.setDayType(start.getYear() + 1 + "");
                    warningOrDealVo.setCount(0);
                    warningOrDealVos.put(start.getYear() + 1 + "",warningOrDealVo);
                }
                break;
        }
        return warningOrDealVos;
    }

    public static Map<String,WarningOrDealVo> getIsDealCountList(Date start, Date end, Integer dayType){
        // 时间类型,1:日,2:周,3:月,4:年   默认为月
        Map<String,WarningOrDealVo> warningOrDealVos = new HashMap<>();
        SimpleDateFormat formatter;
        switch (dayType){
            case 1:
                int days = (int) ((end.getTime() - start.getTime()) / (1000*3600*24));
                formatter = new SimpleDateFormat("MMdd");
                for (int j=0;j<=days;j++){
                    start.setDate(start.getDate()+j);
                    String dateString = formatter.format(start);
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
                }
                break;
            case 2:
                int l = Integer.parseInt((end.getTime() - start.getTime()) / (1000 * 3600 * 24 * 7) + "");
                long k =(end.getTime() - start.getTime()) % (1000 * 3600 * 24 * 7);
                SimpleDateFormat dateFm = new SimpleDateFormat("EEEE");
                String endFormat = dateFm.format(end);
                Integer endWeek = get(endFormat);
                String startFormat = dateFm.format(start);
                Integer startWeek = get(startFormat);
                if(endWeek<=startWeek || k!=0){
                    l = l + 1 ;
                }
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
                }
                break;
            case 3:
                Calendar startCalendar = Calendar.getInstance();
                startCalendar.setTime(start);
                Calendar endCalendar = Calendar.getInstance();
                endCalendar.setTime(end);
                int year = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
                int month = year * 12 + endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);
                formatter = new SimpleDateFormat("yyyyMM");
                for (int i=0 ;i<=month;i++){
                    start.setMonth(start.getMonth()+i);
                    String dateString = formatter.format(start);
                    WarningOrDealVo warningOrDealVo=new WarningOrDealVo();
                    warningOrDealVo.setDayType(dateString);
                    warningOrDealVo.setCount(0);
                    warningOrDealVo.setDeal(0);
                    warningOrDealVos.put(dateString+"0",warningOrDealVo);warningOrDealVo=new WarningOrDealVo();
                    warningOrDealVo.setDayType(dateString);
                    warningOrDealVo.setCount(0);
                    warningOrDealVo.setDeal(1);
                    warningOrDealVos.put(dateString+"1",warningOrDealVo);
                }
                break;
            case 4:
                int years = end.getYear() - start.getYear();
                for (int i=0;i<=years;i++){
                    WarningOrDealVo warningOrDealVo=new WarningOrDealVo();
                    warningOrDealVo.setDayType(start.getYear() + 1 + "");
                    warningOrDealVo.setCount(0);
                    warningOrDealVo.setDeal(0);
                    warningOrDealVos.put(start.getYear() + 1 +"0",warningOrDealVo);
                    warningOrDealVo=new WarningOrDealVo();
                    warningOrDealVo.setDayType(start.getYear() + 1 + "");
                    warningOrDealVo.setCount(0);
                    warningOrDealVo.setDeal(1);
                    warningOrDealVos.put(start.getYear() + 1 +"1",warningOrDealVo);
                }
                break;
        }
        return warningOrDealVos;
    }

    public static Integer get(String format){
        int k = 0;
        switch (format){
            case "星期一": k = 1; break;
            case "星期二": k = 2; break;
            case "星期三": k = 3; break;
            case "星期四": k = 4; break;
            case "星期五": k = 5; break;
            case "星期六": k = 6; break;
            case "星期日": k = 7; break;
        }
        return k;
    }
}
