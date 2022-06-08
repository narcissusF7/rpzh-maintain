package com.rpzh.service;

import com.github.pagehelper.PageInfo;
import com.rpzh.dto.TaskDto;
import com.rpzh.vo.TaskVo;

/**
 * @Author liubc
 * @create 2022-05-30 11:33
 * @Description: good good study,day day up
 */
public interface TaskManagementService {

    /**
     * 新建任务
     * @param taskDto
     * @return
     */
    int addTask(TaskDto taskDto);

    /**
     * 查询任务列表
     * @param taskDto
     * @return
     */
    PageInfo<TaskVo> findTask(TaskDto taskDto);

    /**
     * 修改任务状态
     * @param msg
     * @return
     */
    int mqUpdateStatus(String msg);

    /**
     * 批量删除任务
     * @param ids
     * @return
     */
    int delTask(Long[] ids);

    /**
     * 查询任务详情
     * @param id
     * @return
     */
    TaskVo findTaskDetail(Long id);

    /**
     * 工作任务查询任务列表
     * @param taskDto
     * @return
     */
    PageInfo<TaskVo> findTaskByUser(TaskDto taskDto);

    /**
     * 任务处理
     * @param taskDto
     * @return
     */
    int taskDispose(TaskDto taskDto);

    /**
     * 任务评价
     * @param taskDto
     * @return
     */
    int taskEvaluate(TaskDto taskDto);
}