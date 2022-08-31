package com.rpzh.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.rpzh.constant.NodeConstant;
import com.rpzh.constant.WarningTypeConstant;
import com.rpzh.dao.*;

import com.rpzh.entity.ToolUtil;
import com.rpzh.enums.NodeTypeEnum;
import com.rpzh.handler.DataHandler;
import com.rpzh.pojo.GeneralSetting;
import com.rpzh.pojo.NodeData;
import com.rpzh.service.MonitoringCenterService;
import com.rpzh.util.ManuallyPageUtil;
import com.rpzh.util.MonitorCondition;
import com.rpzh.vo.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
public class MonitoringCenterServiceImpl implements MonitoringCenterService {
    private static final String CACHE_KEY_REDIS_MAINTAIN = "MAINTAIN_";

    private static final String GENERAL_SETTING_VALUE = "GENERAL_SETTING_VALUE_";

    @Autowired
    AlarmValueSettingDao alarmValueSettingDao;
    @Autowired
    EarlyWarningDao earlyWarningDao;
    @Autowired
    NodeDao nodeDao;

    @Autowired
    WeatherDao weatherDao;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    GeneralSettingDao generalSettingDao;

    @Autowired
    DataHandler dataHandler;

    public MonitoringCenterServiceImpl() {
    }

    /**
     * 先通过条件查询出设备  在通过设备在缓存中获取数据，进行排序
     */
    @Override
    public PageInfo<NodeDataVo> findMonitoringData(MonitorCondition monitorCondition) {
        //设备数据集
        List<NodeDataVo> nodeDataVos = new ArrayList<>();
        int pageNum = monitorCondition.getPage();
        int pageSize = monitorCondition.getSize();

        //判断位置路由是否为空
        if (!ToolUtil.isEmpty(monitorCondition.getPidRoute())){
            //拼接真实的位置路由
            monitorCondition.setPidRoute(monitorCondition.getPidRoute()+",[0"+monitorCondition.getPidRoute().split(",").length+":"+monitorCondition.getSiteId()+"]");
        }

        List<NodeVo> nodeVos;
        //判断是否包含仓间的温湿度设备
        if (NodeConstant.NODE_WARE_AREA.equals(monitorCondition.getSiteType())&& Arrays.asList(monitorCondition.getNodeTypes()).contains(NodeConstant.HUMITURE_NODE_TYPE_VALUE)){
            //通过条件查询对应的所有设备（采集位置、采集位置类型（仓间、非仓间）、设备类别（可以多个）、物料代码）
            //查询 温湿度设备
            nodeVos = nodeDao.findHumitureDevice(monitorCondition);
            //TODO 空调
            //查询空调设备
//            nodeVos.addAll(nodeDao.findAirConditioner(monitorCondition));
            //再排序
            nodeVos = nodeVos.stream().sorted(Comparator.comparing(NodeVo::getSiteRoute)).collect(Collectors.toList());
        }else{
            //通过条件查询对应的所有设备（采集位置、采集位置类型（仓间、非仓间）、设备类别（可以多个）、物料代码）
            nodeVos = nodeDao.findNodeByMonitorCondition(monitorCondition);
        }

        //添加筛选完后的数据
        nodeDataVos =setNodeDataVos(nodeDataVos, nodeVos, monitorCondition.getMonitorStatus());

        Stream<NodeDataVo> stream =nodeDataVos.stream();
        if (monitorCondition.getSortCondition() == 1){
            //按预警等级排序
            stream = stream.sorted(Comparator.comparing(NodeDataVo::getAlertLevel).reversed());
        }
        List<NodeDataVo> collect = stream.collect(Collectors.toList());

        //向缓存中添加预警颜色
        Map entries = putCacheWarningColor();

        //所有设备
        for (int i = 0; i < collect.size(); i++) {
            collect.get(i).setWarningName();
            //设置预警颜色
            NodeServiceImpl.setWarningColor(collect.get(i),entries);
            //如果物料等级不为空
            String materialCode = collect.get(i).getMaterialCode();
            if (ToolUtil.isNotEmpty(materialCode)){
                collect.get(i).setMaterialCode(materialCode.replace(",","、"));
            }
            //温湿度设备额外对添加温度湿度预警颜色
            if(NodeConstant.HUMITURE_NODE_TYPE_VALUE.equals(collect.get(i).getType())){
                if (ToolUtil.isEmpty(collect.get(i).getTempLevel())){
                    collect.get(i).setTempLevel(0);
                }
                if (collect.get(i).getTempLevel()==1){
                    collect.get(i).setTempColor(entries.get("one").toString());
                }else if (collect.get(i).getTempLevel()==2){
                    collect.get(i).setTempColor(entries.get("two").toString());
                }else if (collect.get(i).getTempLevel()==3){
                    collect.get(i).setTempColor(entries.get("three").toString());
                }else {
                    collect.get(i).setTempColor("");
                }
                if (ToolUtil.isEmpty(collect.get(i).getHumidityLevel())){
                    collect.get(i).setHumidityLevel(0);
                }
                if (collect.get(i).getHumidityLevel()==1){
                    collect.get(i).setHumidityColor(entries.get("one").toString());
                }else if (collect.get(i).getHumidityLevel()==2){
                    collect.get(i).setHumidityColor(entries.get("two").toString());
                }else if (collect.get(i).getHumidityLevel()==3){
                    collect.get(i).setHumidityColor(entries.get("three").toString());
                }else {
                    collect.get(i).setHumidityColor("");
                }
            }
        }
        List<NodeDataVo> list = new ArrayList();

        //判断是否是第一页且包含温湿度设备  是的话需要添加天气数据
        if (pageNum==1&&Arrays.asList(monitorCondition.getNodeTypes()).contains(NodeConstant.HUMITURE_NODE_TYPE_VALUE)&&NodeConstant.NODE_WARE_AREA.equals(monitorCondition.getSiteType())){
            pageSize++;
            WeatherDataVo nowWeather = findNowWeather();
            nowWeather.setHumiture(nowWeather.getHumidity());
            list.add(nowWeather);
        }
//        collect.forEach(nodeDataVo -> {
//            if (nodeDataVo.getAlertLevel()>0){
//                Integer count = earlyWarningDao.findUndoStatusByNodeId(nodeDataVo.getNodeId());
//                if (count>0){
//                    nodeDataVo.setTaskStatus(0);
//                }else {
//                    nodeDataVo.setTaskStatus(1);
//                }
//            }
//        });
        list.addAll(collect);


        Page<NodeDataVo> page = ManuallyPageUtil.manuallyPage(list, pageNum, pageSize);


        return new PageInfo<>(page);
    }

