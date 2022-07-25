package com.rpzh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Author liubc
 * @create 2022-07-25 18:45
 * @Description: good good study,day day up
 */
@SpringBootApplication
@EnableScheduling
@EnableDiscoveryClient //服务发现客户端
public class RpzhJobApplication {
    public static void main(String[] args) {
        SpringApplication.run(RpzhJobApplication.class, args);
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
                        "养护定时任务微服务启动成功");
    }
}