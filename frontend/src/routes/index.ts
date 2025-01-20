import { useUserStore } from '@/stores';
import { createRouter, createWebHistory } from 'vue-router';
import { Message } from '@arco-design/web-vue';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/entry',
      name: '登录注册忘记密码',
      component: () => import('@/views/entry/index.vue')
    },
    {
      path: '',
      name: '主界面1级',
      redirect: '/file/all',
      component: () => import('@/layout/index.vue'),
      children: [
        {
          path: '/file/:category',
          name: '文件界面',
          component: () => import('@/views/file/index.vue')
        },
        {
          path: '/share',
          name: 'share',
          component: () => import('@/views/share')
        },
        {
          path: '/recycle',
          name: 'recycle',
          component: () => import('@/views/recycle')
        },
        {
          path: '/admin',
          name: 'admin',
          redirect: '/admin/setting',
          children: [
            {
              path: '/admin/filelist',
              name: 'fileList',
              component: () => import('@/views/admin/FileList')
            },
            {
              path: '/admin/userlist',
              name: 'userList',
              component: () => import('@/views/admin/UserList')
            },
            {
              path: '/admin/setting',
              name: 'setting',
              component: () => import('@/views/admin/Setting')
            }
          ]
        }
      ]
    }
  ]
});

router.beforeEach((to, from) => {
  const { userInfo } = storeToRefs(useUserStore());
  if (to.path !== '/entry' && !userInfo.value.accessToken) {
    Message.warning('登录失效，请重新登录');
    return '/entry';
  } else if (to.path === '/entry' && userInfo.value.accessToken) {
    // 如果有token，去不了登录页
    return '/file/all';
  }
  return true;
});

export default router;