    /**
     * 查询故障设备
     * @return 预警设备列表
     */
    @Override
    public List<EarlyWarningVo> findTroubleNode() {
        List<EarlyWarningVo> earlyWarningVos = earlyWarningDao.findTroubleNode();
        return earlyWarningVos;
    }

    /**
     * 查询预警升级阈值
     */
    @Override
    public Map<String, String> findWarningThreshold() {
        HashMap<String, String> thresholdMap = new HashMap<>();
        //缓存中获取预警升级阈值
        Object oneToTwo = redisTemplate.opsForValue().get(CACHE_KEY_REDIS_MAINTAIN+"oneToTwo");
        Object twoToThree = redisTemplate.opsForValue().get(CACHE_KEY_REDIS_MAINTAIN+"twoToThree");
        if (ToolUtil.isEmpty(oneToTwo)||ToolUtil.isEmpty(twoToThree)) {
            //查询通用表的数据
            List<String> thresholds = earlyWarningDao.findWarningThreshold("warningThreshold");
            oneToTwo = thresholds.get(0);
            redisTemplate.opsForValue().set(CACHE_KEY_REDIS_MAINTAIN+"oneToTwo", oneToTwo);
            twoToThree = thresholds.get(1);
            redisTemplate.opsForValue().set(CACHE_KEY_REDIS_MAINTAIN+"twoToThree", twoToThree);
        }

        thresholdMap.put("oneToTwo", oneToTwo.toString());
        thresholdMap.put("twoToThree", twoToThree.toString());
        return thresholdMap;
    }

    /**
     * 查询预警值
     * @param nodeData
     * @return
     */
    @Override
    public Map<String,String> findAlarmValue(NodeData nodeData) {
        //该设备需要预警的字段
        String[] column = AlarmValueSettingVo.getColumn(nodeData.getType(), nodeData.getSiteType());
        //通过这个字段查询缓存中这个养护计划阶段的预警规则是否存在
        Map<String,String> map = new HashMap<>();
        //如果是仓间设备
        if (NodeConstant.NODE_WARE_AREA.equals(nodeData.getSiteType())){
            //将位置路由转成map 然后再拼接kv 作为redis的对象名
            List<String> hashKeys = dataHandler.getHashKeys(nodeData.getSiteRoute());
            //判断缓存中存在该对象名 存在 获取 不存在 从数据库中向上查找 存到缓存中 并返回对象名
            String redisHashKey = dataHandler.putAlarmValueToCache(hashKeys, column[0]);
            //获取这些字段的预警值
            setWarningColumn(column,map,redisHashKey);
        }else {
            Boolean hasKey = redisTemplate.opsForHash().hasKey(CACHE_KEY_REDIS_MAINTAIN + nodeData.getMaintainStageId(), column[0]);
            //如果不存在
            if (!hasKey) {
                //数据库获取
                AlarmValueSettingVo alarmValueSettingVo = alarmValueSettingDao.findByMaintainStageId(nodeData.getMaintainStageId());
                //获取所有的字段
                Field[] fields = AlarmValueSettingVo.class.getDeclaredFields();
                for (Field field : fields) {
                    try {
                        //把每个字段的值存到redis中  对象为MAINTAIN_加养护阶段id主键 ，key为字段名 value为字段值
                        //强制反射
                        if (!field.getName().contains("ware")){
                            field.setAccessible(true);
                            redisTemplate.opsForHash().put("MAINTAIN_" + nodeData.getMaintainStageId(), field.getName(), field.get(alarmValueSettingVo));
                        }
                    } catch (IllegalAccessException exception) {
                        exception.printStackTrace();
                    }
                }
            }
            //获取这些字段的预警值
            setWarningColumn(column,map,CACHE_KEY_REDIS_MAINTAIN  + nodeData.getMaintainStageId());
        }
        GeneralSetting generalSetting = new GeneralSetting();
        generalSetting.setSettingKey("color");
        List<GeneralSetting> generalSettings = generalSettingDao.findGeneralSetting(generalSetting);
        for (int i = 0; i <generalSettings.size() ; i++) {
            map.put(generalSettings.get(i).getSettingName(),generalSettings.get(i).getSettingValue());
        }
        return map;
    }

