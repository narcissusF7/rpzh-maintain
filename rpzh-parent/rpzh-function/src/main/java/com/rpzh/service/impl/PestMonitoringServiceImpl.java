package com.rpzh.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rpzh.constant.NodeConstant;
import com.rpzh.constant.WarningTypeConstant;
import com.rpzh.dao.EarlyWarningDao;
import com.rpzh.dao.GeneralSettingDao;
import com.rpzh.dao.PestMonitoringDao;
import com.rpzh.dao.RfidRelationshipDao;
import com.rpzh.dto.PestBoardDto;
import com.rpzh.dto.PestDataDto;
import com.rpzh.dto.PestMonitoringRuleDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.handler.DataHandler;
import com.rpzh.pojo.EarlyWarning;
import com.rpzh.pojo.GeneralSetting;
import com.rpzh.pojo.PestData;
import com.rpzh.service.PestMonitoringService;
import com.rpzh.util.MonitorCondition;
import com.rpzh.util.TokenDecode;
import com.rpzh.vo.EarlyWarningVo;
import com.rpzh.vo.PestBoardVo;
import com.rpzh.vo.PestDataVo;
import com.rpzh.vo.PestMonitoringRuleVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 虫情监控服务实现类
 *
 * @Author liujj
 * @create 2022-06-08 11:31
 * @Description: good good study,day day up
 */
@Service
public class PestMonitoringServiceImpl implements PestMonitoringService {

    @Autowired
    PestMonitoringDao pestMonitoringDao;

    @Autowired
    GeneralSettingDao generalSettingDao;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    EarlyWarningDao earlyWarningDao;

    @Autowired
    RfidRelationshipDao rfidRelationshipDao;

    @Autowired
    DataHandler dataHandler;

    public static final String SPECIAL_SITE_ROUTE = "SPECIAL_SITE_ROUTE";
    public static final String GENERAL_PEST_WARNING_VALUE = "GENERAL_PEST_WARNING_VALUE";

    public static final String PEST_MONITOR = "PEST_MONITOR_";
    public static final String PEST_BOARD = "PEST_BOARD_";

    /**
     * 添加虫情板
     *
     * @param pestBoardDto
     * @return
     */
    @Override
    public Result addPestBoard(PestBoardDto pestBoardDto) {
        if (ToolUtil.isEmpty(pestBoardDto.getPestBoardId())) {
            return new Result(false, StatusCode.ERROR, "虫情点位id不能为空");
        }
        if (ToolUtil.isEmpty(pestBoardDto.getSiteRoute())) {
            return new Result(false, StatusCode.ERROR, "位置路由不能为空");
        }
        if (ToolUtil.isEmpty(pestBoardDto.getRfid())) {
            return new Result(false, StatusCode.ERROR, "rfid不能为空");
        }
        if (ToolUtil.isEmpty(pestBoardDto.getSiteId())) {
            return new Result(false, StatusCode.ERROR, "位置id不能为空");
        }

        Integer pestBoardCount = pestMonitoringDao.findPestBoardCount(pestBoardDto.getPestBoardId());

        if (pestBoardCount > 0) {
            return new Result(false, StatusCode.ERROR, "虫情点位id已存在");
        }
        Integer rfidCount = rfidRelationshipDao.findRfidCount(pestBoardDto.getRfid());

        if (rfidCount > 0) {
            return new Result(false, StatusCode.ERROR, "该rfid已存在");
        }

        //获取用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户账号
        String username = userInfo.get("username");
        pestBoardDto.setCreatedUser(username);
        //根据选择的位置的路由id  判断是仓间还是垛内设备
        Integer siteLevel = pestBoardDto.getSiteRoute().split(",").length;
        pestBoardDto.setSiteType(siteLevel == 4 ? NodeConstant.NODE_WARE_AREA : NodeConstant.NODE_STACK);
        //拼接成自己的路由id
        pestBoardDto.setSiteRoute(pestBoardDto.getSiteRoute() + ",[0" + siteLevel + ":" + pestBoardDto.getSiteId() + "]");
        synchronized (this) {
            //通过selectKey 将主键id返回到Bean中
            pestMonitoringDao.addPestBoard(pestBoardDto);

//            redisTemplate.opsForValue().set(PEST_BOARD+pestBoardDto.getId(),JSONObject.toJSONString(pestBoardDto));
            //向rfid关系表插入数据
            rfidRelationshipDao.addRfidRelationship(pestBoardDto.getRfid(), "02", pestBoardDto.getId());
        }

        return new Result(true, StatusCode.OK, "添加成功");
    }

