package com.rpzh.netty;

import com.rpzh.dao.NodeDao;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;


@Component
@ChannelHandler.Sharable
@Slf4j
public class RfIdNettyServerHandler extends ChannelInboundHandlerAdapter {

    @Resource
    private NodeDao nodeDao;
    /**
     * 获取数据
     *
     * @param ctx 上下文
     * @param msg 获取的数据
     * @throws UnsupportedEncodingException
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        //msg为接收到的客户端传递的数据   个人这边直接传的json 数据
        String str = msg.toString();
        log.info("原始数据" + str);
        String DEVSN = "";
        int tagNum = 0;
        if(str.indexOf("435400") != -1){
            String strMeg = str.replaceFirst("435400","");
            if(strMeg.length() > 24){
                //获取RFID读卡器SN识别码
                DEVSN = strMeg.substring(8, 22);
                //获取当前数据中所含标签数量
                tagNum = Integer.parseInt(strMeg.substring(22,24),16);
                int site = 24;
                for (int i = 0; i < tagNum; i++) {
                    //获取当前标签数据的长度
                    int count = Integer.parseInt(strMeg.substring(site, site+2), 16);
                    //获取当前标签的设备id
                    String nodeId = strMeg.substring(site + 6, site + 14);
                    Integer nodeCount = findNodeId(nodeId);
                    if(nodeCount>0){
                        String rmsg = "53 57 00 03 FF 86 CE";
                        byte[] cmdInfor2 = hexStrToBinaryStr(rmsg);
                        ByteBuf message = Unpooled.copiedBuffer(cmdInfor2);
                        ctx.writeAndFlush(message);//返回信息
                        break;
                    }else{
                        site = site + count * 2 + 2;
                    }
                }
            }
        }
        //刷新缓存区
        ctx.flush();
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


    /*
    * 判断设备id是否存在
    * */
    public Integer findNodeId(String nodeId){
        Integer nodeType = nodeDao.findCountByNodeID(nodeId);
        if(nodeType==0){
            nodeType = nodeDao.findCountByCaseNo(nodeId);
        }
        return nodeType;
    }


    /**
     * 异常处理
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * 空闲次数
     */
    private int idle_count = 1;
    /**
     * 发送次数
     */
    private int count = 1;

    /**
     * 超时处理
     * 如果5秒没有接受客户端的心跳，就触发;
     * 如果超过两次，则直接关闭;
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object obj) throws Exception {
        if (obj instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) obj;
            if (IdleState.READER_IDLE.equals(event.state())) {//如果读通道处于空闲状态，说明没有接收到心跳命令
                System.out.println("已经30秒没有接收到客户端的信息了");
                if (idle_count > 2) {
                    System.out.println("关闭这个不活跃的channel");
                    ctx.channel().close();
                }
                idle_count++;
            }
        } else {
            super.userEventTriggered(ctx, obj);
        }
    }
}
