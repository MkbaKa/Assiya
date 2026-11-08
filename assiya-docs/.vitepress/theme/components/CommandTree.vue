<script setup>
import { computed, reactive, ref, watch } from 'vue'
import CommandTreeBranch from './CommandTreeBranch.vue'

const props = defineProps({
  root: { type: String, required: true },
  commands: { type: Array, required: true },
  separator: { type: String, required: true },
  suffix: { type: String, required: true },
  treeTitle: { type: String, required: true },
  exampleTitle: { type: String, required: true },
  paramsTitle: { type: String, required: true },
  copyLabel: { type: String, required: true }
})

const selected = ref(0)
const forms = reactive([])
const copied = ref(false)
const copyError = ref(false)
let copyAttempt = 0

watch(() => props.commands, commands => {
  forms.splice(0, forms.length, ...commands.map(command => Object.fromEntries(
    command.params.map(param => [param.key, param.initial ?? param.default ?? ''])
  )))
  selected.value = 0
}, { immediate: true })

const current = computed(() => props.commands[selected.value])
const tree = computed(() => {
  const roots = []
  props.commands.forEach((command, index) => {
    let siblings = roots
    command.path.forEach((segment, depth) => {
      let node = siblings.find(item => item.segment === segment)
      if (!node) {
        node = { segment, path: command.path.slice(0, depth + 1).join(' '), children: [] }
        siblings.push(node)
      }
      if (depth === command.path.length - 1) {
        node.index = index
        node.label = command.label
      }
      siblings = node.children
    })
  })
  return roots
})

function valueOf(param) {
  const value = forms[selected.value][param.key]
  if (value === '' || value === null) return param.required ? '<' + param.name + '>' : param.default
  return typeof value === 'string' ? value.trim() || (param.required ? '<' + param.name + '>' : param.default) : value
}

const generatedCommand = computed(() => {
  if (!current.value) return props.root
  const params = current.value.params
  let last = params.length - 1
  while (last >= 0 && !params[last].required && String(valueOf(params[last])) === String(params[last].default)) last--
  return [props.root, ...current.value.path, ...params.slice(0, last + 1).map(valueOf)].join(props.separator) + props.suffix
})

watch(generatedCommand, () => {
  copyAttempt++
  copied.value = false
  copyError.value = false
})

async function copyCommand() {
  const attempt = ++copyAttempt
  try {
    await navigator.clipboard.writeText(generatedCommand.value)
    if (attempt !== copyAttempt) return
    copied.value = true
    copyError.value = false
    setTimeout(() => { if (attempt === copyAttempt) copied.value = false }, 1500)
  } catch {
    if (attempt === copyAttempt) copyError.value = true
  }
}
</script>