    /**
     * 删除虫情板
     *
     * @param ids
     * @return
     */
    @Override
    public Integer deletePestBoard(Integer[] ids) {
        //删除虫情板
        Integer delFalg = pestMonitoringDao.deletePestBoard(Arrays.asList(ids));
        //删除rfid与虫情板的绑定关系
        rfidRelationshipDao.deleteRfidRelationship(Arrays.asList(ids), "02");
//        for (int i = 0; i <ids.length ; i++) {
//            redisTemplate.delete(PEST_BOARD+ids[i]);
//        }

        return delFalg;
    }

    /**
     * 修改虫情规则配置
     *
     * @param pestMonitoringRuleDto
     * @return
     */
    @Override
    public Integer updatePestRule(PestMonitoringRuleDto pestMonitoringRuleDto) {
        //虫情预警值 预警颜色修改
        Integer updateFlag = pestMonitoringDao.updatePestMonitor(pestMonitoringRuleDto);

        putValueToCache();

        //虫情板替换周期修改  该值在通用表中
        GeneralSetting generalSetting = new GeneralSetting();
        generalSetting.setSettingName("replacePeriod");
        generalSetting.setSettingValue(pestMonitoringRuleDto.getReplacePeriod() + "");
        generalSettingDao.updateGeneralSetting(generalSetting);
        //虫情巡查时间修改
        Integer[] weekPatrol = pestMonitoringRuleDto.getWeekPatrol();
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < weekPatrol.length; i++) {
            map.put("x" + (i + 1), weekPatrol[i]);
        }
        pestMonitoringDao.updateWeekPatrol(map);
        String specialStack = pestMonitoringRuleDto.getSpecialStack();
        if (!ToolUtil.isEmpty(specialStack)){
            if (redisTemplate.hasKey(SPECIAL_SITE_ROUTE)){
                redisTemplate.delete(SPECIAL_SITE_ROUTE);
            }
            //删除数据库中的特殊垛位
            pestMonitoringDao.delSpecialSite();
            List<Map<String,Object>> list = JSONObject.parseObject(specialStack, List.class);
            list.forEach(specialStackMap ->{
                String siteRoute = specialStackMap.get("pidRoute")+"";
                siteRoute = siteRoute + ",[0" + siteRoute.split(",").length + ":" + specialStackMap.get("siteId") + "]";
                //将特殊位置存到数据库中
                pestMonitoringDao.insertSpecialSiteRoute(siteRoute, specialStackMap.get("little")+"",specialStackMap.get("middle")+"",specialStackMap.get("big")+"", specialStackMap.get("siteName")+"");
                //将特殊位置存到缓存中
                String specialWarningValue = specialStackMap.get("little")+"|"+specialStackMap.get("middle")+"|"+specialStackMap.get("big")+"";
                redisTemplate.opsForHash().put(SPECIAL_SITE_ROUTE, siteRoute, specialWarningValue);
            });
        }

