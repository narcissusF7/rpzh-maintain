package com.rpzh.aspect;
import cn.hutool.core.date.DateUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.alibaba.fastjson.JSON;
import com.rpzh.annotation.LoginLogging;
import com.rpzh.entity.IpAddress;
import com.rpzh.entity.LogLogin;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.enums.BusinessStatus;
import com.rpzh.service.AsyncLogService;
import com.rpzh.util.IpUtil;
import com.rpzh.util.ServletUtils;
import com.rpzh.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

/**
 * @author wbl
 * @version 0.1.0
 * @create 2021-05-25 19:42
 * @description 登录日志切面
 */
@Aspect
@Slf4j
public class LoggingAspect {


    @Resource
    private AsyncLogService asyncLogService;

    private static final String LOCALHOST = "127.0.0.1";

    /**
     * @return void
     * @version 0.1.0
     * @description 配置登录日志切入点
     * @author wbl
     * @date 2021/5/25 19:47
     */
    @Pointcut("@annotation(com.rpzh.annotation.LoginLogging)")
    public void loginLogPointCut() {
    }


    /**
     * 登录成功操作
     *
     * @param joinPoint  切点
     * @param jsonResult 数据
     */
    @AfterReturning(pointcut = "loginLogPointCut()", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Object jsonResult) throws Exception {
        operation(joinPoint, null, jsonResult);
    }

    /**
     * 拦截异常操作
     *
     * @param joinPoint 切点
     * @param e         异常
     */
    @AfterThrowing(value = "loginLogPointCut()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Exception e) throws Exception {
        operation(joinPoint, e, null);
    }

    private void operation(final JoinPoint joinPoint, final Exception e, Object jsonResult) throws Exception {

        if (!Objects.isNull(e)) {
            e.printStackTrace();
        }

        // 获得注解
        LoginLogging controllerLog = getAnnotationLog(joinPoint);
        if (controllerLog == null) {
            return;
        }
        LogLogin loginLog = new LogLogin();
        //获得返回对象
        Result result = JSON.parseObject(JSON.toJSONString(jsonResult), Result.class);
        Map data = (Map) result.getData();
        loginLog.setUsername(data.get("username").toString());
        //设置时间
        loginLog.setLoginTime(DateUtil.date());
        //设置操作系统
        loginLog.setOs(System.getProperty("os.name"));
        //获得ip地址
        String ipAddr = IpUtil.getIpAddress(Objects.requireNonNull(ServletUtils.getRequest()));
        loginLog.setLoginIp(ipAddr);
        if (LOCALHOST.equals(ipAddr)) {
            loginLog.setLoginAddress("本地IP");
        } else {
            //设置ip地理位置
            loginLog.setLoginAddress(IpAddress.guideGetCityCode(ipAddr));
        }
        UserAgent ua = UserAgentUtil.parse(ServletUtils.getRequest().getHeader("User-Agent"));
        //获得浏览器信息
        loginLog.setBrowserType(ua.getBrowser().getName());
        //登录成功
        loginLog.setLoginStatus(BusinessStatus.SUCCESS.ordinal());
        // 设置方法名称
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        loginLog.setClassMethodName(className + "." + methodName + "()");
        //设置返回的信息
        loginLog.setReturnInfo(JSON.toJSONString(jsonResult));
        // 设置请求方式
        loginLog.setRequestMethod(ServletUtils.getRequest().getMethod());

        if (!result.isFlag() || result.getCode() == StatusCode.ERROR) {
            //异常登录失败
            loginLog.setLoginStatus(BusinessStatus.FAIL.ordinal());
            loginLog.setErrorInfo(StringUtils.substring(result.getMessage(), 0, 2000));
            log.error(" ErrorAndException:   " + result.getMessage());
        } else {
            log.info("  LoginInfo:    " + loginLog.toString());
        }
        asyncLogService.saveLoginLog(loginLog);
    }

    /**
     * 是否存在注解，如果存在就获取
     */
    private LoginLogging getAnnotationLog(JoinPoint joinPoint) throws Exception {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();

        if (method != null) {
            return method.getAnnotation(LoginLogging.class);
        }
        return null;
    }
}
