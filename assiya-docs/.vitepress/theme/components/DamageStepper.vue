<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  hint: { type: String, required: true },
  sideLabels: { type: Object, required: true },
  valuesTitle: { type: String, required: true },
  initialStep: { type: Number, default: 0 },
  steps: { type: Array, required: true }
})

const activeStep = ref(props.initialStep)

const current = computed(() => props.steps[activeStep.value] || props.steps[0] || { buckets: {} })
const total = computed(() => {
  return Object.values(current.value.buckets || {}).reduce((a, b) => a + b, 0)
})
</script>

<template>
  <div v-if="steps.length" class="stepper-card">
    <div class="stepper-header">
      <span class="stepper-hint">{{ hint }}</span>
    </div>

    <div class="stepper-timeline">
      <button
        v-for="(item, idx) in steps"
        :key="idx"
        type="button"
        class="step-pill"
        :class="[
          idx === activeStep ? 'active' : '',
          'side-' + (item.side || 'system')
        ]"
        @click="activeStep = idx"
      >
        <span class="pill-prio">{{ item.priority }}</span>
        <span class="pill-title">{{ item.title }}</span>
      </button>
    </div>

    <div class="stepper-stage">
      <div class="stage-info">
        <div class="stage-title-row">
          <span class="stage-badge" :class="'badge-' + (current.side || 'system')">
            {{ current.badge || sideLabels[current.side || 'system'] }}
          </span>
          <code v-if="current.attr" class="stage-attr">{{ current.attr }}</code>
        </div>
        <p class="stage-desc" v-html="current.desc" />
      </div>

      <div class="stage-buckets">
        <div class="buckets-label">{{ valuesTitle }}</div>
        <div class="buckets-grid">
          <div
            v-for="(val, key) in current.buckets"
            :key="key"
            class="bucket-item"
            :class="{ 'bucket-neg': val < 0, 'bucket-zero': val === 0 }"
          >
            <span class="bucket-name">{{ key }}</span>
            <strong class="bucket-val">{{ val > 0 ? '+' + val : val }}</strong>
          </div>
        </div>
        <div class="buckets-total">
          <span>{{ current.note }}</span>
          <strong class="total-badge">当前合计：{{ total }} 点</strong>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.stepper-card {
  margin: 20px 0;
  padding: 18px 20px;
  border-radius: 12px;
  border: 1px solid var(--vp-c-divider);
  background: var(--vp-c-bg-soft);
}

.stepper-header {
  margin-bottom: 12px;
}

.stepper-hint {
  font-size: 13px;
  font-weight: 600;
  color: var(--vp-c-text-2);
}

.stepper-timeline {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(135px, 1fr));
  gap: 8px;
  margin-bottom: 16px;
}

.step-pill {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 8px 10px;
  border-radius: 8px;
  border: 1px solid var(--vp-c-divider);
  background: var(--vp-c-bg);
  cursor: pointer;
  text-align: left;
  transition: all 0.15s ease;
}

.step-pill:hover {
  border-color: var(--vp-c-brand-1);
}

.step-pill.active {
  border-color: var(--vp-c-brand-1);
  background: var(--vp-c-brand-soft);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.pill-prio {
  font-size: 11px;
  font-family: var(--vp-font-family-mono);
  color: var(--vp-c-text-3);
}

.step-pill.active .pill-prio {
  color: var(--vp-c-brand-1);
  font-weight: 600;
}

.pill-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--vp-c-text-1);
  margin-top: 2px;
}

.stepper-stage {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 18px;
  padding: 16px;
  border-radius: 10px;
  background: var(--vp-c-bg);
  border: 1px solid var(--vp-c-divider);
}

@media (max-width: 720px) {
  .stepper-stage {
    grid-template-columns: 1fr;
  }
}

.stage-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.stage-badge {
  font-size: 12px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 6px;
}

.badge-attack {
  background: rgba(239, 68, 68, 0.15);
  color: #ef4444;
}

.badge-defense {
  background: rgba(59, 130, 246, 0.15);
  color: #3b82f6;
}

.badge-system {
  background: var(--vp-c-default-soft);
  color: var(--vp-c-text-2);
}

.stage-attr {
  font-size: 13px;
}

.stage-desc {
  margin: 0;
  font-size: 14px;
  line-height: 1.65;
  color: var(--vp-c-text-1);
}

.stage-buckets {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 12px 14px;
  border-radius: 8px;
  background: var(--vp-c-bg-soft);
}

.buckets-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--vp-c-text-2);
  margin-bottom: 10px;
}

.buckets-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.bucket-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 6px;
  background: var(--vp-c-bg);
  border: 1px solid var(--vp-c-divider);
  font-family: var(--vp-font-family-mono);
  font-size: 13px;
}

.bucket-val {
  color: #10b981;
}

.bucket-item.bucket-neg .bucket-val {
  color: #f59e0b;
}

.bucket-item.bucket-zero {
  opacity: 0.65;
}

.bucket-item.bucket-zero .bucket-val {
  color: var(--vp-c-text-3);
}

.buckets-total {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 8px;
  padding-top: 10px;
  border-top: 1px dashed var(--vp-c-divider);
  font-size: 13px;
  color: var(--vp-c-text-2);
}

.total-badge {
  font-size: 15px;
  color: var(--vp-c-brand-1);
}
</style>
