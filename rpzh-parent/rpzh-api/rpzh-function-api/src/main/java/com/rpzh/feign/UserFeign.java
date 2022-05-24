package com.rpzh.feign;

import com.rpzh.entity.Result;
import com.rpzh.feign.impl.UserFeignImpl;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @Author liubc
 * @create 2021/4/1 20:04
 */
@FeignClient(name = "rpzh-system",path = "/username" ,fallback = UserFeignImpl.class)
public interface UserFeign {

    /**
     * 根据id查询用户名
     * @param
     * @return
     */
    @PostMapping("/getUsernameById")
    public Result<List<String>> findUsernameById(@RequestParam("ids")List<Integer> ids);
}
