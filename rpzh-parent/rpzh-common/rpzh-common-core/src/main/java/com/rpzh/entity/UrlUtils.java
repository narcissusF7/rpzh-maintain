package com.rpzh.entity;

/**
 * 去除URL中指定的参数
 * @Author liubc成
 * @create 2021/3/29 15:33
 */
public class UrlUtils {
    public static String replateUrlParameter(String url,String... names){
        for (String name : names) {
            url = url.replaceAll("(&"+name+"=([0-9\\w]+))|("+name+"=([0-9\\w]+)&)|("+name+"=([0-9\\w]+))", "");
        }
        return url;
    }
}