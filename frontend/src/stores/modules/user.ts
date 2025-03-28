import { reqUserInfo } from '@/api/entry';
import type { LoginVO } from '@/types/entry';

const defaultUserInfo: LoginVO = {
  userId: '',
  nickName: '',
  accessToken: '',
  refreshToken: '',
  avatar: '',
  usedSpace: 0,
  totalSpace: 0,
  isVip: 0,
  maxFolderLevel: 6,
  isAdmin: 0
};

function accountStore() {
  const userInfo = ref<LoginVO>({} as LoginVO);
  const updateUserInfo = async () => {
    const resp = await reqUserInfo()
    if (resp.code === 200) userInfo.value = { ...userInfo.value, ...resp.data }
  }
  const clear = () => {
    userInfo.value = defaultUserInfo;
  };

  return {
    userInfo,
    updateUserInfo,
    clear
  };
}

export const useUserStore = defineStore('account', accountStore, {
  persist: true
});
