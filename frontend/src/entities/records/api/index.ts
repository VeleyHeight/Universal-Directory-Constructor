import type {PageResponse, RecordDTO} from "@/entities/records/model/types.ts";
import {http} from "@/shared/api/http.ts";

export const recordsApi = {
    async list(directoryId: number, params?: { page?: number; size?: number; search?: string }) {
        const { data } = await http.get<PageResponse<RecordDTO>>(`/api/records/${directoryId}`, { params })
        return data
    },

    async create(directoryId: number, payload: { values: Record<string, unknown> }) {
        const { data } = await http.post<RecordDTO>(`/api/records/${directoryId}`, payload)
        return data
    },

    async delete(recordId: number) {
        await http.delete(`/api/records/${recordId}`)
    },
}