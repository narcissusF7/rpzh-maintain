package com.rpzh.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * @author wbl
 * @description [养护数量前端传入类]
 */
@Data

public class MaintainCountDto {

    private String materialCode;
    /**
     * 开始时间
     */
    private String startTime;
    /**
     * 结束时间
     */
    private String endTime;
    /**
     * 养护方式 (1:常规养护、2:机械调控)
     */
    private String maintenceStyle;
    /**
     * 阶段 （1:养护阶段 2:充氮杀虫阶段 3:磷化铝杀虫阶段 4:充氮保质阶段）
     */
    private String phases;

    private String level;

    private String pidRoute;

    private Integer index;

    /**
     * 第几页
     */
    private Integer page = 1;
    /**
     * 有几条数据
     */
    private Integer size = 10;

    private Integer stackId;
}
