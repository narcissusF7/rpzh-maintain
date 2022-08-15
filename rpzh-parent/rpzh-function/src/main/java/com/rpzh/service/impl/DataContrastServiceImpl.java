package com.rpzh.service.impl;

import com.alibaba.fastjson.JSONObject;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.util.StringUtil;
import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author: zyq
 * @Date: 2022/06/23/13:46
 * @Description: 数据对比
 */
@Service
public class DataContrastServiceImpl implements DataContrastService {

    @Resource
    private MaintainPlanDao maintainPlanDao;
    @Resource
    private NodeDao nodeDao;

    @Resource
    private WarehouseDao warehouseDao;

    /*
    * 数据对比中的条件选择设备
    */
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
        //设备名称
        String nodeName = "";
        //满足条件的所有位置路由
        List<String> siteRoutes = null;
        //物料代码不为空
        if(ToolUtil.isNotEmpty(materialCode)) {
            //根据物料代码查询位置路由(完整路由)
            siteRoutes = new ArrayList<>();
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
            siteRoutes = new ArrayList<>();
            if(size<5){
                //位置最多选择到层那一级
                siteRoutes.addAll(warehouseDao.findWareAreaSiteRoute(siteRoute));
                siteRoutes.addAll(warehouseDao.findStackSiteRoute(siteRoute));
            }else if(size==5){
                //位置选择到仓间那一级
                siteRoutes.addAll(warehouseDao.findStackSiteRoute(siteRoute));
            }else if(size==6){
                //位置选择到垛位那一级
                siteRoutes.add(siteRoute);
            }
        }
        List<NodeData> data = new ArrayList<>();
        //contrastType: 01:设备  02:位置
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
        //查询出来的数据为空
        if(ToolUtil.isEmpty(data)){
            return new Result<>(true, StatusCode.OK, "查询数据对比中的条件选择设备成功!",new HashMap<>());
        }
        //获取有关的仓间id
        List<Integer> areaIds = data.stream().filter((NodeData nodeData)->nodeData.getSiteRoute().split(",").length==5).map((NodeData nodeData)->Integer.parseInt(nodeData.getSiteRoute().split("04:")[1].split("]")[0])).distinct().collect(Collectors.toList());
        Map<String, String> mapInfos = new HashMap<>();
        if(ToolUtil.isNotEmpty(areaIds)) {
            //获取有关的仓间名称
            List<Map<String, String>> wareAreaInfos = warehouseDao.findWareAreaInfos(areaIds);
            //将仓间类型、名称设置到mapInfos中
            wareAreaInfos.forEach(wareAreaInfo -> {
                mapInfos.put(wareAreaInfo.get("siteRoute"), wareAreaInfo.get("siteName"));
                mapInfos.put(wareAreaInfo.get("siteRoute")+"_siteType","04");
            });
        }
        //获取有关的垛位id
        List<Integer> stackIds = data.stream().filter((NodeData nodeData)->nodeData.getSiteRoute().split(",").length==6).map((NodeData nodeData)->Integer.parseInt(nodeData.getSiteRoute().split("05:")[1].split("]")[0])).distinct().collect(Collectors.toList());
        if(ToolUtil.isNotEmpty(stackIds)) {
            //获取有关的垛位名称
            List<Map<String, String>> stackInfos = warehouseDao.findStackInfos(stackIds);
            //将垛位类型、名称设置到mapInfos中
            stackInfos.forEach(stackInfo -> {
                mapInfos.put(stackInfo.get("siteRoute"), stackInfo.get("siteName"));
                mapInfos.put(stackInfo.get("siteRoute")+"_siteType","05");
            });
        }
        for (NodeData dataInfo : data) {
            //设置位置名称
            dataInfo.setSiteName(mapInfos.get(dataInfo.getSiteRoute()));
            //设置曲线名称
            if("01".equals(nodeType) && "04".equals(mapInfos.get(dataInfo.getSiteRoute()+"_siteType"))){
                dataInfo.setNodeName("仓间"+nodeName);
            }else if("01".equals(nodeType) && "05".equals(mapInfos.get(dataInfo.getSiteRoute()+"_siteType"))){
                dataInfo.setNodeName("垛内"+nodeName);
            }else{
                dataInfo.setNodeName(nodeName);
            }
            dataInfo.setColor(getColor());
        }
        return new Result<>(true, StatusCode.OK, "查询数据对比中的条件选择设备成功!",data);
    }

    /*
    * 随机生成rgba的颜色,传给前端
    * */
    public String getColor(){
        Random random = new Random();
        int r = random.nextInt(256);
        int g = random.nextInt(256);
        int b = random.nextInt(256);
        return "rgba("+r+","+g+","+b+",1)";
    }

    @Resource
    private RedisTemplate redisTemplate;
    /*
    * 数据对比
    */
    @Override
    public Result getNodeDateList(String dataContrasts, String gatherType, Date startTime,Date endTime,Date beginTime,Date finishTime) {
        String user = TokenDecode.getUserInfo().get("username");
        //dataContrasts为[],将之前存入到redis中的数据清空
        Set<String> keys = redisTemplate.keys(user+"_maintain_data" + "*");
        List<DataContrastDto> dataContrastDtos = JSONObject.parseArray(dataContrasts, DataContrastDto.class);
        if(dataContrastDtos.size()==0){
            redisTemplate.delete(keys);
            return new Result(true, StatusCode.OK, "",new ArrayList<>());
        }
        //时间类型出错
        if (!("1".equals(gatherType)) && !("2".equals(gatherType)) && !("3".equals(gatherType)) && !("4".equals(gatherType))) {
            return new Result(false, StatusCode.ERROR, "时间类型错误!");
        }
        List<Map<String,Object>> gatherInfos = new ArrayList<>();
        List<Map<String,Object>> contrastInfos = new ArrayList<>();
        //采集的开始时间startTime、结束时间endTime
        Map<String, Date> setTime = GetBetweenDayUtils.getMonth(startTime, endTime);
        startTime = setTime.get("startTime");
        endTime = setTime.get("endTime");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String start = sdf.format(startTime);
        String end = sdf.format(endTime);
        //对比的开始时间beginTime、结束时间finishTime
        String begin = "";
        String finish = "";
        if (ToolUtil.isNotEmpty(beginTime) && ToolUtil.isNotEmpty(finishTime)) {
            begin = sdf.format(beginTime);
            finish = sdf.format(finishTime);
        }
        //获取采集的所有时间
        List<String> timeDays = new ArrayList<>();
        //获取对比的所有时间
        List<String> contrastDays = new ArrayList<>();
        switch (gatherType) {
            case "1":
                //按天统计
                gatherType = "formatDateTime(transfer_time,'%Y-%m-%d')";
                timeDays = GetBetweenDayUtils.getBetweenDays(start, end);
                if (StringUtil.isNotEmpty(begin) && StringUtil.isNotEmpty(finish)) {
                    contrastDays = GetBetweenDayUtils.getBetweenDays(begin, finish);
                }
                break;
            case "2":
                //按周统计
                gatherType = "toMonday(transfer_time)";
                timeDays = GetBetweenDayUtils.getBetweenWeek(start, end);
                if (StringUtil.isNotEmpty(begin) && StringUtil.isNotEmpty(finish)) {
                    contrastDays = GetBetweenDayUtils.getBetweenWeek(begin, finish);
                }
                break;
            case "3":
                //按月统计
                gatherType = "formatDateTime(transfer_time,'%Y-%m')";
                timeDays = GetBetweenDayUtils.getMonthBetweenDate(start, end);
                if (StringUtil.isNotEmpty(begin) && StringUtil.isNotEmpty(finish)) {
                    contrastDays = GetBetweenDayUtils.getMonthBetweenDate(begin, finish);
                }
                break;
            case "4":
                gatherType = "formatDateTime(transfer_time,'%Y')";
                timeDays = GetBetweenDayUtils.getYearBetweenDate(start, end);
                if (StringUtil.isNotEmpty(begin) && StringUtil.isNotEmpty(finish)) {
                    contrastDays = GetBetweenDayUtils.getYearBetweenDate(begin, finish);
                }
                break;
        }
        List<String> nodeNames = new ArrayList<>();
        nodeNames.add("时间");
        for (DataContrastDto dataContrastDto : dataContrastDtos) {
            String siteRoute = dataContrastDto.getSiteRoute();
            String nodeType = dataContrastDto.getNodeType();
            String nodeId = dataContrastDto.getNodeId();
            String siteName = dataContrastDto.getSiteName();
            String nodeName = dataContrastDto.getNodeName();
            String contrast_key = user+"_maintain_data_contrast"+siteRoute + "_"+nodeType+"_"+nodeId+"_"+beginTime+finishTime+startTime+endTime;
            String gather_key = user+"_maintain_data_gather"+siteRoute + "_"+nodeType+"_"+nodeId+"_"+beginTime+finishTime+startTime+endTime;
            if(ToolUtil.isNotEmpty(redisTemplate.opsForValue().get(gather_key))){
                Map<String,Object> gatherRedis = (Map<String,Object>)redisTemplate.opsForValue().get(gather_key);
                gatherInfos.add(gatherRedis);
                if (ToolUtil.isNotEmpty(beginTime) && ToolUtil.isNotEmpty(finishTime) && ToolUtil.isNotEmpty(redisTemplate.opsForValue().get(contrast_key))) {
                    Map<String,Object> contrastRedis = (Map<String,Object>)redisTemplate.opsForValue().get(contrast_key);
                    contrastInfos.add(contrastRedis);
                }
            }else {
                //采集的数据
                List<Map<String, Object>> data = new ArrayList<>();
                Map<String, Object> dataMap = new TreeMap<>();
                Map<String, Object> contrastDataMap = new TreeMap<>();
                //对比的数据
                List<Map<String, Object>> contrastData = new ArrayList<>();
                if ("01".equals(nodeType)) {
                    data = nodeDao.findSelectedNode(nodeId, startTime, endTime, gatherType, "oxygen", "tb_node_o2_history");
                    if (ToolUtil.isNotEmpty(beginTime) && ToolUtil.isNotEmpty(finishTime)) {
                        contrastData = nodeDao.findSelectedNode(nodeId, beginTime, finishTime, gatherType, "oxygen", "tb_node_o2_history");
                    }
                } else if ("02".equals(nodeType)) {
                    data = nodeDao.findSelectedNode(nodeId, startTime, endTime, gatherType, "temp", "tb_node_temp_core_chip_history");
                    if (ToolUtil.isNotEmpty(beginTime) && ToolUtil.isNotEmpty(finishTime)) {
                        contrastData = nodeDao.findSelectedNode(nodeId, beginTime, finishTime, gatherType, "temp", "tb_node_temp_core_chip_history");
                    }
                } else if ("03".equals(nodeType)) {
                    data = nodeDao.findSelectedNode(nodeId, startTime, endTime, gatherType, "temp", "tb_node_env_history");
                    if (ToolUtil.isNotEmpty(beginTime) && ToolUtil.isNotEmpty(finishTime)) {
                        contrastData = nodeDao.findSelectedNode(nodeId, beginTime, finishTime, gatherType, "temp", "tb_node_env_history");
                    }
                } else if ("04".equals(nodeType)) {
                    data = nodeDao.findSelectedNode(nodeId, startTime, endTime, gatherType, "humiture", "tb_node_env_history");
                    if (ToolUtil.isNotEmpty(beginTime) && ToolUtil.isNotEmpty(finishTime)) {
                        contrastData = nodeDao.findSelectedNode(nodeId, beginTime, finishTime, gatherType, "humiture", "tb_node_env_history");
                    }
                } else if ("05".equals(nodeType)) {
                    data = nodeDao.findSelectedSiteRoute(siteRoute,startTime, endTime, gatherType, "temp", "tb_node_temp_core_chip_history");
                    if (ToolUtil.isNotEmpty(beginTime) && ToolUtil.isNotEmpty(finishTime)) {
                        contrastData = nodeDao.findSelectedSiteRoute(siteRoute, beginTime, finishTime, gatherType, "temp", "tb_node_temp_core_chip_history");
                    }
                } else if ("06".equals(nodeType)) {
                    data = nodeDao.findSelectedSiteRoute(siteRoute, startTime, endTime, gatherType, "oxygen", "tb_node_o2_history");
                    if (ToolUtil.isNotEmpty(beginTime) && ToolUtil.isNotEmpty(finishTime)) {
                        contrastData = nodeDao.findSelectedSiteRoute(siteRoute, beginTime, finishTime, gatherType, "oxygen", "tb_node_o2_history");
                    }
                } else if ("07".equals(nodeType)) {
                    data = nodeDao.findSelectedSiteRoute(siteRoute, startTime, endTime, gatherType, "temp", "tb_node_env_history");
                    if (ToolUtil.isNotEmpty(beginTime) && ToolUtil.isNotEmpty(finishTime)) {
                        contrastData = nodeDao.findSelectedSiteRoute(siteRoute, beginTime, finishTime, gatherType, "temp", "tb_node_env_history");
                    }
                } else {
                    data = nodeDao.findSelectedSiteRoute(siteRoute, startTime, endTime, gatherType, "humiture", "tb_node_env_history");
                    if (ToolUtil.isNotEmpty(beginTime) && ToolUtil.isNotEmpty(finishTime)) {
                        contrastData = nodeDao.findSelectedSiteRoute(siteRoute, beginTime, finishTime, gatherType, "humiture", "tb_node_env_history");
                    }
                }
                data.forEach(d -> {
                    dataMap.put(d.get("dayTime").toString(),d.get("data"));
                });
                //将没有的采集日期补0
                for (String timeDay : timeDays) {
                    if (ToolUtil.isEmpty(dataMap.get(timeDay))) {
                        dataMap.put(timeDay,0);
                    }
                }
                Map<String,Object> contrastInfo = new HashMap<>();
                if (ToolUtil.isNotEmpty(beginTime) && ToolUtil.isNotEmpty(finishTime)) {
                    contrastData.forEach(d -> {
                        contrastDataMap.put(d.get("dayTime").toString(),d.get("data"));
                    });
                    //将没有的对比日期补0
                    for (String timeDay : contrastDays) {
                        if (ToolUtil.isEmpty(contrastDataMap.get(timeDay))) {
                            contrastDataMap.put(timeDay,0);
                        }
                    }
                    redisTemplate.opsForValue().set(contrast_key,contrastDataMap,15, TimeUnit.MINUTES);
                    contrastInfos.add(contrastDataMap);
                }
                redisTemplate.opsForValue().set(gather_key,dataMap,15, TimeUnit.MINUTES);
                gatherInfos.add(dataMap);
            }
            nodeId = nodeId != null ? nodeId + "、":"";
            nodeNames.add(nodeId+siteName+"、"+nodeName);
        }
        String totalNameKey = user+"_maintain_data_total_nodeName";
        redisTemplate.opsForValue().set(totalNameKey,nodeNames,30, TimeUnit.MINUTES);
        List<Map> gatherList = new ArrayList<>();
        List<Map> contrastList = new ArrayList<>();
        List<Map> totalList = new ArrayList<>();
        Set<String> gatherKeys = gatherInfos.get(0).keySet();
        gatherKeys.forEach(key -> {
            Map t =new HashMap();
            t.put("day",key);
            List<Object> data = new ArrayList<>();
            for (int i = 0; i < dataContrastDtos.size(); i++) {
                data.add(gatherInfos.get(i).get(key));
            }
            t.put("data",data);
            gatherList.add(t);
            totalList.add(t);
        });
        Map dataInfos = new HashMap();
        dataInfos.put("gatherList",gatherList);
        if (ToolUtil.isNotEmpty(beginTime) && ToolUtil.isNotEmpty(finishTime)) {
            Set<String> contrastKeys = contrastInfos.get(0).keySet();
            contrastKeys.forEach(key -> {
                Map t = new HashMap();
                t.put("day", key);
                List<Object> data = new ArrayList<>();
                for (int i = 0; i < dataContrastDtos.size(); i++) {
                    data.add(contrastInfos.get(i).get(key));
                }
                t.put("data" , data);
                contrastList.add(t);
                totalList.add(t);
            });
        }
        totalList.sort((v1, v2) -> v1.get("day").toString().compareTo(v2.get("day").toString()));
        dataInfos.put("contrastList",contrastList);
        dataInfos.put("totalList",totalList);
        dataInfos.put("username",user);
        String totalKey = user+"_maintain_data_total_nodeData";
        redisTemplate.opsForValue().set(totalKey,totalList,30, TimeUnit.MINUTES);
        return new Result<>(true, StatusCode.OK, "数据对比成功!",dataInfos);
    }
}
