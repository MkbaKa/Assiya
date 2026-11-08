<script setup>
defineProps({
  label: { type: String, default: '语法格式' },
  syntax: { type: String, required: true },
  aliases: { type: Array, default: () => [] },
  badge: { type: String, default: '' },
  path: { type: String, default: '' }
})
</script>

<template>
  <div class="vp-syntax-bar">
    <div class="syntax-top">
      <div class="syntax-left">
        <span class="syntax-label">{{ label }}</span>
        <code class="syntax-main">{{ syntax }}</code>
      </div>
      <span v-if="badge" class="syntax-badge">{{ badge }}</span>
    </div>

    <div v-if="aliases.length || path" class="syntax-footer">
      <div v-if="aliases.length" class="syntax-aliases">
        <span class="footer-label">可用别称</span>
        <code v-for="(a, idx) in aliases" :key="idx" class="alias-chip">{{ a }}</code>
      </div>
      <div v-if="path" class="syntax-path">
        <span class="footer-label">文件位置</span>
        <code class="path-chip">{{ path }}</code>
      </div>
    </div>
  </div>
</template>

<style scoped>
.vp-syntax-bar {
  margin: 16px 0 22px;
  padding: 14px 18px;
  border-radius: 12px;
  border: 1px solid var(--vp-c-brand-soft);
  background: linear-gradient(135deg, var(--vp-c-bg-soft) 0%, var(--vp-c-bg-alt) 100%);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
}

.syntax-top {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 10px;
}

.syntax-left {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.syntax-label {
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  padding: 3px 8px;
  border-radius: 6px;
  background: var(--vp-c-brand-soft);
  color: var(--vp-c-brand-1);
}

.syntax-main {
  font-size: 15px;
  font-weight: 700;
  color: var(--vp-c-text-1);
  background: var(--vp-c-bg);
  padding: 4px 12px;
  border-radius: 8px;
  border: 1px solid var(--vp-c-divider);
}

.syntax-badge {
  font-size: 12px;
  font-weight: 600;
  padding: 2px 10px;
  border-radius: 999px;
  background: var(--vp-c-default-soft);
  color: var(--vp-c-text-2);
}

.syntax-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 10px 20px;
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px dashed var(--vp-c-divider);
}

.syntax-aliases,
.syntax-path {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
}

.footer-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--vp-c-text-3);
  margin-right: 4px;
}

.alias-chip,
.path-chip {
  font-size: 12.5px;
  padding: 1px 7px;
  border-radius: 5px;
  background: var(--vp-c-bg);
  border: 1px solid var(--vp-c-divider);
  color: var(--vp-c-text-2);
}
</style>
