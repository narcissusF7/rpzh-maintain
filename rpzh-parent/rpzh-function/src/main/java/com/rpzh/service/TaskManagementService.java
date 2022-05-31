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
}