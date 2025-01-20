/**
 * 验证邮箱是否有效s
 * @param email
 * @returns
 */
export const validateEmail = (email: string) =>
  /^[A-Za-z0-9\u4e00-\u9fa5]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/.test(email);

export const getRules = (password: string) => {
  return {
    email: [
      { required: true, message: '邮箱不能为空' },
      {
        validator(value: string, cb: (msg: string) => void) {
          if (!validateEmail(value)) {
            cb('邮箱格式不正确');
          }
        }
      }
    ],
    password: [
      { required: true, message: '密码不能为空' },
      {
        validator(value: string, cb: (msg: string) => void) {
          if (!/^[a-zA-Z0-9-_@#]{6,18}$/.test(value)) {
            cb(
              '密码至少6位，最多18位，只能由数字、大小写字母、下划线、短横线、@和#组成'
            );
          } else if (/^[0-9]{1,}$/.test(value)) {
            cb('密码不能是纯数字');
          }
        }
      }
    ],
    captcha: [{ required: true, message: '验证码不能为空' }],
    emailCheckCode: [{ required: true, message: '密码不能为空' }],
    confirmPassword: [
      { required: true, message: '密码不能为空' },
      {
        validator(value: string, cb: (msg: string) => void) {
          if (password !== value) cb('两次密码不一致');
        }
      }
    ],
    nickname: [
      { required: true, message: '昵称不能为空' },
      { mexLength: 15, message: '昵称最多15个字符' }
    ]
  };
};