<template>
  <div v-if="current" class="cmd-explorer">
    <aside class="cmd-tree-pane">
      <div class="tree-pane-header">
        <span class="pane-title">{{ treeTitle }}</span>
      </div>
      <div class="root-node"><code class="root-code">{{ root }}</code></div>
      <ul class="tree-list">
        <CommandTreeBranch v-for="node in tree" :key="node.path" :node="node" :selected="selected" @select="selected = $event" />
      </ul>
    </aside>

    <section class="cmd-detail-pane">
      <div class="detail-top">
        <div class="syntax-tokens">
          <span class="tok tok-lit">{{ [root, ...current.path].join(separator) }}</span>
          <span v-for="param in current.params" :key="param.key" class="tok" :class="param.required ? 'tok-req' : 'tok-opt'">{{ param.required ? '<' + param.name + '>' : '[' + param.name + ']' }}</span>
          <span v-if="suffix" class="tok tok-lit">{{ suffix }}</span>
        </div>
        <span v-if="current.permission" class="perm-chip" title="命令执行权限节点">权限 <code>{{ current.permission }}</code></span>
      </div>
      <p class="detail-desc" v-html="current.description" />

      <div class="builder-card">
        <div class="builder-head"><span class="builder-title">{{ exampleTitle }}</span></div>
        <div class="builder-grid">
          <div v-for="param in current.params" :key="param.key" class="field" :class="{ 'field-wide': param.presets || param.input === 'select', 'field-switch': param.input === 'boolean' }">
            <label v-if="param.input === 'boolean'" class="switch-label"><input v-model="forms[selected][param.key]" type="checkbox" /><span>{{ param.name }}</span></label>
            <label v-else>
              {{ param.name }}
              <select v-if="param.input === 'select'" v-model="forms[selected][param.key]">
                <option v-for="option in param.options" :key="option.value" :value="option.value">{{ option.label }}</option>
              </select>
              <input v-else-if="param.input === 'number'" v-model.number="forms[selected][param.key]" type="number" :placeholder="String(param.initial ?? param.default ?? '')" />
              <input v-else v-model="forms[selected][param.key]" type="text" :placeholder="param.placeholder || param.name" />
            </label>
            <div v-if="param.presets" class="preset-chips">
              <button v-for="preset in param.presets" :key="preset.value" type="button" class="preset-btn" :class="{ active: forms[selected][param.key] === preset.value }" @click="forms[selected][param.key] = preset.value">{{ preset.label }}</button>
            </div>
          </div>
        </div>
        <div class="cmd-output"><code class="cmd-output-code">{{ generatedCommand }}</code><button type="button" class="copy-btn" @click="copyCommand">{{ copied ? '已复制' : copyLabel }}</button></div>
        <p v-if="copyError" role="status">复制失败，请手动选择并复制上方命令。</p>
      </div>

      <div v-if="current.params.length" class="param-section">
        <div class="param-section-title"><span>{{ paramsTitle }}</span><span class="token-legend"><i class="dot dot-req" />必填<i class="dot dot-opt" />可选</span></div>
        <div class="param-rows">
          <div v-for="param in current.params" :key="param.key" class="param-item">
            <div class="param-item-head"><code class="param-name">{{ param.name }}</code><div class="param-badges"><span v-if="param.type" class="p-badge p-type">{{ param.type }}</span><span v-if="param.required" class="p-badge p-req">必填</span><span v-else class="p-badge p-def">默认 <code>{{ param.default }}</code></span></div></div>
            <div class="param-item-desc" v-html="param.description" />
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.cmd-explorer {
  display: grid;
  grid-template-columns: 235px minmax(0, 1fr);
  gap: 16px;
  margin: 18px 0 24px;
}

@media (max-width: 768px) {
  .cmd-explorer {
    grid-template-columns: 1fr;
  }
}

.cmd-tree-pane {
  align-self: start;
  padding: 16px 14px;
  background: var(--vp-c-bg-alt);
  border: 1px solid var(--vp-c-divider);
  border-radius: 12px;
}

.tree-pane-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
  padding: 0 4px;
}

.pane-title {
  font-size: 12.5px;
  font-weight: 700;
  color: var(--vp-c-text-2);
}

.root-node {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  border-radius: 8px;
  background: var(--vp-c-bg);
  border: 1px solid var(--vp-c-divider);
}

.root-code {
  font-size: 14px;
  font-weight: 700;
  color: var(--vp-c-brand-1);
  background: transparent;
  padding: 0;
}

.tree-list,
.tree-sublist {
  list-style: none !important;
  margin: 0 !important;
  padding: 0 !important;
}

.tree-list {
  margin-left: 14px !important;
  padding-top: 6px !important;
}

.tree-sublist {
  margin-left: 14px !important;
  padding-top: 4px !important;
}

.tree-branch,
.tree-subbranch {
  position: relative;
  padding-left: 16px;
  margin: 0 !important;
  padding-top: 4px;
  padding-bottom: 4px;
}

.tree-branch::before,
.tree-subbranch::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 1px;
  background: var(--vp-c-divider);
}

