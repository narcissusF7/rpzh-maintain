package com.rpzh.dao;

import com.rpzh.dto.TaskDto;
import com.rpzh.vo.TaskVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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


    /**
     * 查询任务列表
     * @param taskDto
     * @return
     */
    List<TaskVo> findTask(@Param("taskDto") TaskDto taskDto);

    /**
     * mq修改任务状态
     * @param msg
     * @param taskStatus
     * @return
     */
    int mqUpdateStatus(@Param("msg")String msg, @Param("taskStatus")String taskStatus);

    /**
     * 任务中心删除任务
     * @param ids
     * @return
     */
    int delTask(@Param("ids") Long[] ids);

    /**
     * 查询任务详情
     * @param id
     * @return
     */
    TaskVo findTaskDetail(@Param("id") Long id);

    /**
     * 工作任务查询列表
     * @param taskDto
     * @return
     */
    List<TaskVo> findTaskByUser(@Param("taskDto") TaskDto taskDto);
}