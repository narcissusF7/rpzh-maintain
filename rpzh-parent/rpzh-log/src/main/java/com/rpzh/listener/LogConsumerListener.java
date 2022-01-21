package com.rpzh.listener;

import com.alibaba.fastjson.JSONObject;
import com.rpzh.entity.LogOperation;
import com.rpzh.enums.GatherTables;
import com.rpzh.enums.SqlType;
import com.rpzh.util.EnumUtil;
import com.rpzh.util.RedisUtil;
import com.rpzh.util.TokenDecode;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;


/**
 * @author wbl
 * [日志收集类]
 */
@Component
@RocketMQMessageListener(consumerGroup = "log-consumer-group",
        topic = "log-gather", selectorExpression = "*",
        consumeMode = ConsumeMode.CONCURRENTLY,
        messageModel = MessageModel.CLUSTERING)
public class LogConsumerListener implements RocketMQListener<String> {

    private final String accountId = "ACCOUNT_ID_";

    @Resource
    private RedisUtil redisUtil;

    @Override
    public void onMessage(String message) {
        JSONObject jsonObject = JSONObject.parseObject(message);
        Object dataObj = jsonObject.get("data");
        // 如果data为null 则不解析
        if (dataObj == null) {
            return;
        }
        // 根据字符串获取对应的sql语句类型
        SqlType sqlType = EnumUtil.getEnumFromString(SqlType.class, jsonObject.getString("type"));

        // 获取表
        GatherTables gatherTables = EnumUtil.getEnumFromString(GatherTables.class,
                jsonObject.getString("table").toUpperCase());
        // 创建日志操作类
        LogOperation logOperation = new LogOperation();
        // 获取账号
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        String account = userInfo.get("username");
        // 获得名称
        String name = redisUtil.get(accountId + account).toString();
        // 获得日志类型
        String logType = gatherTables.getExplain() + sqlType.getExplain();

    }
}
