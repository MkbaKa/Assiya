<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  presets: { type: Array, required: true },
  resultLabels: { type: Object, required: true },
  effects: { type: Object, required: true }
})

const presetIndex = ref(0)
const expression = ref(props.presets[0]?.expression || '')
const randomSeed = ref(0)
const variables = ref([])
const effects = ref([])
const selectedExpression = ref('')
const resultLabel = ref('')
const copyStatus = ref('')
let copyAttempt = 0

watch(expression, () => {
  copyAttempt++
  copyStatus.value = ''
})

async function copyExpression() {
  const attempt = ++copyAttempt
  try {
    await navigator.clipboard.writeText(expression.value)
    if (attempt === copyAttempt) copyStatus.value = '已复制'
  } catch {
    if (attempt === copyAttempt) copyStatus.value = '复制失败，请重试'
  }
}

function resetPreset() {
  const p = props.presets[presetIndex.value]
  expression.value = p?.expression || ''
  variables.value = (p?.varMeta || []).map(meta => ({ ...meta, value: p.vars[meta.key] }))
  effects.value = (props.effects[p?.formulaId] || []).map(effect => ({ ...effect }))
  resultLabel.value = props.resultLabels[p?.formulaId] || '计算结果'
  selectedExpression.value = ''
  syncVariables()
}

watch(
  () => [presetIndex.value, props.presets],
  resetPreset,
  { deep: true, immediate: true }
)

const variableValues = computed(() => {
  const values = Object.create(null)
  for (const variable of variables.value) {
    if (!isReferenced(variable.key)) continue
    if (variable.value === '' || !Number.isFinite(Number(variable.value))) throw new Error(`请填写变量 <${variable.key}> 的数值`)
    values[variable.key] = Number(variable.value)
  }
  return values
})

function referencedVariables() {
  const keys = new Set()
  for (const text of [expression.value, ...effects.value.map(effect => effect.expression)]) {
    for (const match of text.matchAll(/<([^<>]+)>/g)) {
      const key = match[1].trim()
      if (key && key !== 'result') keys.add(key)
    }
  }
  return keys
}

function syncVariables() {
  const referenced = referencedVariables()
  variables.value = variables.value.filter(variable => referenced.has(variable.key))
  const existing = new Set(variables.value.map(variable => variable.key))
  for (const key of referenced) {
    if (!existing.has(key)) variables.value.push({ key, label: key, value: 0, min: 0, max: 100, step: 1 })
  }
}

function isReferenced(key) {
  return referencedVariables().has(key)
}

watch(() => [expression.value, ...effects.value.map(effect => effect.expression)], syncVariables)

function captureSelection(event) {
  const input = event.target
  selectedExpression.value = input.value.slice(input.selectionStart, input.selectionEnd)
}

function addEffect() {
  effects.value.push({ label: '', expression: selectedExpression.value, format: 'number' })
}

function evaluate(expr, values) {
  const value = compileAndEval(expr, values)
  if (!Number.isFinite(value)) throw new Error('计算结果不是有限数值，请检查除数和变量范围。')
  return value
}

const OPERATORS = {
  '+': { priority: 1, rightAssociative: false, unary: false, apply: (l, r) => l + r },
  '-': { priority: 1, rightAssociative: false, unary: false, apply: (l, r) => l - r },
  '*': { priority: 2, rightAssociative: false, unary: false, apply: (l, r) => l * r },
  '/': { priority: 2, rightAssociative: false, unary: false, apply: (l, r) => l / r },
  '%': { priority: 2, rightAssociative: false, unary: false, apply: (l, r) => l % r },
  '^': { priority: 3, rightAssociative: true, unary: false, apply: (l, r) => Math.pow(l, r) },
  'neg': { priority: 4, rightAssociative: true, unary: true, apply: (_, r) => -r }
}

const FUNCTIONS = {
  random: { params: 2, apply: ([from, to]) => from + (to - from) * Math.random() },
  min: { params: 2, apply: ([a, b]) => Math.min(a, b) },
  max: { params: 2, apply: ([a, b]) => Math.max(a, b) }
}

function isLetterOrUnderscore(ch) {
  return /^[a-zA-Z_]$/.test(ch)
}

