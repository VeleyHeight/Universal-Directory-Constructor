export interface RecordDTO {
    id: number | null,
    values: Record<string, unknown>
}

export interface PageInfo {
    size: number
    number: number
    totalElements: number
    totalPages: number
}

export interface PageResponse<T> {
    content: T[]
    page: PageInfo
}

export interface DraftRecord {
    id: number
    values: Record<string, unknown>
}