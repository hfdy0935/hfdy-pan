import { reqRefreshToken } from '@/api/entry';
import { useUserStore } from '@/stores';
import type { CommonResponse } from '@/types/common';
import axios, { AxiosHeaders } from 'axios';
import { isRefreshTokenConfig } from './is';

const request = axios.create({
  baseURL: '/api',
  timeout: 5000
});

request.interceptors.request.use(config => {
  const { userInfo } = storeToRefs(useUserStore());
  // 优先带accessToken
  config.headers.Authorization = userInfo.value.accessToken || userInfo.value.refreshToken;
  return config;
});

request.interceptors.response.use(
  async res => {
    if (res.data.code === 401) {
      const { userInfo } = storeToRefs(useUserStore());
      const { clear } = useUserStore();
      // 如果是refreshToken失效
      console.log(res.config.url);

      if (isRefreshTokenConfig(res.config)) {
        clear();
        useRouter().push('/entry');
      } else {
        // accessToken失效，刷新
        userInfo.value.accessToken = '';
        try {
          const refreshRes = await reqRefreshToken();
          if (refreshRes.data.code === 200) {
            const newAccessToken = (refreshRes.headers as AxiosHeaders).get('Authorization') as string;
            userInfo.value.accessToken = newAccessToken;
            // 再继续发之前的请求
            const res1 = await request<unknown, CommonResponse<unknown>>(res.config);
            if (res1.code === 200) return res1.data;
          }
        } catch {
          // refreshToken也过期了
          clear();
          useRouter().push('/entry');
        }
      }
    }
    if (isRefreshTokenConfig(res.config)) return res;
    return res.data;
  },
  async err => {
    return Promise.reject(err);
  }
);

export default request;
