import { http } from '@/shared/api/http.ts'
import type { DirectoryAndCount, DirectoryCreateDto, DirectoryDto } from "@/entities/directory/model/types.ts";

export const directoriesApi = {
    async getAll() : Promise<DirectoryAndCount[]> {
        const { data } = await http.get<DirectoryAndCount[]>('/api/directories')
        return data
    },
    async create(payload: DirectoryCreateDto) : Promise<DirectoryDto> {
        const {data} = await http.post<DirectoryDto>('/api/directories', payload)
        return data
    },
    async update(id: number, payload: DirectoryDto) : Promise<DirectoryDto> {
        const { data } = await http.put<DirectoryDto>(`/api/directories/${id}`, payload)
        return data
    }
}