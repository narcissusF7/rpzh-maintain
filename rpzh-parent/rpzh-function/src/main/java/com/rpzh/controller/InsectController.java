package com.rpzh.controller;

import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.netty.RfIdNettyServerFilter;
import com.rpzh.service.AppMessageService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * @author zyq
 * @create 2022/8/1 10:30
 */
@Slf4j
@Controller
public class InsectController {

    @Resource
    private AppMessageService appMessageService;
    @Resource
    private RedisTemplate redisTemplate;
    @RequestMapping(value = "/issue")
    @ResponseBody
    public Result asd(Integer id, String ip) {
        String rmsg = "53 57 00 03 FF 86 CE";
        byte[] cmdInfor2 = hexStrToBinaryStr(rmsg);
        ByteBuf message = Unpooled.copiedBuffer(cmdInfor2);
        Channel channel = RfIdNettyServerFilter.usermap.get("/" + ip);
        channel.writeAndFlush(message);
        //刷新缓存区
        channel.flush();
        appMessageService.updateStatus(id);
        redisTemplate.delete("maintain_app_message_"+ip);
        return new Result(true, StatusCode.OK, "声光报警器关闭成功");
    }

    public static byte[] hexStrToBinaryStr(String hexString) {
        hexString = hexString.replaceAll(" ", "");
        int len = hexString.length();
        int index = 0;
        byte[] bytes = new byte[len / 2];
        while (index < len) {
            String sub = hexString.substring(index, index + 2);
            bytes[index / 2] = (byte) Integer.parseInt(sub, 16);
            index += 2;
        }
        return bytes;
    }
}