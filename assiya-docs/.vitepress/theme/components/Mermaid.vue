<script setup>
import { onMounted, ref, watch } from 'vue'
import { useData } from 'vitepress'

const props = defineProps({
  // 图表源码, 使用 Mermaid 语法
  code: { type: String, required: true }
})

const { isDark } = useData()
const svg = ref('')
let counter = 0

async function render() {
  const { default: mermaid } = await import('mermaid')
  mermaid.initialize({
    startOnLoad: false,
    theme: isDark.value ? 'dark' : 'default',
    fontFamily: 'var(--vp-font-family-base)'
  })
  const id = `mermaid-${Math.random().toString(36).slice(2)}-${counter++}`
  const result = await mermaid.render(id, props.code)
  svg.value = result.svg
}

onMounted(render)
watch(isDark, render)
</script>

<template>
  <div class="mermaid-diagram" v-html="svg" />
</template>

<style scoped>
.mermaid-diagram {
  display: flex;
  justify-content: center;
  margin: 16px 0;
  overflow-x: auto;
}
</style>
