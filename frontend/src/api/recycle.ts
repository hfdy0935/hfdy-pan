import request from '@/utils/request';
import type { CommonResponse } from '@/types/common';
import type { IRecycle } from '@/types/recycle';

/**
 * 获取回收站文件信息
 */
export function reqGetRecycleFiles() {
    return request<null, CommonResponse<IRecycle[]>>({
        url: '/recycle',
        method: 'GET',
    });
}

/**
 * 彻底删除回收站的某个文件
 */
export function reqDelRecycleFile(ids: string[], complete: boolean) {
    return request<null, CommonResponse<null>>({
        url: '/recycle',
        method: 'DELETE',
        data: {
            ids,
            complete,
        },
    });
}

/**
 * 恢复回收站的信息
 * @param ids
 * @param pid
 */
export function reqRecoverFiles(ids: string[], pid: string) {
    return request<null, CommonResponse<null>>({
        url: '/recycle/recover',
        method: 'PUT',
        data: { ids, pid },
    });
}
