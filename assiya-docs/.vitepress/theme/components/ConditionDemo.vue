<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  initialLines: { type: Array, required: true },
  initialLevel: { type: Number, required: true },
  initialSlot: { type: String, required: true },
  initialPerms: { type: Array, required: true },
  availablePerms: { type: Array, required: true },
  levelRange: { type: Object, required: true },
  attributes: { type: Array, required: true },
  conditions: { type: Array, required: true },
  slots: { type: Array, required: true }
})

const NUMBER = '([-+]?\\d+(?:\\.\\d+)?)'
const SEP = '[\\s:：]*'

const playerLevel = ref(props.initialLevel)
const playerSlot = ref(props.initialSlot)
const playerPerms = ref([...props.initialPerms])
const lore = ref(props.initialLines.join('\n'))

const DEFAULT_RULES = [
  { key: 'range', regex: new RegExp(`^${SEP}${NUMBER}\\s*-\\s*${NUMBER}`), parse: (m) => ({ min: Number(m[1]), max: Number(m[2]) }) },
  { key: 'percent', regex: new RegExp(`^${SEP}${NUMBER}%`), parse: (m) => ({ percent: Number(m[1]) }) },
  { key: 'value', regex: new RegExp(`^${SEP}${NUMBER}`), parse: (m) => ({ value: Number(m[1]) }) }
]

const TEXT_RULE = new RegExp(`^${SEP}(.+)`)

const ALL_ENTRIES = computed(() => [
  ...props.attributes.flatMap((a) => a.names.map((name) => ({ kind: 'attribute', name, def: a }))),
  ...props.conditions.flatMap((c) => c.names.map((name) => ({ kind: 'condition', name, def: c })))
])

function matchDefault(rest) {
  for (const rule of DEFAULT_RULES) {
    const m = rule.regex.exec(rest)
    if (m) {
      return { captures: rule.parse(m), length: m[0].length }
    }
  }
  return null
}

function parseLine(line) {
  const candidates = []
  for (const entry of ALL_ENTRIES.value) {
    let idx = 0
    while (true) {
      const pos = line.indexOf(entry.name, idx)
      if (pos < 0) break
      candidates.push({ startIndex: pos, entry })
      idx = pos + 1
    }
  }
  candidates.sort((a, b) => a.startIndex - b.startIndex)

  const matches = []
  let consumed = -1
  let i = 0

  while (i < candidates.length) {
    const start = candidates[i].startIndex
    if (start <= consumed) {
      i++
      continue
    }

    let best = null
    while (i < candidates.length && candidates[i].startIndex === start) {
      const { entry } = candidates[i]
      i++
      const rest = line.slice(start + entry.name.length)
      let res = null
      if (entry.kind === 'attribute' || entry.def.reader === 'default') {
        res = matchDefault(rest)
      } else {
        const m = TEXT_RULE.exec(rest)
        if (m) {
          res = { captures: { value: m[1] }, length: m[0].length }
        }
      }
      if (!res) continue
      const end = start + entry.name.length + res.length - 1
      if (!best || end > best.end) {
        best = {
          kind: entry.kind,
          name: entry.name,
          def: entry.def,
          captures: res.captures,
          start,
          end
        }
      }
    }

    if (best) {
      matches.push(best)
      consumed = best.end
    }
  }

  return matches
}

