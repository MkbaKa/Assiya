<script setup>
defineProps({
  title: { type: String, default: '' },
  items: { type: Array, default: () => [] }
})
</script>

<template>
  <div class="vp-param-list">
    <div v-if="title" class="vp-param-header">{{ title }}</div>
    <div class="vp-param-body">
      <div v-for="(item, idx) in items" :key="idx" class="vp-param-row">
        <div class="vp-param-meta">
          <div class="vp-param-names">
            <code class="vp-param-primary">{{ item.name }}</code>
            <template v-if="item.aliases && item.aliases.length">
              <code
                v-for="(alias, aIdx) in item.aliases"
                :key="aIdx"
                class="vp-param-alias"
                title="简写 / 别名"
              >{{ alias }}</code>
            </template>
          </div>
          <div class="vp-param-badges">
            <span v-if="item.type" class="vp-badge vp-badge-type">{{ item.type }}</span>
            <span v-if="item.required" class="vp-badge vp-badge-req">{{ item.required === true ? '必填' : item.required }}</span>
            <span v-else-if="item.default !== undefined && item.default !== ''" class="vp-badge vp-badge-def">
              <code>{{ item.default }}</code>
            </span>
          </div>
        </div>
        <div class="vp-param-desc" v-html="item.desc" />
        <div v-if="item.example" class="vp-param-example">
          <span class="ex-label">例子：</span>
          <code>{{ item.example }}</code>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.vp-param-list {
  margin: 18px 0;
  border: 1px solid var(--vp-c-divider);
  border-radius: 12px;
  background: var(--vp-c-bg-soft);
  overflow: hidden;
}

.vp-param-header {
  padding: 10px 18px;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.02em;
  color: var(--vp-c-text-2);
  background: var(--vp-c-bg-alt);
  border-bottom: 1px solid var(--vp-c-divider);
}

.vp-param-body {
  display: flex;
  flex-direction: column;
}

.vp-param-row {
  padding: 14px 18px;
  border-bottom: 1px solid var(--vp-c-divider);
  background: var(--vp-c-bg);
  transition: background 0.15s ease;
}

.vp-param-row:last-child {
  border-bottom: none;
}

.vp-param-row:hover {
  background: var(--vp-c-bg-soft);
}

.vp-param-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 8px 12px;
  margin-bottom: 6px;
}

.vp-param-names {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
}

.vp-param-primary {
  font-size: 14px;
  font-weight: 700;
  color: var(--vp-c-brand-1);
  background: var(--vp-c-brand-soft);
  padding: 2px 8px;
  border-radius: 6px;
}

.vp-param-alias {
  font-size: 12px;
  color: var(--vp-c-text-2);
  background: var(--vp-c-default-soft);
  padding: 1px 6px;
  border-radius: 5px;
}

.vp-param-badges {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
}

.vp-badge {
  font-size: 11.5px;
  padding: 2px 8px;
  border-radius: 999px;
  font-weight: 600;
}

.vp-badge-type {
  background: var(--vp-c-default-soft);
  color: var(--vp-c-text-2);
  font-family: var(--vp-font-family-mono);
}

.vp-badge-req {
  background: rgba(239, 68, 68, 0.14);
  color: #ef4444;
}

.vp-badge-def {
  background: rgba(16, 185, 129, 0.12);
  color: var(--vp-c-text-2);
}

.vp-badge-def code {
  font-size: 11.5px;
  background: transparent;
  padding: 0;
  color: var(--vp-c-green-1, #10b981);
  font-weight: 700;
}

.vp-param-desc {
  font-size: 14px;
  line-height: 1.65;
  color: var(--vp-c-text-1);
}

.vp-param-desc :deep(code) {
  font-size: 12.5px;
  padding: 1px 5px;
  border-radius: 4px;
  background: var(--vp-c-default-soft);
}

.vp-param-example {
  margin-top: 8px;
  font-size: 12.5px;
  color: var(--vp-c-text-2);
}

.ex-label {
  color: var(--vp-c-text-3);
}
</style>
