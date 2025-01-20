// 保存到仓库中，多个组件共用


import type {
  FileCategory,
  FileListDTO,
  FileListVO,
  IFileBreadcrumbItem,
  IFileItem,
  ReqCreateFolder
} from '@/types/file';
import { reqCreateFolder, reqDeleteFile, reqFileList, reqRenameFile } from '@/api/file';
import { Message } from '@arco-design/web-vue';
import { OrderType } from '@/enums';

const fileStore = () => {
  const route = useRoute();
  // 文件列表数据
  const fileListData = ref<FileListVO>({} as FileListVO);
  // 文件夹列表
  const folderList = computed(() => fileListData.value.records.filter(f => f.mediaType === 'folder'));
  // 文件列表
  const fileList = computed(() => fileListData.value.records.filter(f => f.mediaType !== 'folder'));
  // 搜索关键词
  const keyword = ref('');
  // 文件更新时间排序
  const orderByUpdateTime = ref<OrderType>(OrderType.DEFAULT);
  // 文件大小排序
  const orderBySize = ref<OrderType>(OrderType.DEFAULT);
  // 当前页数
  const page = ref(1);
  // 文件夹面包屑导航
  const fileBreadcrumbList = ref<IFileBreadcrumbItem>([]);

  /**
   * 发送请求更新文件列表
   * @param data
   */
  const getFileListData = async (data?: Partial<FileListDTO>) => {
    try {
      const resp = await reqFileList({
        category: route.params.category as FileCategory,
        page: page.value,
        pageSize: 10,
        pid: fileListData.value.parent?.id,
        keyword: keyword.value,
        orderByUpdateTime: orderByUpdateTime.value,
        orderBySize: orderBySize.value,
        ...(data || {})
      });
      if (resp.code === 200) {
        fileListData.value = resp.data;
        // 更新面包屑导航
        // if在，就把他之后的都删了，跳转到他，else，push到最后
        const index = fileBreadcrumbList.value.findIndex(b => b.id === resp.data.parent.id);
        if (index === -1) fileBreadcrumbList.value.push(resp.data.parent);
        else fileBreadcrumbList.value = fileBreadcrumbList.value.slice(0, index + 1);
        return true;
      } else Message.error('获取文件失败，' + resp.message);
    } catch {
      Message.error('获取文件失败');
    }
  };
  // 表格左侧选中的文件id列表
  const selectedIdList = ref<string[]>([]);
  // 保存点击右键菜单时的文件
  const contextItem = ref<IFileItem>({} as IFileItem);
  // 右键选择的是文件还是文件夹
  const isContextFolder = computed(() => contextItem.value.mediaType === 'folder');
  // 是否正在编辑文件（夹）名
  const isEditing = ref(false);
  // 编辑是用于新建文件夹还是修改已有的名
  const editOp = ref<'create' | 'change'>('change');
  /**
   * 发送请求新建文件夹
   * @param data
   */
  const createFolder = async (data: ReqCreateFolder) => {
    try {
      const resp = await reqCreateFolder(data);
      if (resp.code === 200) {
        Message.success('创建成功');
        await getFileListData();
      } else {
        Message.error(resp.message);
        // 删除fileListData开头添加的记录
        fileListData.value.records.shift();
      }
    } catch {
      Message.error('创建失败');
      fileListData.value.records.shift();
    }
  };
  /**
   * 发送请求修改文件名
   * @param name
   */
  const renameItem = async (name: string) => {
    try {
      const resp = await reqRenameFile({ id: contextItem.value.id, name });
      if (resp.code === 200) {
        await getFileListData();
        Message.success('修改成功');
      } else Message.error(resp.message);
    } catch {
      Message.error('修改失败');
    }
  };
  /**
   * 发送请求删除文件/文件夹
   */
  const deleteItem = async () => {
    try {
      const resp = await reqDeleteFile(contextItem.value.id);
      if (resp.code === 200) {
        await getFileListData();
        Message.success('删除成功');
      } else Message.error(resp.message);
    } catch {
      Message.error('删除失败');
    }
  };


  return {
    fileListData, folderList, fileList,
    keyword, orderByUpdateTime, orderBySize, page,
    getFileListData,
    selectedIdList,
    contextItem, isContextFolder,
    isEditing, editOp, createFolder, renameItem, deleteItem,
    fileBreadcrumbList
  };
};

export default defineStore('fileStore', fileStore);
