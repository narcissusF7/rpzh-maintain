package com.rpzh.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rpzh.dao.MaintainPlanDao;
import com.rpzh.dao.NodeDao;
import com.rpzh.dto.DataContrastDto;
import com.rpzh.dto.NodeDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.pojo.NodeData;
import com.rpzh.service.DataContrastService;
import com.rpzh.util.DateTypeUtils;
import com.rpzh.util.TokenDecode;
import com.rpzh.vo.DataContrastVo;
import com.rpzh.vo.DataVo;
import com.rpzh.vo.MaintainCountVo;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import tk.mybatis.mapper.util.StringUtil;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: zyq
 * @Date: 2022/06/23/13:46
 * @Description: 功能
 */
@Service
public class DataContrastServiceImpl implements DataContrastService {
    public static String MAINTAIN ="MAINTAIN_NODE_TYPE_";
    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private MaintainPlanDao maintainPlanDao;
    @Resource
    private NodeDao nodeDao;

    @Override
    public Result getNodeData(NodeDto nodeDto) {
        String nodeType = nodeDto.getNodeType();
        String contrastType = nodeDto.getContrastType();
        if(StringUtil.isEmpty(nodeType) || StringUtil.isEmpty(contrastType)){
            return new Result(false, StatusCode.ERROR, "设备类型或对比类型不能为空!");
        }
        String username = TokenDecode.getUserInfo().get("username");
        Integer flag = nodeDto.getFlag();
        Object o = redisTemplate.opsForValue().get(MAINTAIN+username);
        //被重置过
        if(flag==0){
            if(ToolUtil.isNotEmpty(o)) {
                // 把redis中的之前的设备类型值删除
                redisTemplate.delete(MAINTAIN+username);
            }
            // 往redis中存入本次的设备类型值
            redisTemplate.opsForValue().set(MAINTAIN+username, nodeType);
        }
        //未被重置   判断本次的设备类型与上一次的设备类型是否相同
        if(flag==1){
            String type = o.toString();
            // 01:氧气 02:包芯温度 03:环境温度 04:环境湿度
            boolean o2Flag = nodeType.equals("01") && !type.equals("01");
            boolean tempFlag = (nodeType.equals("02") || nodeType.equals("03")) && (type.equals("01") || type.equals("04"));
            boolean moistureFlag = nodeType.equals("04") && !type.equals("04");
            if (o2Flag || tempFlag || moistureFlag) {
                return new Result(false, StatusCode.ERROR, "设备类型与之前选择的不同!",1);
            }
        }
        Integer stackId = nodeDto.getStackId();
        String siteRoute = nodeDto.getSiteRoute();
        if(StringUtil.isNotEmpty(siteRoute)) {
            siteRoute = siteRoute + ",[0" + siteRoute.split(",").length + ":" + stackId + "]";
        }
        String materialCode = nodeDto.getMaterialCode();
        String nodeName = "";
        List<MaintainCountVo> maintainCountVos = maintainPlanDao.findSite(materialCode);
        Map<String,String> map = new HashMap<>();
        if(maintainCountVos.size()>0) {
            //垛位位置
            maintainCountVos.forEach(maintainCountVo -> {
                map.put(maintainCountVo.getPidRoute()+"_name", maintainCountVo.getStackName());
                //物料代码为空  仓间位置
                if(StringUtil.isEmpty(materialCode)) {
                    map.put(maintainCountVo.getPidRoute().split(",\\[05")[0]+"_name", maintainCountVo.getStackName().replace(maintainCountVo.getNameCn(),""));
                }
            });
        }
        List<NodeData> data = new ArrayList<>();
        //01:氧气传感器,02:包芯温度传感器,03:温湿度传感器-温度,温湿度传感器-湿度
        switch (nodeType){
            case "01":
                nodeName = "氧气设备";
                data = nodeDao.findData("tb_node_o2_history", "oxygen", siteRoute,materialCode);
                break;
            case "02":
                nodeName = "包芯温度";
                data = nodeDao.findData("tb_node_temp_core_chip_history", "temp", siteRoute,materialCode);
                break;
            case "03":
                nodeName = "环境温度";
                data = nodeDao.findData("tb_node_env_history_test", "temp", siteRoute,materialCode);
                if(StringUtil.isEmpty(materialCode)) {
                    List<NodeData> airData1 = nodeDao.findData("tb_air_conditioner_history", "temp", siteRoute, materialCode);
                    for (NodeData airData : airData1) {
                        data.add(airData);
                    }
                }
                break;
            case "04":
                nodeName = "环境湿度";
                data = nodeDao.findData("tb_node_env_history_test", "humiture", siteRoute,materialCode);
                if(StringUtil.isEmpty(materialCode)) {
                    List<NodeData> airData2 = nodeDao.findData("tb_air_conditioner_history", "humiture", siteRoute, materialCode);
                    for (NodeData airData : airData2) {
                        data.add(airData);
                    }
                }
                break;
        }
        data.sort((v1, v2) -> v1.getPidRoute().compareTo(v2.getPidRoute()));
        //对比的类型:1:设备   2:位置
        if("1".equals(contrastType)){
            String name = nodeName;
            data.forEach(d ->{
                d.setSiteName(map.get(d.getPidRoute()+"_name"));
                d.setNodeName(name);
                d.setType(nodeType);
            });
        }
        if("2".equals(contrastType)){
            List<NodeData> nodeInfo = new ArrayList<>();
            int index = 0;
            nodeInfo.add(getNodeData(nodeType, data.get(0),map));
            for (int i = 1; i < data.size(); i++) {
                if(data.get(i-1).getPidRoute().equals(data.get(i).getPidRoute())){
                    nodeInfo.get(index).setOxygen(nodeInfo.get(index).getOxygen()+data.get(i).getOxygen());
                    nodeInfo.get(index).setTemp(nodeInfo.get(index).getTemp()+data.get(i).getTemp());
                    nodeInfo.get(index).setHumiture(nodeInfo.get(index).getHumiture()+data.get(i).getHumiture());
                    nodeInfo.get(index).setCount(nodeInfo.get(index).getCount()+1);
                }
                if(!data.get(i-1).getPidRoute().equals(data.get(i).getPidRoute())){
                    nodeInfo.add(getNodeData(nodeType, data.get(i),map));
                    index++;
                }
            }
            nodeInfo.forEach(nodeData -> {
                nodeData.setType(nodeType);
                nodeData.setOxygen(Double.parseDouble(String.format("%.1f",nodeData.getOxygen()/nodeData.getCount())));
                nodeData.setTemp(Double.parseDouble(String.format("%.1f",nodeData.getTemp()/nodeData.getCount())));
                nodeData.setHumiture(Double.parseDouble(String.format("%.1f",nodeData.getHumiture()/nodeData.getCount())));
            });
            data = nodeInfo;
        }
        return new Result<>(true, StatusCode.OK, "查询数据对比中的条件选择设备成功!",data);
    }

