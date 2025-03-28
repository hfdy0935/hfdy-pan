import { reqRefreshToken } from '@/api/entry';
import type { CommonResponse } from '@/types/common';
import axios, { AxiosHeaders } from 'axios';
import { isRefreshTokenConfig } from './is';
import { Message } from '@arco-design/web-vue';
import { useUserStore } from '@/stores/modules/user';

const request = axios.create({
  baseURL: import.meta.env.VITE_BACKEND_BASE_URL,
  // timeout: 5000,
});

request.interceptors.request.use(config => {
  const { userInfo } = storeToRefs(useUserStore());
  // 优先带accessToken
  config.headers.Authorization = userInfo.value.accessToken || userInfo.value.refreshToken;
  return config;
});

request.interceptors.response.use(
  async res => {
    const { userInfo } = storeToRefs(useUserStore());
    const { clear } = useUserStore();
    if (res.data.code === 401) {
      // refreshToken过期
      if (isRefreshTokenConfig(res.config)) {
        clear();
        Message.warning('登录已过期')
        setTimeout(() => window.open('/entry', '_self'), 1000)
      } else {
        // accessToken过期
        userInfo.value.accessToken = '';
        const refreshRes = await reqRefreshToken();
        if (refreshRes.data.code === 200) {
          const newAccessToken = (refreshRes.headers as AxiosHeaders).get('Authorization') as string;
          userInfo.value.accessToken = newAccessToken;
          // 再继续发之前的请求
          const res1 = await request<unknown, CommonResponse<unknown>>(res.config);
          if (res1.code === 200) return res1.data;
        }
      }
    }
    else if (res.data.code === 402) {
      clear()
      Message.error('你已被封禁')
      setTimeout(() => window.open('/entry', '_self'), 1000)
    }
    if (isRefreshTokenConfig(res.config)) return res;
    return res.data;
  },
  async err => {
    return Promise.reject(err);
  }
);

export default request;
