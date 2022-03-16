package com.rpzh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关C启动类
 * @Author liubc
 * @create 2022/1/7 15:21
 * @Description: good good study,day day up
 */
@SpringBootApplication
@EnableDiscoveryClient //服务发现客户端
public class RpzhGatewayApplicationC {
    public static void main(String[] args) {
        SpringApplication.run(RpzhGatewayApplicationC.class, args);
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
                        "网关工程C启动成功");
    }
    /***
     * 根据进行IP限流
     * @return
     */
    @Bean(name="ipKeyResolver")
    public KeyResolver userKeyResolver() {
        return new KeyResolver() {
            @Override
            public Mono<String> resolve(ServerWebExchange exchange) {
                //获取远程客户端IP
                String hostName = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
                //System.out.println("用户请求的ip地址:"+hostName);
                return Mono.just(hostName);
            }
        };
    }
}