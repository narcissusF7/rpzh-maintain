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

    /**
     * 任务处理
     * @param taskDto
     * @return
     */
    int taskDispose(@Param("taskDto") TaskDto taskDto);

    /**
     * 任务评价
     * @param taskDto
     * @return
     */
    int taskEvaluate(@Param("taskDto") TaskDto taskDto);

    /**
     * 根据位置位置路由和任务类型查询任务详情
     *
     * @param siteRoute
     * @return
     */
    TaskVo findTaskDetailBySiteId(@Param("siteRoute") String siteRoute, @Param("type") String type);

    /**
     * 查询任务图片
     * @param id
     * @return
     */
    List<TaskVo> findPicMes(@Param("id") String id);

    /**
     * 修改任务状态
     * @param id
     * @param status
     * @return
     */
    int updateTaskStatus(@Param("id")String id,@Param("status")String status,@Param("dealResult") String dealResult);

    /**
     * 虫情数据插入虫情板
     * @param pestBoardId
     * @param gatherSite
     * @param beetle
     * @param mealworm
     * @param nowTime
     * @return
     */
    int insertPest(@Param("pestBoardId")String pestBoardId, @Param("gatherSite")String gatherSite, @Param("beetle")int beetle,
                   @Param("mealworm") int mealworm, @Param("nowTime") String nowTime);

    /**
     * 查询所有的虫情板
     * @param siteRoute
     * @return
     */
    List<String> findPestBoard(@Param("siteRoute")String siteRoute);

    /**
     * 一键上报虫情
     * @param pestBoard
     * @param taskAddress
     * @param nowTime
     * @return
     */
    int oneButtonPestReport(@Param("pestBoard")String pestBoard, @Param("taskAddress")String taskAddress, @Param("nowTime")String nowTime);

    /**
     * 查询垛位下所有的设备
     * @param siteRoute
     * @return
     */
    List<TaskVo> findAllNodeByStack(@Param("siteRoute") String siteRoute);

    /**
     * 任务中心移垛任务处理
     * @param nodeId
     * @param x
     * @param y
     * @param z
     * @param siteRoute
     * @param siteId
     * @param siteName
     * @return
     */
    int updateNodeAddress(@Param("nodeId")String nodeId,@Param("x") int x, @Param("y")int y,
                          @Param("z")int z, @Param("siteRoute")String siteRoute,
                          @Param("siteId")String siteId,@Param("siteName") String siteName,@Param("stackId") String stackId);

    /**
     * 翻垛后,修改设备位置
     * @param nodeId
     * @param x
     * @param y
     * @param z
     * @return
     */
    int updateNodeXYZ(@Param("nodeId")String nodeId, @Param("x")String x, @Param("y")String y, @Param("z")String z);
}