package com.rpzh.controller;

import com.alibaba.fastjson.JSON;
import com.rpzh.dao.AlarmValueSettingDao;
import com.rpzh.dto.AlarmValueSettingDto;
import com.rpzh.dto.MaterialCodeYearDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.service.MaintainPlanService;
import com.rpzh.service.MaterialService;
import com.rpzh.vo.AlarmValueSettingVo;
import com.rpzh.vo.MaterialVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * @author zyq
 * @description [物料控制层]
 */
@RestController
@RequestMapping("/maintain/material")
@Slf4j
public class MaterialController {
    @Resource
    private MaterialService materialService;

    @Resource
    private MaintainPlanService maintainPlanService;

    /**
     * 通过位置id查找物料信息
     * @param stackId
     * @return
     */
    @GetMapping(value = "/getMaterialInfoBySiteId")
    public Result getMaterialInfoBySiteId(Integer stackId){
        Integer stackIdCount = maintainPlanService.isExistStackId(stackId);
        if(stackIdCount>0){
            return new Result(false, StatusCode.ERROR, "该垛位已存在养护计划!");
        }
        MaterialVo materialVo = materialService.getMaterialInfoBySiteId(stackId);
        return new Result(true, StatusCode.OK, "查询成功!", materialVo);
    }

    @GetMapping(value = "/findMaterialCode")
    public Result findMaterialCode(Integer siteId){
        List<String> materialCode = materialService.findMaterialCode(siteId);
        if(materialCode.size()>0) {
            return new Result(true, StatusCode.OK, "查询成功!", materialCode);
        }
        return new Result(false, StatusCode.ERROR, "该垛位暂无物料代码,请重新选择垛位!", materialCode);
    }

    @GetMapping(value = "/findYear")
    public Result findYear(Integer siteId,String materialCode){
        List<String> tobaccoYear = materialService.findYear(siteId,materialCode);
        return new Result(true, StatusCode.OK, "查询成功!", tobaccoYear);
    }
}
