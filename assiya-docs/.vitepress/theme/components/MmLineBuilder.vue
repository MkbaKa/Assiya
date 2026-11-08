<script setup>
import { computed, reactive, ref, watch } from 'vue'

const props = defineProps({
  title: { type: String, default: '' },
  hint: { type: String, required: true },
  skill: { type: String, required: true },
  fields: { type: Array, default: () => [] },
  switches: { type: Array, default: () => [] },
  targeter: { type: String, required: true },
  targeters: {
    type: Array,
    required: true
  },
  suffix: { type: String, default: '' }
})

const state = reactive({})
const selectedTargeter = ref(props.targeter)

function initState() {
  for (const f of props.fields) {
    state[f.id] = f.default !== undefined ? f.default : ''
  }
  for (const s of props.switches) {
    state[s.id] = Boolean(s.default)
  }
  selectedTargeter.value = props.targeter
}

initState()
watch(() => [props.skill, props.fields, props.switches, props.targeter], initState, { deep: true })

const generatedLine = computed(() => {
  const parts = []
  const rawParts = []
  for (const s of props.switches) {
    if (state[s.id]) {
      parts.push(`${s.key || s.id}=true`)
    }
  }
  for (const f of props.fields) {
    const val = state[f.id]
    if (val === '' || val === null || val === undefined) continue
    if (f.omitIf !== undefined && String(val) === String(f.omitIf)) continue
    if (f.raw) {
      const trimmed = String(val).trim()
      if (trimmed) rawParts.push(trimmed)
    } else {
      const key = f.key || f.id
      parts.push(`${key}=${val}`)
    }
  }
  parts.push(...rawParts)
  const body = parts.length ? `{${parts.join(';')}}` : ''
  const tail = props.suffix ? ` ${props.suffix}` : selectedTargeter.value ? ` ${selectedTargeter.value}` : ''
  return `- ${props.skill}${body}${tail}`
})

const editableLine = ref('')
const copyStatus = ref('')
let copyAttempt = 0
watch(generatedLine, line => { editableLine.value = line }, { immediate: true })
watch(editableLine, () => {
  copyAttempt++
  copyStatus.value = ''
}, { flush: 'sync' })

async function copyLine() {
  const attempt = ++copyAttempt
  try {
    await navigator.clipboard.writeText(editableLine.value)
    if (attempt === copyAttempt) copyStatus.value = '已复制'
  } catch {
    if (attempt === copyAttempt) copyStatus.value = '复制失败，请重试'
  }
}
</script>

<template>
  <div class="mm-builder">
    <div class="mm-builder-head">
      <span class="mm-title">{{ title || `${skill} 交互拼装` }}</span>
      <span class="mm-hint">{{ hint }}</span>
    </div>

    <div class="mm-grid">
      <div
        v-for="f in fields"
        :key="f.id"
        class="mm-field"
        :class="{ 'mm-field-wide': f.wide }"
      >
        <label>{{ f.label }}</label>
        <input
          v-if="f.type === 'number'"
          v-model.number="state[f.id]"
          type="number"
          :min="f.min"
          :max="f.max"
          :placeholder="f.placeholder || ''"
        />
        <select v-else-if="f.options" v-model="state[f.id]">
          <option v-for="opt in f.options" :key="opt" :value="opt">{{ opt }}</option>
        </select>
        <input
          v-else
          v-model="state[f.id]"
          type="text"
          :placeholder="f.placeholder || ''"
        />
      </div>

      <div v-if="switches.length" class="mm-switches">
        <label v-for="s in switches" :key="s.id" class="mm-check">
          <input v-model="state[s.id]" type="checkbox" />
          <span v-html="s.label" />
        </label>
      </div>

      <div v-if="!suffix && targeters && targeters.length" class="mm-field">
        <label>MM 目标选择器</label>
        <select v-model="selectedTargeter">
          <option v-for="t in targeters" :key="t" :value="t">{{ t }}</option>
        </select>
      </div>
    </div>

    <div class="mm-field">
      <label>
        生成的配置行
        <textarea v-model="editableLine" class="res-code" rows="3" spellcheck="false" />
      </label>
      <div class="res-editor">
        <button type="button" :disabled="!editableLine.trim()" @click="copyLine">复制</button>
      </div>
      <span v-if="copyStatus" class="res-status" role="status">{{ copyStatus }}</span>
    </div>
  </div>
</template>

<style scoped>
.mm-builder {
  margin: 18px 0;
  padding: 16px 20px;
  border-radius: 12px;
  border: 1px solid var(--vp-c-divider);
  background: var(--vp-c-bg-soft);
}

.mm-builder-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 14px;
}

.mm-title {
  font-size: 13px;
  font-weight: 700;
  color: var(--vp-c-text-1);
}

.mm-hint {
  font-size: 12px;
  color: var(--vp-c-text-3);
}

.mm-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px 16px;
  margin-bottom: 14px;
}

.mm-field-wide {
  grid-column: 1 / -1;
}

.mm-field label {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: var(--vp-c-text-2);
  margin-bottom: 5px;
}

.mm-field input,
.mm-field select,
.mm-field textarea {
  width: 100%;
  padding: 6px 10px;
  border-radius: 6px;
  border: 1px solid var(--vp-c-divider);
  background: var(--vp-c-bg);
  font-size: 13px;
  font-family: var(--vp-font-family-mono);
}

.mm-switches {
  grid-column: 1 / -1;
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  padding: 4px 0;
}

.mm-check {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  cursor: pointer;
}

.res-code {
  display: block;
  margin-top: 5px;
  line-height: 1.7;
  font-weight: 400;
  color: var(--vp-c-text-1);
  overflow-wrap: anywhere;
  resize: vertical;
}

.res-editor { display: flex; width: 100%; gap: 8px; }
.res-editor button { flex-shrink: 0; padding: 6px 12px; border: 1px solid var(--vp-c-divider); border-radius: 6px; font-size: 13px; }
.res-editor button:disabled { opacity: 0.5; cursor: default; }
.res-status { font-size: 12px; color: var(--vp-c-text-2); }
</style>
