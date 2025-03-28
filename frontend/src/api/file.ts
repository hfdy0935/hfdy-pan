import type {
    DeleteChunksDTO,
    FileDetailVO,
    ItemListDTO,
    ItemListVO,
    IFileItem,
    MergeFileChunksDTO,
    MoveItemDTO,
    CreateFolderDTO,
    RenameFileDTO,
    GetShareFileVO,
    GetShareFileDTO,
    GetUploadedChunkIndexes,
    FolderLevelInfoVO,
    SaveShareToMyPanDTO,
} from '@/types/file';
import request from '@/utils/request';
import type { CommonResponse } from '@/types/common';
import type { GenericAbortSignal } from 'axios';
import type { IUploadChunks } from '@/stores/modules/file';

/* ------------------------------------ 查 ----------------------------------- */
/**
 * 请求文件列表
 * @param params
 */
export function reqFileList(params: ItemListDTO) {
    return request<ItemListDTO, CommonResponse<ItemListVO>>({
        url: '/file/list',
        method: 'GET',
        params,
    });
}

/**
 * 手动请求视频转码
 * @param id
 */
export function reqTransVideoCode(id: string) {
    return request<any, CommonResponse<any>>({
        url: '/file/transVideoCode',
        method: 'POST',
        params: { id },
    });
}

/**
 * 请求文件状态
 * @param id
 * @returns
 */
export function reqFileStatus(ids: string[]) {
    return request<
        any,
        CommonResponse<
            {
                id: string;
                status: IFileItem['status'];
            }[]
        >
    >({
        url: '/file/transCodeStatus',
        method: 'POST',
        data: { ids },
    });
}

/**
 * 上传歌词
 * @param id
 * @returns
 */
export function reqUploadLyric(data: FormData) {
    return request<any, CommonResponse<any>>({
        url: '/file/uploadLyric',
        method: 'POST',
        data,
    });
}

/**
 * 获取文件详情
 * @param id
 */
export function reqFileDetail(id: string) {
    return request<string, CommonResponse<FileDetailVO>>({
        url: '/file/detail',
        method: 'GET',
        params: { id },
    });
}

/* ----------------------------------- 增删改 ----------------------------------- */

/**
 * 创建文件夹
 * @param data
 */
export function reqCreateFolder(data: CreateFolderDTO) {
    return request<CreateFolderDTO, CommonResponse<null>>({
        url: '/file/folder',
        method: 'post',
        data,
    });
}

/**
 * 修改文件名
 * @param data
 */
export function reqRenameItem(data: RenameFileDTO) {
    return request<RenameFileDTO, CommonResponse<null>>({
        url: '/file/rename',
        method: 'PUT',
        data,
    });
}

/**
 * 删除文件/文件夹
 * @param ids
 * @param complete
 */
export function reqDeleteFile(ids: string[], complete: boolean) {
    return request<string, CommonResponse<null>>({
        url: '/file',
        method: 'DELETE',
        data: { ids, complete },
    });
}

/**
 * 移动文件/文件夹
 * @param data
 * @returns
 */
export function reqMoveItem(data: MoveItemDTO) {
    return request<any, CommonResponse<any>>({
        url: '/file/move',
        method: 'PUT',
        data,
    });
}

/**
 * 上传文件分片
 * @param data
 */
export function reqUploadFileChunk(data: FormData, signal: GenericAbortSignal) {
    return request<any, CommonResponse<any>>({
        url: '/file/uploadChunk',
        method: 'POST',
        data,
        signal,
        headers: {
            'Content-Type': 'multipart/form-data',
        },
    });
}

/**
 * 删除已上传的分块
 * @param data
 * @returns
 */
export function reqDeleteChunks(data: DeleteChunksDTO) {
    return request<any, CommonResponse<any>>({
        url: '/file/deleteChunks',
        method: 'DELETE',
        data,
    });
}

/**
 * 获取已上传分块的索引
 * @param md5
 * @param uid
 * @returns
 */
export function reqUploadedChunkIndexes(md5: DeleteChunksDTO['md5']) {
    return request<any, CommonResponse<GetUploadedChunkIndexes>>({
        url: '/file/uploadedChunkIndexes',
        method: 'GET',
        params: { md5 },
    });
}

/**
 * 秒传
 * @param item
 * @returns
 */
export function reqInstantUpload(data: IUploadChunks) {
    return request<any, CommonResponse<any>>({
        url: '/file/instantUpload',
        method: 'POST',
        data: {
            pid: data.pid,
            md5: data.md5,
        },
    });
}

/**
 * 合并分块
 * @param md5
 * @returns
 */
export function reqMergeFileChunks(data: MergeFileChunksDTO) {
    return request<any, CommonResponse<any>>({
        url: '/file/mergeChunks',
        method: 'POST',
        data,
    });
}

/**
 * 直接上传小文件
 * @param data
 * @returns
 */
export function reqUplaodFile(data: FormData) {
    return request<any, CommonResponse<any>>({
        url: 'file/uploadFile',
        method: 'POST',
        data,
        headers: {
            'Content-Type': 'multipart/form-data',
        },
    });
}

/**
 * 预览文件
 * @param fileId
 */
export function reqPreviewFile(fileId: string, control: AbortController, shareId?: string) {
    return request<any, Blob>({
        url: `/file/preview/${fileId}`,
        method: 'GET',
        headers: { shareId: shareId ?? '' },
        responseType: 'blob',
        signal: control.signal,
    });
}


/**
 * 下载文件
 * @param fileIds
 * @returns
 */
export function reqDownloadFiles(fileIds: string[]) {
    return request<any, Blob>({
        url: `/file/download`,
        method: 'POST',
        data: { fileIds },
        responseType: 'blob',
    });
}

/**
 * 获取文件夹层级信息，用于转存分享时获取用户自己的文件夹层级
 * @param fileIds
 * @returns
 */
export function reqFolderLevelInfo() {
    return request<any, CommonResponse<FolderLevelInfoVO[]>>({
        url: `/file/folderLevelInfo`,
        method: 'GET',
    });
}

/**
 * 转存到自己的网盘
 * @param fileIds
 * @returns
 */
export function reqSaveToMyPan(data: SaveShareToMyPanDTO) {
    return request<any, CommonResponse<any>>({
        url: `/file/saveShareToMyPan`,
        method: 'POST',
        data,
    });
}
