package com.rpzh.feign;

import com.rpzh.entity.Result;
import com.rpzh.feign.impl.UserFeignImpl;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * @Author liubc
 * @create 2021/4/1 20:04
 */
@FeignClient(name = "rpzh-system",fallback = UserFeignImpl.class)
public interface UserFeign {

    /**
     * 根据id查询用户名
     * @param
     * @return
     */
    @PostMapping("/username/getUsernameById")
    public List<String> findUsernameById(@RequestParam("ids")List<Integer> ids);

    /**
     * 根据id查询手机号
     * @return
     */
    @PostMapping("/username/findPhoneNumberById")
    public Result findPhoneNumberById(@RequestParam("id") String id);

    @PostMapping("/repair/findRepairInfo")
    public Map<String, String> findRepairInfo(@RequestParam("id") String id);
}
