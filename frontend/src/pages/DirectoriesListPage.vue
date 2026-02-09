<script lang="ts">
import {defineComponent} from "vue";
import Header from "@/widgets/Header.vue";
import AppButton from "@/shared/ui/AppButton.vue";
import {useDirectoryStore} from "@/entities/directory/model/store.ts";
import DirectoryDataTab from "@/widgets/DirectoryDataTab.vue";

export default defineComponent({
  name: 'DirectoryListPage',
  components: { Header, AppButton, DirectoryDataTab  },

  data() {
    return {
      store: useDirectoryStore(),
      activeTab: 'structure' as 'structure' | 'data',

      newFieldName: '',
      newFieldType: 'STRING' as 'STRING' | 'NUMBER' | 'DIRECTORY_REFERENCE',
      newFieldRefId: null as number | null,

      formError: null as string | null,
      newFieldError: null as string | null,
    }
  },

  computed: {
    directoryId(): number | null {
      const raw = this.$route.params.id
      const n = Number(raw)
      return Number.isNaN(n) ? null : n
    },

    dir(){
      if (this.directoryId == null) return null
      return this.store.getById(this.directoryId)
    },

    isDraft(): boolean {
      return this.dir?.isDraft ?? false
    },

    dataTabDisabled(): boolean {
      return !this.dir
    },

    directoryCode(): string {
      return this.dir?.code ?? ''
    },

    referenceDirectories() {
      const currentId = this.dir?.id
      return this.store.remotes.filter(d => d.id !== currentId)
    },

    directoryNameById() {
      const map: Record<number, string> = {}
      for (const d of this.store.items) map[d.id] = d.name || d.code || `#${d.id}`
      return map
    }

  },

  methods: {
    async setTab(tab: 'structure' | 'data') {
      this.activeTab = tab
    },

    createNew() {
      const draft = this.store.createDraft()
      this.$router.push({ name: 'directory-editor', params: { id: String(draft.id) } })
    },

    onNameInput(e: Event) {
      if (!this.dir) return
      const v = (e.target as HTMLInputElement).value
      this.store.setName(this.dir.id, v)
    },

    removeField(idx: number) {
      if (!this.dir) return
      this.store.removeField(this.dir.id, idx)
    },

    getDirectoryName(id: number | null) {
      if (!id) return ''
      return this.directoryNameById[id] ?? `#${id}`
    },

    addField() {
      this.newFieldError = null
      if (!this.dir) return

      const name = this.newFieldName.trim()
      if (!name) {
        this.newFieldError = 'Введите имя поля'
        return
      }

      const exists = (this.dir.fields ?? []).some(f => f.name.trim().toLowerCase() === name.toLowerCase())
      if (exists) {
        this.newFieldError = 'Поле с таким именем уже существует'
        return
      }

      if (this.newFieldType === 'DIRECTORY_REFERENCE') {
        if (this.newFieldRefId == null) {
          this.newFieldError = 'Выберите справочник для связи'
          return
        }

        if (this.newFieldRefId === this.dir.id) {
          this.newFieldError = 'Справочник не может ссылаться на самого себя'
          return
        }

        const ok = this.store.remotes.some(d => d.id === this.newFieldRefId)
        if (!ok) {
          this.newFieldError = 'Нельзя ссылаться на черновик или несуществующий справочник'
          return
        }
      }

      this.store.addField(this.dir.id, {
        name,
        type: this.newFieldType,
        directoryId: this.newFieldType === 'DIRECTORY_REFERENCE' ? this.newFieldRefId : null,
      })

      this.newFieldName = ''
      this.newFieldType = 'STRING'
      this.newFieldRefId = null
    },

    validateBeforeSave(): string | null {
      if (!this.dir) return 'Справочник не выбран'

      const name = (this.dir.name ?? '').trim()
      if (!name) return 'Укажите название справочника'

      const fields = this.dir.fields ?? []

      const emptyField = fields.find(f => !f.name || !f.name.trim())
      if (emptyField) return 'Есть поле с пустым именем'

      const normalized = fields.map(f => f.name.trim().toLowerCase())
      const unique = new Set(normalized)
      if (unique.size !== normalized.length) return 'Имена полей должны быть уникальными'

      for (const f of fields) {
        if (f.type === 'DIRECTORY_REFERENCE') {
          if (f.directoryId == null) return `Поле "${f.name}": не выбран справочник`
          const ok = this.store.remotes.some(d => d.id === f.directoryId)
          if (!ok) return `Поле "${f.name}": связь должна быть с сохранённым справочником`
          if (f.directoryId === this.dir.id) return `Поле "${f.name}": нельзя ссылаться на себя`
        } else {
          if (f.directoryId != null) return `Поле "${f.name}": directoryId должен быть пустым`
        }
      }

      return null
    },

    async save() {
      this.formError = null
      if (!this.dir) return

      const err = this.validateBeforeSave()
      if (err) {
        this.formError = err
        return
      }

      try {
        const res = await this.store.save(this.dir.id)
        if (res?.savedId && res.savedId !== this.dir.id) {
          this.$router.replace({ name: 'directory-editor', params: { id: String(res.savedId) } })
        }
      }
      catch {
        // ошибка бэка будет в store.error
      }
    },

    fieldTypeLabel(type: string) {
      switch (type) {
        case 'STRING': return 'Строка'
        case 'NUMBER': return 'Число'
        case 'DIRECTORY_REFERENCE': return 'Справочник'
        default: return type
      }
    },

    typeChipText(f: any): string {
      if (f.type === 'DIRECTORY_REFERENCE') {
        return `🔗 ${this.getDirectoryName(f.directoryId)}`
      }
      return this.fieldTypeLabel(f.type)
    },

  },

  watch: {
    directoryId: {
      immediate: true,
      handler() {
        this.store.clearError()
        this.formError = null
        this.newFieldError = null
      },
    },
  },

})

