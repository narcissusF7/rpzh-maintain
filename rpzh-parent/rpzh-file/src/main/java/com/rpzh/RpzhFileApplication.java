package com.rpzh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 养护文件上传服务
 * @Author liubc
 * @create 2022-05-31 9:29
 * @Description: good good study,day day up
 */
@SpringBootApplication
@EnableDiscoveryClient //服务发现客户端
public class RpzhFileApplication {
    public static void main(String[] args) {
        SpringApplication.run(RpzhFileApplication.class, args);
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
                        "养护文件上传微服务启动成功");
    }
}