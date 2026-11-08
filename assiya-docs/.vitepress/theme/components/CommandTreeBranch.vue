<script setup>
defineProps({
  node: { type: Object, required: true },
  selected: { type: Number, required: true }
})
defineEmits(['select'])
</script>

<template>
  <li class="command-branch">
    <button v-if="node.index !== undefined" type="button" class="command-node" :class="{ active: selected === node.index }" :aria-pressed="selected === node.index" @click="$emit('select', node.index)">
      <span class="command-name">{{ node.segment }}</span>
      <span class="command-label">{{ node.label }}</span>
    </button>
    <div v-else class="command-node command-group"><span class="command-name">{{ node.segment }}</span></div>
    <ul v-if="node.children.length" class="command-children">
      <CommandTreeBranch v-for="child in node.children" :key="child.path" :node="child" :selected="selected" @select="$emit('select', $event)" />
    </ul>
  </li>
</template>

<style scoped>
.command-branch {
  --command-tree-line: #000;
  position: relative;
  list-style: none;
  margin: 0 !important;
  padding: 4px 0 4px 16px;
}
.command-branch::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  border-left: 1px solid var(--command-tree-line);
}
.command-branch:last-child::before { bottom: auto; height: 22px; }
.command-branch::after {
  content: '';
  position: absolute;
  left: 0;
  top: 22px;
  width: 12px;
  border-top: 1px solid var(--command-tree-line);
}
:global(.dark .command-branch) {
  --command-tree-line: #fff;
}
.command-children {
  list-style: none;
  margin: 0 0 0 10px !important;
  padding: 4px 0 0 !important;
}
.command-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
  width: 100%;
  min-height: 36px;
  padding: 6px 10px;
  border: 1px solid transparent;
  border-radius: 7px;
  background: transparent;
  text-align: left;
  cursor: pointer;
}
.command-group { cursor: default; }
button.command-node:hover { background: var(--vp-c-bg); border-color: var(--vp-c-divider); }
.command-node.active { background: var(--vp-c-brand-soft); border-color: var(--vp-c-brand-1); }
.command-name { font-family: var(--vp-font-family-mono); font-size: 13px; font-weight: 700; color: var(--vp-c-text-1); }
.command-label { font-size: 11.5px; color: var(--vp-c-text-3); white-space: nowrap; }
.active .command-name, .active .command-label { color: var(--vp-c-brand-1); }
</style>
