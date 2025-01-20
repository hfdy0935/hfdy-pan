import type { LoginVO } from '@/types/entry';

const defaultUserInfo: LoginVO = {
  userId: '',
  nickName: '',
  accessToken: '',
  refreshToken: '',
  avatar: '',
  usedSpace: 0,
  totalSpace: 0
};

function accountStore() {
  const userInfo = ref<LoginVO>({} as LoginVO);
  const clear = () => {
    userInfo.value = defaultUserInfo;
  };

  return {
    userInfo,
    clear
  };
}

export default defineStore('account', accountStore, {
  persist: true
});
