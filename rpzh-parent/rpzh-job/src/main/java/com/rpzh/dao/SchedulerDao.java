package com.rpzh.dao;

import com.rpzh.entity.ScheduleJob;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author liubc
 * @create 2022-07-26 9:52
 * @Description: good good study,day day up
 */
@Mapper
public interface SchedulerDao {

    /**
     * 所有的定时任务
     * @return
     */
    List<ScheduleJob> findAllJob();

    /**
     * 新增定时任务
     * @param scheduleJob
     */
    void insert(@Param("scheduleJob") ScheduleJob scheduleJob);

    /**
     * 查询名称是否存在
     * @param jobGroup
     * @return
     */
    int findJobName(@Param("jobGroup") String jobGroup);

    /**
     * 根据任务id查询任务
     * @param jobId
     * @return
     */
    ScheduleJob findJobById(@Param("jobId") Long jobId);

    /**
     * 修改任务状态
     * @param jobId
     * @param cmd
     */
    void updateStatus(@Param("jobId")Long jobId, @Param("cmd")String cmd);

    /**
     * 更新定时任务执行时间
     * @param jobId
     * @param cron
     */
    void updateJob(@Param("jobId")Long jobId, @Param("cron")String cron);

    /**
     * 查询定时任务id
     * @return
     */
    List<Long> findJobId();
}
