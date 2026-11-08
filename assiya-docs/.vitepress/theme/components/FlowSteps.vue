<script setup>
defineProps({
  steps: { type: Array, default: () => [] },
  columns: { type: Number, default: 2 }
})
</script>

<template>
  <div class="vp-flow-steps" :class="'cols-' + columns">
    <div v-for="(step, idx) in steps" :key="idx" class="flow-card">
      <div class="flow-card-head">
        <span class="flow-num">{{ step.tag || (idx + 1) }}</span>
        <div class="flow-title-wrap">
          <div class="flow-title">{{ step.title }}</div>
          <code v-if="step.sub" class="flow-sub">{{ step.sub }}</code>
        </div>
      </div>
      <div class="flow-desc" v-html="step.desc" />
    </div>
  </div>
</template>

<style scoped>
.vp-flow-steps {
  margin: 18px 0;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
}

.vp-flow-steps.cols-1 {
  grid-template-columns: 1fr;
}

.vp-flow-steps.cols-3 {
  grid-template-columns: repeat(3, 1fr);
}

@media (max-width: 720px) {
  .vp-flow-steps,
  .vp-flow-steps.cols-3 {
    grid-template-columns: 1fr;
  }
}

.flow-card {
  padding: 16px 18px;
  border-radius: 12px;
  border: 1px solid var(--vp-c-divider);
  background: var(--vp-c-bg-soft);
  transition: transform 0.15s ease, border-color 0.15s ease;
}

.flow-card:hover {
  border-color: var(--vp-c-brand-1);
}

.flow-card-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.flow-num {
  display: inline-flex;
  flex-shrink: 0;
  white-space: nowrap;
  align-items: center;
  justify-content: center;
  min-width: 28px;
  height: 28px;
  padding: 0 8px;
  border-radius: 8px;
  background: var(--vp-c-brand-soft);
  color: var(--vp-c-brand-1);
  font-size: 13px;
  font-weight: 800;
  font-family: var(--vp-font-family-mono);
}

.flow-title-wrap {
  display: flex;
  min-width: 0;
  align-items: baseline;
  flex-wrap: wrap;
  gap: 8px;
}

.flow-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--vp-c-text-1);
}

.flow-sub {
  font-size: 12px;
  padding: 1px 6px;
  border-radius: 4px;
  background: var(--vp-c-bg);
  color: var(--vp-c-text-2);
}

.flow-desc {
  font-size: 13.5px;
  line-height: 1.65;
  color: var(--vp-c-text-2);
}

.flow-desc :deep(code) {
  font-size: 12.5px;
  background: var(--vp-c-bg);
  padding: 1px 5px;
  border-radius: 4px;
}
</style>
