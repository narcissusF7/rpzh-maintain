package com.rpzh;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.sun.media.sound.SoftTuning;
import sun.rmi.runtime.Log;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author liubc
 * @create 2022-07-07 11:45
 * @Description: good good study,day day up
 */
public class Demo02 {
    public static void main(String[] args) {
        double sum = 19.9+17.6+17.8+13
                     +240+120+300+120
                     +20+30+30+8
                     +16+28+28+40
                     +52+31+44+38
                     +13+19+15+30
                     +20+16+30+30
                     +34+38+30+31
                     +31+20+30+16
                     +8+12+17+8
                     +6+15+10+15
                     +15+16+12+12
                     +11+17+14;
        System.out.println(sum);

    }
    /**
     * 获取两个日期之间的所有年
     *
     * @param startTime
     * @param endTime
     * @return：YYYY
     */
    public static List<String> getYearBetweenDate(String startTime, String endTime){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        // 声明保存日期集合
        List<String> list = new ArrayList<String>();
        try {
            // 转化成日期类型
            Date startDate = sdf.parse(startTime);
            Date endDate = sdf.parse(endTime);

            //用Calendar 进行日期比较判断
            Calendar calendar = Calendar.getInstance();
            while (startDate.getTime()<=endDate.getTime()){
                // 把日期添加到集合
                list.add(sdf.format(startDate));
                // 设置日期
                calendar.setTime(startDate);
                //把日期增加一天
                calendar.add(Calendar.YEAR, 1);
                // 获取增加后的日期
                startDate=calendar.getTime();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return list;
    }
}