function evalCondition(cond) {
  const { id } = cond.def
  const c = cond.captures
  if (id === 'level-require') {
    if (c.min != null && c.max != null) {
      const ok = playerLevel.value >= Math.trunc(c.min) && playerLevel.value <= Math.trunc(c.max)
      return {
        passed: ok,
        desc: `等级 ${Math.trunc(c.min)}~${Math.trunc(c.max)}（当前 ${playerLevel.value}）`
      }
    }
    const req = Math.trunc(c.value ?? 0)
    const ok = playerLevel.value >= req
    return {
      passed: ok,
      desc: `等级 ≥ ${req}（当前 ${playerLevel.value}）`
    }
  }
  if (id === 'permission') {
    const raw = String(c.value ?? '')
    const parts = raw.split('/').map((s) => s.trim())
    const ok = parts.some((p) => playerPerms.value.includes(p))
    return {
      passed: ok,
      desc: `拥有权限 [${parts.join(' 或 ')}]`
    }
  }
  if (id === 'equipment-slot') {
    const raw = String(c.value ?? '')
    const curSlot = props.slots.find((s) => s.key === playerSlot.value)
    const slotNames = curSlot?.matches || []
    const parts = raw.split('/').map((s) => s.trim())
    const ok = parts.some((req) => slotNames.includes(req))
    return {
      passed: ok,
      desc: `槽位匹配 [${parts.join(' 或 ')}]`
    }
  }
  return { passed: true, desc: '' }
}

function formatAttrCaptures(captures) {
  const { value = 0, percent = 0, min = 0, max = 0 } = captures
  const parts = []
  if (value) parts.push(`+${value}`)
  if (percent) parts.push(`+${percent}%`)
  if (min || max) parts.push(`+${min}~${max}`)
  return parts.join(' ') || '+0'
}

const parsedLines = computed(() => {
  return lore.value.split('\n').map((text, index) => {
    const matches = parseLine(text)
    const attrs = matches.filter((m) => m.kind === 'attribute')
    const conds = matches.filter((m) => m.kind === 'condition').map((m) => ({
      ...m,
      eval: evalCondition(m)
    }))
    const scope = conds.length === 0 ? 'none' : attrs.length === 0 ? 'item' : 'line'
    const allCondsPassed = conds.every((c) => c.eval.passed)
    return { index, text, attrs, conds, scope, allCondsPassed }
  })
})

const itemLevelBlocked = computed(() => {
  return parsedLines.value.some((l) => l.scope === 'item' && !l.allCondsPassed)
})

const effectiveAttributes = computed(() => {
  if (itemLevelBlocked.value) return []
  const map = new Map()
  for (const line of parsedLines.value) {
    if (line.scope === 'line' && !line.allCondsPassed) continue
    for (const attr of line.attrs) {
      const id = attr.def.id
      if (!map.has(id)) {
        map.set(id, { label: attr.def.label, value: 0, percent: 0, min: 0, max: 0 })
      }
      const cur = map.get(id)
      cur.value += attr.captures.value ?? 0
      cur.percent += attr.captures.percent ?? 0
      cur.min += attr.captures.min ?? 0
      cur.max += attr.captures.max ?? 0
    }
  }
  return Array.from(map.values())
})
</script>

