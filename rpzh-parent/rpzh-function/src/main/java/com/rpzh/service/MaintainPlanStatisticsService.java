package com.rpzh.service;

import com.rpzh.dao.MaintainPlanStatisticsDao;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 养护计划「全链路统计」：养护完成后（或养护过程中）按计划汇总
 * 阶段转换、预警次数与分类、虫害数量、养护结果与累计预警时长。
 *
 * 纯只读聚合，不新增表、不写快照；每次查询实时计算，因此进行中的计划也能看。
 */
@Service
public class MaintainPlanStatisticsService {

    private static final String TIME_FMT = "yyyy-MM-dd HH:mm:ss";
    private static final long DAY_MS = 24L * 60 * 60 * 1000;

    /** 仓间级预警：这些类型覆盖整个仓间，按 area_id 纳入（其余只按垛位） */
    private static final Set<String> AREA_WARNING_TYPES =
            new HashSet<>(Arrays.asList("05", "07", "09", "11", "14"));
    /** 虫害类预警 */
    private static final Set<String> PEST_WARNING_TYPES =
            new HashSet<>(Arrays.asList("04", "05", "14"));
    /** 设备异常/故障类预警 */
    private static final Set<String> DEVICE_WARNING_TYPES =
            new HashSet<>(Arrays.asList("01", "02", "03"));

    private static final Map<String, String> WARNING_NAMES = new LinkedHashMap<>();
    static {
        WARNING_NAMES.put("01", "设备电量故障");
        WARNING_NAMES.put("02", "设备信号强度故障");
        WARNING_NAMES.put("03", "设备丢包故障");
        WARNING_NAMES.put("04", "垛内甲虫预警");
        WARNING_NAMES.put("05", "仓间甲虫预警");
        WARNING_NAMES.put("06", "垛内氧气预警");
        WARNING_NAMES.put("07", "仓间氧气预警");
        WARNING_NAMES.put("08", "垛内温度预警");
        WARNING_NAMES.put("09", "仓间温度预警");
        WARNING_NAMES.put("10", "垛内湿度预警");
        WARNING_NAMES.put("11", "仓间湿度预警");
        WARNING_NAMES.put("12", "包芯温度预警");
        WARNING_NAMES.put("13", "平均包芯高于环境预警");
        WARNING_NAMES.put("14", "仓间粉螟预警");
        WARNING_NAMES.put("19", "包芯低温预警");
    }

    @Autowired
    private MaintainPlanStatisticsDao statisticsDao;

