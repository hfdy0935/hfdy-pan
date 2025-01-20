import { type IMenuItem } from '@/types/home';
import {
  IconApps,
  IconCloud,
  IconDelete,
  IconFile,
  IconFileAudio,
  IconFileVideo,
  IconImage,
  IconMore,
  IconSettings,
  IconShareAlt
} from '@arco-design/web-vue/es/icon';

export const menus: IMenuItem[] = [
  {
    id: '0',
    name: '首页',
    icon: <IconCloud stroke-width={5} />,
    path: '/file/all',
    isShow: true,
    children: [
      {
        id: '0',
        name: '全部',
        icon: <IconApps />,
        path: '/file/all',
        isShow: true
      },
      {
        id: '1',
        name: '视频',
        icon: <IconFileVideo />,
        path: '/file/video',
        isShow: true
      },
      {
        id: '2',
        name: '音频',
        icon: <IconFileAudio />,
        path: '/file/audio',
        isShow: true
      },
      {
        id: '3',
        name: '图片',
        icon: <IconImage />,
        path: '/file/image',
        isShow: true
      },
      {
        id: '4',
        name: '文档',
        icon: <IconFile />,
        path: '/file/docs',
        isShow: true
      },
      {
        id: '5',
        name: '其他',
        icon: <IconMore />,
        path: '/file/others',
        isShow: true
      }
    ]
  },
  {
    id: '1',
    name: '分享',
    icon: <IconShareAlt stroke-width={5} />,
    path: '/share',
    isShow: true,
    children: [
      {
        id: '0',
        name: '分享记录',
        path: '/share',
        isShow: false
      }
    ]
  },
  {
    id: '2',
    name: '回收站',
    icon: <IconDelete stroke-width={5} />,
    path: '/recycle',
    tips: '回收站为你保存10天内删除的文件',
    isShow: true,
    children: [
      {
        id: '0',
        name: '删除的文件',
        path: '/recycle',
        isShow: false
      }
    ]
  },
  {
    id: '3',
    name: '管理',
    icon: <IconSettings stroke-width={5} />,
    path: '/admin',
    isShow: true,
    children: [
      {
        id: '0',
        name: '用户文件',
        path: '/admin/filelist',
        isShow: true
      },
      {
        id: '1',
        name: '用户管理',
        path: '/admin/userlist',
        isShow: true
      },
      {
        id: '2',
        name: '系统设置',
        path: '/admin/setting',
        isShow: true
      }
    ]
  }
];
