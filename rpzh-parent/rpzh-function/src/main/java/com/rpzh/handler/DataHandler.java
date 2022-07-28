package com.rpzh.handler;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rpzh.constant.NodeConstant;
import com.rpzh.constant.WarningTypeConstant;
import com.rpzh.dao.*;
import com.rpzh.dto.TaskDto;
import com.rpzh.entity.ToolUtil;
import com.rpzh.enums.NodeTypeEnum;
import com.rpzh.parse.ParseData;
import com.rpzh.pojo.EarlyWarning;
import com.rpzh.pojo.NodeData;
import com.rpzh.service.MonitoringCenterService;
import com.rpzh.service.TaskManagementService;
import com.rpzh.service.impl.WarehouseServiceImpl;
import com.rpzh.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Stream;

/**
 * 数据处理类
 *
 * @Author liujj
 * @create 2022/4/30
 */
@Component
public class DataHandler {

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    AlarmValueSettingDao alarmValueSettingDao;

    @Autowired
    EarlyWarningDao earlyWarningDao;

    @Autowired
    NodeDao nodeDao;

    @Autowired
    MonitoringCenterService monitoringCenterService;

    @Autowired
    WareAreaAlarmSettingDao wareAreaAlarmSettingDao;

    @Autowired
    MaintainPlanDao maintainPlanDao;

    @Autowired
    WarehouseServiceImpl warehouseService;

    @Autowired
    TaskManagementService taskManagementService;

    @Value("${node.voltage}")
    Double voltage;
    @Value("${node.signalStrength}")
    Integer signalStrength;

    String warningType = "";


    public static String MAINTAIN = "MAINTAIN_";
    public static String ALERT_NUM = "ALERT_NUM_";
    public static String MAINTAIN_CACHE_NODE = "MAINTAIN_CACHE_NODE_";
    public static String FAULT = "FAULT_";

    /**
     * 设备推送数据处理
     * 设备数据 redis存放规则 redisTemplate.opsForValue().set(MAINTAIN + nodeVo.getNodeId(), nodeData);
     * 仓间及仓间以上的预警值 redis存放规则 redisTemplate.opsForHash().put(MAINTAIN + hashKey, columnName, columnValue);  hashkey =MAINTAIN + 层级（01，02，03，04）+ 对应的层级id
     * 垛内预警值 redis存放规则 redisTemplate.opsForHash().put(MAINTAIN + maintainStageId, columnName, columnValue);
     * 连续预警次数  redis存放规则
     * @param parseData
     */
    /**
     * 用来处理空调数据  暂时还没用
     * @param parseData
     * @param siteRoute
     */
    public void handlerData(ParseData parseData ,String siteRoute) {
        //该设备需要预警的字段
        String[] alarmColumns = AlarmValueSettingVo.getColumn(NodeConstant.NODE_WARE_AREA,NodeConstant.HUMITURE_NODE_TYPE_VALUE);

        //将位置路由转成map 后 key value 拼接
        List<String> hashKeys = getHashKeys(siteRoute);
        //redis中的对象名
        String  redisHashKey = putAlarmValueToCache(hashKeys,alarmColumns[0]);

        //从缓存中获取预警阈值
        List<Double> list = new ArrayList<>();
        for (String alarmColumn : alarmColumns) {
            list.add(Double.parseDouble((String) redisTemplate.opsForHash().get(redisHashKey, alarmColumn)));
        }

        NodeData nodeData = new NodeData();


        //温度数据
        Double data = parseData.getData().get(0);
        //判断当前数据所属等级
        int level = decideValue(data, list);
        level = level==-1 ? 1:level;
        //siteRoute 例[00:0],[01:2],[02:1],[03:1],[04:1]
        Integer areaId = Integer.parseInt(hashKeys.get(4).substring(2));
        //温度处理
        level =earlyWarningHandle(level, parseData,"temp",areaId);

        nodeData.setAlertLevel(level);
        //湿度数据
        data = parseData.getData().get(1);

        //湿度数据等级
        level = decideValue(data, list.subList(4, 8));
        level = level==-1 ? 1:level;
        //湿度处理
        level = earlyWarningHandle(level, parseData, "humidity", areaId);
        if (nodeData.getAlertLevel()<level){
            nodeData.setAlertLevel(level);
        }
        //给nodeData赋值
        nodeData.setNodeId(parseData.getNodeId());
        nodeData.setType(parseData.getNodeType());
        nodeData.setTransferTime(parseData.getDateTime().get(0));
        nodeData.setTemp(parseData.getData().get(0));
        nodeData.setHumiture(parseData.getData().get(1));
        nodeData.setSiteType(NodeConstant.NODE_WARE_AREA);
        nodeData.setPidRoute(siteRoute);
        nodeData.setWarningType(warningType.substring(1));
        //向数据库插入当前数据
        Integer insertFlag = nodeDao.insertNodeHistory(nodeData, "tb_air_conditioner_history");

        //将该数据放入到缓存中
        redisTemplate.opsForValue().set(MAINTAIN + parseData.getNodeId(), JSON.toJSONString(nodeData));


    }

