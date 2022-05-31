package com.rpzh.dao;

import com.rpzh.dto.TaskDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 任务中心
 * @Author liubc
 * @create 2022-05-30 11:33
 * @Description: good good study,day day up
 */
@Mapper
public interface TaskManagementDao {


    /**
     * 添加任务
     * @param taskDto
     * @return
     */
    int addTask(@Param("taskDto") TaskDto taskDto);
}