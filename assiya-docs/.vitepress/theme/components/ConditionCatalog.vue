<script setup>
defineProps({
  items: { type: Array, required: true }
})
</script>

<template>
  <div class="condition-catalog">
    <article v-for="item in items" :key="item.id" class="condition-card">
      <div class="condition-meta">
        <div class="condition-name">{{ item.name }}</div>
        <code class="condition-id">{{ item.id }}</code>
        <div class="condition-reading">
          <span class="meta-label">读取名</span>
          <div class="reading-names">
            <span v-for="name in item.names" :key="name" class="reading-name">{{ name }}</span>
          </div>
        </div>
        <div class="condition-reader"><span class="meta-label">读取组</span><code>{{ item.reader }}</code></div>
      </div>
      <div class="condition-body">
        <p class="condition-description">{{ item.desc }}</p>
        <div class="condition-examples">
          <div v-for="example in item.examples" :key="example.code" class="condition-example">
            <code>{{ example.code }}</code>
            <span>{{ example.description }}</span>
          </div>
        </div>
        <div v-if="item.values?.length" class="condition-values">
          <span v-for="value in item.values" :key="value" class="value-tag">{{ value }}</span>
        </div>
        <p v-if="item.note" class="condition-note">{{ item.note }}</p>
      </div>
    </article>
  </div>
</template>

<style scoped>
.condition-catalog {
  display: grid;
  gap: 16px;
  margin: 20px 0;
}
.condition-card {
  display: grid;
  grid-template-columns: 180px minmax(0, 1fr);
  gap: 24px;
  padding: 24px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 12px;
  background: var(--vp-c-bg-soft);
}
.condition-meta { min-width: 0; }
.condition-name {
  margin-bottom: 6px;
  font-size: 17px;
  font-weight: 600;
  color: var(--vp-c-text-1);
}
.condition-id { font-size: 12px; overflow-wrap: anywhere; }
.condition-reading { margin-top: 20px; }
.meta-label { font-size: 12px; color: var(--vp-c-text-2); }
.reading-names { display: flex; flex-wrap: wrap; gap: 6px; margin-top: 6px; }
.reading-name, .value-tag {
  padding: 2px 8px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 6px;
  background: var(--vp-c-bg);
  font-size: 13px;
  line-height: 22px;
}
.condition-reader { display: flex; align-items: center; gap: 10px; margin-top: 12px; }
.condition-body { min-width: 0; }
.condition-card .condition-description { margin: 0 0 14px; font-size: 14px; line-height: 1.8; }
.condition-examples { overflow: hidden; border: 1px solid var(--vp-c-divider); border-radius: 8px; background: var(--vp-c-bg); }
.condition-example { display: flex; flex-wrap: wrap; align-items: baseline; justify-content: space-between; gap: 6px 16px; padding: 10px 12px; }
.condition-example + .condition-example { border-top: 1px solid var(--vp-c-divider); }
.condition-example code { padding: 0; background: transparent; font-size: 13px; white-space: pre-wrap; overflow-wrap: anywhere; }
.condition-example span { font-size: 12px; color: var(--vp-c-text-2); }
.condition-values { display: flex; flex-wrap: wrap; gap: 6px; margin-top: 14px; }
.condition-card .condition-note { margin: 12px 0 0; font-size: 13px; line-height: 1.7; color: var(--vp-c-text-2); }
@media (max-width: 640px) {
  .condition-card { grid-template-columns: minmax(0, 1fr); gap: 18px; padding: 20px; }
  .condition-reading { margin-top: 14px; }
}
</style>
