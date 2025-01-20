import type { CommonResponse } from '@/types/common';
import type { IUpdatePassword, UpdateUserInfoVO } from '@/types/home';
import request from '@/utils/request';

/**
 * 修改用户信息
 * @param data 昵称和头像组成的表单
 * @returns
 */
export const reqUpdateUserInfo = (data: FormData) =>
  request<FormData, Required<CommonResponse<UpdateUserInfoVO>>>({
    url: '/updateUserInfo',
    method: 'post',
    data
  });

/**
 * 登录之后在主页修改密码
 * @returns
 * @param data
 */
export const reqHomeUpdatePassword = (data: IUpdatePassword) =>
  request<string, CommonResponse<null>>({
    url: '/updateHomePassword',
    method: 'post',
    data
  });
