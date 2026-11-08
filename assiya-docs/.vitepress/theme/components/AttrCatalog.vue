<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  items: { type: Array, required: true },
  tagOrder: { type: Array, required: true },
  placeholder: { type: String, required: true }
})

const selectedTags = ref([])
const query = ref('')

const tags = computed(() => {
  const counts = new Map()
  for (const item of props.items) {
    for (const tag of item.tags) counts.set(tag, (counts.get(tag) || 0) + 1)
  }
  const labels = [...new Set([...props.tagOrder, ...counts.keys()])]
  return labels.filter(label => counts.has(label)).map(label => ({ label, count: counts.get(label) }))
})

function toggleTag(tag) {
  selectedTags.value = selectedTags.value.includes(tag)
    ? selectedTags.value.filter(value => value !== tag)
    : [...selectedTags.value, tag]
}

const filtered = computed(() => {
  const q = query.value.trim().toLowerCase()
  return props.items.filter((item) => {
    if (!selectedTags.value.every(tag => item.tags.includes(tag))) return false
    if (!q) return true
    return (
      item.id.toLowerCase().includes(q) ||
      (item.names || []).some((n) => n.toLowerCase().includes(q)) ||
      item.tags.some(tag => tag.toLowerCase().includes(q)) ||
      (item.desc || '').toLowerCase().includes(q)
    )
  })
})
</script>

<template>
  <div class="attr-catalog">
    <div class="catalog-toolbar">
      <div class="catalog-tabs">
        <button type="button" :class="{ active: !selectedTags.length }" :aria-pressed="!selectedTags.length" @click="selectedTags = []">全部 ({{ items.length }})</button>
        <button
          v-for="tag in tags"
          :key="tag.label"
          type="button"
          :class="{ active: selectedTags.includes(tag.label) }"
          :aria-pressed="selectedTags.includes(tag.label)"
          @click="toggleTag(tag.label)"
        >
          {{ tag.label }} ({{ tag.count }})
        </button>
      </div>
      <input
        v-model="query"
        type="text"
        class="catalog-search"
        :placeholder="placeholder"
      />
    </div>

    <div class="catalog-grid">
      <div v-for="item in filtered" :key="item.id" class="attr-card">
        <div class="attr-card-top">
          <code class="attr-id">{{ item.id }}</code>
          <div class="attr-badges">
            <span v-if="item.priority !== null && item.priority !== undefined" class="attr-prio">
              优先级 {{ item.priority }}
            </span>
          </div>
        </div>

        <div class="attr-names">
          <span class="names-label">读取名：</span>
          <span v-for="(n, i) in item.names" :key="i" class="name-pill">{{ n }}</span>
        </div>

        <div class="attr-desc" v-html="item.desc" />

        <div class="attr-footer">
          <span class="attr-file">{{ item.file }}</span>
          <div class="attr-tags">
            <button v-for="tag in item.tags" :key="tag" type="button" class="attr-tag" :class="{ active: selectedTags.includes(tag) }" :aria-pressed="selectedTags.includes(tag)" @click="toggleTag(tag)">{{ tag }}</button>
          </div>
        </div>
      </div>
    </div>
    <p v-if="!filtered.length" class="catalog-empty" role="status">没有符合条件的属性。可取消部分标签或修改搜索内容。</p>
  </div>
</template>

<style scoped>
.attr-catalog {
  margin: 18px 0;
}

.catalog-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

.catalog-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.catalog-tabs button {
  padding: 5px 12px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  border: 1px solid var(--vp-c-divider);
  background: var(--vp-c-bg-soft);
  color: var(--vp-c-text-2);
  cursor: pointer;
  transition: all 0.15s;
}

.catalog-tabs button.active {
  border-color: var(--vp-c-brand-1);
  background: var(--vp-c-brand-soft);
  color: var(--vp-c-brand-1);
}

.catalog-search {
  flex: 1;
  min-width: 220px;
  padding: 7px 12px;
  border-radius: 8px;
  border: 1px solid var(--vp-c-divider);
  background: var(--vp-c-bg-soft);
  font-size: 13px;
}

.catalog-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(290px, 1fr));
  gap: 14px;
}

.attr-card {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 14px 16px;
  border-radius: 12px;
  border: 1px solid var(--vp-c-divider);
  background: var(--vp-c-bg-soft);
  transition: border-color 0.15s ease;
}

.attr-card:hover {
  border-color: var(--vp-c-brand-1);
}

.attr-card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 8px;
}

.attr-id {
  font-size: 14px;
  font-weight: 700;
  color: var(--vp-c-brand-1);
}

.attr-badges {
  display: flex;
  align-items: center;
  gap: 6px;
}

.attr-prio {
  font-size: 11px;
  font-family: var(--vp-font-family-mono);
  color: var(--vp-c-text-3);
}

.attr-names {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 8px;
}

.names-label {
  font-size: 12px;
  color: var(--vp-c-text-3);
}

.name-pill {
  font-size: 12.5px;
  font-weight: 600;
  padding: 1px 8px;
  border-radius: 6px;
  background: var(--vp-c-bg);
  border: 1px solid var(--vp-c-divider);
  color: var(--vp-c-text-1);
}

.attr-desc {
  font-size: 13px;
  line-height: 1.6;
  color: var(--vp-c-text-2);
  margin-bottom: 10px;
}

.attr-footer {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
  padding-top: 8px;
  border-top: 1px dashed var(--vp-c-divider);
  font-size: 11.5px;
  color: var(--vp-c-text-3);
}

.attr-tags {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
}

.attr-tag {
  font-size: 12px;
  padding: 2px 8px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 999px;
  background: var(--vp-c-bg);
  color: var(--vp-c-text-2);
  cursor: pointer;
}
.attr-tag:hover,
.attr-tag.active {
  border-color: var(--vp-c-brand-1);
  background: var(--vp-c-brand-soft);
  color: var(--vp-c-brand-1);
}
.catalog-empty {
  color: var(--vp-c-text-2);
}
</style>
