import type { UpdateUserDTO, UserListVO } from "@/types/admin";
import type { CommonResponse } from "@/types/common";
import request from "@/utils/request";

/**
 * 管理员获取用户列表
 * @returns 
 */
export function reqUserList() {
    return request<any, CommonResponse<UserListVO[]>>({
        url: '/admin/users',
        method: 'GET'
    })
}
/**
 * 管理员删除用户
 * @returns 
 */
export function reqDeleteUser(id: string) {
    return request<any, CommonResponse<any>>({
        url: '/admin/users',
        method: 'DELETE',
        params: { id }
    })
}
/**
 * 管理员修改用户
 * @returns 
 */
export function reqUpdateUser(data: UpdateUserDTO) {
    return request<any, CommonResponse<any>>({
        url: '/admin/users',
        method: 'PUT',
        data
    })
}