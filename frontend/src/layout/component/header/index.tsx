import { reqHomeUpdatePassword, reqUpdateUserInfo } from '@/api/home';
import { useAppStore } from '@/stores/modules/app';
import { useUserStore } from '@/stores/modules/user';
import type { IUpdatePassword, UpdateUserInfoDTO } from '@/types/home';

import { IconEdit, IconPlus } from '@arco-design/web-vue/es/icon';
import { getRules } from '@/views/entry/validate';
import { Message, type FileItem } from '@arco-design/web-vue';
import UploadFileCard from './upload-file-card.vue'
import Logo from '@/component/logo.vue';

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
  const { isDark, menuIdx, subMenuIdx,textColor,bgColor,iconColor } = storeToRefs(useAppStore());
  const { toggleTheme } = useAppStore();
  const { clear } = useUserStore();
  // 对话框类型，‘’表示不显示
  const modal = ref<'updateUserInfo' | 'updatePassword' | ''>('updateUserInfo');
  const showModal = ref(false);
  const style=computed(()=>({color:textColor.value,backgroundColor:bgColor.value}))
  // 右上角菜单的icon公共属性
  const iconProps = {
    class: 'scale-125', size: 20, 
  }
  const iconStyle=computed(()=>({color:iconColor.value,backgroundColor:'transparent'}))
  // 用户信息
  const editedUserInfo = ref<UpdateUserInfoDTO>({
    nickname: userInfo.value.nickName,
    avatar: { uid: '0', url: userInfo.value.avatar }
  });
  /**
   * 更新用户信息
   * @param done
   */
  const doUpdateUserInfo = () => {
    if (!editedUserInfo.value.nickname.trim()) {
      Message.warning('昵称不能为空');
      return false;
    }
    const data = new FormData();
    data.append('nickname', editedUserInfo.value.nickname);
    data.append('avatar', editedUserInfo.value.avatar.file as File);
    reqUpdateUserInfo(data)
      .then(res => {
        if (res.code === 200) {
          Message.success('修改成功');
          userInfo.value.nickName = res.data.nickname;
          userInfo.value.avatar = res.data.avatar;
          isAvatarOk.value=true
          // 更新页面显示的头像
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
    const res = await reqHomeUpdatePassword(updatePassword.value);
      if (res.code === 200) {
        Message.success('修改成功，请重新登录');
        clear();
        await router.push('/entry');
      } else {
        Message.error(`修改失败，${res.message}`);
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
    <>
    <div class="w-screen flex items-center px-4" style={style.value}>
      <div class="flex items-center cursor-pointer select-none" onClick={ () => {
        menuIdx.value = 0;
        subMenuIdx.value = 0;
         router.push('/');
      }}>
        <Logo></Logo>
      </div>
      <div class="flex-1 flex justify-end items-center">
        <a-button shape="circle" size="large" class="mr-1" style={iconStyle.value}>
          {{
            icon: () => <>
              {
                isDark.value ? <icon-moon {...iconProps} onClick={toggleTheme} /> :
                  <icon-sun {...iconProps} onClick={toggleTheme} />
              }
            </>
          }}
        </a-button>
        <UploadFileCard/>
        <a-button shape="circle" size="large" class="mr-2">
          {{
            icon: () => <>
              {isAvatarOk.value ?
                <img src={userInfo.value.avatar} alt="" key={userInfo.value.avatar}
                     onError={() => (isAvatarOk.value = false)} /> :
                <icon-user {...iconProps} />} </>
          }}
        </a-button>
        <a-dropdown trigger="hover">
          {{
            default: () => (
              <a-button type="outline">
                <span class="max-w-32 truncate" style={{color:iconColor.value}}>欢迎，{userInfo.value.nickName}</span>
              </a-button>
            ),
            content: () => (
              <div class="w-28">
                <a-doption
                  class="w-28 flex justify-center"
                  onClick={() => {
                    modal.value = 'updateUserInfo';
                    showModal.value = true;
                  }}
                >
                  修改信息
                </a-doption>
                <a-doption
                  class="w-28 flex justify-center"
                  onClick={() => {
                    modal.value = 'updatePassword';
                    showModal.value = true;
                  }}
                >
                  修改密码
                </a-doption>
                <a-doption class="w-28 flex justify-center">
                  <div onClick={() => logoutModalShow.value = true}>退出登录</div>
                </a-doption>
              </div>
            )
          }}
        </a-dropdown>
      </div>
      <a-modal
        v-model:visible={showModal.value}
        closable={false}
        unmount-on-close
        title={modal.value === 'updateUserInfo' ? '修改信息' : '修改密码'}
        okText="提交"
        onOk={modal.value === 'updateUserInfo' ? doUpdateUserInfo : doUpdatePassword}
        onCancel={() => {
          updatePassword.value.password = '';
          updatePassword.value.newPassword = '';
        }}
      >
        {modal.value === 'updateUserInfo' && (
          <a-form model={editedUserInfo.value} rules={updateUserInfoRules}>
            <a-form-item field="nickname" label="用户名">
              <a-input v-model={editedUserInfo.value.nickname} placeholder="用户名" allowClear></a-input>
            </a-form-item>
            <a-form-item field="avatar" label="头像">
              <a-upload
                show-file-list={false}
                onChange={(_:any, currentFile:FileItem) => {
                  editedUserInfo.value.avatar = currentFile;
                }}
                onProgress={(currentFile:FileItem) => (editedUserInfo.value.avatar = currentFile)}
              >
                {{
                  'upload-button': () => (
                    <div
                      class={`arco-upload-list-item${editedUserInfo.value.avatar?.status === 'error' ? ' arco-upload-list-item-error' : ''}`}
                    >
                      {editedUserInfo.value.avatar.url ? (
                        <div class="arco-upload-list-picture custom-upload-avatar">
                          <img src={editedUserInfo.value.avatar.url} alt="" />
                          <div class="arco-upload-list-picture-mask">
                            <IconEdit />
                          </div>
                          {((editedUserInfo.value.avatar.status === 'uploading' && editedUserInfo.value.avatar.percent) ||
                            101 < 100) && (
                            <a-progress
                              percent={editedUserInfo.value.avatar.percent}
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
              </a-upload>
            </a-form-item>
          </a-form>
        )}
        {
          modal.value === 'updatePassword' && (
            <a-form model={updatePassword.value}
                  rules={computed(() => getUpdatePasswordRules(updatePassword.value.password)).value}>
              <a-form-item field="password" label="输入旧密码">
                <a-input-password v-model={updatePassword.value.password}></a-input-password>
              </a-form-item>
              <a-form-item field="newPassword" label="确认新密码">
                <a-input-password v-model={updatePassword.value.newPassword}></a-input-password>
              </a-form-item>
            </a-form>
          )
        }
      </a-modal>
      <a-modal v-model:visible={logoutModalShow.value} closable={false} unmount-on-close
             onOk={async () => {
               await doLogout();
               logoutModalShow.value = false;
             }} onCancel={() => logoutModalShow.value = false}
             ok-button-props={{ status: 'danger' }}>
        <div class="text-lg">确定要退出登录吗？</div>
      </a-modal>
    </div>
    <div class='w-full h-[1px] bg-[#E5E6EB]'></div>
    </>
  );
}

export default defineComponent(HHeader, {
  props: []
});

