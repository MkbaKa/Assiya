<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  attrName: { type: String, required: true },
  initialLines: { type: Array, required: true },
  toggleLabel: { type: String, required: true },
  rules: { type: Object, required: true },
  normalOrder: { type: Array, required: true },
  alternateOrder: { type: Array, required: true }
})

const lore = ref(props.initialLines.join('\n'))
const wrongOrder = ref(false)

const order = computed(() =>
  wrongOrder.value ? props.alternateOrder : props.normalOrder
)

function readLine(line) {
  const results = []
  let from = 0
  while (true) {
    const start = line.indexOf(props.attrName, from)
    if (start < 0) break
    const rest = line.slice(start + props.attrName.length)
    let matched = null
    for (const [index, key] of order.value.entries()) {
      const rule = props.rules[key]
      const m = new RegExp(`^${rule.regex}`).exec(rest)
      if (!m) continue
      const values = {}
      rule.captures.forEach((id, i) => (values[id] = Number(m[i + 1])))
      matched = { rule: index + 1, label: rule.label, values, length: m[0].length }
      break
    }
    if (matched) {
      results.push(matched)
      from = start + props.attrName.length + matched.length
    } else {
      from = start + props.attrName.length
    }
  }
  return results
}

const lines = computed(() =>
  lore.value.split('\n').map((text) => ({ text, matches: readLine(text) }))
)

const merged = computed(() => {
  const captures = { value: 0, percent: 0, min: 0, max: 0 }
  for (const line of lines.value) {
    for (const match of line.matches) {
      for (const [id, value] of Object.entries(match.values)) captures[id] += value
    }
  }
  return captures
})

function format(number) {
  return Number.isInteger(number) ? String(number) : number.toFixed(2)
}

const total = computed(() => {
  const { value, percent, min, max } = merged.value
  const rate = percent / 100
  const low = (value + Math.min(min, max)) * (1 + rate)
  const high = (value + Math.max(min, max)) * (1 + rate)
  return low === high ? format(low) : `${format(low)} ~ ${format(high)}`
})

function describe(values) {
  return Object.entries(values)
    .map(([id, value]) => `${id} = ${format(value)}`)
    .join('，')
}
</script>

<template>
  <div class="reader-demo">
    <div class="demo-input">
      <label class="demo-title">物品 Lore</label>
      <textarea v-model="lore" rows="4" spellcheck="false" />
      <label class="demo-toggle">
        <input v-model="wrongOrder" type="checkbox" />
        <span v-html="toggleLabel" />
      </label>
    </div>

    <table>
      <thead>
        <tr><th>Lore</th><th>命中的规则</th><th>读到的值</th></tr>
      </thead>
      <tbody>
        <template v-for="(line, index) in lines" :key="index">
          <tr v-if="line.matches.length === 0">
            <td><code>{{ line.text || '（空行）' }}</code></td>
            <td colspan="2" class="demo-miss">没有识别到 {{ attrName }}</td>
          </tr>
          <tr v-for="(match, i) in line.matches" :key="`${index}-${i}`">
            <td><code>{{ line.text }}</code></td>
            <td>第 {{ match.rule }} 条 <code>{{ match.label }}</code></td>
            <td>{{ describe(match.values) }}</td>
          </tr>
        </template>
      </tbody>
    </table>

    <div class="demo-result">
      <div>
        <span class="demo-title">合并后的捕获组</span>
        <code>value = {{ format(merged.value) }}</code>
        <code>percent = {{ format(merged.percent) }}</code>
        <code>min = {{ format(merged.min) }}</code>
        <code>max = {{ format(merged.max) }}</code>
      </div>
      <div>
        <span class="demo-title">读取 total</span>
        <strong>{{ total }}</strong>
      </div>
    </div>
  </div>
</template>

<style scoped>
.reader-demo {
  margin: 16px 0;
  padding: 16px 20px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 8px;
  background: var(--vp-c-bg-soft);
}

.demo-title {
  display: block;
  margin-bottom: 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--vp-c-text-2);
}

.demo-input textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 6px;
  background: var(--vp-c-bg);
  font-family: var(--vp-font-family-mono);
  font-size: 14px;
  resize: vertical;
}

.demo-toggle {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  font-size: 14px;
  cursor: pointer;
}

.reader-demo table {
  display: table;
  width: 100%;
  margin: 16px 0;
}

.demo-miss {
  color: var(--vp-c-text-3);
}

.demo-result {
  display: flex;
  flex-wrap: wrap;
  gap: 16px 32px;
}

.demo-result code {
  margin-right: 6px;
}

.demo-result strong {
  font-size: 20px;
  color: var(--vp-c-brand-1);
}
</style>
