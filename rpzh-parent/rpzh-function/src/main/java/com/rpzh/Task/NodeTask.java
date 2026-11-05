package com.rpzh.Task;

import com.rpzh.config.NodeAbnormalConfig;
import com.rpzh.constant.NodeStatusConstant;
import com.rpzh.dao.EarlyWarningDao;
import com.rpzh.dao.NodeDao;
import com.rpzh.handler.MaintainDataHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 设备异常状态定时判定。
 *
 * <p>两类触发条件：
 * 1) 长时间未收到已入垛设备上报的数据 —— 本任务扫描，超过 node.abnormal.timeout-hours 置 status=5；
 * 2) 设备多次发出异常的数据 —— MaintainDataHandler.nodeMalfunction 累计到
 *    node.abnormal.max-error-count 次时直接置 status=5 并生成故障预警。</p>
 *
 * <p>恢复：异常设备重新上报数据，且已无未处置的故障类预警时，恢复为 status=1。
 * 这样「多次异常数据」类异常需先在预警中心处置才会恢复，不会被立即冲掉。</p>
 */
@Component
@Slf4j
public class NodeTask {

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private EarlyWarningDao earlyWarningDao;

    @Autowired
    private NodeAbnormalConfig nodeAbnormalConfig;

    @Autowired
    private RedisTemplate redisTemplate;

    @Scheduled(cron = "${node.abnormal.cron:0 0/10 * * * ?}")
    public void checkNodeAbnormal() {
        if (!nodeAbnormalConfig.isEnabled()) {
            return;
        }
        try {
            Date end = new Date();
            Date begin = shift(end, Calendar.HOUR_OF_DAY, -nodeAbnormalConfig.getTimeoutHours());

            // 历史数据在 ClickHouse：不可用或完全无数据时必须跳过，
            // 否则 reported 为空会把所有入垛设备误标为异常。
            Set<String> reported;
            try {
                reported = new HashSet<>(safe(nodeDao.findAllNodeHistory(begin, end)));
            } catch (Exception e) {
                log.warn("历史数据表不可用，跳过本次设备异常扫描：{}", e.getMessage());
                return;
            }
            if (reported.isEmpty()) {
                Date farBegin = shift(end, Calendar.DATE, -30);
                List<String> anyHistory;
                try {
                    anyHistory = safe(nodeDao.findAllNodeHistory(farBegin, end));
                } catch (Exception e) {
                    log.warn("历史数据表不可用，跳过本次设备异常扫描：{}", e.getMessage());
                    return;
                }
                if (anyHistory.isEmpty()) {
                    log.warn("近 30 天历史数据表无任何记录，跳过超时判定，避免把全部入垛设备误标为异常");
                    return;
                }
            }

            List<String> entered = safe(nodeDao.findEnterStackNodeId());
            List<String> abnormal = safe(nodeDao.findNodeIdsByStatus(NodeStatusConstant.ABNORMAL));

            List<String> markAbnormal = new ArrayList<>();
            List<String> recover = new ArrayList<>();

            // 正常设备：窗口内没有上报数据 -> 置异常
            for (String nodeId : entered) {
                if (nodeId != null && !reported.contains(nodeId)) {
                    markAbnormal.add(nodeId);
                }
            }
            // 异常设备：重新上报 且 未处置故障预警已清空 -> 恢复为入垛
            for (String nodeId : abnormal) {
                if (nodeId == null || !reported.contains(nodeId)) {
                    continue;
                }
                Integer undo = earlyWarningDao.countUndoFaultWarning(nodeId);
                if (undo == null || undo <= 0) {
                    recover.add(nodeId);
                }
            }

            if (!markAbnormal.isEmpty()) {
                nodeDao.updateStatus(markAbnormal, NodeStatusConstant.ABNORMAL);
                markAbnormal.forEach(nodeId ->
                        redisTemplate.delete(MaintainDataHandler.MAINTAIN_CACHE_NODE + nodeId));
                log.info("设备异常判定：{} 台长时间未收到数据 -> status=5：{}", markAbnormal.size(), markAbnormal);
            }
            if (!recover.isEmpty()) {
                nodeDao.updateStatus(recover, NodeStatusConstant.ENTERED);
                recover.forEach(nodeId ->
                        redisTemplate.delete(MaintainDataHandler.MAINTAIN_CACHE_NODE + nodeId));
                log.info("设备状态恢复：{} 台重新上报且无未处置故障预警 -> status=1：{}", recover.size(), recover);
            }
        } catch (Exception e) {
            // 定时任务内异常必须吞掉，避免影响其它调度
            log.error("设备异常状态扫描失败", e);
        }
    }

    private static Date shift(Date base, int field, int amount) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(base);
        cal.add(field, amount);
        return cal.getTime();
    }

    private static <T> List<T> safe(List<T> list) {
        return list == null ? new ArrayList<>() : list;
    }
}
