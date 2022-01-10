package com.rpzh.entity;

import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * @author wbl
 * @version 0.1.0
 * @create 2021-06-09 19:33
 * @description ip地理位置获取
 */
public class IpAddress {
    /**
     * 读取
     * @param rd
     * @return
     * @throws IOException
     */
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    /**
     * 创建链接
     * @param url
     * @throws IOException
     * @throws JSONException
     */
    private static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        }
    }

    /**
     * 高德获取城市信息
     * @return
     * @throws JSONException
     * @throws IOException
     */
    public static String guideGetCityCode(String ip) throws JSONException, IOException{
        //高德key
        String key="1abdb3d4917dce8e82bde3a342c80b6d";
        JSONObject json = readJsonFromUrl("https://restapi.amap.com/v3/ip?parameters?ip="+ip+"&key="+key);
        return json.get("province").toString()+ json.get("city").toString();
    }
}