    public Result statistics(Integer id) {
        if (id == null) {
            return new Result(false, StatusCode.ERROR, "养护计划 id 不能为空");
        }
        Map<String, Object> plan = statisticsDao.findPlanBase(id);
        if (plan == null || plan.isEmpty()) {
            return new Result(false, StatusCode.ERROR, "养护计划不存在：id=" + id);
        }

        Integer stackId = toInteger(plan.get("stackId"));
        Integer areaId = toInteger(plan.get("areaId"));
        boolean finished = "1".equals(str(plan.get("isEnd")));

        List<Map<String, Object>> stages = statisticsDao.findStages(id);
        if (stages == null) stages = new ArrayList<>();

        // ---- 1. 统计窗口：业务养护起止由阶段表推算；未完成则截至当前 ----
        Date now = new Date();
        Date begin = null, planEnd = null, lastUpdate = null, firstCreated = null;
        for (Map<String, Object> st : stages) {
            Date s = parse(str(st.get("startTime")));
            Date e = parse(str(st.get("endTime")));
            Date u = parse(str(st.get("updatedTime")));
            Date c = parse(str(st.get("createdTime")));
            if (s != null && (begin == null || s.before(begin))) begin = s;
            if (e != null && (planEnd == null || e.after(planEnd))) planEnd = e;
            if (u != null && (lastUpdate == null || u.after(lastUpdate))) lastUpdate = u;
            if (c != null && (firstCreated == null || c.before(firstCreated))) firstCreated = c;
        }
        if (begin == null) begin = firstCreated;
        if (begin == null) begin = parse("1970-01-01 00:00:00");

        // MySQL 的 NOW() 与 JVM 可能不在同一时区（本机 MySQL 容器是 UTC，应用是 GMT+8）。
        // 开垛完成时 endMaintainPlanStage 用 NOW() 写 updated_time，直接按本地时间解析会少 8 小时，
        // 导致完成当天的预警/虫情被排除在窗口之外。用数据库当前时间与 JVM 当前时间的差值校正。
        Date dbNow = parse(str(plan.get("dbNow")));
        long tzOffset = dbNow == null ? 0L : now.getTime() - dbNow.getTime();
        if (Math.abs(tzOffset) < 60_000L || Math.abs(tzOffset) >= DAY_MS) tzOffset = 0L;
        if (lastUpdate != null && tzOffset != 0L) {
            lastUpdate = new Date(lastUpdate.getTime() + tzOffset);
        }

        Date end;
        if (finished) {
            // 开垛时 endMaintainPlanStage 会写 updated_time；没有则退回计划的最晚阶段结束日
            end = lastUpdate != null ? lastUpdate : planEnd;
            if (end == null || end.after(now)) end = now;
        } else {
            end = now;
        }
        if (end.before(begin)) end = begin;
        String beginStr = fmt(begin), endStr = fmt(end);

        // ---- 2. 取窗口内的预警与虫害明细 ----
        List<Map<String, Object>> warnings =
                statisticsDao.findWarnings(stackId, areaId, beginStr, endStr);
        if (warnings == null) warnings = new ArrayList<>();
        // 移垛会改 tb_maintain_plan.stack_id，导致历史预警 stack_id 与当前不一致。
        // 主口径查不到时按仓间兜底，并在结果里标注，避免全链路统计出现整段空白。
        boolean areaFallback = false;
        if (warnings.isEmpty() && areaId != null) {
            List<Map<String, Object>> byArea =
                    statisticsDao.findWarnings(null, areaId, beginStr, endStr);
            if (byArea != null && !byArea.isEmpty()) {
                warnings = byArea;
                areaFallback = true;
            }
        }
        List<Map<String, Object>> pests = statisticsDao.findPests(areaId, beginStr, endStr);
        if (pests == null) pests = new ArrayList<>();

        // ---- 3. 阶段转换时间线：每阶段附注该阶段窗口内的预警数与虫数 ----
        int stageCount = stages.size();
        for (Map<String, Object> st : stages) {
            Date ss = parse(str(st.get("startTime")));
            Date ee = parse(str(st.get("endTime")));
            if (ss == null) ss = begin;
            if (ee == null) ee = end;
            if (ee.after(end)) ee = end;
            if (ss.after(ee)) ss = ee;
            int wc = 0, bc = 0, mc = 0, pc = 0;
            for (Map<String, Object> w : warnings) {
                Date t = parse(str(w.get("warningTime")));
                if (t != null && !t.before(ss) && !t.after(ee)) wc++;
            }
            for (Map<String, Object> p : pests) {
                Date t = parse(str(p.get("gatherTime")));
                if (t != null && !t.before(ss) && !t.after(ee)) {
                    bc += toInt(p.get("beetleCount"));
                    mc += toInt(p.get("mealwormCount"));
                    pc++;
                }
            }
            st.put("windowStart", fmt(ss));
            st.put("windowEnd", fmt(ee));
            st.put("durationDays", round1((ee.getTime() - ss.getTime()) / (double) DAY_MS));
            st.put("warningCount", wc);
            st.put("beetleCount", bc);
            st.put("mealwormCount", mc);
            st.put("pestSampleCount", pc);
            st.put("isChange", "1".equals(str(st.get("isChange"))));
        }

        // ---- 4. 预警：总数 / 分类 / 分类型 / 分等级 / 处置情况 ----
        Map<String, Map<String, Object>> byType = new LinkedHashMap<>();
        Map<String, Integer> byGrade = new LinkedHashMap<>();
        int untreated = 0, misreport = 0, pestWarning = 0, deviceFault = 0, environment = 0;
        for (Map<String, Object> w : warnings) {
            String type = str(w.get("warningType"));
            String grade = str(w.get("warningGrade"));
            String affirm = str(w.get("isAffirm"));
            boolean open = "1".equals(affirm) || "2".equals(affirm);
            if (open) untreated++;
            if ("3".equals(affirm)) misreport++;
            if (PEST_WARNING_TYPES.contains(type)) pestWarning++;
            else if (DEVICE_WARNING_TYPES.contains(type)) deviceFault++;
            else environment++;

            Map<String, Object> m = byType.get(type);
            if (m == null) {
                m = new LinkedHashMap<>();
                m.put("warningType", type);
                m.put("name", WARNING_NAMES.getOrDefault(type, "未知类型(" + type + ")"));
                m.put("category", PEST_WARNING_TYPES.contains(type) ? "虫害"
                        : DEVICE_WARNING_TYPES.contains(type) ? "设备异常" : "环境");
                m.put("areaLevel", AREA_WARNING_TYPES.contains(type));
                m.put("count", 0);
                m.put("untreated", 0);
                byType.put(type, m);
            }
            m.put("count", toInt(m.get("count")) + 1);
            if (open) m.put("untreated", toInt(m.get("untreated")) + 1);
            if (!grade.isEmpty()) byGrade.put(grade, byGrade.getOrDefault(grade, 0) + 1);
            w.put("warningName", WARNING_NAMES.getOrDefault(type, "未知类型(" + type + ")"));
            w.put("untreated", open);
        }

        // ---- 5. 虫害：总量 / 峰值 / 按诱捕板 ----
        int beetleTotal = 0, mealwormTotal = 0, maxBeetle = 0, maxMealworm = 0, alertSamples = 0;
        Map<String, Map<String, Object>> byBoard = new LinkedHashMap<>();
        for (Map<String, Object> p : pests) {
            int b = toInt(p.get("beetleCount")), mw = toInt(p.get("mealwormCount"));
            beetleTotal += b;
            mealwormTotal += mw;
            maxBeetle = Math.max(maxBeetle, b);
            maxMealworm = Math.max(maxMealworm, mw);
            if (toInt(p.get("alertLevel")) > 0) alertSamples++;
            String board = str(p.get("pestBoardId"));
            Map<String, Object> bm = byBoard.get(board);
            if (bm == null) {
                bm = new LinkedHashMap<>();
                bm.put("pestBoardId", board);
                bm.put("gatherSite", str(p.get("gatherSite")));
                bm.put("sampleCount", 0);
                bm.put("beetleCount", 0);
                bm.put("mealwormCount", 0);
                byBoard.put(board, bm);
            }
            bm.put("sampleCount", toInt(bm.get("sampleCount")) + 1);
            bm.put("beetleCount", toInt(bm.get("beetleCount")) + b);
            bm.put("mealwormCount", toInt(bm.get("mealwormCount")) + mw);
        }

        // ---- 6. 养护结果与累计预警时长 ----
        Map<String, Object> early = statisticsDao.findEarlyTime(id);
        if (early == null) early = new LinkedHashMap<>();
        String resultLabel = "未判定";
        Object mr = early.get("maintainResult");
        if (mr != null && !"".equals(str(mr))) {
            resultLabel = "1".equals(str(mr)) ? "合格" : "0".equals(str(mr)) ? "不合格" : "未判定";
        }
        double earlyHours = toDouble(early.get("coreTempAllEarlyTime"))
                + toDouble(early.get("oxygenAllEarlyTime"))
                + toDouble(early.get("tempAllEarlyTime"))
                + toDouble(early.get("humidityAllEarlyTime"));

        // ---- 7. 组装 ----
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("planId", id);
        summary.put("materialCode", str(plan.get("materialCode")));
        summary.put("stackName", firstNonEmpty(plan.get("stackName"), plan.get("stackNameCn")));
        summary.put("stackId", stackId);
        summary.put("areaId", areaId);
        summary.put("principalName", str(plan.get("principalName")));
        summary.put("maintenceStyle", str(plan.get("maintenceStyle")));
        summary.put("finished", finished);
        summary.put("beginTime", beginStr);
        summary.put("endTime", endStr);
        summary.put("durationDays", round1((end.getTime() - begin.getTime()) / (double) DAY_MS));
        summary.put("stageCount", stageCount);
        summary.put("changeCount", Math.max(0, stageCount - 1));
        summary.put("warningTotal", warnings.size());
        summary.put("warningUntreated", untreated);
        summary.put("warningMisreport", misreport);
        summary.put("pestWarningCount", pestWarning);
        summary.put("deviceFaultCount", deviceFault);
        summary.put("environmentWarningCount", environment);
        summary.put("beetleTotal", beetleTotal);
        summary.put("mealwormTotal", mealwormTotal);
        summary.put("pestSampleCount", pests.size());
        summary.put("pestAlertSamples", alertSamples);
        summary.put("maintainResultLabel", resultLabel);
        summary.put("earlyWarningHours", round1(earlyHours));
        summary.put("areaFallback", areaFallback);
        summary.put("scopeNote", "预警按「垛位 stack_id + 仓间级预警(05/07/09/11/14)」关联，"
                + "虫害按垛位所属仓间下的诱捕板关联；时间窗口取养护阶段起止"
                + (finished ? "至开垛完成时间" : "至当前时间")
                + "。warning_time 会被预警升级更新覆盖，故按记录数统计口径。"
                + (areaFallback ? " 当前垛位下无预警，已改用所属仓间兜底统计。" : ""));

        Map<String, Object> warningStat = new LinkedHashMap<>();
        warningStat.put("total", warnings.size());
        warningStat.put("untreated", untreated);
        warningStat.put("misreport", misreport);
        warningStat.put("pestWarningCount", pestWarning);
        warningStat.put("deviceFaultCount", deviceFault);
        warningStat.put("environmentCount", environment);
        warningStat.put("byType", new ArrayList<>(byType.values()));
        warningStat.put("byGrade", byGrade);
        warningStat.put("list", warnings);

        Map<String, Object> pestStat = new LinkedHashMap<>();
        pestStat.put("beetleTotal", beetleTotal);
        pestStat.put("mealwormTotal", mealwormTotal);
        pestStat.put("sampleCount", pests.size());
        pestStat.put("maxBeetle", maxBeetle);
        pestStat.put("maxMealworm", maxMealworm);
        pestStat.put("alertSamples", alertSamples);
        pestStat.put("byBoard", new ArrayList<>(byBoard.values()));
        pestStat.put("list", pests);

        Map<String, Object> resultStat = new LinkedHashMap<>(early);
        resultStat.put("resultLabel", resultLabel);
        resultStat.put("earlyWarningHours", round1(earlyHours));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("summary", summary);
        data.put("plan", plan);
        data.put("stages", stages);
        data.put("warnings", warningStat);
        data.put("pests", pestStat);
        data.put("maintainResult", resultStat);
        return new Result(true, StatusCode.OK, "查询成功", data);
    }

    // ---------- helpers ----------
    private static Date parse(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return new SimpleDateFormat(TIME_FMT).parse(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static String fmt(Date d) {
        return d == null ? "" : new SimpleDateFormat(TIME_FMT).format(d);
    }

    private static String str(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    private static String firstNonEmpty(Object... values) {
        for (Object v : values) {
            if (v != null && !"".equals(String.valueOf(v))) return String.valueOf(v);
        }
        return "";
    }

    private static int toInt(Object o) {
        if (o == null) return 0;
        try {
            return new BigDecimal(String.valueOf(o)).intValue();
        } catch (Exception e) {
            return 0;
        }
    }

    private static Integer toInteger(Object o) {
        if (o == null || "".equals(String.valueOf(o))) return null;
        try {
            return new BigDecimal(String.valueOf(o)).intValue();
        } catch (Exception e) {
            return null;
        }
    }

    private static double toDouble(Object o) {
        if (o == null) return 0d;
        try {
            return new BigDecimal(String.valueOf(o)).doubleValue();
        } catch (Exception e) {
            return 0d;
        }
    }

    private static double round1(double v) {
        return Math.round(v * 10) / 10.0;
    }
}
