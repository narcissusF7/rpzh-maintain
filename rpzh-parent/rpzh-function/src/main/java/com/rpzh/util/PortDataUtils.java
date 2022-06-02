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

public class PortDataUtils {

    /**
     * 通过接口获取数据
     * @param str 地址
     * @return 数据
     */
    public static String GetPortData(String str) {

        StringBuilder sb = new StringBuilder();

        BufferedReader reader = null;

        try {

            URL url = new URL(str);

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
