package com.rpzh.feign;

import com.rpzh.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @Author liubc
 * @create 2022/1/7 9:19
 * @Description: good good study,day day up
 */
@FeignClient(name = "rpzh-system",path = "/rfid")
public interface RfidFeign {

    @RequestMapping("/findIps")
    public List<String> findIps();

    @PostMapping("/updateStatus")
    public Result updateStatus(@RequestParam("ip")String ip,@RequestParam("status") String status);

}