    /**
     * 正常数据处理
     * @param parseData
     */
    public void handlerData(ParseData parseData) {
        //通过设备id找到对应的设备
        Object o = redisTemplate.opsForValue().get(MAINTAIN_CACHE_NODE + parseData.getNodeId());

        NodeVo nodeVo = getNodeVo(o,parseData.getNodeId());

        //如果设备处于是解垛状态  数据不做任何处理
        if (nodeVo.getStatus().equals("0")){
            return;
        }


        warningType = "";
        //判断电量
        decideFault(parseData.getVoltage(),voltage,WarningTypeConstant.NODE_LOW_BATTERY_BREAKDOWN,parseData);

        //判断信号强度
        decideFault(parseData.getSignalStrength().doubleValue(),signalStrength.doubleValue(),WarningTypeConstant.NODE_SIGNAL_STRENGTH_BREAKDOWN,parseData);


        //该设备需要预警的字段
        String[] alarmColumns = AlarmValueSettingVo.getColumn(nodeVo.getNodeType(), nodeVo.getSiteType());

        String redisHashKey;

        if(NodeConstant.NODE_WARE_AREA.equals(nodeVo.getSiteType())){//如果是仓间设备

            //将位置路由转成map 后 key value 拼接
            List<String> hashKeys = getHashKeys(nodeVo.getSiteRoute());
            //redis中的对象名
            redisHashKey = putAlarmValueToCache(hashKeys,alarmColumns[0]);
        }else {//垛内设备

            List<String> hashKeys = Arrays.asList(nodeVo.getMaintainStageId().toString());

            redisHashKey = putAlarmValueToCache(hashKeys, alarmColumns[0], nodeVo.getAlermValueSettingId());

        }

        //从缓存中获取预警阈值
        List<Double> list = new ArrayList<>();
        for (String alarmColumn : alarmColumns) {
            list.add((Double) redisTemplate.opsForHash().get(redisHashKey, alarmColumn));
        }

        //根据丢包个数判断需要循环几次
        int loopCount = parseData.getPacketLossNum() > 2 ? 2 : parseData.getPacketLossNum();

        //根据丢包个数处理
        for (int i = loopCount; i >= 0; --i) {
            Double data;
            //判断设备类型
            //设备如果是温湿度设备
            if (NodeConstant.HUMITURE_NODE_TYPE_VALUE.equals(nodeVo.getNodeType())) {
                //根据丢包个数  丢2个以上的包是从第三条数据开始
                //温度数据
                data = parseData.getData().get(i);
                //判断当前数据所属等级
                int tempLevel = decideValue(data, list);
                tempLevel = tempLevel==-1 ? 1:tempLevel;
                //温度处理
                earlyWarningHandle(nodeVo, tempLevel, parseData, data, "temp");
                //设置温度预警等级
                nodeVo.setTempLevel(tempLevel);
                //湿度数据
                data = parseData.getData().get(i + 3);

                //湿度数据等级
                int humidityLevel = decideValue(data, list.subList(4, 8));
                humidityLevel = humidityLevel==-1 ? 1:humidityLevel;
                //湿度处理
                earlyWarningHandle(nodeVo, humidityLevel, parseData, data, "humidity");
                //设置湿度预警等
                nodeVo.setHumidityLevel(humidityLevel);
                //设置该设备预警等级
                nodeVo.setAlertLevel(tempLevel>humidityLevel?tempLevel:humidityLevel);

                //如果是其他设备
            } else {
                data = parseData.getData().get(0);
                //判断当前数据所属等级
                int level = decideValue(data, list);
                level = level==-1 ? 1:level;
                //处理该设备数据
                earlyWarningHandle(nodeVo, level, parseData, data);
            }
            //将需要存到数据库中的数据放到NodeData中
            NodeData nodeData = new NodeData(parseData, nodeVo, i);
            if (!ToolUtil.isEmpty(warningType)){
                nodeData.setWarningType(warningType.substring(1));
            }
            //向数据库插入当前数据
            Integer insertFlag = nodeDao.insertNodeHistory(nodeData, NodeTypeEnum.getNodeType(nodeVo.getNodeType()).getTableName());

            //最后一条数据需要存放到redis中
            if (i==0){
                redisTemplate.opsForValue().set(MAINTAIN + nodeVo.getNodeId(),JSON.toJSONString(nodeData));
            }
        }

        //如果设备时包芯温度设备时 需要判断垛位平均温度是否正常
        if (NodeConstant.TEMPERATURE_CORE_CHIP_NODE_TYPE_VALUE.equals(nodeVo.getNodeType())) {

            //查询该垛位下的所有设备
            List<NodeVo> nodeVos = nodeDao.findNodeByStackId(nodeVo.getStackId());

            //过滤 选择包芯设备 取温度
            double avg = getAvg(nodeVos,NodeConstant.TEMPERATURE_CORE_CHIP_NODE_TYPE_VALUE);

            //过滤 选择环境温湿度设备 取温度
            double evnTemp = getAvg(nodeVos,NodeConstant.HUMITURE_NODE_TYPE_VALUE);
            //计算插值
            double v = avg - evnTemp;

            //获取缓存中的对应字段的值
            alarmColumns = AlarmValueSettingVo.getStackColumn();
            list.clear();
            for (String alarmColumn : alarmColumns) {
                list.add((Double) redisTemplate.opsForHash().get(redisHashKey, alarmColumn));
            }
            //判断等级
            int level = decideValue(v, list);

            //处理
            avgCoreHandle(nodeVo, level, parseData, v);
        }

    }