        return updateFlag;
    }

    /**
     * 查询虫情监控规则设置
     *
     * @return
     */
    @Override
    public PestMonitoringRuleVo findPestRule() {
        //虫情巡查时间查询
        Map<String, Integer> weekPatrol = pestMonitoringDao.findWeekPatrol();

        //虫情板替换周期查询
        GeneralSetting generalSetting = new GeneralSetting();
        generalSetting.setSettingName("replacePeriod");
        Integer settingValue = Integer.parseInt(generalSettingDao.findGeneralSetting(generalSetting).get(0).getSettingValue());

        //虫情预警值 预警颜色查询
        Map<String, Object> pestMonitorValues = pestMonitoringDao.findPestMonitorValue();

        //查询特殊垛位
        List<Map<String, Object>> specialStacks = pestMonitoringDao.findSpecialSite();
        specialStacks.forEach(map ->{
            //用 ,分割
            String[] pidRoutes = map.get("pidRoute").toString().split(",");

            // 获取最后一段的id作为siteId
            String str = pidRoutes[pidRoutes.length-1];
            String siteId = str.substring(4,str.length()-1);

            //截取掉最后一段
            map.put("pidRoute", map.get("pidRoute").toString().split((",\\["+str.substring(1,str.length()-1)))[0]);
            map.put("siteId",siteId);
        });
        PestMonitoringRuleVo pestMonitoringRuleVo = new PestMonitoringRuleVo(weekPatrol, settingValue, pestMonitorValues, specialStacks);

        return pestMonitoringRuleVo;
    }

    /**
     * 手动导入excel上报
     *
     * @param pestDatas excel数据
     * @param siteType  位置类型 仓间、垛位
     * @return
     */
    @Override
    public Result exportPestData(List<Map<Integer, String>> pestDatas, String siteType) {
        //存放虫情数据
        List<PestData> pestDataList = new ArrayList<>();
        for (int i = 0; i < pestDatas.size(); i++) {
            PestData pestData;
            //0 虫情板点位id 1 采集位置 2 采集时间 3 甲虫数量 4 粉螟数量
            Map<Integer, String> integerStringMap = pestDatas.get(i);

            String pestBoardId = integerStringMap.get(0).trim();

            if (ToolUtil.isEmpty(pestBoardId)){
                continue;
            }
            //采集位置
            String gatherSite = integerStringMap.get(1).trim();
            //采集时间
            String gatherTime = integerStringMap.get(2).trim();
            //甲虫数量
            Double xsxl3 = Double.parseDouble(integerStringMap.get(3).trim());
            Integer beetleCount = xsxl3.intValue();
            Integer mealwormCount = null;

            //如果是仓间虫情点位
            if (NodeConstant.NODE_WARE_AREA.equals(siteType)) {
                //粉螟数量
                Double y = Double.parseDouble(integerStringMap.get(4).trim());
                mealwormCount = y.intValue();
                if (ToolUtil.isEmpty(mealwormCount)) {
                    return new Result(false, StatusCode.ERROR, "第" + i + 2 + "行有数据为空");
                }
            }
            //判断预警等级
            Map<String, Integer> levelMap = decideLevel(pestBoardId, beetleCount, mealwormCount);
            Integer level = levelMap.get("level");

            //判断表格中是否存在空数据
            if (ToolUtil.isEmpty(pestBoardId) || ToolUtil.isEmpty(gatherSite) || ToolUtil.isEmpty(gatherTime) || ToolUtil.isEmpty(beetleCount)) {
                return new Result(false, StatusCode.ERROR, "第" + i + 2 + "行有数据为空");
            } else {
                if (NodeConstant.NODE_STACK.equals(siteType)) {
                    pestData = new PestData(pestBoardId, gatherSite, gatherTime, beetleCount,level);
                } else {
                    pestData = new PestData(pestBoardId, gatherSite, gatherTime, beetleCount, level,levelMap.get("mealwormLevel"),levelMap.get("beetleLevel"));
                }
            }
            //将当前行数据存到list中
            pestDataList.add(pestData);


        }
        //执行数据库操作
        pestMonitoringDao.exportPestData(pestDataList, siteType);

        //把最新的数据存到缓存中
        pestDataList.forEach(pestData -> {
            redisTemplate.opsForValue().set(PEST_MONITOR + pestData.getPestBoardId(), JSONObject.toJSONString(pestData));
        });

        return new Result(true, StatusCode.OK, "导入成功");
    }

    /**
     * 一键上报
     *
     * @return
     */
    @Override
    public Result fullExport() {
        //获取用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户账号
        //TODO 需要将用户信息存入缓存中
        String username = userInfo.get("username");
        List<PestData> stackList = new ArrayList<>();
        List<PestData> wareList = new ArrayList<>();
        //获取该负责人的负责的所有虫情点位
        List<Map<String, String>> list = pestMonitoringDao.findByPrincipalName(username);
        if (list.size()<1){
            new Result(true, StatusCode.OK, "一键导入失败，该用户没有负责的虫情点位");
        }
        //遍历所有虫情点位 分别放在垛位集合 仓间集合里面
        for (int i = 0; i < list.size(); i++) {
            Map<String, String> map = list.get(i);
            SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            if (NodeConstant.NODE_STACK.equals(map.get("siteType"))) {
                //垛内
                stackList.add(new PestData(map.get("pestBoardId"), map.get("siteName"), ft.format(new Date()), 0, 0));
            } else {
                //仓间
                wareList.add(new PestData(map.get("pestBoardId"), map.get("siteName"), ft.format(new Date()), 0, 0, 0,0));
            }
        }
        //执行数据库操作
        if (stackList.size() > 0) {
            pestMonitoringDao.exportPestData(stackList, NodeConstant.NODE_STACK);
        }
        if (wareList.size() > 0) {
            pestMonitoringDao.exportPestData(wareList, NodeConstant.NODE_WARE_AREA);
        }

        //给集合中的所有对象设置预警等级 并将该数据放置到redis中
        stackList.forEach(pestData -> {
            Integer level = setWarningLevel(WarningTypeConstant.STACK_BEETLE_OVERRUN_WARNING, pestData);
            pestData.setAlertLevel(level);
            redisTemplate.opsForValue().set(PEST_MONITOR + pestData.getPestBoardId(), JSONObject.toJSONString(pestData));
        });
        wareList.forEach(pestData -> {
            Integer beetleLevel = setWarningLevel(WarningTypeConstant.WARE_BEETLE_OVERRUN_WARNING, pestData);
            Integer mealwormLevel = setWarningLevel(WarningTypeConstant.WARE_MEALWORM_OVERRUN_WARNING, pestData);
            pestData.setAlertLevel(Math.max(beetleLevel,mealwormLevel));
            redisTemplate.opsForValue().set(PEST_MONITOR + pestData.getPestBoardId(), JSONObject.toJSONString(pestData));
        });

        return new Result(true, StatusCode.OK, "一键导入成功");
    }

    /**
     * 查询虫情板信息
     *
     * @param pestBoardDto
     * @return
     */
    @Override
    public List<PestBoardVo> findPestBoard(PestBoardDto pestBoardDto) {
        if (!ToolUtil.isEmpty(pestBoardDto.getSiteRoute())) {
            //拼接完整的位置路由
            String siteRoute = pestBoardDto.getSiteRoute() + ",[0" + pestBoardDto.getSiteRoute().split(",").length + ":" + pestBoardDto.getSiteId() + "]";
            pestBoardDto.setSiteRoute(siteRoute);
        }
        return pestMonitoringDao.findPestBoard(pestBoardDto);
    }


    /**
     * 判断等级
     *
     * @param value 当前值
     * @param strs  预警阈值数组
     * @return
     */
    public int decideValue(Integer value, String[] strs) {
        //判断等级
        int level = 0;
        if (value >= Integer.parseInt(strs[2])) {
            level = 3;
        } else if (value >= Integer.parseInt(strs[1])) {
            level = 2;
        } else if (value >= Integer.parseInt(strs[0])) {
            level = 1;
        }
        return level;
    }

    /**
     * 判断预警
     *
     * @param pestBoardVo 当前虫情点位信息
     * @param level       预警等级
     * @param data        值
     * @param warningType 预警类型
     * @return
     */
    private Integer decideEarlyWarning(PestBoardVo pestBoardVo, Integer level, Integer data, String warningType) {

        //获取数据库中未处理的预警
        EarlyWarningVo earlyWarningVo = earlyWarningDao.findUndoEarlyWarningByNodeId(pestBoardVo.getPestBoardId(), warningType);

        EarlyWarning earlyWarning = new EarlyWarning();

        //如果存在
        if (!ToolUtil.isEmpty(earlyWarningVo)) {

            //如果当前预警等级更高
            if (earlyWarningVo.getWarningGrade() < level) {
                //设置等级
                dataHandler.setLevel(earlyWarning, level, data.doubleValue());
                //设置主键id
                earlyWarning.setId(earlyWarningVo.getId());
                //更新数据
                earlyWarningDao.updateWarning(earlyWarning);
            } else {
                earlyWarning.setWarningGrade(earlyWarningVo.getWarningGrade());
            }

        } else {//如果不存在
            //如果出现预警
            if (level > 0) {
                String siteRoute = pestBoardVo.getSiteRoute();
                String[] splits = siteRoute.split(",");

                //剔除中括号
                String strip = StringUtils.strip(splits[splits.length - 1], "[]");
                if (splits.length == 5) {
                    //设置仓间id
                    earlyWarning.setAreaId(Integer.parseInt(strip.split(":")[1]));
                } else {
                    //设置垛位id
                    earlyWarning.setStackId(Integer.parseInt(strip.split(":")[1]));
                }
                earlyWarning.setWarningType(warningType);
                //设置虫情点位id
                earlyWarning.setNodeId(pestBoardVo.getPestBoardId());
                //设置等级及对应的值
                dataHandler.setLevel(earlyWarning, level, data.doubleValue());

                earlyWarning.setNodeType("07");
                //将该预警记录插入到数据库中
                earlyWarningDao.insertWarning(earlyWarning);

            }else {
                earlyWarning.setWarningGrade(0);
            }
        }
        return earlyWarning.getWarningGrade();
    }

    /**
     * 虫情数据实时监控
     *
     * @param monitorCondition
     * @return
     */
    @Override
    public PageInfo<PestDataVo> findPestDataHistory(MonitorCondition monitorCondition) {
        if (!ToolUtil.isEmpty(monitorCondition.getPidRoute())) {
            monitorCondition.setPidRoute(monitorCondition.getPidRoute() + ",[0" + monitorCondition.getPidRoute().split(",").length + ":" + monitorCondition.getSiteId() + "]");
        }
        //虫情点位数据集
        List<PestDataVo> pestDataVos = new ArrayList<>();
        int pageNum = monitorCondition.getPage();
        int pageSize = monitorCondition.getSize();

        if (monitorCondition.getSortCondition() == 0 && monitorCondition.getMonitorStatus() == 0) {
            //正常排序
            PageHelper.startPage(pageNum, pageSize);
        }
        //通过监控条件查找
        List<String> pestBoardIds = pestMonitoringDao.findPestBoardId(monitorCondition);

        //从缓存中获取对应的数据
        pestBoardIds.forEach(pestBoardId -> {
            Object o = redisTemplate.opsForValue().get(PEST_MONITOR + pestBoardId);
            if (!ToolUtil.isEmpty(o)) {
                pestDataVos.add(JSONObject.parseObject(o.toString(), PestDataVo.class));
            }
        });
        //根据监控类型过滤
        Stream<PestDataVo> stream = pestDataVos.stream();
        if (monitorCondition.getMonitorStatus() == 1) {
            //预警数据
            stream = stream.filter(pestDataVo -> pestDataVo.getAlertLevel() > 0);
        } else if (monitorCondition.getMonitorStatus() == 2) {
            //预警数据
            stream = stream.filter(pestDataVo -> pestDataVo.getAlertLevel() == 0);
        }
        if (monitorCondition.getSortCondition() == 1) {
            //按预警等级排序
            stream = stream.sorted(Comparator.comparing(PestDataVo::getAlertLevel).reversed());
        }
        List<PestDataVo> collect = stream.collect(Collectors.toList());

        //设置预警颜色
        setPestWarningColor(collect,monitorCondition.getSiteType());
        //手动分页
        Page<PestDataVo> page = manuallyPage(collect, monitorCondition.getPage(), monitorCondition.getSize());
        return new PageInfo<>(page);
    }

    /**
     * 手动分页
     *
     * @param list     需要分页的数据
     * @param pageNum  当前页
     * @param pageSize 每页数量
     * @return
     */
    public Page<PestDataVo> manuallyPage(List<PestDataVo> list, Integer pageNum, Integer pageSize) {
        //创建一个page类
        Page<PestDataVo> page = new Page<>(pageNum, pageSize);
        //总数量
        int total = list.size();
        page.setTotal(total);
        //起始下标
        int startIndex = (pageNum - 1) * pageSize;
        //终止下标
        int endIndex = Math.min(startIndex + pageSize, total);
        //截取数量
        page.addAll(list.subList(startIndex, endIndex));

        return page;
    }

    /**
     * 查询单个虫情点位的历史数据
     * @param pestDataDto
     * @return
     */
    @Override
    public List<PestDataVo> findPestDataHistoryByOne(PestDataDto pestDataDto) {
        List<PestDataVo> pestDataHistoryByOne = pestMonitoringDao.findPestDataHistoryByOne(pestDataDto);
        //通过传输时间排序
        List<PestDataVo> collect = pestDataHistoryByOne.stream().sorted((x, y) ->
                x.getGatherTime().compareTo(y.getGatherTime()) > 0 ? 1 : -1
        ).collect(Collectors.toList());
        //设置预警颜色
        setPestWarningColor(collect,pestDataDto.getSiteType());
        return collect;
    }

    /**
     * 查询所有特殊位置的位置路由
     * @return
     */
    public List<String> getSpecialSiteRoute() {
        //存放特殊位置的位置路由
        List<String> siteRoutes;

        //获取缓存中的虫情预警特殊位置
        Object o = redisTemplate.opsForHash().entries(SPECIAL_SITE_ROUTE);
        //如果缓存中不存在
        if (ToolUtil.isEmpty(o)) {
            //查找数据库 并放置到缓存中
            List<Map<String, Object>> specialSites = pestMonitoringDao.findSpecialSite();
            siteRoutes = new ArrayList<>();
            specialSites.forEach(map -> {
                //添加特殊位置位置路由
                siteRoutes.add(map.get("pidRoute").toString());

                //放到缓存中
                StringBuffer sb = new StringBuffer();
                sb.append(map.get("valueOne")).append("|").append("valueTwo").append("|").append("valueThree");
                redisTemplate.opsForHash().put(SPECIAL_SITE_ROUTE, map.get("siteRoute"), sb.toString());
            });
        } else {
            siteRoutes = new ArrayList<>(((Map) o).keySet());
        }
        return siteRoutes;
    }

    /**
     * 判断等级
     * @param pestBoardId
     * @param beetleCount
     * @param mealwormCount
     * @return
     */
    public Map<String,Integer> decideLevel(String pestBoardId, Integer beetleCount, Integer mealwormCount) {
        //通过 pestBoardId 找到对应的虫情板信息
        Map<String,Integer> map = new HashMap<>();
        PestBoardVo pestBoardVo = pestMonitoringDao.findPestBoardByPestBoardId(pestBoardId);
        //查找特殊位置
        List<String> siteRoutes = getSpecialSiteRoute();

        //通过特殊位置的路由值是否被当前包含，包含的话取最长的那条路径
        Stream<String> stringStream = siteRoutes.stream().filter(siteRoute -> pestBoardVo.getSiteRoute().contains(siteRoute));

        String s = stringStream.max(Comparator.comparingInt(String::length)).orElse("");

        //该数据预警等级
        int level = 0;
        //如果s为空 代表非特殊位置
        if ("".equals(s)) {
            putValueToCache();
            //获取缓存中的通用虫情预警设置
            String values = (String) redisTemplate.opsForValue().get(GENERAL_PEST_WARNING_VALUE);
            //分割后获取每个预警等级阈值
            String[] split = values.split("\\|");
            //求出对应的预警等级
            level = decideValue(beetleCount, split);
            //判断预警
            level = decideEarlyWarning(pestBoardVo, level, beetleCount, WarningTypeConstant.STACK_BEETLE_OVERRUN_WARNING);
            map.put("beetleLevel",level);

        } else {
            //获取特殊位置的预警阈值
            String values = (String) redisTemplate.opsForHash().get(SPECIAL_SITE_ROUTE, s);
            //截取
            String[] split = values.split("\\|");
            //求出对应的预警等级
            Integer beetleLevel = decideValue(beetleCount, split);
            //通过甲虫预警等级和数据库该虫情
            level = decideEarlyWarning(pestBoardVo, beetleLevel, beetleCount, WarningTypeConstant.WARE_BEETLE_OVERRUN_WARNING);
            map.put("beetleLevel",level);
            if (!ToolUtil.isEmpty(mealwormCount)) {
                //仓间粉螟
                int mealwormLevel = decideValue(mealwormCount, split);
                map.put("mealwormLevel",level);
                mealwormLevel = decideEarlyWarning(pestBoardVo, mealwormLevel, mealwormCount, WarningTypeConstant.WARE_MEALWORM_OVERRUN_WARNING);
                    map.put("level",Math.max(level,mealwormLevel));
            }
        }
        return map;
    }

    /**
     * 向缓存中添加通用的预警值及预警颜色
     * @return
     */
    private Map<String,Object> putValueToCache() {
        Object o = redisTemplate.opsForValue().get(GENERAL_PEST_WARNING_VALUE);
        Map<String,Object> map = new HashMap<>();
        if (!ToolUtil.isEmpty(o)){
            String[] split = o.toString().split("\\|");
            map.put("valueOne",split[0]);
            map.put("valueTwo",split[1]);
            map.put("valueThree",split[2]);
            map.put("colorOne",split[3]);
            map.put("colorTwo",split[4]);
            map.put("colorThree",split[5]);
        }else {
            map = pestMonitoringDao.findPestMonitorValue();
            StringBuffer sb = new StringBuffer();
            sb.append(map.get("valueOne")).append("|").append(map.get("valueTwo")).append("|").append(map.get("valueThree")).append("|")
                    .append(map.get("colorOne")).append("|").append(map.get("colorTwo")).append("|").append(map.get("colorThree"));
            redisTemplate.opsForValue().set(GENERAL_PEST_WARNING_VALUE,sb.toString());
        }
        return map;

    }

    /**
     * 设置预警颜色
     * @param list
     * @param siteType
     */
    public void setPestWarningColor(List<PestDataVo> list,String siteType) {
        Map<String, String> pestMonitorValue = pestMonitoringDao.findPestWarningColor();
        list.forEach(pestDataVo -> {
            //设置预警颜色
            pestDataVo.setWarningColor(getPestWarningColor(pestDataVo.getAlertLevel(),pestMonitorValue));
            //甲虫预警颜色
            pestDataVo.setBeetleColor(getPestWarningColor(pestDataVo.getBeetleLevel(),pestMonitorValue));

            if (NodeConstant.NODE_WARE_AREA.equals(siteType)){
                //粉螟预警颜色
                pestDataVo.setMealwormColor(getPestWarningColor(pestDataVo.getMealwormLevel(),pestMonitorValue));
            }
        });
    }

    /**
     * 通过rfid查询虫情点位信息
     * @param rfid
     * @return
     */
    @Override
    public Result findPestBoardByRfid(String rfid) {
        return pestMonitoringDao.findPestBoardByRfid(rfid);
    }

    /**
     * 通过虫情点位id查询预警阈值
     * @param pestBoardId
     * @return
     */
    @Override
    public Map<String, Object> findPestWarningValue(String pestBoardId) {

        Map<String, Object> map = putValueToCache();
        PestBoardVo pestBoardVo = pestMonitoringDao.findPestBoardByPestBoardId(pestBoardId);
        Map entries = redisTemplate.opsForHash().entries(SPECIAL_SITE_ROUTE);
        //倒序排
        TreeMap treeMap = new TreeMap(Comparator.reverseOrder());
        treeMap.putAll(entries);
        treeMap.forEach((k,y)->{
            if (pestBoardVo.getSiteRoute().contains(k.toString())){
                String[] split = y.toString().split("\\|");
                map.put("valueOne",split[0]);
                map.put("valueTwo",split[1]);
                map.put("valueThree",split[2]);
                return;
            }
        });
        return map;
    }

    /**
     * 设置预警等级
     * @param warningType
     * @param pestData
     */
    public Integer setWarningLevel(String warningType,PestData pestData){
        //虫情点位id和预警类型找对应的未处理的预警数据
        EarlyWarningVo earlyWarningVo = earlyWarningDao.findUndoEarlyWarningByNodeId(pestData.getPestBoardId(), warningType);
        //如果存在
        if (!ToolUtil.isEmpty(earlyWarningVo)){
            //给当前的虫情数据设置预警等级
            if (warningType.equals(WarningTypeConstant.WARE_MEALWORM_OVERRUN_WARNING)){
                pestData.setMealwormLevel(earlyWarningVo.getWarningGrade());
            }else {
                pestData.setBeetleLevel(earlyWarningVo.getWarningGrade());
            }
            return earlyWarningVo.getWarningGrade();
        }
        return 0;
    }

    /**
     * 获取虫情预警对应的颜色
     * @param level  等级
     * @param pestMonitorValue 监控规则配置值
     * @return 颜色代码
     */
    public String getPestWarningColor(Integer level,Map<String, String> pestMonitorValue){
        switch (level) {
            case 1:
                return pestMonitorValue.get("colorOne");
            case 2:
                return pestMonitorValue.get("colorTwo");
            case 3:
                return pestMonitorValue.get("colorThree");
            default:
                return pestMonitorValue.get("");
        }
    }
}
