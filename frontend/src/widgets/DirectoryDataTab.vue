  <script lang="ts">
  import { defineComponent, type PropType } from 'vue'
  import AppButton from '@/shared/ui/AppButton.vue'
  import { recordsApi } from '@/entities/records/api'
  import { useDirectoryStore } from '@/entities/directory/model/store'
  import { useRecCacheStore } from '@/entities/records/model/store'
  import type { DirectoryListItem, DraftDirectory } from '@/entities/directory/model/types'
  import type { RecordDTO } from '@/entities/records/model/types'

  export default defineComponent({
    name: 'DirectoryDataTab',
    components: { AppButton },

    props: {
      dir: {
        type: Object as PropType<DirectoryListItem>,
        required: true,
      },
    },

    data() {
      return {
        store: useDirectoryStore(),
        refCache: useRecCacheStore(),

        fetching: false,
        recordsError: null as string | null,
        records: [] as RecordDTO[],
        totalRecords: 0,

        recordsDirId: null as number | null,
        recordsReqId: 0,

        search: '',
        searchTimer: null as any,

        modalOpen: false,
        modalValues: {} as Record<string, string | number | null>,
        modalErrors: {} as Record<string, string>,

        loadingDelayTimer: null as any,
        loadingVisible: false,
      }
    },

    computed: {
      isDraft(): boolean {
        return this.dir.isDraft
      },

      displayRecords(): any[] {
        if (this.isDraft) {
          const dd = this.dir as DraftDirectory
          const all = dd.records ?? []
          const q = this.search.trim().toLowerCase()
          if (!q) return all
          return all.filter((r: any) =>
              Object.values(r.values ?? {}).some(v => String(v ?? '').toLowerCase().includes(q))
          )
        }

        return this.recordsDirId === this.dir.id ? (this.records as any[]) : []
      },

      recordCountText(): string {
        return this.isDraft
            ? `${this.displayRecords.length} зап.`
            : `${this.totalRecords} зап.`
      },

      canAddRecord(): boolean {
        return (this.dir.fields?.length ?? 0) > 0
      },
    },

    mounted() {
      void this.onDirChanged()
    },

    watch: {
      'dir.id': {
        immediate: false,
        handler() {
          void this.onDirChanged()
        },
      },
    },

    methods: {
      resetDataState() {
        this.recordsDirId = null
        this.records = []
        this.totalRecords = 0
        this.recordsError = null
        this.modalErrors = {}

        this.fetching = false
        this.loadingVisible = false
        clearTimeout(this.loadingDelayTimer)
        this.loadingDelayTimer = null

        this.search = ''
        clearTimeout(this.searchTimer)
        this.searchTimer = null

        this.modalOpen = false
        this.modalValues = {}
      },

      async onDirChanged() {
        this.resetDataState()

        if (!this.isDraft) {
          this.fetching = true
          await this.loadRecordsFor(this.dir.id)
        }

        await this.preloadReferenceCaches()
      },

      async loadRecordsFor(dirId: number) {
        const reqId = ++this.recordsReqId

        this.fetching = true
        this.recordsError = null
        this.recordsDirId = dirId

        this.loadingVisible = false
        clearTimeout(this.loadingDelayTimer)
        this.loadingDelayTimer = setTimeout(() => {
          if (reqId === this.recordsReqId) this.loadingVisible = true
        }, 120)

        try {
          const data = await recordsApi.list(dirId, {
            page: 0,
            size: 50,
            search: this.search.trim() || undefined,
          })

          if (reqId !== this.recordsReqId || this.dir.id !== dirId) return

          this.records = data.content
          this.totalRecords = data.page.totalElements
        } catch (e: any) {
          if (reqId !== this.recordsReqId) return
          this.recordsError = e?.message ?? 'Ошибка загрузки записей'
        } finally {
          if (reqId === this.recordsReqId) {
            this.fetching = false
            this.loadingVisible = false
            clearTimeout(this.loadingDelayTimer)
            this.loadingDelayTimer = null
          }
        }
      },

      onSearchInput() {
        if (this.isDraft) return

        clearTimeout(this.searchTimer)
        this.searchTimer = setTimeout(() => {
          void this.loadRecordsFor(this.dir.id)
        }, 300)
      },

      async openModal() {
        this.modalValues = {}
        this.modalErrors = {}
        for (const f of this.dir.fields ?? []) this.modalValues[f.name] = null
        await this.preloadReferenceCaches()
        this.modalOpen = true
      },

      closeModal() {
        this.modalOpen = false
      },

      async submitRecord() {
        if (!this.validateModal()) return

        const values = this.buildNormalizedValues()

        if (this.isDraft) {
          this.store.addDraftRecord(this.dir.id, values)
          this.closeModal()
          return
        }

        try {
          await recordsApi.create(this.dir.id, { values })
          this.closeModal()
          await this.loadRecordsFor(this.dir.id)

          const d = this.store.getById(this.dir.id)
          if (d && !d.isDraft) d.recordCount = this.totalRecords
        } catch (e: any) {
          this.recordsError = e?.message ?? 'Ошибка сохранения записи'
        }
      },

      async deleteRecord(recId: number) {
        if (this.isDraft) {
          this.store.deleteDraftRecord(this.dir.id, recId)
          return
        }

        try {
          await recordsApi.delete(recId)
          await this.loadRecordsFor(this.dir.id)

          const d = this.store.getById(this.dir.id)
          if (d && !d.isDraft) d.recordCount = this.totalRecords
        } catch (e: any) {
          this.recordsError = e?.message ?? 'Ошибка удаления записи'
        }
      },

      getFirstStringFieldName(dirId: number): string | null {
        const refDir = this.store.remotes.find(d => d.id === dirId)
        if (!refDir) return null
        return refDir.fields.find(f => f.type === 'STRING')?.name ?? null
      },

      async preloadReferenceCaches() {
        const refDirIds = (this.dir.fields ?? [])
            .filter(f => f.type === 'DIRECTORY_REFERENCE' && !!f.directoryId)
            .map(f => f.directoryId as number)

        const uniq = Array.from(new Set(refDirIds))
        await Promise.all(
            uniq.map(id => this.refCache.ensureLoaded(id, this.getFirstStringFieldName(id)))
        )
      },

      getRefOptions(dirId: number | null) {
        if (dirId == null) return []
        return this.refCache.optionsByDirId[dirId] ?? []
      },

      cellDisplay(field: any, record: any): string {
        const raw = record?.values?.[field.name]
        if (raw == null) return ''

        if (field.type !== 'DIRECTORY_REFERENCE') return String(raw)

        const refId = Number(raw)
        return this.refCache.labelByDirId[field.directoryId]?.[refId] ?? `#${refId}`
      },

      validateModal(): boolean {
        this.modalErrors = {}
        if (!this.dir) return false

        for (const f of this.dir.fields ?? []) {
          const raw = this.modalValues[f.name]

          if (f.type === 'STRING') {
            const v = String(raw ?? '').trim()
            if (!v) this.modalErrors[f.name] = 'Заполните поле'
            continue
          }

          if (f.type === 'NUMBER') {
            const s = String(raw ?? '').trim()
            if (!s) {
              this.modalErrors[f.name] = 'Введите число'
              continue
            }
            const n = Number(s)
            if (Number.isNaN(n)) {
              this.modalErrors[f.name] = 'Некорректное число'
            }
            continue
          }

          if (f.type === 'DIRECTORY_REFERENCE') {
            if (f.directoryId == null) {
              this.modalErrors[f.name] = 'Не задан связанный справочник'
              continue
            }

            const opts = this.getRefOptions(f.directoryId)
            if (!opts.length) {
              this.modalErrors[f.name] = 'В связанном справочнике нет записей'
              continue
            }

            if (raw == null || raw === '') {
              this.modalErrors[f.name] = 'Выберите значение'
              continue
            }

            const id = Number(raw)
            if (Number.isNaN(id)) {
              this.modalErrors[f.name] = 'Некорректный выбор'
            }
          }
        }

        return Object.keys(this.modalErrors).length === 0
      },

      buildNormalizedValues(): Record<string, unknown> {
        const values: Record<string, unknown> = {}

        for (const f of this.dir.fields ?? []) {
          const raw = this.modalValues[f.name]

          if (f.type === 'STRING') {
            values[f.name] = String(raw ?? '').trim()
            continue
          }

          if (f.type === 'NUMBER') {
            values[f.name] = Number(String(raw ?? '').trim())
            continue
          }

          if (f.type === 'DIRECTORY_REFERENCE') {
            values[f.name] = Number(raw)
            continue
          }
        }

        return values
      },

      getImmediateRefError(f: any): string | null {
        if (f.type !== 'DIRECTORY_REFERENCE') return null

        if (f.directoryId == null) return 'Не задан связанный справочник'

        const opts = this.getRefOptions(f.directoryId)
        if (!opts.length) return 'В связанном справочнике нет записей'

        return null
      },

      getFieldError(f: any): string | null {
        return this.modalErrors[f.name] ?? this.getImmediateRefError(f)
      },

    },
  })
  </script>

  <template>
    <div class="data-toolbar">
      <div class="search-wrapper">
        <span class="search-icon">🔍</span>
        <input
            id="dataSearch"
            name="dataSearch"
            class="input search-input"
            v-model="search"
            placeholder="Поиск..."
            @input="onSearchInput"
        />
      </div>

      <div class="data-right">
        <span class="record-count">{{ recordCountText }}</span>
        <AppButton :disabled="!canAddRecord" @click="openModal">+ Добавить</AppButton>
      </div>

    </div>

    <div class="table-wrapper">
      <table class="data-table">
        <thead>
        <tr>
          <th v-for="f in (dir?.fields ?? [])" :key="f.name">{{ f.name }}</th>
          <th style="width: 50px; padding: 12px 16px"></th>
        </tr>
        </thead>

        <tbody>
        <tr v-if="fetching">
          <td :colspan="(dir?.fields?.length ?? 0) + 1" class="empty-msg">
            <span v-if="loadingVisible">Загрузка...</span>
          </td>
        </tr>

        <tr v-else-if="recordsError">
          <td :colspan="(dir?.fields?.length ?? 0) + 1" class="error-msg">{{ recordsError }}</td>
        </tr>

        <template v-else-if="displayRecords.length">
          <tr v-for="r in displayRecords" :key="r.id">
            <td v-for="f in (dir?.fields ?? [])" :key="f.name">
              {{ cellDisplay(f, r) }}
            </td>
            <td style="text-align:right">
              <button class="icon-btn" type="button" @click="deleteRecord(r.id)">✕</button>
            </td>
          </tr>
        </template>

        <tr v-else>
          <td :colspan="(dir?.fields?.length ?? 0) + 1" class="empty-msg">Нет данных.</td>
        </tr>
        </tbody>
      </table>
    </div>

    <div class="modal-overlay" v-if="modalOpen" @click.self="closeModal">
      <div class="modal">
        <div class="modal-title">Новая запись</div>

        <div class="modal-body">
          <div class="input-group" v-for="f in (dir?.fields ?? [])" :key="f.name">
            <label>{{ f.name }}</label>

            <input
                v-if="f.type === 'STRING'"
                class="input input--modal"
                v-model="modalValues[f.name]"
            />

            <input
                v-else-if="f.type === 'NUMBER'"
                class="input input--modal"
                type="number"
                v-model="modalValues[f.name]"
            />

            <select
                v-else-if="f.type === 'DIRECTORY_REFERENCE'"
                class="input input--modal"
                v-model.number="modalValues[f.name]"
                :disabled="f.directoryId == null || getRefOptions(f.directoryId).length === 0"
            >
              <option :value="null">Выбрать...</option>

              <option
                  v-for="opt in getRefOptions(f.directoryId)"
                  :key="opt.value"
                  :value="opt.value"
              >
                {{ opt.label }}
              </option>
            </select>

            <small v-if="getFieldError(f)" class="field-error">
              {{ getFieldError(f) }}
            </small>

          </div>
        </div>

        <div class="modal-footer">
          <AppButton variant="secondary" @click="closeModal">Отмена</AppButton>
          <AppButton @click="submitRecord">Сохранить</AppButton>
        </div>
      </div>
    </div>
  </template>

  <style scoped>
  .data-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: var(--space-4);
    margin-bottom: var(--space-4);
    border-bottom: 1px solid var(--border);
  }

  .search-wrapper {
    position: relative;
    width: 300px;
  }

  .search-icon {
    position: absolute;
    left: 10px;
    top: 50%;
    transform: translateY(-50%);
    color: var(--muted);
    pointer-events: none;
  }

  .data-right {
    display: flex;
    align-items: center;
    gap: var(--space-3);
  }

  .record-count {
    color: var(--muted);
    font-size: 12px;
  }

  .table-wrapper {
    border: 1px solid var(--border);
    border-radius: var(--radius-sm);
    overflow: hidden;
  }

  .data-table {
    width: 100%;
    border-collapse: collapse;
    min-width: 600px;
    table-layout: fixed;
  }

  .data-table th,
  .data-table td {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .data-table th:last-child,
  .data-table td:last-child {
    width: var(--actions-col-width);
    overflow: visible;
    text-overflow: clip;
    padding: 0 var(--space-2);
  }

  .data-table th {
    text-align: left;
    padding: var(--space-3) var(--space-4);
    background: var(--bg-muted);
    border-bottom: 1px solid var(--border);
    font-weight: 600;
    color: var(--muted);
    font-size: 12px;
  }

  .data-table td {
    padding: var(--space-3) var(--space-4);
    border-bottom: 1px solid var(--border);
    color: var(--text);
    vertical-align: middle;
  }

  .data-table tr:last-child td {
    border-bottom: none;
  }

  .data-table tr:hover td {
    background: var(--bg-hover);
  }

  .input {
    box-sizing: border-box;
    height: 34px;
    padding: var(--space-2) var(--space-3);
    border: 1px solid var(--input-border);
    border-radius: var(--radius-sm);
    font-size: 14px;
    font-family: inherit;
    background: var(--bg-card);
    color: var(--text);
  }

  .input:focus {
    border-color: var(--primary);
    outline: 2px solid var(--primary-focus);
  }

  .search-input {
    width: 100%;
    padding-left: 34px;
  }

  .data-table td.empty-msg {
    padding: var(--space-10);
    text-align: center;
    color: var(--muted);
  }

  .data-table td.error-msg {
    padding: var(--space-3) var(--space-4);
    color: var(--danger);
    font-size: 13px;
  }

  .field-error {
    margin-top: 4px;
    font-size: 12px;
    color: var(--danger);
  }

  .icon-btn {
    border: none;
    background: var(--icon-btn-bg);
    border-radius: var(--radius-sm);
    cursor: pointer;
    padding: 4px 8px;
  }

  .icon-btn:hover {
    background: var(--icon-btn-bg-hover);
  }

  .modal-overlay {
    position: fixed;
    inset: 0;
    background: var(--overlay);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;
  }

  .modal {
    background: var(--bg-card);
    width: 420px;
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow-modal);
    padding: var(--space-6);
  }

  .modal-title {
    font-size: 18px;
    font-weight: 600;
    margin-bottom: var(--space-4);
    color: var(--text);
  }

  .modal-body {
    max-height: 55vh;
    overflow: auto;
  }

  .input-group {
    display: flex;
    flex-direction: column;
    gap: 6px;
    margin-bottom: 14px;
  }

  .input-group label {
    font-size: 13px;
    font-weight: 500;
    color: var(--muted);
  }

  .input--modal {
    border-radius: var(--radius-sm);
    width: 100%;
    max-width: 100%;
    min-width: 0;
    display: block;
  }

  select.input--modal {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .modal-footer {
    margin-top: var(--space-4);
    padding-top: var(--space-4);
    border-top: 1px solid var(--border);
    display: flex;
    justify-content: flex-end;
    gap: var(--space-3);
  }
  </style>