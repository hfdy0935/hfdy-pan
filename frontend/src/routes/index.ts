import { useUserStore } from '@/stores/modules/user';
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
          component: () => import('@/views/share/manage-share-file/index.vue')
        },
        {
          path: '/recycle',
          name: 'recycle',
          component: () => import('@/views/recycle/index.vue')
        },
        {
          path: '/admin',
          name: 'admin',
          component: () => import('@/views/admin/index.vue'),
          meta: {
            hasAdminPermission: true
          }
        }
      ]
    },
    {
      path: '/share/:shareId',
      name: 'getShareFile',
      component: () => import('@/views/share/get-share-file/index.vue')
    },
  ]
});

router.beforeEach((to, from) => {
  const { userInfo } = storeToRefs(useUserStore());
  if (/share\/.*?/.test(to.path)) return true
  if (to.path !== '/entry' && !userInfo.value.accessToken) {
    Message.warning('登录已过期或没有权限，请重新登录');
    return '/entry';
  } else if (to.path === '/entry' && userInfo.value.accessToken) {
    // 如果有token，去不了登录页
    return false;
  } else if (to.path === '/admin' && !userInfo.value.isAdmin) return false
  return true;
});

export default router;
