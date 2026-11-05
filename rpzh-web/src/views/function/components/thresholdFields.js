/**
 * 阈值表单字段共享配置。
 *
 * 后端 GetBetweenDayUtils 对「阈值模板 / 养护计划」校验 20 个字段全部非 null，
 * 且每组严格递增：下限 < 一级 < 二级 < 三级；包芯高于环境 一级 < 二级 < 三级。
 * WareAreaAlarmSettingServiceImpl 另有一组 12 个仓间阈值，规则相同。
 * 这里统一给出字段定义与可通过校验的默认值，供 CrudDialog 复用。
 */

const num = (prop, label, defaultValue, extra = {}) => ({
  prop,
  label,
  type: 'number',
  span: 8,
  required: true,
  defaultValue,
  min: 0,
  precision: 1,
  step: 1,
  ...extra
})

/** 阈值模板 / 养护计划共用的 20 个阈值 */
export const TEMPLATE_THRESHOLDS = [
  num('stackOxygenLower', '垛内氧气下限', 2),
  num('stackOxygenUpperOne', '垛内氧气一级', 5),
  num('stackOxygenUpperTwo', '垛内氧气二级', 8),
  num('stackOxygenUpperThree', '垛内氧气三级', 12),
  num('stackTempLower', '垛内温度下限', 10),
  num('stackTempUpperOne', '垛内温度一级', 25),
  num('stackTempUpperTwo', '垛内温度二级', 30),
  num('stackTempUpperThree', '垛内温度三级', 35),
  num('stackMoistureLower', '垛内湿度下限', 40),
  num('stackMoistureUpperOne', '垛内湿度一级', 60),
  num('stackMoistureUpperTwo', '垛内湿度二级', 65),
  num('stackMoistureUpperThree', '垛内湿度三级', 70),
  num('tempCoreChipLower', '包芯温度下限', 10),
  num('tempCoreChipUpperOne', '包芯温度一级', 25),
  num('tempCoreChipUpperTwo', '包芯温度二级', 30),
  num('tempCoreChipUpperThree', '包芯温度三级', 35),
  num('coreTempHighEnvTempOne', '包芯高于环境一级', 2),
  num('coreTempHighEnvTempTwo', '包芯高于环境二级', 4),
  num('coreTempHighEnvTempThree', '包芯高于环境三级', 6),
  num('coreTempLowEnvTemp', '包芯低于环境', 0)
]

export const TEMPLATE_THRESHOLD_DEFAULTS = Object.fromEntries(
  TEMPLATE_THRESHOLDS.map((f) => [f.prop, f.defaultValue])
)

/** 仓间报警设置的 12 个阈值 */
export const AREA_THRESHOLDS = [
  num('warehouseOxygenLower', '仓间氧气下限', 2),
  num('warehouseOxygenUpperOne', '仓间氧气一级', 5),
  num('warehouseOxygenUpperTwo', '仓间氧气二级', 8),
  num('warehouseOxygenUpperThree', '仓间氧气三级', 12),
  num('warehouseTempLower', '仓间温度下限', 10),
  num('warehouseTempUpperOne', '仓间温度一级', 25),
  num('warehouseTempUpperTwo', '仓间温度二级', 30),
  num('warehouseTempUpperThree', '仓间温度三级', 35),
  num('warehouseMoistureLower', '仓间湿度下限', 40),
  num('warehouseMoistureUpperOne', '仓间湿度一级', 60),
  num('warehouseMoistureUpperTwo', '仓间湿度二级', 65),
  num('warehouseMoistureUpperThree', '仓间湿度三级', 70)
]

export const AREA_THRESHOLD_DEFAULTS = Object.fromEntries(
  AREA_THRESHOLDS.map((f) => [f.prop, f.defaultValue])
)

/** 前端按后端同样规则预校验，避免白跑一趟 */
export function checkThresholdGroups(payload, groups) {
  for (const g of groups) {
    const vals = g.keys.map((k) => Number(payload[k]))
    for (let i = 1; i < vals.length; i += 1) {
      if (!(vals[i - 1] < vals[i])) return `${g.label}阈值需严格递增：下限 < 一级 < 二级 < 三级`
    }
  }
  return ''
}

export const TEMPLATE_GROUPS = [
  { label: '垛内氧气', keys: ['stackOxygenLower', 'stackOxygenUpperOne', 'stackOxygenUpperTwo', 'stackOxygenUpperThree'] },
  { label: '垛内温度', keys: ['stackTempLower', 'stackTempUpperOne', 'stackTempUpperTwo', 'stackTempUpperThree'] },
  { label: '垛内湿度', keys: ['stackMoistureLower', 'stackMoistureUpperOne', 'stackMoistureUpperTwo', 'stackMoistureUpperThree'] },
  { label: '包芯温度', keys: ['tempCoreChipLower', 'tempCoreChipUpperOne', 'tempCoreChipUpperTwo', 'tempCoreChipUpperThree'] },
  { label: '包芯高于环境', keys: ['coreTempHighEnvTempOne', 'coreTempHighEnvTempTwo', 'coreTempHighEnvTempThree'] }
]

export const AREA_GROUPS = [
  { label: '仓间氧气', keys: ['warehouseOxygenLower', 'warehouseOxygenUpperOne', 'warehouseOxygenUpperTwo', 'warehouseOxygenUpperThree'] },
  { label: '仓间温度', keys: ['warehouseTempLower', 'warehouseTempUpperOne', 'warehouseTempUpperTwo', 'warehouseTempUpperThree'] },
  { label: '仓间湿度', keys: ['warehouseMoistureLower', 'warehouseMoistureUpperOne', 'warehouseMoistureUpperTwo', 'warehouseMoistureUpperThree'] }
]
