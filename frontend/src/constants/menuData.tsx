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
    icon: <IconCloud />,
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
        isShow: true,
        category: 'video'
      },
      {
        id: '2',
        name: '音频',
        icon: <IconFileAudio />,
        path: '/file/audio',
        isShow: true,
        category: 'audio'
      },
      {
        id: '3',
        name: '图片',
        icon: <IconImage />,
        path: '/file/image',
        isShow: true,
        category: 'image'
      },
      {
        id: '4',
        name: '文档',
        icon: <IconFile />,
        path: '/file/docs',
        isShow: true,
        category: 'docs'
      },
      {
        id: '5',
        name: '其他',
        icon: <IconMore />,
        path: '/file/others',
        isShow: true,
        category: 'others'
      }
    ]
  },
  {
    id: '1',
    name: '分享',
    icon: <IconShareAlt />,
    path: '/share',
    isShow: true
  },
  {
    id: '2',
    name: '回收站',
    icon: <IconDelete />,
    path: '/recycle',
    tips: '回收站为你保存10天内删除的文件',
    isShow: true,
  },
  {
    id: '3',
    name: '管理',
    icon: <IconSettings />,
    path: '/admin',
    isShow: true
  }
];
