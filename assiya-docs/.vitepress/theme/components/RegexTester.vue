<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  initialPattern: { type: String, required: true },
  initialName: { type: String, required: true },
  initialLines: { type: Array, required: true },
  captures: { type: Object, required: true }
})

const pattern = ref(props.initialPattern)
const name = ref(props.initialName)
const text = ref(props.initialLines.join('\n'))
const escape = (value) => value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')

const compiled = computed(() => {
  try {
    if (!name.value) throw new Error('请填写读取名。')
    const ids = []
    const source = pattern.value.replaceAll('{name}', `(?:${escape(name.value)})`)
      .replace(/<([A-Za-z][A-Za-z0-9]*)>/g, (token, id, offset, input) => {
        // A named regex group is syntax, rather than a capture placeholder.
        if (input.slice(Math.max(0, offset - 2), offset) === '(?') return token
        if (!Object.hasOwn(props.captures, id)) throw new Error(`未声明捕获组：${id}`)
        if (ids.includes(id)) throw new Error(`捕获组不能重复：${id}`)
        ids.push(id)
        return `(?<${id}>${props.captures[id]})`
      })
    return { source, ids, regex: new RegExp(source, 'g'), error: '' }
  } catch (error) {
    return { error: error.message }
  }
})

const results = computed(() => {
  if (compiled.value.error) return []
  const { source, ids } = compiled.value
  return text.value.split('\n').map((line) => {
    const matches = []
    const regex = new RegExp(source, 'g')
    const started = performance.now()
    for (const match of line.matchAll(regex)) {
      if (!match[0] || ids.some((id) => match.groups?.[id] == null)) continue
      matches.push({ start: match.index, text: match[0], values: ids.map((id) => [id, match.groups[id]]) })
    }
    const elapsed = performance.now() - started
    const parts = []
    let cursor = 0
    for (const match of matches) {
      parts.push({ text: line.slice(cursor, match.start), hit: false })
      parts.push({ text: match.text, hit: true })
      cursor = match.start + match.text.length
    }
    parts.push({ text: line.slice(cursor), hit: false })
    return { line, matches, parts, elapsed }
  })
})

function formatDuration(elapsed) {
  return elapsed < 0.01 ? '< 0.01 ms' : `${elapsed.toFixed(2)} ms`
}
</script>

<template>
  <div class="regex-tester">
    <label class="regex-field">
      <span>读取格式</span>
      <input v-model="pattern" spellcheck="false" />
    </label>
    <label class="regex-field">
      <span>读取名</span>
      <input v-model="name" spellcheck="false" />
    </label>
    <label class="regex-field">
      <span>测试文本</span>
      <textarea v-model="text" rows="4" spellcheck="false" />
    </label>
    <p v-if="compiled.error" class="regex-error" role="alert">{{ compiled.error }}</p>
    <template v-else>
      <div class="regex-results" aria-live="polite">
        <div v-for="(result, index) in results" :key="index" class="regex-row">
          <span class="regex-duration">匹配耗时 {{ formatDuration(result.elapsed) }}</span>
          <div class="regex-content">
          <div class="regex-line"><template v-for="(part, i) in result.parts" :key="i"><mark v-if="part.hit">{{ part.text }}</mark><span v-else>{{ part.text }}</span></template><span v-if="!result.line">（空行）</span></div>
          <span v-if="!result.matches.length" class="regex-miss">未匹配</span>
          <div v-for="(match, i) in result.matches" :key="i" class="regex-values">
            <span v-if="!match.values.length">已匹配</span>
            <code v-for="[id, value] in match.values" :key="id">{{ id }} = {{ value }}</code>
          </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.regex-tester { margin: 16px 0; padding: 20px; border: 1px solid var(--vp-c-divider); border-radius: 8px; background: var(--vp-c-bg-soft); }
.regex-field { display: block; margin-bottom: 14px; }
.regex-field > span { display: block; margin-bottom: 6px; font-size: 13px; font-weight: 600; color: var(--vp-c-text-2); }
.regex-field input, .regex-field textarea { width: 100%; padding: 8px 12px; border: 1px solid var(--vp-c-divider); border-radius: 6px; background: var(--vp-c-bg); color: var(--vp-c-text-1); font-family: var(--vp-font-family-mono); font-size: 14px; }
.regex-field textarea { resize: vertical; }
.regex-field input:focus, .regex-field textarea:focus { outline: 2px solid var(--vp-c-brand-1); outline-offset: 1px; }
.regex-results { margin-top: 14px; }
.regex-row { display: grid; grid-template-columns: minmax(0, 1fr) auto; column-gap: 16px; padding: 10px 0; border-top: 1px solid var(--vp-c-divider); }
.regex-content { grid-column: 1; grid-row: 1; min-width: 0; }
.regex-duration { grid-column: 2; grid-row: 1; align-self: center; color: var(--vp-c-text-3); font-size: 12px; white-space: nowrap; font-variant-numeric: tabular-nums; }
.regex-line { white-space: pre-wrap; overflow-wrap: anywhere; font-family: var(--vp-font-family-mono); font-size: 14px; }
mark { background: var(--vp-c-brand-soft); color: var(--vp-c-brand-1); border-radius: 3px; }
.regex-values { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 6px; }
.regex-miss { font-size: 13px; color: var(--vp-c-text-3); }
.regex-error { color: var(--vp-c-danger-1); overflow-wrap: anywhere; }
@media (max-width: 480px) {
  .regex-row { grid-template-columns: minmax(0, 1fr); }
  .regex-duration { grid-column: 1; grid-row: auto; justify-self: end; margin-bottom: 4px; }
  .regex-content { grid-row: auto; }
}
</style>
