<script lang="ts">
import { defineComponent } from "vue";

export default defineComponent({
  name: 'Header',
  props: {
    name: { type: String, default: '' },
    code: { type: String, default: '' },
    draft: { type: Boolean, default: false },
  },
})
</script>

<template>
  <header class="header">
    <template v-if="draft">
      <div class="header-draft-code">{{ code }}</div>
    </template>
    <template v-else>
      <h1 class="header-title">{{ name }}</h1>
      <span class="header-code">{{ code }}</span>
    </template>
  </header>
</template>

<style scoped>
.header {
  padding: var(--space-5) var(--space-8);
  background: var(--bg-card);
  border-bottom: 1px solid var(--border);
}

.header-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: var(--text);
  white-space: normal;
  overflow-wrap: anywhere;
  word-break: break-word;
}

.header-code {
  display: block;
  margin-top: 2px;
  font-size: 12px;
  line-height: 1.4;
  color: var(--muted);
}

.header-draft-code {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  line-height: 1.2;
  color: var(--text);
}
</style>