.tree-branch:last-child::before,
.tree-subbranch:last-child::before {
  height: 18px;
  bottom: auto;
}

.tree-branch::after,
.tree-subbranch::after {
  content: '';
  position: absolute;
  left: 0;
  top: 18px;
  width: 12px;
  height: 1px;
  background: var(--vp-c-divider);
}

.group-label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 5px 10px;
  border-radius: 7px;
  color: var(--vp-c-text-2);
  font-size: 13px;
}

.group-cmd {
  font-family: var(--vp-font-family-mono);
  font-weight: 700;
  color: var(--vp-c-text-1);
}

.group-sub {
  font-size: 11.5px;
  color: var(--vp-c-text-3);
}

.tree-leaf {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
  padding: 6px 10px;
  border-radius: 7px;
  border: 1px solid transparent;
  background: transparent;
  cursor: pointer;
  text-align: left;
  transition: all 0.15s ease;
}

.tree-leaf:hover {
  background: var(--vp-c-bg);
  border-color: var(--vp-c-divider);
}

.tree-leaf.active {
  background: var(--vp-c-brand-soft);
  border-color: var(--vp-c-brand-1);
}

.leaf-cmd {
  font-family: var(--vp-font-family-mono);
  font-size: 13px;
  font-weight: 700;
  color: var(--vp-c-text-1);
}

.tree-leaf.active .leaf-cmd {
  color: var(--vp-c-brand-1);
}

.leaf-tag {
  font-size: 11.5px;
  color: var(--vp-c-text-3);
  white-space: nowrap;
}

.tree-leaf.active .leaf-tag {
  color: var(--vp-c-brand-1);
  font-weight: 600;
}

.cmd-detail-pane {
  min-width: 0;
  border: 1px solid var(--vp-c-divider);
  border-radius: 12px;
  padding: 18px 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  background: var(--vp-c-bg);
}

.detail-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 8px 12px;
}

.syntax-tokens {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
}

.tok {
  font-family: var(--vp-font-family-mono);
  font-size: 13px;
  font-weight: 700;
  padding: 3px 9px;
  border-radius: 6px;
}

.tok-lit {
  background: var(--vp-c-brand-soft);
  color: var(--vp-c-brand-1);
}

.tok-req {
  background: rgba(239, 68, 68, 0.12);
  color: #ef4444;
}

.tok-opt {
  background: rgba(16, 185, 129, 0.12);
  color: var(--vp-c-green-1, #10b981);
}

.perm-chip {
  font-size: 11.5px;
  color: var(--vp-c-text-3);
  background: var(--vp-c-bg-soft);
  border: 1px solid var(--vp-c-divider);
  padding: 2px 8px;
  border-radius: 999px;
}

.perm-chip code {
  font-size: 11.5px;
  background: transparent;
  padding: 0;
  color: var(--vp-c-text-2);
}

.detail-desc {
  margin: 0 !important;
  font-size: 14px;
  line-height: 1.65;
  color: var(--vp-c-text-1);
}

.detail-desc :deep(code) {
  font-size: 12.5px;
  padding: 1px 5px;
  border-radius: 4px;
  background: var(--vp-c-default-soft);
}

.builder-card {
  padding: 14px 16px;
  border-radius: 10px;
  border: 1px solid var(--vp-c-divider);
  background: var(--vp-c-bg-soft);
}

.builder-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.builder-title {
  font-size: 12.5px;
  font-weight: 700;
  color: var(--vp-c-text-2);
}

.token-legend {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 11.5px;
  color: var(--vp-c-text-3);
}

.dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-left: 6px;
}

.dot-req {
  background: #ef4444;
}

.dot-opt {
  background: #10b981;
}

.builder-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(170px, 1fr));
  gap: 10px 14px;
  margin-bottom: 12px;
}

.field-wide,
.field-switch {
  grid-column: 1 / -1;
}

.field label:not(.switch-label) {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: var(--vp-c-text-2);
  margin-bottom: 5px;
}

