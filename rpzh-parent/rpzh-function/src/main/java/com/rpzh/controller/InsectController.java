package com.rpzh.controller;

import com.rpzh.netty.RfIdNettyServerFilter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
// 43 54 00 2C 00 45 01 C4 DD 93 8E 17 01 23 02 0F 01 02 E2 00 20 75 33 0F 01 03 E3 00 20 75 34 07
/**
 * @author zyq
 * @create 2022/8/1 10:30
 */
@Slf4j
@Controller
//@RequestMapping("/util")
public class InsectController {

    @RequestMapping(value = "/issue")
    @ResponseBody
    public String asd( String ip) {
        String rmsg = "53 57 00 03 FF 85 CF";
        byte[] cmdInfor2 = hexStrToBinaryStr(rmsg);
        ByteBuf message = Unpooled.copiedBuffer(cmdInfor2);
        //ctx.write(in2);//返回信息
        Channel channel = RfIdNettyServerFilter.usermap.get("/" + ip);
        channel.writeAndFlush(message);
        //刷新缓存区
        channel.flush();
        return "成功";
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