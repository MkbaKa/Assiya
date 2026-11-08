<script setup>
import { computed } from 'vue'

const props = defineProps({
  name: { type: String, required: true },
  lines: { type: Array, default: () => [] },
  caption: { type: String, default: '' }
})

const COLOR_MAP = {
  '0': '#000000',
  '1': '#0000AA',
  '2': '#00AA00',
  '3': '#00AAAA',
  '4': '#AA0000',
  '5': '#AA00AA',
  '6': '#FFAA00',
  '7': '#AAAAAA',
  '8': '#555555',
  '9': '#5555FF',
  'a': '#55FF55',
  'b': '#55FFFF',
  'c': '#FF5555',
  'd': '#FF55FF',
  'e': '#FFFF55',
  'f': '#FFFFFF'
}

function parseMcText(raw) {
  if (!raw) return [{ text: ' ', color: '#AAAAAA', bold: false }]
  const segments = []
  let color = '#AAAAAA'
  let bold = false
  let italic = false
  let buffer = ''

  for (let i = 0; i < raw.length; i++) {
    if ((raw[i] === '&' || raw[i] === '§') && i + 1 < raw.length) {
      const code = raw[i + 1].toLowerCase()
      if (code in COLOR_MAP || code === 'l' || code === 'o' || code === 'r') {
        if (buffer) {
          segments.push({ text: buffer, color, bold, italic })
          buffer = ''
        }
        if (code in COLOR_MAP) {
          color = COLOR_MAP[code]
          bold = false
          italic = false
        } else if (code === 'l') {
          bold = true
        } else if (code === 'o') {
          italic = true
        } else if (code === 'r') {
          color = '#AAAAAA'
          bold = false
          italic = false
        }
        i++
        continue
      }
    }
    buffer += raw[i]
  }
  if (buffer) {
    segments.push({ text: buffer, color, bold, italic })
  }
  return segments
}

const parsedName = computed(() => parseMcText(props.name))

const normalizedLines = computed(() => {
  return props.lines.map((item) => {
    if (typeof item === 'string') {
      return { segments: parseMcText(item), note: '', status: '' }
    }
    return {
      segments: parseMcText(item.text),
      note: item.note || '',
      status: item.status || ''
    }
  })
})
</script>

<template>
  <div class="mc-card-wrap">
    <div class="mc-tooltip">
      <div class="mc-title">
        <span
          v-for="(seg, idx) in parsedName"
          :key="idx"
          :style="{
            color: seg.color,
            fontWeight: seg.bold ? '700' : '600',
            fontStyle: seg.italic ? 'italic' : 'normal'
          }"
        >{{ seg.text }}</span>
      </div>
      <div class="mc-lore">
        <div v-for="(line, rIdx) in normalizedLines" :key="rIdx" class="mc-lore-row">
          <div class="mc-lore-text">
            <span
              v-for="(seg, sIdx) in line.segments"
              :key="sIdx"
              :style="{
                color: seg.color,
                fontWeight: seg.bold ? '700' : '400',
                fontStyle: seg.italic ? 'italic' : 'normal'
              }"
            >{{ seg.text }}</span>
          </div>
          <span
            v-if="line.note"
            class="mc-lore-note"
            :class="line.status ? 'note-' + line.status : ''"
          >
            {{ line.note }}
          </span>
        </div>
      </div>
    </div>
    <div v-if="caption" class="mc-caption">{{ caption }}</div>
  </div>
</template>

<style scoped>
.mc-card-wrap {
  margin: 18px 0;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.mc-tooltip {
  background: #110211;
  border: 2px solid #2b0662;
  box-shadow:
    inset 0 0 0 1px #4f12a8,
    0 8px 24px rgba(0, 0, 0, 0.35);
  border-radius: 6px;
  padding: 12px 16px;
  width: fit-content;
  max-width: 100%;
  font-family: 'JetBrains Mono', 'Consolas', 'Microsoft YaHei', monospace;
  line-height: 1.55;
}

.mc-title {
  font-size: 16px;
  margin-bottom: 6px;
  text-shadow: 1px 1px 0 rgba(0, 0, 0, 0.8);
}

.mc-lore {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.mc-lore-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  flex-wrap: wrap;
}

.mc-lore-text {
  font-size: 14px;
  text-shadow: 1px 1px 0 rgba(0, 0, 0, 0.8);
  white-space: pre-wrap;
}

.mc-lore-note {
  font-size: 12px;
  padding: 1px 8px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  color: #cbd5e1;
  font-family: var(--vp-font-family-base);
}

.mc-lore-note.note-ok {
  background: rgba(16, 185, 129, 0.18);
  color: #6ee7b7;
  border: 1px solid rgba(16, 185, 129, 0.35);
}

.mc-lore-note.note-warn {
  background: rgba(245, 158, 11, 0.18);
  color: #fcd34d;
  border: 1px solid rgba(245, 158, 11, 0.35);
}

.mc-lore-note.note-blocked {
  background: rgba(239, 68, 68, 0.18);
  color: #fca5a5;
  border: 1px solid rgba(239, 68, 68, 0.35);
}

.mc-caption {
  margin-top: 6px;
  font-size: 13px;
  color: var(--vp-c-text-2);
}
</style>
