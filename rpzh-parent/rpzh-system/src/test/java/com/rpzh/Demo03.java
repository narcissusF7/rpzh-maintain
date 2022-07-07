package com.rpzh;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.rpzh.entity.StatusCode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @Author liubc
 * @create 2022-06-09 15:47
 * @Description: good good study,day day up
 */
public class Demo03 {
    public static void main(String[] args) {
//        String startStr = "2021-02-26 00:00:00";
//        String endStr = "2021-03-09 00:00:00";
//        List<String> list = getBetweenDays(startStr, endStr);
//        System.out.println(list);
    }
    /**
     *  获取两个日期之间的所有日期 (年月日)
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public static List<String> getBetweenDays(String startTime, String endTime){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
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
                calendar.add(Calendar.DATE, 1);
                // 获取增加后的日期
                startDate=calendar.getTime();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return list;
    }

}