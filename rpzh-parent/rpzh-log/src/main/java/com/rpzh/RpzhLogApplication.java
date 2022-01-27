package com.rpzh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 日志微服务
 *
 * @author wbl
 * @create 2022/1/6 17:23
 * @Description: good good study,day day up
 */
@SpringBootApplication
@EnableDiscoveryClient //服务发现客户端
@EnableFeignClients(basePackages = "com.rpzh.feign")   //开启feign
public class RpzhLogApplication {
    public static void main(String[] args) {
        SpringApplication.run(RpzhLogApplication.class, args);
    }
}