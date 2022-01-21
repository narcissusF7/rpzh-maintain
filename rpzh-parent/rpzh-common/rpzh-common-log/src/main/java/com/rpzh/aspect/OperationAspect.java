package com.rpzh.aspect;

import com.alibaba.fastjson.JSON;
import com.rpzh.annotation.OperationLog;
import com.rpzh.entity.IpAddress;
import com.rpzh.entity.LogOperation;
import com.rpzh.enums.BusinessStatus;
import com.rpzh.util.IpUtil;
import com.rpzh.util.ServletUtils;
import io.swagger.models.HttpMethod;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.validation.BindingResult;
import com.rpzh.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * @author wbl
 * @version 0.1.0
 * @create 2021-05-31 14:54
 * @description 操作切面
 */
@Aspect
@Slf4j
public class OperationAspect {

   /* @Resource
    private AsyncLogService asyncLogService;*/


    private static final String LOCALHOST = "127.0.0.1";

    /**
     * @description 配置登录日志切入点
     * @author wbl
     * @date 2021/5/25 19:47
     */
    @Pointcut("@annotation(com.rpzh.annotation.OperationLog)")
    public void operationLogPointCut() {
    }


    /**
     * 操作成功操作
     *
     * @param joinPoint  切点
     * @param jsonResult 数据
     */
    @AfterReturning(pointcut = "operationLogPointCut()", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Object jsonResult) throws Exception {
        operation(joinPoint, null, jsonResult);
    }

    /**
     * 拦截异常操作
     *
     * @param joinPoint 切点
     * @param e         异常
     */
    @AfterThrowing(value = "operationLogPointCut()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Exception e) throws Exception {
        operation(joinPoint, e, null);
    }

    private void operation(final JoinPoint joinPoint, final Exception e, Object jsonResult) throws Exception {
        // 获得注解
        OperationLog controllerLog = getAnnotationLog(joinPoint);
        if (controllerLog == null) {
            return;
        }
        LogOperation operationLog = new LogOperation();
        //获得ip地址
        String ipAddr = IpUtil.getIpAddress(Objects.requireNonNull(ServletUtils.getRequest()));
        operationLog.setLogIp(ipAddr);
        if (LOCALHOST.equals(ipAddr)) {
            operationLog.setLogIp("本地IP");
        } else {
            //设置ip地理位置
            operationLog.setLogIp(IpAddress.guideGetCityCode(ipAddr));
        }
        //操作状态
        operationLog.setLogStatus(BusinessStatus.SUCCESS.ordinal() == 1);
        // 设置方法名称
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        operationLog.setLogClassMethodName(className + "." + methodName + "()");
        //设置请求参数
        setRequestValue(joinPoint, operationLog);
        //设置操作类型
        operationLog.setLogType(controllerLog.title());
        //设置返回信息
        operationLog.setLogReturnInfo(JSON.toJSONString(jsonResult));
        operationLog.setLogRequestUrl(Objects.requireNonNull(ServletUtils.getRequest()).getServletPath());
        if (e != null) {
            //异常登录失败
            operationLog.setLogStatus(BusinessStatus.FAIL.ordinal() == 0);
            operationLog.setLogErrorInfo(StringUtils.substring(e.getMessage(), 0, 2000));
            log.error(" ErrorAndException:   " + e.getMessage());
        } else {
            log.info("  OperationInfo:    " + operationLog);
        }
    }

    /**
     * 是否存在注解，如果存在就获取
     */
    private OperationLog getAnnotationLog(JoinPoint joinPoint) throws Exception {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();

        if (method != null) {
            return method.getAnnotation(OperationLog.class);
        }
        return null;
    }

    /**
     * 获取请求的参数，放到log中
     *
     * @param logOperation 操作日志
     * @throws Exception 异常
     */
    private void setRequestValue(JoinPoint joinPoint, LogOperation logOperation) {
        String requestMethod = logOperation.getLogRequestParams();
        if (HttpMethod.PUT.name().equals(requestMethod) || HttpMethod.POST.name().equals(requestMethod)) {
            String params = argsArrayToString(joinPoint.getArgs());
            logOperation.setLogRequestParams(StringUtils.substring(params, 0, 2000));
        }
    }

    /**
     * 参数拼装
     */
    private String argsArrayToString(Object[] paramsArray) {
        StringBuilder params = new StringBuilder();
        if (paramsArray != null && paramsArray.length > 0) {
            for (Object o : paramsArray) {
                if (StringUtils.isNotNull(o) && !isFilterObject(o)) {
                    try {
                        Object jsonObj = JSON.toJSON(o);
                        params.append(jsonObj.toString()).append(" ");
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        return params.toString().trim();
    }

    /**
     * 判断是否需要过滤的对象。
     *
     * @param o 对象信息。
     * @return 如果是需要过滤的对象，则返回true；否则返回false。
     */
    @SuppressWarnings("rawtypes")
    public boolean isFilterObject(final Object o) {
        Class<?> clazz = o.getClass();
        if (clazz.isArray()) {
            return clazz.getComponentType().isAssignableFrom(MultipartFile.class);
        } else if (Collection.class.isAssignableFrom(clazz)) {
            Collection collection = (Collection) o;
            for (Object value : collection) {
                return value instanceof MultipartFile;
            }
        } else if (Map.class.isAssignableFrom(clazz)) {
            Map map = (Map) o;
            for (Object value : map.entrySet()) {
                Map.Entry entry = (Map.Entry) value;
                return entry.getValue() instanceof MultipartFile;
            }
        }
        return o instanceof MultipartFile || o instanceof HttpServletRequest || o instanceof HttpServletResponse
                || o instanceof BindingResult;
    }
}