    /**
     * 向缓存中存放预警值
     * @param hashKeys 位置id和位置类型（位置类型,01:仓库, 02: 楼号, 03: 层, 04: 区）拼接的集合
     * @param alarmColumn 字段名称（用来判断缓存是否存在）
     * @return
     */
    public String putAlarmValueToCache(List<String> hashKeys,String alarmColumn){
        return putAlarmValueToCache(hashKeys,alarmColumn,-1);
    }
    public String putAlarmValueToCache(List<String> hashKeys,String alarmColumn,Integer alarmId){

        for (int i = hashKeys.size()-1; i >=0 ; i--) {
            //判断缓存是否存在
            Boolean hasKey = redisTemplate.opsForHash().hasKey(MAINTAIN + hashKeys.get(i), alarmColumn);
            if (hasKey) {
                return MAINTAIN + hashKeys.get(i);
            }else {
                //不存在就从数据库中查找
                AlarmValueSettingVo alarmValueSettingVo;
                if(alarmId==-1){
                    alarmValueSettingVo= wareAreaAlarmSettingDao.findBySite(hashKeys.get(i));
                }else {
                    alarmValueSettingVo = alarmValueSettingDao.findAlarmValueSettingById(alarmId);
                }


                //数据库中有就存在缓存中并跳出循环  没有就向上查找
                if (!ToolUtil.isEmpty(alarmValueSettingVo)){

                    //获取 AlarmValueSettingVo中所有的字段
                    Field[] fields = AlarmValueSettingVo.class.getDeclaredFields();
                    for (Field field : fields) {
                        try {
                            field.setAccessible(true);
                            //把每个字段的值存到redis中  对象为MAINTAIN_加养护阶段id主键 ，key为字段名 value为字段值
                            if (field.get(alarmValueSettingVo)!=""||field.get(alarmValueSettingVo)!=null){
                                redisTemplate.opsForHash().put(MAINTAIN + hashKeys.get(i), field.getName(), field.get(alarmValueSettingVo));
                            }
                        } catch (IllegalAccessException exception) {
                            exception.printStackTrace();
                        }
                    }
                    return MAINTAIN + hashKeys.get(i);
                }
            }
        }
        return MAINTAIN + hashKeys.get(0);
    }

