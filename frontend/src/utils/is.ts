import type { AxiosRequestConfig } from 'axios';

/**
 * 判断配置是不是请求刷新token的
 * @param config
 * @returns
 */
export function isRefreshTokenConfig(config: AxiosRequestConfig) {
    return config.url === '/refreshToken';
}
