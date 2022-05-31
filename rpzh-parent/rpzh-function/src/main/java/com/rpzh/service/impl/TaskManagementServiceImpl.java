package com.rpzh.service.impl;

import cn.hutool.core.date.DateUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rpzh.dao.TaskManagementDao;
import com.rpzh.dto.TaskDto;
import com.rpzh.service.TaskManagementService;
import com.rpzh.util.TokenDecode;
import com.rpzh.vo.TaskVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
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
        //设置创建人
        taskDto.setCreatedUser(username);
        //随机生成任务编号
        String uuid = UUID.randomUUID().toString().replace("-", "");
        //设置任务编号
        taskDto.setTaskNum(uuid);
        //获取当前时间
        String nowDayTime = DateUtil.dateNew(new Date()).toString();
        taskDto.setCreatedTime(nowDayTime);
        int addFlag = taskManagementDao.addTask(taskDto);
        return addFlag;
    }

    /**
     * 查询任务列表
     * @param taskDto
     * @return
     */
    @Override
    public PageInfo<TaskVo> findTask(TaskDto taskDto) {
        int page = taskDto.getPage();
        int size = taskDto.getSize();
        //静态分页
        PageHelper.startPage(page, size);
        List<TaskVo> taskVoList = taskManagementDao.findTask(taskDto);

        return new PageInfo<TaskVo>(taskVoList);
    }
}