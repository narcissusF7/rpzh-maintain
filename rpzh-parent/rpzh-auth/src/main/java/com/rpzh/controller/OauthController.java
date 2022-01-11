package com.rpzh.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.service.LoginService;
import com.rpzh.service.OauthService;
import com.rpzh.util.AuthToken;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author liubc
 * @create 2021/4/15 9:43
 * 密码模式下才会加载配置文件客户端公钥和客户端私钥
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping(value = "/user")
public class OauthController {

    private static final Logger LOG = LoggerFactory.getLogger(OauthController.class);

    @Autowired
    private OauthService oauthService;

    @Autowired
    private LoginService loginService;

    @Value("${oauth.clientId}")
    private String clientId;

    @Value("${oauth.clientSecret}")
    private String clientSecret;

    @Value("${oauth.cookieDomain}")
    private String cookieDomain;

    @Value("${oauth.cookieMaxAge}")
    private Integer cookieMaxAge;

    @Resource
    private HttpServletResponse response;

    /**
     *用户登录
     * @param username 用户名
     * @param password  密码
     * @param uuidCode   前端生成的随机数
     * @param verificationCode  图片验证码
     * @return
     */
    @RequestMapping("/login")
    public Result login(String username, String password, String uuidCode, String verificationCode) {
        if (ToolUtil.isEmpty(uuidCode)){
            throw new RuntimeException("uuidCode不能为空");
        }
        AuthToken authToken = null;
        Map<String, String> map = new HashMap<>();
        map.put("username",username);
        //获取redis中验证码
        String redisCode = loginService.findRedisCode(uuidCode);
       /* if (ToolUtil.isEmpty(verificationCode)){
            return new Result(false, StatusCode.ERROR, "验证码不能为空",map);
        }*/
        //生产环境放开,仅仅测试使用
        /*if (!(verificationCode.equals(redisCode))){
            return new Result(false, StatusCode.ERROR, "验证码错误请重新输入",map);
        }*/
        Integer checkErrorTimes = loginService.checkErrorTimes(username);
        if (ToolUtil.isEmpty(checkErrorTimes)){
            return new Result(false, StatusCode.ERROR, "用户名或密码错误",map);
        }
        if (checkErrorTimes > 6){
            return new Result(false, StatusCode.ERROR, "用户已经被冻结,请联系管理员",map);
        }
        //查询当前用户密码更新时间
        String updateTime = loginService.checkUpdateTime(username);
        Date date1 = DateUtil.parse(updateTime);
        long betweenDay = DateUtil.between(date1, new Date(), DateUnit.DAY);
        if (betweenDay > 90){
            return new Result(false, StatusCode.ERROR, "你的密码已经过期,请联系管理员",map);
        }
        try {
            authToken = oauthService.login(username, password, clientId, clientSecret);
            //登录成功后错误次数赋值为0
            int clearError = loginService.clearError(username);
        } catch (Exception e) {
            e.printStackTrace();
            //连续错误6次禁止登录,每次错误加1
            int count = loginService.addErrorTimes(username);
            return new Result(false, StatusCode.ERROR, "用户名或密码错误",map);
        }
        String accessToken = "bearer " + authToken.getAccessToken();
        map.put("jwt", accessToken);
        //将token存入到cookie中
        //CookieUtil.addCookie(response,cookieDomain,"/","Authorization",accessToken,cookieMaxAge,false);
        if (betweenDay > 60){
            return new Result(true, StatusCode.OK, "登录成功,你的密码即将过期,请及时修改", map);
        }
        return new Result(true, StatusCode.OK, "登录成功", map);

    }


    /**
     * 登录时生成验证码
     * @param uuidCode
     * @param req
     * @param res
     */
    @RequestMapping("/verification")
    public void showValidateCodePic(String uuidCode, HttpServletRequest req, HttpServletResponse res){
        if (ToolUtil.isEmpty(uuidCode)){
            throw new RuntimeException("uuidCode不能为空");
        }
        res.setDateHeader("Expires",0);
        res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        res.addHeader("Cache-Control", "post-check=0, pre-check=0");
        // Set standard HTTP/1.0 no-cache header.
        res.setHeader("Pragma", "no-cache");
        res.setContentType("image/jpeg");
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(299, 97);
        String code = lineCaptcha.getCode();
        LOG.debug("图片验证码uuidCode={},code={}",uuidCode,code);
        loginService.saveCode(uuidCode,code);
        try {
            lineCaptcha.write(res.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 普通用户修改密码
     * @param username
     * @param password
     * @return
     */
    @GetMapping("/changePassWord")
    public Result editPassWord(String username, String password, String newpassWord, String notarizePassword){
        AuthToken authToken = null;
        try {
            authToken = oauthService.login(username, password, clientId, clientSecret);
        } catch (Exception e) {
            return new Result(false, StatusCode.ERROR, "密码错误");
        }
        Result result = oauthService.editPassWord(username,newpassWord,notarizePassword);
        return result;
    }
}