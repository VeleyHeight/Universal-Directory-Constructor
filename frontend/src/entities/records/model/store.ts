import {defineStore} from "pinia";
import {recordsApi} from "@/entities/records/api";

type Option = { value: number; label: string }

export const useRecCacheStore = defineStore('recStore', {
    state: () => ({
        labelByDirId: {} as Record<number, Record<number, string>>,
        optionsByDirId: {} as Record<number, Option[]>,
        loadingByDirId: {} as Record<number, boolean>,
    }),

    actions: {
        async ensureLoaded(refDirId: number, firstStringFieldName: string | null) {
            if (this.optionsByDirId[refDirId]) return
            if (this.loadingByDirId[refDirId]) return

            this.loadingByDirId[refDirId] = true
            try {
                const page = await recordsApi.list(refDirId, { page: 0, size: 200 })

                const byId: Record<number, string> = {}
                const opts: Option[] = []

                for (const r of page.content) {
                    const rid = Number(r.id)
                    let label = `#${rid}`

                    if (firstStringFieldName) {
                        const v = (r.values as any)?.[firstStringFieldName]
                        if (typeof v === 'string' && v.trim()) label = v
                    }

                    byId[rid] = label
                    opts.push({ value: rid, label })
                }

                this.labelByDirId[refDirId] = byId
                this.optionsByDirId[refDirId] = opts
            } finally {
                this.loadingByDirId[refDirId] = false
            }
        },
    },
})