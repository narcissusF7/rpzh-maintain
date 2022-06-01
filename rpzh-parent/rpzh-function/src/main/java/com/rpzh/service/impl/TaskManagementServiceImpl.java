package com.rpzh.service.impl;

import com.rpzh.dao.TaskManagementDao;
import com.rpzh.dto.TaskDto;
import com.rpzh.service.TaskManagementService;
import com.rpzh.util.TokenDecode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * 任务管理中心
 * @Author liubc
 * @create 2022-05-30 11:33
 * @Description: good good study,day day up
 */
@Service
public class TaskManagementServiceImpl implements TaskManagementService {

    @Autowired
    private TaskManagementDao taskManagementDao;



    /**
     * 新建任务
     *
     * @param taskDto
     * @return
     */
    @Override
    public int addTask(TaskDto taskDto) {
        //获取推送方式
        String pushWay = taskDto.getPushWay();
        //TODO 根据不同的推送方式推送,后期增加
        //获取用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户账号
        String username = userInfo.get("username");
        taskDto.setCreatedUser(username);
        String s = UUID.randomUUID().toString();
        System.out.println(s);
        int addFlag = taskManagementDao.addTask(taskDto);
        return addFlag;
    }
}