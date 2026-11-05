<template>
  <div class="app-page">
    <div class="app-card function-head">
      <div class="function-head__text">
        <h2>物料管理</h2>
        <p>按垛位查询物料信息与报警阈值，以及全量物料编码</p>
      </div>
      <el-button size="small" :icon="RefreshRight" :loading="loading" @click="loadAll">刷新</el-button>
    </div>

    <el-row :gutter="12">
      <el-col :xs="24" :md="12">
        <div class="app-card material-card">
          <div class="material-card__title">按垛位查询物料</div>
          <div class="material-card__search">
            <el-select v-model="stackId" placeholder="选择垛位" style="width: 220px" clearable>
              <el-option
                v-for="s in stacks"
                :key="s.id"
                :label="`${s.nameCn}（${s.stackNo}）`"
                :value="s.id"
              />
            </el-select>
            <el-button type="primary" :icon="Search" :disabled="!stackId" @click="queryMaterial">查询</el-button>
          </div>

          <el-alert v-if="materialError" type="warning" show-icon :closable="false" class="material-alert">
            <template #title>物料信息接口暂不可用</template>
            <template #default>{{ shortReason(materialError) }}</template>
          </el-alert>

          <el-descriptions v-if="materialRows.length" :column="2" border size="small" class="material-desc">
            <el-descriptions-item v-for="f in materialRows" :key="f.key" :label="f.label">
              {{ f.value }}
            </el-descriptions-item>
          </el-descriptions>
          <el-empty v-else-if="!materialError" :image-size="64" description="选择垛位后点击查询" />
        </div>
      </el-col>

      <el-col :xs="24" :md="12">
        <div class="app-card material-card">
          <div class="material-card__title">全量物料编码</div>
          <el-alert v-if="codesError" type="warning" show-icon :closable="false" class="material-alert">
            <template #title>物料编码接口暂不可用</template>
            <template #default>{{ shortReason(codesError) }}</template>
          </el-alert>
          <div v-else-if="codes.length" class="code-tags">
            <el-tag v-for="c in codes" :key="c" class="code-tag" effect="plain">{{ c }}</el-tag>
          </div>
          <el-empty v-else :image-size="64" description="暂无物料编码" />
        </div>
      </el-col>
    </el-row>

    <el-alert class="function-alert" type="info" show-icon :closable="false">
      <template #title>外部视图依赖</template>
      <template #default>
        MaterialDao.xml 的查询指向跨库视图 MC.VW_MAINTAIN_INVENTORY（QUICKCODE / MATERIALCODE / INVOICEQTY）。
        本地 MySQL 已增量创建同结构兼容视图，物料编码与详情接口现可正常返回（物料来自外部系统，本页为只读台账，不提供新建／修改）。
        垛位台账的新建、修改、删除请到「仓库管理」页；垛位与养护计划的关联数据可在「养护计划」页查看。
      </template>
    </el-alert>

    <div class="app-card">
      <div class="table-toolbar">
        <span class="table-toolbar__title">垛位列表</span>
        <span class="table-toolbar__count">共 {{ stacks.length }} 条</span>
      </div>
      <div class="table-wrap">
        <el-table v-loading="loading" :data="stacks" border stripe size="small" @row-click="(row) => (stackId = row.id)">
          <el-table-column prop="stackNo" label="垛位编号" width="120" />
          <el-table-column prop="nameCn" label="垛位名称" min-width="150" />
          <el-table-column prop="siteName" label="位置路径" min-width="240" show-overflow-tooltip />
          <el-table-column prop="remark" label="备注" width="110" />
          <template #empty>
            <el-empty :image-size="64" description="暂无垛位数据" />
          </template>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Search, RefreshRight } from '@element-plus/icons-vue'
import { getStackPage } from '@/api/function/warehouse'
import { findMaterialInfo, findAllMaterialCodes } from '@/api/function/material'
import { shortReason } from '@/utils/function'

const loading = ref(false)
const stacks = ref([])
const stackId = ref(null)
const materialInfo = ref(null)
const materialError = ref('')
const codes = ref([])
const codesError = ref('')

const fieldLabels = {
  materialCode: '物料编码',
  quickCode: '快速编码',
  maintainCount: '养护数量',
  stackNo: '垛位编号',
  stackId: '垛位 ID',
  invoiceQty: '单据数量'
}

const materialRows = computed(() => {
  const data = materialInfo.value
  if (!data || typeof data !== 'object') return []
  return Object.entries(data)
    .filter(([, v]) => v !== null && v !== undefined && v !== '')
    .slice(0, 20)
    .map(([key, value]) => ({ key, label: fieldLabels[key] || key, value: String(value) }))
})

async function loadStacks() {
  try {
    const data = await getStackPage({ wareAreaId: 201, page: 1, size: 50 })
    stacks.value = Array.isArray(data?.list) ? data.list : Array.isArray(data) ? data : []
  } catch {
    stacks.value = []
  }
}

async function queryMaterial() {
  materialError.value = ''
  materialInfo.value = null
  try {
    materialInfo.value = await findMaterialInfo(stackId.value)
  } catch (e) {
    materialError.value = e?.message || '接口暂不可用'
  }
}

async function loadCodes() {
  codesError.value = ''
  codes.value = []
  try {
    const data = await findAllMaterialCodes()
    codes.value = Array.isArray(data) ? data.map(String) : data ? [String(data)] : []
  } catch (e) {
    codesError.value = e?.message || '接口暂不可用'
  }
}

async function loadAll() {
  loading.value = true
  await Promise.all([loadStacks(), loadCodes()])
  loading.value = false
}

onMounted(loadAll)
</script>

<style scoped>
.function-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;
  margin-bottom: 12px;
}
.function-head__text h2 {
  margin: 0 0 4px;
  font-size: 18px;
  font-weight: 600;
}
.function-head__text p {
  margin: 0;
  font-size: 12px;
  opacity: 0.65;
}
.material-card {
  padding: 16px;
  min-height: 240px;
  margin-bottom: 12px;
}
.material-card__title {
  font-weight: 600;
  margin-bottom: 14px;
}
.material-card__search {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
}
.material-alert {
  margin-bottom: 12px;
}
.material-desc {
  margin-top: 6px;
}
.code-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.code-tag {
  font-size: 12px;
}
.function-alert {
  margin-bottom: 12px;
}
.table-toolbar__title {
  font-weight: 600;
}
.table-toolbar__count {
  margin-left: 12px;
  font-size: 12px;
  opacity: 0.6;
}
</style>