</script>

<template>
  <div class="page">
    <Header
      :name="dir?.name ?? ''"
      :code="directoryCode"
      :draft="isDraft"
    />

    <section class="main" v-if="dir">
      <div class="main-card">
        <div class="tabs">
          <button
            type="button"
            :class="['tab', { active: activeTab === 'structure' }]"
            @click="setTab('structure')"
          >
            Структура
          </button>

          <button
            type="button"
            :class="['tab', { active: activeTab === 'data' }]"
            :disabled="dataTabDisabled"
            @click="setTab('data')"
          >
            Данные
          </button>
        </div>

        <div class="panel">
          <template v-if="activeTab==='structure'">
            <div class="toolbar">
              <div class="toolbar-col">
                <div class="label">Название справочника</div>
                <input class="input input--text" :value="dir?.name ?? ''" maxlength="255" @input="onNameInput" />
              </div>

              <div class="toolbar-col">
                <div class="label">Системный код</div>
                <input class="input input--code" :value="directoryCode" disabled />
              </div>

              <div class="toolbar-actions">
                <AppButton variant="secondary" @click="createNew">+ Создать новый</AppButton>
                <AppButton variant="primary" @click="save">Сохранить изменения</AppButton>
              </div>
            </div>
            <p v-if="formError" class="form-error">{{ formError }}</p>
            <p v-if="store.error" class="form-error">{{ store.error }}</p>
            <div class="schema-table-wrap" style="margin-top: 16px;">
              <table class="schema-table">
                <thead>
                <tr>
                  <th>Имя поля</th>
                  <th>Тип</th>
                  <th>Связь</th>
                  <th style="width: 60px;"></th>
                </tr>
                </thead>

                <tbody>
                <tr v-for="(f, idx) in (dir?.fields ?? [])" :key="idx">
                  <td>{{ f.name }}</td>
                  <td>
                    <div :class="['type-chip', `type-chip--${f.type}`]">
                      {{ typeChipText(f) }}
                    </div>
                  </td>
                  <td>
                    <span v-if="f.type === 'DIRECTORY_REFERENCE'" class="ref-name">
                      {{ getDirectoryName(f.directoryId) }}
                    </span>
                  </td>
                  <td style="text-align:right;">
                    <button class="icon-btn" type="button" @click="removeField(idx)">✕</button>
                  </td>
                </tr>
                </tbody>
              </table>
            </div>

            <div class="add-field">
              <input class="input" v-model="newFieldName" placeholder="Имя свойства" />

              <select class="input" v-model="newFieldType">
                <option value="STRING">Строка</option>
                <option value="NUMBER">Число</option>
                <option value="DIRECTORY_REFERENCE">🔗 Справочник</option>
              </select>

              <select v-if="newFieldType === 'DIRECTORY_REFERENCE'" class="input" v-model.number="newFieldRefId">
                <option :value="null">--</option>
                <option v-for="d in referenceDirectories" :key="d.id" :value="d.id">
                  {{ d.name || d.code }}
                </option>
              </select>

              <AppButton variant="secondary" @click="addField">Добавить</AppButton>
            </div>
            <p v-if="newFieldError" class="form-error">{{ newFieldError }}</p>
          </template>
          <template v-else>
            <DirectoryDataTab :key="dir.id" :dir="dir" />
          </template>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.main {
  flex: 1;
  padding: var(--space-8);
}