    /**
     * 设置预警等级
     * @param nodeDataVos 设备数据集
     * @param nodeVos  设备集
     * @param status 状态
     * @return
     */
    public List<NodeDataVo> setNodeDataVos(List<NodeDataVo> nodeDataVos , List<NodeVo> nodeVos ,Integer status) {
        for (NodeVo nodeVo : nodeVos) {
            //TODO 获取缓存中该设备的数据 空调数据暂时没有 sql暂用只找仓间温湿度的数据
            Object data = redisTemplate.opsForValue().get(CACHE_KEY_REDIS_MAINTAIN+nodeVo.getNodeId());

            NodeDataVo nodeDataVo;
            if (!ToolUtil.isEmpty(data)){
                nodeDataVo = JSON.parseObject(data.toString(), NodeDataVo.class);
                NodeData nodeData =JSON.parseObject(data.toString(), NodeData.class);
                if (!ToolUtil.isEmpty(nodeData.getFrequency())){
                    try {
                        //传输时间的时间戳
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                        long time = sdf.parse(nodeDataVo.getTransferTime()).getTime();
                        //当前时间和传输时间的差值
                        long intervalTime = System.currentTimeMillis() -time;
                        //根据频率求出丢包个数
                        if (intervalTime/(nodeData.getFrequency()*1000)>0){
                            if (ToolUtil.isEmpty(nodeDataVo.getWarningType())){
                                nodeDataVo.setWarningType(WarningTypeConstant.NODE_PACKET_LOSS_BREAKDOWN);
                            }else {
                                if (!nodeDataVo.getWarningType().contains(WarningTypeConstant.NODE_PACKET_LOSS_BREAKDOWN)){
                                    nodeDataVo.setWarningType(nodeDataVo.getWarningType()+","+WarningTypeConstant.NODE_PACKET_LOSS_BREAKDOWN);
                                }
                            }
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                nodeDataVos.add(nodeDataVo);
            }else {
                //如果设备数据不在缓存中
                nodeDataVo = nodeDao.findNodeNowData(nodeVo.getNodeId(), NodeTypeEnum.getNodeType(nodeVo.getNodeType()).getTableName());
                if (!ToolUtil.isEmpty(nodeDataVo)){
                    //将该数据存放到缓存中
                    redisTemplate.opsForValue().set(CACHE_KEY_REDIS_MAINTAIN+nodeVo.getNodeId(),JSONObject.toJSONString(nodeDataVo));
                    nodeDataVos.add(nodeDataVo);
                }else {
                    nodeDataVo = new NodeDataVo();
                    nodeDataVo.setNodeId(nodeVo.getNodeId());
                    nodeDataVo.setType(nodeVo.getNodeType());
                    nodeDataVo.setAlertLevel(0);
                    nodeDataVo.setGatherSite(nodeVo.getGatherSite());
                    nodeDataVo.setTransferTime("--");
                    nodeDataVos.add(nodeDataVo);
                    continue;
                }

            }


        }
        //监控状态筛选 1.预警  2.正常
        if (status==2){
            nodeDataVos = nodeDataVos.stream().filter(nodeDataVo ->
                    nodeDataVo.getAlertLevel() ==0
            ).collect(Collectors.toList());
        }else if (status==1){
            nodeDataVos = nodeDataVos.stream().filter(nodeDataVo ->
                    nodeDataVo.getAlertLevel() >0
            ).collect(Collectors.toList());
        }

        return nodeDataVos;
    }

    /**
     * 查询当前天气数据
     * @return
     */
    @Override
    public WeatherDataVo findNowWeather() {
        //从缓存中获取
        Object o = redisTemplate.opsForValue().get("MAINTAIN_WEATHER");
        //如果缓存中不存在
        if (ToolUtil.isEmpty(o)){
            //从数据库中获取
            WeatherDataVo weatherDataVo = weatherDao.findNowWeather();
            //存放到缓存中
            redisTemplate.opsForValue().set("MAINTAIN_WEATHER", JSONObject.toJSONString(weatherDataVo));
            return weatherDataVo;
        }
        //类型转换
        WeatherDataVo weatherDataVo = JSONObject.parseObject(o.toString(),WeatherDataVo.class);
        return weatherDataVo;
    }

    /**
     * 查询天气历史数据
     * @param monitorCondition
     * @return
     */
    @Override
    public List<WeatherDataVo> findWeatherData(MonitorCondition monitorCondition) {
        List<WeatherDataVo> weatherData = weatherDao.findWeatherData(monitorCondition);
        weatherData.forEach(weatherDataVo -> {
            weatherDataVo.setTransferTime(weatherDataVo.getCreatedTime());
            weatherDataVo.setHumiture(weatherDataVo.getHumidity());
        });
        return weatherData;
    }

    /**
     * 查询天气数据 按天统计
     * @param monitorCondition
     * @return
     */
    @Override
    public List<HashMap<String, String>> findWeatherDataByDay(MonitorCondition monitorCondition) {
        List<WeatherDataVo> weatherDataVos = weatherDao.findWeatherData(monitorCondition);

        Map<String, List<WeatherDataVo>> collect = weatherDataVos.stream().collect(Collectors.groupingBy((item) -> (
                item.getCreatedTime().split(" ")[0])));//截取空格前的数据 然后过滤
        //转成treeMap
        TreeMap<String, List<WeatherDataVo>> paramTreeMap = new TreeMap(collect);
        List<HashMap<String, String>> historyGraphData = new ArrayList<>();
        paramTreeMap.forEach((k, value) -> {//过滤完后 求最大 最小 平均值
            HashMap<String, String> map = new HashMap();
            //温度最大值
            map.put("max", value.stream().map(WeatherDataVo::getTemp).max(Double::compareTo).get()+"");
            //温度最小值
            map.put("min", value.stream().map(WeatherDataVo::getTemp).min(Double::compareTo).get()+"");
            //温度平均值
            map.put("avg", value.stream().mapToDouble(WeatherDataVo::getTemp).average().getAsDouble()+"");
            //湿度最大值
            map.put("humitureMax", value.stream().map(WeatherDataVo::getHumidity).max(Double::compareTo).get()+"");
            //湿度最小值
            map.put("humitureMin", value.stream().map(WeatherDataVo::getHumidity).min(Double::compareTo).get()+"");
            //湿度平均值
            map.put("humitureAvg", value.stream().mapToDouble(WeatherDataVo::getHumidity).average().getAsDouble()+"");
            map.put("date",k);
            map.put("humitureDate",k);
            historyGraphData.add(map);
        });
        return historyGraphData;
    }

    /**
     * 向缓存添加预警颜色
     * @return
     */
    @Override
    public Map putCacheWarningColor(){
        Map entries = redisTemplate.opsForHash().entries(GENERAL_SETTING_VALUE + "COLOR");
        if (ToolUtil.isEmpty(entries)){
            GeneralSetting generalSetting = new GeneralSetting();
            generalSetting.setSettingKey("color");
            List<GeneralSetting> generalSettings = generalSettingDao.findGeneralSetting(generalSetting);
            entries = new HashMap();
            entries.put("one",generalSettings.get(0).getSettingValue());
            entries.put("two",generalSettings.get(1).getSettingValue());
            entries.put("three",generalSettings.get(2).getSettingValue());
        }
        return entries;
    }

    /**
     * 给map设置统一的返回键one two three 代表预警等级颜色  upper1 upper2 upper3 lower代表预警阈值 upperTwo1 代表湿度1一级阈值
     * @param column
     * @param map
     * @param redisHashKey
     */
    public void setWarningColumn(String[] column ,Map<String,String> map,String redisHashKey){
        for (int i = 0; i < column.length; i++) {
            if (i<4){
                String value = "upper"+(3-i);
                value = value.equals("upper0")? "lower" :value;
                map.put(value,Objects.requireNonNull(redisTemplate.opsForHash().get(redisHashKey, column[i]))+"");
            }else {
                String value = "upperTwo"+(7-i);
                value = value.equals("upperTwo0")? "lowerTwo" :value;
                map.put(value,Objects.requireNonNull(redisTemplate.opsForHash().get(redisHashKey, column[i]))+"");
            }
        }
    }
}