function compileAndEval(expr, variables) {
  const tokens = []
  let index = 0

  while (index < expr.length) {
    const ch = expr[index]
    if (/\s/.test(ch)) {
      index++
      continue
    }
    if (ch === '<') {
      const end = expr.indexOf('>', index + 1)
      if (end < 0) throw new Error('变量缺少右尖括号 >')
      const name = expr.slice(index + 1, end).trim()
      if (!name) throw new Error('变量名不能为空 <>')
      tokens.push({ type: 'var', name })
      index = end + 1
      continue
    }
    if ((ch >= '0' && ch <= '9') || ch === '.') {
      const start = index
      while (index < expr.length && ((expr[index] >= '0' && expr[index] <= '9') || expr[index] === '.')) {
        index++
      }
      const raw = expr.slice(start, index)
      if (!/^\d+(\.\d+)?$|^\.\d+$/.test(raw)) {
        throw new Error(`无效的数字: ${raw}`)
      }
      tokens.push({ type: 'lit', value: Number(raw) })
      continue
    }
    if (isLetterOrUnderscore(ch)) {
      const start = index
      while (index < expr.length && isLetterOrUnderscore(expr[index])) {
        index++
      }
      const name = expr.slice(start, index)
      if (index < expr.length && expr[index] === '(') {
        const fn = FUNCTIONS[name]
        if (!fn) throw new Error(`未知的函数: ${name}（仅支持 max、min、random）`)
        tokens.push({ type: 'fn', id: name, fn })
      } else {
        tokens.push({ type: 'var', name })
      }
      continue
    }
    if (ch === ',') {
      tokens.push({ type: 'comma' })
      index++
      continue
    }
    if (ch === '(') {
      tokens.push({ type: 'lparen' })
      index++
      continue
    }
    if (ch === ')') {
      tokens.push({ type: 'rparen' })
      index++
      continue
    }
    const prev = tokens[tokens.length - 1]
    const unaryPos = !prev || prev.type === 'op' || prev.type === 'lparen' || prev.type === 'comma'
    if (unaryPos && (ch === '+' || ch === '-')) {
      if (ch === '-') {
        tokens.push({ type: 'op', op: OPERATORS.neg })
      }
      index++
      continue
    }
    const binOp = OPERATORS[ch]
    if (!binOp || ch === 'neg') {
      throw new Error(`不支持的运算符: ${ch}`)
    }
    tokens.push({ type: 'op', op: binOp })
    index++
  }

  const output = []
  const opStack = []

  for (const token of tokens) {
    if (token.type === 'op') {
      const operator = token.op
      while (opStack.length > 0) {
        const top = opStack[opStack.length - 1]
        if (top.type !== 'op') break
        const pops = top.op.priority > operator.priority ||
          (top.op.priority === operator.priority && !operator.rightAssociative)
        if (!pops) break
        output.push(opStack.pop())
      }
      opStack.push(token)
    } else if (token.type === 'fn') {
      opStack.push(token)
    } else if (token.type === 'lit' || token.type === 'var') {
      output.push(token)
    } else if (token.type === 'lparen') {
      opStack.push(token)
    } else if (token.type === 'comma') {
      while (true) {
        const top = opStack[opStack.length - 1]
        if (!top) throw new Error('逗号位置错误')
        if (top.type === 'lparen') break
        output.push(opStack.pop())
      }
    } else if (token.type === 'rparen') {
      while (true) {
        const top = opStack.pop()
        if (!top) throw new Error('括号不匹配')
        if (top.type === 'lparen') break
        output.push(top)
      }
      const nextTop = opStack[opStack.length - 1]
      if (nextTop && nextTop.type === 'fn') {
        output.push(opStack.pop())
      }
    }
  }

  while (opStack.length > 0) {
    const top = opStack.pop()
    if (top.type !== 'op') throw new Error('括号不匹配')
    output.push(top)
  }

  let operands = 0
  for (const token of output) {
    if (token.type === 'op') {
      const req = token.op.unary ? 1 : 2
      if (operands < req) throw new Error('算式格式不完整')
      operands -= req - 1
    } else if (token.type === 'fn') {
      const params = token.fn.params
      if (operands < params) throw new Error(`函数 ${token.id} 需要 ${params} 个参数`)
      operands -= params - 1
    } else {
      operands++
    }
  }
  if (operands !== 1) throw new Error('算式格式不正确')

  const stack = []
  for (const token of output) {
    if (token.type === 'lit') {
      stack.push(token.value)
    } else if (token.type === 'var') {
      if (!Object.hasOwn(variables, token.name)) {
        throw new Error(`当前未提供变量 <${token.name}>（可用变量: ${Object.keys(variables).map(k => '<' + k + '>').join('、')}）`)
      }
      stack.push(Number(variables[token.name]))
    } else if (token.type === 'op') {
      const right = stack.pop()
      const left = token.op.unary ? 0 : stack.pop()
      stack.push(token.op.apply(left, right))
    } else if (token.type === 'fn') {
      const params = token.fn.params
      const args = new Array(params)
      for (let i = params - 1; i >= 0; i--) {
        args[i] = stack.pop()
      }
      stack.push(token.fn.apply(args))
    }
  }
  return stack[stack.length - 1]
}