    public NodeData getNodeData(String nodeType,NodeData nodeData,Map<String,String> map){
        int length = nodeData.getPidRoute().split(",").length;
        String nodeName = "";
        if(length==5){
            nodeName = "仓间平均";
        }else{
            nodeName = "垛位平均";
        }
        nodeData.setNodeId(null);
        switch (nodeType){
            case "01":
                nodeData.setNodeName(nodeName + "氧气含量");
                break;
            case "02":
                nodeData.setNodeName(nodeName +"包芯温度");
                break;
            case "03":
                nodeData.setNodeName(nodeName +"环境温度");
                break;
            case "04":
                nodeData.setNodeName(nodeName +"环境湿度");
                break;
        }
        nodeData.setSiteName(map.get(nodeData.getPidRoute()+"_name"));
        nodeData.setCount(1d);
        return nodeData;
    }

    @Override
    public Result getNodeDateList(DataContrastDto dataContrastDto) {
        String nodeDataInfos = dataContrastDto.getNodeDatas();
        Date startTime = dataContrastDto.getStartTime();
        Date endTime = dataContrastDto.getEndTime();
        List<NodeData> nodeDataDtos = JSON.parseArray(nodeDataInfos, NodeData.class);
        if(nodeDataDtos.size()>64){
            return new Result(false, StatusCode.ERROR, "所选设备超过64个!");
        }
        Map<String,String> keyInfo = new HashMap<>();
        nodeDataDtos.forEach(dtos -> {
            String nodeId = dtos.getNodeId();
            if(StringUtil.isNotEmpty(nodeId)){
                nodeId = nodeId + "、";
            }
            keyInfo.put(dtos.getNodeId()+ "-" + dtos.getPidRoute(),nodeId + dtos.getSiteName() + "、" + dtos.getNodeName());
        });
        //采集的开始时间、结束时间startTime endTime
        //对比的开始时间、结束时beginTime finishTime
        if(ToolUtil.isNotEmpty(endTime)){
            dataContrastDto.setEndTime(new Date(endTime.getTime() + 24 * 60 * 60 * 1000 - 1));
        }
        if(ToolUtil.isEmpty(startTime) && ToolUtil.isEmpty(endTime)){
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, 0);
            calendar.set(Calendar.DAY_OF_MONTH, 1);// 设置为1号,当前日期既为本月第一天
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            dataContrastDto.setStartTime(calendar.getTime());
            calendar.add(Calendar.MONTH, 1);
            dataContrastDto.setEndTime(new Date(calendar.getTime().getTime() - 1000));
        }
        String gatherType= dataContrastDto.getGatherType();
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(startTime);
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(endTime);
        //采集的时间类型 1:天  2:周  3:月  4:年  默认月
        SimpleDateFormat simpleDateFormat;
        int year = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
        int month = year * 12 + endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);
        long day = ((endTime.getTime() - startTime.getTime()) / (1000*3600*24));
        List<DataContrastVo> days = new ArrayList<>();
        switch (gatherType){
            case "1":
                gatherType = "formatDateTime(transfer_time,'%m月%d日')";
                simpleDateFormat = new SimpleDateFormat("MM月dd日");
                for (long i = 0;i<=day;i++){
                    String format = simpleDateFormat.format(new Date(startTime.getTime() + i * 1000 * 60 * 60 * 24));
                    DataContrastVo dataContrastVo = new DataContrastVo(format);
                    days.add(dataContrastVo);
                }
                break;
            case "2":
                gatherType = "(toYearWeek(transfer_time) - toYearWeek(toDateTime(#{startTime}))+1)";
                startCalendar.setFirstDayOfWeek(Calendar.MONDAY);
                endCalendar.setFirstDayOfWeek(Calendar.MONDAY);
                int l = endCalendar.get(Calendar.WEEK_OF_YEAR) - startCalendar.get(Calendar.WEEK_OF_YEAR) + 1;
                for (int i = 1;i<=l;i++){
                    DataContrastVo dataContrastVo = new DataContrastVo(i+"");
                    days.add(dataContrastVo);
                }
                break;
            case "3":
                gatherType = "formatDateTime(transfer_time,'%Y年%m月')";
                simpleDateFormat = new SimpleDateFormat("yyyy年MM月");
                for (long i = 0;i<=month;i++){
                    String format = simpleDateFormat.format(startCalendar.getTime());
                    DataContrastVo dataContrastVo = new DataContrastVo(format);
                    days.add(dataContrastVo);
                    startCalendar.add(Calendar.MONTH,1);
                }
                break;
            case "4":
                gatherType = "formatDateTime(transfer_time,'%Y年')";
                simpleDateFormat = new SimpleDateFormat("yyyy年");
                for (long i = 0;i<=year;i++){
                    String format = simpleDateFormat.format(startCalendar.getTime());
                    DataContrastVo dataContrastVo = new DataContrastVo(format);
                    days.add(dataContrastVo);
                    startCalendar.add(Calendar.YEAR,1);
                }
                break;
        }
        String username = TokenDecode.getUserInfo().get("username");
        redisTemplate.opsForValue().set(username+"_days",JSON.toJSONString(days));
        //获取所选的位置设备所有的位置路由
        List<String> pidRoutes=nodeDataDtos.stream().filter((NodeData nodeData)->StringUtil.isEmpty(nodeData.getNodeId())).map(NodeData::getPidRoute).collect(Collectors.toList());
        long count = nodeDataDtos.stream().map(NodeData::getType).distinct().count();
        List<NodeData> dataListByEnv = new ArrayList<>();
        List<DataVo> dataVos = new ArrayList<>();
        //所选设备中包含包芯温度和环境温度
        if(count!=1){
            List<String> core=nodeDataDtos.stream().filter((NodeData nodeData)->nodeData.getType().equals("02")).map(NodeData::getNodeId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
            List<String> env=nodeDataDtos.stream().filter((NodeData nodeData)->nodeData.getType().equals("03")).map(NodeData::getNodeId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
            dataListByEnv = nodeDao.findDataListByEnv(gatherType, startTime, endTime, env,core, pidRoutes);
        }
        //所选设备中只包含一种设备
        if(count==1){
            List<String> node=nodeDataDtos.stream().filter((NodeData nodeData)->StringUtil.isNotEmpty(nodeData.getNodeId())).map(NodeData::getNodeId).distinct().collect(Collectors.toList());
            String type = nodeDataDtos.get(0).getType();
            if("01".equals(type)){
                dataListByEnv = nodeDao.findOxygenList("oxygen",gatherType,"tb_node_o2_history", startTime, endTime, node, pidRoutes);
            }else if("02".equals(type)){
                dataListByEnv = nodeDao.findOxygenList("temp",gatherType,"tb_node_temp_core_chip_history", startTime, endTime, node, pidRoutes);
            }else if("03".equals(type)){
                dataListByEnv = nodeDao.findOxygenList("temp",gatherType,"tb_node_env_history_test", startTime, endTime, node, pidRoutes);
            }else{
                dataListByEnv = nodeDao.findOxygenList("humiture",gatherType,"tb_node_env_history_test", startTime, endTime, node, pidRoutes);
            }
        }
        List<String> nodeNames = new ArrayList<>();
        dataListByEnv.forEach(data->{
            DataContrastVo dataContrastVo = new DataContrastVo(data.getTransferTime());
            String nodeName = keyInfo.get(data.getNodeId()  + "-" + data.getPidRoute());
            if(!nodeNames.contains(nodeName)){
                List<DataContrastVo> dayData = JSONArray.parseArray(redisTemplate.opsForValue().get(username+"_days").toString(), DataContrastVo.class);
                int i = dayData.indexOf(dataContrastVo);
                dataContrastVo.setNode(data.getTemp());
                dayData.set(i,dataContrastVo);
                DataVo dataVo = new DataVo(nodeName,dayData);
                dataVos.add(dataVo);
                nodeNames.add(nodeName);
            }else{
                int i = nodeNames.indexOf(nodeName);
                List<DataContrastVo> dataContrastVos = dataVos.get(i).getDataContrastVos();
                int index = dataContrastVos.indexOf(dataContrastVo);
                dataContrastVos.get(index).setNode(data.getTemp());
            }
        });
        //下周优化
        return new Result<>(true, StatusCode.OK, "数据对比成功!",dataVos);
    }
}
