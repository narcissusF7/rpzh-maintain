package com.rpzh.Task;

import com.rpzh.feign.RfidFeign;
import com.rpzh.netty.RfIdNettyServerFilter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * &#064;Author:  zyq
 * &#064;Date:  2022/09/21/20:37
 * @Description: 功能
 */
@Component
public class RFIDNettyTask {

    @Resource
    private RfidFeign rfidFeign;
    @Scheduled(cron="0 0/30 * * * ?")
    void updateRfidStatus() {
        List<String> ips = rfidFeign.findIps();
        if(!ips.isEmpty()){
            //,0:禁用,1:启用
            for (String ip : ips) {
                if(RfIdNettyServerFilter.usermap.get("/" + ip) != null){
                    rfidFeign.updateStatus(ip,"1");
                }else{
                    rfidFeign.updateStatus(ip,"0");
                }
            }
        }
    }
}
