import type { OrderType } from '@/enums';

export type FileCategory = 'all' | 'video' | 'audio' | 'image' | 'docs' | 'others';

/**
 * 一切皆文件
 */
export interface IFileItem {
  id: string;
  name: string;
  updateTime: string;
  size?: number | string; // 单位是B
  pid?: string;
  category: FileCategory;
  /**
   * 单个的类型
   */
  mediaType: 'video'
    | 'audio'
    | 'image'
    | 'folder'
    | 'zip'
    | 'text'
    | 'ppt'
    | 'pptx'
    | 'pdf'
    | 'xlsx'
    | 'csv'
    | 'doc'
    | 'docx'
    | 'code'
    | 'md'
    | 'unknown';
}

/**
 * 请求文件列表请求体
 */
export interface FileListDTO {
  /**
   * 请求时大的分类, others包括zip、folder、others
   */
  category: FileCategory;
  /**
   * 页码
   */
  page: number;
  /**
   * 每页数量
   */
  pageSize: number;
  /**
   * 父文件夹id，顶层为空
   */
  pid?: string;
  /**
   * 关键词
   */
  keyword?: string;
  /**
   * 是否按照更新时间排序
   */
  orderByUpdateTime?: OrderType;
  /**
   * 是否按照文件大小排序
   */
  orderBySize?: OrderType;
}

/**
 * 文件路径面包屑导航中的记录
 */
export type IFileBreadcrumbItem = IFileItem[];

/**
 * 请求文件列表响应体
 */
export interface FileListVO extends FileListDTO {
  total: number;
  parent: IFileItem; // 父文件夹（如果有）
  records: IFileItem[];
}

/**
 * 表格中显示的文件列表，多了一个key用于多选，值等于文件id
 */
export type TableFileList = (IFileItem & { key: string })[]


/**
 * 创建文件夹请求体
 */
export interface ReqCreateFolder {
  name: string; // 文件夹名
  pid: string | null;// 父文件夹id，null表示在顶级创建
}

/**
 * 修改文件名请求体
 */
export interface ReqRenameFile {
  id: string;
  name: string;
}


/**
 * 文件详情响应体
 */
export interface FileDetailVO extends IFileItem {
  createTime: string;
  deleteTime?: string;
  childNum?: number;
  username: string;
}
