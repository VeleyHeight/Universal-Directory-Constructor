import { defineStore } from 'pinia'
import type {
    DirectoryAndCount,
    DirectoryCreateDto,
    DirectoryDto,
    DirectoryFieldDto,
    DirectoryListItem,
    DraftDirectory, RemoteDirectory,
} from './types'
import { directoriesApi } from '@/entities/directory/api'
import {recordsApi} from "@/entities/records/api";

export const useDirectoryStore = defineStore('directories', {
    state: () => ({
        items: [] as DirectoryListItem[],
        loading: false,
        loaded: false,
        error: null as string | null,
        _fetchPromise: null as Promise<void> | null,
    }),

    getters: {
        getById: (state) => (id: number) => state.items.find(d => d.id === id) ?? null,
        drafts: (state) => state.items.filter((d): d is DraftDirectory => d.isDraft),
        remotes: (state) => state.items.filter((d): d is RemoteDirectory => !d.isDraft),
    },

    actions: {
        async ensureLoaded() {
            if (this.loaded) return
            if (this._fetchPromise) return this._fetchPromise

            this._fetchPromise = this.fetchAll()
                .finally(() => { this._fetchPromise = null })

            return this._fetchPromise
        },

        async fetchAll() {
            this.loading = true
            this.error = null
            try {
                const remote = await directoriesApi.getAll()

                const drafts = this.items.filter((d): d is DraftDirectory => d.isDraft)
                const remotes: RemoteDirectory[] = remote.map(d => ({ ...d, isDraft: false as const }))

                this.items = [...drafts, ...remotes]
                this.loaded = true
            } catch (e: any) {
                this.error = e?.message ?? 'Ошибка'
                throw e
            } finally {
                this.loading = false
            }
        },

        createDraft(): DraftDirectory {
            const id = -Date.now()
            const draft: DraftDirectory = {
                isDraft: true,
                id,
                name: '',
                code: `new_${Math.abs(id)}`,
                fields: [],
                fieldCount: 0,
                recordCount: 0,
                records: [],
            }
            this.items.push(draft)
            return draft
        },


        setName(id: number, name: string) {
            const d = this.getById(id)
            if (!d) return
            d.name = name
        },

        addField(id: number, field: DirectoryFieldDto) {
            const d = this.getById(id)
            if (!d) return
            d.fields.push(field)
            d.fieldCount = d.fields.length
        },

        removeField(id: number, idx: number) {
            const d = this.getById(id)
            if (!d) return
            d.fields.splice(idx, 1)
            d.fieldCount = d.fields.length
        },

        async save(directoryId: number): Promise<{ savedId: number } | null> {
            this.loading = true
            this.error = null

            try {
                const d = this.getById(directoryId)

                if (!d) return null

                if (d.isDraft) {

                    const payload: DirectoryCreateDto = {
                        name: d.name.trim() || 'Без названия',
                        fields: d.fields,
                    }

                    const created = await directoriesApi.create(payload)

                    const savedId = created.id

                    await Promise.all(d.records.map(r => recordsApi.create(savedId, { values: r.values })))

                    this.items = this.items.filter(x => x.id !== directoryId)
                    await this.fetchAll()

                    return {savedId}
                }

                const putPayload: DirectoryDto = {
                    id: d.id,
                    name: (d.name ?? '').trim(),
                    code: d.code,
                    fields: d.fields,
                }

                const updated: DirectoryDto = await directoriesApi.update(d.id, putPayload)

                const next: DirectoryAndCount = {
                    id: updated.id,
                    name: updated.name,
                    code: updated.code,
                    fields: updated.fields,
                    fieldCount: updated.fields.length,
                    recordCount: d.recordCount,
                }
                const idx = this.items.findIndex(x => x.id === d.id)
                if (idx !== -1) this.items[idx] = {...next, isDraft: false}

                return {savedId: d.id}
            }
            catch (e: any) {
                this.error = e?.response?.data?.message ?? e?.message ?? 'Ошибка'
                throw e
            }
            finally {
                this.loading = false
            }
        },

        addDraftRecord(directoryId: number, values: Record<string, unknown>) {
            const d = this.getById(directoryId)
            if (!d || !d.isDraft) return

            d.records.push({ id: Date.now(), values })
            d.recordCount = d.records.length
        },

        deleteDraftRecord(directoryId: number, recordId: number) {
            const d = this.getById(directoryId)
            if (!d || !d.isDraft) return

            d.records = d.records.filter(r => r.id !== recordId)
            d.recordCount = d.records.length
        },

        clearError() { this.error = null }
    },
})