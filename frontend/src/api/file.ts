import type { FileDetailVO, FileListDTO, FileListVO, IFileItem, ReqCreateFolder, ReqRenameFile } from '@/types/file';
import request from '@/utils/request';
import type { CommonResponse } from '@/types/common';

/**
 * 请求文件列表
 * @param params
 */
export function reqFileList(params: FileListDTO) {
  return request<FileListDTO, CommonResponse<FileListVO>>({
    url: '/file/list',
    method: 'GET',
    params
  });
}


/**
 * 创建文件夹
 * @param data
 */
export function reqCreateFolder(data: ReqCreateFolder) {
  return request<ReqCreateFolder, CommonResponse<null>>({
    url: '/file/folder',
    method: 'post',
    data
  });
}


/**
 * 创建文件夹之前获取可以创建的文件夹名，防止重复
 * @param pid
 */
export function reqNewFolderName(pid: string) {
  return request<string, CommonResponse<string>>({
    url: '/file/new-folder-name',
    method: 'get',
    params: { pid }
  });
}


/**
 * 发送请求修改文件名
 * @param data
 */
export function reqRenameFile(data: ReqRenameFile) {
  return request<ReqRenameFile, CommonResponse<null>>({
    url: '/file/rename',
    method: 'PUT',
    data
  });
}

/**
 * 删除文件/文件夹
 * @param id
 */
export function reqDeleteFile(id: string) {
  return request<string, CommonResponse<null>>({
    url: '/file',
    method: 'DELETE',
    params: { id }
  });
}


/**
 * 获取文件详情，比列表中的更详细
 * @param id
 */
export function reqFileDetail(id: string) {
  return request<string, CommonResponse<FileDetailVO>>({
    url: '/file/detail',
    method: 'GET',
    params: { id }
  });
}
