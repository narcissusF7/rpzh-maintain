package com.rpzh.netty;

import com.rpzh.dao.NodeDao;
import com.rpzh.dao.TaskManagementDao;
import com.rpzh.entity.ToolUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.util.StringUtil;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;


@Component
@ChannelHandler.Sharable
@Slf4j
public class RfIdNettyServerHandler extends ChannelInboundHandlerAdapter {

    @Resource
    private NodeDao nodeDao;
    @Resource
    private TaskManagementDao taskManagementDao;
    @Resource
    private RedisTemplate redisTemplate;
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
                    int length = Integer.parseInt(strMeg.substring(site, site+2), 16);
                    //获取当前标签的设备id
                    String nodeId = strMeg.substring(site + 6, site + length*2);
                    Integer countRepair=0,count1=0,countByCaseNo=0;
                    if(ToolUtil.isNotEmpty(redisTemplate.opsForValue().get("repairNode_"+nodeId)) &&
                            ToolUtil.isNotEmpty(redisTemplate.opsForValue().get("maintain_rfid_moveStack_"+nodeId)) &&
                            ToolUtil.isNotEmpty(redisTemplate.opsForValue().get("maintain_rfid_caseNo_"+nodeId))){
                        countRepair = Integer.parseInt(redisTemplate.opsForValue().get("repairNode_"+nodeId).toString());
                        count1 = Integer.parseInt(redisTemplate.opsForValue().get("maintain_rfid_moveStack_"+nodeId).toString());
                        countByCaseNo = Integer.parseInt(redisTemplate.opsForValue().get("maintain_rfid_caseNo_"+nodeId).toString());
                    }else {
                        //查询该设备是否运维中
                        countRepair = nodeDao.findCountByNodeID(nodeId);
                        //设备id是8位数
                        if(countRepair==0 && nodeId.length()==8){
                            //根据设备id查询位置路由
                            String siteRoute = nodeDao.findSiteRouteByNodeId(nodeId);
                            //根据位置路由判断是否有移库任务
                            if(StringUtil.isNotEmpty(siteRoute)) {
                                count1 = taskManagementDao.findCount(siteRoute);
                            }
                        }
                        if(countRepair == 0 && count1 == 0){
                            //判断nodeId和箱号是否绑定
                            countByCaseNo  = nodeDao.findCountByCaseNo(nodeId);
                        }
                        redisTemplate.opsForValue().set("maintain_rfid_repairNode_"+nodeId,countRepair);
                        redisTemplate.opsForValue().set("maintain_rfid_moveStack_"+nodeId,count1);
                        redisTemplate.opsForValue().set("maintain_rfid_caseNo_"+nodeId,countByCaseNo);
                    }
                    if(countRepair==0 && count1==0 && countByCaseNo > 0){
                        log.info("开启声光报警器---------");
                        String rmsg = "53 57 00 03 FF 85 CF";
                        byte[] cmdInfor2 = hexStrToBinaryStr(rmsg);
                        ByteBuf message = Unpooled.copiedBuffer(cmdInfor2);
                        ctx.writeAndFlush(message);//返回信息
                        break;
                    }
                    site = site + length * 2 + 2;
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
