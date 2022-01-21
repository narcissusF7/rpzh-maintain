package com.rpzh.listener;

import com.alibaba.fastjson.JSONObject;
import com.rpzh.dto.UserDto;
import com.rpzh.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 监听RocketMq消息,用于同步不同系统用户信息
 * @Author liubc
 * @create 2022/1/20 9:27
 * @Description: good good study,day day up
 */
@Slf4j
@Component
@RocketMQMessageListener(consumerGroup = "MyConsumerGroup", topic = "UserManageTopic",consumeMode= ConsumeMode.CONCURRENTLY,messageModel = MessageModel.BROADCASTING)
public class RocketMqListener implements RocketMQListener<String> {

    @Autowired
    private UserService userService;

    @Override
    public void onMessage(String message) {
        Map map = JSONObject.parseObject(message, Map.class);
        //获取数据
        String data = map.get("data").toString();
        //获取类型
        String type = map.get("type").toString();
        //获取哪个系统发送的消息
        String system = map.get("system").toString();
        UserDto userDto = new UserDto();
        log.info("养护系统收到消息 : "+ data+",类型:"+type+",发送系统为:"+system);
        //如果是养护系统发送的消息不做处理
        if (!"maintain".equals(system)){
            switch (type){
                case "1"://添加用户,角色需要每个系统单独配置
                    Map addMap = JSONObject.parseObject(data, Map.class);
                    userDto.setNickName((String) addMap.get("nickName"));
                    userDto.setSex((String) addMap.get("sex"));
                    userDto.setPhonenumber((String) addMap.get("phonenumber"));
                    userDto.setRemark((String) addMap.get("remark"));
                    userDto.setUserName((String) addMap.get("userName"));
                    userDto.setCreateBy((String) addMap.get("createBy"));
                    userDto.setPassword((String) addMap.get("noEncryPassWord"));
                    userDto.setEmail((String) addMap.get("email"));
                    userDto.setStatus((String) addMap.get("status"));
                    Integer deptId = (Integer) addMap.get("deptId");
                    userDto.setDeptId(deptId.longValue());
                    Integer insertFlag = userService.addUser(userDto);
                    log.info("养护系统添加用户成功");
                    break;
                case "3":
                    Map statusMap = JSONObject.parseObject(data, Map.class);
                    Integer userId = (Integer) statusMap.get("userId");
                    userDto.setUserId(userId.longValue());
                    userDto.setStatus((String) statusMap.get("status"));
                    userDto.setUserName((String) statusMap.get("userName"));
                    Integer flag = userService.mqChangeUserStatus(userDto);
                    log.info("养护系统修改用户状态成功");
                    break;
            }
        }
    }
}