.field input:not([type="checkbox"]),
.field select {
  width: 100%;
  padding: 6px 10px;
  border-radius: 6px;
  border: 1px solid var(--vp-c-divider);
  background: var(--vp-c-bg);
  font-size: 13px;
  font-family: var(--vp-font-family-mono);
  color: var(--vp-c-text-1);
}

.preset-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}

.preset-btn {
  font-size: 11.5px;
  padding: 2px 8px;
  border-radius: 6px;
  border: 1px solid var(--vp-c-divider);
  background: var(--vp-c-bg);
  color: var(--vp-c-text-2);
  cursor: pointer;
  transition: all 0.15s;
}

.preset-btn:hover,
.preset-btn.active {
  border-color: var(--vp-c-brand-1);
  color: var(--vp-c-brand-1);
  background: var(--vp-c-brand-soft);
}

.switch-label {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--vp-c-text-2);
  cursor: pointer;
}

.switch-label input[type="checkbox"] {
  width: 16px;
  height: 16px;
  margin: 0;
  flex-shrink: 0;
  accent-color: var(--vp-c-brand-1);
}

.switch-label code {
  font-size: 12px;
  padding: 1px 5px;
  border-radius: 4px;
  background: var(--vp-c-default-soft);
}

.cmd-output {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  background: var(--vp-c-bg);
  border: 1px solid var(--vp-c-divider);
}

.cmd-output-code {
  font-size: 13.5px;
  font-weight: 700;
  color: var(--vp-c-brand-1);
  background: transparent;
  padding: 0;
  word-break: break-all;
}

.copy-btn {
  flex-shrink: 0;
  font-size: 12px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 6px;
  border: 1px solid var(--vp-c-divider);
  background: var(--vp-c-bg-soft);
  color: var(--vp-c-text-2);
  cursor: pointer;
  transition: all 0.15s;
}

.copy-btn:hover {
  border-color: var(--vp-c-brand-1);
  color: var(--vp-c-brand-1);
}

.param-section {
  border: 1px solid var(--vp-c-divider);
  border-radius: 10px;
  overflow: hidden;
}

.param-section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 8px;
  padding: 8px 14px;
  font-size: 12px;
  font-weight: 700;
  color: var(--vp-c-text-2);
  background: var(--vp-c-bg-alt);
  border-bottom: 1px solid var(--vp-c-divider);
}

.param-rows {
  display: flex;
  flex-direction: column;
}

.param-item {
  padding: 10px 14px;
  border-bottom: 1px solid var(--vp-c-divider);
}

.param-item:last-child {
  border-bottom: none;
}

.param-item-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 4px;
}

.param-name {
  font-size: 13px;
  font-weight: 700;
  color: var(--vp-c-brand-1);
  background: var(--vp-c-brand-soft);
  padding: 1px 7px;
  border-radius: 5px;
}

.param-badges {
  display: flex;
  align-items: center;
  gap: 6px;
}

.p-badge {
  font-size: 11px;
  font-weight: 600;
  padding: 1px 7px;
  border-radius: 999px;
}

.p-type {
  font-family: var(--vp-font-family-mono);
  background: var(--vp-c-default-soft);
  color: var(--vp-c-text-2);
}

.p-req {
  background: rgba(239, 68, 68, 0.14);
  color: #ef4444;
}

.p-def {
  background: rgba(16, 185, 129, 0.12);
  color: var(--vp-c-text-2);
}

.p-def code {
  font-size: 11px;
  background: transparent;
  padding: 0;
  color: var(--vp-c-green-1, #10b981);
  font-weight: 700;
}

.param-item-desc {
  font-size: 13px;
  line-height: 1.6;
  color: var(--vp-c-text-2);
}

.param-item-desc :deep(code) {
  font-size: 12px;
  padding: 1px 5px;
  border-radius: 4px;
  background: var(--vp-c-default-soft);
}
</style>
