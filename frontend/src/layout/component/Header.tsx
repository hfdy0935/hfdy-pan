import { reqHomeUpdatePassword, reqUpdateUserInfo } from '@/api/home';
import logo from '@/assets/logo.svg';
import { useAppStore, useUserStore } from '@/stores';
import type { IUpdatePassword, UpdateUserInfoDTO } from '@/types/home';
import {
  Avatar,
  Button,
  Doption,
  Dropdown,
  Form,
  FormItem,
  Input,
  InputPassword,
  Message,
  Modal,
  Progress,
  Upload
} from '@arco-design/web-vue';
import { IconEdit, IconPlus, IconUser, IconSwap, IconMoon, IconSun } from '@arco-design/web-vue/es/icon';
import { getRules } from '@/views/entry/validate';

const updateUserInfoRules = {
  nickname: [
    {
      required: true,
      message: '昵称不能为空'
    }
  ]
};
const getUpdatePasswordRules = (password: string) => {
  const rules = getRules(password);
  return {
    password: rules.password,
    newPassword: rules.password
  };
};

function HHeader() {
  const router = useRouter();
  const isAvatarOk = ref(true); // 头像是否加载成功
  const { userInfo } = storeToRefs(useUserStore());
  const { isDark, menuIdx, subMenuIdx } = storeToRefs(useAppStore());
  const { setDark, setLight } = useAppStore();
  const { clear } = useUserStore();
  // 对话框类型，‘’表示不显示
  const modal = ref<'updateUserInfo' | 'updatePassword' | ''>('updateUserInfo');
  const showModal = ref(false);
  // 右上角菜单的icon公共属性
  const iconProps = {
    class: 'scale-125', size: 20, style: { color: '#3491FA' }
  };
  // 用户信息
  const updateUserInfo = ref<UpdateUserInfoDTO>({
    nickname: userInfo.value.nickName,
    avatar: { uid: '0', url: userInfo.value.avatar }
  });
  /**
   * 更新用户信息
   * @param done
   */
  const doUpdateUserInfo = () => {
    if (!updateUserInfo.value.nickname.trim()) {
      Message.warning('昵称不能为空');
      return false;
    }
    const data = new FormData();
    data.append('nickname', updateUserInfo.value.nickname);
    data.append('avatar', updateUserInfo.value.avatar.file as File);
    reqUpdateUserInfo(data)
      .then(res => {
        if (res.code === 200) {
          Message.success('修改成功');
          userInfo.value.nickName = res.data.nickname;
          userInfo.value.avatar = res.data.avatar;
          // 更新页面显示的头像
          console.log(userInfo.value.avatar);
        } else Message.error(`修改失败，${res.message}`);
      })
      .catch(() => {
        Message.error('修改失败');
      });
  };
  // 密码
  const updatePassword = ref<IUpdatePassword>({
    password: '',
    newPassword: ''
  });
  /**
   * 修改密码
   * @param done
   */
  const doUpdatePassword = async () => {
    if (!updatePassword.value.password.trim() || !updatePassword.value.newPassword.trim()) {
      Message.warning('密码不能为空');
      return;
    }
    try {
      const res = await reqHomeUpdatePassword(updatePassword.value);
      if (res.code === 200) {
        Message.success('修改成功，请重新登录');
        clear();
        await router.push('/entry');
      } else {
        Message.error(`修改失败，${res.message}`);
      }
    } catch {
      Message.error('修改失败');
    }
  };
  /**
   * 退出登录
   */
  const logoutModalShow = ref(false);
  const doLogout = async () => {
    // 先清缓存再跳转
    clear();
    await router.replace('/entry');
    Message.success('退出登录成功');
  };

  return () => (
    <div class="w-screen h-20 flex items-center shadow-md px-4">
      <div class="flex items-center cursor-pointer select-none" onClick={async () => {
        menuIdx.value = 0;
        subMenuIdx.value = 0;
        await router.push('/');
      }}>
        <img src={logo} alt="hfdy云盘" class="w-16 mr-2" />
        <h1 class="hidden sm:block">hfdy云盘</h1>
      </div>
      <div class="flex-1 flex justify-end items-center">
        <Button shape="circle" size="large" class="mr-3">
          {{
            icon: () => <>
              {
                isDark.value ? <IconMoon {...iconProps} onClick={setLight} /> :
                  <IconSun {...iconProps} onClick={setDark} />
              }
            </>
          }}
        </Button>
        <Button shape="circle" size="large" class="mr-3">
          {{
            icon: () => <IconSwap rotate={90} {...iconProps} />
          }}
        </Button>
        <Button shape="circle" size="large" class="mr-3">
          {{
            icon: () => <>
              {isAvatarOk.value ?
                <img src={userInfo.value.avatar} alt="" key={userInfo.value.avatar}
                     onError={() => (isAvatarOk.value = false)} /> :
                <IconUser {...iconProps} />} </>
          }}
        </Button>
        <Dropdown trigger="hover">
          {{
            default: () => (
              <Button type="outline">
                <span class="max-w-32 px-2 truncate">欢迎，{userInfo.value.nickName}</span>
              </Button>
            ),
            content: () => (
              <div class="w-28">
                <Doption class="w-28 flex justify-center">个人中心</Doption>
                <Doption
                  class="w-28 flex justify-center"
                  onClick={() => {
                    modal.value = 'updateUserInfo';
                    showModal.value = true;
                  }}
                >
                  修改信息
                </Doption>
                <Doption
                  class="w-28 flex justify-center"
                  onClick={() => {
                    modal.value = 'updatePassword';
                    showModal.value = true;
                  }}
                >
                  修改密码
                </Doption>
                <Doption class="w-28 flex justify-center">
                  <div onClick={() => logoutModalShow.value = true}>退出登录</div>
                </Doption>

              </div>
            )
          }}
        </Dropdown>
      </div>
      <Modal
        v-model:visible={showModal.value}
        closable={false}
        title={modal.value === 'updateUserInfo' ? '修改信息' : '修改密码'}
        okText="提交"
        onOk={modal.value === 'updateUserInfo' ? doUpdateUserInfo : doUpdatePassword}
        onCancel={() => {
          updatePassword.value.password = '';
          updatePassword.value.newPassword = '';
        }}
      >
        {modal.value === 'updateUserInfo' && (
          <Form model={updateUserInfo.value} rules={updateUserInfoRules}>
            <FormItem field="nickname" label="用户名">
              <Input v-model={updateUserInfo.value.nickname} placeholder="用户名" allowClear></Input>
            </FormItem>
            <FormItem field="avatar" label="头像">
              <Upload
                show-file-list={false}
                onChange={(_, currentFile) => {
                  updateUserInfo.value.avatar = currentFile;
                }}
                onProgress={currentFile => (updateUserInfo.value.avatar = currentFile)}
              >
                {{
                  'upload-button': () => (
                    <div
                      class={`arco-upload-list-item${updateUserInfo.value.avatar?.status === 'error' ? ' arco-upload-list-item-error' : ''}`}
                    >
                      {updateUserInfo.value.avatar.url ? (
                        <div class="arco-upload-list-picture custom-upload-avatar">
                          <img src={updateUserInfo.value.avatar.url} alt="" />
                          <div class="arco-upload-list-picture-mask">
                            <IconEdit />
                          </div>
                          {((updateUserInfo.value.avatar.status === 'uploading' && updateUserInfo.value.avatar.percent) ||
                            101 < 100) && (
                            <Progress
                              percent={updateUserInfo.value.avatar.percent}
                              type="circle"
                              size="mini"
                              style={{
                                position: 'absolute',
                                left: '50%',
                                top: '50%',
                                transform: 'translateX(-50%) translateY(-50%)'
                              }}
                            />
                          )}
                        </div>
                      ) : (
                        <div class="arco-upload-picture-card">
                          <div class="arco-upload-picture-card-text">
                            <IconPlus />
                            <div style="margin-top: 10px; font-weight: 600">Upload</div>
                          </div>
                        </div>
                      )}
                    </div>
                  )
                }}
              </Upload>
            </FormItem>
          </Form>
        )}
        {
          modal.value === 'updatePassword' && (
            <Form model={updatePassword.value}
                  rules={computed(() => getUpdatePasswordRules(updatePassword.value.password)).value}>
              <FormItem field="password" label="输入旧密码">
                <InputPassword v-model={updatePassword.value.password}></InputPassword>
              </FormItem>
              <FormItem field="newPassword" label="确认新密码">
                <InputPassword v-model={updatePassword.value.newPassword}></InputPassword>
              </FormItem>
            </Form>
          )
        }
      </Modal>
      <Modal v-model:visible={logoutModalShow.value} closable={false}
             onOk={async () => {
               await doLogout();
               logoutModalShow.value = false;
             }} onCancel={() => logoutModalShow.value = false}
             ok-button-props={{ status: 'danger' }}>
        <div class="text-lg">确定要退出登录吗？</div>
      </Modal>
    </div>
  );
}

export default defineComponent(HHeader, {
  props: []
});

