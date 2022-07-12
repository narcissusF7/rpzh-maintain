package com.rpzh.util;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;

import java.util.Date;

/**
 * 时间工具类
 * @Author liubc
 * @create 2022-07-12 10:49
 * @Description: good good study,day day up
 */
public class TimeUtils {

    /**
     * 开始时间和结束时间相差的分钟数
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return
     */
    public static long betweenMinute(String startTime,String endTime){
        Date date1 = DateUtil.parse(startTime);
        Date date2 = DateUtil.parse(endTime);
        //相差时间,分钟
        long minute = DateUtil.between(date1, date2, DateUnit.MINUTE);
        return minute;
    }


}