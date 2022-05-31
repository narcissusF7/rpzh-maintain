package com.rpzh.handler;

import com.alibaba.fastjson.JSON;
import com.rpzh.constant.NodeConstant;
import com.rpzh.constant.WarningTypeConstant;
import com.rpzh.dao.AlarmValueSettingDao;
import com.rpzh.dao.EarlyWarningDao;
import com.rpzh.dao.NodeDao;
import com.rpzh.entity.ToolUtil;
import com.rpzh.enums.NodeTypeEnum;
import com.rpzh.parse.ParseData;
import com.rpzh.pojo.AlarmValueSetting;
import com.rpzh.pojo.EarlyWarning;
import com.rpzh.pojo.NodeData;
import com.rpzh.service.MonitoringCenterService;
import com.rpzh.vo.AlarmValueSettingVo;
import com.rpzh.vo.EarlyWarningVo;
import com.rpzh.vo.NodeVo;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Stream;

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

    @Value("${node.voltage}")
    Double voltage;
    @Value("${node.signalStrength}")
    Integer signalStrength;


    public static String MAINTAIN = "MAINTAIN_";
    public static String ALERT_LEVER_NUM = "ALERT_LEVER_NUM";

    public void handlerData(ParseData parseData) {
        //判断电量
        if (parseData.getVoltage() < voltage) {
            //判断数据库中该设备是否存在未处理的低电量预警
            nodeMalfunction(parseData, WarningTypeConstant.NODE_LOW_BATTERY_BREAKDOWN, parseData.getVoltage());
        }
        //判断信号强度
        if (parseData.getSignalStrength() < signalStrength) {
            nodeMalfunction(parseData, WarningTypeConstant.NODE_SIGNAL_STRENGTH_BREAKDOWN, parseData.getSignalStrength().doubleValue());
        }
        //判断丢包个数
        if (parseData.getPacketLossNum() > 0) {
            nodeMalfunction(parseData, WarningTypeConstant.NODE_PACKET_LOSS_BREAKDOWN, parseData.getPacketLossNum().doubleValue());
        }


        //通过设备id找到对应的设备 及养护计划阶段
        NodeVo nodeVo = nodeDao.findNodeAndMaintainStageByNodeId(parseData.getNodeId());

        //该设备需要预警的字段
        String[] column = AlarmValueSettingVo.getColumn(nodeVo.getNodeType(), nodeVo.getSiteType());
        //通过这个字段查询缓存中这个养护计划阶段的预警规则是否存在
        Boolean hasKey = redisTemplate.opsForHash().hasKey(MAINTAIN + nodeVo.getMaintainStageId(), column[0]);

        //如果不存在
        if (!hasKey) {
            //数据库获取
            AlarmValueSettingVo alarmValueSettingVo = alarmValueSettingDao.findByMaintainStageId(nodeVo.getMaintainStageId());
            //获取所有的字段
            Field[] fields = AlarmValueSetting.class.getDeclaredFields();
            for (Field field : fields) {
                try {
                    //把每个字段的值存到redis中  对象为MAINTAIN_加养护阶段id主键 ，key为字段名 value为字段值
                    redisTemplate.opsForHash().put(MAINTAIN + nodeVo.getMaintainStageId(), field.getName(), field.get(alarmValueSettingVo.toString()));
                } catch (IllegalAccessException exception) {
                    exception.printStackTrace();
                }
            }
        }

        //获取该设备需要预警的字段  通过这个字段找到redis中的值 然后和当前值做比较
        String[] alarmColumns = AlarmValueSettingVo.getColumn(nodeVo.getNodeType(), nodeVo.getSiteType());

        //redis中的对象名
        String redisHashKey = MAINTAIN + nodeVo.getMaintainStageId();

        //存放预警阈值
        List<Double> list = new ArrayList<>();
        for (String alarmColumn : alarmColumns) {
            list.add(Double.parseDouble((String) redisTemplate.opsForHash().get(redisHashKey, alarmColumn)));
        }

        //根据丢包个数判断需要循环几次
        int loopCount = parseData.getPacketLossNum() > 2 ? parseData.getPacketLossNum() : 2;

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
                int level = decideValue(data, list);

                //温度处理
                earlyWarningHandle(nodeVo, level, parseData, data, "temp");

                //湿度数据
                data = parseData.getData().get(i + 3);

                //湿度数据等级
                level = decideValue(data, list.subList(4, 8));

                //湿度处理
                earlyWarningHandle(nodeVo, level, parseData, data, "humidity");

                //如果是其他设备
            } else {
                data = parseData.getData().get(0);
                //判断当前数据所属等级
                int level = decideValue(data, list);
                //处理该设备数据
                earlyWarningHandle(nodeVo, level, parseData, data, nodeVo.getSiteType());
            }
            //将需要存到数据库中的数据放到NodeData中
            NodeData nodeData = new NodeData(parseData, nodeVo, i);
            //向数据库插入当前数据
            Integer insertFlag = nodeDao.insertNodeHistory(nodeData, NodeTypeEnum.getNodeType(nodeVo.getNodeType()).getTableName());

            //最后一条数据需要存放到redis中
            if (i==1){
                redisTemplate.opsForValue().set(MAINTAIN + nodeVo.getNodeId(), nodeData);
            }
        }

        //如果设备时包芯温度设备时 需要判断垛位平均温度是否正常
        if (NodeConstant.TEMPERATURE_CORE_CHIP_NODE_TYPE_VALUE.equals(nodeVo.getNodeType())) {

            List<NodeVo> nodeVos = nodeDao.findNodeByStackId(nodeVo.getStackId());

            //过滤 选择包芯设备 取温度
            Stream<Double> stream2 = nodeVos.stream().filter(s -> NodeConstant.TEMPERATURE_CORE_CHIP_NODE_TYPE_VALUE.equals(s.getNodeType())).map(vos -> {
                Object o = redisTemplate.opsForValue().get(MAINTAIN + nodeVo.getNodeId());
                NodeData nodeData = new ObjectMapper().convertValue(o, NodeData.class);
                return nodeData.getTemp();
            });
            //取平均值
            double avg = stream2.mapToDouble(Double::doubleValue).sum();
            //过滤 选择环境温湿度设备 取温度
            Stream<Double> stream3 = nodeVos.stream().filter(s -> NodeConstant.HUMITURE_NODE_TYPE_VALUE.equals(s.getNodeType())).map(vos -> {
                Object o = redisTemplate.opsForValue().get(MAINTAIN + nodeVo.getNodeId());
                NodeData nodeData = new ObjectMapper().convertValue(o, NodeData.class);
                return nodeData.getTemp();
            });
            //取环境温度值
            double evnTemp = stream3.mapToDouble(Double::doubleValue).sum();
            //计算插值
            double v = avg - evnTemp;

            //获取缓存中的对应字段的值
            alarmColumns = AlarmValueSettingVo.getStackColumn();
            for (String alarmColumn : alarmColumns) {
                list.add(Double.parseDouble((String) redisTemplate.opsForHash().get(redisHashKey, alarmColumn)));
            }
            //判断等级
            int level = decideValue(v, list);

            //处理
            avgCoreHandle(nodeVo, level, parseData, v);
        }

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
            //不存在就新增一条数据
            EarlyWarning earlyWarning = new EarlyWarning(warningType, parseData.getNodeId(), parseData.getNodeType(), warningValOne, 1);
            earlyWarningDao.insertWarning(earlyWarning);
        }
    }

    public int decideValue(Double value, List<Double> list) {
        //判断等级
        int level = 0;
        if (value > list.get(0)) {
            level = 3;
        } else if (value > list.get(1)) {
            level = 3;
        } else if (value > list.get(2)) {
            level = 2;
        } else if (list.size() > 3 && value < list.get(3)) {
            level = 1;
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
        earlyWarningHandle(nodeVo, level, parseData, data, null);
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

        //查询数据库中该设备未处理的预警
        EarlyWarningVo earlyWarningVo = earlyWarningDao.findUndoEarlyWarningByNodeId(nodeVo.getNodeId());

        EarlyWarning earlyWarning = new EarlyWarning();
        earlyWarning.setNodeId(parseData.getNodeId());
        earlyWarning.setNodeType(parseData.getNodeType());

        //判断是否是仓间设备
        if (NodeConstant.NODE_STACK.equals(nodeVo.getSiteType())) {
            //截取位置路由   位置路径,00:根节点,01:仓库, 02: 楼号, 03: 层, 04: 区, 05: 垛位
            HashMap hashMap = new ObjectMapper().convertValue(data, HashMap.class);
            Object o = hashMap.get("04");
            if (ToolUtil.isEmpty(o)) {
                //设置所在仓间
                earlyWarning.setAreaId(Integer.parseInt(o.toString()));
            }
        } else {
            //设置所在垛位
            earlyWarning.setStackId(nodeVo.getStackId());
        }

        //如果存在未处理的预警
        if (!ToolUtil.isEmpty(earlyWarningVo)) {
            //如果当前预警等级更高
            Object o = redisTemplate.opsForHash().get(ALERT_LEVER_NUM, nodeVo.getNodeId());
            int i = Integer.parseInt(o.toString());
            if (earlyWarningVo.getWarningGrade() < level) {

                setLevel(earlyWarning, level, data);

                earlyWarningDao.updateWarning(earlyWarning);
                //redis中设置连续预警
                redisTemplate.opsForHash().put(ALERT_LEVER_NUM, nodeVo.getNodeId(), 1);
            } else {
                if (ToolUtil.isEmpty(o)) {
                    int frequency = parseData.getFrequency();
                    boolean flag = false;
                    //通过频率计算连续预警次数
                    Map<String, String> warningThreshold = monitoringCenterService.findWarningThreshold();
                    if (level == 1) {
                        flag = Integer.parseInt(warningThreshold.get("oneToTwo")) * 24 * 60 * 60 / frequency - i <= 1;
                    } else if (level == 2) {
                        flag = Integer.parseInt(warningThreshold.get("twoToThree")) * 24 * 60 * 60 / frequency - i <= 1;
                    }
                    if (flag) {
                        level += 1;
                        setLevel(earlyWarning, level, data);
                    }
                }

            }
            redisTemplate.opsForHash().put(ALERT_LEVER_NUM, nodeVo.getNodeId(), i + 1);
        } else {
            //如果不存在未处理的预警 且当前预警等级大于0则需要插入数据
            if (level > 0) {
                //预警类型,01:设备电量故障,02:设备信号强度故障,03:设备丢包故障,04:虫数超限预警,05:包芯温度超限预警,06:环境温度超限预警,07:环境湿度超限预警,08:氧气超限预警,09:垛位温度预警
                if (!ToolUtil.isEmpty(evnType)) {

                    if ("temp".equals(evnType)) {
                        earlyWarning.setWarningType(WarningTypeConstant.ATMOSPHERE_TEMP_OVERRUN_WARNING);
                    } else if ("humidity".equals(evnType)) {
                        earlyWarning.setWarningType(WarningTypeConstant.ATMOSPHERE_HUMIDITY_OVERRUN_WARNING);
                    }
                } else {
                    switch (nodeVo.getNodeType()) {
                        case NodeConstant.OXYGEN_NODE_TYPE_VALUE:
                            earlyWarning.setWarningType(WarningTypeConstant.OXYGEN_OVERRUN_WARNING);
                            break;
                        case NodeConstant.TEMPERATURE_CORE_CHIP_NODE_TYPE_VALUE:
                            earlyWarning.setWarningType(WarningTypeConstant.CORE_TEMP_OVERRUN_WARNING);
                            break;
                    }
                }
                setLevel(earlyWarning, level, data);
                earlyWarningDao.insertWarning(earlyWarning);
                //往redis中设置连续预警次数为1
                if (level < 3) {
                    redisTemplate.opsForHash().put(ALERT_LEVER_NUM, nodeVo.getNodeId(), 1);
                }
            } else {
                redisTemplate.opsForHash().put(ALERT_LEVER_NUM, nodeVo.getNodeId(), 0);
            }
        }
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
        EarlyWarningVo earlyWarningVo = earlyWarningDao.findUndoStackEarlyWarning(nodeVo.getStackId());
        EarlyWarning earlyWarning = new EarlyWarning();
        earlyWarning.setNodeType(parseData.getNodeType());
        earlyWarning.setStackId(nodeVo.getStackId());

        //如果存在未处理的预警
        if (!ToolUtil.isEmpty(earlyWarningVo)) {
            //如果当前的等级更高
            if (earlyWarningVo.getWarningGrade() < level) {
                setLevel(earlyWarning, level, data);
                earlyWarningDao.updateWarning(earlyWarning);
            }
        } else if (level > 0) {
            //如果不存在未处理的预警 且当前预警等级大于0则需要插入数据
            earlyWarning.setWarningType(WarningTypeConstant.STACK_TEMP_OVERRUN_WARNING);
            setLevel(earlyWarning, level, data);
            earlyWarningDao.insertWarning(earlyWarning);
        }

    }
}
