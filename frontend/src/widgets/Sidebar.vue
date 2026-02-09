<script lang="ts">
import { defineComponent } from 'vue'
import { useDirectoryStore } from '@/entities/directory/model/store'

export default defineComponent({
  name: 'Sidebar',
  data() {
    return { store: useDirectoryStore() }
  },
  computed: {
    currentId(): number | null {
      const p = this.$route.params.id
      if (!p) return null
      const n = Number(p)
      return Number.isNaN(n) ? null : n
    },
  },
  mounted() {
    this.store.fetchAll()
  },
  methods: {
    goDirectory(id: number) {
      this.$router.push({ name: 'directory-editor', params: { id: String(id) } })
    },
    isActive(id: number) {
      return this.currentId === id
    },
    create() {
      const draft = this.store.createDraft()
      this.goDirectory(draft.id)
    },
  },
})
</script>

<template>
  <aside class="sidebar">
    <div class="sidebar-header">
      <div class="sidebar-brand">
        <svg class="brand-icon" width="18" height="18" viewBox="0 0 24 24" fill="#0067c0">
          <path d="M3 13h8V3H3v10zm0 8h8v-6H3v6zm10 0h8V11h-8v10zm0-18v6h8V3h-8z"></path>
        </svg>
        <div class="sidebar-title">Data Builder</div>
      </div>

      <button type="button" class="sidebar-create" @click="create" title="Создать справочник">
        +
      </button>
    </div>

    <nav class="sidebar-nav">
      <button
          v-for="dir in store.items"
          :key="dir.id"
          type="button"
          :class="['sidebar-item', { active: isActive(dir.id) }]"
          @click="goDirectory(dir.id)"
      >
        <div class="sidebar-name">{{ dir.name || 'Без названия' }}</div>
        <small class="sidebar-count">
          {{ dir.fieldCount }} полей, {{ dir.recordCount }} зап.
          <span v-if="(dir as any).isDraft"> (черновик)</span>
        </small>
      </button>
    </nav>
  </aside>
</template>

<style>
.sidebar {
  width: 260px;
  flex-shrink: 0;
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  background: var(--bg-card);
  overflow-x: hidden;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  padding: var(--space-4) var(--space-5);
  border-bottom: 1px solid var(--border);
}

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.sidebar-title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: var(--text);
}

.sidebar-create {
  width: 28px;
  height: 28px;
  padding: 0;
  border: none;
  border-radius: var(--radius-md);
  background: var(--btn-secondary);
  color: #333;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.sidebar-create:hover {
  background: var(--btn-secondary-hover);
}

.sidebar-nav {
  flex: 1;
  overflow-y: auto;
  padding: 10px 0;
  display: flex;
  flex-direction: column;
  overflow-x: hidden;
}

.sidebar-item {
  position: relative;
  width: 100%;
  border: none;
  background: transparent;
  padding: 10px var(--space-5);
  text-align: left;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 4px;
  transition: background 0.15s ease, color 0.15s ease;
  font-family: var(--font-sans, system-ui), sans-serif;
  color: var(--text);
}

.sidebar-name {
  font-weight: 500;
  font-size: 14px;
}

.sidebar-count {
  color: var(--muted);
  font-size: 11px;
}

.sidebar-item:hover {
  background: var(--hover);
}

.sidebar-item.active {
  color: var(--primary);
  background: var(--primary-bg);
}

.sidebar-item.active::before {
  content: "";
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: var(--primary-700);
}

</style>