import type { OrderType } from '@/enums';
import type { VNode } from 'vue';

export type IItemCategory = 'all' | 'video' | 'audio' | 'image' | 'docs' | 'others';
export type IItemMediaType = 'video'
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
  | 'unknown'



/**
 * 转码状态枚举
 */
export enum TransCodeStatusEnum {
  NO_NEED = 0,
  ING = 1,
  OK = 2,
  ERR = 3
}


export interface ITreeData extends FolderLevelInfoVO {
  icon: () => VNode;
  disabled: boolean;
  children: ITreeData[]
}
/**
 * 单个文件/文件夹
 */
export interface IFileItem {
  id: string;
  name: string;
  updateTime: string;
  size?: number | string; // 单位是B
  pid: string;
  level: string
  category: IItemCategory
  mediaType: IItemMediaType
  /**
   * 转码状态
   */
  status: TransCodeStatusEnum
  /**
   * 歌词url
   */
  lyricPath: string;
}

/**
 * 请求文件列表请求体
 */
export interface ItemListDTO {
  /**
   * 请求时大的分类
   */
  category: IItemCategory;
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
   * 搜索关键词
   */
  keyword?: string;
  /**
   * 是否按照更新时间排序
   */
  orderByUpdateTime: OrderType;
  /**
   * 是否按照文件大小排序
   */
  orderBySize: OrderType;
}

/**
 * 文件路径面包屑导航中的记录
 */
export type IFileBreadcrumbItem = IFileItem[];

/**
 * 请求文件列表响应体
 */
export interface ItemListVO extends Omit<ItemListDTO, 'pid'> {
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
export interface CreateFolderDTO {
  name: string; // 文件夹名
  pid: string;// 父文件夹id，''表示没有父文件夹
}

/**
 * 修改文件名请求体
 */
export interface RenameFileDTO {
  id: string;
  name: string;
}


/**
 * 文件详情响应体
 */
export interface FileDetailVO extends IFileItem {
  createTime: string;
  deleteTime: string;
  childNum: number;
  username: string;
}


/**
 * 请求移动文件的请求体
 */
export interface MoveItemDTO {
  idList: string[]
  /**
   * 空表示移动到第一层
   */
  targetId: string
  op: 'copy' | 'cut'
}


/* ---------------------------------- 文件上传 ---------------------------------- */
/**
 * 删除分块请求体
 */
export interface DeleteChunksDTO {
  /**
   * 文件md5
   */
  md5: string
  /**
   * 要删除的分块索引数组
   */
  chunkIndexes: number[]
  /**
   * 文件分类
   */
  category: IItemCategory
}


/**
 * 合并文件请求体
 */
export interface MergeFileChunksDTO {
  filename: string
  totalChunkNum: number
  md5: string
  totalSize: number
  pid: string
  category: IItemCategory
  mediaType: IItemMediaType
}

/**
 * 获取文件是否已上传和已上传的分片索引
 */
export interface GetUploadedChunkIndexes {
  uploaded: boolean
  indexes: number[]
}

/* ---------------------------------- 文件分享 ---------------------------------- */
export interface FileShareDTO {
  ids: string[]
  expire?: number
  pwd?: string
  pid?: string
}

export interface FileShareVO {
  shareId: string
  pwd: string
}

export interface GetShareFileDTO {
  id: string
  pwd?: string
  pid?: string
}

export interface GetShareFileVO {
  id: string
  username: string
  avatar: string
  createTime: string
  records: IFileItem[]
  parent: IFileItem
  visitNum: number
}

/**
 * 获取文件夹层级信息响应体
 */
export interface FolderLevelInfoVO {
  id: string
  name: string
  children?: FolderLevelInfoVO[]
  level: string
  status: TransCodeStatusEnum
  category: IItemCategory
  mediaType: IItemMediaType
  lyricPath: string
}

/**
 * 把分享的文件保存到我的网盘请求体
 */
export interface SaveShareToMyPanDTO {
  srcIds: string[]
  to: string
  shareId: string
}

