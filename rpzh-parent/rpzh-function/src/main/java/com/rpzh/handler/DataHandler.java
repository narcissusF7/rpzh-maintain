package com.rpzh.handler;

import com.alibaba.fastjson.JSON;
import com.rpzh.constant.NodeConstant;
import com.rpzh.constant.WarningTypeConstant;
import com.rpzh.dao.AlarmValueSettingDao;
import com.rpzh.dao.EarlyWarningDao;
import com.rpzh.dao.NodeDao;
import com.rpzh.dto.EarlyWarningDto;
import com.rpzh.entity.ToolUtil;
import com.rpzh.parse.ParseData;
import com.rpzh.pojo.AlarmValueSetting;
import com.rpzh.pojo.EarlyWarning;
import com.rpzh.vo.AlarmValueSettingVo;
import com.rpzh.vo.EarlyWarningVo;
import com.rpzh.vo.NodeDataVo;
import com.rpzh.vo.NodeVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

public class DataHandler {

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    AlarmValueSettingDao alarmValueSettingDao;

    @Autowired
    EarlyWarningDao earlyWarningDao;

    @Autowired
    NodeDao nodeDao;

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
            AlarmValueSettingVo alarmValueSettingVo = alarmValueSettingDao.findByMaintainStageId(nodeVo.getMaintainStageId());
            //获取所有的字段
            Field[] fields = AlarmValueSetting.class.getDeclaredFields();
            for (Field field : fields) {
                try {
                    //把每个字段的值存到redis中  对象为MAINTAIN_加养护阶段id主键 ，key为字段名 value为字段值
                    redisTemplate.opsForHash().put(MAINTAIN + alarmValueSettingVo.getPhases(), field.getName(), field.get(alarmValueSettingVo.toString()));
                } catch (IllegalAccessException exception) {
                    exception.printStackTrace();
                }
            }
        }

        //获取该设备需要预警的字段  通过这个字段找到redis中的值 然后和当前值做比较
        String[] alarmColumns = AlarmValueSettingVo.getColumn(nodeVo.getNodeType(), nodeVo.getSiteType());

        //redis中的对象名
        String redisHashKey = "MAINTAIN_" + nodeVo.getMaintainStageId();


        //存放预警阈值
        List<Double> list = new ArrayList<>();
        for (String alarmColumn : alarmColumns) {
            list.add(Double.parseDouble((String) redisTemplate.opsForHash().get(redisHashKey, alarmColumn)));
        }

        //判断设备类型
        //设备如果是温湿度设备

        if (NodeConstant.HUMITURE_NODE_TYPE_VALUE.equals(nodeVo.getNodeType())) {

            //温度处理
            Double data = parseData.getData().get(0);
            //判断当前数据所属等级
            int level = decideValue(data, list);

            earlyWarningHandle(nodeVo,level,parseData,data,"temp");


            //湿度处理
            data = parseData.getData().get(4);

            level = decideValue(data, list.subList(4, 8));

            earlyWarningHandle(nodeVo,level,parseData,data,"humidity");

            //如果是其他设备
        } else {

            Double data = parseData.getData().get(0);
            //判断当前数据所属等级
            int level = decideValue(data, list);
            earlyWarningHandle(nodeVo,level,parseData,data);
        }

        //如果设备时包芯温度设备时 需要判断垛位平均温度是否正常
        if (NodeConstant.TEMPERATURE_CORE_CHIP_NODE_TYPE_VALUE.equals(nodeVo.getNodeType())) {
            Map<String, String> datas = redisTemplate.opsForHash().entries(MAINTAIN + nodeVo.getStackId());
            Collection<String> values = datas.values();

            Stream<Double> stream2 = values.stream().map((String s) -> {
                HashMap hashMap = JSON.parseObject(s, HashMap.class);
                return Double.parseDouble(hashMap.get("data").toString());
            });
            //平均值
            double v = stream2.mapToDouble(Double::doubleValue).sum();

            //获取缓存中的对应字段的值
            alarmColumns = AlarmValueSettingVo.getStackColumn();
            for (String alarmColumn : alarmColumns) {
                list.add(Double.parseDouble((String) redisTemplate.opsForHash().get(redisHashKey, alarmColumn)));
            }
            int level = decideValue(v, list);

            avgCoreHandle(nodeVo,level,parseData,v);
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
        Integer count = earlyWarningDao.findUndoCount(parseData.getNodeId(), warningType);
        if (count < 1) {
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

    public void earlyWarningHandle(NodeVo nodeVo,Integer level,ParseData parseData,Double data){
        earlyWarningHandle(nodeVo,level,parseData,data,null);
    }

    /**
     * 数据预警分析
     * @param nodeVo 当前设备
     * @param level  当前预警等级
     * @param parseData 传感器发送的数据
     * @param data  最新数据的值
     * @param evnType 温湿度类型 用来判断是否为环境温湿度设备
     */
    public void earlyWarningHandle(NodeVo nodeVo,Integer level,ParseData parseData,Double data,String evnType){
        //查询数据库中该设备未处理的预警
        EarlyWarningVo earlyWarningVo = earlyWarningDao.findUndoEarlyWarningByNodeId(nodeVo.getNodeId());

        EarlyWarning earlyWarning = new EarlyWarning();
        earlyWarning.setNodeId(parseData.getNodeId());
        earlyWarning.setNodeType(parseData.getNodeType());
        earlyWarning.setStackId(nodeVo.getStackId());
        //如果存在未处理的预警
        if (!ToolUtil.isEmpty(earlyWarningVo)) {
            //如果当前预警等级更高
            if (earlyWarningVo.getWarningGrade() < level) {

                setLevel(earlyWarning,level,data);

                earlyWarningDao.updateWarning(earlyWarning);
                redisTemplate.opsForHash().put(ALERT_LEVER_NUM,nodeVo.getNodeId(),1);
            }else {

                if (earlyWarningVo.getWarningGrade()<3){
                    //否则获取redis中的连续预警次数
                    Object o = redisTemplate.opsForHash().get(ALERT_LEVER_NUM, nodeVo.getNodeId());

                    if (ToolUtil.isEmpty(o)){

                        int i = Integer.parseInt(o.toString());

                        //TODO 通过频率计算升级所需要的包  达到要求就升级否则就加1
                        if (true){
                            level+=1;
                            setLevel(earlyWarning,level,data);
                            if (level==3){
                                redisTemplate.opsForHash().delete(nodeVo.getNodeId());
                            }else {
                                redisTemplate.opsForHash().put(ALERT_LEVER_NUM,nodeVo.getNodeId(),i);
                            }
                        }else {
                            redisTemplate.opsForHash().put(ALERT_LEVER_NUM,nodeVo.getNodeId(),i+1);
                        }

                    }
                }

            }
        } else {
            //如果不存在未处理的预警 且当前预警等级大于0则需要插入数据
            if (level > 0) {
                //预警类型,01:设备电量故障,02:设备信号强度故障,03:设备丢包故障,04:虫数超限预警,05:包芯温度超限预警,06:环境温度超限预警,07:环境湿度超限预警,08:氧气超限预警,09:垛位温度预警

                if (!ToolUtil.isEmpty(evnType)){

                    if ("temp".equals(evnType)){
                        earlyWarning.setWarningType(WarningTypeConstant.ATMOSPHERE_TEMP_OVERRUN_WARNING);
                    }else if ("humidity".equals(evnType)){
                        earlyWarning.setWarningType(WarningTypeConstant.ATMOSPHERE_HUMIDITY_OVERRUN_WARNING);
                    }
                }else{
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
                if (level<3){
                    redisTemplate.opsForHash().put(ALERT_LEVER_NUM,nodeVo.getNodeId(),1);
                }

            }
        }
        nodeVo.setAlertLevel(level);

        //处理完后需要把该数据放到redis中
        redisTemplate.opsForHash().put(MAINTAIN+nodeVo.getSiteId(),nodeVo.getNodeId(),nodeVo);

    }

    public void avgCoreHandle(NodeVo nodeVo,Integer level,ParseData parseData,Double data){
        //查询数据库中该设备未处理的预警
        EarlyWarningVo earlyWarningVo = earlyWarningDao.findUndoStackEarlyWarning(nodeVo.getStackId());
        EarlyWarning earlyWarning = new EarlyWarning();
        earlyWarning.setNodeType(parseData.getNodeType());
        earlyWarning.setStackId(nodeVo.getStackId());

        //如果存在未处理的预警
        if (!ToolUtil.isEmpty(earlyWarningVo)) {
            //如果当前的等级更高
            if (earlyWarningVo.getWarningGrade() < level) {
                setLevel(earlyWarning,level,data);
                earlyWarningDao.updateWarning(earlyWarning);
            }
        }else {
            //如果不存在未处理的预警 且当前预警等级大于0则需要插入数据
            if (level > 0) {
                earlyWarning.setWarningType(WarningTypeConstant.STACK_TEMP_OVERRUN_WARNING);
                setLevel(earlyWarning, level, data);
                earlyWarningDao.insertWarning(earlyWarning);
            }
        }
    }

//    public List<NodeVo> test(List<NodeVo> nodeVos){
//        Set<Integer> set = new HashSet();
//        //传输的数据集
//        nodeVos.forEach((nodeVo) -> {
//            String[] column = AlarmValueSettingVo.getColumn(nodeVo.getNodeType(),nodeVo.getSiteType());
//            //判断是否存在
//            Boolean hasKey = redisTemplate.opsForHash().hasKey(MAINTAIN + nodeVo.getMaintainStageId(), column[0]);
//            //不存在的话存到set中 去数据库查找对应的
//            if (!hasKey) {
//                set.add(nodeVo.getMaintainStageId());
//            }
//        });
//        //补充缓存中的预警阈值
//        if (set.size() > 0) {
//            //获取缓存中不存在的预警阈值
//            List<AlarmValueSetting> alarmValueSettings = alarmValueSettingDao.findByMaintainStageId(set);
//            //获取所有的字段
//            Field[] fields = AlarmValueSetting.class.getDeclaredFields();
//            alarmValueSettings.forEach((alarmValueSetting) -> {
//
//                for (Field field : fields) {
//                    try {
//                        //把每个字段的值存到redis中  对象为MAINTAIN_加养护阶段id主键 ，key为字段名 value为字段值
//                        redisTemplate.opsForHash().put(MAINTAIN + alarmValueSetting.getPhases(), field.getName(), field.get(alarmValueSetting.toString()));
//                    } catch (IllegalAccessException exception) {
//                        exception.printStackTrace();
//                    }
//                }
//            });
//        }
//        //遍历要确认的设备数据
//        nodeVos.forEach((nodeVo) -> {
//            //TODO 数据放置到redis中时使用hash存储 对象值为MAINTAIN_加垛位 属性为设备id  值为整个对象
//            //通过设备id获取缓存中的该设备的数据
//            Map<String, Object> data = (Map<String, Object>) redisTemplate.opsForHash().get(MAINTAIN + nodeVo.getStackId(), nodeVo.getNodeId());
//            //redis中的对象名
//            String redisHashKey = "MAINTAIN_" +nodeVo.getMaintainStageId();
//            List<String> list = new ArrayList<>();
//            String stackTemp;
//            String stackMoisture;
//            if (NodeConstant.HUMITURE_NODE_TYPE_VALUE.equals(nodeVo.getNodeType())) {
//                //当前的温度数据
//                stackTemp = ((String)data.get("data")).split(",")[0];
//
//                //获取对应的预警字段
//                String[] alarmColumns = AlarmValueSettingVo.getColumn(nodeVo.getNodeType(),nodeVo.getSiteType());
//                for (String alarmColumn : alarmColumns) {
//                    list.add((String) redisTemplate.opsForHash().get(redisHashKey, alarmColumn));
//                }
//                //判断温度预警
//                decideValue(nodeVo, stackTemp, list.subList(0,4),0,WarningTypeConstant.TEMP_OVERRUN_WARNING);
//                //当前的湿度数据
//                stackMoisture = ((String)data.get("data")).split(",")[1];
//                //判断湿度
//                decideValue(nodeVo, stackMoisture, list.subList(4,8),0,WarningTypeConstant.HUMIDITY_OVERRUN_WARNING);
//            } else {
//                stackTemp = (String)data.get("data");
//
//                String[] alarmColumns = AlarmValueSettingVo.getColumn(nodeVo.getNodeType(),nodeVo.getSiteType());
//                for (String alarmColumn : alarmColumns) {
//                    list.add((String) redisTemplate.opsForHash().get(redisHashKey, alarmColumn));
//                }
//                //判断设备预警值
//                decideValue(nodeVo, stackTemp, list,0);
//            }
//            //如果设备时包芯温度设备时 需要判断垛位平均温度是否正常
//            if (NodeConstant.TEMPERATURE_CORE_CHIP_NODE_TYPE_VALUE.equals(nodeVo.getNodeType())){
//                Map<String, String> datas = redisTemplate.opsForHash().entries(MAINTAIN + nodeVo.getStack());
//                Collection<String> values = datas.values();
//
//                Stream<Double> stream2 = values.stream().map((String s) -> {
//                    HashMap hashMap = JSON.parseObject(s, HashMap.class);
//                    return Double.parseDouble(hashMap.get("data").toString());
//                });
//                //平均值
//                double v = stream2.mapToDouble(Double::doubleValue).sum();
//
//                //获取缓存中的对应字段的值
//                String[] alarmColumns = AlarmValueSettingVo.getStackColumn();
//                for (String alarmColumn : alarmColumns) {
//                    list.add(redisTemplate.opsForHash().get(redisHashKey, alarmColumn).toString());
//                }
//                decideValue(nodeVo, String.valueOf(v),list,1);
//
//            }
//        });
//        return nodeVos;
//    }
//
//    public void decideValue(NodeVo nodeVo, String value, List<String> list,int type) {
//        decideValue(nodeVo,value,list,type,null);
//    }
//
//
//    public void decideValue(NodeVo nodeVo, String value, List<String> list,int type,String warningType) {
//        //判断等级
//        int level = 0;
//        if (value.compareTo(list.get(0)) < 0) {
//            level = 1;
//        } else if (value.compareTo(list.get(1)) > 0) {
//            level = 3;
//        } else if (value.compareTo(list.get(2)) > 0) {
//            level = 2;
//        } else if (value.compareTo(list.get(3)) > 0) {
//            level = 1;
//        }
//        EarlyWarningVo earlyWarningVo;
//        if (type==0){
//            //查询设备预警
//            earlyWarningVo = earlyWarningDao.findEarlyWarningByNodeId(nodeVo.getNodeId());
//        }else {
//            //查询垛位预警
//            earlyWarningVo = earlyWarningDao.findEarlyWarningByStack(nodeVo.getStack());
//        }
//        //如果当前数据达到预警
//        if (level>0){
//            //判断预警表中没有数据
//            if (ToolUtil.isEmpty(earlyWarningVo)){
//                //往预警表中添加数据
//                EarlyWarning earlyWarning = new EarlyWarning();
//                earlyWarning.setWarningGrade(level);
//                //判断预警等级 设置对应的字段值
//                if (level==1){
//                    earlyWarning.setWarningValOne(Double.parseDouble(value));
//                }else if (level==2){
//                    earlyWarning.setWarningValTwo(Double.parseDouble(value));
//                }else {
//                    earlyWarning.setWarningValThree(Double.parseDouble(value));
//                }
//
//                //预警类型,01:检测设备故障,02:虫数超限预警,03:温度超限预警,04:湿度超限预警,05:氧气超限预警,06:垛位温度预警
//                //判断是设备预警还是垛位预警
//                if (type==0){
//                    //如果是设备预警
//                    earlyWarning.setNodeId(nodeVo.getNodeId());
//                    earlyWarning.setNodeType(nodeVo.getNodeType());
//                    if (ToolUtil.isEmpty(warningType)){
//                        switch (nodeVo.getNodeType()){
//                            case "01":
//                                earlyWarning.setWarningType(WarningTypeConstant.OXYGEN_OVERRUN_WARNING);
//                                break;
//                            case "04":
//                                earlyWarning.setWarningType(WarningTypeConstant.TEMP_OVERRUN_WARNING);
//                                break;
//                        }
//                    }else {
//                        earlyWarning.setWarningType(warningType);
//                    }
//                    //需要判断redis中该设备连续预警次数
//                    Object o = redisTemplate.opsForHash().get(MAINTAIN + "NODE", nodeVo.getNodeId());
//
//                    if (ToolUtil.isEmpty(o)){
//                        redisTemplate.opsForHash().put(MAINTAIN + "NODE", nodeVo.getNodeId(),1);
//                    }else {
//                        //次数
//                        int times = Integer.parseInt(o.toString());
//
//                    }
//
//                }else {
//                    earlyWarning.setStackId(nodeVo.getStackId());
//                    earlyWarning.setWarningType(WarningTypeConstant.STACK_TEMP_OVERRUN_WARNING);
//                }
//                Integer insertFlag = earlyWarningDao.insertWarning(earlyWarning);
//            }else {
//                //TODO 判断预警表中的等级 如果当前的预警等级更高  或者预警时间超过阈值 则修改预警表中的数据
//                //如果当前的预警等级更高
//                if (level>earlyWarningVo.getWarningGrade()){
//                    EarlyWarning earlyWarning = new EarlyWarning();
//                    earlyWarning.setWarningGrade(level);
//                    earlyWarning.setId(earlyWarningVo.getId());
//                    if (level==2){
//                        earlyWarning.setWarningValTwo(Double.parseDouble(value));
//                    }
//                    if (level==3){
//                        earlyWarning.setWarningValThree(Double.parseDouble(value));
//                    }
//                    //修改数据库中的预警等级
//                    earlyWarningDao.updateWarning(earlyWarning);
//                }
//
//                //如果预警等级大于等于当前等级并且小于3级
//                if (earlyWarningVo.getWarningGrade() >= level ){
//
//                    if (earlyWarningVo.getWarningGrade()==1){
//                        //TODO 判断是否需要升级
//
//                    }
//                    if (earlyWarningVo.getWarningGrade()==2){
//                        long l = decrDate(earlyWarningVo.getWarningTimeTwo());
//                        //和升级阈值做比较
//                        redisTemplate.opsForValue().get("");
//                    }
//                }
//                    level = earlyWarningVo.getWarningGrade() > level ? earlyWarningVo.getWarningGrade() : level;
//
//            }
//        }
//        nodeVo.setAlertLevel(level);
//        list.clear();
//    }
//
//    public long decrDate(String time2){
//        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Date date1= new Date();
//        Date date2=null;
//        try {
//            date2=format.parse(time2);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        Calendar ca1 = Calendar.getInstance();
//        Calendar ca2 = Calendar.getInstance();
//        ca1.setTime(date1);
//        ca2.setTime(date2);
//        return (ca1.getTimeInMillis()- ca2.getTimeInMillis())/(1000*60*60*24);
//    }
}
