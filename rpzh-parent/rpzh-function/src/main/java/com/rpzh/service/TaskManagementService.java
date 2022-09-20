package com.rpzh.service;

import com.github.pagehelper.PageInfo;
import com.rpzh.dto.TaskDto;
import com.rpzh.vo.TaskVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

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

    int findWarmimgCountBySiteId(Integer siteId);

    int findMaintainCountBySiteId(Integer siteId);

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
     * 根据位置id和任务类型查询任务详情
     *
     * @param stackId
     * @return
     */
    TaskVo findTaskDetailBySiteId(Integer stackId,Integer areaId,String type);

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

    /**
     * 根据任务id查询任务图片
     * @param id
     * @return
     */
    List<TaskVo> findPicMes(String id);

    /**
     * 虫情监控任务处理
     * @param pestData
     * @param dealResult
     * @return
     */
    int pestTaskDeal(String pestData, String dealResult,String id,String siteRoute);

    /**
     * 一键虫情上报
     * @param siteRoute
     * @return
     */
    int oneButtonPestReport(String siteRoute,String taskAddress);

    /**
     * 根据位置路径,查询所有的设备,进行移垛处理
     * @param siteRoute
     * @return
     */
    List<TaskVo> findAllNodeByStack(String siteRoute);

    /**
     * 移垛处理
     * @param taskDto
     * @return
     */
    int moveStackDeal(TaskDto taskDto);

    /**
     * 翻垛任务处理
     * @param taskDto
     * @return
     */
    int flipStackDeal(TaskDto taskDto);

    /**
     * 任务进度统计
     * @param taskDto
     * @return
     */
    List<Map> taskScheduleCount(TaskDto taskDto);

    /**
     * 任务量趋势统计
     * @param taskDto
     * @return
     */
    List<Map> taskCountTrend(TaskDto taskDto);

    /**
     * 查询各种类型的任务数量
     * @param taskDto
     * @return
     */
    List<Map> taskCountByType(TaskDto taskDto);

    /**
     * 任务平均完成时间
     * @param taskDto
     * @return
     */
    List<Map> taskAvlFinishTime(TaskDto taskDto);

    /**
     * 查询垛位下所有的虫情板
     * @param siteRoute
     * @return
     */
    List<String> findPestBoardByRoute(String siteRoute);

    /**
     * 查询所有的过期任务
     * @return
     */
    List<Map> findAllOverdueTask();

    /**
     * 虫情任务处理提交后返回虫数量
     * @param id
     * @return
     */
    List<Map> pestCountDetail(String id);

    /**
     * 统计类型占比查询数量
     * @param taskDto
     * @return
     */
    Map taskCountByTypeMap(TaskDto taskDto);

    /**
     * 查询是否存在指定位置指定类型的任务
     * @param siteRoute
     * @param type
     * @return
     */
    Map findTaskBySiteRoute(String siteRoute, String type);

    /**
     * 添加数据处理发送过来的任务
     * @param taskDto
     * @return
     */
    int addDataHandTask(TaskDto taskDto);

    List<String> findSystemType(@Param("ids") Long[] ids);
}