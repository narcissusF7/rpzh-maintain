package com.rpzh.controller;

import com.github.pagehelper.PageInfo;
import com.rpzh.dto.AlarmValueSettingDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.service.MaintainPlanService;
import com.rpzh.dto.MaintainPlanDto;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import com.rpzh.vo.MaintainPlanVo;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author wbl
 * @description [养护计划控制层]
 */
@RestController
@RequestMapping("/maintain/maintainPlan")
@CrossOrigin
public class MaintainPlanController {

    /**
     * 养护服务层
     */
    @Resource
    private MaintainPlanService maintainPlanService;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(true);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }

    /**
     * 插入一条养护计划
     *
     * @param maintainPlanDto 前端传入添加信息
     * @return success or failure
     * @createUser wbl
     */
    @PostMapping(value = "/insert")
    public Result insertMaintainPlan(MaintainPlanDto maintainPlanDto){
        // 烟叶代码是否为null
        if (ToolUtil.isEmpty(maintainPlanDto.getMaterialCode()) || ToolUtil.isEmpty(maintainPlanDto.getTobaccoYear())) {
            return new Result(false, StatusCode.ERROR, "烟叶代码或烟叶年份不能为空!");
        }
        if (ToolUtil.isEmpty(maintainPlanDto.getStackName())) {
            return new Result(false, StatusCode.ERROR, "仓库位置不能为空!");
        }
        if (ToolUtil.isEmpty(maintainPlanDto.getPhases()) || ToolUtil.isEmpty(maintainPlanDto.getMaintenceStyle())) {
            return new Result(false, StatusCode.ERROR, "养护方式或养护阶段没有选择!");
        }
        if (ToolUtil.isEmpty(maintainPlanDto.getMaintainDays())) {
            return new Result(false, StatusCode.ERROR, "养护天数不能为空!");
        }
        if (ToolUtil.isEmpty(maintainPlanDto.getAftercareDateStart()) || ToolUtil.isEmpty(maintainPlanDto.getOxygenReductionTime())
                || ToolUtil.isEmpty(maintainPlanDto.getExpireTime())) {
            return new Result(false, StatusCode.ERROR, "调养日期,降氧完成时间,到期提醒时间不能为空!");
        }
        if (ToolUtil.isEmpty(maintainPlanDto.getMaintainCount())) {
            return new Result(false, StatusCode.ERROR, "养护数量没有填写!");
        }
        if (ToolUtil.isEmpty(maintainPlanDto.getPrincipalName())) {
            return new Result(false, StatusCode.ERROR, "负责人没有填写!");
        }
        AlarmValueSettingDto alarmValueSettingDto = new AlarmValueSettingDto(maintainPlanDto);
        String earlyWarningRuleName = alarmValueSettingDto.getEarlyWarningRuleName();
        if (ToolUtil.isEmpty(earlyWarningRuleName)) {
            return new Result(false, StatusCode.ERROR, "预警规则名称不能为空!");
        }
        int result = maintainPlanService.insertMaintainPlan(maintainPlanDto);
        return new Result<>(true, StatusCode.OK, "养护计划添加成功",result);
    }

    /**
     * 分页查询养护计划
     * @param maintainPlanDto 前端传入添加信息
     * @return Result
     * @createUser wbl
     * @updateUser zyq
     */
    @PostMapping(value = "/findMaintainPlanInfo")
    public Result findMaintainPlanInfo(MaintainPlanDto maintainPlanDto) {
        PageInfo<MaintainPlanVo> maintainPlanInfo = maintainPlanService.findMaintainPlanInfo(maintainPlanDto);
        return new Result(true, StatusCode.OK, "查询成功!", maintainPlanInfo);
    }

    /**
     * 删除养护计划
     * @param stageIds  养护计划id
     * @return Result
     * @createUser wbl
     * @updateUser zyq
     */
    @PostMapping("/deleteMaintainPlanInfo")
    public Result<Integer> deleteMaintainPlanInfo(@RequestParam("stageIds") List<Integer> stageIds) {
        Result result = maintainPlanService.deleteMaintainPlanInfo(stageIds);
        return result;
    }

    /**
     * 根据养护id查询养护详情/更改养护计划时回显
     *
     * @param id 养护计划id
     * @return success or failure
     * @createUser wbl
     */
    @PostMapping("/findMaintainPlanById")
    public Result findMaintainPlanById(Integer id) {
        MaintainPlanVo maintainPlan = maintainPlanService.findMaintainPlan(id);
        return new Result<>(true, StatusCode.OK, "查询养护详情成功",maintainPlan);
    }

    /**
     * 更改养护计划
     * @param maintainPlanDto
     * @return
     */
    @PostMapping("/updateMaintainPlan")
    public Result updateMaintainPlan(MaintainPlanDto maintainPlanDto) {
        Integer i = maintainPlanService.updateMaintainPlan(maintainPlanDto);
        return new Result<>(true, StatusCode.OK, "更改养护计划成功",i);
    }

    /**
     * 养护统计报表
     * @param maintainPlanDto
     * @return
     */
    @PostMapping(value = "/findMaintainCountReport")
    public Result findMaintainCountReport(MaintainPlanDto maintainPlanDto) {
        PageInfo<MaintainPlanVo> maintainPlanVos = maintainPlanService.findMaintainCountReport(maintainPlanDto);
        return new Result(true, StatusCode.OK, "查询养护统计报表成功",maintainPlanVos);
    }

    /**
     * 养护当月新建报表/月底养护报表
     * @param maintainPlanDto
     * @return
     */
    @GetMapping(value = "/findMaintainMonthEndReport")
    public Result findMaintainMonthEndReport(MaintainPlanDto maintainPlanDto) {
        PageInfo<MaintainPlanVo> maintainPlanVos = maintainPlanService.findMaintainMonthEndReport(maintainPlanDto);
        return new Result(true, StatusCode.OK, "查询养护统计报表成功",maintainPlanVos);
    }

    /**
     * 历史汇总单
     * @param maintainPlanDto
     * @return
     */
    @PostMapping(value = "/findMaintainHistoryCollect")
    public Result findMaintainHistoryCollect(MaintainPlanDto maintainPlanDto) {
        PageInfo<MaintainPlanVo> maintainPlanVos = maintainPlanService.findMaintainHistoryCollect(maintainPlanDto);
        return new Result(true, StatusCode.OK, "查询历史汇总单成功",maintainPlanVos);
    }

    /**
     * 开垛使用
     * @param maintainPlanDto
     * @return
     */
    @PostMapping(value = "/endMaintainPlan")
    public Result endMaintainPlan(MaintainPlanDto maintainPlanDto) {
        Integer i = maintainPlanService.endMaintainPlan(maintainPlanDto);
        return new Result<>(true, StatusCode.OK, "开垛成功",i);
    }

    /**
     * 养护到期汇总中的删除
     * @param ids
     * @return
     */
    @PostMapping("/deleteMaintainPlanStage")
    public Result deleteMaintainStage(@RequestParam("ids")List<Integer> ids) {
        Integer i = maintainPlanService.deleteMaintainStage(ids);
        return new Result<>(true, StatusCode.OK, "删除成功",i);
    }

    /**
     * 养护到期汇总中的详情
     * @param id
     * @return
     */
    @PostMapping("/findMaintainPlanDetails")
    public Result findMaintainPlanDetails(Integer id) {
        MaintainPlanVo maintainPlanVo = maintainPlanService.findMaintainPlanDetails(id);
        return new Result<>(true, StatusCode.OK, "查询成功",maintainPlanVo);
    }

    /**
     * 转段
     * @param maintainPlanDto
     * @return
     */
    @PostMapping("/changeMaintainPlanStage")
    public Result changeMaintainPlanStage(MaintainPlanDto maintainPlanDto) {
        Integer result = maintainPlanService.changeMaintainPlanStage(maintainPlanDto);
        return new Result<>(true, StatusCode.OK, "转段成功",result);
    }

    @PostMapping("/findExpireCollectTable")
    public Result findExpireCollectTable(MaintainPlanDto maintainPlanDto) {
        Result maintainPlan = maintainPlanService.findExpireCollectTable(maintainPlanDto);
        return maintainPlan;
    }

    @PostMapping("/getPhases")
    public Result getPhases(String maintenceStyle) {
        List<String> phases = new ArrayList<>();
        phases.add("养护阶段");
        phases.add("充氮杀虫阶段");
        phases.add("磷化铝杀虫阶段");
        if("机械调控方式".equals(maintenceStyle)){
            phases.add("充氮保质阶段");
        }
        return new Result<>(true, StatusCode.OK, "查询养护阶段成功",phases);
    }
}
