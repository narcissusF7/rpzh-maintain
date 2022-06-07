package com.rpzh.aspect;

import com.rpzh.entity.Result;
import org.apache.poi.ss.formula.functions.T;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 位置操作切面
 *
 * @Author liujj
 * @create 2022/5/30
 */
@Component
@Aspect
public class WareHouseAspect {

    @Autowired
    RedisTemplate redisTemplate;

    @AfterReturning(value = "execution(* com.rpzh.controller.WarehouseController.*(..))", returning = "result")
    public void deleteCache(JoinPoint pjp, Result<T> result){
        if (result.isFlag()){
            RequestAttributes ra = RequestContextHolder.getRequestAttributes();
            ServletRequestAttributes sra = (ServletRequestAttributes) ra;
            assert sra != null;
            HttpServletRequest request = sra.getRequest();
            String url = request.getRequestURL().toString();

            //获取执行的Controller层的方法
            String type="";
            if (url.contains("/warehouse/")){
                type =url.split("/warehouse/")[1];
            }
            if (type.startsWith("add")||type.startsWith("update")||type.startsWith("del")){
                redisTemplate.delete("maintain-locating");
                redisTemplate.delete(redisTemplate.keys("*principalName"));
            }
        }
    }
}
