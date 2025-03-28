import type { CommonResponse } from '@/types/common';
import type { GetMyShareVO, UpdateMyShareDTO } from '@/types/share';
import request from '@/utils/request';
import type { FileShareDTO, FileShareVO, GetShareFileDTO, GetShareFileVO } from '@/types/file';


/**
 * 获取分享的文件
 * @param data
 */
export function reqShareFileDTO(params: GetShareFileDTO) {
    const { id, ...rest } = params;
    return request<any, CommonResponse<GetShareFileVO>>({
        url: `/share/${id}`,
        method: 'GET',
        params: rest,
    });
}

/**
 * 从分享的文件下载
 * @param fileIds
 * @returns
 */
export function reqDownloadShareFiles(fileIds: string[]) {
    return request<any, Blob>({
        url: `/share/download`,
        method: 'POST',
        data: { fileIds },
        responseType: 'blob',
    });
}

/**
 * 分享文件
 * @param data
 */
export function reqShareFile(data: FileShareDTO) {
    return request<any, CommonResponse<FileShareVO>>({
        url: '/shareFile',
        method: 'POST',
        data,
    });
}

/**
 * 获取我的分享列表
 * @returns
 */
export function reqGetMyShare() {
    return request<any, CommonResponse<GetMyShareVO>>({
        url: `/shareFile`,
        method: 'GET',
    });
}

/**
 * 删除分享
 * @returns
 */
export function reqDeleteMyShare(id: string) {
    return request<any, CommonResponse<any>>({
        url: `/shareFile`,
        method: 'DELETE',
        params: { id },
    });
}

/**
 * 修改分享配置
 * @returns
 */
export function reqUpdateMyShareOptions(data: UpdateMyShareDTO) {
    return request<any, CommonResponse<any>>({
        url: `/shareFile/options`,
        method: 'PUT',
        data,
    });
}

/**
 * 修改分享文件
 * @returns
 */
export function reqUpdateMyShareFiles(fileIds: string[], shareId: string) {
    return request<any, CommonResponse<any>>({
        url: `/shareFile/files`,
        method: 'PUT',
        data: { fileIds, shareId },
    });
}
