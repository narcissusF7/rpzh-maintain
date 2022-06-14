package com.rpzh.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rpzh.dao.*;
import com.rpzh.dto.EarlyWarningDto;
import com.rpzh.entity.*;
import com.rpzh.service.EarlyWarningService;
import com.rpzh.util.DateTypeUtils;
import com.rpzh.util.StringUtils;
import com.rpzh.vo.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @Author: zyq
 * @Date: 2022/05/20/14:46
 * @Description: 功能
 */
@Service
public class EarlyWarningServiceImpl implements EarlyWarningService {
    @Resource
    private EarlyWarningDao earlyWarningDao;
    @Resource
    private MaintainPlanDao maintainPlanDao;
    @Resource
    private AlarmValueSettingDao alarmValueSettingDao;
    @Resource
    private WarehouseDao warehouseDao;

    @Override
    public PageInfo<EarlyWarningVo> findHistoryWarning(EarlyWarningDto earlyWarningDto) {
        String pidRoute = earlyWarningDto.getPidRoute();
        Integer stackId = earlyWarningDto.getStackId();
        List<EarlyWarningVo> earlyWarningInfos = new ArrayList<>();
        Date endTime = earlyWarningDto.getEndTime();
        if(ToolUtil.isNotEmpty(endTime)){
            earlyWarningDto.setEndTime(new Date(endTime.getTime() + 24 * 60 * 60 * 1000));
        }
        if(StringUtils.isNotEmpty(pidRoute)){
            int length = pidRoute.split(",").length;
            List<Integer> stackIds = new ArrayList<>();
            switch (length){
                case 4:
                    stackIds = warehouseDao.findStackByAreId(stackId);
                    if(ToolUtil.isEmpty(stackIds)){
                        stackIds.add(0);
                    }
                    earlyWarningDto.setStackIds(stackIds);
                    break;
                case 5:
                    stackIds.add(stackId);
                    earlyWarningDto.setStackIds(stackIds);
                    break;
                default:
                    break;
            }
            PageHelper.startPage(earlyWarningDto.getPage(), earlyWarningDto.getSize());
            earlyWarningInfos = earlyWarningDao.findHistoryWarningInfo(earlyWarningDto);
            return new PageInfo<>(earlyWarningInfos);
        }
        PageHelper.startPage(earlyWarningDto.getPage(), earlyWarningDto.getSize());
        earlyWarningInfos = earlyWarningDao.findHistoryWarningData(earlyWarningDto);
        return new PageInfo<>(earlyWarningInfos);
    }