    /**
     * 查询该设备在预警记录表中该类型的预警记录是否存在 不存在就新增一条数据
     *
     * @param parseData     传输的数据
     * @param warningType   预警类型
     * @param warningValOne 一级预警的值
     */
    public void nodeMalfunction(ParseData parseData, String warningType, Double warningValOne) {
        //查询该设备在预警记录表中该类型的预警记录是否存在
        Integer count = earlyWarningDao.findUndoCount(parseData.getNodeId(), warningType);
        if (count < 1) {
            //获取缓存中该设备该预警类型的次数
            Object o = redisTemplate.opsForHash().get(FAULT + warningType, parseData.getNodeId());
            if (!ToolUtil.isEmpty(o)){
                //如果缓存中存在次数
                if ((Integer) o ==2){
                    //达到3次新增一条数据
                    String siteRoute = nodeDao.findByNodeId(parseData.getNodeId()).getSiteRoute();
                    EarlyWarning earlyWarning = new EarlyWarning(warningType, parseData.getNodeId(), parseData.getNodeType(), warningValOne, 1,siteRoute);
                    earlyWarningDao.insertWarning(earlyWarning);
                    redisTemplate.opsForHash().put(FAULT + warningType, parseData.getNodeId(),0);
                }else {
                    //否则次数加1
                    redisTemplate.opsForHash().put(FAULT + warningType, parseData.getNodeId(),(Integer) o+1);
                }
            }else {
                redisTemplate.opsForHash().put(FAULT + warningType, parseData.getNodeId(),1);
            }

        }
    }

    public int decideValue(Double value, List<Double> list) {
        //判断等级
        int level = 0;
        if (value > list.get(0)) {
            level = 3;
        } else if (value > list.get(1)) {
            level = 2;
        } else if (value > list.get(2)) {
            level = 1;
        } else if (list.size() > 3 && value < list.get(3)) {
            level = -1;
        }
        return level;
    }

    //设置等级
    public void setLevel(EarlyWarning earlyWarning, Integer level, Double value) {
        earlyWarning.setWarningGrade(level);
        switch (level) {
            case 1:
                earlyWarning.setWarningValOne(value);
                break;
            case 2:
                earlyWarning.setWarningValTwo(value);
                break;
            case 3:
                earlyWarning.setWarningValThree(value);
                break;
        }
    }

    //非温湿度设备
    public void earlyWarningHandle(NodeVo nodeVo, Integer level, ParseData parseData, Double data) {
        earlyWarningHandle(nodeVo, level, parseData, data, "");
    }

    /**
     * 处理空调数据
     * @param level
     * @param parseData
     * @param evnType
     * @param areaId
     */
    public Integer earlyWarningHandle(Integer level, ParseData parseData,String evnType,Integer areaId) {
        String warningType = WarningTypeConstant.getWarningType(NodeConstant.NODE_WARE_AREA,NodeConstant.HUMITURE_NODE_TYPE_VALUE,evnType);
        //查询数据库中该空调未处理的预警
        EarlyWarningVo earlyWarningVo = earlyWarningDao.findUndoEarlyWarningByNodeId(parseData.getNodeId(),warningType);

        EarlyWarning earlyWarning = new EarlyWarning();
        earlyWarning.setId(areaId);

        level = decideEarlyWarning(earlyWarningVo, earlyWarning, parseData, level, parseData.getData().get(0), evnType, NodeConstant.NODE_WARE_AREA);

        return level;
    }


    /**
     * 数据预警分析
     *
     * @param nodeVo    当前设备
     * @param level     当前预警等级
     * @param parseData 传感器发送的数据
     * @param data      最新数据的值
     * @param evnType   温湿度类型 用来判断是否为环境温湿度设备
     */
    public void earlyWarningHandle(NodeVo nodeVo, Integer level, ParseData parseData, Double data, String evnType) {

        String warningType = WarningTypeConstant.getWarningType(nodeVo.getSiteType(), nodeVo.getNodeType(), evnType);
        //查询数据库中该设备未处理的预警
        EarlyWarningVo earlyWarningVo = earlyWarningDao.findUndoEarlyWarningByNodeId(nodeVo.getNodeId(),warningType);

        EarlyWarning earlyWarning = new EarlyWarning();

        //判断是否是仓间设备
        if (NodeConstant.NODE_WARE_AREA.equals(nodeVo.getSiteType())) {
            //截取位置路由   位置路径,00:根节点,01:仓库, 02: 楼号, 03: 层, 04: 区, 05: 垛位
            String s = nodeVo.getSiteRoute().split("04:")[1].split("]")[0];
            //设置所在仓间
            earlyWarning.setAreaId(Integer.parseInt(s));
        } else {
            //设置所在垛位
            earlyWarning.setStackId(nodeVo.getStackId());
        }

        decideEarlyWarning(earlyWarningVo,earlyWarning,parseData,level,data,evnType,nodeVo.getSiteType());

        nodeVo.setAlertLevel(level);

    }

