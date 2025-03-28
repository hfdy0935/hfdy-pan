// 入口

/**
 * 发送邮箱验证码
 */
export interface SendEmailCheckCodeDTO {
  /**
   * 邮箱
   */
  email: string;
  /**
   * 发送时机，0注册、1修改密码
   */
  type: 0 | 1;
}

/**
 * 登录请求体
 */
export interface LoginDTO {
  email: string;
  password: string;
  /**
   * 图形验证码
   */
  captcha: string;
}

/**
 * 登录响应体
 */
export interface LoginVO extends UserInfoVO {
  accessToken: string;
  refreshToken: string;
}

/**
 * 重置密码请求体
 */
export interface UpdatePasswordDTO extends LoginDTO {
  emailCheckCode: string;
}

/**
 * 修改密码填写表单时的数据类型，多了一个重复密码
 */
export interface IUpdatePassword extends UpdatePasswordDTO {
  confirmPassword: string;
}

/**
 * 注册请求体
 */
export interface RegisterDTO extends UpdatePasswordDTO {
  nickname: string;
}

/**
 * 注册的响应
 */
export type RegisterVO = LoginVO;


/**
 * 获取用户信息响应体
 */
export interface UserInfoVO {
  userId: string
  nickName: string
  avatar: string
  /**
   * 已用空间大小，byte
   */
  usedSpace: number
  /**
   * 总空间大小，byte
   */
  totalSpace: number
  /**
   * 是不是vip
   */
  isVip: 1 | 0
  /**
   * 文件夹最大深度
   */
  maxFolderLevel: number
  /**
   * isAdmin
   */
  isAdmin: number
}