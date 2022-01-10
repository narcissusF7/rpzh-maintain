package com.rpzh.service.impl;

import com.rpzh.dao.LoginDao;
import com.rpzh.entity.Result;
import com.rpzh.feign.UserFeign;
import com.rpzh.service.LoginService;
import com.rpzh.service.OauthService;
import com.rpzh.util.AuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

/**
 * @Author liubc
 * @create 2021/4/15 9:03
 */
@Service
public class OauthServiceImpl implements OauthService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @Autowired
    private UserFeign userFeign;


    /**
     * 授权认证的方法
     *
     * @param username     用户名
     * @param password     密码
     * @param cilentId     客户端id
     * @param clientSecret 客户端秘钥
     */
    @Override
    public AuthToken login(String username, String password, String cilentId, String clientSecret) {
        //查询错误次数
        /*int count = loginService.checkErrorTimes(username);
        if (count > 6){
            return new Result(false, StatusCode.ERROR, "用户已经被冻结,请联系管理员");
        }*/
        //获取服务调用的url路径
        ServiceInstance choose = loadBalancerClient.choose("rpzh-oauth");
        //通过服务的名字获取请求路径
        String url = choose.getUri().toString() + "/oauth/token";
        //String url = "http://localhost:18883/oauth/token";

        //定义headers
        MultiValueMap<String,String> headers = new LinkedMultiValueMap<>();
        headers.set("Authorization",getHeader(cilentId,clientSecret));

        //定义body
        MultiValueMap<String,String> body = new LinkedMultiValueMap<>();
        body.set("grant_type","password");
        body.set("username",username);
        body.set("password",password);

        ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);
        Map<String,String> result = exchange.getBody();

        System.out.println(exchange);
        System.out.println(result);
        String access_token = result.get("access_token");
        String refresh_token = result.get("refresh_token");
        String jti = result.get("jti");

        AuthToken authToken = new AuthToken();
        authToken.setAccessToken(access_token);
        authToken.setRefreshToken(refresh_token);
        authToken.setJti(jti);

        return authToken;
    }

    /**
     * 修改密码
     *
     * @param username
     * @param newpassWord
     * @return
     */
    @Override
    public Result editPassWord(String username, String newpassWord, String notarizePassword) {
        Result result = userFeign.editPassWord(username, newpassWord,notarizePassword);
        return result;
    }


    /**
     * 生成请求头中的参数
     * @param clientId
     * @param clientSecret
     * @return
     */
    private String getHeader(String clientId,String clientSecret){
        String head = clientId + ":" +clientSecret;
        //进行base64加密
        byte[] encode = Base64.getEncoder().encode(head.getBytes());

        return "Basic " + new String(encode);
    }
}