import type { IItemCategory, IItemMediaType } from '@/types/file';

/**
 * 单个回收项
 */
export interface IRecycle {
    id: string;
    name: string;
    category: IItemCategory;
    mediaType: IItemMediaType;
    createTime: string;
    deleteTime: string;
    level: string;
    pid: string;
}
