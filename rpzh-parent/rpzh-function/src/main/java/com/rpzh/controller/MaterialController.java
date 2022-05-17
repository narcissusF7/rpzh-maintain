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
@CrossOrigin
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
    @PostMapping(value = "/getMaterialInfoBySiteId")
    public Result getMaterialInfoBySiteId(Integer stackId){
        if(stackId == null){
            return new Result(false, StatusCode.NULLERROR, "垛位id不能为空!");
        }
        MaterialVo materialVo = materialService.getMaterialInfoBySiteId(stackId);
        return new Result(true, StatusCode.OK, "查询成功!", materialVo);
    }

    @PostMapping(value = "/findMaterialCode")
    public Result findMaterialCode(Integer stackId){
        if(stackId == null){
            return new Result(false, StatusCode.NULLERROR, "垛位id不能为空!");
        }
        Integer stackIdCount = maintainPlanService.isExistStackId(stackId);
        if(stackIdCount>0){
            return new Result(false, StatusCode.EXISTERROR, "该垛位已存在养护计划!");
        }
        List<String> materialCode = materialService.findMaterialCode(stackId);
        if(materialCode.size()>0) {
            return new Result(true, StatusCode.OK, "查询物料代码成功!", materialCode);
        }
        return new Result(false, StatusCode.ERROR, "该垛位暂无物料代码,请重新选择垛位!");
    }

    @PostMapping(value = "/findYear")
    public Result findYear(Integer stackId,String materialCode){
        List<String> tobaccoYear = materialService.findYear(stackId,materialCode);
        return new Result(true, StatusCode.OK, "查询烟叶年份成功!", tobaccoYear);
    }
}
