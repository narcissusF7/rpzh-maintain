package com.rpzh.netty;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@ChannelHandler.Sharable
@Slf4j
public class RfIdNettyServerHandler extends ChannelInboundHandlerAdapter {
    public static Map<String, Integer> map = new LinkedHashMap<>();
    //  该部分功能需要设备采集后确定数据解析规则后完善
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // TODO msg为接收到的客户端传递的数据
        String str = msg.toString();
        JSONObject jsonObject = JSONObject.parseObject(str);

        log.info("原始数据" + str);


        log.info("长度" + str.length());
        Map map=new HashMap();
        if (jsonObject!=null&&!jsonObject.equals("")){
            map.put("status",400);

        }else{
            map.put("status",500);

        }
        String rmsg = JSONObject.toJSONString(map,
                SerializerFeature.DisableCircularReferenceDetect,
                SerializerFeature.WriteNullStringAsEmpty);

        log.info(rmsg);
        ByteBuf message = Unpooled.copiedBuffer(rmsg.getBytes());//处理返回的信息
        //ctx.write(in2);//返回信息
        ctx.writeAndFlush(message);//返回信息
        //刷新缓存区
        ctx.flush();
    }
}
