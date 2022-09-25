package com.rpzh.netty;

import com.rpzh.dao.AppMessageDao;
import com.rpzh.dao.NodeDao;
import com.rpzh.dao.TaskManagementDao;
import com.rpzh.dao.WarehouseDao;
import com.rpzh.dto.AppMessageDto;
import com.rpzh.entity.ToolUtil;
import com.rpzh.util.RedisUtil;
import com.rpzh.vo.*;
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
import javax.validation.constraints.Max;
import java.io.UnsupportedEncodingException;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
    @Resource
    private AppMessageDao appMessageDao;
    @Resource
    private WarehouseDao warehouseDao;
    @Resource
    private RedisUtil redisUtil;
    /**
     * 获取数据
     *
     * @param ctx 上下文
     * @param msg 获取的数据
     * @throws UnsupportedEncodingException
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        SocketAddress address = ctx.channel().remoteAddress();
        String ip = address.toString().split(":")[0].split("/")[1];
        //msg为接收到的客户端传递的数据   个人这边直接传的json 数据
        String str = msg.toString();
        log.info("原始数据" + str);
        String DEVSN = "";
        int tagNum = 0;
        if(str.indexOf("435400") != -1){
            String[] strMegs = str.split("435400");
            for (int j = 0; j < strMegs.length; j++) {
                String strMeg = strMegs[j];
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
                        Integer countRepair=0,count1=0,countByCaseNo=0,count=0;
                        if(ToolUtil.isNotEmpty(redisTemplate.opsForValue().get("maintain_rfid_repairNode_"+nodeId)) &&
                                ToolUtil.isNotEmpty(redisTemplate.opsForValue().get("maintain_rfid_moveStack_"+nodeId)) &&
                                ToolUtil.isNotEmpty(redisTemplate.opsForValue().get("maintain_rfid_caseNo_"+nodeId))){
                            countRepair = Integer.parseInt(redisTemplate.opsForValue().get("maintain_rfid_repairNode_"+nodeId).toString());
                            count1 = Integer.parseInt(redisTemplate.opsForValue().get("maintain_rfid_moveStack_"+nodeId).toString());
                            countByCaseNo = Integer.parseInt(redisTemplate.opsForValue().get("maintain_rfid_caseNo_"+nodeId).toString());
                            if(ToolUtil.isNotEmpty(redisTemplate.opsForValue().get("maintain_app_message_"+ip))){
                                count = Integer.parseInt(redisTemplate.opsForValue().get("maintain_app_message_"+ip).toString());
                            }
                        }else {
                            //查询该设备是否运维中
                            countRepair = nodeDao.findCountByNodeID(nodeId);
                            //设备id是8位数,且该设备不在运维中
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
                                if(ToolUtil.isEmpty(nodeDao.findCountByCaseNo(nodeId))){
                                    countByCaseNo = 0;
                                }else{
                                    countByCaseNo = 1;
                                }
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
                            if(count==0){
                                AppMessageDto appMessageDto = new AppMessageDto();
                                appMessageDto.setIp(ip);
                                appMessageDto.setType(1);
                                appMessageDto.setSendName("出库报警");
                                String siteRoute = nodeDao.findSiteRouteByNodeId(nodeId);
                                String username = null;
                                Integer areaId = null;
                                String siteName = "";
                                if(siteRoute.contains("05")){
                                    StackVo stackVo = warehouseDao.findStackById(Integer.parseInt(siteRoute.split("05:")[1].split("]")[0]));
                                    username = stackVo.getPrincipalName();
                                    siteName = stackVo.getSiteName();
                                    areaId = stackVo.getAreaId();
                                }else{
                                    areaId = Integer.parseInt(siteRoute.split("04:")[1].split("]")[0]);
                                    appMessageDto.setSiteName(warehouseDao.findWareArea(areaId).getSiteName());
                                }
                                Map<String, String> userNameInfo = getUserNameInfo(username, areaId,siteName);
                                appMessageDto.setSiteName(userNameInfo.get("siteName"));
                                appMessageDto.setRecipientName(userNameInfo.get("nickname"));
                                appMessageDto.setRecipientPhone(userNameInfo.get("phonenumber"));
                                appMessageDto.setIsClose(1);
                                Integer appMessage = appMessageDao.insertAppMessage(appMessageDto);
                                redisTemplate.opsForValue().set("maintain_app_message_"+ip,1);
                            }
                            break;
                        }
                        site = site + length * 2 + 2;
                    }
                }
            }
        }
        //刷新缓存区
        ctx.flush();
    }

    public Map<String,String> getUserNameInfo(String principalName,Integer areaId,String siteName){
        Map<String,String> map = new HashMap();
        if(ToolUtil.isEmpty(principalName)) {
            WareAreaVo wareAreaVo = warehouseDao.findWareArea(areaId);
            principalName = wareAreaVo.getPrincipalName();
            siteName = wareAreaVo.getSiteName();
            Integer buildingId = null;
            Integer warehouseId = null;
            if (ToolUtil.isEmpty(principalName)) {
                FloorVo floorVo = warehouseDao.findFloor(wareAreaVo.getFloorId());
                principalName = floorVo.getPrincipalName();
                buildingId = floorVo.getBuildingId();
            }
            if (ToolUtil.isEmpty(principalName)) {
                BuildingVo buildingVo = warehouseDao.findBuilding(buildingId.toString());
                principalName = buildingVo.getPrincipalName();
                warehouseId = buildingVo.getWarehouseId();
            }
            if (ToolUtil.isEmpty(principalName)) {
                principalName = warehouseDao.findWarehouse(warehouseId).getPrincipalName();
            }
        }
        map.put("siteName", siteName);
        String nickname = redisUtil.hget("USER_" + principalName, "nickname").toString();
        map.put("nickname",nickname);
        String phonenumber = redisUtil.hget("USER_" + principalName, "phonenumber").toString();
        map.put("phonenumber",phonenumber);
        return map;
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
                log.info("已经30秒没有接收到客户端的信息了");
                if (idle_count > 2) {
                    log.info("关闭这个不活跃的channel");
                    ctx.channel().close();
                }
                idle_count++;
            }
        } else {
            super.userEventTriggered(ctx, obj);
        }
    }
}