    /**
     * 平均包芯
     * @param nodeVo 设备数据
     * @param level  预警等级
     * @param parseData 推送的数据
     * @param data 数据值
     */
    public void avgCoreHandle(NodeVo nodeVo, Integer level, ParseData parseData, Double data) {
        //查询数据库中该设备未处理的预警
        EarlyWarningVo earlyWarningVo = earlyWarningDao.findUndoEarlyWarningByNodeId(nodeVo.getNodeId(),WarningTypeConstant.CORE_TEMP_OVERRUN_STACK_WARNING);
        EarlyWarning earlyWarning = new EarlyWarning();
        earlyWarning.setNodeType(parseData.getNodeType());
        earlyWarning.setStackId(nodeVo.getStackId());
        NodeVo nodeVo1 = nodeDao.findByNodeId(nodeVo.getNodeId());
        String siteRoute = nodeVo1.getSiteRoute();
        earlyWarning.setSiteRoute(siteRoute);
        //如果存在未处理的预警
        if (!ToolUtil.isEmpty(earlyWarningVo)) {
            //如果当前的等级更高
            if (earlyWarningVo.getWarningGrade() < level) {
                setLevel(earlyWarning, level, data);
                earlyWarningDao.updateWarning(earlyWarning);
            }
        } else if (level > 0) {
            //如果不存在未处理的预警 且当前预警等级大于0则需要插入数据
            earlyWarning.setWarningType(WarningTypeConstant.CORE_TEMP_OVERRUN_STACK_WARNING);
            if (level==-1){
                addTask(nodeVo1,"19");
                setLevel(earlyWarning, 1, data);
            }else {
                addTask(nodeVo1,"13");
                setLevel(earlyWarning, level, data);
            }
            earlyWarningDao.insertWarning(earlyWarning);

        }

    }

    /**
     *  将位置路由转成map 再将map的键值对拼接后 用于作为redis中的key
     * @param siteRoute 位置路由
     * @return
     */
    public List<String> getHashKeys(String siteRoute){
        //将位置路由转成键值对 键:(垛 区 层 楼 仓库) 值 键对应的id
        Map<String,String> map = new HashMap<>();
        String[] strings = siteRoute.split(",");
        for (int i = 0; i < strings.length; i++) {
            String strip = StringUtils.strip(strings[i], "[]");
            map.put(strip.split(":")[0],strip.split(":")[1]);
        }
        List<String> hashKeys = new ArrayList<>();
        map.forEach((k,v) ->{
            hashKeys.add(k+v);
        });

        return hashKeys;
    }

    /**
     * 将一个垛位的所有设备通过过滤 分别取包芯设备 环境温湿度设备 取均值后用于判断预警
     * @param nodeVos 指定垛位下的所有设备
     * @param nodeType 设备类型
     * @return 该设备类型的数据均值
     */
    private Double getAvg(List<NodeVo> nodeVos , String nodeType){
        Stream<Double> stream = nodeVos.stream().filter(s -> nodeType.equals(s.getNodeType())).filter(s->{//过滤掉找不到数据的设备
            Object o = redisTemplate.opsForValue().get(MAINTAIN + s.getNodeId());
            if (ToolUtil.isEmpty(o)){
                NodeDataVo nodeDataVo = nodeDao.findNodeNowData(s.getNodeId(),NodeTypeEnum.getNodeType(s.getNodeType()).getTableName());
                if (ToolUtil.isEmpty(nodeDataVo)){
                    return false;
                }else {
                    redisTemplate.opsForValue().set(MAINTAIN + s.getNodeId(),JSON.toJSONString(nodeDataVo));
                    return true;
                }
            }
            return true;
        }).map(vos -> {
            Object o = redisTemplate.opsForValue().get(MAINTAIN + vos.getNodeId());
            NodeData nodeData = JSON.parseObject(o.toString(),NodeData.class);
            return nodeData.getTemp();
        });
        //取平均值
        return stream.mapToDouble(Double::doubleValue).sum();
    }

