/**
 * 响应
 */
export interface CommonResponse<T> {
  code: number;
  message: string;
  data: T;
}
