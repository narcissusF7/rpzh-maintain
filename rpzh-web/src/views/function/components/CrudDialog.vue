<template>
  <el-dialog
    :model-value="modelValue"
    :title="title"
    :width="width"
    top="8vh"
    :close-on-click-modal="false"
    destroy-on-close
    @update:model-value="(v) => emit('update:modelValue', v)"
  >
    <el-alert v-if="tip" class="crud-tip" type="info" show-icon :closable="false">
      <template #title>{{ tip }}</template>
    </el-alert>

    <el-form ref="formRef" :model="form" :rules="builtRules" :label-width="labelWidth">
      <el-row :gutter="10">
        <el-col v-for="f in visibleFields" :key="f.prop" :span="f.span || 12">
          <el-form-item :label="f.label" :prop="f.prop">
            <el-select
              v-if="f.type === 'select' || f.type === 'multiselect'"
              v-model="form[f.prop]"
              :multiple="f.type === 'multiselect'"
              :filterable="f.filterable !== false"
              :allow-create="!!f.allowCreate"
              :default-first-option="!!f.allowCreate"
              :collapse-tags="f.type === 'multiselect'"
              :clearable="f.clearable !== false"
              :disabled="isDisabled(f)"
              :placeholder="f.placeholder || `请选择${f.label}`"
              style="width: 100%"
              @change="emitChange(f)"
            >
              <el-option
                v-for="o in optionsOf(f)"
                :key="o.value"
                :label="o.label"
                :value="o.value"
              />
            </el-select>

            <el-tree-select
              v-else-if="f.type === 'tree'"
              v-model="form[f.prop]"
              :data="treeOf(f)"
              :props="treeProps"
              node-key="id"
              value-key="id"
              :render-after-expand="false"
              :check-strictly="true"
              :clearable="f.clearable !== false"
              :disabled="isDisabled(f)"
              :placeholder="f.placeholder || `请选择${f.label}`"
              style="width: 100%"
              @change="emitChange(f)"
            />

            <el-input-number
              v-else-if="f.type === 'number'"
              v-model="form[f.prop]"
              :min="f.min ?? 0"
              :max="f.max ?? 999999999"
              :precision="f.precision ?? 0"
              :step="f.step ?? 1"
              :disabled="isDisabled(f)"
              controls-position="right"
              style="width: 100%"
              @change="emitChange(f)"
            />

            <el-date-picker
              v-else-if="f.type === 'date' || f.type === 'datetime'"
              v-model="form[f.prop]"
              :type="f.type === 'datetime' ? 'datetime' : 'date'"
              :value-format="f.type === 'datetime' ? 'YYYY-MM-DD HH:mm:ss' : 'YYYY-MM-DD'"
              :disabled="isDisabled(f)"
              :placeholder="f.placeholder || `请选择${f.label}`"
              style="width: 100%"
              @change="emitChange(f)"
            />

            <el-switch
              v-else-if="f.type === 'switch'"
              v-model="form[f.prop]"
              :active-value="f.activeValue ?? '1'"
              :inactive-value="f.inactiveValue ?? '0'"
              :disabled="isDisabled(f)"
            />

            <el-input
              v-else
              v-model="form[f.prop]"
              :type="f.type === 'textarea' ? 'textarea' : f.type === 'password' ? 'password' : 'text'"
              :rows="f.rows || 2"
              :maxlength="f.maxlength"
              :show-word-limit="!!f.maxlength"
              :disabled="isDisabled(f)"
              :placeholder="f.placeholder || `请输入${f.label}`"
              clearable
            />
            <div v-if="f.hint" class="crud-field-hint">{{ f.hint }}</div>
          </el-form-item>
        </el-col>
      </el-row>
      <slot name="extra" :form="form" />
    </el-form>

    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { cleanParams } from '@/utils/form'

/**
 * 养护业务通用「新建 / 修改」弹窗。
 * 页面只需提供 fields 配置 + submit(payload, mode) 回调，
 * 表单渲染、必填校验、请求失败提示、成功后刷新都在这里统一处理。
 *
 * fields 项：
 * { prop, label, type: text|textarea|password|number|select|multiselect|date|datetime|switch,
 *   span, required, options: Array|() => Array, defaultValue, disabled: bool|fn,
 *   hidden: bool|fn, min/max/precision/step, placeholder, hint, rules, transform }
 */
const props = defineProps({
  modelValue: { type: Boolean, default: false },
  title: { type: String, default: '编辑' },
  fields: { type: Array, default: () => [] },
  /** 编辑时传入当前行；新建时传 null 或默认值对象 */
  model: { type: Object, default: null },
  mode: { type: String, default: 'create' }, // create | edit
  /** 新建时的默认值对象 */
  defaults: { type: Object, default: () => ({}) },
  /** (payload, mode) => Promise；业务失败请 throw / reject */
  submit: { type: Function, required: true },
  width: { type: String, default: '680px' },
  labelWidth: { type: String, default: '120px' },
  tip: { type: String, default: '' },
  successMessage: { type: String, default: '操作成功' },
  /** 提交前最后一次加工：payload => payload | Promise<payload> */
  transform: { type: Function, default: null },
})
const emit = defineEmits(['update:modelValue', 'success', 'opened'])

const formRef = ref(null)
const submitting = ref(false)
const form = reactive({})

