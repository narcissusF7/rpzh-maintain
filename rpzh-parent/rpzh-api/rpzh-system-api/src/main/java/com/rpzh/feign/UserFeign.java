package com.rpzh.feign;

import com.rpzh.entity.Result;
import com.rpzh.pojo.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author liubc
 * @create 2022/1/7 9:19
 * @Description: good good study,day day up
 */
@FeignClient(name = "rpzh-system",path = "/username")
public interface UserFeign {

    /**
     * 根据id查询user数据
     * @param id
     * @return
     */
    @RequestMapping(value = {"/{id}"})
    public Result<User> findById(@PathVariable(value = "id") String id);


    /**
     * 用户修改密码
     * @param username
     * @param newpassWord
     * @return
     */
    @GetMapping("/editPassWord")
    public Result editPassWord(@RequestParam("username") String username, @RequestParam("newpassWord")  String newpassWord, @RequestParam("notarizePassword") String notarizePassword);


    /**
     * 获取用户的角色权限
     * @return
     */
    @GetMapping("/getUserRole")
    public String getUserRole(@RequestParam("username") String username);


    /**
     * seata分布式事务测试
     * @return
     */
    @GetMapping("/seataTest")
    public Result seataTest();
}
