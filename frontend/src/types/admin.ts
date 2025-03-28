export interface UserListVO {
    id: string;
    nickName: string;
    email: string;
    avatar: string;
    registerTime: string;
    lastLoginTime: string;
    status: 0 | 1;
    usedSpace: number;
    totalSpace: number;
    isVip: 0 | 1;
    uploadLimit: number;
    downloadSpeed: number;
}


export interface UpdateUserDTO {
    status: 0 | 1;
    isVip: 0 | 1;
    uploadLimit: number;
    downloadSpeed: number;
    userId: string
}