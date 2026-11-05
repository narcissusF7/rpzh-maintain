<template>
  <FunctionListPage
    ref="listRef"
    title="养护计划"
    description="按物料与垛位编制的养护计划，含养护方式、阶段与结束状态"
    :api="findMaintainPlanInfo"
    :columns="columns"
    :search-fields="searchFields"
    :hint="hint"
    :action-width="430"
  >
    <template #actions>
      <el-button type="primary" :icon="Plus" @click="openCreate">新建养护计划</el-button>
    </template>
    <template #row-actions="{ row }">
      <el-button link type="primary" @click="openEdit(row)">修改</el-button>
      <template v-if="!isFinished(row)">
        <el-button link type="success" @click="openFlip(row)">翻垛</el-button>
        <el-button link type="warning" @click="openMove(row)">移垛</el-button>
        <el-button link type="primary" @click="openStage(row)">转下一阶段</el-button>
        <el-button link type="primary" :loading="finishingId === row.id" @click="finishPlan(row)">养护完成</el-button>
      </template>
      <!-- 全链路统计只在养护完成后展示 -->
      <el-button v-else link type="info" @click="openStatistics(row)">全链路统计</el-button>
      <el-button link type="danger" @click="removePlan(row)">删除</el-button>
    </template>
  </FunctionListPage>

  <el-dialog
    v-model="visible"
    :title="isEdit ? '修改养护计划' : '新建养护计划'"
    width="780px"
    top="6vh"
    :close-on-click-modal="false"
    destroy-on-close
  >
    <el-alert
      v-if="loadError"
      class="plan-alert"
      type="warning"
      show-icon
      :closable="false"
      :title="loadError"
    />
    <el-alert class="plan-alert" type="info" :closable="false" show-icon>
      <template #title>
        {{ isEdit ? '修改会同步更新养护计划、预警阈值、养护阶段与物料数量，垛位保持不变。' : '新建会同时写入养护计划、预警阈值、养护阶段与物料数量；已有养护计划的垛位已从下拉中筛除。' }}
      </template>
    </el-alert>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="132px">
      <el-divider content-position="left">基础信息</el-divider>
      <el-row :gutter="10">
        <el-col :span="12">
          <el-form-item label="物料编码" prop="materialCodes">
            <el-select
              v-model="form.materialCodes"
              multiple
              filterable
              allow-create
              default-first-option
              collapse-tags
              :loading="loadingOptions"
              placeholder="可多选，也可直接输入"
              style="width: 100%"
            >
              <el-option v-for="c in materialCodes" :key="c" :label="c" :value="c" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="垛位" prop="stackId">
            <el-select
              v-model="form.stackId"
              filterable
              :disabled="isEdit"
              :placeholder="isEdit ? '修改时保持原垛位不变' : '仅显示可新建计划的垛位'"
              style="width: 100%"
            >
              <el-option
                v-for="s in filteredStacks"
                :key="s.id"
                :label="stackLabel(s)"
                :value="s.id"
                :disabled="!isEdit && occupied.has(Number(s.id))"
              />
            </el-select>
            <div class="plan-stack-hint">
              {{ stackHint }}
              <el-link type="primary" :underline="false" @click="showOccupied = !showOccupied">
                {{ showOccupied ? '仅看可用' : '显示全部（含已有计划）' }}
              </el-link>
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="养护方式" prop="maintenceStyle">
            <el-select v-model="form.maintenceStyle" style="width: 100%">
              <el-option v-for="o in STYLES" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="养护阶段" prop="phases">
            <el-select v-model="form.phases" filterable allow-create default-first-option style="width: 100%">
              <el-option v-for="p in phasesOptions" :key="p" :label="p" :value="p" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="负责人" prop="principalIds">
            <el-select
              v-model="form.principalIds"
              multiple
              filterable
              collapse-tags
              placeholder="可多选"
              style="width: 100%"
            >
              <el-option
                v-for="u in users"
                :key="u.userId"
                :label="u.nickname || u.username"
                :value="u.userId"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="预警规则名称" prop="earlyWarningRuleName">
            <el-select
              v-model="form.earlyWarningRuleName"
              filterable
              allow-create
              default-first-option
              placeholder="选择模板或输入新名称"
              style="width: 100%"
            >
              <el-option v-for="n in ruleNames" :key="n" :label="n" :value="n" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-divider content-position="left">计划时间</el-divider>
      <el-row :gutter="10">
        <el-col :span="12">
          <el-form-item label="调养开始日期" prop="aftercareDateStart">
            <el-date-picker
              v-model="form.aftercareDateStart"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="选择日期"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="养护天数" prop="maintainDays">
            <el-input-number v-model="form.maintainDays" :min="1" :max="3650" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="降氧完成天数" prop="oxygenReductionTime">
            <el-input v-model="form.oxygenReductionTime" placeholder="如 5" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="到期提醒时间" prop="expireTime">
            <el-date-picker
              v-model="form.expireTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="选择日期时间"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="养护数量" prop="maintainCount">
            <el-input-number v-model="form.maintainCount" :min="1" :precision="2" :step="100" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-collapse v-model="activeCollapse" class="plan-collapse">
        <el-collapse-item name="threshold">
          <template #title>
            <span>预警阈值（已填可用默认值，可展开调整）</span>
          </template>
          <el-alert type="warning" :closable="false" show-icon class="plan-alert">
            <template #title>后端要求每组严格递增：下限 &lt; 一级 &lt; 二级 &lt; 三级；包芯高于环境 一级 &lt; 二级 &lt; 三级。</template>
          </el-alert>
          <el-row :gutter="10">
            <el-col v-for="f in THRESHOLDS" :key="f.prop" :span="8">
              <el-form-item :label="f.label" label-width="140px">
                <el-input-number
                  v-model="form[f.prop]"
                  :min="0"
                  :precision="1"
                  :step="1"
                  controls-position="right"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </el-collapse-item>
      </el-collapse>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">提交</el-button>
    </template>
  </el-dialog>

  <!-- 翻垛：重新排布该养护计划垛位下设备的坐标 -->
  <el-dialog
    v-model="flipVisible"
    title="翻垛"
    width="760px"
    top="6vh"
    :close-on-click-modal="false"
    destroy-on-close
  >
    <el-alert
      v-if="flipStackId == null"
      type="warning"
      :closable="false"
      show-icon
      title="未获取到该养护计划的垛位信息，无法翻垛"
    />
    <template v-else>
      <el-alert class="plan-alert" type="info" :closable="false" show-icon>
        <template #title>
          翻垛 = 在同一垛位内重新排布箱号 / 设备坐标（X/Y/Z）。修改后点击「确认翻垛」提交。
        </template>
      </el-alert>
      <el-table :data="flipNodes" border stripe height="360px" v-loading="flipLoading">
        <el-table-column prop="nodeId" label="节点编号" min-width="150" />
        <el-table-column prop="caseNo" label="设备箱号" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="dictType(NODE_STATUS, row.status)" size="small" effect="light">
              {{ dictLabel(NODE_STATUS, row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="X 坐标" width="120">
          <template #default="{ row }">
            <el-input v-model="row.siteX" placeholder="X" />
          </template>
        </el-table-column>
        <el-table-column label="Y 坐标" width="120">
          <template #default="{ row }">
            <el-input v-model="row.siteY" placeholder="Y" />
          </template>
        </el-table-column>
        <el-table-column label="Z 坐标" width="120">
          <template #default="{ row }">
            <el-input v-model="row.siteZ" placeholder="Z" />
          </template>
        </el-table-column>
      </el-table>
    </template>
    <template #footer>
      <el-button @click="flipVisible = false">取消</el-button>
      <el-button type="primary" :disabled="flipStackId == null" :loading="flipSubmitting" @click="submitFlip">确认翻垛</el-button>
    </template>
  </el-dialog>

  <!-- 移垛：把养护计划及其设备整体迁到目标垛位 -->
  <el-dialog
    v-model="moveVisible"
    title="移垛"
    width="640px"
    top="6vh"
    :close-on-click-modal="false"
    destroy-on-close
  >
    <el-alert
      v-if="moveStackId == null"
      type="warning"
      :closable="false"
      show-icon
      title="未获取到该养护计划的垛位信息，无法移垛"
    />
    <template v-else>
      <el-alert class="plan-alert" type="info" :closable="false" show-icon>
        <template #title>
          移垛 = 把当前养护计划及垛位下所有设备整体迁移到另一个垛位（目标垛位不能已有养护计划）。
        </template>
      </el-alert>
      <el-form label-width="110px">
        <el-form-item label="当前垛位">
          <span>{{ moveCurrentLabel }}</span>
        </el-form-item>
        <el-form-item label="目标垛位" required>
          <el-tree-select
            v-model="moveTargetId"
            :data="moveStackTree"
            value-key="id"
            node-key="id"
            :props="{ label: 'label', children: 'children' }"
            check-strictly
            :render-after-expand="false"
            placeholder="在树中选择目标垛位"
            style="width: 100%"
            @node-click="onMoveTargetPick"
          />
        </el-form-item>
        <el-form-item label="随计划迁移的设备">
          <div style="width: 100%">
            <el-table :data="moveNodes" border size="small" max-height="200px">
              <el-table-column prop="nodeId" label="节点编号" min-width="130" />
              <el-table-column prop="caseNo" label="设备箱号" width="110" />
              <el-table-column label="状态" width="90">
                <template #default="{ row }">
                  <el-tag :type="dictType(NODE_STATUS, row.status)" size="small" effect="light">
                    {{ dictLabel(NODE_STATUS, row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
            <el-alert
              v-if="moveAbnormalCount > 0"
              class="plan-alert"
              type="warning"
              :closable="false"
              show-icon
              :title="`该垛位下有 ${moveAbnormalCount} 台异常 / 解垛设备，移垛会一并迁移，请先确认设备状态`"
            />
            <div class="plan-tip">设备箱号为入垛时录入的数据，移垛只更新垛位与坐标，不会覆盖箱号与设备状态。</div>
          </div>
        </el-form-item>
      </el-form>
    </template>
    <template #footer>
      <el-button @click="moveVisible = false">取消</el-button>
      <el-button type="primary" :disabled="moveStackId == null || !moveTargetId" :loading="moveSubmitting" @click="submitMove">确认移垛</el-button>
    </template>
  </el-dialog>

  <!-- 养护全链路统计：养护完成后自动弹出，也可随时从行操作查看 -->
  <el-dialog
    v-model="statsVisible"
    title="养护完成 · 全链路统计"
    width="980px"
    top="5vh"
    :close-on-click-modal="false"
    destroy-on-close
  >
    <div v-loading="statsLoading">
      <el-alert v-if="statsError" type="error" :closable="false" show-icon :title="statsError" />
      <template v-if="statSummary">
        <el-alert class="plan-alert" type="info" :closable="false" show-icon>
          <template #title>
            {{ statSummary.materialCode || '养护计划 ' + statSummary.planId }} · {{ statSummary.stackName || '未知垛位' }}
            <el-tag size="small" effect="light" :type="statSummary.finished ? 'success' : 'warning'">
              {{ statSummary.finished ? '养护已完成' : '养护进行中' }}
            </el-tag>
          </template>
          <template #default>
            全链路统计为养护完成后的归档视图，统计窗口 {{ statSummary.beginTime }} ~ {{ statSummary.endTime }}。
            {{ statSummary.scopeNote }}
          </template>
        </el-alert>

        <el-row :gutter="10" class="stat-cards">
          <el-col v-for="c in statCards" :key="c.label" :span="4">
            <div class="stat-card">
              <div class="stat-label">{{ c.label }}</div>
              <div class="stat-value" :style="c.color ? { color: c.color } : {}">{{ c.value }}</div>
              <div v-if="c.sub" class="stat-sub">{{ c.sub }}</div>
            </div>
          </el-col>
        </el-row>

        <el-tabs v-model="statsTab">
          <el-tab-pane label="阶段转换" name="stages">
            <div class="plan-tip">
              共 {{ statSummary.stageCount }} 个阶段、{{ statSummary.changeCount }} 次阶段转换；
              每阶段的预警数与虫数按该阶段的养护起止窗口单独统计，未来日期统一截断到统计截止时间。
            </div>
            <el-table :data="statStages" border stripe size="small" max-height="380px">
              <el-table-column type="index" label="序号" width="65" />
              <el-table-column prop="phases" label="养护阶段" min-width="110" />
              <el-table-column label="统计窗口" min-width="290">
                <template #default="{ row }">{{ row.windowStart || '-' }} ~ {{ row.windowEnd || '-' }}</template>
              </el-table-column>
              <el-table-column prop="durationDays" label="阶段天数" width="95" />
              <el-table-column prop="warningCount" label="预警次数" width="95" />
              <el-table-column prop="beetleCount" label="甲虫数" width="85" />
              <el-table-column prop="mealwormCount" label="粉螟数" width="85" />
              <el-table-column prop="pestSampleCount" label="虫情采集" width="95" />
              <el-table-column label="转换" width="95">
                <template #default="{ $index }">
                  <el-tag size="small" effect="light" :type="$index === statStages.length - 1 ? 'success' : 'info'">
                    {{ $index === statStages.length - 1 ? '末阶段' : '已转段' }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="!statStages.length" description="该计划暂无阶段记录" :image-size="70" />
          </el-tab-pane>

          <el-tab-pane :label="`预警明细 (${statSummary.warningTotal})`" name="warnings">
            <el-row :gutter="10" class="stat-cards stat-cards-mini">
              <el-col v-for="c in warningCards" :key="c.label" :span="6">
                <div class="stat-card">
                  <div class="stat-label">{{ c.label }}</div>
                  <div class="stat-value">{{ c.value }}</div>
                </div>
              </el-col>
            </el-row>
            <el-divider content-position="left">按预警类型</el-divider>
            <el-table :data="statWarningTypes" border stripe size="small" max-height="220px">
              <el-table-column prop="warningType" label="类型码" width="90" />
              <el-table-column prop="name" label="预警类型" min-width="170" />
              <el-table-column prop="category" label="分类" width="100" />
              <el-table-column prop="count" label="次数" width="90" />
              <el-table-column prop="untreated" label="未处置" width="90" />
            </el-table>
            <el-empty v-if="!statWarningTypes.length" description="统计窗口内无预警" :image-size="60" />
            <el-divider content-position="left">预警记录</el-divider>
            <el-table :data="statWarningList" border stripe size="small" max-height="240px">
              <el-table-column prop="warningTime" label="预警时间" width="160" />
              <el-table-column prop="warningName" label="预警类型" min-width="160" />
              <el-table-column prop="warningGrade" label="等级" width="70" />
              <el-table-column prop="nodeId" label="设备 / 点位" min-width="140" />
              <el-table-column label="处置状态" width="100">
                <template #default="{ row }">
                  <el-tag size="small" effect="light" :type="row.untreated ? 'danger' : 'success'">
                    {{ row.untreated ? '未处置' : '已处置' }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane :label="`虫害统计 (${statSummary.pestSampleCount})`" name="pests">
            <el-row :gutter="10" class="stat-cards stat-cards-mini">
              <el-col v-for="c in pestCards" :key="c.label" :span="6">
                <div class="stat-card">
                  <div class="stat-label">{{ c.label }}</div>
                  <div class="stat-value">{{ c.value }}</div>
                </div>
              </el-col>
            </el-row>
            <el-divider content-position="left">按诱捕板汇总</el-divider>
            <el-table :data="statPestBoards" border stripe size="small" max-height="200px">
              <el-table-column prop="pestBoardId" label="诱捕板" width="130" />
              <el-table-column prop="gatherSite" label="采集位置" min-width="130" />
              <el-table-column prop="sampleCount" label="采集次数" width="100" />
              <el-table-column prop="beetleCount" label="甲虫累计" width="100" />
              <el-table-column prop="mealwormCount" label="粉螟累计" width="100" />
            </el-table>
            <el-empty v-if="!statPestBoards.length" description="统计窗口内无虫情采集" :image-size="60" />
            <el-divider content-position="left">采集明细</el-divider>
            <el-table :data="statPestList" border stripe size="small" max-height="240px">
              <el-table-column prop="gatherTime" label="采集时间" width="160" />
              <el-table-column prop="pestBoardId" label="诱捕板" width="120" />
              <el-table-column prop="gatherSite" label="采集位置" min-width="130" />
              <el-table-column prop="beetleCount" label="甲虫数" width="90" />
              <el-table-column prop="mealwormCount" label="粉螟数" width="90" />
              <el-table-column prop="alertLevel" label="预警等级" width="100" />
            </el-table>
          </el-tab-pane>
        </el-tabs>

        <div class="plan-tip">
          养护结果判定来自累计预警时长（{{ statMaintainResult.earlyWarningHours }} 小时）；
          当前结论：<strong>{{ statMaintainResult.resultLabel }}</strong>。
        </div>
      </template>
      <el-empty v-else-if="!statsLoading && !statsError" description="暂无统计数据" />
    </div>
    <template #footer>
      <el-button @click="statsVisible = false">关闭</el-button>
      <el-button type="primary" :loading="statsLoading" @click="reloadStatistics">刷新统计</el-button>
    </template>
  </el-dialog>

  <!-- 养护阶段转段：结束当前阶段、插入新阶段记录并指向新阶段 -->
  <el-dialog
    v-model="stageVisible"
    title="养护阶段转段"
    width="640px"
    top="8vh"
    :close-on-click-modal="false"
    destroy-on-close
  >
    <div v-loading="stageLoading">
      <el-alert v-if="stageError" type="error" :closable="false" show-icon :title="stageError" />
      <template v-else>
        <el-alert class="plan-alert" type="info" :closable="false" show-icon>
          <template #title>转下一阶段 = 关闭当前阶段并开启新阶段</template>
          <template #default>
            系统会结束当前阶段（写入实际养护天数）、插入新阶段记录与预警阈值，并把计划指向新阶段。
            当前阶段：<strong>{{ stageCurrent || '未知' }}</strong>；新阶段按预设顺序自动推荐，可手动改选。
          </template>
        </el-alert>
        <el-form label-width="120px">
          <el-form-item label="当前阶段">
            <span>{{ stageCurrent || '-' }}</span>
          </el-form-item>
          <el-form-item label="新阶段" required>
            <el-select
              v-model="stageTarget"
              filterable
              allow-create
              default-first-option
              placeholder="选择或输入阶段名称"
              style="width: 100%"
            >
              <el-option v-for="p in stageCandidates" :key="p" :label="p" :value="p" />
            </el-select>
          </el-form-item>
          <el-form-item label="开始日期" required>
            <el-date-picker
              v-model="stageStart"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="选择调养开始日期"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="养护天数" required>
            <el-input-number v-model="stageDays" :min="1" :max="3650" />
          </el-form-item>
          <el-form-item label="预警规则名称">
            <el-input v-model="stageRuleName" placeholder="留空则沿用当前规则" />
          </el-form-item>
        </el-form>
        <div class="plan-tip">
          转段沿用该计划当前的 20 个预警阈值；提交前会校验阈值严格递增、垛位下无未处理任务。
          若存在未处理任务，请先处置后再转段。
        </div>
      </template>
    </div>
    <template #footer>
      <el-button @click="stageVisible = false">取消</el-button>
      <el-button
        type="primary"
        :disabled="!!stageError || !stageTarget || !stageStart"
        :loading="stageSubmitting"
        @click="submitStage"
      >确认转段</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import FunctionListPage from '../components/FunctionListPage.vue'
import {
  findMaintainPlanInfo,
  findOccupiedStackIds,
  beforeUpdateMaintainPlan,
  findMaintainPlanById,
  insertMaintainPlan,
  updateMaintainPlan,
  deleteMaintainPlanInfo,
  findAlarmSettingTemplateList,
  getPhases,
  moveStack,
  endMaintainPlan,
  findMaintainPlanStatistics,
  changeMaintainPlanStage
} from '@/api/function/plan'
import { findAllMaterialCodes } from '@/api/function/material'
import { getStackPage, getAllWareArea, warehouseTree } from '@/api/function/warehouse'
import { findNodeByStackId, flipStack } from '@/api/function/node'
import { listUser } from '@/api/user'
import { NODE_STATUS, dictLabel, dictType } from '@/utils/function'

const STYLES = [
  { label: '01 · 常规养护', value: '01' },
  { label: '02 · 降氧养护', value: '02' },
  { label: '03 · 梗丝养护', value: '03' }
]

/** 列表列字典：把编码 01/02/03 显示成中文，未命中时 dictLabel 回退原值 */
const STYLE_DICT = Object.fromEntries(
  STYLES.map((o) => [o.value, { label: o.label.replace(/^\d+\s*·\s*/, ''), type: 'info' }])
)
/** 库里 phases 有两套值：种子数据编码 01/02/03，接口模板中文名；两者都要能显示 */
const PHASE_DICT = {
  '01': { label: '降氧养护阶段', type: 'primary' },
  '02': { label: '恒温养护阶段', type: 'primary' },
  '03': { label: '养护复核阶段', type: 'success' },
  '养护阶段': { label: '养护阶段', type: 'info' },
  '充氮杀虫阶段': { label: '充氮杀虫阶段', type: 'warning' },
  '磷化铝杀虫阶段': { label: '磷化铝杀虫阶段', type: 'warning' },
  '充氮保质阶段': { label: '充氮保质阶段', type: 'success' }
}
/** 历史数据里养护方式可能存中文名，回显时归一成编码 */
const STYLE_ALIAS = {
  '常规整仓养护方式': '01',
  '机械调控方式': '02',
  '梗丝养护方式': '03'
}
const normalizeStyle = (v) => {
  const s = String(v ?? '').trim()
  return STYLE_ALIAS[s] || s
}

/** 已完成（is_end=1）才允许查看全链路统计 */
const PLAN_END_DICT = {
  '0': { label: '进行中', type: 'primary' },
  '1': { label: '已完成', type: 'success' }
}
const isFinished = (row) => String(row?.isEnd) === '1'

const columns = [
  { prop: 'materialCode', label: '物料编码', minWidth: 160 },
  { prop: 'stackName', label: '垛位位置', minWidth: 200 },
  { prop: 'maintenceStyle', label: '养护方式', width: 110, dict: STYLE_DICT },
  { prop: 'phases', label: '养护阶段', width: 150, dict: PHASE_DICT },
  { prop: 'stageId', label: '阶段 ID', width: 100 },
  { prop: 'isEnd', label: '计划状态', width: 100, dict: PLAN_END_DICT },
  { prop: 'createdTime', label: '制定时间', width: 170, type: 'time' }
]

// isEnd=1 时后端查「已完成」列表（不再用阶段未过期过滤），未传则维持原「进行中」口径
const PLAN_SCOPE = [
  { label: '进行中（默认）', value: '' },
  { label: '已完成 · 可看全链路统计', value: '1' }
]
const searchFields = [
  { prop: 'materialCode', label: '物料编码', width: 170 },
  { prop: 'maintenceStyle', label: '养护方式', type: 'select', width: 150, options: STYLES },
  { prop: 'isEnd', label: '计划状态', type: 'select', width: 200, options: PLAN_SCOPE }
]

const hint =
  '列表默认显示进行中计划；把「计划状态」切到「已完成」可查看已开垛的计划，并打开其全链路统计（仅养护完成后展示）。' +
  '养护完成后会自动切到该视图，方便随时复查阶段转换、预警次数与虫情数量。'

/** 后端 GetBetweenDayUtils 要求这 20 个字段全部非 null，且每组严格递增 */
const THRESHOLDS = [
  { prop: 'stackOxygenLower', label: '垛内氧气下限' },
  { prop: 'stackOxygenUpperOne', label: '垛内氧气一级' },
  { prop: 'stackOxygenUpperTwo', label: '垛内氧气二级' },
  { prop: 'stackOxygenUpperThree', label: '垛内氧气三级' },
  { prop: 'stackTempLower', label: '垛内温度下限' },
  { prop: 'stackTempUpperOne', label: '垛内温度一级' },
  { prop: 'stackTempUpperTwo', label: '垛内温度二级' },
  { prop: 'stackTempUpperThree', label: '垛内温度三级' },
  { prop: 'stackMoistureLower', label: '垛内湿度下限' },
  { prop: 'stackMoistureUpperOne', label: '垛内湿度一级' },
  { prop: 'stackMoistureUpperTwo', label: '垛内湿度二级' },
  { prop: 'stackMoistureUpperThree', label: '垛内湿度三级' },
  { prop: 'tempCoreChipLower', label: '包芯温度下限' },
  { prop: 'tempCoreChipUpperOne', label: '包芯温度一级' },
  { prop: 'tempCoreChipUpperTwo', label: '包芯温度二级' },
  { prop: 'tempCoreChipUpperThree', label: '包芯温度三级' },
  { prop: 'coreTempHighEnvTempOne', label: '包芯高于环境一级' },
  { prop: 'coreTempHighEnvTempTwo', label: '包芯高于环境二级' },
  { prop: 'coreTempHighEnvTempThree', label: '包芯高于环境三级' },
  { prop: 'coreTempLowEnvTemp', label: '包芯低于环境' }
]

const DEFAULT_THRESHOLDS = {
  stackOxygenLower: 2, stackOxygenUpperOne: 5, stackOxygenUpperTwo: 8, stackOxygenUpperThree: 12,
  stackTempLower: 10, stackTempUpperOne: 25, stackTempUpperTwo: 30, stackTempUpperThree: 35,
  stackMoistureLower: 40, stackMoistureUpperOne: 60, stackMoistureUpperTwo: 65, stackMoistureUpperThree: 70,
  tempCoreChipLower: 10, tempCoreChipUpperOne: 25, tempCoreChipUpperTwo: 30, tempCoreChipUpperThree: 35,
  coreTempHighEnvTempOne: 2, coreTempHighEnvTempTwo: 4, coreTempHighEnvTempThree: 6,
  coreTempLowEnvTemp: 0
}

const pad = (n) => String(n).padStart(2, '0')
const dateStr = (d) => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
const today = () => dateStr(new Date())
const defaultExpire = () => {
  const d = new Date()
  d.setDate(d.getDate() + 30)
  return `${dateStr(d)} 00:00:00`
}

const emptyForm = () => ({
  materialCodes: [],
  stackId: null,
  maintenceStyle: '01',
  phases: '',
  principalIds: [],
  earlyWarningRuleName: '',
  aftercareDateStart: today(),
  maintainDays: 30,
  oxygenReductionTime: '5',
  expireTime: defaultExpire(),
  maintainCount: 1000,
  ...DEFAULT_THRESHOLDS
})

const listRef = ref(null)
const formRef = ref(null)
const visible = ref(false)
const isEdit = ref(false)
const currentId = ref(null)
const submitting = ref(false)
const loadingOptions = ref(false)
const loadError = ref('')
const activeCollapse = ref([])
const optionsLoaded = ref(false)
const showOccupied = ref(false)
/** 修改回显期间，避免 maintenceStyle 的 watch 把后端返回的 phases 清掉 */
const suppressPhasesReset = ref(false)

const form = reactive(emptyForm())
const stacks = ref([])
const occupied = ref(new Set())

/** 已占用垛位：以 tb_maintain_plan.is_end='0' 为准，与后端 /insert 校验口径一致 */
const availableStacks = computed(() => stacks.value.filter((s) => !occupied.value.has(Number(s.id))))
const filteredStacks = computed(() => {
  if (isEdit.value || showOccupied.value) return stacks.value
  return availableStacks.value
})
const stackHint = computed(() =>
  `共 ${stacks.value.length} 个垛位，${occupied.value.size} 个已有养护计划，${availableStacks.value.length} 个可选`
)
const materialCodes = ref([])
const ruleNames = ref([])
const phasesOptions = ref([])
const users = ref([])

const rules = {
  materialCodes: [{ required: true, type: 'array', min: 1, message: '请至少选择一个物料编码', trigger: 'change' }],
  stackId: [{ required: true, message: '请选择垛位', trigger: 'change' }],
  maintenceStyle: [{ required: true, message: '请选择养护方式', trigger: 'change' }],
  phases: [{ required: true, message: '请选择养护阶段', trigger: 'change' }],
  principalIds: [{ required: true, type: 'array', min: 1, message: '请至少选择一位负责人', trigger: 'change' }],
  earlyWarningRuleName: [{ required: true, message: '请选择或填写预警规则名称', trigger: 'change' }],
  aftercareDateStart: [{ required: true, message: '请选择调养开始日期', trigger: 'change' }],
  maintainDays: [{ required: true, message: '请填写养护天数', trigger: 'blur' }],
  oxygenReductionTime: [{ required: true, message: '请填写降氧完成天数', trigger: 'blur' }],
  expireTime: [{ required: true, message: '请选择到期提醒时间', trigger: 'change' }],
  maintainCount: [{ required: true, message: '请填写养护数量', trigger: 'blur' }]
}

const sleep = (ms) => new Promise((r) => setTimeout(r, ms))
const rowsOf = (d) => (Array.isArray(d) ? d : Array.isArray(d?.list) ? d.list : Array.isArray(d?.rows) ? d.rows : [])

function stackLabel(s) {
  const base = `${s.stackNo || s.id} · ${s.nameCn || ''}`
  return occupied.value.has(Number(s.id)) ? `${base}（已有计划）` : base
}

/**
 * SiteRouteUtil.getSiteId 期望 "[00:0],[01:1],[02:11],[03:101],[04:201],[05:301]"。
 *
 * tb_stack.pid_route 存在两种历史格式：
 *   1) 初始化数据：0,1,11,101,201            （纯数字，按下标对应 00~04）
 *   2) 页面新建：0,1,11,101,[04:206]         （后半段带 [类型:id] 标记）
 * 所以不能只按 split(',') 的下标取值，否则在新建分区下建垛会拼出 [04:[04:206]]。
 */
function buildPidRoute(stack) {
  const tokens = String(stack?.pidRoute || '')
    .split(',')
    .map((s) => s.trim())
    .filter(Boolean)
  const types = ['00', '01', '02', '03', '04']
  const byType = {}
  const plain = []
  tokens.forEach((tok) => {
    const m = tok.match(/^\[(\d{2}):(\d+)\]$/)
    if (m) byType[m[1]] = m[2]
    else plain.push(tok)
  })
  const fallback = ['0', '1', '11', '101', '201']
  const base = types.map((t, i) => `[${t}:${byType[t] ?? plain[i] ?? fallback[i]}]`)
  return [...base, `[05:${stack?.id ?? form.stackId}]`].join(',')
}

function pickThresholds() {
  const out = {}
  THRESHOLDS.forEach((f) => { out[f.prop] = form[f.prop] })
  return out
}

/** 前端先按后端 GetBetweenDayUtils.get() 的规则校验一遍，避免白跑一趟 */
function checkThresholds() {
  const groups = [
    { label: '垛内氧气', keys: ['stackOxygenLower', 'stackOxygenUpperOne', 'stackOxygenUpperTwo', 'stackOxygenUpperThree'] },
    { label: '垛内温度', keys: ['stackTempLower', 'stackTempUpperOne', 'stackTempUpperTwo', 'stackTempUpperThree'] },
    { label: '垛内湿度', keys: ['stackMoistureLower', 'stackMoistureUpperOne', 'stackMoistureUpperTwo', 'stackMoistureUpperThree'] },
    { label: '包芯温度', keys: ['tempCoreChipLower', 'tempCoreChipUpperOne', 'tempCoreChipUpperTwo', 'tempCoreChipUpperThree'] }
  ]
  for (const g of groups) {
    const vals = g.keys.map((k) => Number(form[k]))
    for (let i = 1; i < vals.length; i += 1) {
      if (!(vals[i - 1] < vals[i])) {
        ElMessage.error(`${g.label}阈值需严格递增：下限 < 一级 < 二级 < 三级`)
        activeCollapse.value = ['threshold']
        return false
      }
    }
  }
  const env = ['coreTempHighEnvTempOne', 'coreTempHighEnvTempTwo', 'coreTempHighEnvTempThree'].map((k) => Number(form[k]))
  if (!(env[0] < env[1] && env[1] < env[2])) {
    ElMessage.error('包芯高于环境阈值需严格递增：一级 < 二级 < 三级')
    activeCollapse.value = ['threshold']
    return false
  }
  return true
}

async function loadOptions() {
  loadingOptions.value = true
  const failed = []
  try {
    const [m, r, u] = await Promise.all([
      findAllMaterialCodes(),
      findAlarmSettingTemplateList(),
      listUser({ page: 1, size: 50 })
    ])
    materialCodes.value = Array.isArray(m) ? m : []
    ruleNames.value = Array.isArray(r) ? r : []
    users.value = rowsOf(u)
  } catch (e) {
    failed.push('物料编码 / 规则名 / 负责人')
  }

  // 库区动态获取：页面新建的库区（如 206）也要能出现在垛位下拉里，
  // 不能像旧实现那样把 201~205 写死；接口不可用时退回默认库区。
  let areaIds = [201, 202, 203, 204, 205]
  try {
    const areas = rowsOf(await getAllWareArea())
    const ids = areas.map((a) => Number(a.id)).filter((n) => !Number.isNaN(n))
    if (ids.length) areaIds = ids
  } catch (e) { /* 老版本后端没有该接口时继续用默认库区 */ }

  // 垛位按库区逐个查；串行 + 间隔规避网关 10 req/s 限流
  const found = []
  for (const areaId of areaIds) {
    try {
      found.push(...rowsOf(await getStackPage({ wareAreaId: areaId, page: 1, size: 50 })))
    } catch (e) { /* 单个库区失败不影响其它 */ }
    await sleep(140)
  }
  stacks.value = found
  if (!found.length) failed.push('垛位')
  await refreshOccupied()

  loadError.value = failed.length ? `部分下拉数据加载失败（${failed.join('、')}），可手动输入或稍后重试。` : ''
  optionsLoaded.value = true
  loadingOptions.value = false
}

/**
 * 刷新「已有养护计划的垛位」。
 *
 * 旧实现用 findMaintainPlanInfo 列表接口取 stackId，但列表 SQL 只 select 了
 * id/material_code/stack_name/... 并没有 stack_id，导致 occupied 里全是 NaN，
 * 下拉实际上一个都没筛掉；且列表 SQL 还有 `aftercare_date_end >= NOW()` 的过滤，
 * 会漏掉阶段已过期但未结束的计划。
 *
 * 现在优先用后端 /findOccupiedStackIds（直接 select stack_id where is_end='0'），
 * 后端未更新时降级为按 stackName 与垛位 siteName 匹配，保证筛选不会整体失效。
 */
async function refreshOccupied() {
  try {
    const ids = await findOccupiedStackIds()
    if (Array.isArray(ids)) {
      occupied.value = new Set(ids.map(Number).filter((n) => !Number.isNaN(n)))
      return
    }
  } catch (e) { /* 后端尚未部署该接口时走下面的兜底 */ }

  try {
    const plans = rowsOf(await findMaintainPlanInfo({ page: 1, size: 100 }))
    const names = new Set(plans.map((p) => String(p.stackName || '')).filter(Boolean))
    occupied.value = new Set(
      stacks.value
        .filter((s) => names.has(String(s.siteName)) || names.has(String(s.nameCn)))
        .map((s) => Number(s.id))
    )
  } catch (e) {
    occupied.value = new Set()
  }
}

async function loadPhases() {
  try {
    const d = await getPhases(form.maintenceStyle)
    phasesOptions.value = Array.isArray(d) ? d : []
    if (phasesOptions.value.length && !form.phases) form.phases = phasesOptions.value[0]
  } catch (e) {
    phasesOptions.value = []
  }
}

const editMeta = reactive({ stageId: null, alarmValueSettingId: null, pidRoute: '' })

async function openCreate() {
  isEdit.value = false
  currentId.value = null
  showOccupied.value = false
  suppressPhasesReset.value = false
  Object.assign(form, emptyForm())
  activeCollapse.value = []
  visible.value = true
  await nextTick()
  formRef.value?.clearValidate()
  if (!optionsLoaded.value) await loadOptions()
  else await refreshOccupied() // 每次打开都刷新，避免新建/删除后占用集合过期
  await loadPhases()
}

async function openEdit(row) {
  isEdit.value = true
  currentId.value = row.id
  showOccupied.value = true
  suppressPhasesReset.value = true
  Object.assign(form, emptyForm())
  activeCollapse.value = []
  visible.value = true
  await nextTick()
  formRef.value?.clearValidate()
  if (!optionsLoaded.value) await loadOptions()
  else await refreshOccupied()

  try {
    const d = await beforeUpdateMaintainPlan(row.id)
    const vo = d?.data ?? d ?? {}
    editMeta.stageId = vo.stageId ?? null
    editMeta.alarmValueSettingId = vo.alarmValueSettingId ?? null
    editMeta.pidRoute = String(vo.pidRoute || '')

    form.materialCodes = Array.isArray(vo.materialCodes) && vo.materialCodes.length
      ? [...vo.materialCodes]
      : String(vo.materialCode || '').split(',').map((s) => s.trim()).filter(Boolean)
    form.stackId = vo.stackId != null
      ? Number(vo.stackId)
      : (stacks.value.find((s) => String(s.siteName) === String(vo.stackName))?.id ?? null)
    // 不再用 '01' 伪装缺失值：历史中文名归一成编码，确实缺失就留空让用户重选
    form.maintenceStyle = normalizeStyle(vo.maintenceStyle)
    if (!form.maintenceStyle) ElMessage.warning('该计划未记录养护方式，请重新选择后再保存')
    form.phases = vo.phases || ''
    // 后端 getPrincipalIds 在 Redis 里查不到用户时会返回 [null]，直接放进下拉会显示空白
    const pidList = (Array.isArray(vo.principalIds) ? vo.principalIds : [])
      .map((x) => Number(x))
      .filter((n) => Number.isFinite(n) && n > 0)
    form.principalIds = pidList.length ? pidList : [2]
    form.earlyWarningRuleName = vo.alarmValueSettingVo?.earlyWarningRuleName ||
      ruleNames.value[0] || '片烟常规养护阈值'
    form.maintainDays = Number(vo.maintainDays ?? 30)
    form.aftercareDateStart = String(vo.aftercareDateStart || today()).slice(0, 10)
    form.oxygenReductionTime = String(vo.oxygenReductionTime ?? '5')
    form.expireTime = String(vo.expireTime || defaultExpire()).replace('T', ' ').slice(0, 19)
    form.maintainCount = Number(vo.maintainCount ?? 1000)

    // 回显已保存的 20 个阈值；接口未返回时保留可用默认值
    const saved = vo.alarmValueSettingVo || {}
    THRESHOLDS.forEach((f) => {
      const v = saved[f.prop] ?? vo[f.prop]
      if (v !== null && v !== undefined && v !== '') form[f.prop] = Number(v)
    })
  } catch (e) {
    ElMessage.error(e?.message || '回显失败，无法修改该计划')
    visible.value = false
    suppressPhasesReset.value = false
    return
  }
  await loadPhases()
  suppressPhasesReset.value = false
}

async function removePlan(row) {
  try {
    await ElMessageBox.confirm(
      `确认删除养护计划 #${row.id}（${row.materialCode || '-'}）吗？该操作会同时清理关联的阈值、阶段与养护数量。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' }
    )
  } catch (e) {
    return
  }
  try {
    await deleteMaintainPlanInfo([row.id])
    ElMessage.success('删除成功')
    listRef.value?.load()
    await refreshOccupied()
  } catch (e) {
    ElMessage.error(e?.message || '删除失败，请检查后端日志')
  }
}

watch(() => form.maintenceStyle, () => {
  if (suppressPhasesReset.value) return
  form.phases = ''
  if (visible.value) loadPhases()
})

async function submit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  if (!checkThresholds()) return
  if (!isEdit.value && occupied.value.has(Number(form.stackId))) {
    ElMessage.warning('该垛位已有养护计划，请换一个垛位')
    return
  }
  if (isEdit.value && (!editMeta.stageId || !editMeta.alarmValueSettingId)) {
    ElMessage.warning('该计划缺少阶段或阈值记录，暂不支持修改')
    return
  }

  const stack = stacks.value.find((s) => Number(s.id) === Number(form.stackId))
  const params = {
    materialCodes: form.materialCodes,
    stackId: form.stackId,
    principalIds: form.principalIds,
    maintenceStyle: form.maintenceStyle,
    phases: form.phases,
    maintainDays: form.maintainDays,
    aftercareDateStart: form.aftercareDateStart,
    oxygenReductionTime: form.oxygenReductionTime,
    expireTime: form.expireTime,
    maintainCount: form.maintainCount,
    earlyWarningRuleName: form.earlyWarningRuleName,
    pidRoute: buildPidRoute(stack),
    ...pickThresholds()
  }

  if (isEdit.value) {
    params.id = currentId.value
    params.stageId = editMeta.stageId
    params.alarmValueSettingId = editMeta.alarmValueSettingId
    // 回显的 pidRoute 已是 [00:x],... 格式；缺失时按垛位重建
    params.pidRoute = /^\[\d\d:/.test(editMeta.pidRoute)
      ? editMeta.pidRoute
      : buildPidRoute(stack)
  }

  submitting.value = true
  try {
    if (isEdit.value) await updateMaintainPlan(params)
    else await insertMaintainPlan(params)
    ElMessage.success(isEdit.value ? '养护计划修改成功' : '养护计划创建成功')
    visible.value = false
    listRef.value?.load()
    await refreshOccupied()
  } catch (e) {
    ElMessage.error(e?.message || (isEdit.value ? '修改失败，请检查后端日志' : '创建失败，请检查后端日志'))
  } finally {
    submitting.value = false
  }
}

/* ----------------- 翻垛：同一垛位内重排设备坐标 ----------------- */
const flipVisible = ref(false)
const flipRow = ref(null)
const flipStackId = ref(null)
const flipNodes = ref([])
const flipLoading = ref(false)
const flipSubmitting = ref(false)

async function openFlip(row) {
  flipRow.value = row
  flipVisible.value = true
  flipNodes.value = []
  flipStackId.value = null
  flipLoading.value = true
  try {
    const vo = await findMaintainPlanById(row.id)
    const stackId = vo?.stackId
    if (!stackId) {
      ElMessage.warning('该养护计划未关联垛位，无法翻垛')
      return
    }
    flipStackId.value = Number(stackId)
    const nodes = rowsOf(await findNodeByStackId(flipStackId.value))
    flipNodes.value = (Array.isArray(nodes) ? nodes : []).map((n) => ({
      id: n.id,
      nodeId: n.nodeId,
      caseNo: n.caseNo,
      status: n.status,
      siteX: n.siteX ?? '',
      siteY: n.siteY ?? '',
      siteZ: n.siteZ ?? ''
    }))
    if (!flipNodes.value.length) ElMessage.info('该垛位下暂无设备，无需翻垛')
  } catch (e) {
    ElMessage.error(e?.message || '加载垛位设备失败')
  } finally {
    flipLoading.value = false
  }
}

async function submitFlip() {
  if (!flipStackId.value) return
  const payload = flipNodes.value.map((n) =>
    ({ id: n.id, nodeId: n.nodeId, siteX: n.siteX, siteY: n.siteY, siteZ: n.siteZ })
  )
  flipSubmitting.value = true
  try {
    await flipStack({ id: flipRow.value.id, stackId: flipStackId.value, nodeDtoList: JSON.stringify(payload) })
    ElMessage.success('翻垛成功')
    flipVisible.value = false
  } catch (e) {
    ElMessage.error(e?.message || '翻垛失败，请检查后端日志')
  } finally {
    flipSubmitting.value = false
  }
}

/* ----------------- 移垛：计划及设备整体迁到目标垛位 ----------------- */
const moveVisible = ref(false)
const moveRow = ref(null)
const moveStackId = ref(null)
const moveCurrentLabel = ref('')
const moveTargetId = ref('')
const moveStackTree = ref([])
const moveStackMap = ref({})
const moveNodes = ref([])
const moveSubmitting = ref(false)
/** 非「已入垛」的设备（解垛 / 故障 / 运维中 / 异常），移垛前给出提示 */
const moveAbnormalCount = computed(() =>
  moveNodes.value.filter((n) => String(n.status) !== '1').length
)

async function ensureOptionsLoaded() {
  if (!optionsLoaded.value) await loadOptions()
}

// 构建「仓库 → 楼 → 层 → 区 → 垛位」树，供移垛选择目标垛位；已有养护计划的垛位禁用
async function buildMoveStackTree() {
  const tree = []
  try {
    const whRows = rowsOf(await warehouseTree({ warehouseNodeId: 0, warehouseNodeType: '01' }))
    const areas = rowsOf(await getAllWareArea())
    for (const w of whRows) {
      const wn = { id: `w${w.id}`, label: w.name || `仓库 ${w.id}`, type: '01', disabled: true, children: [] }
      const bRows = rowsOf(await warehouseTree({ warehouseNodeId: w.id, warehouseNodeType: '02' }))
      for (const b of bRows) {
        const bn = { id: `b${b.id}`, label: b.buildingName || b.buildingNo || `楼栋 ${b.id}`, type: '02', disabled: true, children: [] }
        const fRows = rowsOf(await warehouseTree({ warehouseNodeId: b.id, warehouseNodeType: '03' }))
        for (const f of fRows) {
          const fn = { id: `f${f.id}`, label: f.floorName || f.floorNo || `楼层 ${f.id}`, type: '03', disabled: true, children: [] }
          for (const a of areas.filter((x) => Number(x.floorId) === Number(f.id))) {
            const an = { id: `a${a.id}`, label: a.areaName || a.areaNo || `分区 ${a.id}`, type: '04', disabled: true, children: [] }
            try {
              const stRows = rowsOf(await getStackPage({ wareAreaId: a.id, page: 1, size: 1000 }))
              for (const s of stRows) {
                const sid = String(s.id)
                const isOccupied = occupied.value.has(Number(sid))
                const isSelf = Number(sid) === Number(moveStackId.value)
                an.children.push({
                  id: sid,
                  label: `${s.nameCn || s.stackNo || '垛位 ' + s.id}${isOccupied ? '（已有计划）' : ''}`,
                  type: '05',
                  disabled: isOccupied || isSelf
                })
                moveStackMap.value[sid] = {
                  id: s.id,
                  nameCn: s.nameCn || s.stackNo || '',
                  pidRoute: s.pidRoute || '',
                  siteName: s.siteName || s.nameCn || ''
                }
              }
            } catch (e) { /* 单个分区失败不阻塞 */ }
            if (an.children.length) fn.children.push(an)
          }
          if (fn.children.length) bn.children.push(fn)
        }
        if (bn.children.length) wn.children.push(bn)
      }
      if (wn.children.length) tree.push(wn)
    }
    moveStackTree.value = tree
  } catch (e) {
    moveStackTree.value = []
  }
}

async function openMove(row) {
  moveRow.value = row
  moveVisible.value = true
  moveStackId.value = null
  moveCurrentLabel.value = ''
  moveTargetId.value = ''
  moveNodes.value = []
  try {
    await ensureOptionsLoaded()
    const vo = await findMaintainPlanById(row.id)
    const stackId = vo?.stackId
    if (!stackId) {
      ElMessage.warning('该养护计划未关联垛位，无法移垛')
      return
    }
    moveStackId.value = Number(stackId)
    const stack = moveStackMap.value[String(stackId)]
    moveCurrentLabel.value = stack ? `${stack.nameCn || '垛位'}（id=${stackId}）` : `垛位 id=${stackId}`
    const nodes = rowsOf(await findNodeByStackId(moveStackId.value))
    moveNodes.value = (Array.isArray(nodes) ? nodes : []).map((n) => ({
      id: n.id,
      nodeId: n.nodeId,
      caseNo: n.caseNo,
      status: n.status,
      // 设备当前所在垛位，后端据此判断"垛位没有移动"，避免同垛位误提交
      stackId: n.stackId ?? moveStackId.value,
      siteX: n.siteX ?? '',
      siteY: n.siteY ?? '',
      siteZ: n.siteZ ?? ''
    }))
    await buildMoveStackTree()
  } catch (e) {
    ElMessage.error(e?.message || '加载移垛信息失败')
  }
}

function onMoveTargetPick(node) {
  if (node?.type === '05') moveTargetId.value = node.id
}

async function submitMove() {
  if (!moveStackId.value || !moveTargetId.value) return
  const target = moveStackMap.value[moveTargetId.value]
  if (!target) {
    ElMessage.error('请选择目标垛位')
    return
  }
  // 完整位置路径（含 [05:目标垛位id]），后端据此解析仓间/层/楼栋/仓库并写节点 siteRoute
  const fullRoute = buildPidRoute({ pidRoute: target.pidRoute, id: target.id })
  const payload = moveNodes.value.map((n) =>
    ({ id: n.id, nodeId: n.nodeId, stackId: n.stackId, siteX: n.siteX, siteY: n.siteY, siteZ: n.siteZ })
  )
  moveSubmitting.value = true
  try {
    await moveStack({
      id: moveRow.value.id,
      stackId: Number(moveTargetId.value),
      nodeDtoList: JSON.stringify(payload),
      pidRoute: fullRoute,
      siteName: target.siteName || target.nameCn || ''
    })
    ElMessage.success('移垛成功')
    moveVisible.value = false
    listRef.value?.load()
    await refreshOccupied()
  } catch (e) {
    ElMessage.error(e?.message || '移垛失败，请检查后端日志')
  } finally {
    moveSubmitting.value = false
  }
}

// ---------- 养护完成 & 全链路统计 ----------
const finishingId = ref(null)
const statsVisible = ref(false)
const statsLoading = ref(false)
const statsError = ref('')
const statsTab = ref('stages')
const statsRow = ref(null)
const statData = ref(null)

const statSummary = computed(() => statData.value?.summary || null)
const statStages = computed(() => statData.value?.stages || [])
const statWarningTypes = computed(() => statData.value?.warnings?.byType || [])
const statWarningList = computed(() => statData.value?.warnings?.list || [])
const statPestBoards = computed(() => statData.value?.pests?.byBoard || [])
const statPestList = computed(() => statData.value?.pests?.list || [])
const statMaintainResult = computed(() => statData.value?.maintainResult || {})

const statCards = computed(() => {
  const s = statSummary.value
  if (!s) return []
  return [
    { label: '养护时长', value: `${s.durationDays} 天`, sub: `共 ${s.stageCount} 个阶段` },
    { label: '阶段转换', value: `${s.changeCount} 次`, sub: `末阶段 ${s.stageCount > 0 ? '已到达' : '无记录'}` },
    { label: '预警次数', value: s.warningTotal, sub: `未处置 ${s.warningUntreated}`, color: s.warningTotal ? 'var(--el-color-danger)' : '' },
    { label: '甲虫数量', value: s.beetleTotal, sub: `${s.pestSampleCount} 次虫情采集` },
    { label: '粉螟数量', value: s.mealwormTotal, sub: `告警采集 ${s.pestAlertSamples} 次` },
    { label: '养护结果', value: s.maintainResultLabel, sub: `累计预警 ${s.earlyWarningHours} 小时` }
  ]
})

const warningCards = computed(() => {
  const s = statSummary.value
  if (!s) return []
  return [
    { label: '预警总数', value: s.warningTotal },
    { label: '虫害预警', value: s.pestWarningCount },
    { label: '设备异常', value: s.deviceFaultCount },
    { label: '环境预警', value: s.environmentWarningCount }
  ]
})

const pestCards = computed(() => {
  const p = statData.value?.pests || {}
  return [
    { label: '甲虫累计', value: p.beetleTotal ?? 0 },
    { label: '粉螟累计', value: p.mealwormTotal ?? 0 },
    { label: '单次甲虫峰值', value: p.maxBeetle ?? 0 },
    { label: '单次粉螟峰值', value: p.maxMealworm ?? 0 }
  ]
})

async function loadStatistics(row) {
  statData.value = null
  statsError.value = ''
  statsLoading.value = true
  try {
    const data = await findMaintainPlanStatistics(row.id)
    statData.value = data || null
    if (!statData.value?.summary) statsError.value = '统计接口未返回数据'
  } catch (e) {
    statsError.value = e?.message || '加载全链路统计失败'
  } finally {
    statsLoading.value = false
  }
}

function openStatistics(row) {
  // 全链路统计只在养护完成后展示，防止对进行中的计划误开归档视图
  if (!isFinished(row)) {
    ElMessage.warning('全链路统计在养护完成后才可查看')
    return
  }
  statsRow.value = row
  statsTab.value = 'stages'
  statsVisible.value = true
  loadStatistics(row)
}

function reloadStatistics() {
  if (statsRow.value) loadStatistics(statsRow.value)
}

async function finishPlan(row) {
  try {
    await ElMessageBox.confirm(
      `确认完成养护计划 #${row.id}（${row.materialCode || '-'}）吗？开垛完成后计划将标记为已结束，并自动展示全链路统计。`,
      '养护完成确认',
      { type: 'warning', confirmButtonText: '确认完成', cancelButtonText: '取消' }
    )
  } catch (e) {
    return
  }
  finishingId.value = row.id
  try {
    const vo = await findMaintainPlanById(row.id)
    const stackId = vo?.stackId ?? row.stackId
    const stageId = vo?.stageId ?? row.stageId
    if (!stackId || !stageId) {
      ElMessage.warning('未获取到该计划的垛位 / 阶段信息，无法完成养护')
      return
    }
    await endMaintainPlan({ id: row.id, stackId, stageId })
    ElMessage.success('养护完成（开垛）成功，已生成全链路统计')
    await refreshOccupied()
    // 完成后该计划不再属于「进行中」列表，自动切到「已完成」筛选，
    // 这样关闭统计弹窗后仍能从列表重新打开全链路统计。
    listRef.value?.setQuery({ isEnd: '1' })
    openStatistics({ ...row, isEnd: '1' })
  } catch (e) {
    ElMessage.error(e?.message || '养护完成失败，请检查是否存在未处理的任务')
  } finally {
    finishingId.value = null
  }
}

// ---------- 养护阶段转段 ----------
/** 与后端 getPhases 的全集顺序一致：养护 → 充氮杀虫 → 磷化铝杀虫（→ 充氮保质） */
const STAGE_SEQUENCE = ['养护阶段', '充氮杀虫阶段', '磷化铝杀虫阶段']
const stageVisible = ref(false)
const stageLoading = ref(false)
const stageSubmitting = ref(false)
const stageError = ref('')
const stageRow = ref(null)
const stageMeta = ref(null)
const stageCurrent = ref('')
const stageCandidates = ref([])
const stageTarget = ref('')
const stageStart = ref(today())
const stageDays = ref(30)
const stageRuleName = ref('')

async function openStage(row) {
  stageRow.value = row
  stageVisible.value = true
  stageLoading.value = true
  stageError.value = ''
  stageMeta.value = null
  stageCurrent.value = ''
  stageCandidates.value = []
  stageTarget.value = ''
  stageStart.value = today()
  stageDays.value = 30
  stageRuleName.value = ''
  try {
    await ensureOptionsLoaded()
    const voRes = await beforeUpdateMaintainPlan(row.id)
    const vo = voRes?.data ?? voRes ?? {}
    const stackId = Number(vo.stackId)
    if (!stackId) throw new Error('该计划未关联垛位，无法转段')

    // 后端 changeMaintainPlanStage 会自己用 SiteRouteUtil.route 拼 [05:垛位]，
    // 所以要传 tb_stack 的原始 pidRoute，不能传 beforeUpdate 已拼好的完整路由。
    const stack = stacks.value.find((s) => Number(s.id) === stackId)
    const rawRoute = String(stack?.pidRoute || '').trim()
    if (!rawRoute) throw new Error('未取到垛位的父级路径(pidRoute)，无法转段')

    // 已用阶段取全链路统计的阶段时间线；统计不可用时不阻塞转段，只跳过自动推荐
    let used = []
    try {
      const stat = await findMaintainPlanStatistics(row.id)
      used = (stat?.stages || []).map((s) => String(s.phases || '').trim()).filter(Boolean)
    } catch (e) { /* 统计接口异常时仍允许手工选阶段 */ }
    stageCurrent.value = used.length ? used[used.length - 1] : String(vo.phases || '')

    const style = normalizeStyle(vo.maintenceStyle) || '01'
    const all = STAGE_SEQUENCE.slice()
    if (style === '02' || style === '03') all.push('充氮保质阶段')
    stageCandidates.value = all.filter((p) => !used.includes(p))
    if (!stageCandidates.value.length) stageCandidates.value = all.slice()
    stageTarget.value = stageCandidates.value[0]

    // 阈值优先沿用计划现有值，缺失时回退到满足严格递增的默认值
    const saved = vo.alarmValueSettingVo || {}
    const thresholds = { ...DEFAULT_THRESHOLDS }
    THRESHOLDS.forEach((f) => {
      const v = saved[f.prop] ?? vo[f.prop]
      if (v !== null && v !== undefined && v !== '') thresholds[f.prop] = Number(v)
    })

    const materialList = Array.isArray(vo.materialCodes) && vo.materialCodes.length
      ? [...vo.materialCodes]
      : String(vo.materialCode || '').split(',').map((s) => s.trim()).filter(Boolean)
    if (!materialList.length) throw new Error('该计划没有物料编码，无法转段')
    const principalList = (Array.isArray(vo.principalIds) ? vo.principalIds : [])
      .map((x) => Number(x))
      .filter((n) => Number.isFinite(n) && n > 0)

    stageRuleName.value = saved.earlyWarningRuleName || ruleNames.value[0] || '转段沿用阈值'
    stageMeta.value = {
      id: row.id,
      stackId,
      pidRoute: rawRoute,
      maintenceStyle: style,
      maintainDays: Number(vo.maintainDays ?? 30),
      oxygenReductionTime: String(vo.oxygenReductionTime ?? '5'),
      expireTime: defaultExpire(),
      maintainCount: Number(vo.maintainCount ?? 1000),
      materialCodes: materialList,
      principalIds: principalList.length ? principalList : [2],
      thresholds
    }
  } catch (e) {
    stageError.value = e?.message || '加载转段信息失败'
  } finally {
    stageLoading.value = false
  }
}

async function submitStage() {
  const m = stageMeta.value
  if (!m || !stageTarget.value || !stageStart.value) return
  stageSubmitting.value = true
  try {
    await changeMaintainPlanStage({
      ...m.thresholds,
      id: m.id,
      stackId: m.stackId,
      pidRoute: m.pidRoute,
      maintenceStyle: m.maintenceStyle,
      phases: stageTarget.value,
      maintainDays: Number(stageDays.value),
      aftercareDateStart: stageStart.value,
      oxygenReductionTime: m.oxygenReductionTime,
      expireTime: m.expireTime,
      maintainCount: m.maintainCount,
      earlyWarningRuleName: stageRuleName.value || '转段沿用阈值',
      materialCodes: m.materialCodes,
      principalIds: m.principalIds
    })
    ElMessage.success(`已转到「${stageTarget.value}」`)
    stageVisible.value = false
    listRef.value?.load()
  } catch (e) {
    ElMessage.error(e?.message || '转段失败，请检查是否存在未处理的任务')
  } finally {
    stageSubmitting.value = false
  }
}
</script>

<style scoped>
.plan-alert {
  margin-bottom: 12px;
}
.plan-tip {
  margin-top: 8px;
  font-size: 12px;
  line-height: 1.5;
  color: var(--el-text-color-secondary);
}
.plan-collapse {
  margin-top: 4px;
}
.plan-stack-hint {
  font-size: 11px;
  line-height: 1.6;
  opacity: 0.7;
  margin-top: 2px;
}
.stat-cards {
  margin-bottom: 14px;
}
.stat-cards-mini {
  margin-top: 8px;
  margin-bottom: 6px;
}
.stat-card {
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  padding: 10px 12px;
  background: var(--el-bg-color-overlay);
  min-height: 74px;
}
.stat-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.4;
}
.stat-value {
  font-size: 21px;
  font-weight: 600;
  line-height: 1.5;
  color: var(--el-text-color-primary);
}
.stat-sub {
  font-size: 11px;
  color: var(--el-text-color-secondary);
  line-height: 1.4;
}
</style>
