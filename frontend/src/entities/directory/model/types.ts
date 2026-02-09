import type {DraftRecord} from "@/entities/records/model/types.ts";


export type FieldsType = 'STRING' | 'NUMBER' | 'DIRECTORY_REFERENCE';

export interface DirectoryFieldDto {
    name: string;
    type: FieldsType;
    directoryId: number | null;
}

export interface DirectoryDto {
    id: number;
    name: string;
    code: string;
    fields: DirectoryFieldDto[];
}

export interface DirectoryCreateDto {
    name: string;
    fields: DirectoryFieldDto[];
}

export interface DirectoryAndCount {
    id: number;
    name: string;
    code: string;
    fields: DirectoryFieldDto[];
    fieldCount: number;
    recordCount: number
}

export interface DraftDirectory {
    isDraft: true;
    id: number;
    name: string;
    code: string;
    fields: DirectoryFieldDto[];
    fieldCount: number;
    recordCount: number;
    records: DraftRecord[]
}

export interface RemoteDirectory extends DirectoryAndCount {
    isDraft: false
}

export type DirectoryListItem = RemoteDirectory | DraftDirectory