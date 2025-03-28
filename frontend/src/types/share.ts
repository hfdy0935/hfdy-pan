export interface IMyShareItem extends UpdateMyShareDTO {
    createTime: string;
    visitNum: number;
    itemNum: number;
    fileIds: string[];
}

/**
 * 获取我的分享响应体
 */
export type GetMyShareVO = IMyShareItem[];

/**
 * 分享基础配置
 */
export interface IMyShareConfig {
    expire: number;
    pwd: string;
}
/**
 * 修改分享请求体
 */
export interface UpdateMyShareDTO extends IMyShareConfig {
    id: string;
}
