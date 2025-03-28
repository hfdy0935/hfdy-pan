import type { FileItem } from '@arco-design/web-vue';
import type { VNode } from 'vue';
import type { IItemCategory } from './file';

/**
 * main页面修改用户信息请求体
 */
export interface UpdateUserInfoDTO {
  nickname: string;
  avatar: FileItem;
}

/**
 * main页面修改用户信息的响应体
 */
export interface UpdateUserInfoVO {
  nickname: string;
  avatar: string;
}


/**
 * 主界面修改密码的表单数据
 */
export interface IUpdatePassword {
  password: string;
  newPassword: string;
}

/**
 * 菜单类型
 */
export interface IMenuItem {
  id: string;
  name: string;
  icon?: VNode;
  path: string;
  isShow: boolean;
  children?: IMenuItem[];
  tips?: string;
  category?: IItemCategory
}
