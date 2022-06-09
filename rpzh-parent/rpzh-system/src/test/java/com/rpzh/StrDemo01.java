package com.rpzh;

/**
 * @Author liubc
 * @create 2022-06-09 15:47
 * @Description: good good study,day day up
 */
public class StrDemo01 {
    public static void main(String[] args) {
        String s = "[00:0],[01:1],[02:1],[03:1],[04:1],[05:2]";
        boolean contains = s.contains("00");
        String siteRouteC = s  + ",[05:" + 213 + "]";
        System.out.println(siteRouteC);
    }
}