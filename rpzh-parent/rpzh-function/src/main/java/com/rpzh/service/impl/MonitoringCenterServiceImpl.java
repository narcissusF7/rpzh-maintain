package com.rpzh.service.impl;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rpzh.constant.NodeConstant;
import com.rpzh.dao.AlarmValueSettingDao;
import com.rpzh.dao.EarlyWarningDao;
import com.rpzh.dao.NodeDao;

import com.rpzh.entity.ToolUtil;
import com.rpzh.pojo.AlarmValueSetting;
import com.rpzh.pojo.NodeData;
import com.rpzh.service.MonitoringCenterService;
import com.rpzh.util.MonitorCondition;
import com.rpzh.vo.*;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


@Service
public class MonitoringCenterServiceImpl implements MonitoringCenterService {
    private static final String CACHE_KEY_REDIS_MAINTAIN = "MAINTAIN_";
    @Autowired
    AlarmValueSettingDao alarmValueSettingDao;
    @Autowired
    EarlyWarningDao earlyWarningDao;
    @Autowired
    NodeDao nodeDao;
    @Autowired
    RedisTemplate redisTemplate;

    public MonitoringCenterServiceImpl() {
    }

    /**
     * 先通过条件查询出设备  在通过设备在缓存中获取数据，如果物料代码不为空，在进行过滤 ，最后进行排序排序
     */
    @Override
    public PageInfo<NodeDataVo> findMonitoringData(MonitorCondition monitorCondition) {
        //设备数据集
        List<NodeDataVo> nodeDataVos = new ArrayList<>();
        int pageNum = monitorCondition.getPage();
        int pageSize = monitorCondition.getSize();

        WeatherDataVo weatherDataVo;

        if (monitorCondition.getSortCondition() == 0) {
            //如果不是仓间温湿度设备并且正常排序 相当于在查找设备时凭借上limit
            PageHelper.startPage(pageNum, pageSize);
        }
        List<NodeVo> nodeVos;
        //判断是否包含仓间的温湿度设备
        if (NodeConstant.NODE_WARE_AREA.equals(monitorCondition.getSiteType())&& Arrays.asList(monitorCondition.getNodeTypes()).contains(NodeConstant.HUMITURE_NODE_TYPE_VALUE)){
            //通过条件查询对应的所有设备（采集位置、采集位置类型（仓间、非仓间）、设备类别（可以多个）、物料代码）
            monitorCondition.setPidRoute(monitorCondition.getPidRoute()+",[0"+monitorCondition.getLevel()+":"+monitorCondition.getSiteId()+"]");
            //查询 温湿度设备包括空调
            nodeVos = nodeDao.findHumitureDevice(monitorCondition);
        }else{
            //通过条件查询对应的所有设备（采集位置、采集位置类型（仓间、非仓间）、设备类别（可以多个）、物料代码）
            nodeVos = nodeDao.findNodeByMonitorCondition(monitorCondition);
        }

        nodeDataVos.addAll(setNodeDataVos(nodeDataVos, nodeVos, monitorCondition.getMonitorStatus()));

        //如果设备时按预警排序  需要手动分页
        if (monitorCondition.getSortCondition() == 1) {
            //排序  按预警等级排序
            nodeDataVos = nodeDataVos.stream().sorted(Comparator.comparing(NodeDataVo::getAlertLevel).reversed()).collect(Collectors.toList());
            //创建一个page类
            Page<NodeDataVo> page = new Page<>(pageNum, pageSize);
            //总数量
            int total = nodeDataVos.size();
            page.setTotal(total);
            //起始下标
            int startIndex = (pageNum - 1) * pageSize;
            //终止下标
            int endIndex = Math.min(startIndex + pageSize, total);
            //截取数量
            page.addAll(nodeDataVos.subList(startIndex, endIndex));
            return new PageInfo<>(page);
        } else {
            return new PageInfo<>(nodeDataVos);
        }
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
     * 修改预警升级阈值
     * @param thresholdMap  oneToTwo:? ,towToThree:?
     */
    @Override
    @Transactional
    public Integer updateWarningThreshold(Map<String, Integer> thresholdMap) {
        AtomicInteger flag = new AtomicInteger();
        thresholdMap.keySet().forEach((key) -> flag.addAndGet(earlyWarningDao.updateWarningThreshold(thresholdMap.get(key), key)));
        //删除掉缓存中的预警升级阈值
        redisTemplate.delete(CACHE_KEY_REDIS_MAINTAIN+"oneToTwo");
        redisTemplate.delete(CACHE_KEY_REDIS_MAINTAIN+"TwoToThree");
        return flag.get();
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
            List<String> thresholds = earlyWarningDao.findWarningThreshold("settingKey");
            oneToTwo = thresholds.get(0);
            redisTemplate.opsForValue().set(CACHE_KEY_REDIS_MAINTAIN+"oneToTwo", oneToTwo);
            twoToThree = thresholds.get(1);
            redisTemplate.opsForValue().set(CACHE_KEY_REDIS_MAINTAIN+"twoToThree", twoToThree);
        }

        thresholdMap.put("oneToTwo", oneToTwo.toString());
        thresholdMap.put("twoToThree", twoToThree.toString());
        return thresholdMap;
    }

