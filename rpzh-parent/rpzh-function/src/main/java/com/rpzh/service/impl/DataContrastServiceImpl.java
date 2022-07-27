package com.rpzh.service.impl;

import com.rpzh.dao.*;
import com.rpzh.dao.WarehouseDao;
import com.rpzh.dto.DataContrastDto;
import com.rpzh.dto.NodeDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.pojo.NodeData;
import com.rpzh.service.DataContrastService;
import com.rpzh.util.*;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.util.StringUtil;
import javax.annotation.Resource;
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

    @Resource
    private MaintainPlanDao maintainPlanDao;
    @Resource
    private NodeDao nodeDao;

    @Resource
    private WarehouseDao warehouseDao;

    @Override
    public Result getNodeData(NodeDto nodeDto) {
        //获取设备类型
        String nodeType = nodeDto.getNodeType();
        //获取对比类型
        String contrastType = nodeDto.getContrastType();
        if(StringUtil.isEmpty(nodeType) || StringUtil.isEmpty(contrastType)){
            return new Result(false, StatusCode.ERROR, "设备类型或对比类型不能为空!");
        }
        //位置id
        Integer stackId = nodeDto.getStackId();
        //位置路由
        String siteRoute = nodeDto.getSiteRoute();
        if(StringUtil.isNotEmpty(siteRoute)) {
            //当前路径拼接
            siteRoute = SiteRouteUtil.route(siteRoute,stackId+"");
        }
        //获取物料等级
        String materialCode = nodeDto.getMaterialCode();
        String nodeName = "";
        //满足条件的所有位置路由
        List<String> siteRoutes = null;
        //物料代码不为空
        if(ToolUtil.isNotEmpty(materialCode)) {
            //根据物料代码查询位置路由
            siteRoutes = maintainPlanDao.findSite(materialCode);
        }
        //前端传入的位置路由和满足条件的所有位置路由都不为空
        if(StringUtil.isNotEmpty(siteRoute) && ToolUtil.isNotEmpty(siteRoutes)) {
            int size = siteRoutes.size();
            for (int i = 0; i < size; i++) {
                //满足条件的所有位置路由不包含前端传入的位置路由
                if (!siteRoutes.get(i).contains(siteRoute)){
                    siteRoutes.remove(i);
                    size--;
                }
            }
        }
        //前端传入的位置路由不为空,满足条件的所有位置路由为空
        if(StringUtil.isNotEmpty(siteRoute) && ToolUtil.isEmpty(siteRoutes)) {
            int size = siteRoute.split(",").length;
            if(size<5){
                siteRoutes.addAll(warehouseDao.findWareAreaSiteRoute(siteRoute));
                siteRoutes.addAll(warehouseDao.findStackSiteRoute(siteRoute));
            }else if(size==5){
                siteRoutes.addAll(warehouseDao.findStackSiteRoute(siteRoute));
            }else if(size==6){
                siteRoutes.add(siteRoute);
            }
        }
        List<NodeData> data = new ArrayList<>();
        if("01".equals(nodeType) && "01".equals(contrastType)){
            nodeName = "氧气设备";
            data = nodeDao.findDataOxygen(siteRoutes);
        }else if("02".equals(nodeType) && "01".equals(contrastType)){
            nodeName = "垛内包芯温度设备";
            data = nodeDao.findDataNode("tb_node_temp_core_chip_history", "temp", siteRoutes);
        }else if("03".equals(nodeType) && "01".equals(contrastType)){
            nodeName = "垛内环境温湿度设备-温度";
            data = nodeDao.findDataNode("tb_node_env_history", "temp", siteRoutes);
        }else if("04".equals(nodeType) && "01".equals(contrastType)){
            nodeName = "垛内环境温湿度设备-湿度";
            data = nodeDao.findDataNode("tb_node_env_history", "humiture", siteRoutes);
        }else if("05".equals(nodeType) && "02".equals(contrastType)) {
            nodeName = "垛内平均包芯温度";
            data = nodeDao.findDataContrast("tb_node_temp_core_chip_history", "avg(temp) as temp", siteRoutes);
        } else if("06".equals(nodeType) && "02".equals(contrastType)) {
            nodeName = "仓间平均氧气";
            data = nodeDao.findDataContrast("tb_node_o2_history", "avg(oxygen) as oxygen", siteRoutes);
        }else if("07".equals(nodeType) && "02".equals(contrastType)) {
            nodeName = "仓间平均温度";
            data = nodeDao.findDataContrast("tb_air_conditioner_history", "avg(temp) as temp", siteRoutes);
        }else if("08".equals(nodeType) && "02".equals(contrastType)) {
            nodeName = "仓间平均湿度";
            data = nodeDao.findDataContrast("tb_air_conditioner_history", "avg(humiture) as humiture", siteRoutes);
        }else {
            return new Result(false, StatusCode.ERROR, "设备类型出错了!");
        }

        if(ToolUtil.isEmpty(data)){
            return new Result<>(true, StatusCode.OK, "查询数据对比中的条件选择设备成功!",new HashMap<>());
        }
        List<Integer> areaIds = data.stream().filter((NodeData nodeData)->nodeData.getPidRoute().split(",").length==5).map((NodeData nodeData)->Integer.parseInt(nodeData.getPidRoute().split("04:")[1].split("]")[0])).distinct().collect(Collectors.toList());
        Map<String, String> mapInfos = new HashMap<>();
        if(ToolUtil.isNotEmpty(areaIds)) {
            warehouseDao.findWareAreaInfos(areaIds).forEach(wareAreaInfo -> {
                mapInfos.put(wareAreaInfo.get("siteRoute"), wareAreaInfo.get("siteName") + "--" + "04");
            });
        }
        List<Integer> stackIds = data.stream().filter((NodeData nodeData)->nodeData.getPidRoute().split(",").length==6).map((NodeData nodeData)->Integer.parseInt(nodeData.getPidRoute().split("05:")[1].split("]")[0])).distinct().collect(Collectors.toList());
        if(ToolUtil.isNotEmpty(stackIds)) {
            warehouseDao.findStackInfos(stackIds).forEach(stackInfo -> {
                mapInfos.put(stackInfo.get("siteRoute"), stackInfo.get("siteName") + "--" + "05");
            });
        }
        String node = nodeName;
        data.forEach(dataInfo -> {
            String[] mapVo = mapInfos.get(dataInfo.getPidRoute()).split("--");
            dataInfo.setSiteName(mapVo[0]);
            if("01".equals(nodeType) && "04".equals(mapVo[1])){
                dataInfo.setNodeName("仓间"+node);
            }else if("01".equals(nodeType) && "05".equals(mapVo[1])){
                dataInfo.setNodeName("垛内"+node);
            }else{
                dataInfo.setNodeName(node);
            }
        });
        return new Result<>(true, StatusCode.OK, "查询数据对比中的条件选择设备成功!",data);
    }

    @Override
    public Result getNodeDateList(DataContrastDto dataContrastDto) {
        String siteRoute = dataContrastDto.getSiteRoute();
        String nodeType = dataContrastDto.getNodeType();
        String nodeId = dataContrastDto.getNodeId();
        String siteName = dataContrastDto.getSiteName();
        if(ToolUtil.isEmpty(siteName) || ToolUtil.isEmpty(siteRoute) || ToolUtil.isEmpty(nodeType)){
            return new Result(false, StatusCode.ERROR, "设备类型或位置不能为空!");
        }
        //时间类型
        String gatherType= dataContrastDto.getGatherType();
        //采集的开始时间startTime、结束时间endTime
        Map<String, Date> setTime = GetBetweenDayUtils.getMonth(dataContrastDto.getStartTime(), dataContrastDto.getEndTime());
        dataContrastDto.setStartTime(setTime.get("startTime"));
        dataContrastDto.setEndTime(setTime.get("endTime"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String startTime = sdf.format(dataContrastDto.getStartTime());
        String endTime = sdf.format(dataContrastDto.getEndTime());
        String beginTime = null;
        String finishTime = null;
        if(ToolUtil.isNotEmpty(dataContrastDto.getBeginTime()) && ToolUtil.isNotEmpty(dataContrastDto.getFinishTime())){
            beginTime = sdf.format(dataContrastDto.getBeginTime());
            finishTime = sdf.format(dataContrastDto.getFinishTime());
        }
        List<String> timeDays = new ArrayList<>();
        List<String> contrastDays = new ArrayList<>();
        switch (gatherType){
            case "1":
                gatherType = "formatDateTime(transfer_time,'%Y-%m-%d')";
                timeDays = GetBetweenDayUtils.getBetweenDays(startTime, endTime);
                if(StringUtil.isNotEmpty(beginTime) && StringUtil.isNotEmpty(finishTime)){
                    contrastDays = GetBetweenDayUtils.getBetweenDays(beginTime, finishTime);
                }
                break;
            case "2":
                gatherType = "toMonday(transfer_time)";
                timeDays = GetBetweenDayUtils.getBetweenWeek(startTime, endTime);
                if(StringUtil.isNotEmpty(beginTime) && StringUtil.isNotEmpty(finishTime)){
                    contrastDays = GetBetweenDayUtils.getBetweenWeek(beginTime, finishTime);
                }
                break;
            case "3":
                gatherType = "formatDateTime(transfer_time,'%Y-%m')";
                timeDays = GetBetweenDayUtils.getMonthBetweenDate(startTime, endTime);
                if(StringUtil.isNotEmpty(beginTime) && StringUtil.isNotEmpty(finishTime)){
                    contrastDays = GetBetweenDayUtils.getMonthBetweenDate(beginTime, finishTime);
                }
                break;
            case "4":
                gatherType = "formatDateTime(transfer_time,'%Y')";
                timeDays = GetBetweenDayUtils.getYearBetweenDate(startTime, endTime);
                if(StringUtil.isNotEmpty(beginTime) && StringUtil.isNotEmpty(finishTime)){
                    contrastDays = GetBetweenDayUtils.getYearBetweenDate(beginTime, finishTime);
                }
                break;
        }
        String nodeName = "";
        List<Map<String,Object>> data = new ArrayList<>();
        List<Map<String,Object>> contrastData = new ArrayList<>();
        if("01".equals(nodeType)){
            if(siteRoute.split(",").length==5) {
                nodeName = "仓间氧气设备";
            }else{
                nodeName = "垛内氧气设备";
            }
            data = nodeDao.findSelectedNode(nodeId,dataContrastDto.getStartTime(),dataContrastDto.getEndTime(),gatherType,"oxygen","tb_node_o2_history");
            if(ToolUtil.isNotEmpty(dataContrastDto.getBeginTime()) && ToolUtil.isNotEmpty(dataContrastDto.getFinishTime())){
                contrastData = nodeDao.findSelectedNode(nodeId,dataContrastDto.getBeginTime(),dataContrastDto.getFinishTime(),gatherType,"oxygen","tb_node_o2_history");
            }
        }else if("02".equals(nodeType)){
            nodeName = "垛内包芯温度设备";
            data = nodeDao.findSelectedNode(nodeId,dataContrastDto.getStartTime(),dataContrastDto.getEndTime(),gatherType,"temp","tb_node_temp_core_chip_history");
            if(ToolUtil.isNotEmpty(dataContrastDto.getBeginTime()) && ToolUtil.isNotEmpty(dataContrastDto.getFinishTime())){
                contrastData = nodeDao.findSelectedNode(nodeId,dataContrastDto.getBeginTime(),dataContrastDto.getFinishTime(),gatherType,"temp","tb_node_temp_core_chip_history");
            }
        }else if("03".equals(nodeType)){
            nodeName = "垛内环境温湿度设备-温度";
            data = nodeDao.findSelectedNode(nodeId,dataContrastDto.getStartTime(),dataContrastDto.getEndTime(),gatherType,"temp","tb_node_env_history");
            if(ToolUtil.isNotEmpty(dataContrastDto.getBeginTime()) && ToolUtil.isNotEmpty(dataContrastDto.getFinishTime())){
                contrastData = nodeDao.findSelectedNode(nodeId,dataContrastDto.getBeginTime(),dataContrastDto.getFinishTime(),gatherType,"temp","tb_node_env_history");
            }
        }else if("04".equals(nodeType)){
            nodeName = "垛内环境温湿度设备-湿度";
            data = nodeDao.findSelectedNode(nodeId,dataContrastDto.getStartTime(),dataContrastDto.getEndTime(),gatherType,"humiture","tb_node_env_history");
            if(ToolUtil.isNotEmpty(dataContrastDto.getBeginTime()) && ToolUtil.isNotEmpty(dataContrastDto.getFinishTime())){
                contrastData = nodeDao.findSelectedNode(nodeId,dataContrastDto.getBeginTime(),dataContrastDto.getFinishTime(),gatherType,"humiture","tb_node_env_history");
            }
        }else if("05".equals(nodeType)) {
            nodeName = "垛内平均包芯温度";
            data = nodeDao.findSelectedSiteRoute(siteRoute,dataContrastDto.getStartTime(),dataContrastDto.getEndTime(),gatherType,"temp","tb_node_temp_core_chip_history");
            if(ToolUtil.isNotEmpty(dataContrastDto.getBeginTime()) && ToolUtil.isNotEmpty(dataContrastDto.getFinishTime())){
                contrastData = nodeDao.findSelectedSiteRoute(siteRoute,dataContrastDto.getBeginTime(),dataContrastDto.getFinishTime(),gatherType,"temp","tb_node_temp_core_chip_history");
            }
        } else if("06".equals(nodeType)) {
            nodeName = "仓间平均氧气";
            data = nodeDao.findSelectedSiteRoute(siteRoute,dataContrastDto.getStartTime(),dataContrastDto.getEndTime(),gatherType,"oxygen","tb_node_o2_history");
            if(ToolUtil.isNotEmpty(dataContrastDto.getBeginTime()) && ToolUtil.isNotEmpty(dataContrastDto.getFinishTime())){
                contrastData = nodeDao.findSelectedSiteRoute(siteRoute,dataContrastDto.getBeginTime(),dataContrastDto.getFinishTime(),gatherType,"oxygen","tb_node_o2_history");
            }
        }else if("07".equals(nodeType)) {
            nodeName = "仓间平均温度";
            data = nodeDao.findSelectedSiteRoute(siteRoute,dataContrastDto.getStartTime(),dataContrastDto.getEndTime(),gatherType,"temp","tb_node_env_history");
            if(ToolUtil.isNotEmpty(dataContrastDto.getBeginTime()) && ToolUtil.isNotEmpty(dataContrastDto.getFinishTime())){
                contrastData = nodeDao.findSelectedSiteRoute(siteRoute,dataContrastDto.getBeginTime(),dataContrastDto.getFinishTime(),gatherType,"temp","tb_node_env_history");
            }
        }else{
            nodeName = "仓间平均湿度";
            data = nodeDao.findSelectedSiteRoute(siteRoute,dataContrastDto.getStartTime(),dataContrastDto.getEndTime(),gatherType,"humiture","tb_node_env_history");
            if(ToolUtil.isNotEmpty(dataContrastDto.getBeginTime()) && ToolUtil.isNotEmpty(dataContrastDto.getFinishTime())){
                contrastData = nodeDao.findSelectedSiteRoute(siteRoute,dataContrastDto.getBeginTime(),dataContrastDto.getFinishTime(),gatherType,"humiture","tb_node_env_history");
            }
        }
        if(!"".equals(nodeId)){
            nodeId = nodeId + "、";
        }
        nodeName = nodeId + siteName + "、" + nodeName;
        List<String> days = data.stream().map((Map map) ->map.get("dayTime").toString()).collect(Collectors.toList());
        for (String timeDay : timeDays) {
            if(!days.contains(timeDay)){
                Map<String,Object> map = new HashMap<>();
                map.put("dayTime",timeDay);
                map.put("data",0);
                data.add(map);
            }
        }
        data.sort((v1, v2) -> v1.get("dayTime").toString().compareTo(v2.get("dayTime").toString()));
        Map<String,Object> mapInfo = new HashMap<>();
        mapInfo.put("data",data);
        mapInfo.put("nodeName",nodeName);
        if(ToolUtil.isNotEmpty(dataContrastDto.getBeginTime()) && ToolUtil.isNotEmpty(dataContrastDto.getFinishTime())){
            List<String> contrastDates = data.stream().map((Map map) ->map.get("dayTime").toString()).collect(Collectors.toList());
            for (String contrastDay : contrastDays) {
                if(!contrastDays.contains(contrastDay)){
                    Map<String,Object> map = new HashMap<>();
                    map.put("dayTime",contrastDay);
                    map.put("data",0);
                    contrastData.add(map);
                }
            }
            contrastData.sort((v1, v2) -> v1.get("dayTime").toString().compareTo(v2.get("dayTime").toString()));
            mapInfo.put("contrastData",contrastData);
        }

        return new Result<>(true, StatusCode.OK, "数据对比成功!",mapInfo);
    }
}