.main-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border);
  box-shadow: var(--shadow-card);
}

.tabs {
  display: flex;
  padding: 4px;
  gap: 4px;
  border-radius: var(--radius-md);
  background: var(--bg-muted);
}

.tab {
  flex: 1;
  padding: 10px 16px;
  border: none;
  cursor: pointer;
  border-radius: var(--radius-sm);
  font-weight: 600;
  background: transparent;
  color: var(--text);
}

.tab.active {
  background: var(--primary);
  color: #fff;
}

.tab:disabled {
  opacity: 1;
  cursor: not-allowed;
}

.panel {
  border-radius: var(--radius-lg);
  padding: var(--space-6);
}

.toolbar {
  display: flex;
  align-items: flex-end;
  gap: var(--space-4);
  flex-wrap: wrap;
  border-bottom: 1px solid var(--border);
  padding-bottom: var(--space-4);
  margin-bottom: var(--space-6);
}

.toolbar-col {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 240px;
  flex: 1;
}

.toolbar-actions {
  display: flex;
  gap: 10px;
}

.label {
  font-size: 11px;
  text-transform: uppercase;
  font-weight: 700;
  color: var(--muted);
}

.input {
  padding: var(--space-2) var(--space-3);
  border: 1px solid transparent;
}

.input:focus,
select.input:focus {
  border-color: var(--primary);
  outline: 2px solid var(--primary-focus);
  border-radius: var(--radius-sm);
}

.input--text:hover {
  background: rgba(0, 0, 0, 0.02);
  border-color: rgba(0, 0, 0, 0.1);
}

.input--text {
  font-size: 20px;
  font-weight: 600;
  border: 1px solid transparent;
  background: transparent;
}

.input--code {
  border: 1px solid rgb(138, 136, 134);
  border-radius: var(--radius-md);
  color: #777;
}

.schema-table-wrap {
  overflow-x: auto;
  border-radius: var(--radius-md);
}

.schema-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 600px;
  table-layout: fixed;
}

.schema-table th {
  text-align: left;
  font-weight: 700;
  color: var(--muted);
  border-bottom: 1px solid var(--border);
  text-transform: uppercase;
  font-size: 11px
}

.schema-table td {
  padding: 10px 0;
  font-weight: 500;
  font-size: 14px;
  color: var(--text);
  border-bottom: 1px solid var(--border);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}


.schema-table tr:last-child td {
  border-bottom: none;
}

.schema-table td:nth-child(4) {
  overflow: visible;
  text-overflow: clip;
  white-space: nowrap;
}

.type-chip {
  display: inline-block;
  max-width: 100%;
  padding: 3px 8px;
  border-radius: var(--radius-pill);
  font-size: 11px;
  font-weight: 600;

  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.type-chip--STRING {
  color: #333;
  background: var(--bg-muted);
}

.type-chip--NUMBER {
  color: rgb(2, 119, 189);
  background: rgb(225, 245, 254);
}

.type-chip--DIRECTORY_REFERENCE {
  color: var(--primary);
  background: #e8f0fe;
}

.ref-name {
  color: var(--muted);
}

.icon-btn {
  border: none;
  background: rgba(0, 0, 0, 0.05);
  border-radius: var(--radius-sm);
  cursor: pointer;
  padding: 4px 8px;
}

.icon-btn:hover {
  background: rgba(0, 0, 0, 0.1);
}

.add-field {
  background: var(--bg-muted);
  padding: var(--space-4);
  border-radius: var(--radius-sm);
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(0, 1fr) minmax(0, 2fr) auto;
  gap: 10px;
  align-items: center;
}

.form-error {
  color: var(--danger);
  margin-top: var(--space-3);
}

</style>