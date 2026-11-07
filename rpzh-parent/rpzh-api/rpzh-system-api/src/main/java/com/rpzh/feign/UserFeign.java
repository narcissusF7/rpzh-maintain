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
     * 中台账号解析为本地账号（中台化改造）。
     * 已绑定则返回本地账号；未绑定则按 defaultRoleKey 自动开户并绑定后返回本地账号。
     *
     * @param platformUid      中台用户唯一标识（sub/openid）
     * @param platformUsername 中台登录账号/昵称
     * @param defaultRoleKey   自动开户时赋予的本地默认角色 role_key
     * @return 本地账号 username
     */
    @GetMapping("/resolvePlatformUser")
    public Result<String> resolvePlatformUser(@RequestParam("platformUid") String platformUid,
                                             @RequestParam("platformUsername") String platformUsername,
                                             @RequestParam(value = "defaultRoleKey", defaultValue = "common") String defaultRoleKey);


    /**
     * seata分布式事务测试
     * @return
     */
    @GetMapping("/seataTest")
    public Result seataTest();
}