    /**
     *
     * @param earlyWarningVo
     * @param earlyWarning
     * @param parseData
     * @param level
     * @param data
     * @param evnType
     * @param siteType
     */
    private Integer decideEarlyWarning(EarlyWarningVo earlyWarningVo ,EarlyWarning earlyWarning , ParseData parseData ,Integer level ,Double data ,String evnType ,String siteType){
        if (level==0){
            redisTemplate.opsForHash().put(ALERT_NUM, parseData.getNodeId()+evnType,0);
        }
        //如果存在未处理的预警
        if (!ToolUtil.isEmpty(earlyWarningVo)) {
            warningType = warningType+","+earlyWarningVo.getWarningType();
            //获取缓存中的连续预警次数
            Object o = redisTemplate.opsForHash().get(ALERT_NUM, parseData.getNodeId()+evnType);
            int i;
            if (ToolUtil.isEmpty(o)){
                i = 0;
            }else {
                i = Integer.parseInt(o.toString());
            }
            //用于判断当前数据判断后预警是否达到更高一级
            boolean flag = false;
            //如果当前预警等级更高
            if (earlyWarningVo.getWarningGrade() < level) {
                flag=true;
            } else {
                int frequency = parseData.getFrequency();

                //通过频率计算升级所需的连续预警次数
                Map<String, String> warningThreshold = monitoringCenterService.findWarningThreshold();
                if (level == 1) {
                    flag = Integer.parseInt(warningThreshold.get("oneToTwo")) * 24 * 60 * 60 / frequency - i <= 1;
                } else if (level == 2) {
                    flag = Integer.parseInt(warningThreshold.get("twoToThree")) * 24 * 60 * 60 / frequency - i <= 1;
                }
                if (flag) {
                    level += 1;
                }
            }
            if (flag){
                //设置等级
                setLevel(earlyWarning, level, data);
                //设置主键id
                earlyWarning.setId(earlyWarningVo.getId());

                //更新数据
                earlyWarningDao.updateWarning(earlyWarning);
                NodeVo nodeVo = nodeDao.findByNodeId(parseData.getNodeId());
                addTask(nodeVo,dexxx(siteType, evnType, parseData.getNodeType())[1]);
            }
            if (level==3){
                //达到3级 不用连续预警
                redisTemplate.opsForHash().put(ALERT_NUM, parseData.getNodeId()+evnType,0);
            }else if (level>0){
                //连续预警次数+1
                redisTemplate.opsForHash().put(ALERT_NUM, parseData.getNodeId()+evnType, i + 1);
            }
        } else {
            //如果不存在未处理的预警 且当前预警等级大于0则需要插入数据
            if (level > 0) {
                String[] dexxx = dexxx(siteType, evnType, parseData.getNodeType());
                String type=dexxx[0];
                String taskType =dexxx[1];
                earlyWarning.setWarningType(type);
                warningType = warningType +","+type;
                NodeVo nodeVo1 =nodeDao.findByNodeId(parseData.getNodeId());
                String siteRoute = nodeVo1.getSiteRoute();
                earlyWarning.setSiteRoute(siteRoute);
                //设置设备id
                earlyWarning.setNodeId(parseData.getNodeId());
                //设置设备类型
                earlyWarning.setNodeType(parseData.getNodeType());
                setLevel(earlyWarning, level, data);
                earlyWarningDao.insertWarning(earlyWarning);
                addTask(nodeVo1,taskType);
                //往redis中设置连续预警次数为1
                if (level < 3) {
                    redisTemplate.opsForHash().put(ALERT_NUM, parseData.getNodeId()+evnType, 1);
                }else {
                    redisTemplate.opsForHash().put(ALERT_NUM, parseData.getNodeId()+evnType, 0);
                }
            }
        }
        return level;
    }

