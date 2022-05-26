package com.rpzh.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rpzh.dao.AlarmValueSettingDao;
import com.rpzh.dao.EarlyWarningDao;
import com.rpzh.dao.NodeDao;

import com.rpzh.entity.ToolUtil;
import com.rpzh.service.MonitoringCenterService;
import com.rpzh.util.MonitorCondition;
import com.rpzh.vo.NodeDataVo;
import com.rpzh.vo.NodeVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
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
        //位置路由
        if (monitorCondition.getStackId() > 0) {
            monitorCondition.setSiteRoute("");
        } else if (monitorCondition.getWareAreaId() > 0) {
            monitorCondition.setSiteRoute("[00:0],[01:" + monitorCondition.getWarehouseId()
                    + "],[02:" + monitorCondition.getBulidingId() + "],[03:" + monitorCondition.getFloodId()
                    + "],[04:" + monitorCondition.getWareAreaId() + "]");
        } else if (monitorCondition.getFloodId() > 0) {
            monitorCondition.setSiteRoute("[00:0],[01:" + monitorCondition.getWarehouseId()
                    + "],[02:" + monitorCondition.getBulidingId() + "],[03:" + monitorCondition.getFloodId() + "]%");
        } else if (monitorCondition.getBulidingId() > 0) {
            monitorCondition.setSiteRoute("[00:0],[01:" + monitorCondition.getWarehouseId()
                    + "],[02:" + monitorCondition.getBulidingId() + "]%");
        } else if (monitorCondition.getWarehouseId() > 0) {
            monitorCondition.setSiteRoute("[00:0],[01:" + monitorCondition.getWarehouseId() + "]%");
        } else {
            monitorCondition.setSiteRoute("[00:0]%");
        }

        //设备数据集
        List<NodeDataVo> nodeDataVos = new ArrayList<>();
        int pageNum = monitorCondition.getPage();
        int pageSize = monitorCondition.getSize();
        if (monitorCondition.getSortCondition() == 0) {
            PageHelper.startPage(pageNum, pageSize);
        }
        //通过条件查询对应的所有设备
        List<NodeVo> nodeVos = nodeDao.findNodeByMonitorCondition(monitorCondition, monitorCondition.getNodeTypes());

        for (NodeVo nodeVo : nodeVos) {
            //获取缓存中该设备的数据
            Map<String,String> data = redisTemplate.opsForHash().entries(CACHE_KEY_REDIS_MAINTAIN + nodeVo.getNodeId());
            try {
                //把该数据添加到集合中
                nodeDataVos.add(JSONObject.parseObject((InputStream) data, NodeDataVo.class, new Feature[0]));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //监控状态筛选 1.正常 2.预警
        if (monitorCondition.getMonitorStatus()==1){
            nodeDataVos = nodeDataVos.stream().filter(nodeDataVo ->
                    nodeDataVo.getAlertLevel() ==0
            ).collect(Collectors.toList());
        }else if (monitorCondition.getMonitorStatus()==1){
            nodeDataVos = nodeDataVos.stream().filter(nodeDataVo ->
                    nodeDataVo.getAlertLevel() >0
            ).collect(Collectors.toList());
        }

        //如果物料代码不为空  过滤
        if (ToolUtil.isEmpty(monitorCondition.getMaterialCode())){
            nodeDataVos = nodeDataVos.stream().filter(nodeDataVo ->
                    nodeDataVo.getMaterialCode().startsWith(monitorCondition.getMaterialCode())
            ).collect(Collectors.toList());
        }
        //如果设备时按预警排序  需要手动分页
        if (monitorCondition.getSortCondition() == 1) {
            //排序  按预警等级才需
            nodeDataVos = nodeDataVos.stream().sorted(Comparator.comparing(NodeDataVo::getAlertLevel).reversed()).collect(Collectors.toList());
            //创建一个page类
            Page<NodeDataVo> page = new Page(pageNum, pageSize);
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
     * TODO 查询故障或者丢包设备
     */
    @Override
    public PageInfo<NodeVo> findTroubleNode() {
        return null;
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
        redisTemplate.delete("MAINTAIN_oneToTwo");
        redisTemplate.delete("MAINTAIN_TwoToThree");
        return flag.get();
    }

    /**
     * 查询预警升级阈值
     */
    @Override
    public Map<String, String> findWarningThreshold() {
        HashMap<String, String> thresholdMap = new HashMap<>();
        String oneToTwo = (String)redisTemplate.opsForValue().get("MAINTAIN_oneToTwo");
        String twoToThree = (String)redisTemplate.opsForValue().get("MAINTAIN_twoToThree");
        if (Objects.equals(oneToTwo, "") || Objects.equals(twoToThree, "")) {
            List<String> thresholds = earlyWarningDao.findWarningThreshold("settingKey");
            oneToTwo = thresholds.get(0);
            redisTemplate.opsForValue().set("MAINTAIN_oneToTwo", oneToTwo);
            twoToThree = thresholds.get(1);
            redisTemplate.opsForValue().set("MAINTAIN_twoToThree", twoToThree);
        }

        thresholdMap.put("oneToTwo", oneToTwo);
        thresholdMap.put("twoToThree", twoToThree);
        return thresholdMap;
    }
}

