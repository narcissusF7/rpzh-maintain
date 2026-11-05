package com.rpzh.feign;

import com.rpzh.entity.Result;
import com.rpzh.feign.impl.UserPhoneFeignFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * @Author liubc
 * @create 2021/4/1 20:04
 * @Description 根据用户id查询手机号（原 UserFeign，因与 rpzh-system-api 的通用 UserFeign 全限定名冲突，重命名为 UserPhoneFeign）
 */
@FeignClient(name = "rpzh-system",fallback = UserPhoneFeignFallback.class)
public interface UserPhoneFeign {

    /**
     * 根据id查询手机号
     * @return
     */
    @PostMapping("/username/findPhoneNumberById")
    Result findPhoneNumberById(@RequestParam("id") String id);
}
