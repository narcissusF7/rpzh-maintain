package com.rpzh.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

public class WeatherUtils {

    private static String weatherUrl = "https://devapi.qweather.com/v7/weather/now?location=101230201&key=8fdc54a3ac93474cbe6ae1efa42493fd";

    /**
     * 通过城市名称获取该城市的天气信息
     */

    public static String GetWeatherData() {

        StringBuilder sb = new StringBuilder();

        BufferedReader reader = null;

        try {

            URL url = new URL(weatherUrl);

            URLConnection conn = url.openConnection();

            InputStream is = conn.getInputStream();

            GZIPInputStream gzin = new GZIPInputStream(is);

            // 设置读取流的编码格式，自定义编码

            InputStreamReader isr = new InputStreamReader(gzin, StandardCharsets.UTF_8);

            reader = new BufferedReader(isr);
            String line = null;

            while ((line = reader.readLine()) != null) {

                sb.append(line).append(" ");

            }
            reader.close();

        } catch (Exception e) {

            e.printStackTrace();

        }
        return sb.toString();
    }
}