    @Override
    public List<Double> findAlarmValue(NodeData nodeData) {
        //该设备需要预警的字段
        String[] column = AlarmValueSettingVo.getColumn(nodeData.getType(), nodeData.getSiteType());
        //通过这个字段查询缓存中这个养护计划阶段的预警规则是否存在
        Boolean hasKey = redisTemplate.opsForHash().hasKey(CACHE_KEY_REDIS_MAINTAIN + nodeData.getMaintainStageId(), column[0]);
        //如果不存在
        if (!hasKey) {
            //数据库获取
            AlarmValueSettingVo alarmValueSettingVo = alarmValueSettingDao.findByMaintainStageId(nodeData.getMaintainStageId());
            //获取所有的字段
            Field[] fields = AlarmValueSetting.class.getDeclaredFields();
            for (Field field : fields) {
                try {
                    //把每个字段的值存到redis中  对象为MAINTAIN_加养护阶段id主键 ，key为字段名 value为字段值
                    redisTemplate.opsForHash().put("MAINTAIN_" + nodeData.getMaintainStageId(), field.getName(), field.get(alarmValueSettingVo.toString()));
                } catch (IllegalAccessException exception) {
                    exception.printStackTrace();
                }
            }
        }
        List<Double> arrayList = new ArrayList<>();
        for (int i = 0; i < column.length; i++) {
            arrayList.add(Double.parseDouble(Objects.requireNonNull(redisTemplate.opsForHash().get(CACHE_KEY_REDIS_MAINTAIN+ + nodeData.getMaintainStageId(), column[0])).toString()));
        }
        return arrayList;
    }

    @Override
    public List<WeatherDataVo> findWeatherData(MonitorCondition monitorCondition) {
        return nodeDao.findWeatherData(monitorCondition);
    }

    @Override
    public Map<String, HashMap<String, Double>> findWeatherDataByDay(MonitorCondition monitorCondition) {
        List<WeatherDataVo> weatherDataVos = nodeDao.findWeatherData(monitorCondition);

        Map<String, List<WeatherDataVo>> collect = weatherDataVos.stream().collect(Collectors.groupingBy((item) -> (
                item.getCreatedTime().split(" ")[0])));
        Map<String, HashMap<String, Double>> historyGraphData = new HashMap();
        collect.forEach((k, value) -> {
            HashMap<String, Double> map = new HashMap();
            map.put("tempMax", value.stream().map(WeatherDataVo::getTemp).max(Double::compareTo).get());
            map.put("tempMin", value.stream().map(WeatherDataVo::getTemp).min(Double::compareTo).get());
            map.put("tempAvg", value.stream().mapToDouble(WeatherDataVo::getTemp).average().getAsDouble());
            map.put("humidityMax", value.stream().map(WeatherDataVo::getHumidity).max(Double::compareTo).get());
            map.put("humidityMin", value.stream().map(WeatherDataVo::getHumidity).min(Double::compareTo).get());
            map.put("humidityAvg", value.stream().mapToDouble(WeatherDataVo::getHumidity).average().getAsDouble());
            historyGraphData.put(k, map);
        });
        return historyGraphData;
    }

    public List<NodeDataVo> setNodeDataVos(List<NodeDataVo> nodeDataVos , List<NodeVo> nodeVos ,Integer status) {
        for (NodeVo nodeVo : nodeVos) {
            //获取缓存中该设备的数据
            Object data = redisTemplate.opsForValue().get(CACHE_KEY_REDIS_MAINTAIN+nodeVo.getNodeId());
            nodeDataVos.add(new ObjectMapper().convertValue(data, NodeDataVo.class));
        }
        //监控状态筛选 1.正常 2.预警
        if (status==1){
            nodeDataVos = nodeDataVos.stream().filter(nodeDataVo ->
                    nodeDataVo.getAlertLevel() ==0
            ).collect(Collectors.toList());
        }else if (status==2){
            nodeDataVos = nodeDataVos.stream().filter(nodeDataVo ->
                    nodeDataVo.getAlertLevel() >0
            ).collect(Collectors.toList());
        }

        return nodeDataVos;
    }
}

