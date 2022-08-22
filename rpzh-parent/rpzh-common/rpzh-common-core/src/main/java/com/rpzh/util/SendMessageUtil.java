package com.rpzh.util;

import com.alibaba.fastjson.JSON;
import com.rpzh.vo.MessageVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

/**
 * 发送短信工具类
 *
 * @Author: liubc
 * @Date: 2022/06/14/9:53
 * @Description: 宁静致远
 */
@Slf4j
public class SendMessageUtil {

    /**
     * 公钥
     * MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAoNAz4Ounfjkk87ozcbw85cMXWURuf/21WKe6VqHrXdcIrlqLZkuXz5IDFuCjun9hX+zn0VX4wgRkzn8BHpmRdVIl5PZKTwgwNcWFQ7os7p3S882eSq6VBBO3Gu/RuxTOqNlZ/apfLSuHAiFRFyhKsnazx6Br/W4mHSXmyTmSI6EkKAecewlNZI6b+ruKZwcbQZeFbLvlkGrbytM78nNmN3LxLLV2YIu2lQl0ID8wN3wfmV4rikr1tRevFj5hERVsTkpWPMM0ZxlX/dReU9cPdItmyx6Ccf5nPReNYKegaE/eJIfTacs879i752Xc2mL5w79uQfvRWytSyCbNgMz16wIDAQAB
     *
     * RSA加密用户名loginName和密码loginPwd
     * */
    /**
     * 短信发送工具类
     *
     * @param servNum 必填，手机号码(多个号码用”,”分隔)，最多1000个号码。
     * @param content 必填，短信内容, 最大460个字。
     * @param apnHd   可选：短信显示号段，h1=通知106509712041041、h2=告警 106509712041042；默认h1。
     * @return
     */
    public static MessageVo sendSms(String servNum, String content, String apnHd) {
        MessageVo messageVo = new MessageVo();
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        CloseableHttpResponse response = null;

        try {

            log.info("发送短信: 号码：{}；短信内容：{}。", servNum, content);

            String publicKeyStr = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAoNAz4Ounfjkk87ozcbw85cMXWURuf/21WKe6VqHrXdcIrlqLZkuXz5IDFuCjun9hX+zn0VX4wgRkzn8BHpmRdVIl5PZKTwgwNcWFQ7os7p3S882eSq6VBBO3Gu/RuxTOqNlZ/apfLSuHAiFRFyhKsnazx6Br/W4mHSXmyTmSI6EkKAecewlNZI6b+ruKZwcbQZeFbLvlkGrbytM78nNmN3LxLLV2YIu2lQl0ID8wN3wfmV4rikr1tRevFj5hERVsTkpWPMM0ZxlX/dReU9cPdItmyx6Ccf5nPReNYKegaE/eJIfTacs879i752Xc2mL5w79uQfvRWytSyCbNgMz16wIDAQAB";

            String loginName = "pyyh";

            String loginPwd = "Pi2A$0nY2@y2H";
            //rsa加密
            PublicKey publicKey = RSAUtil.string2PublicKey(publicKeyStr);
            byte[] encryptUser = RSAUtil.publicEncrypt(loginName.getBytes(), publicKey);
            byte[] encryptPwd = RSAUtil.publicEncrypt(loginPwd.getBytes(), publicKey);
            String secUserStr = RSAUtil.byte2Base64(encryptUser);
            String secPwdStr = RSAUtil.byte2Base64(encryptPwd);

            StringBuffer params = new StringBuffer();
            List<NameValuePair> list = new ArrayList<NameValuePair>();
            list.add(new BasicNameValuePair("loginName", secUserStr));
            list.add(new BasicNameValuePair("loginPwd", secPwdStr));
            list.add(new BasicNameValuePair("servNum", servNum));
            list.add(new BasicNameValuePair("content", content));
            list.add(new BasicNameValuePair("apnHd", StringUtils.isEmpty(apnHd) ? "" : apnHd));

            HttpPost httpPost = new HttpPost("http://10.154.196.221:8082/sms-restful/smsRest/sendSms/");
            httpPost.setEntity(new UrlEncodedFormEntity(list, "utf-8"));
            response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();
            String responseEntityStr = EntityUtils.toString(responseEntity);
            log.info("返回内容是:" + responseEntityStr);
            if (responseEntity != null) {
                messageVo = JSON.parseObject(responseEntityStr, MessageVo.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (httpClient != null) {
                    httpClient.close();
                }
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return messageVo;

    }
}
