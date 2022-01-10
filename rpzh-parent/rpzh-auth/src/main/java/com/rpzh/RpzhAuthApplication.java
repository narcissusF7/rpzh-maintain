package com.rpzh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * 认证授权中心
 * @Author liubc
 * @create 2022/1/6 15:34
 * @Description: good good study,day day up
 */
@SpringBootApplication
@EnableDiscoveryClient //服务发现客户端
@EnableFeignClients(basePackages = "com.rpzh.feign")//开启feign包扫描
public class RpzhAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(RpzhAuthApplication.class, args);
        System.out.println(
                "                                  _ooOoo_\n" +
                "                                 o8888888o\n" +
                "                                 88\" . \"88\n" +
                "                                 (| -_- |)\n" +
                "                                 O\\  =  /O\n" +
                "                              ____/`---'\\____\n" +
                "                            .'  \\\\|     |//  `.\n" +
                "                           /  \\\\|||  :  |||//  \\\n" +
                "                          /  _||||| -:- |||||-  \\\n" +
                "                          |   | \\\\\\  -  /// |   |\n" +
                "                          | \\_|  ''\\---/''  |   |\n" +
                "                          \\  .-\\__  `-`  ___/-. /\n" +
                "                        ___`. .'  /--.--\\  `. . __\n" +
                "                     .\"\" '<  `.___\\_<|>_/___.'  >'\"\".\n" +
                "                    | | :  `- \\`.;`\\ _ /`;.`/ - ` : | |\n" +
                "                    \\  \\ `-.   \\_ __\\ /__ _/   .-` /  /\n" +
                "               ======`-.____`-.___\\_____/___.-`____.-'======\n" +
                "                                  `=---='\n" +
                "               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
                "                         佛祖保佑 永不宕机 永无BUG \n" +
                "认证授权服务启动成功");
    }

    @Bean(name = "restTemplate")
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}