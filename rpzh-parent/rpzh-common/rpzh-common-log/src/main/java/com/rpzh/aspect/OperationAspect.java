package com.rpzh.aspect;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ReflectUtil;
import com.alibaba.fastjson.JSON;
import com.rpzh.annotation.OperationLog;
import com.rpzh.entity.IpAddress;
import com.rpzh.entity.LogOperation;
import com.rpzh.entity.Result;
import com.rpzh.enums.BusinessStatus;
import com.rpzh.enums.BusinessType;
import com.rpzh.service.AsyncLogService;
import com.rpzh.util.IpUtil;
import com.rpzh.util.ServletUtils;
import com.rpzh.util.SpringContextHolder;
import com.rpzh.util.StringUtils;
import io.swagger.models.HttpMethod;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.util.Collection;
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
@Component
public class OperationAspect {

    @Resource
    private AsyncLogService asyncLogService;

    private static final String LOCALHOST = "127.0.0.1";

    private final String accountId = "ACCOUNT_ID_";


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
     * @param point 链接点
     */
    @Around(value = "operationLogPointCut()")
    public Object doAroundReturning(ProceedingJoinPoint point) throws Throwable {
        return operation(point);
    }

    private Object operation(ProceedingJoinPoint point) throws Throwable {
        // 获得注解
        OperationLog controllerLog = getAnnotationLog(point);
        if (controllerLog == null) {
            return null;
        }
        // 修改前的数据
        Object oldData = new Object();
        //设置返回的数据
        Result result = null;
        // 获取操作类型
        BusinessType businessType = controllerLog.businessType();
        // 设置日志类
        LogOperation operationLog = new LogOperation();
        // mapper
        Class<?> mapper = controllerLog.mapper();
        // 获得id , 查询条件
        Object[] args = point.getArgs();
        Object arg = args[0];
        Object operationContent = ReflectUtil.invoke(arg, "findOperationContent");
        switch (businessType) {
            case INSERT:
                operationLog.setLogSysModule("生成");
                operationLog.setLogOperationRecord("添加" + operationContent);
                break;
            case UPDATE:
                operationLog.setLogSysModule("修改");
                operationLog.setLogOperationRecord("修改" + operationContent);

                Object getId = ReflectUtil.invoke(arg, "getId");
                Object bean = SpringContextHolder.getBean(mapper);
                // 获得修改前的数据
                oldData = ReflectUtil.invoke(bean,
                        "logFindById", getId.toString());
                //如果发生异常不继续执行
                try {
                    //执行方法
                    result = (Result) point.proceed(args);
                    //如果没有成功删除直接返回
                    if (result.getCode() == 0) {
                        return 0;
                    }
                    // 获得修改后的数据
                    Object newData = ReflectUtil.invoke(bean,
                            "logFindById", getId.toString());
                    // 获取旧的所有方法
                    Method[] oldMethods = ReflectUtil.getMethods(oldData.getClass());

                    JSONObject oldJson = new JSONObject();
                    JSONObject newJson = new JSONObject();
                    for (Method oldMethod : oldMethods) {
                        String methodName = oldMethod.getName();
                        if ("get".equals(methodName.substring(0, 3))) {
                            Object oldInvoke = ReflectUtil.invoke(oldData, methodName);
                            String oldd = null;
                            String neww = null;
                            if (oldInvoke != null) {
                                oldd = oldInvoke.toString();
                            }
                            Object nweInvoke = ReflectUtil.invoke(newData, methodName);
                            if (nweInvoke != null) {
                                neww = nweInvoke.toString();
                            }
                            // 如果不等于就代表更改过,存起来
                            if (!Objects.equals(oldd, neww)) {
                                String field = methodName.substring(3);
                                oldJson.put(field, oldd);
                                newJson.put(field, neww);
                            }
                        }
                    }
                    operationLog.setLogOldFieldJson(oldJson.toString());
                    operationLog.setLogNewFieldJson(newJson.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    //异常登录失败
                    operationLog.setLogStatus(BusinessStatus.FAIL.ordinal() == 0);
                    operationLog.setLogErrorInfo(StringUtils.substring(e.getMessage(), 0, 2000));
                    log.error(" ErrorAndException:   " + e.getMessage());
                }
                break;
            case DELETE:
                operationLog.setLogSysModule("删除");
                operationLog.setLogOperationRecord("删除" + operationContent);
                break;
        }

        //获得ip地址
        String ipAddr = IpUtil.getIpAddress(Objects.requireNonNull(ServletUtils.getRequest()));
        operationLog.setLogIp(ipAddr);
        if (LOCALHOST.equals(ipAddr)) {
            operationLog.setLogAddress("本地IP");
        } else {
            //设置ip地理位置
            operationLog.setLogAddress(IpAddress.guideGetCityCode(ipAddr));
        }

        //操作状态
        operationLog.setLogStatus(BusinessStatus.SUCCESS.ordinal() == 1);
        // 设置方法名称
        String className = point.getTarget().getClass().getName();
        String methodName = point.getSignature().getName();
        operationLog.setLogClassMethodName(className + "." + methodName + "()");
        //设置请求参数
        setRequestValue(point, operationLog);
        //设置系统模块
        operationLog.setLogType(controllerLog.title());
        //设置返回信息
        operationLog.setLogReturnInfo(com.alibaba.fastjson.JSONObject.toJSONString(point.proceed()));
        operationLog.setLogRequestUrl(Objects.requireNonNull(ServletUtils.getRequest()).getServletPath());
        //设置日志记录时间
        operationLog.setLogTime(DateUtil.date());
        asyncLogService.saveOperationLog(operationLog);
        return result;
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