    @Override
    public Result findWarningCount(EarlyWarningDto earlyWarningDto) {
        String pidRoute = earlyWarningDto.getPidRoute();
        Integer stackId = earlyWarningDto.getStackId();
        Date endTime = earlyWarningDto.getEndTime();
        //结束时间不为空
        if(ToolUtil.isNotEmpty(endTime)){
            earlyWarningDto.setEndTime(new Date(endTime.getTime() + 24 * 60 * 60 * 1000 - 1));
        }
        //开始时间和结束时间为空时，开始时间为当月1号 00:00:00,结束时间为下个月1号 00:00:00
        if(ToolUtil.isEmpty(earlyWarningDto.getStartTime()) && ToolUtil.isEmpty(earlyWarningDto.getEndTime())){
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, 1);// 设置为1号,当前日期既为本月第一天
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            earlyWarningDto.setStartTime(new Date(calendar.getTime().getTime()));
            calendar.add(Calendar.MONTH, 1);
            earlyWarningDto.setEndTime(new Date(calendar.getTime().getTime() - 1000));
        }
        //根据位置路由和位置id设置仓间id和垛位id
        if(StringUtils.isNotEmpty(pidRoute)) {
            int length = pidRoute.split(",").length;
            List<Integer> stackIds = new ArrayList<>();
            switch (length) {
                case 4:
                    stackIds = warehouseDao.findStackByAreId(stackId);
                    if (ToolUtil.isEmpty(stackIds)) {
                        stackIds.add(0);
                    }
                    earlyWarningDto.setStackIds(stackIds);
                    break;
                case 5:
                    stackIds.add(stackId);
                    earlyWarningDto.setStackIds(stackIds);
                    break;
                default:
                    break;
            }
        }
        //查询预警总数量
        earlyWarningDto.setCount(earlyWarningDao.findSum(earlyWarningDto));
        //报警类型所占的百分比
        List<WarningOrDealVo> count = earlyWarningDao.findCountByWarningType(earlyWarningDto);
        if(count.size()>0) {
            for (int i = 1; i <= 3; i++) {
                if (!count.get(i-1).getWarningType().equals("0" + i)) {
                    WarningOrDealVo warningOrDealVo = new WarningOrDealVo();
                    warningOrDealVo.setWarningType("0" + i);
                    warningOrDealVo.setPercentage(0d);
                    count.add(i - 1, warningOrDealVo);
                }
            }
            WarningOrDealVo warningOrDealVo = count.get(0);
            warningOrDealVo.setPercentage(count.get(0).getPercentage() + count.get(1).getPercentage() + count.get(2).getPercentage());
            count.remove(2);
            count.remove(1);
            if(warningOrDealVo.getPercentage()==0d){
                count.remove(0);
            }
        }
        //已处理和未处理所占的比率
        List<WarningOrDealVo> dealCount = earlyWarningDao.findCountByDeal(earlyWarningDto);
        //查询各种报警类型的数量
        List<WarningOrDealVo> countInfo =getMonthCountInfo();
        //报警数量统计
        List<WarningOrDealVo> dayCount = earlyWarningDao.findCountByDay(earlyWarningDto);
        Integer dayType = earlyWarningDto.getDayType();
        if(dayCount.size()>0) {
            Map<String, WarningOrDealVo> listDate = DateTypeUtils.getDayCountList(earlyWarningDto.getStartTime(), earlyWarningDto.getEndTime(), dayType);
            for (WarningOrDealVo warningOrDealVo : dayCount) {
                listDate.put(warningOrDealVo.getDayType(), warningOrDealVo);
            }
            dayCount = new ArrayList<>();
            for (Map.Entry<String, WarningOrDealVo> map : listDate.entrySet()) {
                dayCount.add(map.getValue());
            }
        }
        //报警处理率统计
        List<WarningOrDealVo> isDealCount = earlyWarningDao.findCountByIsDeal(earlyWarningDto);
        if(isDealCount.size()>0) {
            Map<String, WarningOrDealVo> listDate = DateTypeUtils.getIsDealCountList(earlyWarningDto.getStartTime(), earlyWarningDto.getEndTime(), dayType);
            for (WarningOrDealVo warningOrDealVo : isDealCount) {
                listDate.put(warningOrDealVo.getDayType()+warningOrDealVo.getDeal(), warningOrDealVo);
            }
            isDealCount = new ArrayList<>();
            for (Map.Entry<String, WarningOrDealVo> map : listDate.entrySet()) {
                isDealCount.add(map.getValue());
            }
        }
        Collections.sort(isDealCount, new Comparator<WarningOrDealVo>(){
            public int compare(WarningOrDealVo o1, WarningOrDealVo o2) {

                if( Integer.parseInt(o2.getDayType())<Integer.parseInt(o1.getDayType()))
                    return 1;
                else if(Integer.parseInt(o2.getDayType())==Integer.parseInt(o1.getDayType())){
                    if(o1.getDeal()>(o2.getDeal()) )
                        return 1;
                    else
                        return -1;
                }
                else
                    return -1;
            }
        });

