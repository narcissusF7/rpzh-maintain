package com.rpzh;

import com.github.pagehelper.dialect.helper.MySqlDialect;
import com.github.pagehelper.page.PageAutoDialect;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Author liubc
 * @create 2022/1/11 11:14
 * @Description: good good study,day day up
 */
@SpringBootApplication
@EnableDiscoveryClient //服务发现客户端
@EnableFeignClients(basePackages = "com.rpzh.feign")//开启feign包扫描
@EnableScheduling
public class RpzhFunctionApplication {
    public static void main(String[] args) {
        SpringApplication.run(RpzhFunctionApplication.class, args);

        PageAutoDialect.registerDialectAlias("clickhouse", MySqlDialect.class);
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
                        "养护微服务启动成功");
    }
}