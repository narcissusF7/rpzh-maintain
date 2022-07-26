package com.rpzh;

/**
 * @Author liubc
 * @create 2022-07-26 17:23
 * @Description: good good study,day day up
 */
public class Demo01 {
    public static void main(String[] args) {
        //定时任务表达式
        String cron = "0 0 "+23+" * * ?";
        System.out.println(cron);
    }
}