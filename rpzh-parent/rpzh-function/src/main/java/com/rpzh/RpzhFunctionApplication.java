package com.rpzh;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.dialect.helper.MySqlDialect;
import com.github.pagehelper.page.PageAutoDialect;
import com.rpzh.dao.WarehouseDao;
import com.rpzh.entity.IdWorker;

import com.rpzh.entity.ToolUtil;
import com.rpzh.util.RedisUtil;
import com.rpzh.util.SpringContextHolder;
import com.rpzh.vo.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        //获取 WarehouseDao  RedisUtil 的bean对象
        WarehouseDao warehouseDao = SpringContextHolder.getBean(WarehouseDao.class);
        RedisUtil redisUtil = SpringContextHolder.getBean(RedisUtil.class);

        //分别查出 所有位置的信息 并且将负责人存入缓存中
        List<WarehouseVo> allWarehouses = warehouseDao.findAllWarehouses(null);
        setPrincipalToCache(allWarehouses,1,redisUtil);
        List<BuildingVo> allBulid = warehouseDao.findAllBulid(null);
        setPrincipalToCache(allBulid,2,redisUtil);
        List<FloorVo> allFloor = warehouseDao.findAllFloor(null);
        setPrincipalToCache(allFloor,3,redisUtil);
        List<WareAreaVo> allWareArea = warehouseDao.findAllWareArea(null);
        setPrincipalToCache(allWareArea,4,redisUtil);
        List<StackVo> allStack = warehouseDao.findAllStack();
        setPrincipalToCache(allStack,5,redisUtil);
    }

    @Bean
    public IdWorker idWorker() {
        //0,机器的编号,集群的编号
        return new IdWorker(7, 12);
    }

    /**
     * 将负责人存入缓存中
     * @param list
     * @param level
     * @param redisUtil
     * @param <T>
     */
    public static  <T> void  setPrincipalToCache(List<T> list,int level,RedisUtil redisUtil){
        //遍历
        list.forEach(obj->{
            String string = JSONObject.toJSONString(obj);
            Map map = JSONObject.parseObject(string, Map.class);
            Object userName = map.get("principalName");
            //判断负责人是否为空
            if (ToolUtil.isNotEmpty(userName)){
                //缓存中获取负责人信息
                Object nickname = redisUtil.hget("USER_" + userName, "nickname");
                Object phonenumber = redisUtil.hget("USER_" + userName, "phonenumber");
                Map<String,Object> map1 = new HashMap<>();
                map1.put("nickname", nickname);
                map1.put("phonenumber", phonenumber);
                map1.put("username", userName);

                //存到缓存中
                redisUtil.hset("WAREHOUSE_PRINCIPAL",level+"_"+map.get("id"),JSONObject.toJSONString(map1));
            }
        });
    }
//    @Bean(name="redisTemplate")
////    public RedisTemplate redisTemplate(RedisConnectionFactory factory) {
////        RedisTemplate<String, String> template = new RedisTemplate<>();
////        RedisSerializer<String> redisSerializer = new StringRedisSerializer();
////        template.setConnectionFactory(factory);
////        //key序列化方式
////        template.setKeySerializer(redisSerializer);
////        //value序列化
////        template.setValueSerializer(redisSerializer);
////        return template;
////    }

}