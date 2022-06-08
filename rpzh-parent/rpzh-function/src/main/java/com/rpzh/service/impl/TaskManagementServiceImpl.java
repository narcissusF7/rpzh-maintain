package com.rpzh.service.impl;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rpzh.dao.TaskManagementDao;
import com.rpzh.dto.TaskDto;
import com.rpzh.service.TaskManagementService;
import com.rpzh.util.TokenDecode;
import com.rpzh.vo.TaskVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
@Slf4j
public class TaskManagementServiceImpl implements TaskManagementService {

    @Autowired
    private TaskManagementDao taskManagementDao;

    @Autowired
    private RabbitTemplate rabbitTemplate;


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
        //获取结束时间,用于计算时间差
        String endTime = taskDto.getEndTime();
        Date date1 = DateUtil.parse(endTime);
        //设置当前时间,用于计算时间差
        Date date2 = DateUtil.parse(nowDayTime);
        //当前时间与结束时间相差的毫秒数
        long betweenMs = DateUtil.between(date1, date2, DateUnit.MS);
        //当前时间与结束时间相差的毫秒字符串
        String betweenMsStr = String.valueOf(betweenMs);
        //向mq中丢一条消息,当任务到期时候未处理,修改任务的状态
        rabbitTemplate.convertAndSend("queue1", (Object) uuid, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                //消息过期设置
                message.getMessageProperties().setExpiration(betweenMsStr);
                return message;
            }
        });
        log.info("新建任务发送消息成功:"+uuid+":结束时间:"+endTime+":相差时间为:"+betweenMsStr);
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

    /**
     * 修改任务状态
     *
     * @param msg
     * @return
     */
    @Override
    public int mqUpdateStatus(String msg) {
        //只有未完成的任务才能逾期
        String taskStatus = "0";
        int mqUpdateFlag = taskManagementDao.mqUpdateStatus(msg,taskStatus);
        return mqUpdateFlag;
    }

    /**
     * 批量删除任务
     *
     * @param ids
     * @return
     */
    @Override
    public int delTask(Long[] ids) {
        int delFlag = taskManagementDao.delTask(ids);
        return delFlag;
    }

    /**
     * 查询任务详情
     *
     * @param id
     * @return
     */
    @Override
    public TaskVo findTaskDetail(Long id) {
        TaskVo taskVo = taskManagementDao.findTaskDetail(id);
        return taskVo;
    }

    /**
     * 工作任务查询任务列表
     * @param taskDto
     * @return
     */
    @Override
    public PageInfo<TaskVo> findTaskByUser(TaskDto taskDto) {
        //获取用户信息,只查询自己的任务
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户账号
        String username = userInfo.get("username");
        //设置负责人,只能看到自己的任务
        taskDto.setPrincipal(username);
        int page = taskDto.getPage();
        int size = taskDto.getSize();
        //静态分页
        PageHelper.startPage(page, size);
        List<TaskVo> taskVoList = taskManagementDao.findTaskByUser(taskDto);
        return new PageInfo<TaskVo>(taskVoList);
    }

    /**
     * 任务处理
     *
     * @param taskDto
     * @return
     */
    @Override
    public int taskDispose(TaskDto taskDto) {
        //获取当前时间,任务完成的时间
        String nowTime = DateUtil.dateNew(new Date()).toString();
        //设置完成时间
        taskDto.setFinishTime(nowTime);
        //设置任务状态为完成
        taskDto.setStatus("1");
        int updateFlag = taskManagementDao.taskDispose(taskDto);
        return updateFlag;
    }

    /**
     * 任务评价
     * @param taskDto
     * @return
     */
    @Override
    public int taskEvaluate(TaskDto taskDto) {
        //获取用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户账号
        String username = userInfo.get("username");
        //设置评价人
        taskDto.setEvaluateUser(username);
        //获取当前时间
        String nowTime = DateUtil.dateNew(new Date()).toString();
        //设置任务评价时间
        taskDto.setEvaluateTime(nowTime);
        int updateFlag = taskManagementDao.taskEvaluate(taskDto);
        return updateFlag;
    }
}