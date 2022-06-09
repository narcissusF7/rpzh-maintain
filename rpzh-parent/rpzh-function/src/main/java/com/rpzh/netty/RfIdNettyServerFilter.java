package com.rpzh.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class RfIdNettyServerFilter extends ChannelInitializer<SocketChannel> {
    @Resource
    private RfIdNettyServerHandler serverHandler;

    public  static ConcurrentHashMap<String, Channel> usermap=new ConcurrentHashMap<>();

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline ph = socketChannel.pipeline();

        usermap.put(socketChannel.remoteAddress().getAddress().toString(),socketChannel);
        log.info("设备连接");
        log.info(socketChannel.remoteAddress().getAddress().toString());
        // 以("\n")为结尾分割的 解码器
        //ph.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
        // 解码和编码，应和客户端一致
        ph.addLast("decoder", new StringDecoder());
        //ph.addLast("decoder", new MyDecoder());//一包多条数据
        ph.addLast("encoder", new StringEncoder());
        //1）readerIdleTime：为读超时时间（即测试端一定时间内未接受到被测试端消息）;
        //2）writerIdleTime：为写超时时间（即测试端一定时间内未向被测试端发送消息）
        //3）allIdleTime：所有类型的超时时间;
        ph.addLast(new IdleStateHandler(60*10,0,0));
        ph.addLast("handler", serverHandler);// 服务端业务逻辑

    }
}