function fmt(num) {
  if (!Number.isFinite(num)) return String(num)
  return Number.isInteger(num) ? String(num) : num.toFixed(2)
}

const evaluation = computed(() => {
  void randomSeed.value
  try {
    const val = evaluate(expression.value, variableValues.value)
    return { ok: true, value: val }
  } catch (e) {
    return { ok: false, error: e.message }
  }
})

const hasRandom = computed(() => /random\s*\(/.test(expression.value) || effects.value.some(effect => /random\s*\(/.test(effect.expression)))

const effectResults = computed(() => effects.value.map(effect => {
  void randomSeed.value
  try {
    const values = { ...variableValues.value }
    if (evaluation.value.ok) values.result = evaluation.value.value
    const value = evaluate(effect.expression, values)
    return { text: effect.format === 'percent' ? `${fmt(value * 100)}%` : fmt(value) }
  } catch (error) {
    return { error: error.message }
  }
}))
</script>

<template>
  <div class="formula-demo">
    <div class="demo-row">
      <div class="demo-field">
        <label class="demo-title">选择预设示例</label>
        <select v-model.number="presetIndex">
          <option v-for="(item, idx) in presets" :key="idx" :value="idx">
            {{ item.label }}
          </option>
          <option :value="-1">自定义</option>
        </select>
      </div>

      <div class="demo-field demo-expr">
        <label class="demo-title">
          公式算式
        </label>
        <div class="expr-input-wrap">
          <input v-model="expression" type="text" spellcheck="false" @select="captureSelection" @keyup="captureSelection" @mouseup="captureSelection" />
          <button type="button" class="reroll-btn" :disabled="!expression.trim()" @click="copyExpression">复制</button>
          <button v-if="hasRandom" type="button" class="reroll-btn" @click="randomSeed++">
            重新随机
          </button>
        </div>
        <span v-if="copyStatus" class="copy-status" role="status">{{ copyStatus }}</span>
      </div>
    </div>

    <div class="demo-vars">
      <div v-for="meta in variables" :key="meta.key" class="var-item">
        <label class="var-label">
          <span class="demo-title">{{ meta.label }}</span>
          <input
            v-model.number="meta.value"
            type="number"
            step="any"
          />
        </label>
        <input
          v-model.number="meta.value"
          type="range"
          :aria-label="meta.label"
          :min="meta.min"
          :max="meta.max"
          :step="meta.step"
        />
      </div>
    </div>

    <div class="demo-result">
      <template v-if="evaluation.ok">
        <div>
          <label class="demo-title">结果<input v-model="resultLabel" class="result-label-input" aria-label="结果" /></label>
          <strong class="result-num">{{ fmt(evaluation.value) }}</strong>
        </div>
      </template>
      <template v-else>
        <div class="result-error">
          <span class="demo-title">公式无法计算</span>
          <code>{{ evaluation.error }}</code>
        </div>
      </template>
    </div>
    <div v-if="effects.length" class="effect-list">
      <div v-for="(effect, index) in effects" :key="index" class="effect-item">
        <label>效果<input v-model="effect.label" placeholder="例如剩余伤害" /></label>
        <label class="effect-expression">表达式<input v-model="effect.expression" placeholder="例如 <damage> - <result>" spellcheck="false" /></label>
        <label>显示格式<select v-model="effect.format"><option value="number">数值</option><option value="percent">百分比</option></select></label>
        <button type="button" class="text-button" @click="effects.splice(index, 1)">删除</button>
        <div class="effect-output" aria-live="polite">
          <span class="result-error" v-if="effectResults[index].error">{{ effectResults[index].error }}</span>
          <template v-else><span>{{ effect.label || '未命名效果' }}</span><strong class="result-num">{{ effectResults[index].text }}</strong></template>
        </div>
      </div>
      <p class="effect-help">效果表达式可以引用修改项，用 &lt;result&gt; 引用主公式结果。百分比格式将结果乘以 100 后显示。</p>
    </div>
    <div class="calculator-actions">
      <button type="button" @click="addEffect">{{ selectedExpression ? '将选中片段添加为效果' : '添加效果' }}</button>
      <button type="button" @click="resetPreset">重置当前预设</button>
    </div>
  </div>
</template>

<style scoped>
.formula-demo {
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

.demo-row {
  display: grid;
  grid-template-columns: minmax(200px, 1fr) 2fr;
  gap: 12px 16px;
  margin-bottom: 14px;
}

@media (max-width: 640px) {
  .demo-row {
    grid-template-columns: 1fr;
  }
}

.demo-field select,
.demo-field input[type="text"] {
  width: 100%;
  padding: 6px 10px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 6px;
  background: var(--vp-c-bg);
  font-size: 13px;
}

.demo-field input[type="text"] {
  font-family: var(--vp-font-family-mono);
}

.expr-input-wrap {
  display: flex;
  gap: 8px;
}

.expr-input-wrap input {
  min-width: 0;
  flex: 1;
}

.copy-status {
  display: block;
  margin-top: 5px;
  font-size: 12px;
  color: var(--vp-c-text-2);
}

.reroll-btn:disabled {
  opacity: 0.5;
  cursor: default;
}

.reroll-btn {
  flex-shrink: 0;
  padding: 0 12px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 6px;
  background: var(--vp-c-bg);
  font-size: 12px;
  cursor: pointer;
}

.reroll-btn:hover {
  border-color: var(--vp-c-brand-1);
  color: var(--vp-c-brand-1);
}

.demo-vars {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px 20px;
  margin-bottom: 16px;
}

.var-item input[type="range"] {
  width: 100%;
}

.var-label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 8px;
}

.var-label .demo-title {
  margin-bottom: 0;
}

.var-label input[type="number"] {
  width: 84px;
  flex-shrink: 0;
  padding: 4px 8px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 6px;
  background: var(--vp-c-bg);
  color: var(--vp-c-text-1);
  font-size: 13px;
  font-family: var(--vp-font-family-mono);
}

.demo-result {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  gap: 16px 32px;
  padding-top: 12px;
  border-top: 1px solid var(--vp-c-divider);
}

.demo-result .demo-title {
  line-height: 24px;
}

.result-num {
  font-size: 20px;
  color: var(--vp-c-brand-1);
}

.result-desc {
  font-size: 14px;
  color: var(--vp-c-text-1);
}

.result-error code {
  color: var(--vp-c-danger-1, #ef4444);
}

.calculator-actions { display: flex; flex-wrap: wrap; align-items: end; gap: 10px; margin: 16px 0; }
.calculator-actions button { padding: 5px 10px; border: 1px solid var(--vp-c-divider); border-radius: 6px; background: var(--vp-c-bg); font-size: 13px; }
.effect-item label { display: flex; flex-direction: column; gap: 5px; font-size: 12px; color: var(--vp-c-text-2); min-width: 0; }
.effect-item input, .effect-item select, .result-label-input { width: 100%; padding: 6px 8px; border: 1px solid var(--vp-c-divider); border-radius: 6px; background: var(--vp-c-bg); color: var(--vp-c-text-1); font-size: 13px; }
.result-label-input { display: block; margin-top: 5px; }
.text-button { font-size: 12px; color: var(--vp-c-text-2); text-decoration: underline; }
.effect-list { display: grid; gap: 12px; margin-top: 18px; }
.effect-item { display: grid; grid-template-columns: minmax(100px, 1fr) minmax(140px, 2fr) 90px auto; align-items: end; gap: 10px; padding: 14px; border: 1px solid var(--vp-c-divider); border-radius: 8px; }
.effect-output { grid-column: 1 / -1; display: flex; align-items: baseline; gap: 14px; overflow-wrap: anywhere; }
.result-error { color: var(--vp-c-danger-1); font-size: 13px; }
.effect-help { margin: 0; font-size: 12px; color: var(--vp-c-text-2); }
@media (max-width: 640px) { .effect-item { grid-template-columns: minmax(0, 1fr); } }
</style>
