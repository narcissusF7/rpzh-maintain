package com.rpzh.aspect;

import com.alibaba.fastjson.JSON;
import com.rpzh.annotation.OperationUserLog;
import com.rpzh.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * 用户操作同步切面
 *
 * @Author liubc
 * @create 2022/1/17 17:03
 * @Description: good good study,day day up
 */
@Aspect
@Slf4j
@Component
public class OperationUserAspect {


    /**
     * 配置切入点
     */
    @Pointcut("@annotation(com.rpzh.annotation.OperationUserLog)")
    public void operationLogPointCut() {
    }


    /**
     * 成功后执行
     * @param joinPoint
     * @param jsonResult
     * @throws Exception
     */
    @AfterReturning(pointcut = "operationLogPointCut()", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Object jsonResult) throws Exception {
        /*String params = argsArrayToString(joinPoint.getArgs());
        System.out.println("切面数据"+params);*/
        operation(joinPoint, null, null);
    }

    /**
     * 拦截异常操作
     * @param joinPoint 切点
     * @param e         异常
     */
    @AfterThrowing(value = "operationLogPointCut()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Exception e) throws Exception {
        operation(joinPoint, e, null);
    }

    private void operation(final JoinPoint joinPoint, final Exception e, Object jsonResult) throws Exception {
        // 获得注解
        OperationUserLog controllerLog = getAnnotationLog(joinPoint);
        if (controllerLog == null) {
            return;
        }
        int type = controllerLog.businessUserType().ordinal();
        System.out.println("类型:"+type);
        String params = argsArrayToString(joinPoint.getArgs());
        System.out.println("切面数据:"+params);
        if (e != null) {
            log.error("系统用户同步失败:" + e.getMessage());
        } else {
            log.info("系统用户同步成功:");
        }
    }

    /**
     * 是否存在注解，如果存在就获取
     */
    private OperationUserLog getAnnotationLog(JoinPoint joinPoint) throws Exception {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        if (method != null) {
            return method.getAnnotation(OperationUserLog.class);
        }
        return null;
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
            for (Iterator iter = collection.iterator(); iter.hasNext(); ) {
                return iter.next() instanceof MultipartFile;
            }
        } else if (Map.class.isAssignableFrom(clazz)) {
            Map map = (Map) o;
            for (Iterator iter = map.entrySet().iterator(); iter.hasNext(); ) {
                Map.Entry entry = (Map.Entry) iter.next();
                return entry.getValue() instanceof MultipartFile;
            }
        }
        return o instanceof MultipartFile || o instanceof HttpServletRequest || o instanceof HttpServletResponse
                || o instanceof BindingResult;
    }

}