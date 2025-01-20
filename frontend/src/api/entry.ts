import type { CommonResponse } from '@/types/common';
import type {
  LoginDTO,
  RegisterDTO,
  SendEmailCheckCodeDTO,
  UpdatePasswordDTO,
  LoginVO,
  RegisterVO
} from '@/types/entry';
import request from '@/utils/request';
import type { AxiosResponse } from 'axios';

/**
 * 获取图形验证码
 * @returns
 */
export function reqGetCaptcha() {
  return request<null, Blob>({
    url: '/captcha',
    method: 'GET',
    responseType: 'blob'
  });
}

/**
 * 获取邮箱验证码过期时间
 * @returns
 */
export const reqGetEmailCheckCodeExpires = () =>
  request<null, CommonResponse<number>>({
    url: '/emailCheckCodeExpires',
    method: 'GET'
  });

/**
 * 发送邮箱验证码
 * @returns
 */
export function reqSendEmailCheckCode(data: SendEmailCheckCodeDTO) {
  return request<string, CommonResponse<null>>({
    url: '/sendEmailCheckCode',
    method: 'POST',
    data
  });
}

/**
 * 登录
 * @param data
 * @returns
 */
export function reqLogin(data: LoginDTO) {
  return request<LoginDTO, CommonResponse<LoginVO>>({
    url: '/login',
    method: 'POST',
    data
  });
}

/**
 * 刷新token
 * @param refreshToken
 * @returns
 */
export function reqRefreshToken() {
  return request<string, AxiosResponse<CommonResponse<null>>>({
    url: '/refreshToken',
    method: 'POST'
  });
}

/**
 * 重置密码
 * @param data
 * @returns
 */
export function reqUpdatePassword(data: UpdatePasswordDTO) {
  return request<UpdatePasswordDTO, CommonResponse<null>>({
    url: '/updatePassword',
    method: 'PUT',
    data
  });
}

/**
 * 注册
 * @param data
 * @returns
 */
export function reqRegister(data: RegisterDTO) {
  return request<RegisterDTO, CommonResponse<RegisterVO>>({
    url: '/register',
    method: 'POST',
    data
  });
}
