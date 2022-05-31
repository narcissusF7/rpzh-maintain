package com.rpzh.service;

import com.rpzh.dto.TaskDto;

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
}