<template>
  <div class="cond-demo">
    <div class="demo-controls">
      <div class="control-item">
        <label class="demo-title">玩家等级：<strong>{{ playerLevel }}</strong> 级</label>
        <input v-model.number="playerLevel" type="range" :min="levelRange.min" :max="levelRange.max" />
      </div>

      <div class="control-item">
        <label class="demo-title">物品所在槽位</label>
        <select v-model="playerSlot">
          <option v-for="s in slots" :key="s.key" :value="s.key">
            {{ s.label }}
          </option>
        </select>
      </div>

      <div class="control-item control-perms">
        <label class="demo-title">玩家拥有的权限 / 职业</label>
        <div class="perm-tags">
          <label v-for="perm in availablePerms" :key="perm" class="perm-tag">
            <input v-model="playerPerms" type="checkbox" :value="perm" />
            <code>{{ perm }}</code>
          </label>
        </div>
      </div>
    </div>

    <div class="demo-input">
      <label class="demo-title">物品 Lore</label>
      <textarea v-model="lore" rows="5" spellcheck="false" />
    </div>

    <table>
      <thead>
        <tr>
          <th>Lore</th>
          <th>识别结果与生效范围</th>
          <th>当前状态</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="line in parsedLines" :key="line.index">
          <td><code>{{ line.text || '（空行）' }}</code></td>
          <td>
            <template v-if="line.attrs.length === 0 && line.conds.length === 0">
              <span class="demo-miss">未识别到属性或条件</span>
            </template>
            <template v-else>
              <div v-if="line.scope === 'item'" class="scope-line">
                <span class="badge badge-item">整件物品条件</span>
                <span v-for="(c, i) in line.conds" :key="i" class="cond-chip">
                  {{ c.eval.desc }}
                </span>
              </div>
              <div v-else-if="line.scope === 'line'" class="scope-line">
                <span class="badge badge-line">单行条件</span>
                <span v-for="(a, i) in line.attrs" :key="'a' + i" class="attr-chip">
                  {{ a.def.label }} {{ formatAttrCaptures(a.captures) }}
                </span>
                <span v-for="(c, i) in line.conds" :key="'c' + i" class="cond-chip">
                  需：{{ c.eval.desc }}
                </span>
              </div>
              <div v-else class="scope-line">
                <span class="badge badge-none">无条件属性</span>
                <span v-for="(a, i) in line.attrs" :key="i" class="attr-chip">
                  {{ a.def.label }} {{ formatAttrCaptures(a.captures) }}
                </span>
              </div>
            </template>
          </td>
          <td>
            <template v-if="line.attrs.length === 0 && line.conds.length === 0">
              <span class="demo-miss">—</span>
            </template>
            <template v-else-if="line.scope === 'item'">
              <span :class="line.allCondsPassed ? 'status-pass' : 'status-fail'">
                {{ line.allCondsPassed ? '✓ 条件通过' : '✗ 未通过（整件物品失效）' }}
              </span>
            </template>
            <template v-else>
              <span v-if="itemLevelBlocked" class="status-fail">
                ✗ 被整件物品条件拦截
              </span>
              <span v-else-if="!line.allCondsPassed" class="status-fail">
                ✗ 本行条件未通过
              </span>
              <span v-else class="status-pass">
                ✓ 属性生效
              </span>
            </template>
          </td>
        </tr>
      </tbody>
    </table>

    <div class="demo-result">
      <span class="demo-title">玩家最终拿到的属性</span>
      <div v-if="effectiveAttributes.length === 0" class="demo-empty">
        {{ itemLevelBlocked ? '无（整件物品条件未满足，所有属性均不生效）' : '无生效属性' }}
      </div>
      <div v-else class="result-list">
        <code v-for="attr in effectiveAttributes" :key="attr.label">
          {{ attr.label }} {{ formatAttrCaptures(attr) }}
        </code>
      </div>
    </div>
  </div>
</template>

<style scoped>
.cond-demo {
  margin: 16px 0;
  padding: 16px 20px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 8px;
  background: var(--vp-c-bg-soft);
}

.demo-controls {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px 20px;
  margin-bottom: 14px;
}

.control-item select,
.control-item input[type="range"] {
  width: 100%;
}

.control-item select {
  padding: 5px 8px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 6px;
  background: var(--vp-c-bg);
  font-size: 13px;
}

.perm-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.perm-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  cursor: pointer;
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

.cond-demo table {
  display: table;
  width: 100%;
  margin: 16px 0;
}

.scope-line {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
}

.badge {
  display: inline-block;
  padding: 1px 6px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.badge-item {
  background: var(--vp-c-warning-soft);
  color: var(--vp-c-warning-1);
}

.badge-line {
  background: var(--vp-c-brand-soft);
  color: var(--vp-c-brand-1);
}

.badge-none {
  background: var(--vp-c-default-soft);
  color: var(--vp-c-text-2);
}

.attr-chip {
  font-weight: 600;
}

.cond-chip {
  font-size: 13px;
  color: var(--vp-c-text-2);
}

.status-pass {
  font-weight: 600;
  color: var(--vp-c-green-1, #10b981);
}

.status-fail {
  font-weight: 600;
  color: var(--vp-c-danger-1, #ef4444);
}

.demo-miss,
.demo-empty {
  color: var(--vp-c-text-3);
  font-size: 14px;
}

.result-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.result-list code {
  font-size: 14px;
  font-weight: 600;
  color: var(--vp-c-brand-1);
}
</style>