const visibleFields = computed(() =>
  props.fields.filter((f) => {
    if (typeof f.hidden === 'function') return !f.hidden(form, props.mode)
    return !f.hidden
  })
)

const builtRules = computed(() => {
  const rules = {}
  props.fields.forEach((f) => {
    if (f.rules) {
      rules[f.prop] = f.rules
      return
    }
    if (!f.required) return
    const isArray = f.type === 'multiselect'
    const rule = {
      required: true,
      message: `${f.type === 'select' || f.type === 'date' || f.type === 'datetime' || isArray ? '请选择' : '请输入'}${f.label}`,
      trigger: isArray || f.type === 'select' || f.type === 'date' || f.type === 'datetime' ? 'change' : 'blur'
    }
    if (isArray) {
      rule.type = 'array'
      rule.min = 1
    }
    rules[f.prop] = [rule]
  })
  return rules
})

function optionsOf(f) {
  const raw = typeof f.options === 'function' ? f.options() : f.options
  const list = Array.isArray(raw) ? raw.slice() : []
  /**
   * 历史值兜底：编辑时如果当前值在选项里找不到（典型场景：负责人早期存的是姓名，
   * 现在下拉用的是账号），el-select 会显示空白，看起来像"回显丢失"。
   * 这里补一条 `原名（历史值）`，保证原值可见；不动它就不会改数据。
   * 注意：只是「类型不同」（数字 1 vs 字符串 '1'）的情况由 normalizeSelectTypes 处理，
   * 不在这里加别名，否则下拉会出现两个"是"。
   */
  const cur = form[f.prop]
  const values = Array.isArray(cur) ? cur : [cur]
  values.forEach((v) => {
    if (v === '' || v === null || v === undefined) return
    if (!list.some((o) => String(o.value) === String(v))) {
      list.unshift({ value: v, label: `${v}（历史值）` })
    }
  })
  return list
}

/**
 * 下拉值类型归一：接口常返回数字 1/0，而选项值是字符串 '1'/'0'，
 * el-select 用全等匹配不上会把原始值直接显示出来（"自动化 1"而不是"自动化 是"）。
 * 打开弹窗时按选项把模型值换成同类型，副作用最小且不会在下拉里产生重复项。
 */
function normalizeSelectTypes() {
  props.fields.forEach((f) => {
    if (f.type !== 'select' && f.type !== 'multiselect') return
    const raw = typeof f.options === 'function' ? f.options() : f.options
    if (!Array.isArray(raw) || !raw.length) return
    const cur = form[f.prop]
    if (Array.isArray(cur)) {
      form[f.prop] = cur.map((v) => {
        const hit = raw.find((o) => String(o.value) === String(v))
        return hit ? hit.value : v
      })
      return
    }
    if (cur === '' || cur === null || cur === undefined) return
    const hit = raw.find((o) => String(o.value) === String(cur))
    if (hit && hit.value !== cur) form[f.prop] = hit.value
  })
}

/** 树选择器数据：支持 { tree: Array | () => Array }，节点需含 id / label / children */
const treeProps = { label: 'label', children: 'children' }
function treeOf(f) {
  const raw = typeof f.tree === 'function' ? f.tree() : f.tree
  return Array.isArray(raw) ? raw : []
}

function isDisabled(f) {
  if (typeof f.disabled === 'function') return f.disabled(form, props.mode)
  return !!f.disabled
}

/** 字段变更回调：用于「选择 A 自动带出 B」（如选用户带出姓名、选位置带出路径/ID） */
function emitChange(f) {
  if (typeof f.onChange === 'function') f.onChange(form[f.prop], form)
}

function fillForm() {
  Object.keys(form).forEach((k) => delete form[k])
  const base = {}
  props.fields.forEach((f) => {
    base[f.prop] = f.defaultValue !== undefined ? f.defaultValue : (f.type === 'multiselect' ? [] : '')
  })
  Object.assign(form, base, props.defaults || {}, props.model || {})
  normalizeSelectTypes()
}

watch(
  () => props.modelValue,
  async (open) => {
    if (!open) return
    fillForm()
    await nextTick()
    formRef.value?.clearValidate()
    emit('opened', form)
  }
)

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }

  // 只提交当前可见字段，避免把行数据里的 VO 冗余字段（如 xxxNow / Vos）带给后端
  const allowed = new Set(visibleFields.value.map((f) => f.prop))
  let payload = cleanParams(
    Object.fromEntries(Object.entries(form).filter(([k]) => allowed.has(k)))
  )
  visibleFields.value.forEach((f) => {
    if (typeof f.transform === 'function' && payload[f.prop] !== undefined) {
      payload[f.prop] = f.transform(payload[f.prop], form)
    }
  })
  if (typeof props.transform === 'function') {
    payload = await props.transform(payload, props.mode, form)
  }

  submitting.value = true
  try {
    await props.submit(payload, props.mode)
    ElMessage.success(props.successMessage)
    emit('update:modelValue', false)
    emit('success', payload, props.mode)
  } catch (e) {
    // /maintain/** 默认静默，这里把后端 Result.message 明确展示出来
    ElMessage.error(e?.message || '操作失败，请检查后端日志')
  } finally {
    submitting.value = false
  }
}

defineExpose({ form, fillForm })
</script>

<style scoped>
.crud-tip {
  margin-bottom: 14px;
}
.crud-field-hint {
  font-size: 11px;
  line-height: 1.5;
  opacity: 0.6;
  margin-top: 2px;
}
</style>