        Map<String,Object> map = new HashMap<>();
        map.put("count_type",count);
        map.put("countInfo",countInfo);
        map.put("dealCount",dealCount);
        map.put("dayCount",dayCount);
        map.put("isDealCount",isDealCount);
        return new Result(true, StatusCode.OK, "查询报警统计成功",map);
    }

    public List<WarningOrDealVo> getMonthCountInfo(){
        List<WarningOrDealVo> countInfo =earlyWarningDao.findCountByMonth();
        List<String> type = new ArrayList<>();
        countInfo.forEach((countVo) ->{type.add(countVo.getWarningType());});
        for (int i = 1; i < 15;i++) {
            String flag= "0" + i;
            if(i>=10){
                flag= "" + i;
            }
            if (!type.contains(flag)) {
                WarningOrDealVo warningOrDealVo = new WarningOrDealVo();
                warningOrDealVo.setWarningType(flag);
                warningOrDealVo.setCount(0);
                countInfo.add(warningOrDealVo);
            }
        }
        countInfo.sort((v1, v2) -> v1.getWarningType().compareTo(v2.getWarningType()));
        countInfo.get(0).setCount(countInfo.get(0).getCount()+countInfo.get(1).getCount()+countInfo.get(2).getCount());
        countInfo.remove(2);
        countInfo.remove(1);
        return countInfo;
    }
    @Override
    public Result findEarlyWarningInfo(EarlyWarningDto earlyWarningDto) {
        List<WarningCountVo> warningCount = earlyWarningDao.findEarlyWarningCount(earlyWarningDto);
        if(warningCount.size()==0){
            return new Result<>(true, StatusCode.OK, "查询成功,无预警信息");
        }
        String warningType = warningCount.get(0).getWarningType();
        earlyWarningDto.setWarningType(warningType);
        String type = "";
        for (WarningCountVo warningCountVo : warningCount) {
            type = type+" "+warningCountVo.getWarningType();
        }
        WarningCountVo warningCountVo = new WarningCountVo();
        if(!type.contains("01")){
            warningCountVo.setWarningType("01");
            warningCount.add(warningCountVo);
        }
        if(!type.contains("02")){
            warningCountVo.setWarningType("02");
            warningCount.add(warningCountVo);
        }
        if(!type.contains("03")){
            warningCountVo.setWarningType("03");
            warningCount.add(warningCountVo);
        }
        if(!type.contains("04")){
            warningCountVo.setWarningType("04");
            warningCount.add(warningCountVo);
        }
        if(!type.contains("05")){
            warningCountVo.setWarningType("05");
            warningCount.add(warningCountVo);
        }
        if(!type.contains("06")){
            warningCountVo.setWarningType("06");
            warningCount.add(warningCountVo);
        }
        if(!type.contains("07")){
            warningCountVo.setWarningType("07");
            warningCount.add(warningCountVo);
        }
        if(!type.contains("08")){
            warningCountVo.setWarningType("08");
            warningCount.add(warningCountVo);
        }
        if(!type.contains("09")){
            warningCountVo.setWarningType("09");
            warningCount.add(warningCountVo);
        }
        WarningVo warningVo = new WarningVo();
        warningVo.setWarningCount(warningCount);
        List<EarlyWarningVo> earlyWarningVos = earlyWarningDao.findEarlyWarning(earlyWarningDto);
        if(warningType.equals("01")){
            earlyWarningVos.forEach(p -> p.setWarningNews("设备丢包"));
        }
        warningVo.setEarlyWarning(earlyWarningVos);
        return new Result<>(true, StatusCode.OK, "查询成功",warningVo);
    }
    @Override
    public Result findEarlyWarning(EarlyWarningDto earlyWarningDto) {
        if(ToolUtil.isEmpty(earlyWarningDto.getWarningType())){
            return new Result<>(false, StatusCode.ERROR, "预警类型不能为空");
        }
        List<EarlyWarningVo> earlyWarningVos = earlyWarningDao.findEarlyWarning(earlyWarningDto);
        if(earlyWarningDto.getWarningType().equals("01")){
            earlyWarningVos.forEach(p -> p.setWarningNews("设备丢包"));
        }
        return new Result<>(true, StatusCode.OK, "查询成功",earlyWarningVos);
    }

    @Override
    public Result findDetailsEarlyWarning(EarlyWarningDto earlyWarningDto) {
        MaintainPlanVo maintainPlan = maintainPlanDao.findMaintainStackId(earlyWarningDto.getStackId());
        //String newDate = earlyWarningDao.findNewDate(earlyWarningDto);
        AlarmValueSettingVo alarmValueSetting = alarmValueSettingDao.findAlarmValueSettingById(maintainPlan.getAlarmValueSettingId());
        /*switch (earlyWarningDto.getWarningType()){
            case ;
            case ;
            case ;
        }*/
        return null;
    }

}
