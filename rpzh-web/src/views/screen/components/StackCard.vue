<script setup>
import { computed } from 'vue'
import { HEALTH_DICT } from '../mockData'

const props = defineProps({
  stack: { type: Object, required: true },
  selected: { type: Boolean, default: false }
})
defineEmits(['select'])

const tone = computed(() => HEALTH_DICT[props.stack.health] || HEALTH_DICT.normal)
const styleVars = computed(() => ({
  '--tone': tone.value.color,
  '--tone-soft': `${tone.value.color}22`,
  '--tone-line': `${tone.value.color}66`
}))
</script>

<template>
  <button
    type="button"
    class="stack-card"
    :class="{ 'is-selected': selected, [`is-${stack.health}`]: true }"
    :style="styleVars"
    @click="$emit('select', stack)"
  >
    <div class="sc__head">
      <span class="sc__name">{{ stack.code }}</span>
      <span class="sc__dot" :title="tone.label" />
      <span class="sc__phase">{{ stack.plan.phases }}</span>
    </div>

    <div class="sc__plan">
      <div class="sc__plan-line">
        <span class="sc__mat">{{ stack.plan.materialCode }}</span>
        <span class="sc__style">{{ stack.plan.styleLabel }}</span>
      </div>
      <div class="sc__progress">
        <div class="sc__bar"><i :style="{ width: `${stack.plan.progress}%` }" /></div>
        <span>{{ stack.plan.elapsedDays }}/{{ stack.plan.planDays }}天 · {{ stack.plan.progress }}%</span>
      </div>
      <div class="sc__meta">
        <span>负责人 {{ stack.plan.principal }}</span>
        <span>起始 {{ stack.plan.startDate }}</span>
        <span>计划 #{{ stack.plan.id }}</span>
      </div>
    </div>

    <div class="sc__metrics">
      <div><b>T</b>{{ stack.metrics.temp }}<small>℃</small></div>
      <div><b>H</b>{{ stack.metrics.humidity }}<small>%</small></div>
      <div><b>水</b>{{ stack.metrics.moisture }}<small>%</small></div>
    </div>

    <div class="sc__foot">
      <span>节点 {{ stack.onlineNodes }}/{{ stack.totalNodes }}</span>
      <span>虫 {{ stack.metrics.pestTotal }}</span>
      <span v-if="stack.warnings.length" class="sc__warn">预警 {{ stack.warnings.length }}</span>
      <span v-else class="sc__ok">正常</span>
    </div>
  </button>
</template>

<style scoped>
.stack-card {
  position: relative;
  display: block;
  width: 100%;
  text-align: left;
  padding: 9px 10px 8px;
  border-radius: 8px;
  border: 1px solid var(--tone-line);
  background: linear-gradient(160deg, var(--tone-soft), #0e1c28 72%);
  color: #cfe0ec;
  cursor: pointer;
  transition: transform .16s, box-shadow .16s, border-color .16s;
  font-family: inherit;
}
.stack-card:hover { transform: translateY(-2px); box-shadow: 0 6px 18px #0007; border-color: var(--tone); }
.stack-card.is-selected { box-shadow: 0 0 0 2px var(--tone), 0 8px 22px #0008; }
.stack-card::before {
  content: ''; position: absolute; left: 0; top: 10px; bottom: 10px;
  width: 2px; border-radius: 2px; background: var(--tone); opacity: .8;
}
.sc__head { display: flex; align-items: center; gap: 6px; margin-bottom: 6px; }
.sc__name { font-size: 13px; font-weight: 700; color: #eaf5ff; letter-spacing: .3px; }
.sc__dot { width: 7px; height: 7px; border-radius: 50%; background: var(--tone); box-shadow: 0 0 7px var(--tone); flex: none; }
.sc__phase {
  margin-left: auto; font-size: 9px; color: var(--tone);
  background: var(--tone-soft); border: 1px solid var(--tone-line);
  padding: 1px 5px; border-radius: 4px; white-space: nowrap;
  max-width: 88px; overflow: hidden; text-overflow: ellipsis;
}
.sc__plan-line { display: flex; align-items: center; justify-content: space-between; gap: 6px; }
.sc__mat { font-size: 10.5px; color: #9fc0d4; font-family: Consolas, monospace; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.sc__style { font-size: 9.5px; color: #7f9bb0; flex: none; }
.sc__progress { margin-top: 5px; display: flex; align-items: center; gap: 6px; font-size: 9px; color: #6f8ba1; white-space: nowrap; }
.sc__bar { flex: 1; height: 3px; border-radius: 2px; background: #1d3345; overflow: hidden; }
.sc__bar i { display: block; height: 100%; background: var(--tone); border-radius: 2px; }
.sc__meta { display: flex; align-items: center; gap: 10px; margin-top: 6px; font-size: 9px; color: #66879e; white-space: nowrap; overflow: hidden; }
.sc__meta span { overflow: hidden; text-overflow: ellipsis; }
.sc__meta span:last-child { margin-left: auto; flex: none; }
.sc__metrics {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 3px;
  margin: 7px 0 6px; font-size: 12px; color: #e2f0f8;
}
.sc__metrics div { white-space: nowrap; }
.sc__metrics b { font-size: 9px; color: #5f8ba3; font-weight: 500; margin-right: 3px; }
.sc__metrics small { font-size: 9px; color: #7d9bb0; margin-left: 1px; }
.sc__foot {
  display: flex; align-items: center; gap: 8px;
  padding-top: 5px; border-top: 1px solid #ffffff12;
  font-size: 9.5px; color: #7d9bb0;
}
.sc__warn { margin-left: auto; color: var(--tone); }
.sc__ok { margin-left: auto; color: #4fbf8b; }
</style>
