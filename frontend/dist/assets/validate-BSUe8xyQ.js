const s=r=>/^[A-Za-z0-9\u4e00-\u9fa5]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/.test(r),t=r=>({email:[{required:!0,message:"邮箱不能为空"},{validator(e,a){s(e)||a("邮箱格式不正确")}}],password:[{required:!0,message:"密码不能为空"},{validator(e,a){/^[a-zA-Z0-9-_@#]{6,18}$/.test(e)?/^[0-9]{1,}$/.test(e)&&a("密码不能是纯数字"):a("密码至少6位，最多18位，只能由数字、大小写字母、下划线、短横线、@和#组成")}}],captcha:[{required:!0,message:"验证码不能为空"}],emailCheckCode:[{required:!0,message:"密码不能为空"}],confirmPassword:[{required:!0,message:"密码不能为空"},{validator(e,a){r!==e&&a("两次密码不一致")}}],nickname:[{required:!0,message:"昵称不能为空"},{mexLength:15,message:"昵称最多15个字符"}]});export{t as g};
