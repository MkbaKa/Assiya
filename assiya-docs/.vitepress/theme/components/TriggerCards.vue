<script setup>
defineProps({
  // 每组对应一条队列, 组内是排在这条队列上的触发器
  groups: { type: Array, required: true }
})
</script>

<template>
  <div class="trigger-catalog">
    <section v-for="group in groups" :key="group.queue" class="trigger-group">
      <header class="trigger-group-head">
        <code class="trigger-queue">{{ group.queue }}</code>
        <span v-if="group.note" class="trigger-group-note" v-html="group.note" />
      </header>
      <div class="trigger-grid">
        <article v-for="item in group.items" :key="item.id" class="trigger-card">
          <div class="trigger-card-head">
            <code class="trigger-id">{{ item.id }}</code>
            <code class="trigger-entry">{{ item.entry }}</code>
          </div>
          <p class="trigger-when" v-html="item.when" />
          <div v-if="item.facts && item.facts.length" class="trigger-facts">
            <div v-for="fact in item.facts" :key="fact.label" class="trigger-fact">
              <span class="fact-label">{{ fact.label }}</span>
              <span class="fact-value" v-html="fact.value" />
            </div>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>

<style scoped>
.trigger-catalog {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin: 18px 0;
}

.trigger-group {
  padding: 12px 14px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 12px;
  background: var(--vp-c-bg-soft);
}

.trigger-group-head {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}

.trigger-queue {
  font-size: 12px;
  font-weight: 700;
  color: var(--vp-c-brand-1);
  background: var(--vp-c-brand-soft);
  padding: 2px 8px;
  border-radius: 6px;
}

.trigger-group-note {
  font-size: 12.5px;
  color: var(--vp-c-text-2);
}

.trigger-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 10px;
}

.trigger-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px 14px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 10px;
  background: var(--vp-c-bg);
}

.trigger-card:only-child {
  max-width: 440px;
}

.trigger-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 6px;
}

.trigger-id {
  font-size: 14px;
  font-weight: 700;
  color: var(--vp-c-brand-1);
  background: var(--vp-c-brand-soft);
  padding: 2px 8px;
  border-radius: 6px;
}

.trigger-entry {
  font-size: 12px;
  color: var(--vp-c-text-3);
}

.trigger-when {
  margin: 0;
  font-size: 13.5px;
  line-height: 1.6;
  color: var(--vp-c-text-1);
}

.trigger-facts {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding-top: 8px;
  border-top: 1px dashed var(--vp-c-divider);
}

.trigger-fact {
  display: flex;
  gap: 8px;
  font-size: 12.5px;
}

.fact-label {
  flex: none;
  color: var(--vp-c-text-3);
}

.fact-value {
  color: var(--vp-c-text-2);
}

.trigger-group-note :deep(code),
.trigger-when :deep(code),
.fact-value :deep(code) {
  font-size: 12px;
  padding: 1px 5px;
  border-radius: 4px;
  background: var(--vp-c-default-soft);
}
</style>