    /**
     * 故障预警信息处理
     * @param v1 传入值
     * @param v2 预警阈值  如果是丢包统计v1和v2代表的意思相反
     * @param type 类型
     * @param parseData
     */
    public void decideFault(Double v1,Double v2,String type,ParseData parseData){
        if (v1 < v2) {
            warningType= warningType +","+type;
            //判断数据库中该设备是否存在未处理的预警
            nodeMalfunction(parseData, type, v1);

        }else{
            //获取缓存的该设备数据
            Object cacheNodeData = redisTemplate.opsForValue().get(MAINTAIN + parseData.getNodeId());
            if (!ToolUtil.isEmpty(cacheNodeData)){
                //转成NodeDataVo
                NodeDataVo nodeDataVo = JSONObject.parseObject(cacheNodeData + "", NodeDataVo.class);
                //获取预警类ing
                String warningType = nodeDataVo.getWarningType();
                //如果包含当前校验的类型

                if (!ToolUtil.isEmpty(warningType)&&warningType.contains(type)){
                    earlyWarningDao.updateWarningType(parseData.getNodeId(),type);
                }
            }
        }
    }

    public NodeVo getNodeVo(Object o ,String nodeId){
        NodeVo nodeVo;
        if (ToolUtil.isEmpty(o)){
            nodeVo = nodeDao.findByNodeId(nodeId);
            if (NodeConstant.NODE_STACK.equals(nodeVo.getSiteType())){
                //查询对应的养护计划 养护计划阶段
                MaintainPlanVo maintainPlanVo = maintainPlanDao.findMaintainStackId(nodeVo.getStackId());
                nodeVo.setMaintainPlanId(maintainPlanVo.getId());
                nodeVo.setMaintainStageId(maintainPlanVo.getStageId());
                nodeVo.setMaterialCode(maintainPlanVo.getMaterialCode());
                //设置该预警id
                nodeVo.setAlermValueSettingId(maintainPlanVo.getAlarmValueSettingId());
            }
            redisTemplate.opsForValue().set(MAINTAIN_CACHE_NODE + nodeId,JSON.toJSONString(nodeVo));
        }else {
            nodeVo = JSON.parseObject(o.toString(),NodeVo.class);
        }
        return nodeVo;
    }

    //添加任务
    public void addTask(NodeVo nodeVo ,String type){
        //查询该垛位、仓间下该类型任务是否存在
        Integer count = taskManagementService.findTaskBySiteRoute(nodeVo.getSiteRoute(),type);
        if (count<1){
            Map<String, Object> map = warehouseService.getSitePrincipal(nodeVo.getSiteRoute().split(",").length-1, nodeVo.getSiteId());
            TaskDto taskDto = new TaskDto(UUID.randomUUID().toString(),type,nodeVo.getGatherSite(),nodeVo.getSiteRoute(),map.get("username")+"",DateUtil.offsetDay(DateUtil.date(), 3).toString());
            taskManagementService.addDataHandTask(taskDto);
        }
    }

    /**
     * 判断 设备预警类型 任务类型
     * @param siteType
     * @param evnType
     * @param nodeType
     * @return
     */
    public String[] dexxx(String siteType,String evnType,String nodeType ){
        String type="";
        String taskType ="";
        //判断是否是仓间设备
        boolean isNodeStack =  NodeConstant.NODE_STACK.equals(siteType);
        //如果不为空即该设备为温湿度设备

        if (!ToolUtil.isEmpty(evnType)) {
            //温湿度 温度预警
            if ("temp".equals(evnType)) {
                type = isNodeStack ? WarningTypeConstant.STACK_TEMP_OVERRUN_WARNING : WarningTypeConstant.WARE_TEMP_OVERRUN_WARNING;
                taskType = isNodeStack ? "08":"09";
                //温湿度 湿度预警
            } else if ("humidity".equals(evnType)) {
                type = isNodeStack ? WarningTypeConstant.STACK_HUMIDITY_OVERRUN_WARNING : WarningTypeConstant.WARE_HUMIDITY_OVERRUN_WARNING;
                taskType = isNodeStack ? "10":"11";
            }
        } else {
            switch (nodeType) {
                //氧气设备
                case NodeConstant.OXYGEN_NODE_TYPE_VALUE:
                    type = isNodeStack ? WarningTypeConstant.STACK_OXYGEN_OVERRUN_WARNING : WarningTypeConstant.WARE_OXYGEN_OVERRUN_WARNING;
                    taskType = isNodeStack ? "06":"07";
                    break;
                //包芯温度设备
                case NodeConstant.TEMPERATURE_CORE_CHIP_NODE_TYPE_VALUE:
                    type = WarningTypeConstant.CORE_TEMP_OVERRUN_WARNING;
                    taskType = "12";
                    break;
            }
        }
        return new String[]{type,taskType};
    }
}
